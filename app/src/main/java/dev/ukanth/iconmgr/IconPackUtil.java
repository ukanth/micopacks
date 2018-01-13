package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.glidebitmappool.GlideBitmapFactory;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dev.ukanth.iconmgr.util.Util;

/**
 * Created by ukanth on 17/7/17.
 */

public class IconPackUtil {
    private Resources iconPackres = null;

    @NonNull
    public int calcTotal(@NonNull Context mContext, String packageName) {
        Set icons = new HashSet();
        XmlPullParser parser = getXmlParser(mContext, packageName, "drawable");
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


    private Bitmap loadBitmap(String drawableName, String packageName) {
        int id = iconPackres.getIdentifier(drawableName, "drawable", packageName);
        if (id > 0) {
            return GlideBitmapFactory.decodeResource(iconPackres, id, 256, 256);
        }
        return null;
    }

    public Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight, boolean filter) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, filter);
    }

    public Set<Icon> getIcons(Context mContext, String packageName) {
        Set<Icon> mBackImages = new HashSet<Icon>();

        Bitmap mMaskImage;
        Bitmap mFrontImage;

        try {
            XmlPullParser xpp = getXmlParser(mContext, packageName, "appfilter");
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("iconback")) {
                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
                            if (xpp.getAttributeName(i).startsWith("img")) {
                                String drawableName = xpp.getAttributeValue(i);
                                Bitmap iconback = loadBitmap(drawableName, packageName);
                                if (iconback != null)
                                    mBackImages.add(new Icon(drawableName, iconback));
                            }
                        }
                    } else if (xpp.getName().equals("iconmask")) {
                        if (xpp.getAttributeCount() > 0 && xpp.getAttributeName(0).equals("img1")) {
                            String drawableName = xpp.getAttributeValue(0);
                            mMaskImage = loadBitmap(drawableName, packageName);
                            if (mMaskImage != null)
                                mBackImages.add(new Icon(drawableName, mMaskImage));
                        }
                    } else if (xpp.getName().equals("iconupon")) {
                        if (xpp.getAttributeCount() > 0 && xpp.getAttributeName(0).equals("img1")) {
                            String drawableName = xpp.getAttributeValue(0);
                            mFrontImage = loadBitmap(drawableName, packageName);
                            if (mFrontImage != null)
                                mBackImages.add(new Icon(drawableName, mFrontImage));

                        }
                    }
                }
                xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
        }
        return mBackImages;
    }

    public HashMap<String, List<Bitmap>> getIconsList(Context mContext, String packageName) {
        HashMap<String, List<Bitmap>> list = new HashMap();
        List<Bitmap> mBackImages = new ArrayList<>();
        List<Bitmap> mMaskImage = new ArrayList<>();
        List<Bitmap> mFrontImage = new ArrayList<>();
        try {
            XmlPullParser xpp = getXmlParser(mContext, packageName, "appfilter");
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("iconback")) {
                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
                            if (xpp.getAttributeName(i).startsWith("img")) {
                                String drawableName = xpp.getAttributeValue(i);
                                Bitmap iconback = loadBitmap(drawableName, packageName);
                                if (iconback != null)
                                    mBackImages.add(iconback);
                            }
                        }
                    } else if (xpp.getName().equals("iconmask")) {
                        if (xpp.getAttributeCount() > 0 && xpp.getAttributeName(0).equals("img1")) {
                            String drawableName = xpp.getAttributeValue(0);
                            Bitmap image = loadBitmap(drawableName, packageName);
                            if (image != null)
                                mMaskImage.add(image);
                        }
                    } else if (xpp.getName().equals("iconupon")) {
                        if (xpp.getAttributeCount() > 0 && xpp.getAttributeName(0).equals("img1")) {
                            String drawableName = xpp.getAttributeValue(0);
                            Bitmap image = loadBitmap(drawableName, packageName);
                            if (image != null)
                                mFrontImage.add(image);

                        }
                    }
                }
                xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
        }
        list.put("back", mBackImages);
        list.put("mask", mMaskImage);
        list.put("front", mFrontImage);
        return list;
    }

    class Attrb {
        String key;
        String value;

        Attrb(String k, String v) {
            this.key = k;
            this.value = v;
        }
    }

    public Set<Icon> getListIcons(Context mContext, String packageName) {
        Set<Icon> icons = new HashSet<>();
        Key key = Key.ACTIVITY;
        List<Attrb> items = new ArrayList<>();
        //HashMap listMap = getIconsList(mContext, packageName);
        try {
            XmlPullParser xpp = getXmlParser(mContext, packageName, "appfilter");
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("item")) {
                        String sKey = xpp.getAttributeValue(null, key.getKey());
                        if (sKey != null) {
                            sKey = sKey.replace("ComponentInfo{", "").replace("}", "");
                            if (sKey != null) {
                                String name = xpp.getAttributeValue(null, "drawable");
                                if (name != null) {
                                    items.add(new Attrb(sKey, name));
                                }
                            }
                        }
                    }
                }
                xpp.next();
            }
            icons = processXpp(mContext, packageName, items);
        } catch (Exception e) {
            Log.e("MICO", e.getMessage(), e);
        }
        return icons;
    }

    private Bitmap generateBitmap(Bitmap defaultBitmap, List mBackImages, float mFactor, Paint mPaint, Bitmap mMaskImage, Bitmap mFrontImage) {
        // No need to go through below process id defaultBitmap is null
        if (defaultBitmap == null) {
            return null;
        }
        // If no back images, return default app icon
        if (mBackImages.size() == 0) {
            return defaultBitmap;
        }
        // Get a random back image
        Bitmap backImage = getMostAppropriateBackImage(defaultBitmap, mBackImages);

        int backImageWidth = backImage.getWidth();
        int backImageHeight = backImage.getHeight();
        // Create a bitmap for the result
        Bitmap result;
        try {
            result = Bitmap.createBitmap(backImageWidth, backImageHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }
        // Instantiate a canvas to combine the icon / background
        Canvas canvas = new Canvas(result);
        // Draw the background first
        canvas.drawBitmap(backImage, 0, 0, null);
        // Create rects for scaling the default bitmap
        Rect srcRect = new Rect(
                0,
                0,
                defaultBitmap.getWidth(),
                defaultBitmap.getHeight()
        );
        float scaledWidth = mFactor * ((float) backImageWidth);
        float scaledHeight = mFactor * ((float) backImageHeight);
        RectF destRect = new RectF(
                ((float) backImageWidth) / 2.0f - scaledWidth / 2.0f,
                ((float) backImageHeight) / 2.0f - scaledHeight / 2.0f,
                ((float) backImageWidth) / 2.0f + scaledWidth / 2.0f,
                ((float) backImageHeight) / 2.0f + scaledHeight / 2.0f
        );
        // Handle mask image
        if (mMaskImage != null) {
            // First get mask bitmap
            Bitmap mask;
            try {
                mask = Bitmap.createBitmap(backImageWidth, backImageHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return null;
            }
            // Make a temp mask canvas
            Canvas maskCanvas = new Canvas(mask);
            // Draw the bitmap with mask into the result
            maskCanvas.drawBitmap(
                    defaultBitmap, srcRect, destRect, mPaint
            );
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            maskCanvas.drawBitmap(mMaskImage, 0, 0, mPaint);
            mPaint.setXfermode(null);
            canvas.drawBitmap(mask, 0, 0, mPaint);
        } else {
            // Draw the scaled bitmap without mask
            canvas.drawBitmap(
                    defaultBitmap, srcRect, destRect, mPaint
            );
        }
        // Draw the front image
        if (mFrontImage != null) {
            canvas.drawBitmap(mFrontImage, 0, 0, mPaint);
        }
        return result;
    }


    public static @ColorInt
    int getPaletteColorFromApp(Icon app) {
        return getPaletteColorFromBitmap(app.getIconBitmap());
    }

    public static @ColorInt
    int getPaletteColorFromBitmap(Bitmap bitmap) {
        Palette palette;
        try {
            palette = Palette.from(bitmap).generate();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Color.BLACK;
        }
        if (palette.getSwatches().size() > 0) {
            int swatchIndex = 0;
            for (int i = 1; i < palette.getSwatches().size(); i++) {
                if (palette.getSwatches().get(i).getPopulation()
                        > palette.getSwatches().get(swatchIndex).getPopulation()) {
                    swatchIndex = i;
                }
            }
            return palette.getSwatches().get(swatchIndex).getRgb();
        } else {
            return Color.BLACK;
        }
    }

    public static float getHueColorFromColor(@ColorInt int color) {
        float[] hsvValues = new float[3];
        Color.colorToHSV(color, hsvValues);
        return hsvValues[0];
    }

    private Bitmap getMostAppropriateBackImage(Bitmap defaultBitmap, List<Bitmap> mBackImages) {
        if (mBackImages.size() == 1) {
            return mBackImages.get(0);
        }
        @ColorInt int defaultPaletteColor = getPaletteColorFromBitmap(defaultBitmap);
        float defaultHueColor = getHueColorFromColor(defaultPaletteColor);
        float difference = Float.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < mBackImages.size(); i++) {
            @ColorInt int backPaletteColor = getPaletteColorFromBitmap(mBackImages.get(i));
            float backHueColor = getHueColorFromColor(backPaletteColor);
            if (Math.abs(defaultHueColor - backHueColor) < difference) {
                difference = Math.abs(defaultHueColor - backHueColor);
                index = i;
            }
        }
        return mBackImages.get(index);

    }

    public Set<Icon> processXpp(final Context mContext, final String packageName, List<Attrb> input) {
        try {
            ExecutorService service = Executors.newFixedThreadPool(5);
            final List<ResolveInfo> listPackages = Util.getInstalledApps(mContext);
            List<Future<Icon>> futures = new ArrayList<Future<Icon>>();
            for (final Attrb attr : input) {
                Callable<Icon> callable = new Callable<Icon>() {
                    public Icon call() throws Exception {
                        if (isSupported(mContext, packageName, listPackages, attr.key)) {
                            Bitmap iconBitmap = loadBitmap(attr.value, packageName);
                            if (iconBitmap != null) {
                                //iconBitmap = generateBitmap(iconBitmap, mBackImages, mFactor, mPaint, maskImage, frontImage);
                               /* Intent intent = new Intent(mContext, IconPreviewActivity.class);
                                intent.setAction("iconupdate");
                                intent.putExtra("icon",new Icon(attr.value, iconBitmap));
                                mContext.sendBroadcast(intent);*/
                                return new Icon(attr.value, iconBitmap);
                            }
                        }
                        return new Icon("");
                    }
                };
                futures.add(service.submit(callable));
            }
            service.shutdown();

            Set<Icon> outputs = new HashSet<>();
            for (Future<Icon> future : futures) {
                outputs.add(future.get());
            }
            return outputs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashSet<>();
    }


    public HashMap<String, String> getAppFilter(String packageName, Context mContext, Key key) {
        HashMap<String, String> activities = new HashMap<>();

        try {
            XmlPullParser xpp = getXmlParser(mContext, packageName, "appfilter");
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

    public boolean isSupported(@NonNull Context context, String currentPackage, List<ResolveInfo> listPackages, String currentActivity) {
        for (ResolveInfo app : listPackages) {
            String activity = app.activityInfo.packageName + "/" + app.activityInfo.name;
            if (currentActivity.equals(activity)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    public List<String> getMissingApps(@NonNull Context context, String currentPackage, List<ResolveInfo> listPackages) {
        List<String> requests = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        HashMap<String, String> appFilter = getAppFilter(currentPackage, context, Key.ACTIVITY);
        for (ResolveInfo app : listPackages) {
            String packageName = app.activityInfo.packageName;
            String activity = packageName + "/" + app.activityInfo.name;
            String value = appFilter.get(activity);
            if (value == null) {
                String name = (String) app.activityInfo.applicationInfo.loadLabel(packageManager);
                //String name = app.activityInfo.applicationInfo.loadLabel(packageManager).toString();
                requests.add(name);
            }
        }
        return requests;
    }

    /*public static Set<String> getInstalledIconPacks(Context mContext) {

        HashSet returnList = new HashSet<>();
        PackageManager pm = mContext.getPackageManager();
        int flags = PackageManager.GET_META_DATA |
                PackageManager.GET_SHARED_LIBRARY_FILES;

        List<ApplicationInfo> packages = pm.getInstalledApplications(flags);
        ArrayList<String> packageList = new ArrayList<>();
        for (ApplicationInfo info : packages) {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                // System application
                packageList.add(info.packageName);
            } else {
                // Installed by user
                packageList.add(info.packageName);
            }
        }
        List<ResolveInfo> rinfo = pm.queryIntentActivities(new Intent("com.gau.go.launcherex.theme"), PackageManager.GET_META_DATA);
        rinfo.addAll(pm.queryIntentActivities(new Intent("com.novalauncher.THEME"), PackageManager.GET_META_DATA));
        rinfo.addAll(pm.queryIntentActivities(new Intent("org.adw.launcher.THEMES"), PackageManager.GET_META_DATA));

        for (ResolveInfo ri : rinfo) {
            returnList.add(ri.activityInfo.packageName);
        }
        return returnList;
    }*/

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

    /*private Bitmap maskImage(Bitmap original, Bitmap mask) {
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(original, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        return result;
    }*/

    public Set<Icon> getNonThemeIcons(Context context, String currentPackage) throws ExecutionException, InterruptedException {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> listPackages = Util.getInstalledApps(context);
        HashMap<String, String> appFilter = getAppFilter(currentPackage, context, Key.ACTIVITY);
        HashMap<String, List<Bitmap>> listMap = getIconsList(context, currentPackage);
        List<Bitmap> mBackImages = listMap.get("back");
        List<Bitmap> mMaskImages = listMap.get("mask");
        Bitmap maskImage = mMaskImages.size() > 0 ? mMaskImages.get(0) : null;
        List<Bitmap> mFrontImages = listMap.get("front");
        Bitmap frontImage = mFrontImages.size() > 0 ? mFrontImages.get(0) : null;
        float mFactor = 1.0f;
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);

        List<Future<Icon>> futures = new ArrayList<Future<Icon>>();
        ExecutorService service = Executors.newFixedThreadPool(8);


        for (ResolveInfo app : listPackages) {
            String packageName = app.activityInfo.packageName;
            String activity = packageName + "/" + app.activityInfo.name;
            String value = appFilter.get(activity);
            if (value == null) {
                Callable<Icon> callable = () -> {
                    String label = (String)  app.activityInfo.applicationInfo.loadLabel(packageManager);
                    //String name = app.activityInfo.applicationInfo.loadLabel(packageManager).toString();
                    try {
                        Drawable drawable = packageManager.getApplicationIcon(packageName);
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        bitmap = getResizedBitmap(bitmap, 256, 256, true);
                        bitmap = generateBitmap(bitmap, mBackImages, mFactor, mPaint, maskImage, frontImage);
                        //try to mask icon
                        if (bitmap != null) {
                            return new Icon(label, bitmap);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                    return new Icon("");
                };
                futures.add(service.submit(callable));
            }
        }

        service.shutdown();

        Set<Icon> outputs = new HashSet<>();
        for (Future<Icon> future : futures) {
            outputs.add(future.get());
        }
        return outputs;
    }
}