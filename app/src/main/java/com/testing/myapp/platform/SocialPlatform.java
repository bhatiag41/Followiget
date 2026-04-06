package com.testing.myapp.platform;

public interface SocialPlatform {
    
    interface PlatformCallback {
        void onSuccess(PlatformResult result);
        void onError(String error);
    }
    
    class PlatformResult {
        public final String accountId;
        public final String displayName;
        public final String count;
        public final String profilePhotoUrl;
        
        public PlatformResult(String accountId, String displayName, String count, String profilePhotoUrl) {
            this.accountId = accountId;
            this.displayName = displayName;
            this.count = count;
            this.profilePhotoUrl = profilePhotoUrl;
        }
    }

    void fetchFollowerCount(String accountId, PlatformCallback callback);
    String getPlatformName();
}
