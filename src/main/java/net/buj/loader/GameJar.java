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
    public RosepadLoadingWindow window;

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

    public void fetch() throws IOException, URISyntaxException {
        if (window != null)
            window.setStep("Patching jar...");

        if (window != null)
            window.setTask("Downloading original jar...");

        Path orig = home.resolve(".orig-mc.jar");
        Path resulting = home.resolve("minecraft.jar");
        Path hashPath = home.resolve(".hash");

        System.out.println("Jar located at: " + resulting.toAbsolutePath());

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

        if (window != null)
            window.setTask("Patching jar...");

        byte[] bytes = Files.readAllBytes(orig);
        byte[] patchedBytes = BadBinDiff.diff(bytes, patch);

        Files.write(resulting, patchedBytes);
        Files.write(hashPath, ("" + Arrays.hashCode(patch)).getBytes());
    }

    public URL getURL() throws MalformedURLException {
        Path deobfuscated = home.resolve("minecraft.jar");
        return deobfuscated.toUri().toURL();
    }
}
