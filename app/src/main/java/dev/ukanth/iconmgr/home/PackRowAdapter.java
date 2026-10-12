package dev.ukanth.iconmgr.home;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.ukanth.iconmgr.IconPackUtil;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.iconpack.InstalledApps;
import dev.ukanth.iconmgr.util.BitmapCache;
import dev.ukanth.iconmgr.util.Util;

/**
 * Rows of the Packs list. Only shows what helps pick a pack (icon, name, coverage, icon count and
 * three of its icons); everything else is behind tap (pack screen), long-press and swipes.
 */
public class PackRowAdapter extends ListAdapter<IPObj, PackRowAdapter.Holder> {

    public interface Listener {
        void onOpen(IPObj pack);

        void onShowActions(IPObj pack);

        void onAction(IPObj pack, PackAction action);
    }

    private static final DiffUtil.ItemCallback<IPObj> DIFF = new DiffUtil.ItemCallback<IPObj>() {
        @Override
        public boolean areItemsTheSame(@NonNull IPObj a, @NonNull IPObj b) {
            return a.getIconPkg().equals(b.getIconPkg());
        }

        @Override
        public boolean areContentsTheSame(@NonNull IPObj a, @NonNull IPObj b) {
            return a.sameContentAs(b);
        }
    };

    private static final int PREVIEW_COUNT = 3;

    // Pack launcher icons, shared across adapter instances (activity recreation, theme change)
    private static final LruCache<String, Drawable> iconCache = new LruCache<>(64);
    private static final ExecutorService iconExecutor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final IconPackUtil previewUtil = new IconPackUtil();

    private final Listener listener;
    private int installed;
    private String appliedPkg;

    public PackRowAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    /** Number of launcher apps, for the coverage percentage. */
    public void setInstalled(int installed) {
        if (this.installed != installed) {
            this.installed = installed;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    /** Pack shown as "In use"; null for none. */
    public void setAppliedPkg(String pkg) {
        if (pkg == null ? appliedPkg == null : pkg.equals(appliedPkg)) {
            return;
        }
        String old = appliedPkg;
        appliedPkg = pkg;
        notifyPack(old);
        notifyPack(pkg);
    }

    private void notifyPack(String pkg) {
        if (pkg == null) return;
        List<IPObj> list = getCurrentList();
        for (int i = 0; i < list.size(); i++) {
            if (pkg.equals(list.get(i).getIconPkg())) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    public boolean isApplied(IPObj pack) {
        return pack.getIconPkg().equals(appliedPkg);
    }

    /** Pack at an adapter position (as reported by the swipe helper). */
    public IPObj packAt(int position) {
        return position >= 0 && position < getItemCount() ? getItem(position) : null;
    }

    /** "76% of your apps · 14,381", or just the icon count before coverage is known. */
    public String metaFor(Context ctx, IPObj pack) {
        String count = NumberFormat.getIntegerInstance().format(pack.getTotal());
        int coverage = installed > 0 ? InstalledApps.coveragePercent(installed, pack.getMissed()) : -1;
        return coverage >= 0
                ? ctx.getString(R.string.row_meta, coverage, count)
                : ctx.getString(R.string.meta_icons, count);
    }

    final class Holder extends RecyclerView.ViewHolder {
        final MaterialCardView row;
        final ImageView icon;
        final TextView name;
        final ImageView star;
        final TextView inUse;
        final TextView meta;
        final LinearLayout previewStack;
        final List<Integer> a11yActions = new ArrayList<>();
        IPObj pack;

        Holder(View view) {
            super(view);
            row = view.findViewById(R.id.row);
            icon = view.findViewById(R.id.ipack_icon);
            name = view.findViewById(R.id.ipack_name);
            star = view.findViewById(R.id.icon_star);
            inUse = view.findViewById(R.id.in_use);
            meta = view.findViewById(R.id.meta);
            previewStack = view.findViewById(R.id.preview_stack);
            buildPreviewSlots(previewStack);

            row.setOnClickListener(v -> {
                if (pack != null) listener.onOpen(pack);
            });
            row.setOnLongClickListener(v -> {
                if (pack == null) return false;
                listener.onShowActions(pack);
                return true;
            });
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.pack_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        IPObj pack = getItem(position);
        Context ctx = holder.itemView.getContext();
        holder.pack = pack;
        boolean applied = isApplied(pack);

        holder.name.setText(pack.getIconName());
        holder.star.setVisibility(pack.isFavorite() ? View.VISIBLE : View.GONE);
        holder.inUse.setVisibility(applied ? View.VISIBLE : View.GONE);

        // the row is opaque so a swipe's coloured background only shows where the row has moved away
        int surface = MaterialColors.getColor(holder.row, com.google.android.material.R.attr.colorSurface);
        int rowColor = applied
                ? MaterialColors.layer(surface, MaterialColors.getColor(holder.row, com.google.android.material.R.attr.colorPrimaryContainer), 0.5f)
                : surface;
        holder.row.setCardBackgroundColor(ColorStateList.valueOf(rowColor));

        String metaText = metaFor(ctx, pack);
        holder.meta.setText(metaText);
        holder.row.setContentDescription(pack.getIconName() + ", " + metaText
                + (applied ? ", " + ctx.getString(R.string.in_use) : "")
                + (pack.isFavorite() ? ", " + ctx.getString(R.string.icon_favorited) : ""));

        bindAccessibilityActions(holder, pack);
        bindPreviews(holder, pack);
        bindPackIcon(holder, pack);
    }

    /** Swipes aren't usable with TalkBack, so the same actions are offered as accessibility actions. */
    private void bindAccessibilityActions(Holder holder, IPObj pack) {
        for (int id : holder.a11yActions) {
            ViewCompat.removeAccessibilityAction(holder.row, id);
        }
        holder.a11yActions.clear();
        Context ctx = holder.itemView.getContext();
        holder.a11yActions.add(ViewCompat.addAccessibilityAction(holder.row, ctx.getString(R.string.action_apply),
                (v, args) -> {
                    listener.onAction(pack, PackAction.APPLY);
                    return true;
                }));
        holder.a11yActions.add(ViewCompat.addAccessibilityAction(holder.row,
                ctx.getString(pack.isFavorite() ? R.string.remove_favorite : R.string.add_favorite),
                (v, args) -> {
                    listener.onAction(pack, PackAction.FAVORITE);
                    return true;
                }));
        holder.a11yActions.add(ViewCompat.addAccessibilityAction(holder.row, ctx.getString(R.string.more_actions),
                (v, args) -> {
                    listener.onShowActions(pack);
                    return true;
                }));
    }

    private void bindPackIcon(Holder holder, IPObj pack) {
        final String pkg = pack.getIconPkg();
        Drawable cached = iconCache.get(pkg);
        holder.icon.setImageDrawable(cached);
        if (cached != null) {
            return;
        }
        final Context ctx = holder.itemView.getContext();
        iconExecutor.execute(() -> {
            Drawable drawable;
            try {
                drawable = Util.resizeImage(ctx, ctx.getApplicationContext().getPackageManager().getApplicationIcon(pkg));
            } catch (PackageManager.NameNotFoundException e) {
                return;
            }
            iconCache.put(pkg, drawable);
            mainHandler.post(() -> {
                // the holder may have been recycled for another pack meanwhile
                if (holder.pack != null && pkg.equals(holder.pack.getIconPkg())) {
                    holder.icon.setImageDrawable(drawable);
                }
            });
        });
    }

    private void buildPreviewSlots(LinearLayout stack) {
        Context ctx = stack.getContext();
        float density = ctx.getResources().getDisplayMetrics().density;
        int size = (int) (28 * density);
        for (int i = 0; i < PREVIEW_COUNT; i++) {
            ImageView slot = new ImageView(ctx);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            if (i > 0) lp.setMarginStart((int) (-6 * density)); // overlap like a stack
            slot.setLayoutParams(lp);
            slot.setScaleType(ImageView.ScaleType.FIT_CENTER);
            stack.addView(slot);
        }
    }

    /** The pack's own icons for the first common apps (same apps on every row, see PackRepository). */
    private void bindPreviews(Holder holder, IPObj pack) {
        String value = pack.getPreviewIcons();
        String[] names = value == null || value.isEmpty() ? new String[0] : value.split("\\|");
        holder.previewStack.setVisibility(names.length == 0 ? View.GONE : View.VISIBLE);
        final String pkg = pack.getIconPkg();
        int slotPx = (int) (28 * holder.itemView.getResources().getDisplayMetrics().density);
        for (int i = 0; i < PREVIEW_COUNT; i++) {
            ImageView slot = (ImageView) holder.previewStack.getChildAt(i);
            if (i >= names.length) {
                slot.setImageDrawable(null);
                slot.setTag(null);
                continue;
            }
            final String name = names[i];
            final String key = pkg + "|" + name;
            slot.setTag(key);
            Bitmap cached = BitmapCache.getInstance().get(pkg, name + "#" + slotPx);
            slot.setImageBitmap(cached);
            if (cached == null) {
                iconExecutor.execute(() -> {
                    if (!key.equals(slot.getTag())) return; // row was rebound
                    Bitmap bmp = previewUtil.loadThumbnail(pkg, name, slotPx);
                    if (bmp != null) {
                        mainHandler.post(() -> {
                            if (key.equals(slot.getTag())) slot.setImageBitmap(bmp);
                        });
                    }
                });
            }
        }
    }
}
