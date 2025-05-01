plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.refine)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
  alias(libs.plugins.room)
  id("delta.lint.kotlin")
  id("delta.lint.kts")
}

android {
  namespace = "dev.shadoe.delta.data"
  compileSdk = 36

  defaultConfig {
    minSdk = 30
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  kotlinOptions { jvmTarget = "21" }
}

room { schemaDirectory("$projectDir/schema") }

dependencies {
  compileOnly(project(path = ":system-api-stubs"))
  implementation(project(path = ":api"))
  implementation(libs.shizuku.api)
  implementation(libs.hilt.android)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.refine.runtime)
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  implementation(libs.shizuku.api)
  implementation(libs.shizuku.provider)
  ksp(libs.hilt.compiler)
  ksp(libs.room.compiler)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(path = ":system-api-stubs"))
}
