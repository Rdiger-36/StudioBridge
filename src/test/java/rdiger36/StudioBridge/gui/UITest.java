package rdiger36.StudioBridge.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for reading the theme setting and the dark mode of Linux desktops.
 */
public class UITest {

    @Test
    public void fixedThemesIgnoreTheSystem() {
        assertTrue(UI.isDark(UI.THEME_DARK));
        assertFalse(UI.isDark(UI.THEME_LIGHT));
    }

    @Test
    public void readsTheGnomeColorScheme() {
        assertTrue(UI.isLinuxDark("'prefer-dark'", "'Adwaita'"));
        assertFalse(UI.isLinuxDark("'prefer-light'", "'Adwaita-dark'"));
        assertFalse(UI.isLinuxDark("'default'", "'Adwaita'"));
    }

    @Test
    public void fallsBackToTheGtkThemeName() {
        assertTrue(UI.isLinuxDark("'default'", "'Adwaita-dark'"));
        assertTrue(UI.isLinuxDark(null, "'Yaru-Dark'"));
        assertFalse(UI.isLinuxDark(null, null));
    }
}
