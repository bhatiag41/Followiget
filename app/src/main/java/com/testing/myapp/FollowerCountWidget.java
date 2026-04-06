package com.testing.myapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.testing.myapp.cache.CacheManager;
import com.testing.myapp.config.WidgetConfigManager;
import com.testing.myapp.platform.PlatformFactory;
import com.testing.myapp.platform.SocialPlatform;
import com.testing.myapp.utils.WidgetCanvasRenderer;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import com.bumptech.glide.request.transition.Transition;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FollowerCountWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetManager.getAppWidgetOptions(appWidgetId));
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            WidgetConfigManager.deleteConfig(context, appWidgetId);
        }
    }

    private static RemoteViews buildBaseViews(Context context, AppWidgetManager appWidgetManager, int appWidgetId, WidgetConfigManager.WidgetConfig config, Bundle options) {
        int minWidth  = options != null ? options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,  200) : 200;
        int minHeight = options != null ? options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 100) : 100;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // Always show username — the card always has room
        views.setViewVisibility(R.id.username_text, View.VISIBLE);

        // Bitmap canvas size matches actual widget dimensions
        float density = context.getResources().getDisplayMetrics().density;
        int bgWidth  = Math.max((int) (minWidth  * density), 200);
        int bgHeight = Math.max((int) (minHeight * density), 100);

        boolean isYouTube = "youtube".equals(config.platform);
        boolean isRibbons = "lines".equals(config.themeStyle);
        boolean isMinimal = "minimal".equals(config.themeStyle);
        boolean isDark    = config.isDarkTheme;

        if (isMinimal) {
            // --- MINIMAL ---
            Bitmap bg = WidgetCanvasRenderer.createMinimalBackground(context, bgWidth, bgHeight, isDark);
            views.setImageViewBitmap(R.id.widget_background_image, bg);
            views.setViewVisibility(R.id.scrim_overlay, View.GONE);

            if (isYouTube) {
                views.setInt(R.id.photo_container, "setBackgroundResource", R.drawable.photo_ring_minimal_youtube);
            } else {
                views.setInt(R.id.photo_container, "setBackgroundResource", R.drawable.photo_ring_minimal_instagram);
            }

            int themeColor = isYouTube
                ? context.getResources().getColor(R.color.widget_minimal_yt)
                : context.getResources().getColor(R.color.widget_minimal_ig);

            int countColor = isDark
                ? context.getResources().getColor(R.color.widget_text_primary)
                : context.getResources().getColor(R.color.widget_text_primary_light);
            int tagColor = isDark
                ? context.getResources().getColor(R.color.widget_text_followers_tag)
                : context.getResources().getColor(R.color.widget_text_followers_tag_light);

            views.setTextColor(R.id.follower_tag, tagColor);
            views.setTextColor(R.id.follower_count_1, countColor);
            views.setTextColor(R.id.follower_count_2, countColor);
            views.setTextColor(R.id.username_text, themeColor);

        } else if (isRibbons) {
            // --- LINES / RIBBONS ---
            Bitmap bg = WidgetCanvasRenderer.createRibbonWavesBackground(context, bgWidth, bgHeight, config.platform);
            views.setImageViewBitmap(R.id.widget_background_image, bg);
            views.setViewVisibility(R.id.scrim_overlay, View.GONE);
            views.setInt(R.id.photo_container, "setBackgroundResource", R.drawable.photo_ring_gradient);

            // Ribbons always have dark card bg → white text
            views.setTextColor(R.id.follower_tag,     context.getResources().getColor(R.color.widget_text_followers_tag_ribbons));
            views.setTextColor(R.id.follower_count_1, context.getResources().getColor(R.color.widget_text_primary));
            views.setTextColor(R.id.follower_count_2, context.getResources().getColor(R.color.widget_text_primary));
            views.setTextColor(R.id.username_text,    context.getResources().getColor(R.color.widget_text_username_ribbons));

        } else {
            // --- GRADIENT ---
            // Programmatic so we can control dark/light mode endpoint
            Bitmap bg = WidgetCanvasRenderer.createGradientBackground(context, bgWidth, bgHeight, config.platform, isDark);
            views.setImageViewBitmap(R.id.widget_background_image, bg);
            views.setViewVisibility(R.id.scrim_overlay, View.GONE);
            views.setInt(R.id.photo_container, "setBackgroundResource", R.drawable.photo_ring_gradient);

            // Left side of gradient is always vivid/dark → white text safe there
            // Right side: dark mode = black bg → white text; light mode = white bg → dark text
            int countColor = isDark
                ? context.getResources().getColor(R.color.widget_text_primary)
                : context.getResources().getColor(R.color.widget_text_primary_light);
            int tagColor = isDark
                ? context.getResources().getColor(R.color.widget_text_followers_tag)
                : context.getResources().getColor(R.color.widget_text_followers_tag_light);
            int usernameColor = isDark
                ? context.getResources().getColor(R.color.widget_text_username)
                : context.getResources().getColor(R.color.widget_text_username_light);

            views.setTextColor(R.id.follower_tag,     tagColor);
            views.setTextColor(R.id.follower_count_1, countColor);
            views.setTextColor(R.id.follower_count_2, countColor);
            views.setTextColor(R.id.username_text,    usernameColor);
        }

        views.setTextViewText(R.id.follower_tag, isYouTube ? "subscribers" : "followers");

        Intent configIntent = new Intent(context, WidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pi = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pi);

        return views;
    }


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle options) {
        WidgetConfigManager.WidgetConfig config = WidgetConfigManager.loadConfig(context, appWidgetId);

        if (config == null) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.username_text, "Not configured");
            views.setTextViewText(R.id.follower_count_1, "Tap to set up");
            views.setTextViewText(R.id.follower_count_2, "Tap to set up");
            
            Intent configIntent = new Intent(context, WidgetConfigureActivity.class);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        RemoteViews views = buildBaseViews(context, appWidgetManager, appWidgetId, config, options);

        SocialPlatform platform = PlatformFactory.getPlatform(context, config.platform);
        if (platform == null) return;

        platform.fetchFollowerCount(config.accountId, new SocialPlatform.PlatformCallback() {
            @Override
            public void onSuccess(SocialPlatform.PlatformResult result) {
                // Save to cache
                CacheManager.saveCache(context, config.platform, config.accountId, result.count, result.profilePhotoUrl);

                // Update ALL widgets that rely on this exact same account/platform to prevent out-of-sync cache values
                int[] allIds = appWidgetManager.getAppWidgetIds(new android.content.ComponentName(context, FollowerCountWidget.class));
                for (int id : allIds) {
                    WidgetConfigManager.WidgetConfig c = WidgetConfigManager.loadConfig(context, id);
                    if (c != null && c.platform.equals(config.platform) && c.accountId.equals(config.accountId)) {
                        Bundle opts = appWidgetManager.getAppWidgetOptions(id);
                        RemoteViews specificViews = buildBaseViews(context, appWidgetManager, id, c, opts);
                        updateViews(context, appWidgetManager, id, specificViews, result, false, c.platform);
                    }
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("FollowerCountWidget", "Error fetching from " + config.platform + " for " + config.accountId + ": " + error);
                // Try to load from cache
                CacheManager.CacheEntry cacheEntry = CacheManager.loadCache(context, config.platform, config.accountId);
                if (cacheEntry != null) {
                     // Create a dummy result using cached count
                     SocialPlatform.PlatformResult cachedResult = new SocialPlatform.PlatformResult(
                             config.accountId, config.accountId, cacheEntry.count, cacheEntry.profilePhotoUrl 
                     );
                     updateViews(context, appWidgetManager, appWidgetId, views, cachedResult, true, config.platform);
                } else {
                     views.setTextViewText(R.id.follower_count_1, "---");
                     views.setTextViewText(R.id.follower_count_2, "---");
                     views.showNext(R.id.follower_count_flipper);
                     views.setViewVisibility(R.id.loading_indicator, View.GONE);
                     appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        });
    }
    
    private static void updateViews(Context context, AppWidgetManager appWidgetManager, int appWidgetId, 
                                    RemoteViews views, SocialPlatform.PlatformResult result, boolean isCached, String platformName) {
        
        String prefix = "";
        if ("instagram".equals(platformName)) prefix = "@";

        views.setTextViewText(R.id.username_text, prefix + result.accountId);

        String countText = result.count;
        views.setTextViewText(R.id.follower_count_1, countText);
        views.setTextViewText(R.id.follower_count_2, countText);
        views.showNext(R.id.follower_count_flipper);

        if (!result.profilePhotoUrl.isEmpty()) {
            AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.profile_image, views, appWidgetId) {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    views.setImageViewBitmap(R.id.profile_image, resource);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            };
            Glide.with(context.getApplicationContext())
                 .asBitmap()
                 .load(result.profilePhotoUrl)
                 .circleCrop()
                 .into(appWidgetTarget);
        } else {
             views.setImageViewBitmap(R.id.profile_image, null);
        }

        // Badge completely disabled per constraints
        // if (isCached) {
        //      views.setViewVisibility(R.id.cached_badge, View.VISIBLE);
        // } else {
        //      views.setViewVisibility(R.id.cached_badge, View.GONE);
        // }
        
        // --- 5. Profile Picture Deep Links ---
        Intent profileIntent = new Intent(Intent.ACTION_VIEW);
        profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        if ("instagram".equals(platformName)) {
             // The native IG app usually drops https App Links to the home feed in newer versions.
             // Using the explicit custom internal schematic forces the app strictly inside the User Profile view.
             profileIntent.setData(Uri.parse("instagram://user?username=" + result.accountId));
        } else if ("youtube".equals(platformName)) {
             if (result.accountId.startsWith("UC") && result.accountId.length() > 10) {
                 profileIntent.setData(Uri.parse("https://www.youtube.com/channel/" + result.accountId));
             } else {
                 profileIntent.setData(Uri.parse("https://www.youtube.com/" + result.accountId));
             }
        } else if ("spotify".equals(platformName)) {
             // Spotify Native URI to force the app to open the entity
             profileIntent.setData(Uri.parse("spotify:user:" + result.accountId));
        }

        // Create a PendingIntent for the profile picture specifically
        PendingIntent profilePendingIntent = PendingIntent.getActivity(
                context, 
                appWidgetId + 10000, // offset ID so it doesn't conflict with widget config intent
                profileIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.profile_image, profilePendingIntent);

        views.setViewVisibility(R.id.loading_indicator, View.GONE);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}