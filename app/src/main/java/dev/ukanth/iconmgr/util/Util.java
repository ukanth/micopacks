package dev.ukanth.iconmgr.util;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.stericson.roottools.RootTools;
import com.topjohnwu.superuser.Shell;

import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.TimeUnit;
import org.ocpsoft.prettytime.units.JustNow;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.detail.PackDetailActivity;
import dev.ukanth.iconmgr.RandomActivity;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by ukanth on 20/7/17.
 */

public class Util {

    public static final String CMD_FIND_XML_FILES = "find /data/data/%s -type f -name \\*.xml";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String CMD_CHOWN = "chown %s.%s \"%s\"";
    public static final String CMD_CAT_FILE = "cat \"%s\"";
    public static final String CMD_CP = "cp \"%s\" \"%s\"";
    public static final String TMP_FILE = ".temp";
    private static final String TAG = "MICOPACK";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";




    public static void updateFile(final String fileName, final String packageName, final String key, final String value, final Context ctx) {
        Log.i(TAG, String.format("Read Preference - (%s)", fileName));

        Executors.newSingleThreadExecutor().execute(() -> {
            StringBuilder temp = new StringBuilder();
            List<String> lines = Shell.cmd(String.format(CMD_CAT_FILE, fileName)).exec().getOut();
            if (lines != null) {
                for (String line : lines) {
                    temp.append(line).append(LINE_SEPARATOR);
                }
            }
            String fileContent = temp.toString();
            if (fileContent != null && !fileContent.isEmpty()) {
                PreferenceFile preferenceFile = PreferenceFile.fromXml(fileContent);
                Log.i(TAG, " Updating " + key + " with value: " + value);
                preferenceFile.updateValue(key, value);
                savePreferences(preferenceFile, fileName, packageName, ctx);
            }
        });
    }

    public static void updateFile(final String fileName, final String packageName, final HashMap<String, String> data, final Context ctx, final boolean restart) {
        Log.i(TAG, String.format("Read Preference - (%s)", fileName));

        Executors.newSingleThreadExecutor().execute(() -> {
            StringBuilder temp = new StringBuilder();
            List<String> lines = Shell.cmd(String.format(CMD_CAT_FILE, fileName)).exec().getOut();
            if (lines != null) {
                for (String line : lines) {
                    temp.append(line).append(LINE_SEPARATOR);
                }
            }
            String fileContent = temp.toString();
            if (fileContent != null && !fileContent.isEmpty()) {
                PreferenceFile preferenceFile = PreferenceFile.fromXml(fileContent);
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    Log.i(TAG, " Updating " + entry.getKey() + " with value: " + entry.getValue());
                    preferenceFile.updateValue(entry.getKey(), entry.getValue());
                }
                savePreferences(preferenceFile, fileName, packageName, ctx);
                if (restart) Util.restartLauncher(ctx, packageName);
            }
        });
    }


    public static boolean savePreferences(PreferenceFile preferenceFile, String file, String packageName, Context ctx) {
        Log.d(TAG, String.format("savePreferences(%s, %s)", file, packageName));
        if (preferenceFile == null) {
            Log.e(TAG, "Error preferenceFile is null");
            return false;
        }

        if (!preferenceFile.isValid()) {
            Log.e(TAG, "Error preferenceFile is not valid");
            return false;
        }

        String preferences = preferenceFile.toXml();
        if (TextUtils.isEmpty(preferences)) {
            Log.e(TAG, "Error preferences is empty");
            return false;
        }

        final File tmpFile = new File(ctx.getFilesDir(), TMP_FILE);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ctx.openFileOutput(TMP_FILE, Context.MODE_PRIVATE));
            outputStreamWriter.write(preferences);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing temporary file", e);
            return false;
        }

        Shell.cmd(String.format(CMD_CP, tmpFile.getAbsolutePath(), file)).exec();

        if (!fixUserAndGroupId(ctx, file, packageName)) {
            Log.e(TAG, "Error fixUserAndGroupId");
            return false;
        }

        if (!tmpFile.delete()) {
            Log.e(TAG, "Error deleting temporary file");
        }
        Log.d(TAG, "Preferences correctly updated");
        return true;
    }

   /* public static double getPercent(int totalInstall, double matchIcons) {
        double data = matchIcons / (double) totalInstall;
        data = (int) (data * 100);
        return data;
    }*/

    public static int getUid(Context ctx, String packageName) {
        int uid = 0;
        try {
            uid = ctx.getPackageManager().getApplicationInfo(packageName, 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return uid;
    }

    private static boolean fixUserAndGroupId(Context ctx, String file, String packageName) {
        Log.d(TAG, String.format("fixUserAndGroupId(%s, %s)", file, packageName));
        String uid;
        PackageManager pm = ctx.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            uid = String.valueOf(appInfo.uid);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "error while getting uid", e);
            return false;
        }

        if (TextUtils.isEmpty(uid)) {
            Log.d(TAG, "uid is undefined");
            return false;
        }

        Shell.cmd(String.format(CMD_CHOWN, uid, uid, file)).exec();
        return true;
    }

    private static File getDataDir(Context ctx, String packageName) {
        try {
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(packageName, 0);
            if (packageInfo == null) return null;
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if (applicationInfo == null) return null;
            if (applicationInfo.dataDir == null) return null;
            return new File(applicationInfo.dataDir);
        } catch (PackageManager.NameNotFoundException ex) {
            return null;
        }
    }

    public static void restartLauncher(final Context context, final String packageName) {

        Executors.newSingleThreadExecutor().execute(() -> {
            RootTools.killProcess(packageName);
            Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (LaunchIntent != null) {
                LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(LaunchIntent);
            }
        });
    }

   /* public static List<String> findXmlFiles(final String packageName) {
        Log.d(TAG, String.format("findXmlFiles(%s)", packageName));
        List<String> files = Shell.SU.run(String.format(CMD_FIND_XML_FILES, packageName));
        Log.d(TAG, "files: " + Arrays.toString(files.toArray()));
        return files;
    }*/

    public static boolean changeSharedPreferences(Context ctx, String packageName, String key, String value, String preferenceFileName) {
        boolean res = false;
        try {
            String fileName = getDataDir(ctx, packageName) + File.separator + "shared_prefs" + File.separator + preferenceFileName;
            Log.d(TAG, String.format("readTextFile(%s)", fileName));
            updateFile(fileName, packageName, key, value, ctx);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return res;
    }

    public static String prettyFormat(Date date) {
        PrettyTime prettyTime = new PrettyTime();
        for (TimeUnit t : prettyTime.getUnits()) {
            if (t instanceof JustNow) {
                prettyTime.removeUnit(t);
                break;
            }
        }
        prettyTime.setReference(date);
        return prettyTime.format(new Date(0));
    }

    public static boolean changeSharedPreferences(Context ctx, String packageName, HashMap<String, String> data, String preferenceFileName, boolean restart) {
        boolean res = false;
        try {
            String fileName = getDataDir(ctx, packageName) + File.separator + "shared_prefs" + File.separator + preferenceFileName;
            Log.d(TAG, String.format("readTextFile(%s)", fileName));
            updateFile(fileName, packageName, data, ctx, restart);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return res;
    }

    public static void determineApply(Context ctx, IPObj currentItem) {
        String currentLauncher = getCurrentLauncher(ctx);
        if (currentLauncher != null && !Prefs.usePrompt()) {
            if(currentItem != null) {
                LauncherHelper.apply(ctx, currentItem.getIconPkg(), currentLauncher);
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.unable_iconpack), Toast.LENGTH_SHORT).show();
            }
        } else {
            showInstalledLaunchers(currentItem, ctx);
        }
    }

    public static void showInstalledLaunchers(IPObj currentItem, Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> lst = pm.queryIntentActivities(i, 0);
        HashMap<String,String> launcherMap=new HashMap<>();
        List<String> launchers = new ArrayList<>();
        for (ResolveInfo resolveInfo : lst) {
            try {
                if(!resolveInfo.activityInfo.packageName.equals("com.android.settings")){
                    String app_name = (String) pm.getApplicationLabel(
                            pm.getApplicationInfo(resolveInfo.activityInfo.packageName
                                    , PackageManager.GET_META_DATA));
                    launcherMap.put(app_name,resolveInfo.activityInfo.packageName);
                    launchers.add(app_name);
                }
            } catch(Exception e){
                Log.e("MICO", e.getMessage(), e);
            }
        }

        if (currentItem == null) {
            Toast.makeText(ctx, ctx.getString(R.string.unable_iconpack), Toast.LENGTH_SHORT).show();
            return;
        }
        final int[] selected = {-1};
        new MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.select_launcher)
                .setSingleChoiceItems(launchers.toArray(new String[0]), -1, (dialog, which) -> selected[0] = which)
                .setPositiveButton(R.string.apply, (dialog, which) -> {
                    if (selected[0] >= 0) {
                        LauncherHelper.apply(ctx, currentItem.getIconPkg(), launcherMap.get(launchers.get(selected[0])));
                    }
                })
                .show();
    }

    public static String getCurrentLauncher(Context ctx) {
        String name = null;
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = ctx.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            // should not happen. A home is always installed, isn't it?
        } else if ("android".equals(res.activityInfo.packageName)) {
            // No default selected
        } else {
            name = res.activityInfo.packageName;
        }
        return name;
    }

    public static int hash(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = 31 * h + s.charAt(i);
        }
        return h;
    }


    public static List<String> getExcludedPackages(){
        List<String> excludePackages = new ArrayList<>();
        excludePackages.add("com.arun.kustomiconpack");
        excludePackages.add("ginlemon.iconpackstudio");
        return excludePackages;
    }

    public static void showNotification(String packageName, String name) {
        Context context = App.getContext();
        String CHANNEL_ID = "my_channel_01";

        if (Prefs.isNotify()) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                /* Create or update. */
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "New Icon pack",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Micopacks");
                channel.setShowBadge(true);
                notificationManager.createNotificationChannel(channel);
            }

            int notificationId = hash(packageName);
            // Android 12+ requires an explicit mutability flag, and blocks starting activities
            // from a receiver triggered by a notification, so every action opens an activity directly.
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

            Intent intent = PackDetailActivity.intent(context, packageName);
            intent.setAction("dev.ukanth.iconmgr.OPEN_PACK");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
            PendingIntent pIntent = PendingIntent.getActivity(context, notificationId, intent, flags);

            Intent applyIntent = new Intent(context, RandomActivity.class);
            applyIntent.setAction("dev.ukanth.iconmgr.APPLY_ACTION");
            applyIntent.putExtra("pack", packageName);
            applyIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
            applyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent applyIntentYes = PendingIntent.getActivity(context, notificationId, applyIntent, flags);

            Intent previewActivity = PackDetailActivity.intent(context, packageName);
            previewActivity.setAction("dev.ukanth.iconmgr.PREVIEW_ACTION");
            previewActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            previewActivity.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
            PendingIntent previewIntent = PendingIntent.getActivity(context, notificationId, previewActivity, flags);


            NotificationCompat.Action applyAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_apply, "Apply", applyIntentYes).build();

            NotificationCompat.Action previewAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_preview, "Preview", previewIntent).build();

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "default")
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(name + " " + context.getString(R.string.iconinstalled))
                    .setSmallIcon(R.drawable.iconpack)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setChannelId(CHANNEL_ID)
                    .addAction(applyAction)
                    .addAction(previewAction);
            notificationManager.notify(notificationId, mBuilder.build());
        }
    }


    public static Drawable resizeImage(Context context, Drawable image) {
        Bitmap bitmap = getBitmapFromDrawable(image);
        int SIZE_DP = 60;
        final float scale = context.getResources().getDisplayMetrics().density;
        int p = (int) (SIZE_DP * scale + 0.5f);

        Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, p, p, false);
        return new BitmapDrawable(context.getResources(), bitmapResized);
    }

    @NonNull
    private static Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    private static boolean isSystemPackage(ResolveInfo ri) {
        return ((ri.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    /**
     * Query PackageManager directly for installed apps (uncached)
     * Internal use only - prefer getInstalledApps() for cached access
     */
    public static List<ResolveInfo> queryInstalledApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return App.getContext().getPackageManager().queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
    }

    /**
     * Get installed apps with caching for performance
     */
    public static List<ResolveInfo> getInstalledApps() {
        return AppCache.getInstance().getInstalledApps(false);
    }

    /**
     * Get installed apps, forcing cache refresh
     */
    public static List<ResolveInfo> getInstalledApps(boolean forceRefresh) {
        return AppCache.getInstance().getInstalledApps(forceRefresh);
    }

    public static boolean isPackageExisted(Context context, String targetPackage) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }


    /**
     * Parse author name from certificate (expensive operation)
     * Internal use only - prefer getAuthorName() for cached access
     */
    public static String parseAuthorName(Context context, String targetPackage) {
        PackageManager pm = context.getPackageManager();
        try {
            Signature[] sigs = context.getPackageManager().getPackageInfo(targetPackage, PackageManager.GET_SIGNATURES).signatures;
            for (Signature sig : sigs)
            {
                /*
                 * Get the X.509 certificate.
                 */
                final byte[] rawCert = sig.toByteArray();
                InputStream certStream = new ByteArrayInputStream(rawCert);

                try {
                    CertificateFactory certFactory = CertificateFactory.getInstance("X509");
                    X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(certStream);
                    String issuerDN = x509Cert.getIssuerDN().getName();

                    String[] nameComponents = issuerDN.split(",");
                    for (String nameComponent : nameComponents) {
                        String[] keyValue = nameComponent.trim().split("=");
                        if (keyValue.length == 2 && keyValue[0].trim().equalsIgnoreCase("CN")) {
                            return keyValue[1].trim();
                        }
                    }
                }
                catch (CertificateException e) {
                    Log.e("getAuthorName","CertificateException"+Log.getStackTraceString(e));
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("getAuthorName","NameNotFoundException"+Log.getStackTraceString(e));
        }
        return null;
    }

    /**
     * Get author name with caching for performance
     */
    public static String getAuthorName(Context context, String targetPackage) {
        return AuthorCache.getInstance().getAuthorName(context, targetPackage);
    }



    public static IPObj getRandomInstalledIconPack(IPObjDao ipObjDao) {

        List<IPObj> list = ipObjDao.getAll();
        try {
            if (list != null && !list.isEmpty()) {
                Random rand = new Random();
                int random = rand.nextInt(list.size());
                return list.get(random);
            }
        } catch (IllegalArgumentException |
                NullPointerException e) {
            Log.e("MICO", e.getMessage(), e);
        }
        return null;
    }
}
