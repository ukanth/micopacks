# Micopacks R8 rules. Manifest components, Room and libsu are kept by their own/consumer rules;
# these cover the code that is only reached through reflection.

# Gson reflects over IconAttr's fields (legacy JSON, read by the icons-db v1 -> v2 migration)
-keep class dev.ukanth.iconmgr.IconAttr { <fields>; <init>(); }

# RootTools / RootShell: RootClass loads classes by name and runs them through app_process
-keep class com.stericson.** { *; }
-dontwarn com.stericson.**

# Locale / Tasker plug-in contract (bundle keys and helpers used by the edit and fire components)
-keep class com.twofortyfouram.locale.** { *; }

# PrettyTime loads its locale resource bundles by class name
-keep class org.ocpsoft.prettytime.i18n.** { *; }

# readable crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
