package net.buj.loader;

import net.buj.rml.annotations.Nullable;

import java.awt.*;
import java.util.Objects;

public class RosepadLoadingWindow implements Runnable {
    @SuppressWarnings("removal")
    public final @Nullable java.applet.Applet applet;
    public boolean doDraw = true;
    public @Nullable Exception error;
    private String task = null;
    private String step = null;

    @SuppressWarnings("removal")
    public RosepadLoadingWindow(@Nullable java.applet.Applet applet) {
        this.applet = applet;
    }

    @SuppressWarnings("removal")
    public synchronized java.applet.Applet release() {
        doDraw = false;
        return applet;
    }

    public synchronized void crash(Exception e) {
        error = e;
    }
    public synchronized void setTask(String name) {
        if (applet == null) System.out.println("[TASK] " + name);
        task = name;
        step = null;
    }
    public synchronized void setStep(String name) {
        if (applet == null) System.out.println("[STEP] " + name);
        step = name;
    }

    @SuppressWarnings("deprecated")
    public String getParameter(String param) {
        return applet.getParameter(param);
    }
    @SuppressWarnings("deprecated")
    public String getParameter(String param, String or) {
        return Objects.toString(applet.getParameter(param), or);
    }

    public synchronized void draw() {
        if (applet == null) return;
        Graphics graphics = applet.getGraphics();
        if (graphics == null) return;
        int w = applet.getWidth();
        int h = applet.getHeight();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, w, h);
        graphics.setColor(Color.WHITE);

        if (error == null) {
            graphics.drawString("Loading Rosepad...", 10, 10);
            if (task != null)
                graphics.drawString(task, 10, 25);
            if (step != null)
                graphics.drawString(step, 10, 40);
        }
        else {
            graphics.drawString("An error occured while loading Rosepad:", 10, 10);
            Throwable err = error;
            int i = 0;
            while (err != null) {
                graphics.drawString(err + " - " + err.getMessage(), 10, 25 + (i++) * 15);
                for (StackTraceElement e : err.getStackTrace()) {
                    graphics.drawString("at " + e.toString(), 15, 25 + (i++) * 15);
                }
                err = err.getCause();
                if (err != null) {
                    graphics.drawString("Caused by:", 10, 25 + (i++) * 15);
                }
            }
        }

        graphics.dispose();
    }

    @Override
    public void run() {
        if (applet == null) return;
        while (doDraw) {
            draw();
            try {
                Thread.sleep(1000/20);
            } catch (InterruptedException ignored) {}
        }
        Graphics graphics = applet.getGraphics();
        if (graphics == null) return;
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, applet.getWidth(), applet.getHeight());
        graphics.dispose();
    }
}
