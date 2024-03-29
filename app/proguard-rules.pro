# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Serializer for classes with named companion objects are retrieved using `getDeclaredClasses`.
# If you have any, replace classes with those containing named companion objects.

-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.morpho.app.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class morpho.app.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class com.morpho.app.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.morpho.butterfly.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class morpho.butterfly.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class com.morpho.butterfly.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}


-keep,includedescriptorclasses class app.bsky.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class app.bsky.** { # <-- change package name to your app's
    *** Companion;
}

-keep,includedescriptorclasses class com.atproto.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class com.atproto.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class com.atproto.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Don't print notes about potential mistakes or omissions in the configuration for kotlinx-serialization classes
# See also https://github.com/Kotlin/kotlinx.serialization/issues/1900
-dontnote kotlinx.serialization.**

# Serialization core uses `java.lang.ClassValue` for caching inside these specified classes.
# If there is no `java.lang.ClassValue` (for example, in Android), then R8/ProGuard will print a warning.
# However, since in this case they will not be used, we can disable these warnings
-dontwarn kotlinx.serialization.internal.ClassValueReferences

# Serializer for classes with named companion objects are retrieved using `getDeclaredClasses`.
# If you have any, replace classes with those containing named companion objects.
-keepattributes InnerClasses # Needed for `getDeclaredClasses`.

-if @kotlinx.serialization.Serializable class
app.bsky.actor.PreferencesUnion, # <-- List serializable classes with named companions.
app.bsky.actor.ProfileLabelsUnion,
app.bsky.feed.ThreadViewPostParentUnion,
app.bsky.feed.ThreadViewPostReplieUnion,
app.bsky.feed.PostViewEmbedUnion,
app.bsky.feed.PostEmbedUnion,
app.bsky.feed.PostLabelsUnion,
app.bsky.richtext.FacetFeatureUnion,
com.atproto.label.SubscribeLabelsMessageUnion,
com.atproto.moderation.ReportRequestSubject,
com.atproto.moderation.ReportResponseSubject,
com.atproto.sync.SubscribeReposMessageUnion,
com.atproto.admin.BlobViewDetailsUnion,
com.morpho.app.model.ThreadPost,
com.morpho.app.model.ThreadViewPostUnion,
com.morpho.app.model.SkylineItem,
com.morpho.app.model.BskyNotification,
com.morpho.app.model.FacetType,
com.morpho.app.model.BskyPostFeature,
com.morpho.app.model.EmbedPost,
com.morpho.app.model.BskyPostReason
{
    static **$* *;
}
-keepnames class <1>$$serializer { # -keepnames suffices; class is kept when serializer() is kept.
    static <1>$$serializer INSTANCE;
}
