package rdiger36.StudioBridge.functions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for the validation of profile names, which are used as file names.
 */
public class ConfigTest {

    @Test
    public void acceptsNormalNames() {
        assertTrue(Config.isValidProfileName("X1C Werkstatt"));
        assertTrue(Config.isValidProfileName("P1S-2 (Büro)"));
    }

    @Test
    public void rejectsEmptyAndReservedNames() {
        assertFalse(Config.isValidProfileName(null));
        assertFalse(Config.isValidProfileName(""));
        assertFalse(Config.isValidProfileName("   "));
        assertFalse(Config.isValidProfileName("New profile"));
        assertFalse(Config.isValidProfileName("---"));
    }

    @Test
    public void rejectsPathAndSpecialCharacters() {
        assertFalse(Config.isValidProfileName("../evil"));
        assertFalse(Config.isValidProfileName("a/b"));
        assertFalse(Config.isValidProfileName("a\\b"));
        assertFalse(Config.isValidProfileName("a:b"));
        assertFalse(Config.isValidProfileName("what?"));
        assertFalse(Config.isValidProfileName(".hidden"));
        assertFalse(Config.isValidProfileName(" padded "));
    }
}
