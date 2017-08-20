package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import dev.ukanth.iconmgr.dao.IPObj;
import me.grantland.widget.AutofitTextView;

/**
 * Created by ukanth on 15/8/17.
 */

public class DetailViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final Context activityContext;
    private final List<Detail> mHomes;
    private final Detail.Style mImageStyle;

    private int mOrientation;

    private boolean onBind;

    private IPObj pkgObj;
    private static final int TYPE_ICON_REQUEST = 0;
    private static final int TYPE_CONTENT_TOTAL = 1;
    private static final int TYPE_CONTENT_PERCENT = 2;

    public DetailViewAdapter(@NonNull Context activityContext, @NonNull Context context, List<Detail> homes, int orientation, @NonNull IPObj pkgName) {
        this.mContext = context;
        this.activityContext = activityContext;
        this.mHomes = homes;
        this.pkgObj = pkgName;
        this.mOrientation = orientation;
        this.mImageStyle = ViewHelper.getHomeImageViewStyle("landscape");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ICON_REQUEST) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.details_card, parent, false);
            return new IconRequestViewHolder(view);
        } else if (viewType == TYPE_CONTENT_TOTAL) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.content_card, parent, false);
            return new ContentViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.content_card, parent, false);
            return new ContentViewHolder(view);
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        /*if (holder.getItemViewType() == TYPE_CONTENT_TOTAL) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
            contentViewHolder.autoFitTitle.setSingleLine(false);
            contentViewHolder.autoFitTitle.setMaxLines(10);
            contentViewHolder.autoFitTitle.setSizeToFit(true);
            contentViewHolder.autoFitTitle.setGravity(Gravity.CENTER_VERTICAL);
            contentViewHolder.autoFitTitle.setIncludeFontPadding(true);
            contentViewHolder.autoFitTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            contentViewHolder.subtitle.setVisibility(View.VISIBLE);
            contentViewHolder.subtitle.setGravity(Gravity.CENTER_VERTICAL);
        }*/
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        onBind = true;
        try {
            if (holder.itemView != null) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams)
                        holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(isFullSpan(holder.getItemViewType()));
            }
        } catch (Exception e) {
        }

        if (holder.getItemViewType() == TYPE_CONTENT_TOTAL) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
            int finalPosition = position - 1;
            contentViewHolder.autoFitTitle.setText(mHomes.get(finalPosition).getTitle());

            if (mHomes.get(finalPosition).getSubtitle().length() > 0) {
                contentViewHolder.subtitle.setText(mHomes.get(finalPosition).getSubtitle());
                contentViewHolder.subtitle.setVisibility(View.VISIBLE);
            }
        } else if (holder.getItemViewType() == TYPE_CONTENT_PERCENT) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
            int finalPosition = position - 1;

            contentViewHolder.autoFitTitle.setText(mHomes.get(finalPosition).getTitle());
            if (mHomes.get(finalPosition).getSubtitle().length() > 0) {
                contentViewHolder.subtitle.setText(mHomes.get(finalPosition).getSubtitle());
                contentViewHolder.subtitle.setVisibility(View.VISIBLE);
            }
        } else if (holder.getItemViewType() == TYPE_ICON_REQUEST) {
            final IconRequestViewHolder iconRequestViewHolder = (IconRequestViewHolder) holder;
            int installed = 0;
            int missed = 0;
            missed = pkgObj.getMissed();
            //refresh package
            if (missed == 0) {
                final MaterialDialog plsWait = new MaterialDialog.Builder(activityContext).cancelable(false).title(mContext.getString(R.string.loading_stats)).content(R.string.please_wait).progress(true, 0).show();
                IconRequest.process(mContext, pkgObj.getIconPkg(), AsyncTask.THREAD_POOL_EXECUTOR, new IconRequest.AsyncResponse() {
                    @Override
                    public void processFinish(List<String> output) {
                        plsWait.dismiss();
                        if (output != null) {
                            PackageManager pm = mContext.getPackageManager();
                            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                            int installed = packages.size();
                            int missed = output.size();
                            int themed = installed - output.size();

                            double percent = ((double) themed / installed) * 100;
                            String result = String.format("%.2f", percent);

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

                            //now add new card about details
                            Detail d = new Detail(-1, String.valueOf(result + "%"),
                                    mContext.getResources().getString(R.string.iconPercent),
                                    Detail.Type.PERCENT);
                            addNewContent(d);
                        }
                    }
                });
            } else {

                PackageManager pm = mContext.getPackageManager();
                List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                installed = packages.size();
                int themed = installed - missed;

                double percent = ((double) themed / installed) * 100;
                String result = String.format("%.2f", percent);


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

                //now add new card about details
                Detail d = new Detail(-1, String.valueOf(result + "%"),
                        mContext.getResources().getString(R.string.iconPercent),
                        Detail.Type.PERCENT);
                addNewContent(d);
            }
        }
        onBind = false;
    }

    @Override
    public int getItemCount() {
        return mHomes.size() + 1;
    }

    public void addNewContent(@Nullable Detail detail) {
        if (detail == null) return;

        mHomes.add(detail);
        new Runnable() {
            @Override
            public void run() {
                notifyItemInserted(mHomes.size());
            }
        };

    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_ICON_REQUEST;
        if (position == 1) return TYPE_CONTENT_TOTAL;
        if (position == 2) return TYPE_CONTENT_PERCENT;
        return -1;
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder {

        private final TextView subtitle;
        private final AutofitTextView autoFitTitle;

        ContentViewHolder(View itemView) {
            super(itemView);
            autoFitTitle = (AutofitTextView) itemView.findViewById(R.id.title_content);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle_content);

            CardView card = (CardView) itemView.findViewById(R.id.content_card);
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
            card.setCardElevation(0);
        }

    }


    private class IconRequestViewHolder extends RecyclerView.ViewHolder {

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
        }
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
