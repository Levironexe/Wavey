plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.wavey"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wavey"
        minSdk = 26
        targetSdk = 35
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
        viewBinding = true
    }
}

dependencies {
    // Core Android Libraries
    implementation(libs.androidx.core.ktx) // Android KTX extensions for Kotlin support
    implementation(libs.androidx.appcompat) // AppCompat for backward-compatible UI components
    implementation(libs.material) // Material Design components for modern UI elements
    implementation(libs.androidx.constraintlayout) // ConstraintLayout for flexible and responsive layouts

    // Lifecycle and Navigation Libraries
    implementation(libs.androidx.lifecycle.livedata.ktx) // LiveData for observing data changes
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // ViewModel for managing UI-related data lifecycle
    implementation(libs.androidx.navigation.fragment.ktx) // Navigation components for fragment-based navigation
    implementation(libs.androidx.navigation.ui.ktx) // Navigation UI components for better integration with the UI

    // Firebase Libraries
    implementation(libs.firebase.firestore) // Firestore for cloud-based NoSQL database
    implementation(libs.firebase.database) // Realtime Database for cloud-based data storage
    implementation(libs.firebase.auth) // Firebase Authentication for user sign-in and management

    // Credentials and Google Sign-In Libraries
    implementation(libs.androidx.credentials) // AndroidX credentials library for secure storage and authentication
    implementation(libs.androidx.credentials.play.services.auth) // Play services for handling authentication
    implementation(libs.googleid) // Google Identity library for Google login and authentication

    // Testing Libraries
    testImplementation(libs.junit) // JUnit for unit testing
    androidTestImplementation(libs.androidx.junit) // Android-specific JUnit testing
    androidTestImplementation(libs.androidx.espresso.core) // Espresso for UI testing

    // Image Handling Libraries
    // Glide for displaying images from URLs
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Image and Layout Enhancements
    implementation(libs.blurry) // Blurry effect for views/images
    implementation("androidx.gridlayout:gridlayout:1.0.0") // GridLayout for flexible grid-based layouts

    // Additional Material Design Libraries
    implementation("com.google.android.material:material:1.12.0") // Material Design components

    // Constraint Layout for complex layouts
    implementation("androidx.constraintlayout:constraintlayout:2.2.1") // ConstraintLayout for responsive layouts

    // Kotlin Coroutines for Firebase asynchronous handling
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1") // Coroutines for Firebase and Play services

    // UI Enhancements
    // SwipeRefreshLayout for pull-to-refresh functionality
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Fragment KTX Extensions
    implementation("androidx.fragment:fragment-ktx:1.8.2") // Kotlin extensions for fragment handling

    // Circle Image View for circular images
    implementation("de.hdodenhof:circleimageview:3.1.0") // Library for displaying circular images

    // Google Authentication for sign-in
    implementation("com.google.android.gms:play-services-auth:21.3.0") // Google Sign-In services

    // Animation Libraries
    implementation("com.github.gayanvoice:android-animations-kotlin:1.0.1") // Kotlin-based animation library
    implementation("com.daimajia.androidanimations:library:2.4@aar") // Library for advanced UI animations


    implementation ("com.google.android.gms:play-services-auth:21.3.0")
    implementation ("com.google.android.gms:play-services-base:18.6.0")
}
kapt {
    correctErrorTypes = true
}

