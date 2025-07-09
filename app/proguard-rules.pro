# --- Credential Manager (AndroidX Credentials, Google Sign-In) ---
-keep class androidx.credentials.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.firebase.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }

# --- Moshi (Kotlin reflection, codegen, adapters) ---
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }
-keep class com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory { *; }
-dontwarn com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
-dontwarn kotlin.reflect.jvm.internal.**
-dontwarn org.yaml.snakeyaml.Yaml

# --- OkHttp ---
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# --- AiManager, ApiKeyManager, and Providers ---
-keep class himanshu.com.apikeymanager.** { *; }

# --- General Android rules ---
-keep class android.support.** { *; }
-keep class android.arch.** { *; }
-keep class android.databinding.** { *; }
-keep class android.** { *; }

# --- Keep annotation attributes ---
-keepattributes *Annotation*
-keepattributes Signature