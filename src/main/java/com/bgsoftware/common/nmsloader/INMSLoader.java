package com.bgsoftware.common.nmsloader;

public interface INMSLoader {

    <T> T loadNMSHandler(Class<T> nmsClass) throws NMSLoadException;

}
