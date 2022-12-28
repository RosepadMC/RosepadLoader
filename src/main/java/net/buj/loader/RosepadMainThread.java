package net.buj.loader;

import net.buj.rml.Environment;
import net.buj.rml.annotations.Nullable;
import org.lwjgl.Sys;

import java.applet.Applet;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class RosepadMainThread extends Thread {
    private @Nullable Applet applet;
    private RosepadLoader loader;

    public RosepadMainThread(@Nullable Applet applet, RosepadLoader loader) {
        super("Rosepad main thread");

        this.applet = applet;
        this.loader = loader;
    }

    private GameJar initJar(Environment env, Path versionHome) throws IOException, URISyntaxException {
        GameJar jar = new GameJar(env, versionHome);

        jar.fetch();

        return jar;
    }

    @Override
    public void run() {
        GameJar jar;
        try {
            jar = initJar(loader.environment, loader.home.resolve("versions/" + R.VERSION_NAME));
        } catch (IOException | URISyntaxException e) {
            applet.stop();
            applet.destroy();
            throw new RuntimeException(e);
        }

        // TODO: Mixins
        if (applet != null) {
            applet.stop();
            applet.destroy();
        }

        URLClassLoader loader;
        try {
            loader = new URLClassLoader(new URL[]{ jar.getURL() }, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Class<?> klass = loader.loadClass(
                this.loader.environment == Environment.CLIENT
                    ? "net.minecraft.client.Minecraft"
                    : "net.minecraft.server.MinecraftServer"
            );

            for (Field field : klass.getDeclaredFields()) { // Home path fix (inspired by MultiMC)
                if (field.getModifiers() != (Modifier.PRIVATE | Modifier.STATIC)) continue;

                if (!field.getType().getName().equals(File.class.getName())) continue;

                field.setAccessible(true);
                field.set(null, this.loader.home.toFile());

                break;
            }

            if (applet != null && this.loader.environment == Environment.CLIENT) { // Use applet launcher if available
                Class<?> $MinecraftApplet = loader.loadClass("net.minecraft.client.MinecraftApplet");
                Applet mcApplet = (Applet) $MinecraftApplet.newInstance();
                applet.add(mcApplet, "Center");
                mcApplet.resize(applet.getWidth(), applet.getHeight());
                mcApplet.setStub(new PassthroughStub(applet, mcApplet));
                mcApplet.init();
                mcApplet.start();
            }
            else { // Legacy launch
                Method method = klass.getMethod("main", String[].class);
                method.invoke(null, new Object[] { this.loader.args });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
