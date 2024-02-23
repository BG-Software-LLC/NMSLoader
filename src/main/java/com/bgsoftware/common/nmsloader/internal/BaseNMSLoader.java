package com.bgsoftware.common.nmsloader.internal;

import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;

public abstract class BaseNMSLoader implements INMSLoader {

    protected final JavaPlugin plugin;

    protected BaseNMSLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public final <T> T loadNMSHandler(Class<T> nmsClass) throws NMSLoadException {
        Class<?> implementationNMSClass = findImplementationClass(nmsClass);
        try {
            return loadNMSClass(implementationNMSClass);
        } catch (Exception error) {
            throw new NMSLoadException("Failed to load nms handler for class " + nmsClass.getSimpleName(), error);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T loadNMSClass(Class<?> nmsClass) throws Exception {
        try {
            Constructor<?> constructor = nmsClass.getConstructor(plugin.getClass());
            return (T) constructor.newInstance(plugin);
        } catch (NoSuchMethodException error) {
            return (T) nmsClass.newInstance();
        }
    }

    protected abstract Class<?> findImplementationClass(Class<?> nmsClass) throws NMSLoadException;

}
