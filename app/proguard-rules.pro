# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.talmudfinance.app.**$$serializer { *; }
-keepclassmembers class com.talmudfinance.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.talmudfinance.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
