# ============================================================
# ProGuard / R8 rules – MyVoice release
# ============================================================

# --- Kotlin ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers @kotlin.Metadata class ** { *; }
-dontwarn kotlin.**

# --- Jetpack Compose ---
-keepclassmembers class **Composable* { *; }
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Entity class * { *; }

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# --- DataStore ---
-keep class androidx.datastore.** { *; }

# --- App Widgets ---
-keep public class * extends android.appwidget.AppWidgetProvider

# --- Generali Android ---
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

