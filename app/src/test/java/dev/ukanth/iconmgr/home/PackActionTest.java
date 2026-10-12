package dev.ukanth.iconmgr.home;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PackActionTest {

    @Test
    public void fromKey_roundTripsEveryAction() {
        for (PackAction action : PackAction.values()) {
            assertEquals(action, PackAction.fromKey(action.key));
        }
    }

    @Test
    public void fromKey_unknownOrMissingIsOff() {
        assertEquals(PackAction.OFF, PackAction.fromKey("bogus"));
        assertEquals(PackAction.OFF, PackAction.fromKey(""));
    }

    @Test
    public void keysMatchSettingsValues() {
        // must stay in sync with @array/swipe_action_values and the defaults in Prefs
        assertEquals("apply", PackAction.APPLY.key);
        assertEquals("favorite", PackAction.FAVORITE.key);
        assertEquals("uninstall", PackAction.UNINSTALL.key);
    }
}
