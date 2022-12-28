package net.minecraft.client;

import net.buj.loader.RosepadLoader;
import net.buj.rml.Environment;

import java.io.File;

public class Minecraft implements Runnable {

    private static File minecraftDir = null;

    private enum EnumOS {
        LINUX,
        SOLARIS,
        WINDOWS,
        MACOS,
        UNKNOWN;
    }

    public static File getMinecraftDir() {
        if (minecraftDir != null) return minecraftDir;
        return (minecraftDir = new File(".minecraft")); // .minecraft > ~/.minecraft
    }

    public static void main(String[] args) {
        new RosepadLoader().main(Environment.CLIENT, args, getMinecraftDir().toPath());
    }

    @Override
    public void run() {
        System.out.println("why");
    }
}
