package dev.ukanth.iconmgr;

import android.app.NotificationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.LauncherHelper;
import dev.ukanth.iconmgr.util.Util;


public class DetailsActivity extends AppCompatActivity {

    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Prefs.isDarkTheme(getApplicationContext())) {
            setTheme(R.style.AppTheme_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        setupActionBar();

        Bundle bundle = getIntent().getExtras();
        final String pkgName = bundle.getString("pkg");


        if (Prefs.isNotify(getApplicationContext())) {
            NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationmanager.cancel(Util.hash(pkgName));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_details);
        recyclerView.setHasFixedSize(true);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        List<Detail> homes = new ArrayList<>();

        App app = ((App) getApplicationContext());
        DaoSession daoSession = app.getDaoSession();
        IPObjDao ipObjDao = daoSession.getIPObjDao();
        IPObj pkgObj = null;
        if (ipObjDao != null) {
            pkgObj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(pkgName)).unique();

            homes.add(new Detail(-1, String.valueOf(pkgObj.getTotal()),
                    getResources().getString(R.string.iconCount),
                    Detail.Type.TOTAL));
            setTitle(pkgObj.getIconName());
        }

        fab = (FloatingActionButton) findViewById(R.id.fabdetail);
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

        DetailViewAdapter rcAdapter = new DetailViewAdapter(DetailsActivity.this, getApplicationContext(), homes, 1, pkgObj);
        recyclerView.setAdapter(rcAdapter);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
