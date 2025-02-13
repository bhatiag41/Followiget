package com.testing.myapp;

import android.os.Handler;
import android.os.Looper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InstagramService {
    private static final String BASE_URL = "https://www.instagram.com/";
    private final OkHttpClient client;
    private final Handler mainHandler;

    public InstagramService() {
        this.client = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface FollowerCallback {
        void onSuccess(int followerCount);
        void onError(String error);
    }

    public void getFollowerCount(String username, FollowerCallback callback) {
        new Thread(() -> {
            try {
                String url = BASE_URL + username;
                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", "Mozilla/5.0")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Request failed");

                    String html = response.body().string();
                    Document doc = Jsoup.parse(html);
                    String description = doc.select("meta[property=og:description]").attr("content");

                    System.out.println("OG Description: " + description); // Debugging

                    // Extract the first number (Followers count)
                    String[] parts = description.split(" ");
                    int followerCount = Integer.parseInt(parts[0].replace(",", "")); // Remove comma for large numbers

                    mainHandler.post(() -> callback.onSuccess(followerCount));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

}