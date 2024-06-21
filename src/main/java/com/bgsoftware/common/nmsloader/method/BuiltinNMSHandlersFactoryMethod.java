package com.bgsoftware.common.nmsloader.method;

import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.common.nmsloader.config.NMSConfiguration;
import com.bgsoftware.common.nmsloader.internal.BaseNMSLoader;
import com.bgsoftware.common.nmsloader.internal.NMSLoaderContext;
import org.bukkit.plugin.java.JavaPlugin;

public class BuiltinNMSHandlersFactoryMethod implements INMSHandlersFactoryMethod {

    private static final BuiltinNMSHandlersFactoryMethod INSTANCE = new BuiltinNMSHandlersFactoryMethod();

    public static BuiltinNMSHandlersFactoryMethod getInstance() {
        return INSTANCE;
    }

    private BuiltinNMSHandlersFactoryMethod() {

    }

    @Override
    public INMSLoader createNMSLoader(NMSLoaderContext context, String nmsPackageVersionName) throws NMSLoadException {
        ClassLoader classLoader = context.getClassLoader();
        if (classLoader == null)
            classLoader = context.getPlugin().getClass().getClassLoader();

        NMSConfiguration configuration = context.getConfiguration();

        String nmsResourcePackagePath = configuration.getNMSResourcePathForVersion(nmsPackageVersionName);

        if (classLoader.getResource(nmsResourcePackagePath) == null)
            throw new NMSLoadException("Cannot find nms package in plugin");

        return new BuiltinNMSLoader(context.getPlugin(), configuration, nmsPackageVersionName);
    }

    private static class BuiltinNMSLoader extends BaseNMSLoader {

        private final NMSConfiguration configuration;
        private final String nmsPackageVersion;

        BuiltinNMSLoader(JavaPlugin plugin, NMSConfiguration configuration, String nmsPackageVersion) {
            super(plugin);
            this.configuration = configuration;
            this.nmsPackageVersion = nmsPackageVersion;
        }

        @Override
        protected Class<?> findImplementationClass(Class<?> nmsClass) throws NMSLoadException {
            String nmsHandlerClass = this.configuration.getPackagePathForNMSHandler(
                    this.nmsPackageVersion, nmsClass.getSimpleName());

            try {
                return Class.forName(nmsHandlerClass);
            } catch (ClassNotFoundException error) {
                throw new NMSLoadException("Failed to load nms handler for class " + nmsHandlerClass, error);
            }
        }

    }

}
