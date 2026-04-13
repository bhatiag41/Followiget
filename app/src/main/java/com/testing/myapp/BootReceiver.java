package com.testing.myapp;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Re-initialises every FollowerCountWidget after the device boots.
 *
 * Without this, Android restores widget slots on the launcher but the
 * AppWidgetProvider's onUpdate() is NOT called automatically on reboot,
 * so all widgets show blank / reset state until the next periodic update.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                && !"android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            return;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, FollowerCountWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        if (appWidgetIds == null || appWidgetIds.length == 0) return;

        // Re-trigger the same update logic used by onUpdate()
        for (int appWidgetId : appWidgetIds) {
            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
            FollowerCountWidget.updateAppWidget(context, appWidgetManager, appWidgetId, options);
        }
    }
}
