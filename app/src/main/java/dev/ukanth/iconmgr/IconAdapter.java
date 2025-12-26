package dev.ukanth.iconmgr;

/**
 * Created by ukanth on 17/7/17.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.AuthorCache;
import dev.ukanth.iconmgr.util.Util;

import static dev.ukanth.iconmgr.tasker.FireReceiver.TAG;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconPackViewHolder> {

    private Context ctx;
    protected List<IPObj> iconPacks;
    private int installed;
    private Set<String> expandedItems = new HashSet<>();

    // Cached instances to avoid repeated allocations
    private static final Gson gson = new Gson();
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    // Cached preference values
    private boolean prefUseFavorite;
    private boolean prefShowTotalIcons;
    private boolean prefShowSize;
    private boolean prefShowPercentage;
    private boolean prefShowAuthorName;
    private String prefSortBy;

    public class IconPackViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        IPObj currentItem;
        LocalIcon localIcon;

        // Header views
        TextView ipackName;
        TextView ipackAuthor;
        ImageView icon;
        ImageView iconStar;
        ImageView iconExpand;

        // Stats views
        LinearLayout statsRow;
        LinearLayout statIconsContainer;
        LinearLayout statSizeContainer;
        LinearLayout statThemedContainer;
        TextView statIconsValue;
        TextView statSizeValue;
        TextView statThemedValue;

        // Action row views
        View divider;
        LinearLayout actionRow;
        MaterialButton btnApply;
        MaterialButton btnPreview;
        MaterialButton btnStats;
        MaterialButton btnLaunch;
        ImageButton btnOverflow;

        public IconPackViewHolder(View view) {
            super(view);

            // Header
            cardView = (MaterialCardView) view.findViewById(R.id.cv);
            ipackName = view.findViewById(R.id.ipack_name);
            ipackAuthor = view.findViewById(R.id.ipack_author);
            icon = view.findViewById(R.id.ipack_icon);
            iconStar = view.findViewById(R.id.icon_star);
            iconExpand = view.findViewById(R.id.icon_expand);

            // Stats
            statsRow = view.findViewById(R.id.stats_row);
            statIconsContainer = view.findViewById(R.id.stat_icons_container);
            statSizeContainer = view.findViewById(R.id.stat_size_container);
            statThemedContainer = view.findViewById(R.id.stat_themed_container);
            statIconsValue = view.findViewById(R.id.stat_icons_value);
            statSizeValue = view.findViewById(R.id.stat_size_value);
            statThemedValue = view.findViewById(R.id.stat_themed_value);

            // Action row
            divider = view.findViewById(R.id.divider);
            actionRow = view.findViewById(R.id.action_row);
            btnApply = view.findViewById(R.id.btn_apply);
            btnPreview = view.findViewById(R.id.btn_preview);
            btnStats = view.findViewById(R.id.btn_stats);
            btnLaunch = view.findViewById(R.id.btn_launch);
            btnOverflow = view.findViewById(R.id.btn_overflow);

            // Favorite star click
            iconStar.setOnClickListener(v -> toggleFavorite());

            // Card click to expand/collapse
            cardView.setOnClickListener(v -> toggleExpand());
            iconExpand.setOnClickListener(v -> toggleExpand());

            // Action buttons
            btnApply.setOnClickListener(v -> {
                if (currentItem != null) {
                    Util.determineApply(ctx, currentItem);
                }
            });

            btnPreview.setOnClickListener(v -> {
                if (currentItem != null) {
                    preview(ctx, currentItem);
                }
            });

            btnStats.setOnClickListener(v -> {
                if (currentItem != null) {
                    stats(ctx, currentItem);
                }
            });

            btnLaunch.setOnClickListener(v -> {
                if (currentItem != null) {
                    openApp(ctx, currentItem);
                }
            });

            // Overflow menu
            btnOverflow.setOnClickListener(v -> showOverflowMenu(v));
        }

        private void toggleFavorite() {
            if (currentItem != null && currentItem.getIconPkg() != null) {
                IconAttr attr = gson.fromJson(currentItem.getAdditional(), IconAttr.class);
                attr.setFavorite(!attr.isFavorite());
                currentItem.setAdditional(gson.toJson(attr));

                // Update database on background thread
                final IPObj itemToUpdate = currentItem;
                dbExecutor.execute(() -> {
                    IPObjDao ipObjDao = App.getInstance().getIPObjDao();
                    ipObjDao.update(itemToUpdate);
                });

                if (attr.isFavorite()) {
                    iconStar.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_star_black_24dp));
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                        Intent intent = new Intent(ctx, RandomActivity.class);
                        intent.setAction("dev.ukanth.iconmgr.shortcut.RANDOM");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("pack", currentItem.getIconPkg());
                        ShortcutInfo shortcut = new ShortcutInfo.Builder(ctx, currentItem.getIconPkg())
                                .setShortLabel(currentItem.getIconName())
                                .setLongLabel(currentItem.getIconName())
                                .setIntent(intent)
                                .build();
                        ShortcutManager manager = ctx.getSystemService(ShortcutManager.class);
                        if (manager.getDynamicShortcuts().size() < manager.getMaxShortcutCountPerActivity()) {
                            manager.addDynamicShortcuts(Arrays.asList(shortcut));
                        }
                    }
                } else {
                    iconStar.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_star_border_black_24dp));
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                        ctx.getSystemService(ShortcutManager.class).removeDynamicShortcuts(Arrays.asList(currentItem.getIconPkg()));
                    }
                }
            }
        }

        private void toggleExpand() {
            if (currentItem == null) return;

            String pkg = currentItem.getIconPkg();
            boolean isExpanded = expandedItems.contains(pkg);

            if (isExpanded) {
                expandedItems.remove(pkg);
                collapseActionRow();
            } else {
                expandedItems.add(pkg);
                expandActionRow();
            }
        }

        private void expandActionRow() {
            divider.setVisibility(View.VISIBLE);
            actionRow.setVisibility(View.VISIBLE);
            actionRow.setAlpha(0f);
            actionRow.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
            iconExpand.animate()
                    .rotation(180f)
                    .setDuration(200)
                    .start();
        }

        private void collapseActionRow() {
            actionRow.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        actionRow.setVisibility(View.GONE);
                        divider.setVisibility(View.GONE);
                    })
                    .start();
            iconExpand.animate()
                    .rotation(0f)
                    .setDuration(200)
                    .start();
        }

        private void showOverflowMenu(View anchor) {
            PopupMenu popup = new PopupMenu(ctx, anchor);
            popup.getMenuInflater().inflate(R.menu.card_overflow_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_playstore) {
                    openPlay(ctx, currentItem);
                    return true;
                } else if (id == R.id.action_uninstall) {
                    uninstall(ctx, currentItem);
                    return true;
                }
                return false;
            });
            popup.show();
        }

        void updateExpandState() {
            if (currentItem == null) return;
            boolean isExpanded = expandedItems.contains(currentItem.getIconPkg());
            actionRow.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            divider.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            actionRow.setAlpha(isExpanded ? 1f : 0f);
            iconExpand.setRotation(isExpanded ? 180f : 0f);
        }
    }

    private void preview(Context ctx, IPObj currentItem) {
        Intent intent = new Intent(ctx, IconPreviewActivity.class);
        intent.putExtra("pkg", currentItem.getIconPkg());
        ctx.startActivity(intent);
    }

    private void stats(Context ctx, IPObj currentItem) {
        if (currentItem != null && currentItem.getIconPkg() != null) {
            Intent intent = new Intent(ctx, DetailsActivity.class);
            intent.putExtra("pkg", currentItem.getIconPkg());
            ctx.startActivity(intent);
        }
    }

    private void openApp(Context ctx, IPObj currentItem) {
        try {
            if (currentItem != null && currentItem.getIconPkg() != null && !currentItem.getIconPkg().isEmpty()
                    && Util.isPackageExisted(ctx, currentItem.getIconPkg())) {
                Intent i = ctx.getPackageManager().getLaunchIntentForPackage(currentItem.getIconPkg());
                if (i != null) {
                    ctx.startActivity(i);
                }
            }
        } catch (Exception e) {
            Log.e("MICO", e.getMessage(), e);
        }
    }

    private void uninstall(Context ctx, IPObj currentItem) {
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.fromParts("package", currentItem.getIconPkg(), null));
        if (intent.resolveActivity(ctx.getPackageManager()) != null) {
            ctx.startActivity(intent);
        }
    }

    private void openPlay(Context ctx, IPObj currentItem) {
        try {
            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + currentItem.getIconPkg())));
        } catch (android.content.ActivityNotFoundException anfe) {
            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + currentItem.getIconPkg())));
        }
    }

    IconAdapter(List<IPObj> ipacks, int installed) {
        this.installed = installed;
        this.iconPacks = ipacks;
        // Cache preference values once to avoid repeated SharedPreferences reads
        this.prefUseFavorite = Prefs.useFavorite();
        this.prefShowTotalIcons = Prefs.isTotalIcons();
        this.prefShowSize = Prefs.showSize();
        this.prefShowPercentage = Prefs.showPercentage();
        this.prefShowAuthorName = Prefs.showAuthorName();
        this.prefSortBy = Prefs.sortBy();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public IconPackViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ctx = viewGroup.getContext();
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pack_card, viewGroup, false);
        return new IconPackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IconPackViewHolder holder, int i) {
        View currentView = holder.itemView;
        currentView.setTag(holder);
        IPObj obj = iconPacks.get(i);

        holder.currentItem = obj;
        holder.localIcon = new LocalIcon();

        // Set pack name
        holder.ipackName.setText(obj.getIconName());

        // Set author name
        String authorName = AuthorCache.getInstance().getAuthorName(ctx, obj.getIconPkg());
        if (authorName != null && !authorName.isEmpty()) {
            holder.ipackAuthor.setVisibility(View.VISIBLE);
            holder.ipackAuthor.setText(authorName);
        } else {
            holder.ipackAuthor.setVisibility(View.GONE);
        }

        // Favorite star
        if (Prefs.useFavorite()) {
            holder.iconStar.setVisibility(View.VISIBLE);
        } else {
            holder.iconStar.setVisibility(View.GONE);
        }

        IconAttr attr = gson.fromJson(obj.getAdditional(), IconAttr.class);
        holder.iconStar.setImageDrawable(ContextCompat.getDrawable(ctx,
                attr.isFavorite() ? R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp));

        // Stats
        bindStats(holder, obj, attr);

        // Update expand state
        holder.updateExpandState();

        // Load icon async
        try {
            new LoadIcon().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder.currentItem,
                    ctx.getPackageManager(), holder.localIcon, currentView);
        } catch (Exception r) {
            Log.e(TAG, "Error loading icon", r);
        }
    }

    private void bindStats(IconPackViewHolder holder, IPObj obj, IconAttr attr) {
        boolean anyStatVisible = false;

        // Icon count
        if (prefShowTotalIcons) {
            holder.statIconsContainer.setVisibility(View.VISIBLE);
            holder.statIconsValue.setText(String.valueOf(obj.getTotal()));
            anyStatVisible = true;
        } else {
            holder.statIconsContainer.setVisibility(View.GONE);
        }

        // Size
        if (prefShowSize) {
            holder.statSizeContainer.setVisibility(View.VISIBLE);
            holder.statSizeValue.setText(attr.getSize() + " MB");
            anyStatVisible = true;
        } else {
            holder.statSizeContainer.setVisibility(View.GONE);
        }

        // Themed percentage
        if (prefShowPercentage) {
            holder.statThemedContainer.setVisibility(View.VISIBLE);
            int missed = obj.getMissed();
            int themed = installed - missed;
            double percent = ((double) themed / installed) * 100;
            String result = String.format("%.0f%%", percent);
            holder.statThemedValue.setText(result);
            anyStatVisible = true;
        } else {
            holder.statThemedContainer.setVisibility(View.GONE);
        }

        // Hide stats row if no stats are visible
        holder.statsRow.setVisibility(anyStatVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return iconPacks.size();
    }

    private class LoadIcon extends AsyncTask<Object, Void, View> {
        @Override
        protected View doInBackground(Object... params) {
            IPObj ipObj = (IPObj) params[0];
            final PackageManager pkgMgr = (PackageManager) params[1];
            final LocalIcon icon = (LocalIcon) params[2];
            final View viewToUpdate = (View) params[3];
            try {
                icon.setDrawable(Util.resizeImage(ctx, pkgMgr.getApplicationIcon(ipObj.getIconPkg())));
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Package not found: " + ipObj.getIconPkg());
            }
            return viewToUpdate;
        }

        protected void onPostExecute(View viewToUpdate) {
            try {
                final IconPackViewHolder entryToUpdate = (IconPackViewHolder) viewToUpdate.getTag();
                entryToUpdate.icon.setImageDrawable(entryToUpdate.localIcon.drawable);
            } catch (Exception e) {
                Log.e(TAG, "Error showing icon", e);
            }
        }
    }

    private class LocalIcon {
        public Drawable getDrawable() {
            return drawable;
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        private Drawable drawable;
    }
}
