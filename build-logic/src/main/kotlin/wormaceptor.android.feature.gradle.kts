/** Convention plugin for feature modules (Compose + Koin + lifecycle + collections). */
plugins {
    id("wormaceptor.android.compose")
}

dependencies {
    "implementation"("androidx.core:core-ktx:1.13.1")
    "implementation"("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    "implementation"("io.insert-koin:koin-androidx-compose:4.0.0")
    "implementation"("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
}
