package net.buj.loader;

import net.buj.rml.Environment;
import net.buj.rml.annotations.NotNull;
import net.buj.rml.annotations.Nullable;
import net.minecraft.client.MinecraftApplet;
import org.lwjgl.Sys;

import java.applet.Applet;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class RosepadMainThread extends Thread {
    private @Nullable MinecraftApplet applet;
    private RosepadLoader loader;
    private Timer resizeTimer;

    public RosepadMainThread(@Nullable Applet applet, RosepadLoader loader) {
        super("Rosepad main thread");

        this.applet = (MinecraftApplet) applet;
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

            if (applet != null && this.loader.environment == Environment.CLIENT) { // Replace the applet
                // Do all the work that applet does, but with reflection!
                boolean fullscreen = "true".equalsIgnoreCase(applet.getParameter("fullscreen"));
                @NotNull String username = Objects.toString(applet.getParameter("username"), "Player");
                @NotNull String sessionID = Objects.toString(applet.getParameter("sessionid"), "");

                Canvas canvas; // Useless type definition!
                {
                    Class<?> klass2 = Class.forName("net.minecraft.src.CanvasMinecraftApplet", true, loader);
                    Constructor<?> constructor = klass2.getConstructor(Runnable.class);
                    canvas = (Canvas) constructor.newInstance((Object) null);
                }
                Runnable minecraft; // More useless type definitions!
                {
                    Class<?> klass2 = loader.loadClass("net.minecraft.src.MinecraftAppletImpl");
                    Constructor<?> constructor = klass2.getConstructor(Applet.class, Component.class, Canvas.class, Applet.class, int.class, int.class, boolean.class);
                    minecraft = (Runnable) constructor.newInstance(applet, applet, canvas, applet, applet.getWidth(), applet.getHeight(), fullscreen);
                }
                canvas.getClass().getField("mc").set(canvas, minecraft);
                {
                    Class<?> klass2 = loader.loadClass("net.minecraft.src.Session");
                    Object session = klass2.getConstructor(String.class, String.class).newInstance(username, sessionID);
                    minecraft.getClass().getField("session").set(minecraft, session);
                }
                // Don't care about mppass thing
                // Don't care about loadmap either

                if (applet.getParameter("server") != null && applet.getParameter("port") != null) {
                    minecraft.getClass().getMethod("setServer", String.class, String.class)
                        .invoke(minecraft, applet.getParameter("server"), applet.getParameter("port"));
                }

                minecraft.getClass().getField("appletMode").set(minecraft, true);

                // Finally starting MC
                applet.setLayout(new BorderLayout());
                applet.add(canvas, "Center");
                canvas.setFocusable(true);
                applet.validate();
            }
          //else if (this.loader.environment == Environment.CLIENT) { // Use applet launcher if available
          //    Class<?> $MinecraftApplet = loader.loadClass("net.minecraft.client.MinecraftApplet");
          //    Applet mcApplet = (Applet) $MinecraftApplet.newInstance();
          //    applet.wrap(mcApplet);
          //    mcApplet.resize(applet.getWidth(), applet.getHeight());
          //    mcApplet.setStub(new PassthroughStub(applet, mcApplet));
          //    mcApplet.init();
          //    mcApplet.start();

          //    {
          //        resizeTimer = new Timer();
          //        resizeTimer.schedule(new TimerTask() {
          //            private int resizeTimes = 0;

          //            @Override
          //            public void run() {
          //                if (applet.getWidth() != mcApplet.getWidth() || applet.getHeight() != mcApplet.getHeight()) {
          //                    resizeTimes = 10;
          //                }
          //                if (resizeTimes > 0) {
          //                    resizeTimes--;
          //                    mcApplet.resize(applet.getWidth(), applet.getHeight());
          //                }
          //            }
          //        }, 100, 100);
          //    }
          //}
            else { // Launch main if not
                Method method = klass.getMethod("main", String[].class);
                method.invoke(null, new Object[] { this.loader.args });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
