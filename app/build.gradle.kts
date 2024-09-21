plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.luhyah.ocr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.luhyah.ocr"
        minSdk = 23
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.ui.graphics.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
   // implementation("net.sourceforge.tess4j:tess4j:5.7.0")

    implementation("com.vanniktech:android-image-cropper:4.6.0")

    // To recognize Latin script
    implementation ("com.google.mlkit:text-recognition:16.0.1")

//    // To recognize Chinese script
//    implementation ("com.google.mlkit:text-recognition-chinese:16.0.1")
//
//    // To recognize Devanagari script
//    implementation ("com.google.mlkit:text-recognition-devanagari:16.0.1")
//
//    // To recognize Japanese script
//    implementation ("com.google.mlkit:text-recognition-japanese:16.0.1")
//
//    // To recognize Korean script
//    implementation ("com.google.mlkit:text-recognition-korean:16.0.1")


}