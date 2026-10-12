package dev.ukanth.iconmgr.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import dev.ukanth.iconmgr.R;

/**
 * Material 3 replacements for the old material-dialogs helpers.
 */
public final class Dialogs {

    private Dialogs() {
    }

    /** Non-cancelable "please wait" dialog with an indeterminate spinner; already shown. */
    public static AlertDialog progress(Context context, CharSequence title) {
        int pad = (int) (24 * context.getResources().getDisplayMetrics().density);
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(pad, pad / 2, pad, pad / 2);

        CircularProgressIndicator spinner = new CircularProgressIndicator(context);
        spinner.setIndeterminate(true);
        row.addView(spinner);

        TextView message = new TextView(context);
        message.setText(R.string.please_wait_normal);
        message.setPadding(pad, 0, 0, 0);
        row.addView(message);

        return new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setView(row)
                .setCancelable(false)
                .show();
    }
}
