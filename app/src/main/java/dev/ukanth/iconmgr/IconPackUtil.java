package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.Intent;
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
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;

import com.glidebitmappool.GlideBitmapFactory;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
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
    private static final String TAG = "MicoPacks";
    private Resources iconPackres = null;


    public final static String[] ICON_INTENTS = new String[] {
            "com.fede.launcher.THEME_ICONPACK",
            "com.anddoes.launcher.THEME",
            "com.novalauncher.THEME",
            "com.teslacoilsw.launcher.THEME",
            "com.gau.go.launcherex.theme",
            "org.adw.launcher.THEMES",
            "org.adw.launcher.icons.ACTION_PICK_ICON"
    };

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

    private int getIconCountByDrawable(@NonNull String packageName){
        int count = 0;
        String sectionTitle = "";
        List<String> icons = new ArrayList<>();
        XmlPullParser parser = getXmlParser(packageName, "drawable");
        try {
            if (parser != null) {
                PackageManager pm = App.getContext().getPackageManager();
                iconPackres = pm.getResourcesForApplication(packageName);
                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (parser.getName().equals("category")) {
                            String title = parser.getAttributeValue(null, "title");
                            if (!sectionTitle.equals(title)) {
                                if (sectionTitle.length() > 0) {
                                    count += icons.size();
                                }
                            }
                            sectionTitle = title;
                            icons = new ArrayList<>();
                        } else if (parser.getName().equals("item")) {
                            String name = parser.getAttributeValue(null, "drawable");
                            if (name != null) {
                                int id = iconPackres.getIdentifier(name, "drawable", packageName);
                                if (id > 0) {
                                    icons.add(name);
                                }
                            }
                        }
                    }
                    eventType = parser.next();
                }
            }
        } catch (XmlPullParserException | PackageManager.NameNotFoundException | IOException e) {
        }
        count += icons.size();
        return count;
    }

    private int getIconCountByFilter(@NonNull String packageName){
        Set icons = new HashSet();
        XmlPullParser parser = getXmlParser(packageName, "appfilter");
        try {
            if (parser != null) {
                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        String parserName = parser.getName();
                        if (parserName.equals("item")) {
                            String name = parser.getAttributeValue(null, "drawable");
                            if(name != null && !name.isEmpty()) {
                                icons.add(name);
                            }
                        } else if (parserName.equals("calendar")) {
                            String name = parser.getAttributeValue(null, "prefix");
                            if(name != null && !name.isEmpty()) {
                                icons.add(name);
                            }
                        }
                    }
                    eventType = parser.next();
                }
            }
        } catch (XmlPullParserException | IOException e) {
        }
        return icons.size();
    }

    @NonNull
    public int calcTotal(@NonNull String packageName) {
        int drawCount = getIconCountByDrawable(packageName);
        int filterCount = getIconCountByFilter(packageName);
        return  drawCount > filterCount ? drawCount : filterCount;

    }

    private XmlPullParser getXmlParser(String packageName, String type) {
        XmlPullParser parser = null;
        try {
            PackageManager pm = App.getContext().getPackageManager();
            iconPackres = pm.getResourcesForApplication(packageName);

            int fileIdentity = iconPackres.getIdentifier(type, "xml", packageName);
            if (fileIdentity > 0) {
                parser = iconPackres.getXml(fileIdentity);
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


    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Bitmap loadBitmap(String drawableName, String packageName) {
        try {

            int id = iconPackres.getIdentifier(drawableName, "drawable", packageName);
            if (id > 0) {
                Bitmap bitmap;
                try {
                    bitmap = GlideBitmapFactory.decodeResource(iconPackres, id, 256, 256);
                } catch (Exception e) {
                    bitmap = drawableToBitmap(iconPackres.getDrawable(id));
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                new Thread(() -> {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.putExtra("image",byteArray);
                    broadcastIntent.setAction("UPDATEUI");
                    LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(broadcastIntent);
                }).start();
                return bitmap;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight, boolean filter) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, filter);
    }

    public Set<Icon> getIcons(String packageName) {
        Set<Icon> mBackImages = new HashSet<Icon>();

        Bitmap mMaskImage;
        Bitmap mFrontImage;

        try {
            XmlPullParser xpp = getXmlParser(packageName, "appfilter");
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
            XmlPullParser xpp = getXmlParser(packageName, "appfilter");
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




    public Set<Icon> getListIcons(String packageName) {
        Set<Icon> icons = new HashSet<>();
        Key key = Key.ACTIVITY;
        List<Attrb> items = new ArrayList<>();
        try {
            XmlPullParser xpp = getXmlParser(packageName, "appfilter");
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
            icons = processXpp(packageName, items);
        } catch (Exception e) {
            Log.e("MICO", e.getMessage(), e);
        }
        return icons;
    }

    public Set<Icon> getFilterIcons(String packageName,String query) {
        Set<Icon> icons = new HashSet<>();
        Key key = Key.ACTIVITY;
        List<Attrb> items = new ArrayList<>();
        try {
            XmlPullParser xpp = getXmlParser(packageName, "appfilter");
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("item")) {
                        String sKey = xpp.getAttributeValue(null, key.getKey());
                        if (sKey != null) {
                            sKey = sKey.replace("ComponentInfo{", "").replace("}", "");
                            if (sKey != null) {
                                String name = xpp.getAttributeValue(null, "drawable");
                                if (name != null && name.contains(query)) {
                                    items.add(new Attrb(sKey, name));
                                }
                            }
                        }
                    }
                }
                xpp.next();
            }
            icons = processXpp(packageName, items);
        } catch (Exception e) {
            Log.e("MICO", e.getMessage(), e);
        }
        return icons;
    }

    private Bitmap generateBitmap(Bitmap defaultBitmap, List mBackImages, Paint mPaint, Bitmap mMaskImage, Bitmap mFrontImage, String packageName) {
        // No need to go through below process id defaultBitmap is null

        Log.d(TAG, "-------- " + packageName + " -----------");

        if (packageName.equals("com.act.mobile.apps")) {
            // we need to check now
            Log.e(TAG, packageName);
        }

        float mFactor = 0.8f;

        if (defaultBitmap == null) {
            return null;
        }
        // If no back images, return default app icon
        if (mBackImages.size() == 0) {
            return defaultBitmap;
        }

        // Get a random back image
        Bitmap backImage = getMostAppropriateBackImage(defaultBitmap, mBackImages);

        /*Bitmap emptyBitmap = Bitmap.createBitmap(backImage.getWidth(), backImage.getHeight(), backImage.getConfig());

        if (backImage.sameAs(emptyBitmap)) {
            mFactor = 1.0f;
        }*/


        int backImageWidth = backImage.getWidth();
        int backImageHeight = backImage.getHeight();


        // Create a bitmap for the result
        Bitmap result;
        try {
            result = Bitmap.createBitmap(backImageWidth, backImageHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }
        // Instantiate a canvas to combinecanvas the icon / background
        Canvas canvas = new Canvas(result);
        // Draw the background first
        canvas.drawBitmap(backImage, 0, 0, mPaint);
        // Create rects for scaling the default bitmap
        Rect srcRect = new Rect(
                0,
                0,
                defaultBitmap.getWidth(),
                defaultBitmap.getHeight()
        );


        float scaledWidth = mFactor * ((float) backImageWidth);
        float scaledHeight = mFactor * ((float) backImageHeight);

        float left = ((float) backImageWidth) / 2.0f - scaledWidth / 2.0f;
        float top = ((float) backImageHeight) / 2.0f - scaledHeight / 2.0f;

        float right = ((float) backImageWidth) / 2.0f + scaledWidth / 2.0f;
        float bottom = ((float) backImageHeight) / 2.0f + scaledHeight / 2.0f;

        RectF destRect = new RectF(left, top, right, bottom);

        // Handle mask image
        if (mMaskImage != null) {
            // First get mask bitmap
            Bitmap mask;
            try {
                mask = Bitmap.createBitmap(backImageWidth, backImageHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError e) {
                //e.printStackTrace();
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
            canvas.drawBitmap(defaultBitmap, srcRect, destRect, mPaint);
        }
        // Draw the front image
        if (mFrontImage != null) {
            canvas.drawBitmap(mFrontImage, 0, 0, mPaint);
        }
        return result;
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

    /*public List<Icon> lookUpSearch(final String searchQuery, List<IPObj> objList) {
        try {
            ExecutorService service = Executors.newFixedThreadPool(3);
            List<Future<Set<Icon>>> futures = new ArrayList<Future<Set<Icon>>>();
            for (final IPObj attr : objList) {
                Callable<Set<Icon>> callable = () -> getFilterIcons(attr.getIconPkg(), searchQuery);
                futures.add(service.submit(callable));
            }
            service.shutdown();

            List<Icon> outputs = new ArrayList<>();
            for (Future<Set<Icon>> future : futures) {
                outputs.addAll(future.get());
            }
            return outputs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }*/

    public Set<Icon> processXpp(final String packageName, List<Attrb> input) {
        try {
            ExecutorService service = Executors.newFixedThreadPool(5);
            final List<ResolveInfo> listPackages = Util.getInstalledApps();
            List<Future<Icon>> futures = new ArrayList<Future<Icon>>();
            for (final Attrb attr : input) {
                Callable<Icon> callable = () -> {
                    if (isSupported(listPackages, attr.key)) {
                        Bitmap iconBitmap = loadBitmap(attr.value, packageName);
                        if (iconBitmap != null) {
                            return new Icon(attr.value, packageName, iconBitmap);
                        }
                    }
                    return new Icon("");
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

    public HashMap<String, String> getAppFilter(String packageName, Key key) {
        HashMap<String, String> activities = new HashMap<>();

        try {
            XmlPullParser xpp = getXmlParser(packageName, "appfilter");
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

    public boolean isSupported(List<ResolveInfo> listPackages, String currentActivity) {
        for (ResolveInfo app : listPackages) {
            String activity = app.activityInfo.packageName + "/" + app.activityInfo.name;
            if (currentActivity.equals(activity)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    public List<String> getMissingApps(String currentPackage, List<ResolveInfo> listPackages) {
        List<String> requests = new ArrayList<>();
        PackageManager packageManager = App.getContext().getPackageManager();
        HashMap<String, String> appFilter = getAppFilter(currentPackage, Key.ACTIVITY);
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


    @NonNull
    private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    public Set<Icon> getNonThemeIcons(String currentPackage) throws ExecutionException, InterruptedException {
        Context context = App.getContext();
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> listPackages = Util.getInstalledApps();
        HashMap<String, String> appFilter = getAppFilter(currentPackage, Key.ACTIVITY);
        HashMap<String, List<Bitmap>> listMap = getIconsList(context, currentPackage);
        List<Bitmap> mBackImages = listMap.get("back");
        List<Bitmap> mMaskImages = listMap.get("mask");
        Bitmap maskImage = mMaskImages.size() > 0 ? mMaskImages.get(0) : null;
        List<Bitmap> mFrontImages = listMap.get("front");
        Bitmap frontImage = mFrontImages.size() > 0 ? mFrontImages.get(0) : null;
        //float mFactor = 1.0f;

        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setFilterBitmap(true);
        //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mPaint.setDither(true);


        List<Future<Icon>> futures = new ArrayList<Future<Icon>>();
        ExecutorService service = Executors.newSingleThreadExecutor();

        Set<Icon> outputs = new HashSet<>();

        for (ResolveInfo app : listPackages) {
            String packageName = app.activityInfo.packageName;
            String activity = packageName + "/" + app.activityInfo.name;
            String value = appFilter.get(activity);
            if (value == null) {
                Callable<Icon> callable = () -> {
                    String label = (String) app.activityInfo.applicationInfo.loadLabel(packageManager);
                    try {
                        Drawable drawable = packageManager.getApplicationIcon(packageName);
                        Bitmap bitmap = getBitmapFromDrawable(drawable);
                        bitmap = getResizedBitmap(bitmap, 256, 256, true);
                        bitmap = generateBitmap(bitmap, mBackImages, mPaint, maskImage, frontImage, packageName);
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
        for (Future<Icon> future : futures) {
            outputs.add(future.get());
        }
        return outputs;
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

    class Attrb {
        String key;
        String value;

        Attrb(String k, String v) {
            this.key = k;
            this.value = v;
        }
    }
}