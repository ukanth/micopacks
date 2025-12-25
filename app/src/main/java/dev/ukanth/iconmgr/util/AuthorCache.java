package dev.ukanth.iconmgr.util;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Global cache for author names to avoid expensive certificate parsing
 */
public class AuthorCache {
    private static final String TAG = "AuthorCache";
    private static AuthorCache instance;
    private final Map<String, String> authorCache;
    
    private AuthorCache() {
        authorCache = new HashMap<>();
    }
    
    public static synchronized AuthorCache getInstance() {
        if (instance == null) {
            instance = new AuthorCache();
        }
        return instance;
    }
    
    /**
     * Get author name from cache or parse certificate
     */
    public String getAuthorName(Context context, String packageName) {
        if (packageName == null) {
            return null;
        }
        
        String cached = authorCache.get(packageName);
        if (cached != null) {
            return cached.isEmpty() ? null : cached;
        }
        
        // Parse certificate (expensive operation)
        String authorName = Util.parseAuthorName(context, packageName);
        
        // Cache result (even if null, cache as empty string to avoid re-parsing)
        authorCache.put(packageName, authorName != null ? authorName : "");
        
        return authorName;
    }
    
    /**
     * Pre-populate cache for multiple packages (can be done in background)
     */
    public void preloadAuthors(Context context, Iterable<String> packageNames) {
        for (String packageName : packageNames) {
            if (!authorCache.containsKey(packageName)) {
                getAuthorName(context, packageName);
            }
        }
    }
    
    /**
     * Clear cache
     */
    public void clear() {
        authorCache.clear();
    }
    
    /**
     * Remove specific package from cache
     */
    public void remove(String packageName) {
        authorCache.remove(packageName);
    }
}
