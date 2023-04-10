package net.buj.loader;

import net.buj.rml.Environment;
import net.buj.rml.annotations.NotNull;

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

@SuppressWarnings("removal")
public class RosepadMainThread extends Thread {
    private @NotNull RosepadLoadingWindow window;
    private RosepadLoader loader;

    public RosepadMainThread(@NotNull RosepadLoadingWindow window, RosepadLoader loader) {
        super("Rosepad main thread");

        this.window = window;
        this.loader = loader;
    }

    private GameJar initJar(Environment env, Path versionHome) throws IOException, URISyntaxException {
        GameJar jar = new GameJar(env, versionHome);
        jar.window = window;

        jar.fetch();

        return jar;
    }

    @Override
    public void run() {
        GameJar jar;
        try {
            jar = initJar(loader.environment, loader.home.resolve("rosepad_versions/" + R.VERSION_NAME));
        } catch (IOException | URISyntaxException e) {
            window.crash(e);
            return;
        }

        new CheckLWJGL(window, loader.home).run();

        // TODO: Mixins

        window.setTask("Starting game...");

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

            if (window.applet != null && this.loader.environment == Environment.CLIENT) { // Replace the applet
                window.setTask("Setting up applet...");

                // Do all the work that applet does, but with reflection!
                boolean fullscreen = "true".equalsIgnoreCase(window.getParameter("fullscreen"));
                @NotNull String username = window.getParameter("username", "RPlayer");
                @NotNull String sessionID = window.getParameter("sessionid", "");

                Canvas canvas;
                {
                    Class<?> klass2 = Class.forName("net.minecraft.src.CanvasMinecraftApplet", true, loader);
                    Constructor<?> constructor = klass2.getConstructor(Runnable.class);
                    canvas = (Canvas) constructor.newInstance((Object) null);
                }
                Runnable minecraft;
                {
                    Class<?> klass2 = loader.loadClass("net.minecraft.src.MinecraftAppletImpl");
                    Constructor<?> constructor = klass2.getConstructor(java.applet.Applet.class,
                                                                       Component.class,
                                                                       Canvas.class,
                                                                       java.applet.Applet.class,
                                                                       int.class,
                                                                       int.class,
                                                                       boolean.class);
                    minecraft = (Runnable) constructor.newInstance(window.applet, window.applet, canvas, window.applet,
                                                                   window.applet.getWidth(), window.applet.getHeight(),
                                                                   fullscreen);
                }
                canvas.getClass().getField("mc").set(canvas, minecraft);
                {
                    Class<?> klass2 = loader.loadClass("net.minecraft.src.Session");
                    Object session = klass2.getConstructor(String.class, String.class).newInstance(username, sessionID);
                    minecraft.getClass().getField("session").set(minecraft, session);
                }
                // Don't care about mppass thing
                // Don't care about loadmap either

                if (window.getParameter("server") != null && window.getParameter("port") != null) {
                    minecraft.getClass().getMethod("setServer", String.class, String.class)
                        .invoke(minecraft, window.getParameter("server"), window.getParameter("port"));
                }

                minecraft.getClass().getField("appletMode").set(minecraft, true);

                java.applet.Applet applet = window.release();
                // Finally starting MC
                applet.setLayout(new BorderLayout());
                applet.add(canvas, "Center");
                canvas.setFocusable(true);
                applet.validate();
            }
            else { // Launch main if not
                window.setTask("Running main...");
                window.release(); // Even though there's no applet, we gotta release it jic something is handling the release

                Method method = klass.getMethod("main", String[].class);
                method.invoke(null, new Object[] { this.loader.args });
            }
        } catch (Exception e) {
            window.crash(e);
        }
    }
}
