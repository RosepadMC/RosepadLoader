package net.minecraft.client;

import net.buj.loader.RosepadLoader;
import net.buj.rml.Environment;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.util.ArrayList;
import java.util.List;

public class MinecraftApplet extends Applet {
    Applet wrapped = null;

    private String[] sanitize(String[] a) {
        List<String> l = new ArrayList<>();
        for (String s : a) {
            if (s != null) l.add(s);
        }
        return l.toArray(new String[0]);
    }

    public void wrap(Applet applet) {
        wrapped = applet;
        add(wrapped, "Center");
    }

    @Override
    public void resize(int width, int height) {
        System.out.println(width + "x" + height);
        super.resize(width, height);
        if (wrapped != null)
            wrapped.resize(width, height);
    }

    @Override
    public void resize(Dimension d) {
        System.out.println(d);
        super.resize(d);
        if (wrapped != null)
            wrapped.resize(d);
    }

    public void init() {}
    public void start() {
        new RosepadLoader(this).main(
            Environment.CLIENT,
            sanitize(new String[]{
                getParameter("username"),
                getParameter("sessionid"),
                getParameter("server"),
                getParameter("port")
            }),
            Minecraft.getMinecraftDir().toPath()
        );
    }
}
