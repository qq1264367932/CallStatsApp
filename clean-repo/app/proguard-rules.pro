# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguard side in the build.gradle file.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.annotation.Keep <methods>;
}
