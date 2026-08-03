# ============================================================
# Android Starter - ProGuard / R8 Rules
# ============================================================
# Comprehensive rules for all libraries used in this project.
# Enable minification in app/build.gradle:
#   release { minifyEnabled true }
# ============================================================

# ---- General Android ----
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep application entry points
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ---- ViewBinding ----
-keep class **.databinding.*Binding { *; }

# ---- Retrofit ----
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ---- OkHttp ----
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# ---- Gson ----
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Keep all model/entity classes used with Gson serialization
-keep class com.androidstarter.app.data.model.** { *; }
-keep class com.androidstarter.app.data.local.entity.** { *; }

# ---- Firebase ----
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ---- Firebase Crashlytics ----
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# ---- Firebase Auth & Credentials ----
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn com.google.android.libraries.identity.**
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ---- Glide ----
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder { *** rewind(); }

# ---- Google Play Core (In-App Update & Review) ----
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

# ---- AndroidX Security (EncryptedSharedPreferences) ----
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# ---- WorkManager ----
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }

# ---- AdMob ----
-keep class com.google.android.gms.ads.** { *; }

# ---- Enums ----
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---- Parcelable ----
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ---- Serializable ----
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ---- R8 Full Mode Compatibility ----
-dontwarn java.lang.invoke.StringConcatFactory
