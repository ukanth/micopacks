package dev.ukanth.iconmgr.home;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.color.MaterialColors;

import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.util.LauncherHelper;

/** Long-press sheet on a pack row: every {@link PackAction}, whatever the swipe settings are. */
public final class PackActionsSheet {

    private PackActionsSheet() {
    }

    public static void show(Activity activity, IPObj pack, CharSequence meta, PackRowAdapter.Listener listener) {
        BottomSheetDialog sheet = new BottomSheetDialog(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.sheet_pack_actions, null);

        ((TextView) view.findViewById(R.id.sheet_pack_name)).setText(pack.getIconName());
        ((TextView) view.findViewById(R.id.sheet_pack_meta)).setText(meta);
        ImageView icon = view.findViewById(R.id.sheet_pack_icon);
        try {
            icon.setImageDrawable(activity.getPackageManager().getApplicationIcon(pack.getIconPkg()));
        } catch (PackageManager.NameNotFoundException e) {
            icon.setImageDrawable(null);
        }

        ViewGroup actions = view.findViewById(R.id.sheet_actions);
        for (PackAction action : PackAction.values()) {
            if (action == PackAction.OFF) continue;
            TextView row = (TextView) LayoutInflater.from(activity).inflate(R.layout.item_sheet_action, actions, false);
            row.setText(labelFor(activity, action, pack));
            Drawable drawable = ContextCompat.getDrawable(activity, action.icon);
            row.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
            int color = MaterialColors.getColor(row, action == PackAction.UNINSTALL
                    ? androidx.appcompat.R.attr.colorError
                    : com.google.android.material.R.attr.colorOnSurfaceVariant);
            TextViewCompat.setCompoundDrawableTintList(row, ColorStateList.valueOf(color));
            if (action == PackAction.UNINSTALL) {
                row.setTextColor(color);
            }
            row.setOnClickListener(v -> {
                sheet.dismiss();
                listener.onAction(pack, action);
            });
            actions.addView(row);
        }

        sheet.setContentView(view);
        sheet.show();
    }

    private static String labelFor(Activity activity, PackAction action, IPObj pack) {
        switch (action) {
            case APPLY:
                // name the launcher when the apply will go straight to it
                String launcher = LauncherHelper.getLauncherPackage(activity);
                if (launcher != null && !"android".equals(launcher) && !Prefs.usePrompt()) {
                    return activity.getString(R.string.apply_to, LauncherHelper.getLauncherName(activity, launcher));
                }
                return activity.getString(R.string.action_apply);
            case FAVORITE:
                return activity.getString(pack.isFavorite() ? R.string.remove_favorite : R.string.add_favorite);
            default:
                return activity.getString(action.label);
        }
    }
}
