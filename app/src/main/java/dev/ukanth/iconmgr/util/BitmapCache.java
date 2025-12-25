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
    
    private BitmapCache() {
        // Use 1/8th of available memory for bitmap cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        
        Log.d(TAG, "Bitmap cache size: " + cacheSize + "KB");
        
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
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
    }
    
    /**
     * Get cache statistics
     */
    public String getStats() {
        return "Cache hits: " + memoryCache.hitCount() + 
               ", misses: " + memoryCache.missCount() +
               ", size: " + memoryCache.size() + "KB";
    }
}
