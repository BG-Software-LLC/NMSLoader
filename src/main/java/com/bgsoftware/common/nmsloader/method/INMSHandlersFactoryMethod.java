package com.bgsoftware.common.nmsloader.method;

import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import org.bukkit.plugin.java.JavaPlugin;

public interface INMSHandlersFactoryMethod {

    INMSLoader createNMSLoader(JavaPlugin plugin, String nmsPackageVersionName) throws NMSLoadException;

}
