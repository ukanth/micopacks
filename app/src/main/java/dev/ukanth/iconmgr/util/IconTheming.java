package dev.ukanth.iconmgr.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import java.util.List;
import java.util.Random;

/**
 * Engine for generating themed icons using icon pack masking resources
 */
public class IconTheming {
    
    private static final Random random = new Random();
    
    /**
     * Generate a themed icon by applying icon pack's masking resources
     * 
     * @param originalIcon The original app icon bitmap
     * @param maskInfo The masking resources from the icon pack
     * @return Themed icon bitmap, or null if generation fails
     */
    public static Bitmap generateThemedIcon(Bitmap originalIcon, IconPackMaskingInfo maskInfo) {
        if (originalIcon == null || maskInfo == null || !maskInfo.hasMaskingResources()) {
            return null;
        }
        
        try {
            int size = 192; // Standard icon size
            Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            
            // Step 1: Draw background (if available)
            List<Bitmap> backgrounds = maskInfo.getBackgrounds();
            if (!backgrounds.isEmpty()) {
                // Pick a random background
                Bitmap background = backgrounds.get(random.nextInt(backgrounds.size()));
                Bitmap scaledBg = Bitmap.createScaledBitmap(background, size, size, true);
                canvas.drawBitmap(scaledBg, 0, 0, null);
            }
            
            // Step 2: Draw scaled original icon
            float scale = maskInfo.getScale();
            if (scale <= 0 || scale > 2) {
                scale = 0.85f; // Default scale if invalid
            }
            
            int scaledSize = (int) (size * scale);
            int padding = (size - scaledSize) / 2;
            
            Bitmap scaledOriginal = Bitmap.createScaledBitmap(originalIcon, scaledSize, scaledSize, true);
            canvas.drawBitmap(scaledOriginal, padding, padding, null);
            
            // Step 3: Apply mask (if available)
            Bitmap mask = maskInfo.getMask();
            if (mask != null) {
                Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                
                Bitmap scaledMask = Bitmap.createScaledBitmap(mask, size, size, true);
                canvas.drawBitmap(scaledMask, 0, 0, maskPaint);
            }
            
            // Step 4: Draw overlay (if available)
            Bitmap overlay = maskInfo.getOverlay();
            if (overlay != null) {
                Bitmap scaledOverlay = Bitmap.createScaledBitmap(overlay, size, size, true);
                canvas.drawBitmap(scaledOverlay, 0, 0, null);
            }
            
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Add "AUTO" badge overlay to a bitmap
     * 
     * @param icon The icon bitmap
     * @return Icon with badge overlay
     */
    public static Bitmap addAutoBadge(Bitmap icon) {
        if (icon == null) return null;
        
        try {
            Bitmap result = icon.copy(icon.getConfig(), true);
            Canvas canvas = new Canvas(result);
            
            // Add semi-transparent overlay to indicate auto-generated
            Paint alphaPaint = new Paint();
            alphaPaint.setAlpha(217); // 85% opacity
            canvas.drawBitmap(icon, 0, 0, alphaPaint);
            
            // Could add "AUTO" text badge here if desired
            // For now, just the transparency indicates it's auto-generated
            
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            return icon;
        }
    }
}
