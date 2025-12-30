package dev.ukanth.iconmgr.util;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores masking metadata for an icon pack's auto-theming capability
 */
public class IconPackMaskingInfo {
    private List<Bitmap> backgrounds;    // iconback images
    private Bitmap mask;                  // iconmask
    private Bitmap overlay;               // iconupon
    private float scale;                  // scale factor
    
    // Cache of masking info per icon pack
    private static final Map<String, IconPackMaskingInfo> cache = new HashMap<>();
    
    public IconPackMaskingInfo() {
        this.backgrounds = new ArrayList<>();
        this.scale = 1.0f;
    }
    
    public List<Bitmap> getBackgrounds() {
        return backgrounds;
    }
    
    public void addBackground(Bitmap background) {
        if (background != null) {
            this.backgrounds.add(background);
        }
    }
    
    public Bitmap getMask() {
        return mask;
    }
    
    public void setMask(Bitmap mask) {
        this.mask = mask;
    }
    
    public Bitmap getOverlay() {
        return overlay;
    }
    
    public void setOverlay(Bitmap overlay) {
        this.overlay = overlay;
    }
    
    public float getScale() {
        return scale;
    }
    
    public void setScale(float scale) {
        this.scale = scale;
    }
    
    public boolean hasMaskingResources() {
        return !backgrounds.isEmpty() || mask != null || overlay != null;
    }
    
    // Cache management
    public static void putCache(String packageName, IconPackMaskingInfo info) {
        cache.put(packageName, info);
    }
    
    public static IconPackMaskingInfo getCache(String packageName) {
        return cache.get(packageName);
    }
    
    public static boolean hasCache(String packageName) {
        return cache.containsKey(packageName);
    }
    
    public static void clearCache() {
        cache.clear();
    }
    
    public static void invalidateCache(String packageName) {
        cache.remove(packageName);
    }
}
