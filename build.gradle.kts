// Top-level build file where you can add configuration options common to all sub-projects/modules.
//buildscript {
//
//    dependencies {
//        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
//    }
//}
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.49" apply false
    // Google services Gradle plugin dependency
    id("com.google.gms.google-services") version "4.4.0" apply false
    // Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    // navigation safeargs
    id("androidx.navigation.safeargs.kotlin") version "2.7.5" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
    id("androidx.room") version "2.6.0" apply false
}