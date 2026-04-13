package com.testing.myapp;

import androidx.appcompat.app.AppCompatActivity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.testing.myapp.config.WidgetConfigManager;
import com.testing.myapp.utils.WidgetCanvasRenderer;

public class WidgetConfigureActivity extends AppCompatActivity {

    int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    EditText accountIdInput;
    
    // Platform Selectors
    View platformIgContainer;
    View platformYtContainer;
    View platformSpotifyContainer;
    
    // The background containers where we apply the circular colors
    View platformIgBg;
    View platformYtBg;
    View platformSpotifyBg;

    TextView platformIgLabel;
    TextView platformYtLabel;
    TextView platformSpotifyLabel;
    
    // Style Selectors
    CardView styleMinimalCard;
    CardView styleLinesCard;
    CardView styleGradientCard;

    TextView styleMinimal;
    TextView styleLines;
    TextView styleGradient;

    String selectedPlatform = "instagram";
    String selectedStyle = "minimal";
    
    ImageView spotifyWavyBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_widget_configure);
        
        accountIdInput = findViewById(R.id.account_id_input);
        
        platformIgContainer = findViewById(R.id.platform_ig_container);
        platformYtContainer = findViewById(R.id.platform_yt_container);
        platformSpotifyContainer = findViewById(R.id.platform_spotify_container);
        
        platformIgBg = findViewById(R.id.platform_ig_bg);
        platformYtBg = findViewById(R.id.platform_yt_bg);
        platformSpotifyBg = findViewById(R.id.platform_spotify_bg);

        platformIgLabel = findViewById(R.id.platform_ig_label);
        platformYtLabel = findViewById(R.id.platform_yt_label);
        platformSpotifyLabel = findViewById(R.id.platform_spotify_label);
        
        styleMinimalCard = findViewById(R.id.style_minimal_card);
        styleLinesCard = findViewById(R.id.style_lines_card);
        styleGradientCard = findViewById(R.id.style_gradient_card);

        styleMinimal = findViewById(R.id.style_minimal);
        styleLines = findViewById(R.id.style_lines);
        styleGradient = findViewById(R.id.style_gradient);

        spotifyWavyBg = findViewById(R.id.spotify_wavy_bg);

        // Generate the wavy banner bitmap once layout is measured
        spotifyWavyBg.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                spotifyWavyBg.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int w = spotifyWavyBg.getWidth();
                int h = spotifyWavyBg.getHeight();
                if (w > 0 && h > 0) {
                    Bitmap banner = WidgetCanvasRenderer.createSpotifyBannerBitmap(WidgetConfigureActivity.this, w, h);
                    spotifyWavyBg.setImageBitmap(banner);
                }
            }
        });

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        
        // Add settings button mapping added per user feedback
        findViewById(R.id.settings_button).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        platformIgContainer.setOnClickListener(v -> selectPlatform("instagram"));
        platformYtContainer.setOnClickListener(v -> selectPlatform("youtube"));
        platformSpotifyContainer.setOnClickListener(v -> selectPlatform("spotify"));

        styleMinimalCard.setOnClickListener(v -> selectStyle("minimal"));
        styleLinesCard.setOnClickListener(v -> selectStyle("lines"));
        styleGradientCard.setOnClickListener(v -> selectStyle("gradient"));

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
                    true, 
                    selectedStyle
            );
            WidgetConfigManager.saveConfig(this, newConfig);
            
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
        
        // Reset all
        styleMinimalCard.setCardBackgroundColor(Color.TRANSPARENT);
        styleLinesCard.setCardBackgroundColor(Color.TRANSPARENT);
        styleGradientCard.setCardBackgroundColor(Color.TRANSPARENT);
        
        styleMinimal.setTextColor(Color.parseColor("#888888"));
        styleLines.setTextColor(Color.parseColor("#888888"));
        styleGradient.setTextColor(Color.parseColor("#888888"));
        
        if (style.equals("gradient")) {
            styleGradientCard.setCardBackgroundColor(Color.parseColor("#333333"));
            styleGradient.setTextColor(Color.WHITE);
        } else if (style.equals("lines")) {
            styleLinesCard.setCardBackgroundColor(Color.parseColor("#333333"));
            styleLines.setTextColor(Color.WHITE);
        } else {
            styleMinimalCard.setCardBackgroundColor(Color.parseColor("#333333"));
            styleMinimal.setTextColor(Color.WHITE);
        }
    }
    
    private void selectPlatform(String platform) {
        selectedPlatform = platform;
        
        // Reset all to unselected visual (#666666 grey via view background color)
        platformIgBg.setBackgroundColor(Color.parseColor("#666666"));
        platformYtBg.setBackgroundColor(Color.parseColor("#666666"));
        platformSpotifyBg.setBackgroundColor(Color.parseColor("#666666"));
        
        platformIgLabel.setVisibility(View.INVISIBLE);
        platformYtLabel.setVisibility(View.INVISIBLE);
        platformSpotifyLabel.setVisibility(View.INVISIBLE);
        
        if (platform.equals("youtube")) {
            platformYtBg.setBackgroundColor(Color.parseColor("#FF0000")); // YouTube Red
            platformYtLabel.setVisibility(View.VISIBLE);
            
            accountIdInput.setHint("YouTube Handle");
        } else if (platform.equals("spotify")) {
            platformSpotifyBg.setBackgroundColor(Color.parseColor("#1DB954")); // Spotify Green
            platformSpotifyLabel.setVisibility(View.VISIBLE);

            accountIdInput.setHint("Spotify Artist URL/ID");
        } else {
            // instagram
            platformIgBg.setBackgroundResource(R.drawable.ig_gradient_bg); // IG Gradient
            platformIgLabel.setVisibility(View.VISIBLE);

            accountIdInput.setHint("Instagram Username");
        }

        // Show / hide the wavy Spotify banner with a smooth fade
        boolean isSpotify = "spotify".equals(platform);
        spotifyWavyBg.animate().cancel();
        spotifyWavyBg.animate()
            .alpha(isSpotify ? 1f : 0f)
            .setDuration(400)
            .start();
    }
}
