import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Read secrets from .env — never hardcode keys in source
val envProps = Properties()
val envFile = rootProject.file(".env")
if (envFile.exists()) envFile.inputStream().use { envProps.load(it) }
fun env(key: String) = envProps.getProperty(key, "")

android {
    namespace = "com.testing.myapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.testing.myapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "YOUTUBE_API_KEY",          "\"${env("YOUTUBE_API_KEY")}\"")
        buildConfigField("String", "YOUTUBE_API_KEY_FALLBACK",  "\"${env("YOUTUBE_API_KEY_FALLBACK")}\"")
        buildConfigField("String", "SPOTIFY_CLIENT_ID",         "\"${env("SPOTIFY_CLIENT_ID")}\"")
        buildConfigField("String", "SPOTIFY_CLIENT_SECRET",     "\"${env("SPOTIFY_CLIENT_SECRET")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.security.crypto)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("org.jsoup:jsoup:1.14.3")
    implementation ("com.google.android.material:material:1.10.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
}
