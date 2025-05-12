plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // added for firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.groupproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.groupproject"
        minSdk = 24
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
}

dependencies {
    // added for firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.0")) //changed from 33.13.0
    implementation("com.google.firebase:firebase-analytics")

    // ads
//    implementation("com.google.android.gms:play-services-ads:23.5.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.database.ktx)
    implementation(libs.play.services.ads.api)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}