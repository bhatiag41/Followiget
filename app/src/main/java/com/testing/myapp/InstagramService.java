package com.testing.myapp;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstagramService {
    private static final String BASE_URL = "https://www.instagram.com/";
    private final OkHttpClient client;
    private final Handler mainHandler;

    public InstagramService() {
        this.client = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    public static class UserProfile {
        public final String username;
        public final String displayName;
        public final String followerCount;
        public final String profilePhotoUrl;
        public final int weeklyChange;

        public UserProfile(String username, String displayName, String followerCount,
                           String profilePhotoUrl, int weeklyChange) {
            this.username = username;
            this.displayName = displayName;
            this.followerCount = followerCount;
            this.profilePhotoUrl = profilePhotoUrl;
            this.weeklyChange = weeklyChange;
        }
    }
    public interface ProfileCallback {
        void onSuccess(UserProfile profile);
        void onError(String error);
    }

    private int calculateWeeklyChange(Document doc) {
        try {
            // Look for activity feed element that contains recent changes
            Element activityFeed = doc.select("script:containsData(edge_followed_by)").first();
            if (activityFeed != null) {
                String data = activityFeed.html();
                // Extract the count_changed value from the activity feed
                Pattern pattern = Pattern.compile("\"count_changed\":\\s*(\\d+)");
                Matcher matcher = pattern.matcher(data);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void getUserProfile(String username, ProfileCallback callback) {
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

                    System.out.println("Raw HTML: " + html); // Debug print

                    // Get meta description which contains follower count
                    String description = doc.select("meta[property=og:description]").attr("content");
                    String[] parts = description.split(" ");
                    String followerCount = parts[0];

                    // Get weekly change from the activity feed
                    int weeklyChange = calculateWeeklyChange(doc);

                    // Rest of your existing profile fetching code...
                    String profilePhotoUrl = doc.select("meta[property=og:image]").attr("content");
                    String displayName = parseDisplayName(doc.select("meta[property=og:title]").attr("content"), username);

                    UserProfile profile = new UserProfile(
                            username,
                            displayName,
                            followerCount,
                            profilePhotoUrl,
                            weeklyChange
                    );

                    mainHandler.post(() -> callback.onSuccess(profile));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    private String parseDisplayName(String titleContent, String username) {
        try {
            // Convert HTML entities to characters
            String decodedTitle = Html.fromHtml(titleContent, Html.FROM_HTML_MODE_LEGACY).toString();

            // Remove "@username" part
            decodedTitle = decodedTitle.replace("@" + username, "");

            // Remove "• Instagram photos and videos" part
            decodedTitle = decodedTitle.split("•")[0];

            // Remove any parentheses
            decodedTitle = decodedTitle.replace("(", "").replace(")", "");

            // Trim any extra whitespace
            decodedTitle = decodedTitle.trim();

            return decodedTitle;
        } catch (Exception e) {
            // If anything goes wrong, return the username as fallback
            return username;
        }
    }



    // Original follower callback interface
    public interface FollowerCallback {
        void onSuccess(int followerCount);
        void onError(String error);
    }

    // Original method for backward compatibility
    public void getFollowerCount(String username, FollowerCallback callback) {
        getUserProfile(username, new ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                try {
                    int count = Integer.parseInt(profile.followerCount.replace(",", ""));
                    callback.onSuccess(count);
                } catch (NumberFormatException e) {
                    callback.onError("Failed to parse follower count");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}