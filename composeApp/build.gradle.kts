import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeHotReload)
  alias(libs.plugins.sqlDelight)
}

kotlin {
  androidTarget {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
  }

  jvm("desktop")

  sourceSets {
    val desktopMain by getting

    androidMain.dependencies {
      implementation(compose.preview)
      implementation(libs.androidx.activity.compose)
      implementation(libs.android.driver)
    }
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)
      implementation(libs.material.icons.extended)
      implementation(libs.back.handler)

      // Dependency injection
      implementation(libs.koin.core)
      // Navigation
      implementation(libs.voyager.navigator)
      implementation(libs.voyager.screen.model)
      implementation(libs.voyager.transitions)
      implementation(libs.voyager.koin)
      // Datetime handling
      implementation(libs.kotlinx.datetime)
      // Logging
      implementation(libs.kermit) // Add latest version

      // Constraint layout
      /// Compose 1.8.0+
      implementation(libs.constraintlayout.compose.multiplatform)
      /// Compose 1.8.0+ with different tech.annexflow.constraintlayout.core package
      implementation(libs.compose.constraintlayout.compose.multiplatform)
      /// Compose 1.8.0+ with different tech.annexflow.constraintlayout package
      implementation(libs.constraintlayout.compose.multiplatform.v060shaded)

      implementation(libs.coroutines.extensions)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
    desktopMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutinesSwing)
      implementation(libs.sqlite.driver)
    }
  }
}

android {
  namespace = "me.elmanss.melate"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "me.elmanss.melate"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
  buildTypes { getByName("release") { isMinifyEnabled = false } }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies { debugImplementation(compose.uiTooling) }

compose.desktop {
  application {
    mainClass = "me.elmanss.melate.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "me.elmanss.melate"
      packageVersion = "1.0.0"
    }
  }
}

sqldelight { databases { create("Database") { packageName.set("me.elmanss.melate") } } }
