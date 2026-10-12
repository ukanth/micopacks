package dev.ukanth.iconmgr.detail;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PackDetailFilterTest {

    private final List<IconItem> items = Arrays.asList(
            IconItem.packIcon("WhatsApp", "whatsapp", "com.whatsapp"),
            IconItem.packIcon("Google Maps", "maps", "com.google.android.apps.maps"),
            IconItem.missingApp("Revolut", "com.revolut.revolut"));

    @Test
    public void blankQueryReturnsSameList() {
        assertSame(items, PackDetailViewModel.filter(items, ""));
        assertSame(items, PackDetailViewModel.filter(items, null));
    }

    @Test
    public void matchesTitleOrDrawableCaseInsensitively() {
        assertEquals(1, PackDetailViewModel.filter(items, "WHATS").size());
        // "maps" is the drawable name, "Google Maps" the label: both should match once
        assertEquals(1, PackDetailViewModel.filter(items, "maps").size());
        assertEquals("Revolut", PackDetailViewModel.filter(items, "rev").get(0).title);
    }

    @Test
    public void noMatchIsEmpty() {
        assertTrue(PackDetailViewModel.filter(items, "zzz").isEmpty());
        assertTrue(PackDetailViewModel.filter(null, "a").isEmpty());
    }

    @Test
    public void keysSeparatePackIconsFromApps() {
        IconItem icon = IconItem.packIcon("Revolut", "revolut", "com.revolut.revolut");
        IconItem app = IconItem.missingApp("Revolut", "com.revolut.revolut");
        assertTrue(!icon.key().equals(app.key()));
    }
}
