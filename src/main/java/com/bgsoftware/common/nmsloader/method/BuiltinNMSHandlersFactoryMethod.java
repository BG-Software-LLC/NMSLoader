package com.bgsoftware.common.nmsloader.method;

import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.common.nmsloader.internal.BaseNMSLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class BuiltinNMSHandlersFactoryMethod implements INMSHandlersFactoryMethod {

    private static final BuiltinNMSHandlersFactoryMethod INSTANCE = new BuiltinNMSHandlersFactoryMethod();

    public static BuiltinNMSHandlersFactoryMethod getInstance() {
        return INSTANCE;
    }

    private BuiltinNMSHandlersFactoryMethod() {

    }

    @Override
    public INMSLoader createNMSLoader(JavaPlugin plugin, String nmsPackageVersionName) throws NMSLoadException {
        String pluginPackageName = getPluginPackageFromClass(plugin.getClass());

        String nmsResourcePackagePath = String.format("com/bgsoftware/%s/nms/%s", pluginPackageName, nmsPackageVersionName);

        if (plugin.getClass().getClassLoader().getResource(nmsResourcePackagePath) == null)
            throw new NMSLoadException("Cannot find nms package in plugin");

        return new BuiltinNMSLoader(plugin, pluginPackageName, nmsPackageVersionName);
    }

    private String getPluginPackageFromClass(Class<?> nmsClass) throws NMSLoadException {
        String[] pathClassSections = nmsClass.getName().split("\\.");

        if (pathClassSections.length < 3 || !pathClassSections[0].equals("com") ||
                !pathClassSections[1].equals("bgsoftware")) {
            throw new NMSLoadException("Class is not under the com.bgsoftware package: " + nmsClass.getName());
        }

        return pathClassSections[2];
    }

    private static class BuiltinNMSLoader extends BaseNMSLoader {

        private final String pluginPackageName;
        private final String nmsPackageVersion;

        BuiltinNMSLoader(JavaPlugin plugin, String pluginPackageName, String nmsPackageVersion) {
            super(plugin);
            this.pluginPackageName = pluginPackageName;
            this.nmsPackageVersion = nmsPackageVersion;
        }

        @Override
        protected Class<?> findImplementationClass(Class<?> nmsClass) throws NMSLoadException {
            String nmsHandlerClass = String.format("com.bgsoftware.%s.nms.%s.%s",
                    pluginPackageName, this.nmsPackageVersion, nmsClass.getSimpleName() + "Impl");

            try {
                return Class.forName(nmsHandlerClass);
            } catch (ClassNotFoundException error) {
                throw new NMSLoadException("Failed to load nms handler for class " + nmsHandlerClass, error);
            }
        }

    }

}
