plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "dev.pinaki.backstage.library"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
