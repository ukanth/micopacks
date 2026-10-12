package dev.ukanth.iconmgr.iconpack;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Log;
import android.util.LruCache;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;

/**
 * Parses each pack's appfilter.xml at most once per installed version.
 */
public final class AppFilterCache {

    private static final String TAG = "AppFilterCache";
    private static final LruCache<String, AppFilter> cache = new LruCache<>(16);

    private AppFilterCache() {
    }

    public static AppFilter get(Context context, String packageName) {
        String key = cacheKey(context, packageName);
        if (key == null) {
            return AppFilter.EMPTY;
        }
        AppFilter cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        AppFilter parsed = load(context, packageName);
        cache.put(key, parsed);
        return parsed;
    }

    public static void invalidate(String packageName) {
        for (String key : cache.snapshot().keySet()) {
            if (key.startsWith(packageName + "@")) {
                cache.remove(key);
            }
        }
    }

    private static String cacheKey(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            return packageName + "@" + info.lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static AppFilter load(Context context, String packageName) {
        try {
            Resources res = context.getPackageManager().getResourcesForApplication(packageName);
            int id = res.getIdentifier("appfilter", "xml", packageName);
            if (id > 0) {
                XmlPullParser xpp = res.getXml(id);
                try {
                    return AppFilterParser.parse(xpp);
                } finally {
                    if (xpp instanceof android.content.res.XmlResourceParser) {
                        ((android.content.res.XmlResourceParser) xpp).close();
                    }
                }
            }
            try (InputStream in = res.getAssets().open("appfilter.xml")) {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(in, "utf-8");
                return AppFilterParser.parse(xpp);
            }
        } catch (Exception e) {
            Log.w(TAG, "No usable appfilter in " + packageName + ": " + e.getMessage());
            return AppFilter.EMPTY;
        }
    }
}
