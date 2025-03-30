# Keep Application class and all subclasses of Activity, Service, BroadcastReceiver
-keep class * extends android.app.Application { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.content.ContentProvider { *; }

# For view binding / data binding
-keep class **.databinding.* { *; }
-keep class **.viewbinding.* { *; }

# Keep annotations
-keepattributes *Annotation*

# If you use Gson or other reflection-based libraries
-keep class com.yourpackage.model.** { *; }

# Firebase (if used)
-keep class com.google.firebase.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit / Moshi / Gson (for serialization)
-keep class retrofit2.** { *; }
-keep class com.squareup.moshi.** { *; }
-keep class com.google.gson.** { *; }

# Remove all Log calls from release build
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
