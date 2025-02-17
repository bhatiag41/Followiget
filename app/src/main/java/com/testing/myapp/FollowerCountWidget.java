package com.testing.myapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;

public class FollowerCountWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // Get saved user preferences
        String username = context.getSharedPreferences("InstagramWidget", Context.MODE_PRIVATE)
                .getString("username", "");
        boolean isDark = context.getSharedPreferences("InstagramWidget", Context.MODE_PRIVATE)
                .getBoolean("dark_theme", true);

        // Set background based on theme
        views.setInt(R.id.widget_container, "setBackgroundResource",
                isDark ? R.drawable.widget_background_dark : R.drawable.widget_background_light);

        // Set text colors based on theme
        int textColor = isDark ? Color.WHITE : Color.BLACK;
        int secondaryTextColor = isDark ? Color.parseColor("#CCCCCC") : Color.parseColor("#666666");

        views.setTextColor(R.id.follower_count, textColor);
        views.setTextColor(R.id.username_text, textColor);
        views.setTextColor(R.id.display_name, secondaryTextColor);
        views.setTextColor(R.id.follower_tag, secondaryTextColor);

        if (!username.isEmpty()) {
            // Update profile information
            InstagramService service = new InstagramService();
            service.getUserProfile(username, new InstagramService.ProfileCallback() {
                @Override
                public void onSuccess(InstagramService.UserProfile profile) {
                    // Set username and display name
                    views.setTextViewText(R.id.username_text, "@" + profile.username);
                    views.setTextViewText(R.id.display_name, profile.displayName);

                    // Set follower count directly (no formatting needed)
                    views.setTextViewText(R.id.follower_count, profile.followerCount);



                    // Load profile photo using Glide
                    AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.profile_image, views, widgetId);
                    Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(profile.profilePhotoUrl)
                            .circleCrop()
                            .into(appWidgetTarget);

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
            views.setViewVisibility(R.id.display_name, View.GONE);

        }

        // Add click listener to open main activity
        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        appWidgetManager.updateAppWidget(widgetId, views);
    }
}