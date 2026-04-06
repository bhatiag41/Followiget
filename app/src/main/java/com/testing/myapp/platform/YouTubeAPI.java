package com.testing.myapp.platform;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.testing.myapp.config.GlobalSettingsManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class YouTubeAPI implements SocialPlatform {
    private final Context context;
    private final OkHttpClient client;
    private final Handler mainHandler;

    public YouTubeAPI(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void fetchFollowerCount(String accountId, PlatformCallback callback) {
        String apiKey = GlobalSettingsManager.loadSettings(context).youtubeApiKey;
        if (apiKey.isEmpty()) {
            callback.onError("YouTube API Key not set");
            return;
        }

        String url;
        if (accountId.startsWith("@")) {
            url = "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&forHandle=" + accountId + "&key=" + apiKey;
        } else if (accountId.startsWith("UC") && accountId.length() == 24) {
            url = "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&id=" + accountId + "&key=" + apiKey;
        } else {
             // Fallback to legacy username
            url = "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&forUsername=" + accountId + "&key=" + apiKey;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = "";
                    try {
                        if (response.body() != null) errorBody = response.body().string();
                    } catch (Exception ignored) {}
                    
                    final String errorMsg = "Request failed: " + response.code() + " " + errorBody;
                    mainHandler.post(() -> callback.onError(errorMsg));
                    return;
                }

                try {
                    String jsonString = response.body().string();
                    JSONObject json = new JSONObject(jsonString);
                    
                    if (!json.has("items") || json.getJSONArray("items").length() == 0) {
                        mainHandler.post(() -> callback.onError("Channel not found for ID: " + accountId));
                        return;
                    }

                    JSONObject channel = json.getJSONArray("items").getJSONObject(0);
                    JSONObject snippet = channel.getJSONObject("snippet");
                    JSONObject statistics = channel.getJSONObject("statistics");

                    String displayName = snippet.getString("title");
                    String subscriberCount = statistics.getString("subscriberCount");
                    String profilePhotoUrl = snippet.getJSONObject("thumbnails").getJSONObject("default").getString("url");
                    
                    // Format count
                    int count = Integer.parseInt(subscriberCount);
                    String formattedCount = formatCount(count);

                    PlatformResult result = new PlatformResult(accountId, displayName, formattedCount, profilePhotoUrl);
                    mainHandler.post(() -> callback.onSuccess(result));

                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Error parsing JSON: " + e.getMessage()));
                }
            }
        });
    }
    
    // Simplistic formatting (e.g., 1500 -> 1.5K)
    private String formatCount(int count) {
        if (count < 1000) return String.valueOf(count);
        if (count < 1000000) return String.format("%.1fK", count / 1000.0);
        return String.format("%.1fM", count / 1000000.0);
    }

    @Override
    public String getPlatformName() {
        return "youtube";
    }
}
