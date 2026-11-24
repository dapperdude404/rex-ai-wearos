plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.dapperdude404.rexai"; compileSdk = 35
 defaultConfig { applicationId = "com.dapperdude404.rexai"; minSdk = 30; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}