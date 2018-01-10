package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.license.LicenseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.LauncherHelper;
import dev.ukanth.iconmgr.util.LicenseCallbackHelper;
import dev.ukanth.iconmgr.util.PackageComparator;
import dev.ukanth.iconmgr.util.Util;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private TextView emptyView;
    private LicenseHelper mLicenseHelper;
    private IconAdapter adapter;
    private IPObjDao ipObjDao;
    private List<IPObj> iconPacksList;
    private SwipeRefreshLayout mSwipeLayout;
    private MaterialDialog plsWait;
    private Menu mainMenu;
    private static boolean reloadTheme = false;
    private static boolean reloadApp = false;
    private static int installed = 0;
    private IntentFilter filter;
    private IntentFilter insertFilter;
    private BroadcastReceiver mMessageReceiver;

    private BroadcastReceiver updateReceiver;


    private static final String SHORT_RANDOM =
            "dev.ukanth.iconmgr.shortcut.RANDOM";

    public static void setReloadTheme(boolean reloadTheme) {
        MainActivity.reloadTheme = reloadTheme;
    }

    public static void setReloadApp(boolean b) {
        MainActivity.reloadApp = b;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SHORT_RANDOM.equals(getIntent().getAction())) {
            callRandom();
        } else {
            if (Prefs.isDarkTheme(getApplicationContext())) {
                setTheme(R.style.AppTheme_Dark);
            } else {
                setTheme(R.style.AppTheme_Light);
            }

            setContentView(R.layout.content_main);

            DaoSession daoSession = ((App) getApplication()).getDaoSession();
            ipObjDao = daoSession.getIPObjDao();

            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            emptyView = (TextView) findViewById(R.id.empty_view);


            iconPacksList = new ArrayList<>();

            LinearLayoutManager llm = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(llm);
            recyclerView.setNestedScrollingEnabled(false);

            adapter = new IconAdapter(iconPacksList, installed);
            recyclerView.setAdapter(adapter);

            mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
            mSwipeLayout.setOnRefreshListener(this);

            loadApp(false);
            if (BuildConfig.LICENSE) {
                startLicenseCheck();
            }

            filter = new IntentFilter();
            filter.addAction("updatelist");
            mMessageReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String pkgName = intent.getStringExtra("pkgName");
                    if (pkgName != null) {
                        for (IPObj pack : iconPacksList) {
                            if (pack != null && pack.getIconPkg() != null && pack.getIconPkg().equals(pkgName)) {
                                iconPacksList.remove(pack);
                                adapter.notifyDataSetChanged();
                                setTitle(getString(R.string.app_name) + " - #" + iconPacksList.size());
                                return;
                            }
                        }
                    }
                }
            };

            insertFilter = new IntentFilter();
            insertFilter.addAction("insertlist");
            updateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String pkgName = intent.getStringExtra("pkgName");
                    if (pkgName != null) {
                        IPObj obj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(pkgName)).unique();
                        if (obj != null) {
                            iconPacksList.add(obj);
                            adapter.notifyDataSetChanged();
                            setTitle(getString(R.string.app_name) + " - #" + iconPacksList.size());
                        }
                    }
                }
            };

            registerReceiver(mMessageReceiver, filter);
            registerReceiver(updateReceiver, insertFilter);
        }


    }

    private void callRandom() {
        String currentLauncher = Util.getCurrentLauncher(getApplicationContext());
        IPObj ipObj = Util.getRandomInstalledIconPack(ipObjDao);
        if (currentLauncher != null) {
            if (ipObj != null) {
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.selected_pack) + ipObj.getIconName(), Toast.LENGTH_LONG).show();
                LauncherHelper.apply(getApplicationContext(), ipObj.getIconPkg(), currentLauncher);
            }
        } else {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.nodefault), Toast.LENGTH_LONG).show();
        }
    }

    private void startLicenseCheck() {
        try {
            byte[] salt = new byte[]{
                    -11, 115, 10, -19, -33,
                    -12, 18, -24, 21, 68,
                    -15, -45, 97, -17, -16,
                    -13, -11, 12, -14, 81
            };

            String licenseKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApXY+Hz2FyJ7rgvDjNiisklEMS6o0fRtQHgPi8uDpJxhr5IrOBu0LE8utemYXZYkYU8Hx4dhFr/lcgXJf9Sg6XXMybSwq0mS/N6OFAhI6Mo9Hjaw7sKfmf/8ogyMMQ0s88qjE4A7J0Eu8I12Bw0e2zPSb3Nz/oi3Wz9G0weGf6lNAqcrGaZwxSN/5fVOjy5fafKlH52Iln0t2GSuW97yiakD2XERTeQGlpTq5Dm7Lp4Ve4SqfmFi9m9w5PKLZJgkotFPcH8VsZgqElAwM3UK0Q4+J1TvBeQxugZHI6Uc5vUJeFvPpL8lGK80Dh16Z4kMcJyJsZpjFz6aoI2VdFrNhkQIDAQAB";

            if (Prefs.isFirstTime(getApplicationContext())) {
                mLicenseHelper = new LicenseHelper(this);
                mLicenseHelper.run(licenseKey, salt, new LicenseCallbackHelper(MainActivity.this));
                return;
            }

            if (!Prefs.isPS(getApplicationContext())) {
                if (!Prefs.isPS(getApplicationContext())) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.license_check)
                            .content(getString(R.string.license_check_failed))
                            .positiveText(R.string.close)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .cancelable(false)
                            .canceledOnTouchOutside(false)
                            .show();

                }
            }
        } catch (Exception e) {
            Log.e("MICO", e.getMessage(), e);
            Toast.makeText(getApplicationContext(), "Unable to validate license", Toast.LENGTH_LONG);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLicenseHelper != null) {
            mLicenseHelper.destroy();
        }
        if (mMessageReceiver != null) {
            unregisterReceiver(mMessageReceiver);
        }
        if (updateReceiver != null) {
            unregisterReceiver(updateReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (reloadTheme) {
            reloadTheme = false;
            restartActivity();
        }
        if (reloadApp) {
            reloadApp = false;
            restartActivity();
        }
    }

    private void restartActivity() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void loadApp(boolean forceLoad) {
        iconPacksList = new ArrayList<>();
        LoadAppList getAppList = new LoadAppList();
        if (plsWait == null && (getAppList.getStatus() == AsyncTask.Status.PENDING ||
                getAppList.getStatus() == AsyncTask.Status.FINISHED)) {
            getAppList.setContext(forceLoad, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void shareBitmap(Bitmap src, String fileName) {
        try {

            int w = src.getWidth();
            int h = src.getHeight();
            Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());
            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(src, 0, 0, null);
            Paint paint = new Paint();
            paint.setColor(Color.YELLOW);
            paint.setTextSize(50);
            paint.setAntiAlias(true);
            canvas.drawText("Micopacks", 400, 60, paint);

            File file = new File(getCacheDir(), fileName + ".png");
            FileOutputStream fOut = new FileOutputStream(file);
            result.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public Bitmap getScreenshotFromRecyclerView(RecyclerView view) {
        RecyclerView.Adapter adapter = view.getAdapter();
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = adapter.getItemCount();
            int height = 0;
            Paint paint = new Paint();
            int iHeight = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);
            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {
                    bitmaCache.put(String.valueOf(i), drawingCache);
                }
                height += holder.itemView.getMeasuredHeight();
            }
            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);
            bigCanvas.drawColor(Color.BLACK);
            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmaCache.get(String.valueOf(i));
                bigCanvas.drawBitmap(bitmap, 0f, iHeight, paint);
                iHeight += bitmap.getHeight();
                bitmap.recycle();
            }

        }
        return bigBitmap;
    }

    @Override
    public void onRefresh() {
        loadApp(false);
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        mainMenu = menu;
        //make sure we update sort entry
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (Prefs.sortBy(getApplicationContext())) {
                    case "s0":
                        mainMenu.findItem(R.id.sort_alpha).setChecked(true);
                        break;
                    case "s1":
                        mainMenu.findItem(R.id.sort_lastupdate).setChecked(true);
                        break;
                    case "s2":
                        mainMenu.findItem(R.id.sort_count).setChecked(true);
                        break;
                }
            }
        });

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView;


        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(this);
            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        searchView.setIconified(true);
                    }
                }
            });
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.pref:
                showPreference();
                return true;
            case R.id.about:
                showAbout();
                return true;
            case R.id.force:
                loadApp(true);
                return true;
            case R.id.share:
                final Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.generating), Toast.LENGTH_LONG);
                toast.show();
                new Thread(() -> shareBitmap(getScreenshotFromRecyclerView(recyclerView), System.currentTimeMillis() + "")).start();
                return true;
            case R.id.changelog:
                showChangelog();
                return true;
            case R.id.help:
                showHelp();
                return true;
            case R.id.report:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");

                String version = "";
                PackageInfo pInfo = null;
                try {
                    pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {

                }
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"uzoftinc+mico@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Report Issue: " + getString(R.string.app_name) + " " + version);
                i.putExtra(Intent.EXTRA_TEXT, "");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_random:
                callRandom();
                return true;
            case R.id.sort_alpha:
                Prefs.sortBy(getApplicationContext(), "s0");
                item.setChecked(true);
                reload();
                return true;
            case R.id.sort_lastupdate:
                Prefs.sortBy(getApplicationContext(), "s1");
                item.setChecked(true);
                reload();
                return true;
            case R.id.sort_count:
                Prefs.sortBy(getApplicationContext(), "s2");
                item.setChecked(true);
                reload();
                return true;
            case R.id.sort_size:
                Prefs.sortBy(getApplicationContext(), "s3");
                item.setChecked(true);
                reload();
                return true;
            case R.id.sort_percent:
                Prefs.sortBy(getApplicationContext(), "s4");
                item.setChecked(true);
                reload();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void reload() {
        Collections.sort(iconPacksList, new PackageComparator().setCtx(getApplicationContext()));
        adapter = new IconAdapter(iconPacksList, installed);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void showChangelog() {
        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .customView(R.layout.activity_changelog, false)
                .positiveText(R.string.ok)
                .show();
    }

    private void showPreference() {
        Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(myIntent);
    }

    private void showHelp() {
        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.about_help)
                .positiveText(R.string.ok)
                .show();
    }

    private void showAbout() {
        String version = "";
        PackageInfo pInfo = null;
        try {
            pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {

        }

        new MaterialDialog.Builder(this)
                .title(getApplicationContext().getString(R.string.app_name) + " " + version)
                .content(R.string.about_content)
                .positiveText(R.string.ok)
                .show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        List<IPObj> filteredModelList = filter(query);

        Collections.sort(new ArrayList(filteredModelList), new PackageComparator().setCtx(getApplicationContext()));
        adapter = new IconAdapter(filteredModelList, installed);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return true;
    }

    private List<IPObj> filter(String query) {
        List<IPObj> filteredPack = new ArrayList<>();
        if (query.length() >= 1) {
            for (IPObj ipack : iconPacksList) {
                if (ipack.getIconName().toLowerCase().contains(query.toLowerCase())) {
                    filteredPack.add(ipack);
                }
            }
        }
        return filteredPack.size() > 0 ? filteredPack : iconPacksList;
    }


    public class LoadAppList extends AsyncTask<Void, Integer, Void> {

        Context context = null;
        long startTime;
        boolean forceLoad = false;


        public LoadAppList setContext(boolean forceLoad, Context context) {
            this.forceLoad = forceLoad;
            this.context = context;
            return this;
        }

        @Override
        protected void onPreExecute() {
            plsWait = new MaterialDialog.Builder(context).cancelable(false).title(context.getString(R.string.loading)).content(R.string.please_wait_normal).progress(true, 0).show();
            startTime = System.currentTimeMillis();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                new IconPackManager(getApplicationContext()).updateIconPacks(ipObjDao, forceLoad, plsWait);
                installed = Util.getInstalledApps(getApplicationContext()).size();
                if (isCancelled())
                    return null;
                return null;
            } catch (SQLiteException sqe) {
                sqe.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Void result) {
            try {
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
                mSwipeLayout.setRefreshing(false);
                if (iconPacksList != null && !iconPacksList.isEmpty()) {
                    setTitle(getString(R.string.app_name) + " - #" + iconPacksList.size());
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    Collections.sort(iconPacksList, new PackageComparator().setCtx(getApplicationContext()));
                    adapter = new IconAdapter(iconPacksList, installed);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
                //Log.i("MICO", "Total time:" + (System.currentTimeMillis() - startTime) / 1000 + " sec");
            } catch (Exception e) {
                // nothing
                if (plsWait != null) {
                    plsWait.dismiss();
                    plsWait = null;
                }
                mSwipeLayout.setRefreshing(false);
            }
        }
    }
}
