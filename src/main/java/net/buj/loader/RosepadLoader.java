package net.buj.loader;

import net.buj.rml.Environment;
import net.minecraft.client.MinecraftApplet;

import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

        for (String arg : args) {
            System.out.println(arg);
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

        ArgsParser parser = new ArgsParser(args);

        environment = env;
        this.args = args;
        this.home = home;

        {
            List<String> l = parser.param("gameDir");
            if (!l.isEmpty()) {
                this.home = Paths.get(l.get(0));
            }
        }

        if (env == Environment.CLIENT && applet == null) {
            applet = new MinecraftApplet();
            Frame frame = new LauncherWindow(self -> self.dispose());
            frame.add(applet);
            Stub stub = new Stub(applet);
            stub.setParameter("username", parser.paramOr("username", parser.arg(0)));
            stub.setParameter("sessionid", parser.paramOr("accessToken", parser.arg(0)));
            applet.setStub(stub);
            frame.setVisible(true);
            frame.setResizable(true);
            frame.setMinimumSize(new Dimension(400, 300));
            frame.setPreferredSize(new Dimension(400, 300));
            frame.setName("Rosepad Loader Dev Build");
        }

        RosepadLoadingWindow loadingWindow = new RosepadLoadingWindow(applet);
        new Thread(loadingWindow, "Rosepad loading window thread").start();

        new CheckLWJGL(loadingWindow, home).run();

        RosepadMainThread thread = new RosepadMainThread(loadingWindow, this);
        thread.run();
    }
}
