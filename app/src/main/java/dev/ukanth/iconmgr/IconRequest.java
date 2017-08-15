package dev.ukanth.iconmgr;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.greendao.query.Query;

import java.util.List;
import java.util.concurrent.Executor;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;

/**
 * Created by ukanth on 14/8/17.
 */


public class IconRequest extends AsyncTask<Void, Void, Integer> {

    private Context mContext;
    private String packageName;

    private IconRequest(Context context, String packageName) {
        this.mContext = context;
        this.packageName = packageName;
    }

    public static AsyncTask start(@NonNull Context context, String packageName) {
        return start(context, packageName, SERIAL_EXECUTOR);
    }

    public static AsyncTask start(@NonNull Context context, String packageName, @NonNull Executor executor) {
        return new IconRequest(context, packageName).executeOnExecutor(executor);
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        while (!isCancelled()) {
            try {
                IconPackUtil ip = new IconPackUtil();
                List<String> missPackage = ip.getMissingApps(mContext, packageName);
                return missPackage.size();
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer count) {
        super.onPostExecute(count);
        App app = ((App) mContext.getApplicationContext());
        DaoSession daoSession = app.getDaoSession();
        IPObjDao ipObjDao = daoSession.getIPObjDao();
        IPObj ipobj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(packageName)).build().uniqueOrThrow();
        ipobj.setMissed(count);
        ipObjDao.update(ipobj);
        //do something with the count and package.
    }
}