package dev.ukanth.iconmgr.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dev.ukanth.iconmgr.RandomActivity;
import dev.ukanth.iconmgr.dao.IPObj;

/**
 * Launcher shortcuts: "Random" plus one per favourite pack, capped at the launcher's limit.
 */
public final class ShortcutHelper {

    private static final String ACTION = "dev.ukanth.iconmgr.shortcut.RANDOM";

    private ShortcutHelper() {
    }

    public static void publish(Context context, List<IPObj> packs) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1 || packs == null) {
            return;
        }
        ShortcutManager manager = context.getSystemService(ShortcutManager.class);
        if (manager == null) {
            return;
        }
        int max = manager.getMaxShortcutCountPerActivity();
        List<ShortcutInfo> shortcuts = new ArrayList<>();
        shortcuts.add(new ShortcutInfo.Builder(context, "card")
                .setShortLabel("Random")
                .setLongLabel("Apply Random Iconpack")
                .setIntent(intent(context, ""))
                .build());
        for (IPObj pack : packs) {
            if (shortcuts.size() >= max) {
                break;
            }
            if (pack.isFavorite() && pack.getIconName() != null) {
                shortcuts.add(new ShortcutInfo.Builder(context, pack.getIconPkg())
                        .setShortLabel(pack.getIconName())
                        .setLongLabel(pack.getIconName())
                        .setIntent(intent(context, pack.getIconPkg()))
                        .build());
            }
        }
        try {
            manager.setDynamicShortcuts(shortcuts);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Log.w("ShortcutHelper", "Could not publish shortcuts: " + e.getMessage());
        }
    }

    private static Intent intent(Context context, String pkg) {
        Intent intent = new Intent(context, RandomActivity.class);
        intent.setAction(ACTION);
        intent.putExtra("pack", pkg);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }
}
