package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;


import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.dao.IPObjDatabase;
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

    public IconDetails(String packageName, AsyncResponse response, String type) {
        this.delegate = response;
        this.mContext = App.getContext();
        this.packageName = packageName;
        this.type = type;
    }

    public AsyncTask process(@NonNull String packageName, AsyncResponse response, String type) {
        return process(packageName, SERIAL_EXECUTOR, response, type);
    }

    public static AsyncTask process(@NonNull String packageName, @NonNull Executor executor, AsyncResponse response, String type) {
        return new IconDetails(packageName, response, type).executeOnExecutor(executor);
    }

    @Override
    protected HashMap<String, List> doInBackground(Object... voids) {
        while (!isCancelled()) {
            try {
                IconPackUtil ip = new IconPackUtil();
                HashMap<String, List> returnData = new HashMap<>();
                switch (type) {
                    case "MISSED":
                        List<ResolveInfo> listPackages = Util.getInstalledApps();
                        List<String> missPackage = ip.getMissingApps(packageName, listPackages);
                        returnData.put("package", missPackage);
                        returnData.put("install", listPackages);
                        break;
                    case "BITMAP":
                        Set<Icon> icons = ip.getIcons( packageName);
                        List<Icon> listBitMap = new ArrayList<Icon>(icons);
                        returnData.put("bitmap", listBitMap);
                        break;
                    case "INSTALL":
                        listPackages = Util.getInstalledApps();
                        returnData.put("install", listPackages);
                        break;
                    default:
                        listPackages = Util.getInstalledApps();
                        missPackage = ip.getMissingApps(packageName, listPackages);
                        returnData.put("package", missPackage);
                        returnData.put("install", listPackages);

                        icons = ip.getIcons(packageName);
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
                IPObjDatabase db = IPObjDatabase.getInstance(mContext.getApplicationContext());
                IPObjDao ipObjDao = db.ipObjDao();


                IPObj ipObj = new IPObj();
                ipObj.setIconPkg(packageName);

                    IPObj ipobj = ipObjDao.getByIconPkg(packageName);
                    if(ipobj != null) {
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