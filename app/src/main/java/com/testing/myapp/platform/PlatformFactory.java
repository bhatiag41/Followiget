package com.testing.myapp.platform;

import android.content.Context;

public class PlatformFactory {
    public static SocialPlatform getPlatform(Context context, String platformName) {
        if (platformName == null) return null;
        
        switch (platformName.toLowerCase()) {
            case "instagram":
                return new InstagramScraper();
            case "youtube":
                return new YouTubeAPI(context);
            case "spotify":
                return new SpotifyAPI(context);
            default:
                return null;
        }
    }
}
