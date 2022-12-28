package net.buj.loader;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.URL;

public class PassthroughStub implements AppletStub {
    private Applet source;
    private Applet target;

    public PassthroughStub(Applet source, Applet target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public boolean isActive() {
        return source.isActive();
    }

    @Override
    public URL getDocumentBase() {
        return source.getDocumentBase();
    }

    @Override
    public URL getCodeBase() {
        return source.getCodeBase();
    }

    @Override
    public String getParameter(String name) {
        return source.getParameter(name);
    }

    @Override
    public AppletContext getAppletContext() {
        return source.getAppletContext();
    }

    @Override
    public void appletResize(int width, int height) {
        source.resize(width, height);
        target.resize(width, height);
    }
}
