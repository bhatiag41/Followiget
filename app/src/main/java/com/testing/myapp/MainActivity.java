package com.testing.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

public class MainActivity extends AppCompatActivity {
    private EditText usernameInput;
    private Button applyButton;
    private Button themeToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameInput = findViewById(R.id.username_input);
        applyButton = findViewById(R.id.apply_button);
        themeToggle = findViewById(R.id.theme_toggle);

        // Load saved username if exists
        String savedUsername = getSharedPreferences("InstagramWidget", MODE_PRIVATE)
                .getString("username", "");
        usernameInput.setText(savedUsername);

        applyButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            if (!username.isEmpty()) {
                saveUsername(username);
                updateWidgets();
                Toast.makeText(this, "Widget will update shortly", Toast.LENGTH_SHORT).show();
            }
        });

        themeToggle.setOnClickListener(v -> {
            boolean isDark = getSharedPreferences("InstagramWidget", MODE_PRIVATE)
                    .getBoolean("dark_theme", true);
            getSharedPreferences("InstagramWidget", MODE_PRIVATE)
                    .edit()
                    .putBoolean("dark_theme", !isDark)
                    .apply();
            updateWidgets();
        });
    }

    private void saveUsername(String username) {
        getSharedPreferences("InstagramWidget", MODE_PRIVATE)
                .edit()
                .putString("username", username)
                .apply();
    }

    private void updateWidgets() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this, FollowerCountWidget.class);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        Intent updateIntent = new Intent(this, FollowerCountWidget.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        sendBroadcast(updateIntent);
    }
}
