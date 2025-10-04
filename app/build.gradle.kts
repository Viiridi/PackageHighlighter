import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.labelhighlighter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.labelhighlighter"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        val openApiKey: String =
            gradleLocalProperties(rootDir, providers).getProperty("OPENAI_API_KEY") ?: ""
        buildConfigField("String", "OPENAI_API_KEY", "\"$openApiKey\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

//    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
//    implementation("androidx.compose.ui:ui")
//    implementation("androidx.compose.ui:ui-graphics")
//    implementation("androidx.compose.ui:ui-tooling-preview")
//    implementation("androidx.compose.material3:material3")
//    implementation("androidx.activity:activity-compose:1.8.2")


    implementation("androidx.camera:camera-core:1.2.3")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("androidx.camera:camera-view:1.2.3")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    testImplementation("junit:junit:4.13.2")
}
