plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.owlagenda"
    compileSdk = 35

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

//    splits {
//        abi {
//            // Habilita a divisão de ABI
//            isEnable = true
//            // Define as ABIs a serem incluídas no APK
//            reset()
//            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
//            // Define se o APK universal será gerado
//            isUniversalApk = false
//        }
//    }
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
    implementation(platform(libs.firebase.bom))
    // implementation(project(":opencv"))
    implementation (libs.google.api.client)
    implementation (libs.google.oauth.client.jetty)
    implementation (libs.google.api.services.calendar)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.ucrop)
    implementation(libs.generativeai)
    implementation (libs.facebook.login)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    implementation (libs.truetime.android)
    implementation (libs.googleid)
    implementation (libs.carousellayoutmanager)
    implementation(libs.compose)
    implementation(libs.view)
    implementation (libs.lottie)
    implementation(libs.recyclerview.animators)
    implementation (libs.firebase.firestore)
    implementation(libs.grpc.okhttp)
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
