// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath (libs.secrets.gradle.plugin)
    }
}
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
   // alias (libs.plugins.maps) apply false
    alias (libs.plugins.googleServices) apply false
   // id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.firebase.crashlytics") version "3.0.1" apply false
}