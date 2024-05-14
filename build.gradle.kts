// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath(libs.gradle)
        classpath(libs.secrets.gradle.plugin)
    }
}
plugins {
    alias(libs.plugins.androidApplication) apply false
}
