package com.testing.myapp.config;

import android.content.Context;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class WidgetConfigManager {
    private static final String CONFIG_DIR_NAME = "widget_config";

    public static class WidgetConfig {
        public final int widgetId;
        public final String platform; // "instagram", "youtube", "spotify"
        public final String accountId;
        public final boolean isDarkTheme;
        public final String themeStyle; // "minimal", "lines", "gradient"

        public WidgetConfig(int widgetId, String platform, String accountId, boolean isDarkTheme, String themeStyle) {
            this.widgetId = widgetId;
            this.platform = platform;
            this.accountId = accountId;
            this.isDarkTheme = isDarkTheme;
            this.themeStyle = themeStyle;
        }
    }

    public static void saveConfig(Context context, WidgetConfig config) {
        try {
            File configDir = new File(context.getFilesDir(), CONFIG_DIR_NAME);
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            File configFile = new File(configDir, "widget_" + config.widgetId + ".json");
            JSONObject json = new JSONObject();
            json.put("widgetId", config.widgetId);
            json.put("platform", config.platform);
            json.put("accountId", config.accountId);
            json.put("isDarkTheme", config.isDarkTheme);
            json.put("themeStyle", config.themeStyle);

            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static WidgetConfig loadConfig(Context context, int widgetId) {
        try {
            File configDir = new File(context.getFilesDir(), CONFIG_DIR_NAME);
            File configFile = new File(configDir, "widget_" + widgetId + ".json");

            if (!configFile.exists()) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (FileReader reader = new FileReader(configFile)) {
                int ch;
                while ((ch = reader.read()) != -1) {
                    sb.append((char) ch);
                }
            }

            JSONObject json = new JSONObject(sb.toString());
            String tStyle = json.optString("themeStyle", "minimal");
            if (!json.has("themeStyle")) {
                 if ("instagram".equals(json.optString("platform"))) tStyle = "lines";
            }
            return new WidgetConfig(
                    json.getInt("widgetId"),
                    json.getString("platform"),
                    json.getString("accountId"),
                    json.getBoolean("isDarkTheme"),
                    tStyle
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void deleteConfig(Context context, int widgetId) {
        try {
             File configDir = new File(context.getFilesDir(), CONFIG_DIR_NAME);
             File configFile = new File(configDir, "widget_" + widgetId + ".json");
             if (configFile.exists()) {
                 configFile.delete();
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
