plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.hachiapp"
    compileSdkVersion(36)

    defaultConfig {
        applicationId = "com.example.hachiapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val mapsKey = project.properties["MAPS_API_KEY"]?.toString() ?: ""
        manifestPlaceholders["MAPS_API_KEY"] = "AIzaSyBCFj9xkAEtDT7p9vgGGUx9OktOoDxev2U"

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
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    //gps amiga
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    //
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // imagen en circulo
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation(libs.firebase.messaging)
    implementation("com.google.firebase:firebase-auth-ktx:23.1.0")

    implementation(libs.play.services.maps)
    implementation(libs.androidx.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}