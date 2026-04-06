package com.testing.myapp;

import androidx.appcompat.app.AppCompatActivity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.SwitchCompat;
import com.testing.myapp.config.WidgetConfigManager;
import com.testing.myapp.config.GlobalSettingsManager;

public class WidgetConfigureActivity extends AppCompatActivity {

    int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    EditText accountIdInput;
    SwitchCompat themeSwitch;
    
    TextView platformIg;
    TextView platformYt;
    TextView platformSpotify;
    
    TextView styleMinimal;
    TextView styleLines;
    TextView styleGradient;
    
    CardView inputIconCard;
    ImageView inputIcon;
    
    View spotifyContainer;
    EditText spotifyIdInput;
    EditText spotifySecretInput;

    String selectedPlatform = "instagram";
    String selectedStyle = "minimal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_widget_configure);
        
        accountIdInput = findViewById(R.id.account_id_input);
        themeSwitch = findViewById(R.id.theme_switch);
        
        platformIg = findViewById(R.id.platform_ig);
        platformYt = findViewById(R.id.platform_yt);
        platformSpotify = findViewById(R.id.platform_spotify);
        
        styleMinimal = findViewById(R.id.style_minimal);
        styleLines = findViewById(R.id.style_lines);
        styleGradient = findViewById(R.id.style_gradient);
        
        inputIconCard = findViewById(R.id.input_icon_card);
        inputIcon = findViewById(R.id.input_icon);
        
        spotifyContainer = findViewById(R.id.spotify_credentials_container);
        spotifyIdInput = findViewById(R.id.spotify_client_id_input);
        spotifySecretInput = findViewById(R.id.spotify_client_secret_input);

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());

        GlobalSettingsManager.GlobalSettings settings = GlobalSettingsManager.loadSettings(this);
        spotifyIdInput.setText(settings.spotifyClientId);
        spotifySecretInput.setText(settings.spotifyClientSecret);

        platformIg.setOnClickListener(v -> selectPlatform("instagram"));
        platformYt.setOnClickListener(v -> selectPlatform("youtube"));
        platformSpotify.setOnClickListener(v -> selectPlatform("spotify"));

        styleMinimal.setOnClickListener(v -> selectStyle("minimal"));
        styleLines.setOnClickListener(v -> selectStyle("lines"));
        styleGradient.setOnClickListener(v -> selectStyle("gradient"));

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        WidgetConfigManager.WidgetConfig config = WidgetConfigManager.loadConfig(this, appWidgetId);
        if (config != null) {
            accountIdInput.setText(config.accountId);
            themeSwitch.setChecked(config.isDarkTheme);
            selectPlatform(config.platform);
            selectStyle(config.themeStyle);
        } else {
            selectPlatform("instagram");
            selectStyle("minimal");
        }

        findViewById(R.id.add_button).setOnClickListener(v -> {
            
            String platformId = selectedPlatform;
            String inputId = accountIdInput.getText().toString().trim();
            
            if (platformId.equals("spotify")) {
                if (inputId.contains("spotify.com/artist/")) {
                     inputId = inputId.substring(inputId.indexOf("artist/") + 7);
                     if (inputId.contains("?")) inputId = inputId.substring(0, inputId.indexOf("?"));
                } else if (inputId.startsWith("spotify:artist:")) {
                     inputId = inputId.replace("spotify:artist:", "");
                }
            } else if (platformId.equals("youtube")) {
                if (inputId.contains("youtube.com/@")) {
                    inputId = "@" + inputId.substring(inputId.indexOf("@") + 1);
                    if (inputId.contains("?")) inputId = inputId.substring(0, inputId.indexOf("?"));
                    if (inputId.contains("/")) inputId = inputId.substring(0, inputId.indexOf("/"));
                } else if (inputId.contains("youtube.com/channel/")) {
                    inputId = inputId.substring(inputId.indexOf("channel/") + 8);
                    if (inputId.contains("?")) inputId = inputId.substring(0, inputId.indexOf("?"));
                    if (inputId.contains("/")) inputId = inputId.substring(0, inputId.indexOf("/"));
                }
            } else if (platformId.equals("instagram")) {
                if (inputId.contains("instagram.com/")) {
                    inputId = inputId.substring(inputId.indexOf("instagram.com/") + 14);
                    if (inputId.contains("/")) inputId = inputId.substring(0, inputId.indexOf("/"));
                    if (inputId.contains("?")) inputId = inputId.substring(0, inputId.indexOf("?"));
                }
            }

            WidgetConfigManager.WidgetConfig newConfig = new WidgetConfigManager.WidgetConfig(
                    appWidgetId,
                    platformId,
                    inputId,
                    themeSwitch.isChecked(),
                    selectedStyle
            );
            WidgetConfigManager.saveConfig(this, newConfig);

            if (platformId.equals("spotify")) {
                settings.spotifyClientId = spotifyIdInput.getText().toString().trim();
                settings.spotifyClientSecret = spotifySecretInput.getText().toString().trim();
                GlobalSettingsManager.saveSettings(this, settings);
            }
            
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            FollowerCountWidget.updateAppWidget(this, appWidgetManager, appWidgetId, appWidgetManager.getAppWidgetOptions(appWidgetId));
            
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        });
    }

    private void selectStyle(String style) {
        selectedStyle = style;
        
        styleMinimal.setBackgroundResource(R.drawable.pill_option_inactive);
        styleMinimal.setTextColor(Color.parseColor("#8A8A8A"));
        
        styleLines.setBackgroundResource(R.drawable.pill_option_inactive);
        styleLines.setTextColor(Color.parseColor("#8A8A8A"));
        
        styleGradient.setBackgroundResource(R.drawable.pill_option_inactive);
        styleGradient.setTextColor(Color.parseColor("#8A8A8A"));
        
        if (style.equals("gradient")) {
            styleGradient.setBackgroundResource(R.drawable.pill_option_active);
            styleGradient.setTextColor(Color.WHITE);
        } else if (style.equals("lines")) {
            styleLines.setBackgroundResource(R.drawable.pill_option_active);
            styleLines.setTextColor(Color.WHITE);
        } else {
            styleMinimal.setBackgroundResource(R.drawable.pill_option_active);
            styleMinimal.setTextColor(Color.WHITE);
        }
    }
    
    private void selectPlatform(String platform) {
        selectedPlatform = platform;
        
        platformIg.setBackgroundResource(R.drawable.pill_option_inactive);
        platformIg.setTextColor(Color.parseColor("#8A8A8A"));
        
        platformYt.setBackgroundResource(R.drawable.pill_option_inactive);
        platformYt.setTextColor(Color.parseColor("#8A8A8A"));
        
        platformSpotify.setBackgroundResource(R.drawable.pill_option_inactive);
        platformSpotify.setTextColor(Color.parseColor("#8A8A8A"));
        
        spotifyContainer.setVisibility(View.GONE);
        
        if (platform.equals("youtube")) {
            platformYt.setBackgroundResource(R.drawable.pill_option_active);
            platformYt.setTextColor(Color.WHITE);
            inputIconCard.setCardBackgroundColor(Color.parseColor("#D2F75B"));
            inputIcon.setImageResource(R.drawable.ic_youtube);
            accountIdInput.setHint("YouTube Handle");
        } else if (platform.equals("spotify")) {
            platformSpotify.setBackgroundResource(R.drawable.pill_option_active);
            platformSpotify.setTextColor(Color.WHITE);
            inputIconCard.setCardBackgroundColor(Color.parseColor("#1ED760"));
            inputIcon.setImageResource(R.drawable.ic_spotify);
            accountIdInput.setHint("Spotify Artist URL/ID");
            spotifyContainer.setVisibility(View.VISIBLE);
        } else {
            // instagram
            platformIg.setBackgroundResource(R.drawable.pill_option_active);
            platformIg.setTextColor(Color.WHITE);
            inputIconCard.setCardBackgroundColor(Color.parseColor("#C8A2F9"));
            inputIcon.setImageResource(R.drawable.ic_instagram);
            accountIdInput.setHint("Instagram Username");
        }
    }
}
