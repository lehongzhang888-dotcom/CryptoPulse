plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cryptopulse"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cryptopulse"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
    }

    signingConfigs {
        create("release") {
            storeFile = file("cryptopulse.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "cryptopulse"
            keyAlias = System.getenv("KEY_ALIAS") ?: "cryptopulse"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "cryptopulse"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}
