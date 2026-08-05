plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.divya.fitnesscoach"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.divya.fitnesscoach"
        // GenAI / AICore APIs require API 26+. This is the minSdk floor for this sample for the
        // whole app, not just the AI feature, to keep the build config simple.
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.4.0" // On-Device Fitness Coach blog Part 4

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android + Compose
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    // Needed for collectAsStateWithLifecycle(), so Compose only collects state while
    // the UI is actually visible, rather than for the full lifetime of the ViewModel.
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Coroutines, needed for suspend/Flow wiring around the GenAI client
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // ML Kit GenAI Prompt API — the current, production path to Gemini Nano on-device.
    // Beta: pin this exact version, watch the ML Kit release notes before bumping it.
    implementation("com.google.mlkit:genai-prompt:1.0.0-beta2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
