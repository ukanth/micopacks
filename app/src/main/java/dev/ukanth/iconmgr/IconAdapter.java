package dev.ukanth.iconmgr;

/**
 * Created by ukanth on 17/7/17.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import dev.ukanth.iconmgr.util.Util;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconPackViewHolder> {

    private Context ctx;


    public class IconPackViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        IconPack currentItem;
        TextView ipackName;
        TextView ipackCount;
        ImageView ipackIcon;

        public IconPackViewHolder(View view) {
            super(view);
            ipackName = (TextView) view.findViewById(R.id.ipack_name);
            ipackCount = (TextView) view.findViewById(R.id.ipack_icon_count);
            ipackIcon = (ImageView) view.findViewById(R.id.ipack_icon);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(ctx)
                            .title(R.string.title)
                            .items(R.array.items)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    switch (which) {
                                        case 0:
                                            determineApply(ctx, currentItem);
                                            break;
                                        case 1:
                                            openPlay(ctx, currentItem);
                                            //Util.changeSharedPreferences(ctx, "com.teslacoilsw.launcher", currentItem.name + ":GO:" + currentItem.packageName);
                                            //Util.restartLauncher(ctx, "com.teslacoilsw.launcher");
                                            break;
                                        case 2:
                                            openApp(ctx, currentItem);
                                            break;
                                        case 3:
                                            uninstall(ctx, currentItem);
                                            break;

                                    }
                                }
                            })
                            .show();

                }
            });
        }
    }

    private void openApp(Context ctx, IconPack currentItem) {
        Intent i = ctx.getPackageManager().getLaunchIntentForPackage(currentItem.packageName);
        ctx.startActivity(i);
    }

    private void uninstall(Context ctx, IconPack currentItem) {
        Intent intent = new Intent();
        intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.fromParts("package", currentItem.packageName, null));
        if (intent.resolveActivity(ctx.getPackageManager()) != null) {
            ctx.startActivity(intent);
        }
    }

    private void openPlay(Context ctx, IconPack currentItem) {
        try {
            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + currentItem.packageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + currentItem.packageName)));
        }
    }

    private void determineApply(Context ctx, IconPack currentItem) {
        String currentLauncher = getCurrentLauncher(ctx);
        if (currentLauncher != null) {
            LauncherHelper.apply(ctx, currentItem.packageName, currentLauncher);
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.nodefault), Toast.LENGTH_LONG).show();
        }
    }

    private String getCurrentLauncher(Context ctx) {
        String name = null;
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = ctx.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            // should not happen. A home is always installed, isn't it?
        } else if ("android".equals(res.activityInfo.packageName)) {
            // No default selected
        } else {
            name = res.activityInfo.packageName;
        }
        return name;
    }

    List<IconPack> iconPacks;

    IconAdapter(Context ctx, List<IconPack> ipacks) {
        this.ctx = ctx;
        this.iconPacks = ipacks;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public IconPackViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pack_card, viewGroup, false);
        IconPackViewHolder pvh = new IconPackViewHolder(v);

        return pvh;
    }

    @Override
    public void onBindViewHolder(IconPackViewHolder personViewHolder, int i) {
        personViewHolder.currentItem = iconPacks.get(i);
        personViewHolder.ipackName.setText(iconPacks.get(i).name);
        personViewHolder.ipackCount.setText("Total Icons: " + Integer.toString(iconPacks.get(i).getCount()));
        PackageManager pm = ctx.getPackageManager();
        try {
            Drawable drawable = pm.getApplicationIcon(iconPacks.get(i).packageName);
            personViewHolder.ipackIcon.setImageDrawable(drawable);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    @Override
    public int getItemCount() {
        return iconPacks.size();
    }

}
