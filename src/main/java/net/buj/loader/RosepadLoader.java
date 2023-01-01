package net.buj.loader;

import net.buj.rml.Environment;
import net.minecraft.client.MinecraftApplet;
import org.lwjgl.Sys;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

    public void main(Environment env, String[] args, Path home) {
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

        if (!dirtyOneMain) {
            dirtyOneMain = true;
        }
        else throw new RuntimeException("Calling main multiple times");

        {
            StringBuilder builder = new StringBuilder();
            for (String arg : args) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(arg);
            }
            System.out.println(builder);
        }

        if (env == Environment.CLIENT && applet == null) {
            Frame frame = new LauncherWindow();
            frame.add((applet = new MinecraftApplet()));
            Stub stub = new Stub(applet);
            stub.setParameter("username", parser.arg(0));
            stub.setParameter("sessionid", parser.arg(1));
            applet.setStub(new Stub(applet));
            frame.setVisible(true);
            frame.setResizable(true);
            frame.setMinimumSize(new Dimension(400, 300));
            frame.setPreferredSize(new Dimension(400, 300));
            frame.setName("Rosepad Loader Dev Build");
        }

        RosepadLoadingWindow loadingWindow = new RosepadLoadingWindow(applet);
        new Thread(loadingWindow, "Rosepad loading window thread").start();

        Thread thread = new RosepadMainThread(loadingWindow, this);
        thread.start();
    }
}
