package net.buj.loader.tasks;

import net.buj.loader.Failible;
import net.buj.loader.MinecraftIsoLoader;
import net.buj.loader.RosepadLoadingWindow;
import net.buj.loader.Task;
import net.buj.rml.Environment;
import net.buj.rml.Game;
import net.buj.rml.MinecraftImpl;
import net.buj.rml.annotations.NotNull;
import net.buj.rml.loader.GameJar;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Path;

public class LaunchGameTask extends Task<Void> {
    private final Task<GameJar> gameJarTask;
    private final Environment environment;
    private final Path root;
    private final String[] args;

    public LaunchGameTask(Task<GameJar> gameJarTask, Environment environment, Path root, String[] args) {
        this.gameJarTask = gameJarTask;
        this.environment = environment;
        this.root = root;
        this.args = args;
    }

    @SuppressWarnings({"unchecked", "deprecated"})
    @Override
    protected @NotNull Void run(RosepadLoadingWindow log) throws Failible {
        MinecraftIsoLoader minecraftLoader = Failible.$(() -> new MinecraftIsoLoader(new URL[]{ gameJarTask.await(log).getURL() }, getClass().getClassLoader()));

        log.setTask("Launching game");

        Game.MOD_LOADER.setMinecraftClassLoader(minecraftLoader);

        try {
            Class<? extends MinecraftImpl> klass = Failible.$(() -> (Class<? extends MinecraftImpl>) minecraftLoader.loadClass(
                this.environment == Environment.CLIENT
                    ? "net.minecraft.client.Minecraft"
                    : "net.minecraft.server.MinecraftServer"
            ));

            for (Field field : klass.getDeclaredFields()) { // Home path fix (inspired by MultiMC)
                if (field.getModifiers() != (Modifier.PRIVATE | Modifier.STATIC)) continue;

                if (!field.getType().getName().equals(File.class.getName())) continue;

                field.setAccessible(true);
                field.set(null, root.toFile());

                break;
            }

            if (log.applet != null && this.environment == Environment.CLIENT) { // Replace the applet
                // Do all the work that applet does, but with reflection!
                boolean fullscreen = "true".equalsIgnoreCase(log.getParameter("fullscreen"));
                @NotNull String username = log.getParameter("username", "RPlayer");
                @NotNull String sessionID = log.getParameter("sessionid", "");

                Canvas canvas;
                {
                    Class<?> klass2 = Class.forName("net.minecraft.src.CanvasMinecraftApplet", true, minecraftLoader);
                    Constructor<?> constructor = klass2.getConstructor(Runnable.class);
                    canvas = (Canvas) constructor.newInstance((Object) null);
                }
                Runnable minecraft;
                {
                    Class<?> klass2 = minecraftLoader.loadClass("net.minecraft.src.MinecraftAppletImpl");
                    Constructor<?> constructor = klass2.getConstructor(java.applet.Applet.class,
                        Component.class,
                        Canvas.class,
                        java.applet.Applet.class,
                        int.class,
                        int.class,
                        boolean.class);
                    minecraft = (Runnable) constructor.newInstance(log.applet, log.applet, canvas, log.applet,
                        log.applet.getWidth(), log.applet.getHeight(),
                        fullscreen);
                }
                canvas.getClass().getField("mc").set(canvas, minecraft);
                {
                    Class<?> klass2 = minecraftLoader.loadClass("net.minecraft.src.Session");
                    Object session = klass2.getConstructor(String.class, String.class).newInstance(username, sessionID);
                    minecraft.getClass().getField("session").set(minecraft, session);
                }

                // Don't care about mppass thing
                // Don't care about loadmap either

                if (log.getParameter("server") != null && log.getParameter("port") != null) {
                    minecraft.getClass().getMethod("setServer", String.class, String.class)
                        .invoke(minecraft, log.getParameter("server"), log.getParameter("port"));
                }

                minecraft.getClass().getField("appletMode").set(minecraft, true);

                java.applet.Applet applet = log.release();
                // Finally starting MC
                applet.setLayout(new BorderLayout());
                applet.add(canvas, "Center");
                canvas.setFocusable(true);
                applet.validate();
            }
            else { // Launch main if not
                log.release(); // Even though there's no applet, we gotta release it jic something is handling the release

                Method method = klass.getMethod("main", String[].class);
                method.invoke(null, new Object[] { this.args });
            }
        } catch (Exception e) {
            Failible.$(e);
        }

        return null;
    }
}
