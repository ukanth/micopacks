package dev.ukanth.iconmgr.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import dev.ukanth.iconmgr.dao.IPObj;

/**
 * Swipe a pack row right or left to run the action chosen for that direction in Settings.
 * The row never leaves the list: it always springs back, and the action runs if it was
 * released past the threshold. While swiping, the action's colour, icon and label show behind the row, with a
 * haptic tick when crossing the point where releasing would trigger it.
 */
public class PackSwipeCallback extends ItemTouchHelper.Callback {

    /** Fraction of the row width a swipe must cover; high enough that a sloppy scroll won't trigger it. */
    private static final float THRESHOLD = 0.4f;

    private final PackRowAdapter adapter;
    private final PackRowAdapter.Listener listener;
    private PackAction right;
    private PackAction left;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private RecyclerView.ViewHolder armedHolder;
    private boolean armed;

    public PackSwipeCallback(PackRowAdapter adapter, PackRowAdapter.Listener listener, PackAction right, PackAction left) {
        this.adapter = adapter;
        this.listener = listener;
        setActions(right, left);
    }

    /** Re-read after Settings changes. */
    public void setActions(PackAction right, PackAction left) {
        this.right = right;
        this.left = left;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder holder) {
        // only pack rows (not the header) and only directions that have an action
        if (holder.getBindingAdapter() != adapter) {
            return 0;
        }
        int dirs = 0;
        if (right != PackAction.OFF) dirs |= ItemTouchHelper.RIGHT;
        if (left != PackAction.OFF) dirs |= ItemTouchHelper.LEFT;
        return makeMovementFlags(0, dirs);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder a, @NonNull RecyclerView.ViewHolder b) {
        return false;
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder holder) {
        // never let ItemTouchHelper complete a swipe (it would treat the row as removed and leave
        // it off-screen); the row always springs back and the action fires on release instead
        return Float.MAX_VALUE;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return Float.MAX_VALUE; // a flick alone never completes a swipe either
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direction) {
        // not reached: see getSwipeThreshold
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder holder) {
        super.clearView(recyclerView, holder);
        if (armedHolder == holder) {
            armedHolder = null;
            armed = false;
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder holder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0) {
            PackAction action = dX > 0 ? right : left;
            drawBackground(c, holder.itemView, action, dX);

            if (isCurrentlyActive) {
                boolean past = Math.abs(dX) >= holder.itemView.getWidth() * THRESHOLD;
                if (armedHolder == holder && past && !armed) {
                    holder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
                armedHolder = holder;
                armed = past;
            } else if (armedHolder == holder) {
                // finger lifted: the row is animating back; run the action if it was past the threshold
                boolean fire = armed;
                armedHolder = null;
                armed = false;
                if (fire) {
                    IPObj pack = adapter.packAt(holder.getBindingAdapterPosition());
                    if (pack != null) {
                        // not during drawing: the action may change the list
                        recyclerView.post(() -> listener.onAction(pack, action));
                    }
                }
            }
        }
        super.onChildDraw(c, recyclerView, holder, dX, dY, actionState, isCurrentlyActive);
    }

    private void drawBackground(Canvas c, View item, PackAction action, float dX) {
        Context ctx = item.getContext();
        float density = ctx.getResources().getDisplayMetrics().density;
        // same bounds and corners as the row card (the item view is the card; its margins are outside it)
        float top = item.getTop();
        float bottom = item.getBottom();
        float l = item.getLeft();
        float r = item.getRight();
        rect.set(l, top, r, bottom);

        int bg = MaterialColors.getColor(item, action.color);
        int fg = MaterialColors.getColor(item, onColorFor(action));
        paint.setColor(bg);
        float radius = 18 * density;
        c.drawRoundRect(rect, radius, radius, paint);

        // icon + label on the side the row is moving away from
        int iconSize = (int) (24 * density);
        int pad = (int) (24 * density);
        float cy = (top + bottom) / 2f;
        String label = ctx.getString(labelFor(action));
        textPaint.setColor(fg);
        textPaint.setTextSize(14 * ctx.getResources().getDisplayMetrics().scaledDensity);
        textPaint.setFakeBoldText(true);
        float textWidth = textPaint.measureText(label);
        float gap = 8 * density;

        int iconLeft;
        float textX;
        if (dX > 0) {
            iconLeft = (int) (l + pad);
            textX = iconLeft + iconSize + gap;
        } else {
            iconLeft = (int) (r - pad - iconSize);
            textX = iconLeft - gap - textWidth;
        }
        Drawable icon = ContextCompat.getDrawable(ctx, action.icon);
        if (icon != null) {
            icon = icon.mutate();
            icon.setTint(fg);
            icon.setBounds(iconLeft, (int) (cy - iconSize / 2f), iconLeft + iconSize, (int) (cy + iconSize / 2f));
            icon.draw(c);
        }
        c.drawText(label, textX, cy - (textPaint.descent() + textPaint.ascent()) / 2f, textPaint);
    }

    private static int labelFor(PackAction action) {
        return action == PackAction.FAVORITE ? dev.ukanth.iconmgr.R.string.swipe_label_favorite : action.label;
    }

    private static int onColorFor(PackAction action) {
        switch (action) {
            case APPLY:
                return com.google.android.material.R.attr.colorOnPrimary;
            case FAVORITE:
                return com.google.android.material.R.attr.colorOnSecondary;
            case UNINSTALL:
                return com.google.android.material.R.attr.colorOnError;
            default:
                return com.google.android.material.R.attr.colorOnTertiary;
        }
    }
}
