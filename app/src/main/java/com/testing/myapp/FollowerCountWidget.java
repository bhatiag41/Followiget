package com.testing.myapp;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import java.util.Locale;

public class FollowerCountWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // Get saved username and theme preference
        String username = context.getSharedPreferences("InstagramWidget", Context.MODE_PRIVATE)
                .getString("username", "");
        boolean isDark = context.getSharedPreferences("InstagramWidget", Context.MODE_PRIVATE)
                .getBoolean("dark_theme", true);

        // Set background based on theme
        views.setInt(R.id.widget_container, "setBackgroundResource",
                isDark ? R.drawable.widget_background_dark : R.drawable.widget_background_light);

        // Set text color based on theme
        int textColor = isDark ? Color.WHITE : Color.BLACK;
        views.setTextColor(R.id.follower_count, textColor);
        views.setTextColor(R.id.username_text, textColor);

        if (!username.isEmpty()) {
            views.setTextViewText(R.id.username_text, "@" + username);

            // Update follower count
            InstagramService service = new InstagramService();
            service.getFollowerCount(username, new InstagramService.FollowerCallback() {
                @Override
                public void onSuccess(int count) {
                    String formattedCount = formatFollowerCount(count);
                    views.setTextViewText(R.id.follower_count, formattedCount);
                    views.setViewVisibility(R.id.loading_indicator, View.GONE);
                    appWidgetManager.updateAppWidget(widgetId, views);
                }

                @Override
                public void onError(String error) {
                    views.setTextViewText(R.id.follower_count, "---");
                    views.setViewVisibility(R.id.loading_indicator, View.GONE);
                    appWidgetManager.updateAppWidget(widgetId, views);
                }
            });
        } else {
            views.setTextViewText(R.id.username_text, "Not configured");
            views.setTextViewText(R.id.follower_count, "Tap to set up");
        }

        // Add click listener to open main activity
        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        appWidgetManager.updateAppWidget(widgetId, views);
    }





    private String formatFollowerCount(int count) {
        if (count > 99999) {
            System.out.println("mil");
            System.out.println(count);

            return String.format( "%.1fM", count / 1000000.0);// Less than 1K - show exact number
        }
        if (count > 999) {
            System.out.println("thou");
            return String.format( "%.1fK", count / 1000.0);  // 1K-9.9K - show with one decimal
        }
        else {
              // 1M+ - show with one decimal
            System.out.println("simple");
            return String.valueOf(count);
        }
    }
}
