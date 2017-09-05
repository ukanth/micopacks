package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dev.ukanth.iconmgr.util.LauncherHelper;

/**
 * Created by ukanth on 3/9/17.
 */

public class IconPreviewActivity extends AppCompatActivity {


    private MaterialDialog plsWait;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Prefs.isDarkTheme(getApplicationContext())) {
            setTheme(R.style.AppTheme_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.iconpreview);


        Bundle bundle = getIntent().getExtras();
        final String pkgName = bundle.getString("pkg");

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String launcherPack = LauncherHelper.getLauncherPackage(getApplicationContext());
                LauncherHelper.apply(getApplicationContext(), pkgName, launcherPack);
            }
        });

        if (!Prefs.isFabShow(getApplicationContext())) {
            fab.setVisibility(View.GONE);
        }

        IconsPreviewLoader previewLoader = new IconsPreviewLoader(IconPreviewActivity.this, pkgName);

        if (plsWait == null && (previewLoader.getStatus() == AsyncTask.Status.PENDING ||
                previewLoader.getStatus() == AsyncTask.Status.FINISHED)) {
            previewLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class IconsPreviewLoader extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private String packageName;
        private Set<Icon> themed_icons;
        private Set<Icon> nonthemed_icons;

        private IconsPreviewLoader(Context context, String packageName) {
            this.mContext = context;
            this.packageName = packageName;
        }

        @Override
        protected void onPreExecute() {
            plsWait = new MaterialDialog.Builder(mContext).cancelable(false).title(mContext.getString(R.string.loading_preview)).content(R.string.please_wait).progress(true, 0).show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    IconPackUtil packUtil = new IconPackUtil();
                    themed_icons = packUtil.getListIcons(mContext, packageName);
                    if(Prefs.isNonPreview(getApplicationContext())) {
                        nonthemed_icons = packUtil.getNonThemeIcons(mContext, packageName);
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            try {
                if (plsWait != null && plsWait.isShowing()) {
                    plsWait.dismiss();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {
                plsWait.dismiss();
                plsWait = null;
            }

            if (themed_icons != null) {
                List<Icon> list = new ArrayList<Icon>(themed_icons);
                if (Prefs.isNonPreview(getApplicationContext()) && nonthemed_icons != null) {
                    List<Icon> listNonTheme = new ArrayList<Icon>(nonthemed_icons);
                    list.addAll(listNonTheme);
                }
                Collections.sort(list, new Comparator<Icon>() {
                    public int compare(Icon o1, Icon o2) {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getTitle(), o2.getTitle());
                    }
                });
                GridLayout gridLayout = (GridLayout) findViewById(R.id.iconpreview);
                gridLayout.invalidate();
                int colNumber = Prefs.getCol(getApplicationContext());;
                gridLayout.setColumnCount(colNumber);
                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                int screenWidth = metrics.widthPixels;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth / colNumber, screenWidth / colNumber);
                Resources res = mContext.getResources();
                processInputs(list, res, params, gridLayout);
            }
        }

        public void processInputs(List<Icon> listIcons, final Resources res, final LinearLayout.LayoutParams params, final GridLayout gridLayout) {
            try {
                ExecutorService service = Executors.newFixedThreadPool(2);

                List<Future<String>> futures = new ArrayList<Future<String>>();
                for (final Icon icon : listIcons) {
                    Callable<String> callable = new Callable<String>() {
                        public String call() throws Exception {
                            if (icon.getIconBitmap() != null) {
                                ImageView image = new ImageView(mContext);
                                image.setLayoutParams(params);
                                image.setPadding(15, 15, 15, 15);
                                image.setScaleType(ImageView.ScaleType.FIT_XY);
                                image.setImageDrawable(new BitmapDrawable(res, icon.getIconBitmap()));
                                image.setOnClickListener(new ImageView.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Toast.makeText(mContext, icon.getTitle(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                gridLayout.addView(image);
                            }
                            return "";
                        }
                    };
                    futures.add(service.submit(callable));
                }
                service.shutdown();
                List<String> outputs = new ArrayList<String>();
                for (Future<String> future : futures) {
                    outputs.add(future.get());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
