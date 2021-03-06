# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/orlanth23/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interfaces
# class:
#-keepclassmembers class fqcn.of.javascript.interfaces.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn com.google.common.**
-dontwarn com.google.android.gms.**
-dontwarn android.databinding.**
-dontwarn org.codehaus.**
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn net.simonvt.schematic.**
-dontwarn com.squareup.javapoet.**
-dontwarn com.caverock.androidsvg.**

-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class nc.opt.mobile.optmobile.** { *; }
-keep class org.jsoup.**
# -keep class android.support.v7.app.AlertDialog
# -keep class android.content.DialogInterface.** {*;}
# -keep class android.app.DialogFragment

-keepclassmembers class nc.opt.mobile.optmobile.domain.** {*;}

-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl