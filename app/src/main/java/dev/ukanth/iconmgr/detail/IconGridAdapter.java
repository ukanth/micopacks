package dev.ukanth.iconmgr.detail;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.ukanth.iconmgr.IconPackUtil;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.util.BitmapCache;
import dev.ukanth.iconmgr.util.IconPackMaskingInfo;
import dev.ukanth.iconmgr.util.IconTheming;

/**
 * Grid of pack icons / uncovered apps. Bitmaps are decoded only for cells on screen, and a load
 * whose cell has been rebound to another item is dropped before it does any work.
 */
public class IconGridAdapter extends ListAdapter<IconItem, IconGridAdapter.Holder> {

    public interface OnIconClickListener {
        void onIconClick(IconItem item, Bitmap bitmap);
    }

    private static final DiffUtil.ItemCallback<IconItem> DIFF = new DiffUtil.ItemCallback<IconItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull IconItem a, @NonNull IconItem b) {
            return a.key().equals(b.key());
        }

        @Override
        public boolean areContentsTheSame(@NonNull IconItem a, @NonNull IconItem b) {
            return a.equals(b);
        }
    };

    private static final ExecutorService loader = Executors.newFixedThreadPool(3);
    /** Original launcher icons of uncovered apps, ~8 MB */
    private static final LruCache<String, Bitmap> appIcons = new LruCache<String, Bitmap>(8 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount() / 1024;
        }
    };

    private final Handler main = new Handler(Looper.getMainLooper());
    private final IconPackUtil packUtil = new IconPackUtil();
    private final String packPackage;
    private final boolean themeMissingApps;
    /** thumbnail edge in px, set from the cell width on first bind */
    private volatile int thumbSize = 192;
    private final OnIconClickListener listener;
    private volatile IconPackMaskingInfo maskInfo;
    private volatile boolean maskInfoLoaded;

    public IconGridAdapter(String packPackage, boolean themeMissingApps, OnIconClickListener listener) {
        super(DIFF);
        this.packPackage = packPackage;
        this.themeMissingApps = themeMissingApps;
        this.listener = listener;
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView label;
        /** key of the item currently bound; read by loader threads */
        volatile String boundKey;
        IconItem item;

        Holder(View view) {
            super(view);
            icon = view.findViewById(R.id.icon);
            label = view.findViewById(R.id.label);
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Holder holder = new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon_grid, parent, false));
        holder.itemView.setOnClickListener(v -> {
            if (holder.item != null) {
                android.graphics.drawable.Drawable d = holder.icon.getDrawable();
                Bitmap bitmap = d instanceof android.graphics.drawable.BitmapDrawable
                        ? ((android.graphics.drawable.BitmapDrawable) d).getBitmap() : null;
                listener.onIconClick(holder.item, bitmap);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        IconItem item = getItem(position);
        final String key = item.key();
        holder.item = item;
        holder.boundKey = key;
        holder.label.setText(item.title);
        holder.itemView.setContentDescription(item.title);

        if (holder.icon.getWidth() > 0) {
            thumbSize = holder.icon.getWidth();
        }
        Bitmap cached = cached(item);
        holder.icon.setImageBitmap(cached);
        if (cached != null) {
            return;
        }
        final Context context = holder.itemView.getContext().getApplicationContext();
        loader.execute(() -> {
            if (!key.equals(holder.boundKey)) {
                return; // scrolled away before we got to it
            }
            Bitmap bitmap = load(context, item);
            if (bitmap == null) {
                return;
            }
            main.post(() -> {
                if (key.equals(holder.boundKey)) {
                    holder.icon.setImageBitmap(bitmap);
                }
            });
        });
    }

    private Bitmap cached(IconItem item) {
        if (item.kind == IconItem.Kind.PACK_ICON) {
            return BitmapCache.getInstance().get(packOf(item), item.drawable + "#" + thumbSize);
        }
        return themeMissingApps && maskInfo != null
                ? BitmapCache.getInstance().getThemedIcon(item.appPackage, packPackage)
                : appIcons.get(item.appPackage);
    }

    private String packOf(IconItem item) {
        return item.packPackage != null ? item.packPackage : packPackage;
    }

    private Bitmap load(Context context, IconItem item) {
        if (item.kind == IconItem.Kind.PACK_ICON) {
            // cell-sized thumbnail; the icon sheet loads the full-size icon on demand
            return packUtil.loadThumbnail(packOf(item), item.drawable, thumbSize);
        }
        Bitmap original = appIcons.get(item.appPackage);
        if (original == null) {
            try {
                original = IconPackUtil.drawableToBitmap(context.getPackageManager().getApplicationIcon(item.appPackage));
                int size = thumbSize;
                if (original.getWidth() > size) {
                    original = Bitmap.createScaledBitmap(original, size, size, true);
                }
                appIcons.put(item.appPackage, original);
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }
        if (!themeMissingApps) {
            return original;
        }
        // preview how the pack's iconback/mask/upon would theme this app
        if (!maskInfoLoaded) {
            synchronized (this) {
                if (!maskInfoLoaded) {
                    maskInfo = packUtil.getMaskingInfo(packPackage);
                    maskInfoLoaded = true;
                }
            }
        }
        if (maskInfo == null) {
            return original;
        }
        Bitmap themed = BitmapCache.getInstance().getThemedIcon(item.appPackage, packPackage);
        if (themed == null) {
            themed = IconTheming.generateThemedIcon(original, maskInfo);
            if (themed != null) {
                themed = IconTheming.addAutoBadge(themed);
                BitmapCache.getInstance().putThemedIcon(item.appPackage, packPackage, themed);
            }
        }
        return themed != null ? themed : original;
    }
}
