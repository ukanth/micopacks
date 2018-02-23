package dev.ukanth.iconmgr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.LauncherHelper;
import dev.ukanth.iconmgr.util.Util;

public class RandomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        IPObjDao ipObjDao = daoSession.getIPObjDao();
        String currentLauncher = Util.getCurrentLauncher(getApplicationContext());
        IPObj ipObj = null;
        String pkgName = getIntent().getStringExtra("pack");
        if(pkgName.isEmpty()) {
            ipObj = Util.getRandomInstalledIconPack(ipObjDao);
        } else {
            ipObj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(pkgName)).unique();
        }
        if (currentLauncher != null) {
            if (ipObj != null) {
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.selected_pack) + ipObj.getIconName(), Toast.LENGTH_SHORT).show();
                LauncherHelper.apply(RandomActivity.this, ipObj.getIconPkg(), currentLauncher);
            } else {
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.unable_iconpack), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.nodefault), Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
