package net.buj.loader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Display;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.MalformedURLException;
import java.net.URL;

public class Stub implements AppletStub {
    private String username;
    private Applet applet;

    public Stub(String username, Applet applet) {
        this.username = username;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public URL getDocumentBase() {
        try {
            return new URL("http://minecraft.net/game");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URL getCodeBase() {
        try {
            return new URL("http://minecraft.net/game");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getParameter(String name) {
        return name.equals("username") ? username : null;
    }

    @Override
    public AppletContext getAppletContext() {
        return null;
    }

    @Override
    public void appletResize(int width, int height) {
        applet.resize(width, height);
    }
}
