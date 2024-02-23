package com.bgsoftware.common.nmsloader.internal;

public class NMSVersionRequirement {

    public final int requiredDataVersion;
    public final String versionName;

    public NMSVersionRequirement(int requiredDataVersion, String versionName) {
        this.requiredDataVersion = requiredDataVersion;
        this.versionName = versionName;
    }

}
