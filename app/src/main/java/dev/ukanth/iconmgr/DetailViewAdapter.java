package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashMap;
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

    private IPObj pkgObj;
    private static final int TYPE_ICON_REQUEST = 0;
    private static final int TYPE_CONTENT_TOTAL = 1;
    private static final int TYPE_CONTENT_PERCENT = 2;
    private static final int TYPE_ICON_MASK = 3;

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
        switch (viewType) {
            case TYPE_ICON_REQUEST:
                View view = LayoutInflater.from(mContext).inflate(
                        R.layout.details_card, parent, false);
                return new IconRequestViewHolder(view);
            case TYPE_CONTENT_TOTAL:
                view = LayoutInflater.from(mContext).inflate(
                        R.layout.content_card, parent, false);
                return new ContentViewHolder(view);
            case TYPE_ICON_MASK:
                view = LayoutInflater.from(mContext).inflate(
                        R.layout.iconview_card, parent, false);
                return new IconViewHolder(view);
            case TYPE_CONTENT_PERCENT:
                view = LayoutInflater.from(mContext).inflate(
                        R.layout.content_card, parent, false);
                return new ContentViewHolder(view);
            default:
                view = LayoutInflater.from(mContext).inflate(
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
        try {
            if (holder.itemView != null) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams)
                        holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(isFullSpan(holder.getItemViewType()));
            }
        } catch (Exception e) {
        }

        switch (holder.getItemViewType()) {
            case TYPE_CONTENT_TOTAL:
                ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
                int finalPosition = position - 1;
                contentViewHolder.autoFitTitle.setText(mHomes.get(finalPosition).getTitle());

                if (mHomes.get(finalPosition).getSubtitle().length() > 0) {
                    contentViewHolder.subtitle.setText(mHomes.get(finalPosition).getSubtitle());
                    contentViewHolder.subtitle.setVisibility(View.VISIBLE);
                }
                break;
            case TYPE_CONTENT_PERCENT:
                contentViewHolder = (ContentViewHolder) holder;
                finalPosition = position - 1;

                contentViewHolder.autoFitTitle.setText(mHomes.get(finalPosition).getTitle());
                if (mHomes.get(finalPosition).getSubtitle().length() > 0) {
                    contentViewHolder.subtitle.setText(mHomes.get(finalPosition).getSubtitle());
                    contentViewHolder.subtitle.setVisibility(View.VISIBLE);
                }
                break;

            case TYPE_ICON_MASK:
                IconViewHolder viewHolder = (IconViewHolder) holder;
                finalPosition = position - 1;
                List<Bitmap> maps = mHomes.get(finalPosition).getListIcons();
                if (maps != null && maps.size() > 0) {
                    Drawable d = new BitmapDrawable(mContext.getResources(), maps.get(0));
                    viewHolder.image.setImageDrawable(d);
                    /*for (int i = 1; i < maps.size(); i++) {
                        d = new BitmapDrawable(mContext.getResources(), maps.get(i));
                        ImageView imageView = new ImageView(mContext);
                        imageView.setImageDrawable(d);
                        viewHolder.layout.addView(imageView);
                    }*/
                    viewHolder.autoFitTitle.setText(mHomes.get(finalPosition).getTitle());
                }
                break;
            case TYPE_ICON_REQUEST:

            {
                final IconRequestViewHolder iconRequestViewHolder = (IconRequestViewHolder) holder;
                //refresh package
                final PackageManager pm = mContext.getPackageManager();
                final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

                if (pkgObj.getMissed() == 0) {
                    final MaterialDialog plsWait = new MaterialDialog.Builder(activityContext).cancelable(false).title(mContext.getString(R.string.loading_stats)).content(R.string.please_wait).progress(true, 0).show();
                    IconDetails.process(mContext, pkgObj.getIconPkg(), AsyncTask.THREAD_POOL_EXECUTOR, new IconDetails.AsyncResponse() {
                        @Override
                        public void processFinish(HashMap<String, List> output) {
                            plsWait.dismiss();
                            if (output != null) {
                                List<Bitmap> bitMap = output.get("bitmap");
                                int installed = packages.size();
                                int missed = output.get("package").size();
                                int themed = installed - output.get("package").size();

                                double percent = ((double) themed / installed) * 100;
                                String result = String.format("%.2f", percent) + "%";


                                iconRequestViewHolder.installedApps.setText(String.format(
                                        mContext.getResources().getString(R.string.icon_request_installed_apps),
                                        installed, result));
                                iconRequestViewHolder.missedApps.setText(String.format(
                                        mContext.getResources().getString(R.string.icon_request_missed_apps),
                                        missed));
                                iconRequestViewHolder.themedApps.setText(String.format(
                                        mContext.getResources().getString(R.string.icon_request_themed_apps),
                                        themed));

                                iconRequestViewHolder.progress.setMax(installed);
                                iconRequestViewHolder.progress.setProgress(themed);

                                Detail d = new Detail(-1, String.valueOf(result),
                                        mContext.getResources().getString(R.string.iconPercent),
                                        Detail.Type.PERCENT);

                                addNewContent(d);

                                if (bitMap != null && bitMap.size() > 0) {
                                    d = new Detail(-1, mContext.getResources().getString(R.string.iconMask),
                                            mContext.getResources().getString(R.string.iconMask),
                                            Detail.Type.MASK);
                                    d.setListIcons(bitMap);
                                    addNewContent(d);
                                }
                            }
                        }
                    }, true);
                } else {

                    int installed = packages.size();
                    int themed = installed - pkgObj.getMissed();

                    double percent = ((double) themed / installed) * 100;
                    String result = String.format("%.2f", percent) + "%";


                    iconRequestViewHolder.installedApps.setText(String.format(
                            mContext.getResources().getString(R.string.icon_request_installed_apps),
                            installed, result));
                    iconRequestViewHolder.missedApps.setText(String.format(
                            mContext.getResources().getString(R.string.icon_request_missed_apps),
                            pkgObj.getMissed()));
                    iconRequestViewHolder.themedApps.setText(String.format(
                            mContext.getResources().getString(R.string.icon_request_themed_apps),
                            themed));

                    iconRequestViewHolder.progress.setMax(installed);
                    iconRequestViewHolder.progress.setProgress(themed);

                    Detail d = new Detail(-1, String.valueOf(result),
                            mContext.getResources().getString(R.string.iconPercent),
                            Detail.Type.PERCENT);

                    addNewContent(d);

                    IconDetails.process(mContext, pkgObj.getIconPkg(), AsyncTask.THREAD_POOL_EXECUTOR, new IconDetails.AsyncResponse() {
                        @Override
                        public void processFinish(HashMap<String, List> output) {
                            if (output != null) {
                                List<Bitmap> bitMap = output.get("bitmap");
                                if (bitMap != null && bitMap.size() > 0) {
                                    Detail detail = new Detail(-1, mContext.getResources().getString(R.string.iconMask),
                                            mContext.getResources().getString(R.string.iconMask),
                                            Detail.Type.MASK);
                                    detail.setListIcons(bitMap);
                                    addContent(detail);
                                }
                            }
                        }
                    }, false);
                }
            }
            break;
        }
    }

    @Override
    public int getItemCount() {
        return mHomes.size() + 1;
    }

    public void addNewContent(@NonNull Detail detail) {
        mHomes.add(detail);
        new Runnable() {
            @Override
            public void run() {
                notifyItemInserted(mHomes.size());
            }
        };
    }

    public void addContent(@NonNull Detail detail) {
        mHomes.add(detail);
        notifyItemInserted(mHomes.size());
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_ICON_REQUEST;
        if (position == 1) return TYPE_CONTENT_TOTAL;
        if (position == 2) return TYPE_CONTENT_PERCENT;
        if (position == 3) return TYPE_ICON_MASK;
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
            card.setUseCompatPadding(false);
            int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
            params.setMargins(0, 0, margin, margin);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.setMarginEnd(margin);
            }
        }

    }

    private class IconViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final LinearLayout layout;
        private final AutofitTextView autoFitTitle;

        IconViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.iconview_img);
            layout = (LinearLayout) itemView.findViewById(R.id.iconbase);
            autoFitTitle = (AutofitTextView) itemView.findViewById(R.id.title_icon);

            CardView card = (CardView) itemView.findViewById(R.id.iconview_card);
            card.setUseCompatPadding(false);
            card.setCardElevation(0);
        }

    }


    private class IconRequestViewHolder extends RecyclerView.ViewHolder {

        private final TextView installedApps;
        private final TextView themedApps;
        private final TextView missedApps;
        private final ProgressBar progress;

        IconRequestViewHolder(View itemView) {
            super(itemView);
            installedApps = (TextView) itemView.findViewById(R.id.installed_apps);
            missedApps = (TextView) itemView.findViewById(R.id.missed_apps);
            themedApps = (TextView) itemView.findViewById(R.id.themed_apps);
            progress = (ProgressBar) itemView.findViewById(R.id.progress);

            CardView card = (CardView) itemView.findViewById(R.id.card);
            card.setUseCompatPadding(false);
            int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
            params.setMargins(0, 0, margin, margin);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.setMarginEnd(margin);
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
