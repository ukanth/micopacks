package dev.ukanth.iconmgr;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.dao.IPObjDatabase;
import dev.ukanth.iconmgr.util.Util;

public class RandomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IPObjDao ipObjDao = App.getInstance().getIPObjDatabase().ipObjDao();
        IPObj ipObj = null;
        String pkgName = getIntent().getStringExtra("pack");
        if(pkgName.isEmpty()) {
            ipObj = Util.getRandomInstalledIconPack(ipObjDao);
        } else {
            ipObj = ipObjDao.getByIconPkg(pkgName);
        }
        Util.determineApply(RandomActivity.this, ipObj);
        finish();
    }
}
