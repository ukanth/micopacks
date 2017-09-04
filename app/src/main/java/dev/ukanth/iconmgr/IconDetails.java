package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.Util;

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

    private String type = "ALL";

    public IconDetails(Context context, String packageName, AsyncResponse response, String type) {
        this.delegate = response;
        this.mContext = context;
        this.packageName = packageName;
        this.type = type;
    }

    public AsyncTask process(@NonNull Context context, String packageName, AsyncResponse response, String type) {
        return process(context, packageName, SERIAL_EXECUTOR, response, type);
    }

    public static AsyncTask process(@NonNull Context context, String packageName, @NonNull Executor executor, AsyncResponse response, String type) {
        return new IconDetails(context, packageName, response, type).executeOnExecutor(executor);
    }

    @Override
    protected HashMap<String, List> doInBackground(Object... voids) {
        while (!isCancelled()) {
            try {
                IconPackUtil ip = new IconPackUtil();
                HashMap<String, List> returnData = new HashMap<>();
                switch (type) {
                    case "MISSED":
                        List<ResolveInfo> listPackages = Util.getInstalledApps(mContext);
                        List<String> missPackage = ip.getMissingApps(mContext, packageName, listPackages);
                        returnData.put("package", missPackage);
                        returnData.put("install", listPackages);
                        break;
                    case "BITMAP":
                        Set<Icon> icons = ip.getIcons(mContext, packageName);
                        List<Icon> listBitMap = new ArrayList<Icon>(icons);
                        returnData.put("bitmap", listBitMap);
                        break;
                    case "INSTALL":
                        listPackages = Util.getInstalledApps(mContext);
                        returnData.put("install", listPackages);
                        break;
                    default:
                        listPackages = Util.getInstalledApps(mContext);
                        missPackage = ip.getMissingApps(mContext, packageName, listPackages);
                        returnData.put("package", missPackage);
                        returnData.put("install", listPackages);

                        icons = ip.getIcons(mContext, packageName);
                        listBitMap = new ArrayList<Icon>(icons);
                        returnData.put("bitmap", listBitMap);

                }
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
            if((type.equals("MISSED") || type.equals("ALL")) && listPkg != null) {
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