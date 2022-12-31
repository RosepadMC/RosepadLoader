package net.buj.loader;

import net.buj.rml.Environment;
import net.minecraft.client.MinecraftApplet;

import java.awt.*;
import java.nio.file.Path;
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

    private String arg(String[] args, int i) {
        if (i < 0 || args.length <= i) {
            return null;
        }
        return args[i];
    }

    public void main(Environment env, String[] args, Path home) {
        environment = env;
        this.args = args;
        this.home = home;

        if (!dirtyOneMain) {
            dirtyOneMain = true;
        }
        else throw new RuntimeException("Calling main multiple times");

        if (applet == null) {
            Frame frame = new LauncherWindow();
            frame.add((applet = new MinecraftApplet()));
            Stub stub = new Stub(applet);
            stub.setParameter("username", Objects.toString(arg(args, 0), "Player"));
            stub.setParameter("sessionid", Objects.toString(arg(args, 1), ""));
            applet.setStub(new Stub(applet));
            frame.setVisible(true);
        }

        RosepadLoadingWindow loadingWindow = new RosepadLoadingWindow(applet);
        new Thread(loadingWindow, "Rosepad loading window thread").start();

        Thread thread = new RosepadMainThread(loadingWindow, this);
        thread.start();
    }
}
