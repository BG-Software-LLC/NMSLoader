package com.bgsoftware.common.nmsloader.jar;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectMethod;

import java.nio.file.Path;

public class Remapper {

    private static final ReflectMethod<Object> CREATE_PLUGIN_REMAPPER;
    private static final ReflectMethod<Path> PLUGIN_REMAPPER_REWRITE_PLUGIN;
    private static Object pluginRemapper;

    static {
        ClassInfo pluginRemapperClassInfo = new ClassInfo("io.papermc.paper.pluginremap.PluginRemapper",
                ClassInfo.PackageType.UNKNOWN);
        CREATE_PLUGIN_REMAPPER = new ReflectMethod<>(pluginRemapperClassInfo, "create", Path.class);
        PLUGIN_REMAPPER_REWRITE_PLUGIN = new ReflectMethod<>(pluginRemapperClassInfo, "rewritePlugin", Path.class);
    }

    @Nullable
    public static Path remap(Path file) {
        if (pluginRemapper == null) {
            pluginRemapper = CREATE_PLUGIN_REMAPPER.invoke(null, file.getParent());
        }
        return PLUGIN_REMAPPER_REWRITE_PLUGIN.invoke(pluginRemapper, file);
    }

}
