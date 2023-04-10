package net.buj.loader;

import java.net.URL;
import java.net.URLClassLoader;

public class MinecraftIsoLoader extends URLClassLoader {
    private final ClassLoader actualParent;

    public MinecraftIsoLoader(URL[] urls, ClassLoader parent) {
        super(urls, null);
        actualParent = parent;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!name.startsWith("net.minecraft.")) {
            try {
                Class<?> c = actualParent.loadClass(name);
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            } catch (ClassNotFoundException ignored) {}
        }
        return super.loadClass(name, resolve);
    }
}
