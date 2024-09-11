plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.owlagenda"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.owlagenda"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {
            isMinifyEnabled = true
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

    packaging {
        resources.excludes.add("/META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/INDEX.LIST")
    }

    splits {
        abi {
            // Habilita a divisão de ABI
            isEnable = true
            // Define as ABIs a serem incluídas no APK
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            // Define se o APK universal será gerado
            isUniversalApk = false
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.legacy.support.v4)
    implementation(libs.core.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.material)
    implementation(libs.firebase.database)
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation(project(":opencv"))
    implementation ("com.google.api-client:google-api-client:2.7.0")
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    implementation ("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.github.yalantis:ucrop:2.2.8")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation ("com.facebook.android:facebook-login:17.0.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.github.instacart:truetime-android:3.5")
    implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation ("com.mig35:carousellayoutmanager:1.4.6")
    implementation("com.kizitonwose.calendar:compose:2.5.4")
    implementation("com.kizitonwose.calendar:view:2.5.4")
    implementation ("com.airbnb.android:lottie:6.5.1")
    implementation("jp.wasabeef:recyclerview-animators:4.0.2")
    implementation ("com.google.firebase:firebase-firestore:25.1.0")
    implementation("io.grpc:grpc-okhttp:1.66.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
