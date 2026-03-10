plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.github.snd_r.komelia.infra.ncnn"
version = "unspecified"

kotlin {
    jvmToolchain(17)

    androidTarget {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.logging)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "io.github.snd_r.komelia.infra.ncnn"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    ndkVersion = "25.1.8937393"

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        externalNativeBuild {
            cmake {
                arguments("-DUSE_PREBUILT_NCNN=ON", "-DUSE_SHARED_NCNN=ON", "-DNCNN_VULKAN=ON")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/jni/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
