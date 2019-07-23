package dev.ukanth.iconmgr;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.glidebitmappool.GlideBitmapPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;

/**
 * Created by ukanth on 3/9/17.
 */

public class IconSearchActivity extends AppCompatActivity {

    private MaterialDialog plsWait;

    private LinearLayout.LayoutParams params;

    private GridLayout gridLayout;
    private List<IPObj> objList;

    private SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.i_search);
        mSearchView = (SearchView) searchItem.getActionView();
        setupSearchView(searchItem);

        return true;
    }

    private void setupSearchView(MenuItem searchItem) {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                gridLayout.removeAllViews();
                searchIcons(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() == 0) {
                    gridLayout.removeAllViews();
                }
                return false;
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Prefs.isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }

        GlideBitmapPool.initialize(10 * 1024 * 1024);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.iconsearch);


        gridLayout = (GridLayout) findViewById(R.id.iconsearchpreview);
        int colNumber = 4;
        gridLayout.setColumnCount(colNumber);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        params = new LinearLayout.LayoutParams(screenWidth / colNumber, screenWidth / colNumber);

        //SearchView searchView = findViewById(R.id.search);


        App app = ((App) getApplicationContext());
        DaoSession daoSession = app.getDaoSession();
        IPObjDao ipObjDao = daoSession.getIPObjDao();

        objList = ipObjDao.loadAll();


    }

    private void searchIcons(String query) {
        if (!query.isEmpty()) {
            query = query.toLowerCase();
        }
        IconsPreviewLoader previewLoader = new IconsPreviewLoader(IconSearchActivity.this, query);
        if (plsWait == null && (previewLoader.getStatus() == AsyncTask.Status.PENDING ||
                previewLoader.getStatus() == AsyncTask.Status.FINISHED)) {
            previewLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private class IconsPreviewLoader extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private String query;
        private List<Icon> themed_icons = new ArrayList<>();

        private IconsPreviewLoader(Context context, String query) {
            this.query = query;
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            plsWait = new MaterialDialog.Builder(mContext).cancelable(false).title(mContext.getString(R.string.searching)).content(R.string.please_wait_normal).progress(true, 0).show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    IconPackUtil packUtil = new IconPackUtil();
                    for (IPObj obj : objList) {
                        themed_icons.addAll(packUtil.getFilterIcons(obj.getIconPkg(), query));
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

            gridLayout.removeAllViews();

            if (themed_icons != null) {
                List<Icon> list = new ArrayList<Icon>(themed_icons);
                if (list != null && list.size() > 0) {
                    Collections.sort(list, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getTitle(), o2.getTitle()));

                    for (final Icon icon : list) {
                        if (icon.getIconBitmap() != null) {
                            ImageView image = new ImageView(mContext);
                            image.setLayoutParams(params);
                            image.setPadding(15, 15, 15, 15);
                            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            image.setImageDrawable(new BitmapDrawable(getResources(), icon.getIconBitmap()));
                            image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new MaterialDialog.Builder(mContext)
                                            .title(icon.getTitle())
                                            .positiveText(R.string.save)
                                            .onPositive((dialog, which) -> {

                                            })
                                            .negativeText(R.string.close)
                                            .icon(new BitmapDrawable(getResources(), icon.getIconBitmap()))
                                            .show();
                                }
                            });
                            image.setOnLongClickListener(view -> {

                                return true;
                            });
                            gridLayout.addView(image);
                        }
                    }
                    GlideBitmapPool.clearMemory();
                    //processInputs(list, res, params, gridLayout);
                } else {
                }
            }
        }
    }
}
