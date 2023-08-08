package net.buj.loader.tasks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import net.buj.loader.BadBinDiff;
import net.buj.loader.Failible;
import net.buj.loader.RosepadLoadingWindow;
import net.buj.loader.Task;
import net.buj.rml.Environment;
import net.buj.rml.annotations.NotNull;
import net.buj.rml.annotations.Nullable;
import net.buj.rml.loader.GameJar;

public class CreateGameJarTask extends Task<GameJar> {
    private final DownloadOriginalJarTask downloadTask;
    private final Environment environment;
    private final Path versionPath;

    public CreateGameJarTask(DownloadOriginalJarTask downloadTask, Environment environment, Path versionPath) {
        this.downloadTask = downloadTask;
        this.environment = environment;
        this.versionPath = versionPath;
    }

    private InputStream getResource(String str) throws NullPointerException {
        return Objects.requireNonNull(
            getClass().getClassLoader().getResourceAsStream(str));
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

    @Override
    protected @NotNull GameJar run(RosepadLoadingWindow log) throws Failible {
        Path originalPath = Failible.$(() -> downloadTask.await(log));
        Path patchedPath = this.versionPath.resolve("minecraft.jar");
        Path hashPath = this.versionPath.resolve("hash.txt");

        log.setTask("Patching jar");
        log.setStep("Verifying");

        @Nullable Integer hash = Failible.$(() -> Integer.parseInt(Files.readAllLines(hashPath).get(0)), null);
        byte[] patch = Failible.$(() -> readAllBytes(getResource(environment == Environment.CLIENT ? "client.diff" : "server.diff")));

        if (hash != null && Arrays.hashCode(patch) == hash) {
            System.out.println("Detected matching hashes, skipping patching...");
            log.setStep("Done");
            return new GameJar(patchedPath);
        }

        log.setStep("Patching");
        Failible.$(() -> Files.write(patchedPath, BadBinDiff.patch(Files.readAllBytes(originalPath), patch)));
        log.setStep("Done");

        return new GameJar(patchedPath);
    }
}
