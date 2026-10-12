package dev.ukanth.iconmgr.util;

import android.app.Activity;
import android.os.Build;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Android 15+ enforces edge-to-edge for apps targeting SDK 35+, so the content frame
 * starts at the top of the screen, under the status bar and the window-decor action bar.
 * The insets delivered to the content already include the action bar height, so padding
 * by them keeps content below the bars and clear of the navigation bar.
 */
public final class WindowInsetsHelper {

    private WindowInsetsHelper() {
    }

    public static void applyToContent(Activity activity) {
        // below Android 15 the decor still fits system windows and offsets the content itself
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return;
        }
        View content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            // the content root now clears the bars; children (e.g. BottomNavigationView) must not add them again
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(content);
    }
}
