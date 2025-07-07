import java.util.Properties // Added this import

// Top-level build.gradle.kts plugins block might also define versions for these
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Apply Google Services plugin - consider adding to libs.versions.toml for version management
    id("com.google.gms.google-services") version "4.4.2" // Example version, use your required version
}

android {
    namespace = "com.example.ecoride"
    compileSdk = 35 // Or your desired SDK, e.g., 34 for Android 14 stable

    defaultConfig {
        applicationId = "com.example.ecoride"
        minSdk = 24
        targetSdk = 35 // Match compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Read Razorpay Key ID from local.properties
        val localProperties = Properties() // Now uses the imported Properties
        val localPropertiesFile = rootProject.file("local.properties")
        var razorpayKeyId = "" // Default empty value
        if (localPropertiesFile.exists()) {
            try {
                localPropertiesFile.inputStream().use { input -> // Ensure stream is closed
                    localProperties.load(input)
                }
                razorpayKeyId = localProperties.getProperty("razorpay_key_id", "") // Provide a default if not found
            } catch (e: Exception) {
                // Handle exception if local.properties cannot be read
                println("Warning: Could not read razorpay_key_id from local.properties: ${e.message}")
            }
        } else {
            println("Warning: local.properties file not found. RAZORPAY_KEY_ID will be empty.")
        }
        buildConfigField("String", "RAZORPAY_KEY_ID", "\"$razorpayKeyId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Recommended: true for release builds
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug specific configurations if any
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // Or JavaVersion.VERSION_17 or higher if needed
        targetCompatibility = JavaVersion.VERSION_11 // Or JavaVersion.VERSION_17 or higher if needed
    }

    kotlinOptions {
        jvmTarget = "11" // Or "17" if using Java 17
    }

    buildFeatures {
        compose = true
        buildConfig = true // Enable BuildConfig generation
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.get() // Ensure this matches your Kotlin version compatibility
        // If you have a specific compose compiler version in your TOML, use that:
        // kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get() // Example if you had 'compose-compiler' in versions
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Jetpack Compose and AndroidX (using BOM for Compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Compose Material 3
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.runtime.livedata) // If you use LiveData with Compose

    // Coil for image loading in Compose
    implementation(libs.coil.compose)

    // Material Design Components (for XML views, if any part of your app uses them)
    implementation(libs.material)

    // Material Design Icons for Compose
    implementation(libs.androidx.material.icons.extended)

    // Firebase (using BOM)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)

    // Google Play Services
    implementation(libs.play.services.auth)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.places) // Google Places API
    implementation(libs.maps.compose) // Google Maps Compose Utilities

    // Coroutines
    implementation(libs.kotlinx.coroutines.play.services) // For integration with Play Services Tasks

    // Razorpay Checkout
    implementation(libs.checkout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // For Compose testing
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling) // For Compose tooling in debug builds
    debugImplementation(libs.androidx.ui.test.manifest) // For Compose test manifest
}

