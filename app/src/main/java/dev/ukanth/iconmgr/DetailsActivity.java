package dev.ukanth.iconmgr;

import android.app.NotificationManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.dao.IPObjDatabase;
import dev.ukanth.iconmgr.util.LauncherHelper;
import dev.ukanth.iconmgr.util.Util;


public class DetailsActivity extends AppCompatActivity {

    private GridLayoutManager gridLayoutManager;

    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Prefs.isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        setupActionBar();

        Bundle bundle = getIntent().getExtras();
        final String pkgName = bundle.getString("pkg");


        if (Prefs.isNotify()) {
            NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationmanager.cancel(Util.hash(pkgName));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_details);
        recyclerView.setHasFixedSize(true);

        gridLayoutManager = new GridLayoutManager(getApplicationContext(),2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //define span size for this position
                //some example for your first three items
                if(position == 1) {
                    return 1; //item will take 1/3 space of row
                } else if(position == 2) {
                    return 1; //you will have 2/3 space of row
                } else if(position == 3) {
                    return 2; //you will have full row size item
                } else {
                    return 2;
                }
            }
        });

        recyclerView.setLayoutManager(gridLayoutManager);

        List<Detail> homes = new ArrayList<>();


        IPObjDao ipObjDao = App.getInstance().getIPObjDatabase().ipObjDao();

        IPObj pkgObj = null;
        if (ipObjDao != null) {
            pkgObj = ipObjDao.getByIconPkg(pkgName);
            if(pkgObj != null) {
                homes.add(new Detail(-1, String.valueOf(pkgObj.getTotal()),
                        getResources().getString(R.string.iconCount),
                        Detail.Type.TOTAL));
                setTitle(pkgObj.getIconName());
            }
        }

        fab = (FloatingActionButton) findViewById(R.id.fabdetail);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String launcherPack = LauncherHelper.getLauncherPackage(getApplicationContext());
                LauncherHelper.apply(DetailsActivity.this, pkgName, launcherPack);
            }
        });
        if (!Prefs.isFabShow()) {
            fab.setVisibility(View.GONE);
        }

        DetailViewAdapter rcAdapter = new DetailViewAdapter(DetailsActivity.this, homes, 1, pkgObj);
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
