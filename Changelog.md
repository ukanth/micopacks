Version 5.0.0 (2025-12-30)

## 🎨 New Features
* **Auto-Theming Engine**: Generate themed icons for unsupported apps using icon pack masking resources (opt-in via Settings)
* **Enhanced Favorites**: Click favorite icons to see source icon pack, download or remove from favorites
* **Integrated Preview**: Icon statistics now displayed directly in preview screen (total icons, size, themed percentage)

## 🚀 Performance Improvements
* **Smart Caching**: Implemented multi-layer caching (AppCache, BitmapCache, AuthorCache) for faster loading
* **Batch Database Operations**: Optimized queries for better responsiveness
* **Thread Pool Management**: Replaced individual thread spawning with ExecutorService for efficient resource usage
* **LRU Memory Cache**: Automatic bitmap cache eviction prevents memory issues

## 🎯 UI/UX Enhancements
* **Modernized Dialog**: Redesigned icon preview dialog with cleaner layout and dark theme support
* **Improved Title Display**: Icon pack names now shown in subtitle format for better hierarchy
* **Better Icon Display**: Fixed favorite star icon highlighting
* **Settings Reorganization**: Cleaner settings layout with Auto-Theming toggle

## 🔧 Bug Fixes
* Fixed: Icon packs not showing until force reload
* Fixed: Duplicate items appearing in list
* Fixed: Icon pack name not displaying in preview titlebar
* Fixed: ActionBar theme conflicts in AboutActivity

## 📦 Technical Updates
* Upgraded to Android SDK 35 (Android 15)
* Updated AndroidX libraries (Material 1.12.0, AppCompat 1.7.0, RecyclerView 1.3.2)
* Updated Room database to 2.6.1
* Updated Gradle to 8.13 and AGP to 8.7.3
* Updated Gson to 2.11.0
* Removed deprecated changelog library
* Replaced deprecated jcenter with mavenCentral

## 🗑️ Removed
* Removed unused changelog library and related code

---
Version 3.0

* Feature: Search icons inside iconpacks
* Feature: Posidon Launcher support
* Bug: App Icon size issue on main list
* Bug: Exclude other icon managers on main list
* Bug: Masked icon preview always shows as empty (whitecons etc.,)
* Crash report Fixes and various performance improvements

Version 1.3

* Rewritten the app for faster loading
* Notification on new icon pack install with apply action
* Details view with icon count and missing package status
* Removed unnessary options from preferences

