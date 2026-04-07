package com.testing.myapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

import com.testing.myapp.config.GlobalSettingsManager;

public class SettingsActivity extends AppCompatActivity {

    private EditText spotifyClientIdInput;
    private EditText spotifyClientSecretInput;
    private SwitchCompat darkModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());

        spotifyClientIdInput = findViewById(R.id.spotify_client_id_input);
        spotifyClientSecretInput = findViewById(R.id.spotify_client_secret_input);
        darkModeSwitch = findViewById(R.id.dark_mode_switch);

        // Load existing settings
        GlobalSettingsManager.GlobalSettings settings = GlobalSettingsManager.loadSettings(this);
        spotifyClientIdInput.setText(settings.spotifyClientId);
        spotifyClientSecretInput.setText(settings.spotifyClientSecret);
        darkModeSwitch.setChecked(settings.isDarkMode);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.isDarkMode = isChecked;
            GlobalSettingsManager.saveSettings(this, settings);
            
            // Set app-wide theme
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            
            // Update all widgets
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, FollowerCountWidget.class));
            
            Intent updateIntent = new Intent(this, FollowerCountWidget.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            sendBroadcast(updateIntent);
        });

        findViewById(R.id.save_spotify_btn).setOnClickListener(v -> {
            settings.spotifyClientId = spotifyClientIdInput.getText().toString().trim();
            settings.spotifyClientSecret = spotifyClientSecretInput.getText().toString().trim();
            GlobalSettingsManager.saveSettings(this, settings);
            Toast.makeText(this, "Spotify credentials saved", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.contact_row).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"feedback@example.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for Follower Widgets");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
