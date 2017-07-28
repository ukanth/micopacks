package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by ukanth on 17/7/17.
 */

public class IconPack {
    public String packageName;
    public String name;
    public String type;
    public long installTime;
    int totalInstall;
    private Resources iconPackres = null;
    private HashSet<String> totalDraw = new HashSet<>();
    private HashSet<String> matchPackage = new HashSet<>();

    public int getCount() {
        return totalDraw.size();
    }

    public IconPack(String packageName, Context mContext, ArrayList installedPackages) {
        totalInstall = installedPackages.size();
        boolean calcPercent = Prefs.isCalcPercent(mContext);
        boolean isTotalIcons = Prefs.isTotalIcons(mContext);
        if (isTotalIcons) {
            PackageManager pm = mContext.getPackageManager();
            try {
                XmlPullParser xpp = null;
                String comp;
                iconPackres = pm.getResourcesForApplication(packageName);
                int appfilter = iconPackres.getIdentifier("appfilter", "xml", packageName);
                if (appfilter > 0) {
                    xpp = iconPackres.getXml(appfilter);
                } else {
                    try {
                        InputStream appfilterstream = iconPackres.getAssets().open("appfilter.xml");
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        factory.setNamespaceAware(true);
                        xpp = factory.newPullParser();
                        xpp.setInput(appfilterstream, "utf-8");
                    } catch (IOException e1) {
                    }
                }
                if (xpp != null) {
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("item")) {
                                String component = xpp.getAttributeValue(null, "component");
                                String drawable = xpp.getAttributeValue(null, "drawable");
                                totalDraw.add(drawable);
                                if (calcPercent && component != null) {
                                    int startTag = component.indexOf("{") + 1;
                                    int endTag = component.indexOf("}");
                                    if (startTag >= 0 && endTag > startTag) {
                                        component = component.substring(startTag, endTag);
                                    }
                                    if (component.contains("/")) {
                                        comp = component.split("/")[0];
                                    } else {
                                        comp = component;
                                    }
                                    if (installedPackages.contains(comp)) {
                                        matchPackage.add(comp);
                                    }
                                }
                            }
                        }
                        eventType = xpp.next();
                    }
                }
            } catch (PackageManager.NameNotFoundException | XmlPullParserException | IOException e) {
            }
        }

    }

    public double getMatch() {
        double matchIcons = matchPackage.size();
        double data = matchIcons / (double) totalInstall;
        data = (int) (data * 100);
        return data;
    }

    public String getMatchStr() {
        return " " + getMatch() + "%";
    }

}