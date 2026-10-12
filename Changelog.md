Version 6.0.0 (2026-10-12)

## New Features
* New home screen with bottom tabs: Packs, Icons, Saved and History
* Swipe a pack right to apply it or left to favourite it. Both directions can be changed in Settings (Apply, Favourite, Open pack app, Play Store or Uninstall)
* Long-press a pack to see all its actions in one place
* Undo after applying a pack or adding a favourite
* Icons tab lets you search icons across all installed packs at once
* Saved tab shows your favourite icons grouped by pack
* History tab shows which packs you applied and when, and the packs you uninstalled (tap one to open it on Play Store)
* Save any icon as a PNG to Pictures/Micopacks (Android 10+)
* Long-press the Micopacks app icon for Random and your favourite packs
* Theme option: follow system, light or dark. Material You colours on Android 12+

## UI/UX Enhancements
* Simpler pack list. Each row shows the pack icon, name, how many of your apps it covers and a few sample icons. The pack you are using is marked "In use"
* Title bar shows which pack is applied on which launcher
* Preview and Details are now one screen with tabs (Your apps, Missing, All icons, Masks), icon search and an "Apply to <launcher>" button at the bottom
* Random no longer picks the pack you already have applied
* Material 3 look for colours, dialogs and settings, with edge-to-edge layout on Android 15+
* Removed list display options that are no longer needed

## Performance Improvements
* Refresh only rescans packs that are new or updated. Everything else loads straight from the database
* Icons load in the background at the size they are displayed, so scrolling is smoother
* Large packs use much less memory
* Smaller download thanks to code and resource shrinking

## Bug Fixes
* Fixed: Newly installed icon packs were not detected
* Fixed: Wrong package sent when applying on Niagara, Square Home, Lawnchair (legacy), Flick, Pixel, Omega, Before, Ion, mLauncher and Projectivy
* Fixed: Crash when applying from the install notification or Tasker
* Fixed: "Apply" button in the notification did nothing on Android 12+
* Fixed: Crash with Tasker random pack
* Fixed: Crash when sorting by author name
* Fixed: Favourites were lost after "Rebuild list" or when a pack covered every app
* Fixed: Updated packs showed up twice or disappeared from the list
* Fixed: Reinstalled packs still showed as uninstalled in History

## Technical Updates
* Asks for notification permission on Android 13+ when "Notify on new icon pack install" is enabled
* No longer needs storage permission (icons are saved using MediaStore)
* Database moved fully to Room with proper migrations, so favourites and history survive upgrades
* Replaced greenDAO, Material Dialogs and old support libraries
* Added AndroidX Preference and Lifecycle (ViewModel, LiveData)
* Added unit tests for appfilter parsing, sorting, filtering and icon saving

## Removed
* Separate Favorites, Icon Search, Preview and Details screens. These are now part of the home tabs and the pack screen

---
Version 5.0.0 (2025-12-30)

## New Features
* **Auto-Theming Engine**: Generate themed icons for unsupported apps using icon pack masking resources (opt-in via Settings)
* **Enhanced Favorites**: Click favorite icons to see source icon pack, download or remove from favorites
* **Integrated Preview**: Icon statistics now displayed directly in preview screen (total icons, size, themed percentage)

## Performance Improvements
* **Smart Caching**: Implemented multi-layer caching (AppCache, BitmapCache, AuthorCache) for faster loading
* **Batch Database Operations**: Optimized queries for better responsiveness
* **Thread Pool Management**: Replaced individual thread spawning with ExecutorService for efficient resource usage
* **LRU Memory Cache**: Automatic bitmap cache eviction prevents memory issues

## UI/UX Enhancements
* **Modernized Dialog**: Redesigned icon preview dialog with cleaner layout and dark theme support
* **Improved Title Display**: Icon pack names now shown in subtitle format for better hierarchy
* **Better Icon Display**: Fixed favorite star icon highlighting
* **Settings Reorganization**: Cleaner settings layout with Auto-Theming toggle

## Bug Fixes
* Fixed: Icon packs not showing until force reload
* Fixed: Duplicate items appearing in list
* Fixed: Icon pack name not displaying in preview titlebar
* Fixed: ActionBar theme conflicts in AboutActivity

## Technical Updates
* Upgraded to Android SDK 35 (Android 15)
* Updated AndroidX libraries (Material 1.12.0, AppCompat 1.7.0, RecyclerView 1.3.2)
* Updated Room database to 2.6.1
* Updated Gradle to 8.13 and AGP to 8.7.3
* Updated Gson to 2.11.0
* Removed deprecated changelog library
* Replaced deprecated jcenter with mavenCentral

## Removed
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

