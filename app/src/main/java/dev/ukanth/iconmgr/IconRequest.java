package dev.ukanth.iconmgr;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.Executor;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;

/**
 * Created by ukanth on 14/8/17.
 */


public class IconRequest extends AsyncTask<Void, Void, List<String>> {

    public interface AsyncResponse {
        void processFinish(List<String> output);
    }

    public AsyncResponse delegate = null;

    private Context mContext;
    private String packageName;

    public IconRequest() {

    }

    public IconRequest(Context context, String packageName, AsyncResponse response) {
        this.delegate = response;
        this.mContext = context;
        this.packageName = packageName;
    }

    public AsyncTask start(@NonNull Context context, String packageName, AsyncResponse response) {
        return start(context, packageName, SERIAL_EXECUTOR, response);
    }

    public static AsyncTask start(@NonNull Context context, String packageName, @NonNull Executor executor, AsyncResponse response) {
        return new IconRequest(context, packageName, response).executeOnExecutor(executor);
    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        while (!isCancelled()) {
            try {
                IconPackUtil ip = new IconPackUtil();
                List<String> missPackage = ip.getMissingApps(mContext, packageName);
                return missPackage;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<String> listPkg) {
        super.onPostExecute(listPkg);
        App app = ((App) mContext.getApplicationContext());
        DaoSession daoSession = app.getDaoSession();
        IPObjDao ipObjDao = daoSession.getIPObjDao();
        IPObj ipobj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(packageName)).build().uniqueOrThrow();
        ipobj.setMissed(listPkg.size());
        ipObjDao.update(ipobj);
        if(delegate != null) {
            delegate.processFinish(listPkg);
        }
        //do something with the count and package.
    }

    private List<String> returnData(List<String> listPkg) {
        //handle value
        return listPkg;
    }
}