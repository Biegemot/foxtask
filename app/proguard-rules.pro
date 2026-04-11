# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ================================
# Room Database
# ================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Keep all Room entities
-keep class com.foxtask.app.data.local.entities.** { *; }

# ================================
# Kotlin Coroutines
# ================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ================================
# Jetpack Compose
# ================================
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class androidx.compose.** {
    <methods>;
}

# ================================
# Data Classes and Models
# ================================
# Keep all data classes
-keep class com.foxtask.app.data.models.** { *; }
-keep class com.foxtask.app.domain.models.** { *; }

# Keep data class constructors
-keepclassmembers class com.foxtask.app.data.** {
    <init>(...);
}

# ================================
# Enums
# ================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ================================
# Parcelable
# ================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ================================
# Serialization
# ================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ================================
# Keep ViewModels
# ================================
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class com.foxtask.app.presentation.viewmodel.** { *; }

# ================================
# Keep Use Cases
# ================================
-keep class com.foxtask.app.domain.usecases.** { *; }

# ================================
# Keep Receivers
# ================================
-keep class * extends android.content.BroadcastReceiver {
    <init>(...);
}
-keep class com.foxtask.app.receiver.** { *; }

# ================================
# Keep Application class
# ================================
-keep class com.foxtask.app.FoxTaskApplication { *; }

# ================================
# Keep ServiceLocator
# ================================
-keep class com.foxtask.app.di.ServiceLocator { *; }
-keepclassmembers class com.foxtask.app.di.ServiceLocator {
    public *;
}

# ================================
# Reflection
# ================================
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# ================================
# Debugging
# ================================
# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ================================
# Remove Logging (optional - uncomment for production)
# ================================
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }

# ================================
# Optimization
# ================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# ================================
# Warnings to ignore
# ================================
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn kotlin.jvm.internal.**
