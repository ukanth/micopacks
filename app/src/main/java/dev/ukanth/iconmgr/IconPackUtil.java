package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by ukanth on 17/7/17.
 */

public class IconPackUtil {
    private Resources iconPackres = null;
    private HashSet<String> matchPackage = new HashSet<>();

    @NonNull
    public int calcTotal(@NonNull Context mContext, String packageName) {
        Set icons = new HashSet();
        XmlPullParser parser = getXmlParser(mContext, packageName,"drawable");
        try {
            if (parser != null) {
                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        icons.add(parser.getAttributeValue(null, "drawable"));
                    }
                    eventType = parser.next();
                }
            }
        } catch (XmlPullParserException | IOException e) {
        }
        return icons.size();
    }

    private XmlPullParser getXmlParser(Context mContext, String packageName, String type) {
        XmlPullParser parser = null;
        try {
            PackageManager pm = mContext.getPackageManager();
            iconPackres = pm.getResourcesForApplication(packageName);

            int appfilter = iconPackres.getIdentifier(type, "xml", packageName);
            if (appfilter > 0) {
                parser = iconPackres.getXml(appfilter);
            } else {
                try {
                    InputStream appfilterstream = iconPackres.getAssets().open(type + ".xml");
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    parser = factory.newPullParser();
                    parser.setInput(appfilterstream, "utf-8");
                } catch (IOException e1) {
                }
            }

        } catch (PackageManager.NameNotFoundException | XmlPullParserException e) {

        }
        return parser;

    }

    public HashMap<String, String> getAppFilter(String packageName, Context mContext, Key key) {
        HashMap<String, String> activities = new HashMap<>();

        try {
            XmlPullParser xpp = getXmlParser(mContext, packageName,"appfilter");
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("item")) {
                        String sKey = xpp.getAttributeValue(null, key.getKey());
                        String sValue = xpp.getAttributeValue(null, key.getValue());

                        if (sKey != null && sValue != null) {
                            activities.put(
                                    sKey.replace("ComponentInfo{", "").replace("}", ""),
                                    sValue.replace("ComponentInfo{", "").replace("}", ""));
                        } else {
                        }
                    }
                }
                xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
        }
        return activities;
    }


    public static String getOtherAppLocaleName(@NonNull Context context, @NonNull Locale locale, @NonNull String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            Resources res = packageManager.getResourcesForApplication(packageName);
            Context otherAppContext = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
            Configuration configuration = new Configuration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration = res.getConfiguration();
                configuration.setLocale(locale);
                return otherAppContext.createConfigurationContext(configuration).getString(info.labelRes);
            }
            configuration.locale = locale;
            res.updateConfiguration(configuration, context.getResources().getDisplayMetrics());
            return res.getString(info.labelRes);
        } catch (Exception e) {
        }
        return null;
    }

    @NonNull
    public List<String> getMissingApps(@NonNull Context context, String currentPackage) {
        List<String> requests = new ArrayList<>();
        HashMap<String, String> appFilter = getAppFilter(currentPackage, context, Key.ACTIVITY);
        PackageManager packageManager = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedApps = packageManager.queryIntentActivities(
                intent, PackageManager.GET_RESOLVED_FILTER);
        try {
            Collections.sort(installedApps,
                    new ResolveInfo.DisplayNameComparator(packageManager));
        } catch (Exception ignored) {
        }

        for (ResolveInfo app : installedApps) {
            String packageName = app.activityInfo.packageName;
            String activity = packageName + "/" + app.activityInfo.name;
            String value = appFilter.get(activity);
            if (value == null) {
                String name = getOtherAppLocaleName(context, new Locale("en"), packageName);
                if (name == null) {
                    name = app.activityInfo.loadLabel(packageManager).toString();
                }
                requests.add(name);
            }
        }
        return requests;
    }

    public enum Key {
        ACTIVITY("component", "drawable"),
        DRAWABLE("drawable", "component");

        private String key;
        private String value;

        Key(String key, String value) {
            this.key = key;
            this.value = value;
        }

        private String getKey() {
            return key;
        }

        private String getValue() {
            return value;
        }
    }

    /*public double getMatch() {
        double matchIcons = matchPackage.size();
        double data = matchIcons / (double) totalInstall;
        data = (int) (data * 100);
        return data;
    }*/
}