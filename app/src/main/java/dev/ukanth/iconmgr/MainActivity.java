package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.ukanth.iconmgr.util.Util;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView emptyView;

    private IconAdapter adapter;
    private List<IconPack> iconPacksList;

    private MaterialDialog plsWait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        emptyView = (TextView) findViewById(R.id.empty_view);


        iconPacksList = new ArrayList<>();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);


        LoadAppList getAppList = new LoadAppList();
        if (plsWait == null && (getAppList.getStatus() == AsyncTask.Status.PENDING ||
                getAppList.getStatus() == AsyncTask.Status.FINISHED)) {
            getAppList.setContext(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.help:
                //Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                //startActivity(myIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
                adapter = new IconAdapter(MainActivity.this, iconPacksList);
                recyclerView.setAdapter(adapter);

                if(iconPacksList.isEmpty()) {
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
