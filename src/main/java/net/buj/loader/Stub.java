package net.buj.loader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Display;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Stub implements AppletStub {
    private String username;
    private Applet applet;
    private Map<String, String> params = new HashMap<>();

    public Stub(Applet applet) {
        this.applet = applet;
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
        return params.get(name);
    }

    public void setParameter(String name, String param) {
        params.put(name, param);
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
