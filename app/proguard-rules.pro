# --- Credential Manager, Google Sign-In, Firebase, Hilt/DI ---
-keep class androidx.credentials.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-keep class com.google.firebase.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }

# --- Moshi (Kotlin reflection, codegen, adapters) ---
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-keep class com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory { *; }
-dontwarn com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
-dontwarn kotlin.reflect.jvm.internal.**
-dontwarn org.yaml.snakeyaml.Yaml

# --- OkHttp ---
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# --- Your AI Manager, API Key Manager, and Providers ---
-keep class com.github.himanshusharma.clubapi.** { *; }
-keep class himanshu.com.apikeymanager*

# --- Data classes for Moshi (if in other packages, add them here) ---
-keep class com.serenity.data.model.** { *; }

# --- General Android rules ---
-keep class android.support.** { *; }
-keep class android.arch.** { *; }
-keep class android.databinding.** { *; }
-keep class android.** { *; }

# --- Keep annotation attributes and signatures ---
-keepattributes *Annotation*
-keepattributes Signature

# --- AiManager and API Key Manager (Serenity) ---
-keep class com.serenity.apikeymanager.** { *; }

# --- Gson (if used) ---
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep all data classes used for JSON parsing
-keep class himanshu.com.apikeymanager.ProviderConfig { *; }
-keep class himanshu.com.apikeymanager.KeyStorage { *; }
-keep class himanshu.com.apikeymanager.** { *; }
-keepclassmembers class himanshu.com.apikeymanager.** { *; }
-keepattributes Signature
-keepattributes *Annotation*