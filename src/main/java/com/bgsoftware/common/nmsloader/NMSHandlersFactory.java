package com.bgsoftware.common.nmsloader;

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

import java.util.Arrays;
import java.util.List;

public class NMSHandlersFactory {

    private static final INMSHandlersFactoryMethod[] NMS_FACTORY_METHODS = new INMSHandlersFactoryMethod[]{
            BuiltinNMSHandlersFactoryMethod.getInstance(),
            RemoteNMSHandlersFactoryMethod.getInstance(),
            CachedNMSHandlersFactoryMethod.getInstance(),
    };

    public static INMSLoader createNMSLoader(JavaPlugin plugin) throws NMSLoadException {
        String nmsPackageVersionName = getNMSPackageVersionName(plugin);

        for (INMSHandlersFactoryMethod method : NMS_FACTORY_METHODS) {
            try {
                return method.createNMSLoader(plugin, nmsPackageVersionName);
            } catch (NMSLoadException error) {
                // Ignore
            }
        }

        throw new NMSLoadException("Couldn't find a valid nms loader for your Minecraft version");
    }

    @SuppressWarnings("deprecation")
    private static String getNMSPackageVersionName(JavaPlugin plugin) throws NMSLoadException {
        String nmsPackageVersion = null;

        if (ServerVersion.isLessThan(ServerVersion.v1_17)) {
            nmsPackageVersion = plugin.getServer().getClass().getPackage().getName().split("\\.")[3];
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
                    new NMSVersionRequirement(3465, "v1_20_1"),
                    new NMSVersionRequirement(3578, "v1_20_2"),
                    new NMSVersionRequirement(3700, "v1_20_3"),
                    new NMSVersionRequirement(3837, "v1_20_4")
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
