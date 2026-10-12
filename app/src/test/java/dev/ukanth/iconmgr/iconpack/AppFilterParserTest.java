package dev.ukanth.iconmgr.iconpack;

import org.junit.Test;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AppFilterParserTest {

    private static AppFilter parse(String xml) throws Exception {
        XmlPullParser xpp = new KXmlParser();
        xpp.setInput(new StringReader(xml));
        return AppFilterParser.parse(xpp);
    }

    private static final String SAMPLE = "<resources>"
            + "<iconback img1=\"back_a\" img2=\"back_b\"/>"
            + "<iconmask img1=\"mask\"/>"
            + "<iconupon img1=\"upon\"/>"
            + "<scale factor=\"0.85\"/>"
            + "<item component=\"ComponentInfo{com.android.chrome/com.google.android.apps.chrome.Main}\" drawable=\"chrome\"/>"
            + "<item component=\"ComponentInfo{com.whatsapp/.Main}\" drawable=\"whatsapp\"/>"
            + "<item component=\"ComponentInfo{com.whatsapp/com.whatsapp.Main}\" drawable=\"whatsapp_alt\"/>"
            + "<item component=\"ComponentInfo{org.telegram.messenger/org.telegram.ui.LaunchActivity}\" drawable=\"chrome\"/>"
            + "<item component=\"ComponentInfo{com.broken/}\" drawable=\"broken\"/>"
            + "<item component=\"ComponentInfo{com.nodrawable/com.nodrawable.Main}\" drawable=\"\"/>"
            + "<calendar component=\"ComponentInfo{com.google.android.calendar/com.android.calendar.AllInOneActivity}\" prefix=\"calendar_\"/>"
            + "</resources>";

    @Test
    public void parsesComponentsAndStripsComponentInfo() throws Exception {
        AppFilter filter = parse(SAMPLE);
        assertEquals("chrome", filter.drawableFor("com.android.chrome/com.google.android.apps.chrome.Main"));
        assertTrue(filter.covers("org.telegram.messenger/org.telegram.ui.LaunchActivity"));
    }

    @Test
    public void expandsShorthandActivityNames() throws Exception {
        AppFilter filter = parse(SAMPLE);
        // "com.whatsapp/.Main" must match PackageManager's "com.whatsapp/com.whatsapp.Main"
        assertTrue(filter.covers("com.whatsapp/com.whatsapp.Main"));
    }

    @Test
    public void firstMappingWinsForDuplicateComponents() throws Exception {
        AppFilter filter = parse(SAMPLE);
        assertEquals("whatsapp", filter.drawableFor("com.whatsapp/com.whatsapp.Main"));
    }

    @Test
    public void skipsMalformedItems() throws Exception {
        AppFilter filter = parse(SAMPLE);
        assertFalse(filter.covers("com.broken/"));
        assertFalse(filter.covers("com.nodrawable/com.nodrawable.Main"));
        assertEquals(3, filter.getComponentToDrawable().size());
    }

    @Test
    public void distinctIconCountIncludesCalendarPrefixes() throws Exception {
        // drawables: chrome, whatsapp (whatsapp_alt lost to the duplicate) + calendar_ prefix
        assertEquals(3, parse(SAMPLE).distinctIconCount());
    }

    @Test
    public void parsesMaskingResources() throws Exception {
        AppFilter filter = parse(SAMPLE);
        assertEquals(Arrays.asList("back_a", "back_b"), filter.getBackNames());
        assertEquals("mask", filter.getMaskName());
        assertEquals("upon", filter.getUponName());
        assertEquals(0.85f, filter.getScale(), 0.0001f);
        assertTrue(filter.hasMaskingResources());
    }

    @Test
    public void invalidScaleKeepsDefault() throws Exception {
        AppFilter filter = parse("<resources><scale factor=\"abc\"/></resources>");
        assertEquals(1.0f, filter.getScale(), 0.0001f);
        assertFalse(filter.hasMaskingResources());
    }

    @Test
    public void maskOnlyCountsImg1() throws Exception {
        AppFilter filter = parse("<resources><iconmask img2=\"m\"/><iconupon img1=\"u\"/></resources>");
        assertNull(filter.getMaskName());
        assertEquals("u", filter.getUponName());
    }

    @Test
    public void countMissingUsesExactComponents() throws Exception {
        AppFilter filter = parse(SAMPLE);
        int missing = filter.countMissing(Arrays.asList(
                "com.android.chrome/com.google.android.apps.chrome.Main",   // covered
                "com.whatsapp/com.whatsapp.Main",                           // covered (shorthand)
                "com.android.chrome/com.google.android.apps.chrome.Other",  // different activity
                "com.spotify.music/com.spotify.music.MainActivity"));       // not in pack
        assertEquals(2, missing);
    }

    @Test
    public void emptyDocument() throws Exception {
        AppFilter filter = parse("<resources/>");
        assertEquals(0, filter.distinctIconCount());
        assertEquals(1, filter.countMissing(Arrays.asList("a/b")));
        assertSame(AppFilter.EMPTY.getComponentToDrawable().size(), filter.getComponentToDrawable().size());
    }

    @Test
    public void normalizeComponent() {
        assertEquals("a.b/a.b.C", AppFilterParser.normalizeComponent("ComponentInfo{a.b/a.b.C}"));
        assertEquals("a.b/a.b.C", AppFilterParser.normalizeComponent("a.b/.C"));
        assertEquals("a.b/x.Y", AppFilterParser.normalizeComponent(" ComponentInfo{a.b/x.Y} "));
        assertNull(AppFilterParser.normalizeComponent(null));
        assertNull(AppFilterParser.normalizeComponent("ComponentInfo{}"));
        assertNull(AppFilterParser.normalizeComponent("noslash"));
        assertNull(AppFilterParser.normalizeComponent("/onlyclass"));
    }
}
