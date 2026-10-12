package dev.ukanth.iconmgr.util;

import android.content.Context;
import android.text.format.DateUtils;

import dev.ukanth.iconmgr.R;

/** "Just now" / "5 minutes ago" / "Yesterday" for history entries. */
public final class TimeText {

    private TimeText() {
    }

    public static CharSequence relative(Context context, long time) {
        long now = System.currentTimeMillis();
        if (now - time < DateUtils.MINUTE_IN_MILLIS) {
            return context.getString(R.string.just_now);
        }
        return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
    }
}
