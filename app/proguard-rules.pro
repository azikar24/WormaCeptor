# ProGuard rules for WormaCeptor App
# Add project specific ProGuard rules here.

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# -------------------- OkHttp --------------------
# OkHttp platform used only on JVM and when Conscrypt and other security providers are available
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Keep OkHttp annotations
-keepattributes Signature
-keepattributes *Annotation*

# -------------------- Retrofit --------------------
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept., Response are used in signatures of methods that are kept.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# -------------------- Gson --------------------
# Gson uses generic type information stored in a class file when working with fields.
# R8 removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson specific classes
-dontwarn sun.misc.**

# Keep TypeToken and its subclasses with generic signatures
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# -------------------- Room --------------------
# Room uses annotations extensively
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# -------------------- Compose --------------------
# Keep Compose stability configurations
-keep class androidx.compose.runtime.** { *; }

# -------------------- Kotlin --------------------
# Keep Kotlin Metadata for reflection
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# -------------------- Data Classes --------------------
# Keep data classes used for serialization/deserialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# -------------------- Enums --------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# -------------------- Google Tink / Security Crypto --------------------
# Missing annotations from Tink crypto library
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn com.google.crypto.tink.**

# -------------------- Koin --------------------
# Keep Koin modules and definitions
-keep class org.koin.** { *; }
-keepclassmembers class * {
    public <init>(...);
}
# Keep classes instantiated by Koin (ViewModels, Repositories, etc.)
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends org.koin.core.module.Module { *; }

# -------------------- WormaCeptor --------------------
# Keep all WormaCeptor library classes (uses Koin reflection internally)
-keep class com.azikar24.wormaceptor.** { *; }
-keepclassmembers class com.azikar24.wormaceptor.** { *; }
