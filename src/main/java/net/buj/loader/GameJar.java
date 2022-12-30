package net.buj.loader;

import net.buj.rml.Environment;
import net.fabricmc.tinyremapper.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.ZipOutputStream;

public class GameJar {

    private final Environment environment;
    private final Path home;

    public GameJar(Environment env, Path home) throws IOException {
        environment = env;
        this.home = home;

        Files.createDirectories(home);
    }

    private InputStream getResource(String str) {
        try {
            return Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(str));
        } catch (NullPointerException e) {
            throw new RuntimeException("Failed to load resource " + str, e);
        }
    }

    private byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[32767];

        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    // Shamelessly copied from https://stackoverflow.com/a/50669964
    public static void updateJarFile(File srcJarFile, boolean update, File ...filesToAdd) throws IOException {

        File tmpJarFile = File.createTempFile("tempJar", ".tmp");
        JarFile jarFile = new JarFile(srcJarFile);
        boolean jarUpdated = false;
        List<String> fileNames = new ArrayList<String>();

        try {
            JarOutputStream tempJarOutputStream = new JarOutputStream(Files.newOutputStream(tmpJarFile.toPath()));
            try {
                // Added the new files to the jar.
                for (int i = 0; i < filesToAdd.length; i++) {
                    File file = filesToAdd[i];
                    FileInputStream fis = new FileInputStream(file);
                    try {
                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;
                        JarEntry entry = new JarEntry(file.getName());
                        fileNames.add(entry.getName());
                        tempJarOutputStream.putNextEntry(entry);
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            tempJarOutputStream.write(buffer, 0, bytesRead);
                        }

                        // System.out.println(entry.getName() + " added.");
                    } finally {
                        fis.close();
                    }
                }

                // Copy original jar file to the temporary one.
                Enumeration<?> jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = (JarEntry) jarEntries.nextElement();
                    /*
                     * Ignore classes from the original jar which are being
                     * replaced
                     */
                    String[] fileNameArray = fileNames
                        .toArray(new String[0]);
                    Arrays.sort(fileNameArray);// required for binary search
                    if (Arrays.binarySearch(fileNameArray, entry.getName()) < 0) {
                        InputStream entryInputStream = jarFile
                            .getInputStream(entry);
                        tempJarOutputStream.putNextEntry(entry);
                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;
                        while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                            tempJarOutputStream.write(buffer, 0, bytesRead);
                        }
                    } else if (!update) {
                        throw new IOException(
                            "Jar Update Aborted: Entry "
                                + entry.getName()
                                + " could not be added to the jar"
                                + " file because it already exists and the update parameter was false");
                    }
                }

                jarUpdated = true;
            } catch (Exception ex) {
                System.err.println("Unable to update jar file");
                tempJarOutputStream.putNextEntry(new JarEntry("stub"));
            } finally {
                tempJarOutputStream.close();
            }

        } finally {
            jarFile.close();
            // System.out.println(srcJarFile.getAbsolutePath() + " closed.");

            if (!jarUpdated) {
                tmpJarFile.delete();
            }
        }

        if (jarUpdated) {
            srcJarFile.delete();
            tmpJarFile.renameTo(srcJarFile);
            // System.out.println(srcJarFile.getAbsolutePath() + " updated.");
        }
    }

    public void fetch() throws IOException, URISyntaxException {
        Path orig = home.resolve(".orig-mc.jar");
        Path patched = home.resolve(".minecraft-patched.jar");
        Path deobfuscated = home.resolve(".minecraft-deobfuscated.jar");
        Path resulting = home.resolve("minecraft.jar");
        Path hashPath = home.resolve(".hash");

        System.out.println("Jar located at: " + deobfuscated.toAbsolutePath());

        int hash = 0;
        if (Files.exists(hashPath)) {
            try {
                hash = Integer.parseInt(Files.readAllLines(hashPath).get(0));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Files.delete(hashPath);
            }
        }

        byte[] patch = readAllBytes(getResource(environment == Environment.CLIENT ? "client.diff" : "server.diff"));
        if (Arrays.hashCode(patch) == hash) {
            System.out.println("Detected matching hashes, skipping patching...");
            return;
        }

        System.out.println("Downloading original game jar...");
        if (!Files.exists(orig))
            Files.copy(
                new URI(
                    environment == Environment.CLIENT
                        ? R.CLIENT_DOWNLOAD_URL
                        : R.SERVER_DOWNLOAD_URL
                ).toURL().openStream(),
                orig,
                StandardCopyOption.REPLACE_EXISTING
            );

        System.out.println("Patching jar...");
        byte[] bytes = Files.readAllBytes(orig);
        byte[] patchedBytes = BadBinDiff.diff(bytes, patch);

        Files.write(patched, patchedBytes);
        Files.write(hashPath, ("" + Arrays.hashCode(patch)).getBytes());

        System.out.println("Deobfuscating...");

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(
                getResource(environment == Environment.CLIENT ? "client.tiny2" : "server_deobf.tiny")
            )
        );

        Files.deleteIfExists(deobfuscated);

        TinyRemapper remapper = TinyRemapper.newRemapper()
            .withMappings(TinyUtils.createTinyMappingProvider(
                reader,
                environment == Environment.CLIENT ? "intermediary" : "official",
                "named"
            ))
            .renameInvalidLocals(true)
            .rebuildSourceFilenames(true)
            .build();

        OutputConsumerPath path = new OutputConsumerPath.Builder(deobfuscated).build();
        remapper.readClassPath(patched);
        remapper.readInputs(patched);
        remapper.apply(path);
        path.close();
        remapper.finish();
        reader.close();

        // Merge jars

        ZipOutputStream resultingStream = new ZipOutputStream(Files.newOutputStream(resulting));
        byte[] buffer = new byte[1024];

        { // Resources
            JarFile jar = new JarFile(patched.toFile());
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) continue;
                resultingStream.putNextEntry(entry);
                InputStream stream = jar.getInputStream(entry);
                int read;
                while ((read = stream.read(buffer)) != -1) {
                    if (read == 0) continue;
                    resultingStream.write(buffer, 0, read);
                }
                stream.close();
            }
            jar.close();
        }

        { // Code
            JarFile jar = new JarFile(deobfuscated.toFile());
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                resultingStream.putNextEntry(entry);
                InputStream stream = jar.getInputStream(entry);
                int read;
                while ((read = stream.read(buffer)) != -1) {
                    if (read == 0) continue;
                    resultingStream.write(buffer, 0, read);
                }
                stream.close();
            }
            jar.close();
        }

        resultingStream.close();

        try {
            Files.delete(patched);
            Files.delete(deobfuscated);
        } catch (Exception ignored) {}
    }

    public URL getURL() throws MalformedURLException {
        Path deobfuscated = home.resolve("minecraft.jar");
        return deobfuscated.toUri().toURL();
    }
}
