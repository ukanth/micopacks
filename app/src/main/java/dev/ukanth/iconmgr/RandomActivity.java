package dev.ukanth.iconmgr;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.Util;

public class RandomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        IPObjDao ipObjDao = daoSession.getIPObjDao();
        IPObj ipObj = null;
        String pkgName = getIntent().getStringExtra("pack");
        if(pkgName.isEmpty()) {
            ipObj = Util.getRandomInstalledIconPack(ipObjDao);
        } else {
            ipObj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(pkgName)).unique();
        }
        Util.determineApply(getApplicationContext(), ipObj);
        finish();
    }
}
