package dev.ukanth.iconmgr.iconpack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure appfilter.xml parser (no Android resources), so it can be unit tested with any
 * XmlPullParser implementation.
 */
public final class AppFilterParser {

    private AppFilterParser() {
    }

    public static AppFilter parse(XmlPullParser xpp) throws XmlPullParserException, IOException {
        Map<String, String> components = new HashMap<>();
        Set<String> calendars = new HashSet<>();
        List<String> backs = new ArrayList<>();
        String mask = null;
        String upon = null;
        float scale = 1.0f;

        int event = xpp.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                String tag = xpp.getName();
                switch (tag) {
                    case "item": {
                        String component = normalizeComponent(xpp.getAttributeValue(null, "component"));
                        String drawable = xpp.getAttributeValue(null, "drawable");
                        if (component != null && drawable != null && !drawable.isEmpty()) {
                            // first mapping wins, matching how launchers resolve duplicates
                            if (!components.containsKey(component)) {
                                components.put(component, drawable);
                            }
                        }
                        break;
                    }
                    case "calendar": {
                        String prefix = xpp.getAttributeValue(null, "prefix");
                        if (prefix != null && !prefix.isEmpty()) {
                            calendars.add(prefix);
                        }
                        break;
                    }
                    case "iconback":
                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
                            if (xpp.getAttributeName(i).startsWith("img")) {
                                backs.add(xpp.getAttributeValue(i));
                            }
                        }
                        break;
                    case "iconmask":
                        if (xpp.getAttributeCount() > 0 && "img1".equals(xpp.getAttributeName(0))) {
                            mask = xpp.getAttributeValue(0);
                        }
                        break;
                    case "iconupon":
                        if (xpp.getAttributeCount() > 0 && "img1".equals(xpp.getAttributeName(0))) {
                            upon = xpp.getAttributeValue(0);
                        }
                        break;
                    case "scale":
                        if (xpp.getAttributeCount() > 0 && "factor".equals(xpp.getAttributeName(0))) {
                            try {
                                scale = Float.parseFloat(xpp.getAttributeValue(0));
                            } catch (NumberFormatException e) {
                                // keep default
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            event = xpp.next();
        }
        return new AppFilter(components, calendars, backs, mask, upon, scale);
    }

    /**
     * "ComponentInfo{com.foo/com.foo.Main}" -> "com.foo/com.foo.Main".
     * Also expands the shorthand "com.foo/.Main" so it matches PackageManager's full class names.
     */
    static String normalizeComponent(String raw) {
        if (raw == null) {
            return null;
        }
        String c = raw.trim();
        if (c.startsWith("ComponentInfo{")) {
            c = c.substring("ComponentInfo{".length());
        }
        if (c.endsWith("}")) {
            c = c.substring(0, c.length() - 1);
        }
        int slash = c.indexOf('/');
        if (slash <= 0 || slash == c.length() - 1) {
            return null;
        }
        String pkg = c.substring(0, slash);
        String cls = c.substring(slash + 1);
        if (cls.startsWith(".")) {
            cls = pkg + cls;
        }
        return pkg + "/" + cls;
    }
}
