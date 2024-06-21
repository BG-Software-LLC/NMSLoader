package com.bgsoftware.common.nmsloader.internal;

import com.bgsoftware.common.nmsloader.config.NMSConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class NMSLoaderContext {

    private final JavaPlugin plugin;
    private final NMSConfiguration configuration;
    private final ClassLoader classLoader;

    private NMSLoaderContext(Builder builder) {
        this.plugin = Objects.requireNonNull(builder.plugin);
        this.configuration = Objects.requireNonNull(builder.configuration);
        this.classLoader = builder.classLoader;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public NMSConfiguration getConfiguration() {
        return configuration;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public static class Builder {

        private JavaPlugin plugin;
        private NMSConfiguration configuration;
        private ClassLoader classLoader;

        public Builder setPlugin(JavaPlugin plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder setConfiguration(NMSConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public NMSLoaderContext build() {
            return new NMSLoaderContext(this);
        }

    }

}
