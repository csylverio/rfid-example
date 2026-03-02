plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "br.com.example.rfid"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "br.com.example.rfid"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(mapOf("name" to "API3_READER-release-2.0.5.238", "ext" to "aar"))
    implementation(mapOf("name" to "API3_INTERFACE-release-2.0.5.238", "ext" to "aar"))
    implementation(mapOf("name" to "API3_TRANSPORT-release-2.0.5.238", "ext" to "aar"))
    implementation(mapOf("name" to "API3_CMN-release-2.0.5.238", "ext" to "aar"))
    implementation(mapOf("name" to "API3_LLRP-release-2.0.5.238", "ext" to "aar"))
    implementation(mapOf("name" to "API3_ASCII-release-2.0.5.238", "ext" to "aar"))
    implementation(mapOf("name" to "rfidhostlib", "ext" to "aar"))
    implementation(mapOf("name" to "rfidseriallib", "ext" to "aar"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
