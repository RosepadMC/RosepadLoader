package net.buj.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CheckLWJGL {
    private final RosepadLoadingWindow window;
    private final Path home;

    public CheckLWJGL(RosepadLoadingWindow window, Path home) {
        this.window = window;
        this.home = home;
    }

    private boolean isFaulty() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return true;
        }

        return false;
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private void unzip(Path source, Path dest) throws IOException {
        String fileZip = source.toAbsolutePath().toString();
        File destDir = dest.toAbsolutePath().toFile();

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    private void installLWJGL() {
        window.setTask("Downloading LWJGL...");
        if (!home.resolve("natives/lwjgl.dll").toFile().exists()) {
            try {
                Files.createDirectories(home.resolve("natives"));
                Files.copy(
                           new URL("https", "github.com", "RosepadMC/Static/releases/download/lwjgl-2.9.1/windows.zip")
                           .openStream(),
                           home.resolve("natives/archive.zip")
                           );
                unzip(
                      home.resolve("natives/archive.zip"),
                      home.resolve("natives")
                      );
                Files.delete(home.resolve("natives/archive.zip"));
            } catch (Exception err) {
                System.err.println("Error while downloading LWJGL");
                err.printStackTrace();
            }
        }
        try {
            System.setProperty(
                               "java.library.path",
                               home.resolve("natives").toAbsolutePath().toString() + ":" +
                               System.getProperty("java.library.path")
                               );
            final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
            sysPathsField.set(null, null);
        } catch (Exception err) {
            System.err.println("Error while modifying library path");
            err.printStackTrace();
        }
    }

    public void run() {
        if (!isFaulty()) return;
        installLWJGL();
    }
}
