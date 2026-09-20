plugins {
    alias(libs.plugins.android.library)
}

dependencies {
    implementation(project(":backstage"))
    testImplementation(libs.junit)
}

android {
    namespace = "dev.pinaki.backstage.library.impl"
    enableKotlin = false
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
