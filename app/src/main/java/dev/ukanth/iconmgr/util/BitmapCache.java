package dev.ukanth.iconmgr.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

/**
 * Global LRU cache for icon pack bitmaps to avoid repeated loading and decoding
 */
public class BitmapCache {
    private static final String TAG = "BitmapCache";
    private static BitmapCache instance;
    private final LruCache<String, Bitmap> memoryCache;
    private final LruCache<String, Bitmap> themedIconCache;
    
    private BitmapCache() {
        // Use 1/8th of available memory for bitmap cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        final int themedCacheSize = maxMemory / 16; // Smaller cache for auto-themed icons
        
        Log.d(TAG, "Bitmap cache size: " + cacheSize + "KB");
        Log.d(TAG, "Themed icon cache size: " + themedCacheSize + "KB");
        
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // Size in KB
                return bitmap.getByteCount() / 1024;
            }
        };
        
        themedIconCache = new LruCache<String, Bitmap>(themedCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // Size in KB
                return bitmap.getByteCount() / 1024;
            }
        };
    }
    
    public static synchronized BitmapCache getInstance() {
        if (instance == null) {
            instance = new BitmapCache();
        }
        return instance;
    }
    
    /**
     * Add bitmap to cache
     */
    public void put(String packageName, String drawableName, Bitmap bitmap) {
        if (bitmap != null) {
            String key = packageName + ":" + drawableName;
            memoryCache.put(key, bitmap);
        }
    }
    
    /**
     * Get bitmap from cache
     */
    public Bitmap get(String packageName, String drawableName) {
        String key = packageName + ":" + drawableName;
        return memoryCache.get(key);
    }
    
    /**
     * Remove all bitmaps for a specific package
     */
    public void evictPackage(String packageName) {
        memoryCache.evictAll(); // Simple implementation, could be more granular
    }
    
    /**
     * Clear entire cache
     */
    public void clear() {
        memoryCache.evictAll();
        themedIconCache.evictAll();
    }
    
    /**
     * Add themed icon to cache
     * Key format: "appPackage_iconPackPackage_themed"
     */
    public void putThemedIcon(String appPackage, String iconPackPackage, Bitmap bitmap) {
        if (bitmap != null) {
            String key = appPackage + "_" + iconPackPackage + "_themed";
            themedIconCache.put(key, bitmap);
        }
    }
    
    /**
     * Get themed icon from cache
     */
    public Bitmap getThemedIcon(String appPackage, String iconPackPackage) {
        String key = appPackage + "_" + iconPackPackage + "_themed";
        return themedIconCache.get(key);
    }
    
    /**
     * Clear themed icon cache for a specific icon pack
     */
    public void clearThemedIconsForPack(String iconPackPackage) {
        // Since we can't iterate LruCache efficiently, just clear all
        themedIconCache.evictAll();
    }
    
    /**
     * Get cache statistics
     */
    public String getStats() {
        return "Cache hits: " + memoryCache.hitCount() + 
               ", misses: " + memoryCache.missCount() +
               ", size: " + memoryCache.size() + "KB" +
               ", themed cache size: " + themedIconCache.size() + "KB";
    }
}
