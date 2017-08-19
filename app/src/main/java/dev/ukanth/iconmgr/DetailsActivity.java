package dev.ukanth.iconmgr;

import android.app.NotificationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.Util;


public class DetailsActivity extends AppCompatActivity {

    private StaggeredGridLayoutManager staggeredGridLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Prefs.isDarkTheme(getApplicationContext())) {
            setTheme(R.style.AppTheme_Dark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        setupActionBar();


        if (Prefs.isNotify(getApplicationContext())) {
            // Create Notification Manager
            NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Dismiss Notification
            notificationmanager.cancel(90297);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_details);
        recyclerView.setHasFixedSize(true);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        Bundle bundle = getIntent().getExtras();
        String pkgName = bundle.getString("pkg");

        List<Detail> homes = new ArrayList<>();

        IPObjDao ipObjDao = Util.getDAO(getApplicationContext());
        IPObj pkgObj = null;
        if (ipObjDao != null) {
            pkgObj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(pkgName)).unique();

            homes.add(new Detail(-1, String.valueOf(pkgObj.getTotal()),
                    getResources().getString(R.string.iconCount),
                    Detail.Type.ICONS));
            setTitle(pkgObj.getIconName());
        }
        DetailViewAdapter rcAdapter = new DetailViewAdapter(getApplicationContext(), homes, 1, pkgObj);
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
