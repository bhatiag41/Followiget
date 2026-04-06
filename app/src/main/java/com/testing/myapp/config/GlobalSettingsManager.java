package com.testing.myapp.config;

import android.content.Context;
import com.testing.myapp.BuildConfig;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class GlobalSettingsManager {
    private static final String SETTINGS_FILE_NAME = "global_settings.json";

    public static class GlobalSettings {
        // Defaults come from BuildConfig, which reads from .env at build time
        public String youtubeApiKey    = BuildConfig.YOUTUBE_API_KEY;
        public String spotifyClientId  = BuildConfig.SPOTIFY_CLIENT_ID;
        public String spotifyClientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET;
    }

    public static void saveSettings(Context context, GlobalSettings settings) {
         try {
            File settingsFile = new File(context.getFilesDir(), SETTINGS_FILE_NAME);
            JSONObject json = new JSONObject();
            json.put("youtubeApiKey", settings.youtubeApiKey);
            json.put("spotifyClientId", settings.spotifyClientId);
            json.put("spotifyClientSecret", settings.spotifyClientSecret);

            try (FileWriter writer = new FileWriter(settingsFile)) {
                writer.write(json.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GlobalSettings loadSettings(Context context) {
         GlobalSettings settings = new GlobalSettings();
         try {
            File settingsFile = new File(context.getFilesDir(), SETTINGS_FILE_NAME);

            if (!settingsFile.exists()) {
                return settings;
            }

            StringBuilder sb = new StringBuilder();
            try (FileReader reader = new FileReader(settingsFile)) {
                int ch;
                while ((ch = reader.read()) != -1) {
                    sb.append((char) ch);
                }
            }

            JSONObject json = new JSONObject(sb.toString());

            // Load saved value; fall back to BuildConfig key, then fallback key
            settings.youtubeApiKey = json.optString("youtubeApiKey", settings.youtubeApiKey);
            if (settings.youtubeApiKey.isEmpty()) settings.youtubeApiKey = BuildConfig.YOUTUBE_API_KEY_FALLBACK;

            settings.spotifyClientId = json.optString("spotifyClientId", settings.spotifyClientId);
            if (settings.spotifyClientId.isEmpty()) settings.spotifyClientId = BuildConfig.SPOTIFY_CLIENT_ID;

            settings.spotifyClientSecret = json.optString("spotifyClientSecret", settings.spotifyClientSecret);
            if (settings.spotifyClientSecret.isEmpty()) settings.spotifyClientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET;

            return settings;
        } catch (Exception e) {
            e.printStackTrace();
            return settings;
        }
    }
}
