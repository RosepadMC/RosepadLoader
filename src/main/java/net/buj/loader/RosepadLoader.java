package net.buj.loader;

import net.buj.rml.Environment;
import net.minecraft.client.MinecraftApplet;

import java.nio.file.Path;

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

        System.setSecurityManager(null); // Who needs security?

        if (!dirtyOneMain) {
            dirtyOneMain = true;
        }
        else throw new RuntimeException("Calling main multiple times");

        Thread thread = new RosepadMainThread(applet, this);
        thread.start();
    }
}
