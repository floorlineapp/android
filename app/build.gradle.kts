import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    // Renders Compose screens to PNG on the JVM so the UI can be reviewed
    // without a device. Run: ./gradlew :app:recordRoborazziDebug
    id("io.github.takahirom.roborazzi") version "1.32.2"
}

android {
    namespace = "com.thefloor.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thefloor.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing from ~/.gradle/gradle.properties (never committed).
    // Falls back to debug signing so CI and fresh clones still build.
    val storeFilePath: String? = findProperty("FLOOR_STORE_FILE") as String?
    signingConfigs {
        if (storeFilePath != null) {
            create("release") {
                storeFile = file(storeFilePath)
                storePassword = findProperty("FLOOR_STORE_PASSWORD") as String?
                keyAlias = findProperty("FLOOR_KEY_ALIAS") as String?
                keyPassword = findProperty("FLOOR_KEY_PASSWORD") as String?
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            // Live beta backend on Railway. Sideloaded testers on real phones hit
            // this over HTTPS. No secrets — a public URL only.
            buildConfigField("String", "API_BASE_URL", "\"https://backend-production-957f.up.railway.app/\"")
            buildConfigField("String", "WEB_BASE_URL", "\"https://thefloor.example\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_BASE_URL", "\"https://api.thefloor.example/\"")
            buildConfigField("String", "WEB_BASE_URL", "\"https://thefloor.example\"")
            signingConfig = if (storeFilePath != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        // Roborazzi/Robolectric need real resources (fonts, drawables) to render.
        unitTests.isIncludeAndroidResources = true
    }
}

// Write snapshots somewhere predictable so CI can publish them for review.
roborazzi {
    outputDir.set(file("$rootDir/screenshots"))
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Remote imagery (Floor community photos). Declared directly rather than via
    // the version catalog to keep this a single-file change.
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation(libs.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.installreferrer)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.session)

    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)

    // Screenshot rendering (JVM, no device).
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.32.2")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.32.2")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
