# Add project specific ProGuard rules here.

# Keep FCM service
-keep class com.hanotif.tv.service.FCMService { *; }

# Keep model classes for Gson
-keep class com.hanotif.tv.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# ExoPlayer / Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Coil
-dontwarn coil.**

# Kotlin
-keep class kotlin.** { *; }
-dontwarn kotlin.**
