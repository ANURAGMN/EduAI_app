# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# ========== RETROFIT & OKHTTP ==========
# Keep attributes needed for Retrofit reflection
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Retain Retrofit service method parameters
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit interfaces (created with Proxy at runtime)
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep Kotlin Continuation for suspend functions
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep Retrofit Response class
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Suppress warnings for optional dependencies
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ========== GSON ==========
# Keep Gson classes needed for reflection
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.internal.** { *; }

# Application classes that will be serialized/deserialized with Gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep generic type information for Gson
-keepattributes *Annotation*

# Keep Gson TypeAdapters
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep all fields with @SerializedName annotation (critical for API models)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep generic signatures for Gson
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ========== API DATA CLASSES ==========
# Keep all API request/response models
-keep class com.anurag.eduai.data.remote.** { *; }
-keep class com.anurag.eduai.data.model.** { *; }

# ========== KOTLINX SERIALIZATION ==========
# Keep serializers for kotlinx.serialization (used for other purposes, not Retrofit)
-keepattributes InnerClasses
-keep,includedescriptorclasses class com.anurag.eduai.**$$serializer { *; }
-keepclassmembers class com.anurag.eduai.** {
    *** Companion;
}
-keepclasseswithmembers class com.anurag.eduai.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ========== COROUTINES ==========
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ========== ANDROID COMPONENTS ==========
# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep setters in Views for animations
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

