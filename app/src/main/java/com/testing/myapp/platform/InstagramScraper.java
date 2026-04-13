package com.testing.myapp.platform;

import com.testing.myapp.InstagramService;

public class InstagramScraper implements SocialPlatform {

    private final InstagramService instagramService;

    public InstagramScraper() {
        this.instagramService = new InstagramService();
    }

    @Override
    public void fetchFollowerCount(String accountId, PlatformCallback callback) {
        instagramService.getUserProfile(accountId, new InstagramService.ProfileCallback() {
            @Override
            public void onSuccess(InstagramService.UserProfile profile) {
                callback.onSuccess(new PlatformResult(
                        profile.username,
                        profile.displayName,
                        profile.followerCount,
                        profile.profilePhotoUrl
                ));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    @Override
    public String getPlatformName() {
        return "instagram";
    }
}
