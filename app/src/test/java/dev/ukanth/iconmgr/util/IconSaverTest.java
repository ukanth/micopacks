package dev.ukanth.iconmgr.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IconSaverTest {

    @Test
    public void sanitizeReplacesPathCharacters() {
        assertEquals("a_b_c", IconSaver.sanitize("a/b:c"));
        assertEquals("Lines Free", IconSaver.sanitize(" Lines Free "));
        assertEquals("icon", IconSaver.sanitize(null));
        assertEquals("icon", IconSaver.sanitize("   "));
    }
}
