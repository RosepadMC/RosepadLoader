package net.buj.loader;

import net.buj.rml.Environment;
import net.minecraft.client.MinecraftApplet;

import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RosepadLoader {
    private static boolean dirtyOneMain = false;
    private MinecraftApplet applet = null;

    public Environment environment;
    public String[] args;
    public Path home;

    public MinecraftApplet getApplet() {
        return applet;
    }

    public RosepadLoader() {}

    public RosepadLoader(MinecraftApplet applet) {
        this.applet = applet;
    }

    @SuppressWarnings("deprecation")
    public void main(Environment env, String[] args, Path home) {
        if (!dirtyOneMain) {
            dirtyOneMain = true;
        }
        else {
            System.err.println("Multiple calls to main");
            return;
        }

        try {
            Path logFile = home.resolve("logs/latest.log");
            Files.deleteIfExists(logFile);
            Files.createDirectories(logFile.getParent());
            Files.createFile(logFile);
            OutputStream fileStream = Files.newOutputStream(logFile);
            OutputStream stdout = new MultiOutputStream(new OutputStream[] { fileStream, System.out });
            OutputStream stderr = new MultiOutputStream(new OutputStream[] { fileStream, System.err });
            System.setOut(new PrintStream(stdout));
            System.setErr(new PrintStream(stderr));
        } catch (Exception e) {
            System.err.println("Failed to set logger");
            e.printStackTrace();
        }

        ArgsParser parser = (new ArgsParser.Builder())
            .param("gameDir", true)
            .param("assetsDir", true)
            .param("username", true)
            .param("accessToken", true)
            .build(args);

        environment = env;
        this.args = args;
        this.home = home;

        {
            String homePath = parser.arg("gameDir");
            if (homePath != null) {
                this.home = Paths.get(home.toUri());
            }
        }

        if (env == Environment.CLIENT && applet == null) {
            applet = new MinecraftApplet();
            Frame frame = new LauncherWindow(Window::dispose);
            frame.add(applet);
            Stub stub = new Stub(applet);
            stub.setParameter("username", parser.or("username", parser.arg(0)));
            stub.setParameter("sessionid", parser.or("accessToken", parser.arg(0)));
            applet.setStub(stub);
            frame.setVisible(true);
            frame.setResizable(true);
            frame.setMinimumSize(new Dimension(400, 300));
            frame.setPreferredSize(new Dimension(400, 300));
            frame.setName("Rosepad Loader Dev Build");
        }

        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        RosepadLoadingWindow loadingWindow = new RosepadLoadingWindow(applet);
        new Thread(loadingWindow, "Rosepad loading window thread").start();

        RosepadMainThread thread = new RosepadMainThread(loadingWindow, this);
        thread.start();
    }
}
