plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "com.amhetcnaa.fluxion"
  compileSdk = 36
  defaultConfig {
    applicationId = "com.amhetcnaa.fluxion"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0"
    ndk {
      abiFilters += listOf("arm64-v8a")
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  packaging {
    jniLibs {
      useLegacyPackaging = true
    }
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation("io.github.junkfood02.youtubedl-android:library:0.18.1")
  implementation("io.github.junkfood02.youtubedl-android:ffmpeg:0.18.1")
}
