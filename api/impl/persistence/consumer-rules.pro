# SQLCipher's JNI looks up native fields (e.g. mNativeHandle) and methods by
# name. Stripping or renaming them causes NoSuchFieldError when SQLiteDatabase
# loads its native library.
-keep,includedescriptorclasses class net.zetetic.database.** { *; }
-keep,includedescriptorclasses interface net.zetetic.database.** { *; }
-dontwarn net.zetetic.database.**
