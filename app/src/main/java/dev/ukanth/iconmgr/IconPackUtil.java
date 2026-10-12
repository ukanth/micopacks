package dev.ukanth.iconmgr;

import android.content.pm.PackageManager;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import java.util.List;


import dev.ukanth.iconmgr.iconpack.AppFilter;
import dev.ukanth.iconmgr.iconpack.AppFilterCache;

import dev.ukanth.iconmgr.util.BitmapCache;
import dev.ukanth.iconmgr.util.IconPackMaskingInfo;

/**
 * Created by ukanth on 17/7/17.
 */

public class IconPackUtil {
    private static final String TAG = "MicoPacks";


    private final java.util.Map<String, Resources> packResources = new java.util.concurrent.ConcurrentHashMap<>();

    public final static String[] ICON_INTENTS = new String[] {
            "com.fede.launcher.THEME_ICONPACK",
            "com.anddoes.launcher.THEME",
            "com.novalauncher.THEME",
            "com.teslacoilsw.launcher.THEME",
            "com.gau.go.launcherex.theme",
            "org.adw.launcher.THEMES",
            "org.adw.launcher.icons.ACTION_PICK_ICON"
    };

    private Resources resourcesFor(String packageName) throws PackageManager.NameNotFoundException {
        // per-package so loader threads working on different packs never swap resources
        Resources res = packResources.get(packageName);
        if (res == null) {
            res = App.getContext().getPackageManager().getResourcesForApplication(packageName);
            packResources.put(packageName, res);
        }
        return res;
    }

    private int getIconCountByDrawable(@NonNull String packageName){
        int count = 0;
        String sectionTitle = "";
        List<String> icons = new ArrayList<>();
        XmlPullParser parser = getXmlParser(packageName, "drawable");
        try {
            if (parser != null) {
                Resources res = resourcesFor(packageName);
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
                                int id = res.getIdentifier(name, "drawable", packageName);
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

    /**
     * Drawable names listed in the pack's drawable.xml (its full icon catalogue), in file order.
     * Unlike calcTotal this does not verify each resource exists, so it stays fast for packs with
     * 10k+ icons; a missing one simply renders as an empty cell.
     */
    public List<String> getDrawableXmlNames(@NonNull String packageName) {
        List<String> names = new ArrayList<>();
        XmlPullParser parser = getXmlParser(packageName, "drawable");
        if (parser == null) {
            return names;
        }
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
                    String name = parser.getAttributeValue(null, "drawable");
                    if (name != null && !name.isEmpty()) {
                        names.add(name);
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.w(TAG, "drawable.xml unreadable in " + packageName);
        }
        return names;
    }

    /** One pack icon, decoded at ~256px and cached; null if the pack has no such drawable. */
    public Bitmap loadIcon(String packageName, String drawableName) {
        return loadBitmap(drawableName, packageName);
    }

    /** Grid thumbnail no larger than {@code sizePx}, cached separately from the full icon. */
    public Bitmap loadThumbnail(String packageName, String drawableName, int sizePx) {
        return loadBitmap(drawableName, packageName, sizePx, true);
    }

    @NonNull
    public int calcTotal(@NonNull String packageName) {
        int drawCount = getIconCountByDrawable(packageName);
        int filterCount = AppFilterCache.get(App.getContext(), packageName).distinctIconCount();
        return Math.max(drawCount, filterCount);
    }

    private XmlPullParser getXmlParser(String packageName, String type) {
        XmlPullParser parser = null;
        try {
            Resources res = resourcesFor(packageName);
            int fileIdentity = res.getIdentifier(type, "xml", packageName);
            if (fileIdentity > 0) {
                parser = res.getXml(fileIdentity);
            } else {
                try {
                    InputStream appfilterstream = res.getAssets().open(type + ".xml");
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

        int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 192;
        int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 192;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Bitmap loadBitmap(String drawableName, String packageName) {
        return loadBitmap(drawableName, packageName, 256, false);
    }

    /**
     * @param size     decode target in px
     * @param exact    scale down to at most {@code size} (grid thumbnails); otherwise keep the
     *                 power-of-two decode, which can be up to 2x larger (sheet, saving, theming)
     */
    private Bitmap loadBitmap(String drawableName, String packageName, int size, boolean exact) {
        if (drawableName == null) {
            return null;
        }
        String cacheName = exact ? drawableName + "#" + size : drawableName;
        try {
            // Check cache first
            Bitmap cached = BitmapCache.getInstance().get(packageName, cacheName);
            if (cached != null) {
                return cached;
            }

            Resources res = resourcesFor(packageName);
            int id = res.getIdentifier(drawableName, "drawable", packageName);
            if (id > 0) {
                Bitmap bitmap;
                try {
                    // Decode with inSampleSize to load efficiently near the target size
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeResource(res, id, options);
                    options.inSampleSize = calculateInSampleSize(options, size, size);
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeResource(res, id, options);
                } catch (Exception e) {
                    bitmap = null;
                }
                // decodeResource returns null (no exception) for XML/vector drawables,
                // which many packs (e.g. Arcticons) use; render those via the Drawable instead
                if (bitmap == null) {
                    bitmap = drawableToBitmap(res.getDrawable(id, null));
                }
                if (exact && bitmap != null && (bitmap.getWidth() > size || bitmap.getHeight() > size)) {
                    float scale = (float) size / Math.max(bitmap.getWidth(), bitmap.getHeight());
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.max(1, Math.round(bitmap.getWidth() * scale)),
                            Math.max(1, Math.round(bitmap.getHeight() * scale)), true);
                }

                // Cache the bitmap
                BitmapCache.getInstance().put(packageName, cacheName, bitmap);
                return bitmap;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Get masking information for auto-theming
     *
     * @param packageName Icon pack package name
     * @return IconPackMaskingInfo containing masking resources, or null if none found
     */
    public IconPackMaskingInfo getMaskingInfo(String packageName) {
        // Check cache first
        if (IconPackMaskingInfo.hasCache(packageName)) {
            IconPackMaskingInfo cached = IconPackMaskingInfo.getCache(packageName);
            return cached != null && cached.hasMaskingResources() ? cached : null;
        }

        AppFilter filter = AppFilterCache.get(App.getContext(), packageName);
        IconPackMaskingInfo info = new IconPackMaskingInfo();
        for (String name : filter.getBackNames()) {
            info.addBackground(loadBitmap(name, packageName));
        }
        info.setMask(loadBitmap(filter.getMaskName(), packageName));
        info.setOverlay(loadBitmap(filter.getUponName(), packageName));
        info.setScale(filter.getScale());

        // Cache the result
        IconPackMaskingInfo.putCache(packageName, info);

        return info.hasMaskingResources() ? info : null;
    }
}
