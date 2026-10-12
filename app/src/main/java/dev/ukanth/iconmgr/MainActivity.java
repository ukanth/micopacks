package dev.ukanth.iconmgr;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;

import dev.ukanth.iconmgr.home.HistoryFragment;
import dev.ukanth.iconmgr.home.IconsFragment;
import dev.ukanth.iconmgr.home.PacksFragment;
import dev.ukanth.iconmgr.home.SavedFragment;
import dev.ukanth.iconmgr.util.WindowInsetsHelper;

/**
 * Home host: bottom navigation between Packs, Icons, Saved and History. Tabs are kept alive
 * (show/hide) so switching keeps their scroll position and search.
 */
public class MainActivity extends AppCompatActivity {
    private static final String STATE_TAB = "tab";
    private static boolean reloadTheme = false;
    private static boolean reloadApp = false;

    private BottomNavigationView bottomNav;
    private int currentTab = R.id.nav_packs;

    public static void setReloadTheme(boolean reloadTheme) {
        MainActivity.reloadTheme = reloadTheme;
    }

    public static void setReloadApp(boolean b) {
        MainActivity.reloadApp = b;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowInsetsHelper.applyToContent(this);

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt(STATE_TAB, R.id.nav_packs);
        }
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });
        bottomNav.setOnItemReselectedListener(item -> {
            // nothing: keep the tab as it is
        });
        bottomNav.setSelectedItemId(currentTab);
        showTab(currentTab);

        if (savedInstanceState == null) {
            new ViewModelProvider(this).get(MainViewModel.class).refresh(false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, currentTab);
    }

    private void showTab(int tabId) {
        currentTab = tabId;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        String tag = tagFor(tabId);
        for (int id : new int[]{R.id.nav_packs, R.id.nav_icons, R.id.nav_saved, R.id.nav_history}) {
            Fragment existing = fm.findFragmentByTag(tagFor(id));
            if (existing != null && id != tabId) {
                tx.hide(existing);
            }
        }
        Fragment target = fm.findFragmentByTag(tag);
        if (target == null) {
            tx.add(R.id.tab_container, newTab(tabId), tag);
        } else {
            tx.show(target);
        }
        tx.setReorderingAllowed(true).commitNow();
        updateTitle(tabId);
    }

    private static String tagFor(int tabId) {
        return "tab_" + tabId;
    }

    private static Fragment newTab(int tabId) {
        if (tabId == R.id.nav_icons) return new IconsFragment();
        if (tabId == R.id.nav_saved) return new SavedFragment();
        if (tabId == R.id.nav_history) return new HistoryFragment();
        return new PacksFragment();
    }

    private void updateTitle(int tabId) {
        ActionBar bar = getSupportActionBar();
        if (bar == null || tabId == R.id.nav_packs) {
            return; // the Packs tab sets its own title and pack count
        }
        int title = tabId == R.id.nav_icons ? R.string.nav_icons : tabId == R.id.nav_saved ? R.string.nav_saved : R.string.nav_history;
        bar.setTitle(title);
        bar.setSubtitle(null);
    }

    @Override
    public void onBackPressed() {
        // back from another tab returns to Packs before leaving the app
        if (currentTab != R.id.nav_packs) {
            bottomNav.setSelectedItemId(R.id.nav_packs);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (reloadTheme) {
            reloadTheme = false;
            recreate();
        }
        if (reloadApp) {
            reloadApp = false;
            recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_host, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.pref) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.help) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.about_help)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return true;
        } else if (id == R.id.report) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            String version = "";
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
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
        }
        return super.onOptionsItemSelected(item);
    }

    /** "Share" from the Packs tab: renders the list into one image and shares it. */
    public void shareListScreenshot(RecyclerView list) {
        Toast.makeText(getApplicationContext(), getString(R.string.generating), Toast.LENGTH_LONG).show();
        new Thread(() -> shareBitmap(getScreenshotFromRecyclerView(list), System.currentTimeMillis() + "")).start();
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
}
