package net.buj.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Vector;

public class ToggleableClassLoader extends ClassLoader {
    private ClassLoader parentLoader;

    public ToggleableClassLoader(ClassLoader parent) {
        super(null);
        parentLoader = parent;
    }

    public void setParent(ClassLoader parent) {
        parentLoader = parent;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (parentLoader == null) throw new ClassNotFoundException(name);
        return parentLoader.loadClass(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (parentLoader == null) return new Vector<URL>().elements();
        return parentLoader.getResources(name);
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        if (parentLoader != null) parentLoader.setClassAssertionStatus(className, enabled);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (parentLoader == null) return null;
        return parentLoader.getResourceAsStream(name);
    }

    @Override
    public URL getResource(String name) {
        if (parentLoader == null) return null;
        return parentLoader.getResource(name);
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        if (parentLoader != null) parentLoader.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        if (parentLoader != null) parentLoader.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        if (parentLoader != null) parentLoader.clearAssertionStatus();
    }
}
