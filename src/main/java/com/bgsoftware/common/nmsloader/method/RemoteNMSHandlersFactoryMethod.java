package com.bgsoftware.common.nmsloader.method;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class RemoteNMSHandlersFactoryMethod implements INMSHandlersFactoryMethod {

    private static final String DOWNLOAD_URL = "https://api.bg-software.com/v1/nms/download/";

    private static final Gson GSON = new GsonBuilder().create();

    private static final RemoteNMSHandlersFactoryMethod INSTANCE = new RemoteNMSHandlersFactoryMethod();

    public static RemoteNMSHandlersFactoryMethod getInstance() {
        return INSTANCE;
    }

    private RemoteNMSHandlersFactoryMethod() {

    }

    @Override
    public INMSLoader createNMSLoader(JavaPlugin plugin, String nmsPackageVersionName) throws NMSLoadException {
        File cacheFolder = new File(plugin.getDataFolder(), ".cache");
        String version = null;

        if (cacheFolder.exists()) {
            version = getVersionForNMS(cacheFolder, nmsPackageVersionName);
        }

        File nmsCachedFile = new File(cacheFolder, nmsPackageVersionName + ".jar");

        try {
            String newVersion = downloadRemoteNMSJar(nmsCachedFile, nmsPackageVersionName, version);

            // If we are here, it means we downloaded a new jar.
            // Let's update the versions file.

            updateVersionForNMS(cacheFolder, nmsPackageVersionName, newVersion);

            throw new NMSLoadException("Trigger CachedNMSHandlersFactoryMethod");
        } catch (NMSLoadException error) {
            throw error;
        } catch (Exception error) {
            throw new NMSLoadException("Cannot find remote nms handler", error);
        }
    }

    private static String getVersionForNMS(File cacheFolder, String nmsPackageVersionName) {
        // Cache file exists, let's read its version.
        File versionsFile = new File(cacheFolder, "versions.json");

        if (versionsFile.exists()) {
            try {
                JsonObject versions = GSON.fromJson(new FileReader(versionsFile), JsonObject.class);
                return versions.get(nmsPackageVersionName).getAsString();
            } catch (Exception error) {
                // Ignored
            }
        }

        return null;
    }

    private static void updateVersionForNMS(File cacheFolder, String nmsPackageVersionName, String version) throws IOException {
        // Cache file exists, let's read its version.
        File versionsFile = new File(cacheFolder, "versions.json");
        JsonObject versions = null;

        if (versionsFile.exists()) {
            try {
                versions = GSON.fromJson(new FileReader(versionsFile), JsonObject.class);
            } catch (Exception error) {
                // Ignored
            }
        }

        if (versions == null) {
            versions = new JsonObject();
        }

        versions.addProperty(nmsPackageVersionName, version);

        versionsFile.getParentFile().mkdirs();
        versionsFile.createNewFile();

        try (FileWriter writer = new FileWriter(versionsFile)) {
            writer.write(GSON.toJson(versions));
        }
    }

    private static String downloadRemoteNMSJar(File cachedNMSFile, String nmsPackageVersionName, @Nullable String version) throws Exception {
        String downloadUrl = DOWNLOAD_URL + nmsPackageVersionName + "/?version=" + (version == null ? "0" : version);

        HttpsURLConnection conn = (HttpsURLConnection) new URL(downloadUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(true);

        StringBuilder jsonContents = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null)
                jsonContents.append(line);
        }

        JsonObject response = GSON.fromJson(jsonContents.toString(), JsonObject.class);

        if (response.has("error"))
            throw new NMSLoadException("Failed to download remote nms jar: " + response.get("error"));

        if (!response.has("data")) {
            throw new NMSLoadException("Up to date");
        }

        cachedNMSFile.getParentFile().mkdirs();
        cachedNMSFile.delete();

        byte[] nmsFileData = Base64.getDecoder().decode(response.get("data").getAsString());
        Files.write(Paths.get(cachedNMSFile.toURI()), nmsFileData);

        return response.get("version").getAsString();
    }

}
