import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
    alias(libs.plugins.jetbrainsKotlinAndroid)

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
        buildConfigField("String", "apiKeyGemini", getLocalProperty("apiKeyGemini"))
        buildConfigField("String", "tokenGoogle", getLocalProperty("tokenGoogle"))
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

    // Exclui o arquivo META-INF/DEPENDENCIES
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
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.legacy.support.v4)
    implementation(libs.core.ktx)
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation(project(":opencv"))
    implementation("com.google.android.gms:play-services-auth:21.1.1")
    implementation ("com.google.api-client:google-api-client:2.4.1")
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.35.0")
    implementation ("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")
    implementation("com.google.firebase:firebase-database")
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.yalantis:ucrop:2.2.8")
    implementation("com.google.ai.client.generativeai:generativeai:0.6.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Função para ler propriedades do arquivo local.properties em Kotlin
fun getLocalProperty(propertyName: String): String {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(FileInputStream(localPropertiesFile))
    }
    return properties.getProperty(propertyName)
}