package com.bgsoftware.common.nmsloader.method;

import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.common.nmsloader.config.NMSConfiguration;
import com.bgsoftware.common.nmsloader.internal.NMSLoaderContext;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public interface INMSHandlersFactoryMethod {

    INMSLoader createNMSLoader(NMSLoaderContext context, String nmsPackageVersionName) throws NMSLoadException;

}
