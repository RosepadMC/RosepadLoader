package net.minecraft.client.main;

import net.buj.loader.RosepadLoader;
import net.buj.rml.Environment;

import java.io.File;

public class Main implements Runnable {

    private static File minecraftDir = null;

    public static File getMinecraftDir() {
        if (minecraftDir != null) return minecraftDir;
        return (minecraftDir = new File(".")
            .getAbsoluteFile()
            .getName()
            .equals(".minecraft")
            ? new File(".")
            : new File(".minecraft")); // .minecraft > ~/.minecraft
    }

    public static void main(String[] args) {
        new RosepadLoader().main(Environment.CLIENT, args, getMinecraftDir().toPath());
    }

    @Override
    public void run() {
        System.out.println("why");
    }
}
