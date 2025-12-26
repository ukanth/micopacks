package dev.ukanth.iconmgr.util;

import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache for installed apps to avoid repeated PackageManager queries
 */
public class AppCache {
    private static final String TAG = "AppCache";
    private static AppCache instance;
    private List<ResolveInfo> cachedApps;
    private long lastUpdateTime;
    private static final long CACHE_VALIDITY_MS = 60000; // 1 minute
    
    private AppCache() {
        cachedApps = new ArrayList<>();
        lastUpdateTime = 0;
    }
    
    public static synchronized AppCache getInstance() {
        if (instance == null) {
            instance = new AppCache();
        }
        return instance;
    }
    
    /**
     * Get cached installed apps or refresh if cache is stale
     */
    public synchronized List<ResolveInfo> getInstalledApps(boolean forceRefresh) {
        long now = System.currentTimeMillis();
        
        if (forceRefresh || cachedApps.isEmpty() || (now - lastUpdateTime) > CACHE_VALIDITY_MS) {
            Log.d(TAG, "Refreshing installed apps cache");
            cachedApps = Util.queryInstalledApps();
            lastUpdateTime = now;
        }
        
        return new ArrayList<>(cachedApps); // Return defensive copy
    }
    
    /**
     * Invalidate cache when packages are installed/uninstalled
     */
    public synchronized void invalidate() {
        Log.d(TAG, "Cache invalidated");
        lastUpdateTime = 0;
    }
    
    /**
     * Get size of installed apps without full query
     */
    public synchronized int getInstalledAppsCount(boolean forceRefresh) {
        return getInstalledApps(forceRefresh).size();
    }
}
