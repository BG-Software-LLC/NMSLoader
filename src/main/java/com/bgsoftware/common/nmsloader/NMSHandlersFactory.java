package com.bgsoftware.common.nmsloader;

import com.bgsoftware.common.nmsloader.config.NMSConfiguration;
import com.bgsoftware.common.nmsloader.internal.NMSLoaderContext;
import com.bgsoftware.common.nmsloader.internal.NMSVersionRequirement;
import com.bgsoftware.common.nmsloader.internal.ServerVersion;
import com.bgsoftware.common.nmsloader.method.BuiltinNMSHandlersFactoryMethod;
import com.bgsoftware.common.nmsloader.method.CachedNMSHandlersFactoryMethod;
import com.bgsoftware.common.nmsloader.method.INMSHandlersFactoryMethod;
import com.bgsoftware.common.nmsloader.method.RemoteNMSHandlersFactoryMethod;
import com.bgsoftware.common.reflection.ReflectMethod;
import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class NMSHandlersFactory {

    private static final INMSHandlersFactoryMethod[] NMS_FACTORY_METHODS = new INMSHandlersFactoryMethod[]{
            BuiltinNMSHandlersFactoryMethod.getInstance(),
            RemoteNMSHandlersFactoryMethod.getInstance(),
            CachedNMSHandlersFactoryMethod.getInstance(),
    };

    public static INMSLoader createNMSLoader(JavaPlugin plugin, NMSConfiguration configuration) throws NMSLoadException {
        return createNMSLoader(plugin, configuration, null);
    }

    public static INMSLoader createNMSLoader(JavaPlugin plugin, NMSConfiguration configuration, @Nullable ClassLoader classLoader) throws NMSLoadException {
        String nmsPackageVersionName = getNMSPackageVersionName();

        NMSLoaderContext context = new NMSLoaderContext.Builder()
                .setPlugin(plugin)
                .setConfiguration(configuration)
                .setClassLoader(classLoader)
                .build();

        for (INMSHandlersFactoryMethod method : NMS_FACTORY_METHODS) {
            try {
                return method.createNMSLoader(context, nmsPackageVersionName);
            } catch (NMSLoadException error) {
                // Ignore
            }
        }

        throw new NMSLoadException("Couldn't find a valid nms loader for your Minecraft version");
    }

    @SuppressWarnings("deprecation")
    private static String getNMSPackageVersionName() throws NMSLoadException {
        String nmsPackageVersion = null;

        if (ServerVersion.isLessThan(ServerVersion.v1_17)) {
            nmsPackageVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } else {
            ReflectMethod<Integer> getDataVersion = new ReflectMethod<>(UnsafeValues.class, "getDataVersion");
            int dataVersion = getDataVersion.invoke(Bukkit.getUnsafe());

            List<NMSVersionRequirement> versions = Arrays.asList(
                    new NMSVersionRequirement(2729, null),
                    new NMSVersionRequirement(2730, "v1_17"),
                    new NMSVersionRequirement(2974, null),
                    new NMSVersionRequirement(2975, "v1_18"),
                    new NMSVersionRequirement(3336, null),
                    new NMSVersionRequirement(3337, "v1_19"),
                    new NMSVersionRequirement(3699, null),
                    new NMSVersionRequirement(3700, "v1_20_3"),
                    new NMSVersionRequirement(3839, "v1_20_4"),
                    new NMSVersionRequirement(3953, "v1_21")
            );

            for (NMSVersionRequirement versionData : versions) {
                if (dataVersion <= versionData.requiredDataVersion) {
                    nmsPackageVersion = versionData.versionName;
                    break;
                }
            }

            if (nmsPackageVersion == null) {
                throw new NMSLoadException("Couldn't find valid builtin nms handlers for the data version: " + dataVersion);
            }
        }

        return nmsPackageVersion;
    }

    private NMSHandlersFactory() {

    }

}
