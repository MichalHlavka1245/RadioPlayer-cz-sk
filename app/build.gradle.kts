plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.radio_player_czsk"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.radio_player_czsk"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {

            isMinifyEnabled  = true


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
        compose = true
    }
}

dependencies {
    // 🧱 Základné Android a KTX knižnice
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // 🎨 Jetpack Compose (riadené cez tvoj BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")

    // 🗺️ Navigácia, Prehrávač, Serializácia a Obrázky (Coil)
    implementation("androidx.navigation:navigation-compose:2.8.0-alpha08")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // 🔥 Firebase (Používame stabilnú verziu cez Firebase BOM)
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore") // Čistý Firestore bez starého -ktx
    implementation("com.google.firebase:firebase-auth")      // Čistý Firebase Auth pre login

    // 🚀 Google Credential Manager (Pre moderné prihlasovanie bez chýb)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")

    // ⚡ Google Play Services základ & Asynchrónne Coroutines (pre .await())
    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    // 🧪 Testovacie knižnice
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}