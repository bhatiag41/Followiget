package com.testing.myapp.config;

import android.content.Context;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class GlobalSettingsManager {
    private static final String SETTINGS_FILE_NAME = "global_settings.json";

    public static class GlobalSettings {
        public String youtubeApiKey = "AIzaSyBnm5kz9TDbDXiyzwFaXeu157rbQOuIGLU";
        public String spotifyClientId = "5059b29671c34ab69ba290b1f2ef309e";
        public String spotifyClientSecret = "fe7168feb5fa4051a0744b8831702fdc";
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
            settings.youtubeApiKey = json.optString("youtubeApiKey", settings.youtubeApiKey);
            if (settings.youtubeApiKey.isEmpty()) settings.youtubeApiKey = "AIzaSyCZVCh_qgH1Xe27mbZQep1sw9nwsSoRD6A";
            
            settings.spotifyClientId = json.optString("spotifyClientId", settings.spotifyClientId);
            if (settings.spotifyClientId.isEmpty()) settings.spotifyClientId = "5059b29671c34ab69ba290b1f2ef309e";
            
            settings.spotifyClientSecret = json.optString("spotifyClientSecret", settings.spotifyClientSecret);
            if (settings.spotifyClientSecret.isEmpty()) settings.spotifyClientSecret = "fe7168feb5fa4051a0744b8831702fdc";
            
            return settings;
        } catch (Exception e) {
            e.printStackTrace();
            return settings;
        }
    }
}
