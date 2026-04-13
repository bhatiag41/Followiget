package com.testing.myapp.cache;

import android.content.Context;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CacheManager {
    private static final String CACHE_DIR_NAME = "widget_cache";

    public static class CacheEntry {
        public final String count;
        public final String profilePhotoUrl;
        public final long timestamp;

        public CacheEntry(String count, String profilePhotoUrl, long timestamp) {
            this.count = count;
            this.profilePhotoUrl = profilePhotoUrl;
            this.timestamp = timestamp;
        }
    }

    public static void saveCache(Context context, String platform, String accountId, String count, String profilePhotoUrl) {
        try {
            File cacheDir = new File(context.getFilesDir(), CACHE_DIR_NAME);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            File cacheFile = new File(cacheDir, platform + "_" + accountId + ".json");
            JSONObject json = new JSONObject();
            json.put("count", count);
            json.put("profilePhotoUrl", profilePhotoUrl);
            json.put("timestamp", System.currentTimeMillis());

            try (FileWriter writer = new FileWriter(cacheFile)) {
                writer.write(json.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CacheEntry loadCache(Context context, String platform, String accountId) {
        try {
            File cacheDir = new File(context.getFilesDir(), CACHE_DIR_NAME);
            File cacheFile = new File(cacheDir, platform + "_" + accountId + ".json");

            if (!cacheFile.exists()) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (FileReader reader = new FileReader(cacheFile)) {
                int ch;
                while ((ch = reader.read()) != -1) {
                    sb.append((char) ch);
                }
            }

            JSONObject json = new JSONObject(sb.toString());
            String profileUrl = json.optString("profilePhotoUrl", "");
            return new CacheEntry(json.getString("count"), profileUrl, json.getLong("timestamp"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
