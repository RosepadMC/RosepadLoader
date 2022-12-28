package net.minecraft.server;

import net.buj.loader.RosepadLoader;
import net.buj.rml.Environment;

import java.io.File;

public class MinecraftServer implements Runnable {

    public static void main(String[] args) {
        new RosepadLoader().main(Environment.SERVER, args, new File(".").toPath());
    }

    @Override
    public void run() {

    }
}
