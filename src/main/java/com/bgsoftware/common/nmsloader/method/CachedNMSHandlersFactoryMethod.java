package com.bgsoftware.common.nmsloader.method;

import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.common.nmsloader.internal.BaseNMSLoader;
import com.bgsoftware.common.nmsloader.internal.NMSLoaderContext;
import com.bgsoftware.common.nmsloader.jar.JarClassLoader;
import com.bgsoftware.common.nmsloader.jar.JarFiles;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CachedNMSHandlersFactoryMethod implements INMSHandlersFactoryMethod {

    private static final CachedNMSHandlersFactoryMethod INSTANCE = new CachedNMSHandlersFactoryMethod();

    public static CachedNMSHandlersFactoryMethod getInstance() {
        return INSTANCE;
    }

    private CachedNMSHandlersFactoryMethod() {

    }

    @Override
    public INMSLoader createNMSLoader(NMSLoaderContext context, String nmsPackageVersionName) throws NMSLoadException {
        File cacheFolder = context.getConfiguration().getCacheFolder();

        if (!cacheFolder.exists() || !cacheFolder.isDirectory())
            throw new NMSLoadException("Cache folder does not exist");

        File nmsVersionFile = new File(cacheFolder, nmsPackageVersionName + ".jar");

        if (!nmsVersionFile.exists())
            throw new NMSLoadException("Nms version file does not exist");

        try {
            return new CacheNMSLoader(context.getPlugin(), nmsVersionFile);
        } catch (IOException error) {
            throw new NMSLoadException("Cannot create cache nms loader", error);
        }
    }

    private static class CacheNMSLoader extends BaseNMSLoader {

        private final File file;
        private final ClassLoader fileClassLoader;

        CacheNMSLoader(JavaPlugin plugin, File file) throws IOException {
            super(plugin);

            this.file = file;
            this.fileClassLoader = new JarClassLoader(file, plugin.getClass().getClassLoader());
        }

        @Override
        protected Class<?> findImplementationClass(Class<?> nmsClass) throws NMSLoadException {
            try {
                Class<?> nmsHandlerClass = JarFiles.getClass(this.file.toURL(), nmsClass, fileClassLoader);

                if (nmsHandlerClass == null)
                    throw new NoClassDefFoundError("Cannot find implementation");

                return nmsHandlerClass;
            } catch (Exception error) {
                throw new NMSLoadException("Couldn't find nms handler for class " + nmsClass, error);
            }
        }

    }

}
