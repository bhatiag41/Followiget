package com.testing.myapp.platform;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.testing.myapp.config.GlobalSettingsManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.io.IOException;
import android.util.Base64;
import java.util.concurrent.TimeUnit;

public class SpotifyAPI implements SocialPlatform {
    private final Context context;
    private final OkHttpClient client;
    private final Handler mainHandler;
    private String accessToken = null;

    public SpotifyAPI(Context context) {
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
        if (accessToken == null) {
            refreshAccessToken(accountId, callback);
        } else {
            performFetch(accountId, callback);
        }
    }

    private void refreshAccessToken(String accountId, PlatformCallback callback) {
        GlobalSettingsManager.GlobalSettings settings = GlobalSettingsManager.loadSettings(context);
        String clientId = settings.spotifyClientId;
        String clientSecret = settings.spotifyClientSecret;

        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            callback.onError("Spotify Credentials not set");
            return;
        }

        String authString = clientId + ":" + clientSecret;
        String authHeader = "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();

        Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .post(body)
                .addHeader("Authorization", authHeader)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("Failed to get token: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String err = "";
                    if (response.body() != null) err = response.body().string();
                    final String msg = "Token request failed: " + response.code() + " " + err;
                    mainHandler.post(() -> callback.onError(msg));
                    return;
                }
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    accessToken = json.getString("access_token");
                    // Proceed with fetch
                    performFetch(accountId, callback);
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Error parsing token: " + e.getMessage()));
                }
            }
        });
    }

    private void performFetch(String accountId, PlatformCallback callback) {
        String url = "https://api.spotify.com/v1/artists/" + accountId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 401) {
                    // Token expired, clear and retry
                    accessToken = null;
                    refreshAccessToken(accountId, callback);
                    return;
                }

                if (!response.isSuccessful()) {
                    String err = "";
                    if (response.body() != null) err = response.body().string();
                    final String msg = "Request failed: " + response.code() + " " + err;
                    mainHandler.post(() -> callback.onError(msg));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response.body().string());

                    String displayName = json.getString("name");
                    JSONObject followers = json.getJSONObject("followers");
                    int followerTotal = followers.getInt("total");
                    
                    String profilePhotoUrl = "";
                    if (json.has("images") && json.getJSONArray("images").length() > 0) {
                         profilePhotoUrl = json.getJSONArray("images").getJSONObject(0).getString("url");
                    }

                    String formattedCount = formatCount(followerTotal);

                    PlatformResult result = new PlatformResult(accountId, displayName, formattedCount, profilePhotoUrl);
                    mainHandler.post(() -> callback.onSuccess(result));

                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Error parsing JSON: " + e.getMessage()));
                }
            }
        });
    }

    private String formatCount(int count) {
        if (count < 1000) return String.valueOf(count);
        if (count < 1000000) return String.format("%.1fK", count / 1000.0);
        return String.format("%.1fM", count / 1000000.0);
    }

    @Override
    public String getPlatformName() {
        return "spotify";
    }
}
