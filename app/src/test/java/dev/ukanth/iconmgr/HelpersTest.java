package dev.ukanth.iconmgr;

import org.junit.Test;

import dev.ukanth.iconmgr.dao.IPObj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HelpersTest {

    @Test
    public void iconEqualityIncludesPack() {
        Icon a = new Icon("whatsapp", "pack.one", null);
        Icon b = new Icon("whatsapp", "pack.two", null);
        Icon c = new Icon("whatsapp", "pack.one", null);
        assertFalse(a.equals(b));
        assertTrue(a.equals(c));
        assertEquals(a.hashCode(), c.hashCode());
    }

    @Test
    public void sameContentAsComparesDisplayedFields() {
        IPObj a = new IPObj();
        a.setIconPkg("p");
        a.setIconName("Pack");
        a.setMissed(3);
        IPObj b = new IPObj(a);
        assertTrue(a.sameContentAs(b));
        b.setFavorite(true);
        assertFalse(a.sameContentAs(b));
        assertTrue(a.equals(b)); // identity is the package only
    }

    @Test
    public void legacyAttrJsonParsingIsLenient() {
        assertTrue(IconAttr.fromJson("{\"favorite\":true,\"size\":68}").isFavorite());
        assertFalse(IconAttr.fromJson(null).isFavorite());
        assertFalse(IconAttr.fromJson("not json").isFavorite());
    }
}
