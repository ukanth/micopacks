package dev.ukanth.iconmgr.detail;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.IconPackUtil;
import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.iconpack.InstalledApps;
import dev.ukanth.iconmgr.util.LauncherHelper;
import dev.ukanth.iconmgr.util.Util;
import dev.ukanth.iconmgr.util.WindowInsetsHelper;

/**
 * One icon pack: summary, tabs (your apps / missing / all icons / masks) over a lazily loaded
 * grid, and a sticky Apply button. Replaces the old Preview and Details screens.
 */
public class PackDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PACKAGE = "pkg";
    private static final String STATE_TAB = "tab";

    private PackDetailViewModel viewModel;
    private IconGridAdapter adapter;
    private TabLayout tabs;
    private RecyclerView grid;
    private CircularProgressIndicator progress;
    private TextView empty;
    private MaterialButton applyButton;

    private Map<PackDetailViewModel.Tab, List<IconItem>> lists;
    private PackDetailViewModel.Tab currentTab = PackDetailViewModel.Tab.YOUR_APPS;
    private String query = "";
    private IPObj pack;
    private int installed;

    public static Intent intent(android.content.Context context, String packPackage) {
        return new Intent(context, PackDetailActivity.class).putExtra(EXTRA_PACKAGE, packPackage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pack_detail);
        WindowInsetsHelper.applyToContent(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // the card shadow under the action bar would double the AppBarLayout's lift
            getSupportActionBar().setElevation(0);
        }

        String packageName = getIntent().getStringExtra(EXTRA_PACKAGE);
        if (packageName == null) {
            finish();
            return;
        }
        if (getIntent().hasExtra(Util.EXTRA_NOTIFICATION_ID)) {
            NotificationManagerCompat.from(this).cancel(getIntent().getIntExtra(Util.EXTRA_NOTIFICATION_ID, 0));
        }
        if (savedInstanceState != null) {
            currentTab = PackDetailViewModel.Tab.valueOf(savedInstanceState.getString(STATE_TAB, currentTab.name()));
        }

        tabs = findViewById(R.id.tabs);
        grid = findViewById(R.id.grid);
        progress = findViewById(R.id.progress);
        empty = findViewById(R.id.empty);
        applyButton = findViewById(R.id.apply);

        boolean landscape = getResources().getConfiguration().orientation
                == android.content.res.Configuration.ORIENTATION_LANDSCAPE;
        int columns = Math.max(3, Prefs.getCol());
        if (landscape) {
            // header + Apply bar would leave no room for the grid; drop the stat boxes, widen the grid
            findViewById(R.id.stats_row).setVisibility(View.GONE);
            columns = Math.round(columns * 1.8f);
        }
        grid.setLayoutManager(new GridLayoutManager(this, columns));
        grid.setHasFixedSize(true);
        adapter = new IconGridAdapter(packageName, Prefs.isAutoThemingEnabled(), this::onIconClick);
        grid.setAdapter(adapter);

        setupTabs();
        setupApplyButton();

        viewModel = new ViewModelProvider(this).get(PackDetailViewModel.class);
        viewModel.init(packageName);
        viewModel.getPack().observe(this, this::bindPack);
        viewModel.getInstalledCount().observe(this, count -> {
            installed = count == null ? 0 : count;
            bindPack(pack);
        });
        viewModel.getLists().observe(this, loaded -> {
            lists = loaded;
            updateTabTitles();
            showCurrentTab(false);
        });
        bindPackIcon(packageName);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_TAB, currentTab.name());
    }

    private void setupTabs() {
        addTab(PackDetailViewModel.Tab.YOUR_APPS, R.string.tab_your_apps_plain);
        addTab(PackDetailViewModel.Tab.MISSING, R.string.tab_missing_plain);
        addTab(PackDetailViewModel.Tab.ALL, R.string.tab_all_icons_plain);
        addTab(PackDetailViewModel.Tab.MASKS, R.string.tab_masks_plain);
        TabLayout.Tab initial = findTab(currentTab);
        if (initial != null) {
            initial.select();
        }
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = (PackDetailViewModel.Tab) tab.getTag();
                showCurrentTab(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                grid.smoothScrollToPosition(0);
            }
        });
    }

    private void addTab(PackDetailViewModel.Tab which, int title) {
        tabs.addTab(tabs.newTab().setText(title).setTag(which), false);
    }

    private TabLayout.Tab findTab(PackDetailViewModel.Tab which) {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            TabLayout.Tab tab = tabs.getTabAt(i);
            if (tab != null && tab.getTag() == which) {
                return tab;
            }
        }
        return null;
    }

    private void updateTabTitles() {
        setTabTitle(PackDetailViewModel.Tab.YOUR_APPS, R.string.tab_your_apps);
        setTabTitle(PackDetailViewModel.Tab.MISSING, R.string.tab_missing);
        setTabTitle(PackDetailViewModel.Tab.ALL, R.string.tab_all_icons);
        List<IconItem> masks = lists.get(PackDetailViewModel.Tab.MASKS);
        TabLayout.Tab masksTab = findTab(PackDetailViewModel.Tab.MASKS);
        if (masksTab != null && (masks == null || masks.isEmpty())) {
            // most packs have no masks; don't show an empty tab
            if (currentTab == PackDetailViewModel.Tab.MASKS) {
                TabLayout.Tab first = findTab(PackDetailViewModel.Tab.YOUR_APPS);
                if (first != null) first.select();
            }
            tabs.removeTab(masksTab);
        } else {
            setTabTitle(PackDetailViewModel.Tab.MASKS, R.string.tab_masks);
        }
    }

    private void setTabTitle(PackDetailViewModel.Tab which, int format) {
        TabLayout.Tab tab = findTab(which);
        List<IconItem> items = lists.get(which);
        if (tab != null && items != null) {
            tab.setText(getString(format, items.size()));
        }
    }

    private void showCurrentTab(boolean scrollToTop) {
        if (lists == null) {
            progress.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
            return;
        }
        progress.setVisibility(View.GONE);
        List<IconItem> items = PackDetailViewModel.filter(lists.get(currentTab), query);
        adapter.submitList(items, () -> {
            if (scrollToTop) {
                grid.scrollToPosition(0);
            }
        });
        if (items.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            if (!query.trim().isEmpty()) {
                empty.setText(getString(R.string.empty_search, query.trim()));
            } else if (currentTab == PackDetailViewModel.Tab.MISSING) {
                empty.setText(R.string.empty_missing);
            } else if (currentTab == PackDetailViewModel.Tab.YOUR_APPS) {
                empty.setText(R.string.empty_your_apps);
            } else {
                empty.setText(R.string.empty_all_icons);
            }
        } else {
            empty.setVisibility(View.GONE);
        }
    }

    private void bindPackIcon(String packageName) {
        try {
            ((ImageView) findViewById(R.id.pack_icon)).setImageDrawable(getPackageManager().getApplicationIcon(packageName));
        } catch (PackageManager.NameNotFoundException e) {
            finish(); // pack was uninstalled
        }
    }

    private void bindPack(IPObj pack) {
        if (pack == null) {
            return;
        }
        this.pack = pack;
        setTitle(pack.getIconName());

        TextView author = findViewById(R.id.pack_author);
        author.setVisibility(pack.getAuthor() != null ? View.VISIBLE : View.GONE);
        author.setText(getString(R.string.pack_author, pack.getAuthor()));
        CharSequence updated = DateUtils.getRelativeTimeSpanString(pack.getInstallTime(),
                System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS);
        ((TextView) findViewById(R.id.pack_meta)).setText(getString(R.string.pack_updated, updated) + " · " + pack.getIconPkg());

        setStat(R.id.stat_icons, NumberFormat.getIntegerInstance().format(pack.getTotal()), getString(R.string.stat_icons));
        setStat(R.id.stat_coverage, installed > 0 ? InstalledApps.coveragePercent(installed, pack.getMissed()) + "%" : "–",
                getString(R.string.stat_coverage, installed));
        setStat(R.id.stat_size, pack.getSizeBytes() > 0 ? Formatter.formatShortFileSize(this, pack.getSizeBytes()) : "–",
                getString(R.string.stat_size));
    }

    private void setStat(int id, String value, String label) {
        View stat = findViewById(id);
        ((TextView) stat.findViewById(R.id.stat_value)).setText(value);
        ((TextView) stat.findViewById(R.id.stat_label)).setText(label);
    }

    private void setupApplyButton() {
        String launcher = LauncherHelper.getLauncherPackage(this);
        if (launcher != null && !"android".equals(launcher) && !Prefs.usePrompt()) {
            applyButton.setText(getString(R.string.apply_to, LauncherHelper.getLauncherName(this, launcher)));
        }
        applyButton.setOnClickListener(v -> {
            if (pack != null) {
                Util.determineApply(this, pack);
            }
        });
    }

    private void onIconClick(IconItem item, Bitmap bitmap) {
        String packName = pack != null ? pack.getIconName() : viewModel.getPackageName();
        if (item.kind == IconItem.Kind.MISSING_APP) {
            String subtitle = getString(Prefs.isAutoThemingEnabled() ? R.string.missing_subtitle_preview
                    : R.string.missing_subtitle, packName);
            IconSheet.show(this, bitmap, item.title, subtitle, viewModel.getPackageName(), packName, null);
        } else {
            // "Your apps" shows the app label; the drawable name is useful for icon requests
            String subtitle = item.title.equals(item.drawable) ? packName : item.drawable + " · " + packName;
            String pkg = viewModel.getPackageName();
            // the grid holds a cell-sized thumbnail; show and save the full-size icon
            App.getInstance().getBackgroundExecutor().execute(() -> {
                Bitmap full = new IconPackUtil().loadIcon(pkg, item.drawable);
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        IconSheet.show(this, full != null ? full : bitmap, item.title, subtitle, pkg, packName, item.drawable);
                    }
                });
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pack_detail, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.detail_search).getActionView();
        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.search_icons));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String text) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String text) {
                    query = text;
                    showCurrentTab(true);
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.detail_open) {
            Intent launch = getPackageManager().getLaunchIntentForPackage(viewModel.getPackageName());
            if (launch != null) {
                startActivity(launch);
            }
            return true;
        } else if (id == R.id.detail_playstore) {
            String pkg = viewModel.getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
