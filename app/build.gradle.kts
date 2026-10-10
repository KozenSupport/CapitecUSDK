plugins {
    id("com.android.application")
}

android {
    namespace = "com.capitec.interfaceapp"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.capitec.interfaceapp"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(files("libs/capitec-api.jar"))
    implementation(files("libs/TerminalManagerLib_1.3.0.2_release.aar"))
    implementation(files("libs/FinancialLib_1.5.0_release.aar"))
    implementation(files("libs/ComponentLib_1.3.0_release.aar"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
    implementation("androidx.core:core:1.13.1")
}
