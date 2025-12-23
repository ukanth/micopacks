package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.PackageComparator;
import dev.ukanth.iconmgr.util.Util;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private TextView emptyView;
    private IconAdapter adapter;
    private IPObjDao ipObjDao = App.getInstance().getIPObjDao();;
    private List<IPObj> iconPacksList;
    private HashSet<String> iconPacksSet; // For O(1) duplicate checking
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

    private BroadcastReceiver packageReceiver;
    private IntentFilter packageFilter;
    private ExecutorService receiverExecutor; // Shared thread pool for receivers


    public static void setReloadTheme(boolean reloadTheme) {
        MainActivity.reloadTheme = reloadTheme;
    }

    public static void setReloadApp(boolean b) {
        MainActivity.reloadApp = b;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       /* if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(App.getInstance());*/





        if (Prefs.isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }

        setContentView(R.layout.content_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        emptyView = (TextView) findViewById(R.id.empty_view);

        iconPacksList = new ArrayList<>();
        iconPacksSet = new HashSet<>();
        receiverExecutor = Executors.newSingleThreadExecutor();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        recyclerView.setNestedScrollingEnabled(false);

        adapter = new IconAdapter(iconPacksList, installed);
        recyclerView.setAdapter(adapter);

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);

        loadApp(false);

        filter = new IntentFilter();
        filter.addAction("updatelist");
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String pkgName = intent.getStringExtra("pkgName");
                if (pkgName != null && iconPacksSet.contains(pkgName)) {
                    // Use iterator to safely remove during iteration
                    Iterator<IPObj> iterator = iconPacksList.iterator();
                    while (iterator.hasNext()) {
                        IPObj pack = iterator.next();
                        if (pack != null && pack.getIconPkg() != null && pack.getIconPkg().equals(pkgName)) {
                            iterator.remove();
                            iconPacksSet.remove(pkgName);
                            runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                setTitle(getString(R.string.app_name) + " - #" + iconPacksList.size());
                            });
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
                if (pkgName != null && !iconPacksSet.contains(pkgName)) {
                    // Run database query on shared thread pool
                    receiverExecutor.execute(() -> {
                        IPObj obj = ipObjDao.getByIconPkg(pkgName);
                        if (obj != null) {
                            runOnUiThread(() -> {
                                // Double-check to prevent race conditions
                                if (!iconPacksSet.contains(pkgName)) {
                                    iconPacksList.add(obj);
                                    iconPacksSet.add(pkgName);
                                    adapter.notifyDataSetChanged();
                                    setTitle(getString(R.string.app_name) + " - #" + iconPacksList.size());
                                }
                            });
                        }
                    });
                }
            }
        };


        packageReceiver = new PackageBroadcast();

        packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addDataScheme("package");
        registerReceiver(packageReceiver, packageFilter);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                getApplicationContext().getPackageManager().getUserBadgedLabel("", android.os.Process.myUserHandle());
                //above is reflection for below...
                //UserManager.get();
            } catch (Throwable e) {
            }
        }

        registerReceiver(mMessageReceiver, filter);
        registerReceiver(updateReceiver, insertFilter);

        registerService();;
        //setUpItemTouchHelper();
    }

    private void registerService() {
        Intent i= new Intent(getApplicationContext(), InstallNotificationService.class);
        startService(i);
    }

    private void callRandom() {
        IPObj ipObj = Util.getRandomInstalledIconPack(ipObjDao);
        Util.determineApply(MainActivity.this,ipObj);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMessageReceiver != null) {
            unregisterReceiver(mMessageReceiver);
        }
        if (updateReceiver != null) {
            unregisterReceiver(updateReceiver);
        }
        if(packageReceiver !=null) {
            unregisterReceiver(packageReceiver);
        }
        if (receiverExecutor != null) {
            receiverExecutor.shutdown();
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

            if (src != null) {
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

                File file = new File(getExternalCacheDir(), fileName + ".png");
                FileOutputStream fOut = new FileOutputStream(file);
                result.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
                file.setReadable(true, false);
                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".dev.ukanth.iconmgr.provider", file);
                //Uri fileUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".GenericFileProvider", file.getAbsoluteFile());
                final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                String type = mime.getMimeTypeFromExtension(ext);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    //Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".dev.ukanth.iconmgr.provider", file);
                    intent.setDataAndType(contentUri, type);
                } else {
                    intent.setDataAndType(Uri.fromFile(file), type);
                }

                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                intent.setType("image/png");
                startActivity(intent);
            } else {

            }

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.unable_screenshot), Toast.LENGTH_SHORT).show());
        }
    }


    public Bitmap getScreenshotFromRecyclerView(RecyclerView view) {
        Bitmap bigBitmap = null;
        try {

            RecyclerView.Adapter adapter = view.getAdapter();

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
        } catch (OutOfMemoryError | IllegalArgumentException outOfMemoryError) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.unable_screenshot), Toast.LENGTH_SHORT).show());
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
        runOnUiThread(() -> {
            switch (Prefs.sortBy()) {
                case "s0":
                    mainMenu.findItem(R.id.sort_alpha).setChecked(true);
                    break;
                case "s1":
                    mainMenu.findItem(R.id.sort_lastupdate).setChecked(true);
                    break;
                case "s2":
                    mainMenu.findItem(R.id.sort_count).setChecked(true);
                    break;
                case "s5" :
                    mainMenu.findItem(R.id.author_name).setChecked(true);
            }
        });

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView;


        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(this);
            searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    searchView.setIconified(true);
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
            case R.id.iconsearch:
                showIconSearch();
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
            case R.id.favorite:
                showFav();
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
                Prefs.sortBy("s0");
                item.setChecked(true);
                reload();
                return true;
            case R.id.sort_lastupdate:
                Prefs.sortBy("s1");
                item.setChecked(true);
                reload();
                return true;
            case R.id.sort_count:
                Prefs.sortBy("s2");
                item.setChecked(true);
                reload();
                return true;
            case R.id.sort_size:
                Prefs.sortBy("s3");
                item.setChecked(true);
                reload();
                return true;
            case R.id.sort_percent:
                Prefs.sortBy("s4");
                item.setChecked(true);
                reload();
                return true;
            case R.id.author_name:
                Prefs.sortBy("s5");
                item.setChecked(true);
                reload();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showFav() {
        Intent myIntent = new Intent(MainActivity.this, FavoriteAcitvity.class);
        startActivity(myIntent);
    }

    private void showIconSearch() {
        Intent myIntent = new Intent(MainActivity.this, IconSearchActivity.class);
        startActivity(myIntent);
    }

    private void reload() {
        PackageComparator packageComparator = new PackageComparator(MainActivity.this); // Pass the activity context here
        Collections.sort(iconPacksList, packageComparator);
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

        PackageComparator packageComparator = new PackageComparator(MainActivity.this); // Pass the activity context here
        Collections.sort(new ArrayList<>(filteredModelList), packageComparator);


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


    /*private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.WHITE);
                xMark = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_clear_24dp);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) MainActivity.this.getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                IconAdapter testAdapter = (IconAdapter) recyclerView.getAdapter();
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                IconAdapter adapter = (IconAdapter) recyclerView.getAdapter();
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                if (viewHolder.getAdapterPosition() == -1) {
                    return;
                }
                if (!initiated) {
                    init();
                }
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();
                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                xMark.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }*/

    public class LoadAppList extends AsyncTask<Void, Integer, List<IPObj>> {

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
        protected List<IPObj> doInBackground(Void... params) {
            try {
                List<IPObj> loadedPacks = new IconPackManager().updateIconPacks(ipObjDao, forceLoad, plsWait);
                installed = Util.getInstalledApps().size();
                if (isCancelled())
                    return null;
                return loadedPacks;
            } catch (SQLiteException sqe) {
                sqe.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(List<IPObj> result) {
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
                    plsWait = null;
                }
                mSwipeLayout.setRefreshing(false);
                // Populate iconPacksList from the result
                if (result != null && !result.isEmpty()) {
                    iconPacksList.clear();
                    iconPacksSet.clear();
                    iconPacksList.addAll(result);
                    // Populate the HashSet for fast lookups
                    for (IPObj pack : result) {
                        if (pack != null && pack.getIconPkg() != null) {
                            iconPacksSet.add(pack.getIconPkg());
                        }
                    }
                }
                if (iconPacksList != null && !iconPacksList.isEmpty()) {
                    setTitle(getString(R.string.app_name) + " - #" + iconPacksList.size());
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    Collections.sort(iconPacksList, new PackageComparator(MainActivity.this));
                    final Gson gson = new Gson();
                    if (Prefs.useFavorite()) {
                        Collections.sort(iconPacksList, (o1, o2) -> {
                            IconAttr attr1 = gson.fromJson(o1.getAdditional(), IconAttr.class);
                            IconAttr attr2 = gson.fromJson(o2.getAdditional(), IconAttr.class);
                            return Boolean.compare(attr2.isFavorite(), attr1.isFavorite());
                        });
                    }
                    adapter = new IconAdapter(iconPacksList, installed);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();


                    /*

                    new SwipeHelper(getApplicationContext(), recyclerView) {
                        @Override
                        public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                            underlayButtons.add(new SwipeHelper.UnderlayButton(
                                    "Apply",
                                    0,
                                    Color.parseColor("#FF3C30"),
                                    pos -> {
                                    }
                            ));

                            underlayButtons.add(new SwipeHelper.UnderlayButton(
                                    "Details",
                                    0,
                                    Color.parseColor("#FF9502"),
                                    pos -> {
                                    }
                            ));
                        }
                    };
                     */
                    List<ShortcutInfo> list = new ArrayList<>();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);


                        Intent intent;

                        intent = new Intent(getApplicationContext(), RandomActivity.class);
                        intent.setAction("dev.ukanth.iconmgr.shortcut.RANDOM");
                        intent.putExtra("pack", "");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        ShortcutInfo shortcut = new ShortcutInfo.Builder(getApplicationContext(), "card")
                                .setShortLabel("Random")
                                .setLongLabel("Apply Random Iconpack")
                                .setIntent(intent)
                                .build();
                        list.add(shortcut);

                        for (IPObj pack : iconPacksList) {
                            if (pack != null && pack.getIconPkg() != null && gson.fromJson(pack.getAdditional(), IconAttr.class).isFavorite()) {
                                intent = new Intent(getApplicationContext(), RandomActivity.class);
                                intent.setAction("dev.ukanth.iconmgr.shortcut.RANDOM");
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("pack", pack.getIconPkg());
                                shortcut = new ShortcutInfo.Builder(getApplicationContext(), pack.getIconPkg())
                                        .setShortLabel(pack.getIconName())
                                        .setLongLabel(pack.getIconName())
                                        .setIntent(intent)
                                        .build();
                                list.add(shortcut);

                            }
                        }
                        shortcutManager.setDynamicShortcuts(list);
                    }
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
