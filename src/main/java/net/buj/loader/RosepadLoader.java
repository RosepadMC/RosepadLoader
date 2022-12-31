package net.buj.loader;

import net.buj.rml.Environment;
import net.minecraft.client.MinecraftApplet;

import java.awt.*;
import java.nio.file.Path;
import java.util.Objects;

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
            applet.setStub(new Stub(Objects.toString(args[0], "Player"), applet));
            frame.setVisible(true);
        }

        RosepadLoadingWindow loadingWindow = new RosepadLoadingWindow(applet);
        new Thread(loadingWindow, "Rosepad loading window thread").start();

        Thread thread = new RosepadMainThread(loadingWindow, this);
        thread.start();
    }
}
