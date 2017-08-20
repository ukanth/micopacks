package dev.ukanth.iconmgr.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.stericson.roottools.RootTools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import dev.ukanth.iconmgr.ActionReceiver;
import dev.ukanth.iconmgr.DetailsActivity;
import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.R;
import eu.chainfire.libsuperuser.Shell;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by ukanth on 20/7/17.
 */

public class Util {

    public static final String CMD_FIND_XML_FILES = "find /data/data/%s -type f -name \\*.xml";
    private static final String TAG = "MICOPACK";

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String PACKAGE_NAME_PATTERN = "^[a-zA-Z_\\$][\\w\\$]*(?:\\.[a-zA-Z_\\$][\\w\\$]*)*$";

    public static final String CMD_CHOWN = "chown %s.%s \"%s\"";
    public static final String CMD_CAT_FILE = "cat \"%s\"";
    public static final String CMD_CP = "cp \"%s\" \"%s\"";
    public static final String TMP_FILE = ".temp";


    public static String readFile(final String fileName, final String packageName, final String updateValue, final Context ctx) {
        final StringBuilder sb = new StringBuilder();
        Log.d(TAG, String.format("readFile(%s)", fileName));
        new AsyncTask<Object, Object, StringBuilder>() {
            @Override
            public StringBuilder doInBackground(Object... args) {
                StringBuilder temp = new StringBuilder();
                List<String> lines = Shell.SU.run(String.format(CMD_CAT_FILE, fileName));
                if (lines != null) {
                    for (String line : lines) {
                        temp.append(line).append(LINE_SEPARATOR);
                    }
                }
                return temp;
            }

            @Override
            public void onPostExecute(StringBuilder res) {
                String fileContent = res.toString();
                if (fileContent != null && !fileContent.isEmpty()) {
                    PreferenceFile preferenceFile = PreferenceFile.fromXml(fileContent);
                    preferenceFile.updateValue("theme_icon_pack", updateValue);
                    savePreferences(preferenceFile, fileName, packageName, ctx);
                    Log.i(TAG, "preferenceFile: " + preferenceFile.getList().size());
                }
            }
        }.execute();
        return sb.toString();
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

        File tmpFile = new File(ctx.getFilesDir(), TMP_FILE);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ctx.openFileOutput(TMP_FILE, Context.MODE_PRIVATE));
            outputStreamWriter.write(preferences);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing temporary file", e);
            return false;
        }

        Shell.SU.run(String.format(CMD_CP, tmpFile.getAbsolutePath(), file));

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

    public static double getPercent(int totalInstall, double matchIcons) {
        double data = matchIcons / (double) totalInstall;
        data = (int) (data * 100);
        return data;
    }

    public static int getUid(Context ctx, String packageName) {
        int uid = 0;
        try {
            uid = ctx.getPackageManager().getApplicationInfo(packageName, 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return uid;
    }

    public static String extractFileName(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }
        return s.substring(s.lastIndexOf(FILE_SEPARATOR) + 1);
    }

    public static String extractFilePath(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }
        return s.substring(0, Math.max(s.length(), s.lastIndexOf(FILE_SEPARATOR)));
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

        Shell.SU.run(String.format(CMD_CHOWN, uid, uid, file));
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
        new AsyncTask<Object, Object, Void>() {
            @Override
            public Void doInBackground(Object... args) {
                RootTools.killProcess(packageName);
                Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                context.startActivity(LaunchIntent);
                return null;
            }
        }.execute();
    }

    public static List<String> findXmlFiles(final String packageName) {
        Log.d(TAG, String.format("findXmlFiles(%s)", packageName));
        List<String> files = Shell.SU.run(String.format(CMD_FIND_XML_FILES, packageName));
        Log.d(TAG, "files: " + Arrays.toString(files.toArray()));
        return files;
    }

    public static boolean changeSharedPreferences(Context ctx, String packageName, String updateValue) {
        boolean res = false;
        try {
            String fileName = getDataDir(ctx, packageName) + File.separator + "shared_prefs" + File.separator + packageName + "_preferences.xml";
            Log.d(TAG, String.format("readTextFile(%s)", fileName));
            readFile(fileName, packageName, updateValue, ctx);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return res;
    }

    private static void setElementValue(Element elem, String updateName) {
        Node kid;
        if (elem != null) {
            if (elem.hasChildNodes()) {
                for (kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
                    if (kid.getNodeType() == Node.TEXT_NODE) {
                        kid.setNodeValue(updateName);
                    }
                }
            }
        }
    }

    private static String getElementValue(Node elem) {
        Node kid;
        if (elem != null) {
            if (elem.hasChildNodes()) {
                for (kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
                    if (kid.getNodeType() == Node.TEXT_NODE) {
                        return kid.getNodeValue();
                    }
                }
            }
        }
        return "";
    }

    private static String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final byte buf[] = new byte[4096];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }

    private static Document XMLfromString(String v) {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(v));
            doc = db.parse(is);
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        }
        return doc;

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

    public static void showNotification(Context context, String packageName) {

        if (Prefs.isNotify(context)) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("pkg", packageName);
            PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);


            Intent yesReceive = new Intent();
            yesReceive.putExtra("pkg", packageName);
            yesReceive.setAction(ActionReceiver.APPLY_ACTION);
            PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);


            NotificationCompat.Action applyAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_apply, "Apply", pendingIntentYes).build();

            NotificationCompat.Builder noti = new NotificationCompat.Builder(context)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.iconinstalled))
                    .setSmallIcon(R.drawable.iconpack)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .addAction(applyAction);

            notificationManager.notify(90297, noti.build());
        }
    }
}
