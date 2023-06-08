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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.util.Util;

import static dev.ukanth.iconmgr.tasker.FireReceiver.TAG;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconPackViewHolder> {

    private Context ctx;
    protected List<IPObj> iconPacks;


    private int installed;

    public class IconPackViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        IPObj currentItem;
        LocalIcon localIcon;
        TextView ipackName;
        TextView ipackCount;
        ImageView icon;
        ImageView iconImp;

        public IconPackViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.cv);
            ipackName = (TextView) view.findViewById(R.id.ipack_name);
            ipackCount = (TextView) view.findViewById(R.id.ipack_icon_count);
            icon = (ImageView) view.findViewById(R.id.ipack_icon);
            iconImp = (ImageView) view.findViewById(R.id.icon_star);
            iconImp.setOnClickListener(view13 -> {
                if (currentItem != null && currentItem.getIconPkg() != null) {
                    IconAttr attr = new Gson().fromJson(currentItem.getAdditional(), IconAttr.class);
                    attr.setFavorite(!attr.isFavorite());
                    currentItem.setAdditional(new Gson().toJson(attr).toString());
                    App app = ((App) ctx.getApplicationContext());
                    app.getDaoSession().getIPObjDao().update(currentItem);
                    if (attr.isFavorite()) {
                        iconImp.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_star_black_24dp));
                        //Toast.makeText(ctx, "Added to Favorites", Toast.LENGTH_SHORT).show();
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
                            if (manager.getDynamicShortcuts().size()  < manager.getMaxShortcutCountPerActivity()) {
                                manager.addDynamicShortcuts(Arrays.asList(shortcut));
                            }
                        }

                    } else {
                        iconImp.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_star_border_black_24dp));
                        //Toast.makeText(ctx, "Removed to Favorites", Toast.LENGTH_SHORT).show();
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                            ctx.getSystemService(ShortcutManager.class).removeDynamicShortcuts(Arrays.asList(currentItem.getIconPkg()));
                        }
                    }

                }
            });
            icon.setOnClickListener(view12 -> {
                if (currentItem != null && currentItem.getIconPkg() != null) {
                    Intent intent = new Intent(ctx, IconPreviewActivity.class);
                    intent.putExtra("pkg", currentItem.getIconPkg());
                    ctx.startActivity(intent);
                }

            });
            view.setOnClickListener(v -> new MaterialDialog.Builder(ctx)
                    .title(ctx.getString(R.string.title) + " " + currentItem.getIconName())
                    .items(R.array.items)
                    .itemsCallback((dialog, view1, which, text) -> performAction(which, currentItem))
                    .show());
        }
    }


    private void performAction(int which, IPObj currentItem) {
        switch (which) {
            case 0:
                Util.determineApply(ctx, currentItem);
                break;
            case 1:
                preview(ctx, currentItem);
                break;
            case 2:
                stats(ctx, currentItem);
                break;
            case 3:
                openPlay(ctx, currentItem);
                break;
            case 4:
                openApp(ctx, currentItem);
                break;
            case 5:
                uninstall(ctx, currentItem);
                break;

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
                if(i != null) {
                    ctx.startActivity(i);
                }
            }
        } catch (Exception e) {
            Log.e("MICO", e.getMessage(), e);
        }
    }

    private void uninstall(Context ctx, IPObj currentItem) {
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
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
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public IconPackViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        ctx = viewGroup.getContext();
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pack_card, viewGroup, false);
        return new IconPackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(IconPackViewHolder personViewHolder, int i) {
        View currentView = personViewHolder.itemView;
        currentView.setTag(personViewHolder);
        IPObj obj = iconPacks.get(i);
        if (Prefs.useFavorite()) {
            personViewHolder.iconImp.setVisibility(View.VISIBLE);
        } else {
            personViewHolder.iconImp.setVisibility(View.GONE);
        }
        personViewHolder.iconImp.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_star_border_black_24dp));
        IconAttr attr = new Gson().fromJson(obj.getAdditional(), IconAttr.class);
        if (attr.isFavorite()) {
            personViewHolder.iconImp.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_star_black_24dp));
        }
        personViewHolder.localIcon = new LocalIcon();
        personViewHolder.currentItem = obj;
        personViewHolder.ipackName.setText(obj.getIconName());

        StringBuilder builder = new StringBuilder();
        boolean isshown = false;

        if (Prefs.isTotalIcons()) {
            builder.append(ctx.getString(R.string.noicons) + " " + obj.getTotal());
            isshown = true;
        }
        if (Prefs.showSize()) {
            if (Prefs.isTotalIcons()) {
                builder.append(" - ");
            }
            isshown = true;
            builder.append(attr.getSize() + " MB");
        }
        if (Prefs.showPercentage()) {
            if (Prefs.isTotalIcons() || Prefs.showSize()) {
                builder.append(" - ");
            }
            isshown = true;
            int missed = obj.getMissed();
            int themed = installed - missed;
            double percent = ((double) themed / installed) * 100;
            String result = String.format("%.2f", percent) + "%";
            builder.append(" " + result);
        }

        if (Prefs.sortBy().equals("s1")) {
            if (Prefs.isTotalIcons() || Prefs.showSize() || Prefs.showPercentage()) {
                builder.append(" - ");
            }
            builder.append(" " + Util.prettyFormat(new Date(System.currentTimeMillis() - obj.getInstallTime())));
        }

        personViewHolder.ipackCount.setText(builder.toString());

        if (!isshown) {
            personViewHolder.ipackCount.setVisibility(View.GONE);
        }

        try {
            new LoadIcon().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, personViewHolder.currentItem,
                    ctx.getPackageManager(), personViewHolder.localIcon, currentView);
        } catch (Exception r) {
        }
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

    /*@RequiresApi(api = Build.VERSION_CODES.O)
    public static Bitmap getAppIcon(PackageManager mPackageManager, String packageName) {
        try {
            Drawable drawable = mPackageManager.getApplicationIcon(packageName);

            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            } else if (drawable instanceof AdaptiveIconDrawable) {
                Drawable backgroundDr = ((AdaptiveIconDrawable) drawable).getBackground();
                Drawable foregroundDr = ((AdaptiveIconDrawable) drawable).getForeground();

                Drawable[] drr = new Drawable[2];
                drr[0] = backgroundDr;
                drr[1] = foregroundDr;

                LayerDrawable layerDrawable = new LayerDrawable(drr);

                int width = layerDrawable.getIntrinsicWidth();
                int height = layerDrawable.getIntrinsicHeight();

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);

                layerDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                layerDrawable.draw(canvas);

                return bitmap;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }*/

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
