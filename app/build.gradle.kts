plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.google.hilt.android)
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "com.example.myradio"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myradio"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            // Разрешает Robolectric доступ к ресурсам Android
            isIncludeAndroidResources = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        buildConfig = true
    }
    sourceSets {
        getByName("test") {
            assets.srcDirs("$projectDir/schemas")
        }
        getByName("androidTest") {
            assets.srcDirs("$projectDir/schemas")
        }
    }
    room {
        // Указываем общую папку для сохранения схем всех вариантов сборки
        schemaDirectory("$projectDir/schemas")
    }
}
hilt {
    enableAggregatingTask = false
}

dependencies {
    // Jetpack Compose & UI Components
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.androidx.compose)
    debugImplementation(libs.androidx.ui.tooling) // Вынесено в дебаг для уменьшения APK

    // Architecture & Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Dependency Injection (Hilt)
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Media & Infrastructure
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.timber)
    implementation(libs.coil.compose)
    implementation(libs.jsoup)
    implementation(libs.gson)

    // Тесты (скомпонованы в один бандл)
    testImplementation(libs.bundles.testing.robolectric)

    androidTestImplementation(libs.bundles.testing.android)
}
