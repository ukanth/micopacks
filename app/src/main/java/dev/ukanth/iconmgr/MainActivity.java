package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ukanth.iconmgr.util.PackageComparator;
import dev.ukanth.iconmgr.util.Util;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private TextView emptyView;

    private IconAdapter adapter;
    private List<IconPack> iconPacksList;

    private SwipeRefreshLayout mSwipeLayout;

    private MaterialDialog plsWait;

    private Menu mainMenu;


    public static void setReloadTheme(boolean reloadTheme) {
        MainActivity.reloadTheme = reloadTheme;
    }

    public static void setReloadApp(boolean b) {
        MainActivity.reloadApp = b;
    }

    private static boolean reloadTheme = false;
    private static boolean reloadApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Prefs.isDarkTheme(getApplicationContext())) {
            setTheme(R.style.AppTheme_Dark);
        }

        setContentView(R.layout.content_main);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.iconpack);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        emptyView = (TextView) findViewById(R.id.empty_view);


        iconPacksList = new ArrayList<>();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);

        loadApp();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(reloadTheme) {
            reloadTheme = false;
            restartActivity();
        }
        if(reloadApp) {
            reloadApp = false;
            restartActivity();
        }
    }

    private void restartActivity() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void loadApp() {
        LoadAppList getAppList = new LoadAppList();
        if (plsWait == null && (getAppList.getStatus() == AsyncTask.Status.PENDING ||
                getAppList.getStatus() == AsyncTask.Status.FINISHED)) {
            getAppList.setContext(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onRefresh() {
        loadApp();
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
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"uzoftinc@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Report Issue: " + getString(R.string.app_name) + " " + version);
                i.putExtra(Intent.EXTRA_TEXT, "");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            case R.id.sort_count:
                Prefs.sortBy(getApplicationContext(), "s2");
                item.setChecked(true);
                loadApp();
                return true;
            case R.id.sort_alpha:
                Prefs.sortBy(getApplicationContext(), "s0");
                item.setChecked(true);
                loadApp();
                return true;
            case R.id.sort_lastupdate:
                Prefs.sortBy(getApplicationContext(), "s1");
                item.setChecked(true);
                loadApp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public class LoadAppList extends AsyncTask<Void, Integer, Void> {

        Context context = null;

        public LoadAppList setContext(Context context) {
            this.context = context;
            return this;
        }

        @Override
        protected void onPreExecute() {
            plsWait = new MaterialDialog.Builder(context).cancelable(false).
                    title(context.getString(R.string.loading)).progress(false, context.getPackageManager().getInstalledApplications(0)
                    .size(), true).show();
            doProgress(0);
        }

        public void doProgress(int value) {
            publishProgress(value);
        }

        @Override
        protected Void doInBackground(Void... params) {
            iconPacksList = Util.getListOfPacks(getApplicationContext());
            if (isCancelled())
                return null;
            //publishProgress(-1);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            doProgress(-1);
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
                //mSwipeLayout.setRefreshing(false);
                Collections.sort(iconPacksList, new PackageComparator().setCtx(getApplicationContext()));
                adapter = new IconAdapter(MainActivity.this, iconPacksList);
                recyclerView.setAdapter(adapter);

                if (iconPacksList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                // nothing
                if (plsWait != null) {
                    plsWait.dismiss();
                    plsWait = null;
                }
                mSwipeLayout.setRefreshing(false);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

            if (progress[0] == 0 || progress[0] == -1) {
                //do nothing
            } else {
                if (plsWait != null) {
                    plsWait.incrementProgress(progress[0]);
                }
            }
        }
    }
}
