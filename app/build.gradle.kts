plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Google Services plugin
}

android {
    compileSdkVersion(34) // replace 34 with your desired API level

    defaultConfig {
        namespace = "com.example.catch_up"
        minSdk = 29
        targetSdk = 33
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

    buildFeatures {
        viewBinding = true
    }

    // Add this block to exclude the 'META-INF/AL2.0' file
    packagingOptions {
        exclude("META-INF/AL2.0")
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.auth)
    implementation(libs.coordinatorlayout)
    implementation(libs.google.material)
    implementation(libs.monitor)
    implementation(libs.ext.junit)
    implementation(libs.support.annotations)
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit.v412)
    androidTestImplementation(libs.testng) // Firebase Authentication

}

fun compileSdkVersion(i: Int) {
    // do nothing
}