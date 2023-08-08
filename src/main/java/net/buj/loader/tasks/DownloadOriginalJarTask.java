package net.buj.loader.tasks;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

import net.buj.loader.Failible;
import net.buj.loader.R;
import net.buj.loader.RosepadLoadingWindow;
import net.buj.loader.Task;
import net.buj.rml.Environment;
import net.buj.rml.annotations.NotNull;

public class DownloadOriginalJarTask extends Task<Path> {
    private final Environment environment;
    private final Path versionPath;

    public DownloadOriginalJarTask(Environment environment, Path versionPath) {
        this.environment = environment;
        this.versionPath = versionPath;
    }

    @Override
    protected @NotNull Path run(RosepadLoadingWindow log) throws Failible {
        Path originalPath = this.versionPath.resolve("original.jar");

        if (!Files.exists(originalPath)) {
            log.setTask("Preparing jar");
            log.setStep("Downloading");
            long time = System.currentTimeMillis();

            URLConnection url = Failible.$(() -> new URL(
                environment == Environment.CLIENT
                    ? R.CLIENT_DOWNLOAD_URL
                    : R.SERVER_DOWNLOAD_URL
                ).openConnection());

            final long contentLength = url.getContentLengthLong();

            Failible.$(url::connect);

            try (BufferedInputStream in = new BufferedInputStream(url.getInputStream())) {
                Failible.ok(() -> originalPath.toFile().delete());
                FileOutputStream out = new FileOutputStream(originalPath.toFile());

                byte[] buffer = new byte[32767];
                int count;
                long total = 0;

                while ((count = in.read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, count);
                    total += count;
                    long time2 = System.currentTimeMillis();
                    if (time2 - time > 1000) {
                        log.setStep("Downloading", (int) ((float) total / contentLength * 100), 100);
                        time = time2;
                    }
                }

                log.setStep("Downloading", 100, 100);
                log.setStep("Done");
                out.close();
            } catch (Exception e) {
                Failible.$(e);
            }
        }

        return originalPath;
    }
}
