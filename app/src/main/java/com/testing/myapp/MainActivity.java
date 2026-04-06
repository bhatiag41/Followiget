package com.testing.myapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.add_widget_button).setOnClickListener(v -> {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            ComponentName myProvider = new ComponentName(this, FollowerCountWidget.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                    appWidgetManager.requestPinAppWidget(myProvider, null, null);
                } else {
                    Toast.makeText(this, "Your launcher does not support direct widget placement. Please add from the home screen.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Please add the widget from your home screen.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
