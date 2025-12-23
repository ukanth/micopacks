package dev.ukanth.iconmgr;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.Util;

public class RandomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String pkgName = getIntent().getStringExtra("pack");

        // Run database operations on background thread
        new Thread(() -> {
            IPObjDao ipObjDao = App.getInstance().getIPObjDao();
            IPObj ipObj;
            if (pkgName == null || pkgName.isEmpty()) {
                ipObj = Util.getRandomInstalledIconPack(ipObjDao);
            } else {
                ipObj = ipObjDao.getByIconPkg(pkgName);
            }
            final IPObj finalIpObj = ipObj;
            runOnUiThread(() -> {
                Util.determineApply(RandomActivity.this, finalIpObj);
                finish();
            });
        }).start();
    }
}
