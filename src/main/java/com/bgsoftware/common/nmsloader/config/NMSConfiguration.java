package com.bgsoftware.common.nmsloader.config;

import com.bgsoftware.common.nmsloader.NMSLoadException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public abstract class NMSConfiguration {

    public static NMSConfiguration forPlugin(JavaPlugin plugin) throws NMSLoadException {
        return new PluginNMSConfiguration(plugin);
    }

    protected NMSConfiguration() {

    }

    public abstract String getNMSResourcePathForVersion(String nmsVersionName);

    public abstract String getPackagePathForNMSHandler(String nmsVersionName, String handlerName);

    public abstract File getCacheFolder();

    private static class PluginNMSConfiguration extends NMSConfiguration {

        private final String pluginPackageName;
        private final File cacheFolder;

        PluginNMSConfiguration(JavaPlugin plugin) throws NMSLoadException {
            this.pluginPackageName = getPluginPackageFromClass(plugin.getClass());
            this.cacheFolder = new File(plugin.getDataFolder(), ".cache");
        }

        @Override
        public String getNMSResourcePathForVersion(String nmsVersionName) {
            return String.format("com/bgsoftware/%s/nms/%s", this.pluginPackageName, nmsVersionName);
        }

        @Override
        public String getPackagePathForNMSHandler(String nmsVersionName, String handlerName) {
            return String.format("com.bgsoftware.%s.nms.%s.%s", this.pluginPackageName, nmsVersionName,
                    handlerName + "Impl");
        }

        @Override
        public File getCacheFolder() {
            return this.cacheFolder;
        }

    }

    private static String getPluginPackageFromClass(Class<?> nmsClass) throws NMSLoadException {
        String[] pathClassSections = nmsClass.getName().split("\\.");

        if (pathClassSections.length < 3 || !pathClassSections[0].equals("com") ||
                !pathClassSections[1].equals("bgsoftware")) {
            throw new NMSLoadException("Class is not under the com.bgsoftware package: " + nmsClass.getName());
        }

        return pathClassSections[2];
    }

}
