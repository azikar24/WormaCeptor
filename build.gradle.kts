/*
 * Copyright AziKar24 21/12/2025.
 */

plugins {
    id("com.android.application") version "8.5.0" apply false
    id("com.android.library") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.24" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
}

extra.apply {
    set("minSdkVersion", 21)
    set("targetSdkVersion", 34)
    set("compileSdkVersion", 34)

    set("versionCode", 35)
    set("versionName", "1.0.2")

    set("appCompatVersion", "1.6.1")
    set("materialComponentsVersion", "1.11.0")

    set("testArchCore", "2.2.0")
    set("lifeCycleVersion", "2.6.2")
    set("pagingVersion", "3.2.1")
    set("roomVersion", "2.6.1")

    set("testJunit4", "4.13.2")
    set("testAssertJCore", "3.24.2")
    set("testMockitoCore", "5.8.0")

    set("okhttp3Version", "4.12.0")
    set("retrofitVersion", "2.9.0")
}
