import java.util.Locale

plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Google Services plugin
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    compileSdk = 34 // replace 34 with your desired API level
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

    applicationVariants.all {
        outputs.all {
            // Ensure processResources and processGoogleServices tasks are used correctly
            val processGoogleServicesTask = tasks.named(
                "process${
                    name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                }GoogleServices"
            )
            tasks.named(
                "merge${
                    name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                }Resources"
            )
                .configure {
                    dependsOn(processGoogleServicesTask)
                }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.material)
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
    implementation(libs.appcompat.resources)
    implementation(libs.appcompat)

    implementation(libs.play.services.location) // google location services
    implementation(libs.play.services.maps) // google maps sdk
    implementation(libs.places) // google places sdk
    implementation(platform(libs.kotlin.bom)) // kotlin-bom needed for google places

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.testng) // Firebase Authentication
}

secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}
