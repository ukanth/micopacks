package dev.ukanth.iconmgr;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by ukanth on 14/8/17.
 */


public class IconRequest extends AsyncTask<Void, Void, Boolean> {

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
    protected Boolean doInBackground(Void... voids) {
        while (!isCancelled()) {
            try {
                IconPackUtil ip = new IconPackUtil();
                List<String> missPackage = ip.getMissingApps(mContext, packageName);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            if (mContext == null) return;

            FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
            if (fm == null) return;

            Fragment fragment = fm.findFragmentByTag("home");
            if (fragment == null) return;

        } else {
        }
    }
}