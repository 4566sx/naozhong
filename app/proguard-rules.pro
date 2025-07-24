# 圣经闹钟应用混淆规则

# 基本混淆配置
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# 保留注解
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# 保留行号信息用于调试
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Android基础组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Hilt依赖注入
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Room数据库
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract *;
}

# WorkManager
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# MediaPlayer和音频相关
-keep class android.media.** { *; }
-keep class androidx.media.** { *; }

# 数据模型类
-keep class com.biblealarm.app.data.model.** { *; }

# 保留Parcelable实现
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable类
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Kotlin协程
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Kotlin反射
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# 保留枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留R类资源
-keep class **.R
-keep class **.R$* {
    <fields>;
}

# 保留BuildConfig
-keep class com.biblealarm.app.BuildConfig { *; }

# 移除日志
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# 保留崩溃报告相关
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# 保留自定义View
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}

# 保留WebView相关（如果使用）
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
    public *;
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, jav.lang.String);
}

# 保留网络请求相关（如果使用）
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# 保留JSON序列化相关
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保留应用特定的关键类
-keep class com.biblealarm.app.manager.** { *; }
-keep class com.biblealarm.app.service.** { *; }
-keep class com.biblealarm.app.receiver.** { *; }
-keep class com.biblealarm.app.worker.** { *; }

# 保留数据库实体和DAO
-keep class com.biblealarm.app.data.database.** { *; }
-keep class com.biblealarm.app.data.repository.** { *; }

# 保留权限管理相关
-keep class com.biblealarm.app.manager.PermissionManager { *; }

# 保留闹钟相关核心功能
-keep class com.biblealarm.app.manager.AlarmManager { *; }
-keep class com.biblealarm.app.manager.PsalmSelectionManager { *; }

# 优化配置
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# 忽略警告
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**