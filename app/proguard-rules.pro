# ProGuard rules for Xiaomi Super Island Demo
-dontwarn com.xiaomi.**
-keep class com.xiaomi.** { *; }

# Keep notification extras
-keepclassmembers class android.app.Notification {
    android.os.Bundle extras;
}

# Keep JSON serialization
-keepattributes *Annotation*
-keep class org.json.** { *; }
