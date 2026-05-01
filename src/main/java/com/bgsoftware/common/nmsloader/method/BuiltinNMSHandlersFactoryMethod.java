package com.bgsoftware.common.nmsloader.method;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.common.nmsloader.config.NMSConfiguration;
import com.bgsoftware.common.nmsloader.internal.BaseNMSLoader;
import com.bgsoftware.common.nmsloader.internal.NMSLoaderContext;
import com.bgsoftware.common.nmsloader.jar.JarClassLoader;
import com.bgsoftware.common.nmsloader.jar.Remapper;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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

        return new BuiltinNMSLoader(context.getPlugin(), classLoader, configuration, nmsPackageVersionName);
    }

    private static class BuiltinNMSLoader extends BaseNMSLoader {

        private final NMSConfiguration configuration;
        private final String nmsPackageVersion;
        @Nullable
        private final ClassLoader classLoader;
        @Nullable
        private Throwable error;

        BuiltinNMSLoader(JavaPlugin plugin, ClassLoader classLoader, NMSConfiguration configuration, String nmsPackageVersion) {
            super(plugin);
            this.configuration = configuration;
            this.nmsPackageVersion = nmsPackageVersion;
            this.classLoader = loadNMSToRuntime(configuration, classLoader);
        }

        @Override
        protected Class<?> findImplementationClass(Class<?> nmsClass) throws NMSLoadException {
            if (this.classLoader == null) {
                throw new NMSLoadException("An error occurred while loading nms jar", error);
            }

            String nmsHandlerClass = this.configuration.getPackagePathForNMSHandler(
                    this.nmsPackageVersion, nmsClass.getSimpleName());

            try {
                return this.classLoader.loadClass(nmsHandlerClass);
            } catch (ClassNotFoundException error) {
                throw new NMSLoadException("Failed to load nms handler for class " + nmsHandlerClass, error);
            }
        }

        @Nullable
        private ClassLoader loadNMSToRuntime(NMSConfiguration configuration, ClassLoader parentClassLoader) {
            File cacheFolder = configuration.getCacheFolder();

            if (!cacheFolder.exists() || !cacheFolder.isDirectory())
                cacheFolder.mkdirs();

            File nmsVersionFile = new File(cacheFolder, this.nmsPackageVersion + ".jar");

            if (nmsVersionFile.exists())
                nmsVersionFile.delete();

            try {
                String nmsResourceName = this.configuration.getNMSResourcePathForVersion(this.nmsPackageVersion);

                Path tempJarPath = Files.createTempFile("nms-", ".jar");
                try (InputStream inputStream = this.configuration.getResource(nmsResourceName)) {
                    Files.copy(inputStream, tempJarPath, StandardCopyOption.REPLACE_EXISTING);
                }
                tempJarPath.toFile().deleteOnExit();

                Path remappedJarPath = Remapper.remap(tempJarPath);

                if (remappedJarPath == null) {
                    // Jar was not remapped, let's just move the file to the destination
                    tempJarPath.toFile().renameTo(nmsVersionFile);
                } else {
                    // Jar was remapped, move the remapped to the destination
                    remappedJarPath.toFile().renameTo(nmsVersionFile);
                }

                nmsVersionFile.deleteOnExit();

                return new JarClassLoader(nmsVersionFile, parentClassLoader);
            } catch (IOException error) {
                this.error = error;
                return null;
            }
        }

    }

}
