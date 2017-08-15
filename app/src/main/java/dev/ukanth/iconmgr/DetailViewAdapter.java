package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;

/**
 * Created by ukanth on 15/8/17.
 */

public class DetailViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Detail> mHomes;
    private final Detail.Style mImageStyle;
    private String packageName;

    private int mItemsCount;
    private int mOrientation;

    private static final int TYPE_ICON_REQUEST = 0;
    private static final int TYPE_CONTENT = 1;

    public DetailViewAdapter(@NonNull Context context, @NonNull String pkgName, int orientation) {
        mContext = context;
        mHomes = new ArrayList<>();
        this.packageName = pkgName;
        mOrientation = orientation;
        mImageStyle = ViewHelper.getHomeImageViewStyle("landscape");
        mItemsCount = 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.details_card, parent, false);
        return new IconRequestViewHolder(view);

    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder.itemView != null) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams)
                        holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(isFullSpan(holder.getItemViewType()));
            }
        } catch (Exception e) {
        }

        final IconRequestViewHolder iconRequestViewHolder = (IconRequestViewHolder) holder;

        App app = ((App) mContext.getApplicationContext());
        DaoSession daoSession = app.getDaoSession();
        IPObjDao ipObjDao = daoSession.getIPObjDao();
        int installed = 0;
        int missed = 0;

        IPObj obj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(packageName)).unique();
        missed = obj.getMissed();
        List<String> missPackage;
        //refresh package
        if (missed == 0) {
            IconRequest.start(mContext, packageName, AsyncTask.THREAD_POOL_EXECUTOR, new IconRequest.AsyncResponse() {
                @Override
                public void processFinish(List<String> output) {
                    if (output != null) {
                        PackageManager pm = mContext.getPackageManager();
                        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                        int installed = packages.size();
                        int missed = output.size();
                        int themed = installed - output.size();

                        iconRequestViewHolder.installedApps.setText(String.format(
                                mContext.getResources().getString(R.string.icon_request_installed_apps),
                                installed));
                        iconRequestViewHolder.missedApps.setText(String.format(
                                mContext.getResources().getString(R.string.icon_request_missed_apps),
                                missed));
                        iconRequestViewHolder.themedApps.setText(String.format(
                                mContext.getResources().getString(R.string.icon_request_themed_apps),
                                themed));

                        iconRequestViewHolder.progress.setMax(installed);
                        iconRequestViewHolder.progress.setProgress(themed);
                    }
                }
            });
        } else {

            PackageManager pm = mContext.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            installed = packages.size();
            int themed = installed - missed;

            iconRequestViewHolder.installedApps.setText(String.format(
                    mContext.getResources().getString(R.string.icon_request_installed_apps),
                    installed));
            iconRequestViewHolder.missedApps.setText(String.format(
                    mContext.getResources().getString(R.string.icon_request_missed_apps),
                    missed));
            iconRequestViewHolder.themedApps.setText(String.format(
                    mContext.getResources().getString(R.string.icon_request_themed_apps),
                    themed));

            iconRequestViewHolder.progress.setMax(installed);
            iconRequestViewHolder.progress.setProgress(themed);
        }


    }

    @Override
    public int getItemCount() {
        return mHomes.size() + mItemsCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == (mHomes.size() + 1)) return TYPE_ICON_REQUEST;
        return TYPE_CONTENT;
    }

    private class IconRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView title;
        private final TextView installedApps;
        private final TextView themedApps;
        private final TextView missedApps;
        private final LinearLayout container;
        private final ProgressBar progress;

        IconRequestViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            installedApps = (TextView) itemView.findViewById(R.id.installed_apps);
            missedApps = (TextView) itemView.findViewById(R.id.missed_apps);
            themedApps = (TextView) itemView.findViewById(R.id.themed_apps);
            progress = (ProgressBar) itemView.findViewById(R.id.progress);
            container = (LinearLayout) itemView.findViewById(R.id.container);

            CardView card = (CardView) itemView.findViewById(R.id.card);

            if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                card.setRadius(0f);
                card.setUseCompatPadding(false);
                int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                params.setMargins(0, 0, margin, margin);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    params.setMarginEnd(margin);
                }
            }
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
        }
    }


    public Detail getItem(int position) {
        return mHomes.get(position - 1);
    }

    public int getIconsIndex() {
        int index = -1;
        for (int i = 0; i < getItemCount(); i++) {
            int type = getItemViewType(i);
            if (type == TYPE_CONTENT) {
                int pos = i - 1;
                if (mHomes.get(pos).getType() == Detail.Type.ICONS) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public int getIconRequestIndex() {
        int index = -1;
        for (int i = 0; i < getItemCount(); i++) {
            int type = getItemViewType(i);
            if (type == TYPE_ICON_REQUEST) {
                index = i;
                break;
            }
        }
        return index;
    }

    private boolean isFullSpan(int viewType) {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        } else if (mImageStyle.getType() == Detail.Style.Type.SQUARE ||
                mImageStyle.getType() == Detail.Style.Type.LANDSCAPE) {
            return true;
        }
        return false;
    }
}
