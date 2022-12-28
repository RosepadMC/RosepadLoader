package net.buj.loader;

import net.buj.rml.Environment;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
        return Objects.requireNonNull(
            getClass().getClassLoader().getResourceAsStream(str));
    }

    private byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    protected void repairZipFile(String file) throws IOException { // Stolen from stackoverflow
        File repairZipFile = new File(file+".repair");
        ZipFile zipFile = new ZipFile(file);
        Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
        InputStream zis;
        ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(repairZipFile.toPath()));
        byte[] b = new byte[1024];
        while(zipFileEntries.hasMoreElements()){
            ZipEntry zipEntry = zipFileEntries.nextElement();
            zos.putNextEntry(zipEntry);
            zis = zipFile.getInputStream(zipEntry);
            int n = zis.read(b);
            while(n>=0) {
                zos.write(b, 0, n);
                n = zis.read(b);
            }
            zis.close();
            zos.closeEntry();
        }
        zipFile.close();
        zos.flush();
        zos.close();
        Files.move(repairZipFile.toPath(), (new File(file)).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public void fetch() throws IOException, URISyntaxException {
        Path orig = home.resolve(".orig-mc.jar");
        Path patched = home.resolve(".minecraft-patched.jar");
        Path deobfuscated = home.resolve("minecraft.jar");
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

        Files.write(deobfuscated, patchedBytes);
        Files.write(hashPath, ("" + Arrays.hashCode(patch)).getBytes());

        /* One day it'll be used

        System.out.println("Deobfuscating...");

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(
                getResource(environment == Environment.CLIENT ? "client.tiny2" : "server_deobf.tiny")
            )
        );

        TinyRemapper remapper = TinyRemapper.newRemapper()
            .withMappings(TinyUtils.createTinyMappingProvider(
                reader,
                environment == Environment.CLIENT ? "intermediary" : "official",
                "named"
            ))
            .build();
        remapper.readInputs(patched);
        remapper.apply(new OutputConsumerPath.Builder(deobfuscated).build());
        remapper.finish();

        try {
            //Files.delete(patched);
        } catch (Exception ignored) {}

        reader.close();

         */
    }

    public URL getURL() throws MalformedURLException {
        Path deobfuscated = home.resolve("minecraft.jar");
        return deobfuscated.toUri().toURL();
    }
}
