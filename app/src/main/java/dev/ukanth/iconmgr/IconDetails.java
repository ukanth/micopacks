package dev.ukanth.iconmgr;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;

/**
 * Created by ukanth on 14/8/17.
 */


public class IconDetails extends AsyncTask<Object, Object, HashMap<String, List>> {

    public interface AsyncResponse {
        void processFinish(HashMap<String, List> output);
    }

    public AsyncResponse delegate = null;

    private Context mContext;
    private String packageName;

    private boolean includeMiss = true;

    public IconDetails(Context context, String packageName, AsyncResponse response, boolean onlyBitmap) {
        this.delegate = response;
        this.mContext = context;
        this.packageName = packageName;
        this.includeMiss = onlyBitmap;
    }

    public AsyncTask process(@NonNull Context context, String packageName, AsyncResponse response, boolean includeMiss) {
        return process(context, packageName, SERIAL_EXECUTOR, response, includeMiss);
    }

    public static AsyncTask process(@NonNull Context context, String packageName, @NonNull Executor executor, AsyncResponse response, boolean includeMiss) {
        return new IconDetails(context, packageName, response, includeMiss).executeOnExecutor(executor);
    }

    @Override
    protected HashMap<String, List> doInBackground(Object... voids) {
        while (!isCancelled()) {
            try {
                IconPackUtil ip = new IconPackUtil();

                HashMap<String, List> returnData = new HashMap<>();
                if(includeMiss) {
                    List<String> missPackage = ip.getMissingApps(mContext, packageName);
                    returnData.put("package", missPackage);
                }

                List<Bitmap> listBitMap = ip.getIcons(mContext, packageName);
                returnData.put("bitmap", listBitMap);

                return returnData;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(HashMap<String, List> listPkg) {
        super.onPostExecute(listPkg);
        if (packageName != null) {
            Log.i("MICO", "PackageName: " + packageName);
            if(includeMiss) {
                App app = ((App) mContext.getApplicationContext());
                DaoSession daoSession = app.getDaoSession();
                IPObjDao ipObjDao = daoSession.getIPObjDao();
                IPObj ipObj = new IPObj();
                ipObj.setIconPkg(packageName);
                if (ipObjDao != null && ipObjDao.hasKey(ipObj)) {
                    Log.i("MICO", "Exist in DB: " + packageName);
                    IPObj ipobj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(packageName)).build().unique();
                    List<String> missedPackage = listPkg.get("package");
                    ipobj.setMissed(missedPackage != null ? missedPackage.size() : 0);
                    ipObjDao.update(ipobj);
                }
            }
            if (delegate != null) {
                delegate.processFinish(listPkg);
            }
        }
    }

   /* private List<String> returnData(List<String> listPkg) {
        //handle value
        return listPkg;
    }*/
}