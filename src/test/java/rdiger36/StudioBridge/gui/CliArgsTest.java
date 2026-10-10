package rdiger36.StudioBridge.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for reading values of command line arguments.
 */
public class CliArgsTest {

    @Test
    public void readsPlainValue() {
        assertEquals("/path/to/profiles", MainMenu.argValue("--customProfilesDir=/path/to/profiles"));
    }

    @Test
    public void keepsEqualSignsInValue() {
        assertEquals("/path/a=b", MainMenu.argValue("--customProfilesDir=/path/a=b"));
    }

    @Test
    public void stripsSurroundingQuotes() {
        assertEquals("/path/to/profiles/", MainMenu.argValue("--customProfilesDir='/path/to/profiles/'"));
        assertEquals("/path with space", MainMenu.argValue("--customProfilesDir=\"/path with space\""));
    }

    @Test
    public void returnsEmptyValueWhenMissing() {
        assertEquals("", MainMenu.argValue("--customProfilesDir="));
    }
}
