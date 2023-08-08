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
    private int progress = 0;
    private @Nullable Integer max = null;
    private boolean shouldRedraw = true;
    private int previousWidth = 0;
    private int previousHeight = 0;

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
        e.printStackTrace();
        shouldRedraw = true;
        error = e;
        this.progress = 0;
        this.max = null;
    }
    public synchronized void setTask(String name) {
        System.out.println(name);
        shouldRedraw = true;
        task = name;
        step = null;
        this.progress = 0;
        this.max = null;
    }
    public synchronized void setStep(String name) {
        if (name != null) System.out.println(task + " / " + name);
        shouldRedraw = true;
        step = name;
        this.progress = 0;
        this.max = null;
    }
    public synchronized void setStep(String name, int progress, int max) {
        if (name != null) System.out.println(task + " / " + name + "[" + progress + "/" + max + "]");
        shouldRedraw = true;
        step = name;
        this.progress = progress;
        this.max = max;
    }

    @SuppressWarnings("deprecated")
    public String getParameter(String param) {
        return applet.getParameter(param);
    }
    @SuppressWarnings("deprecated")
    public String getParameter(String param, String or) {
        return Objects.toString(applet.getParameter(param), or);
    }

    private synchronized boolean shouldRedraw() {
        if (applet == null) return false;

        boolean shouldRedraw = this.shouldRedraw;

        if (previousWidth != applet.getWidth() || previousHeight != applet.getHeight()) {
            shouldRedraw = true;
            previousWidth = applet.getWidth();
            previousHeight = applet.getHeight();
        }

        this.shouldRedraw = false;
        return shouldRedraw;
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
                graphics.drawString(step + (max == null ? "" : " [" + progress + "/" + max + "]"), 10, 40);
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
            if (shouldRedraw()) draw();
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
