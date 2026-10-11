package rdiger36.StudioBridge.functions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for the comparison of the online release version with the local version.
 */
public class UpdateTest {

    @Test
    public void detectsNewerVersion() {
        assertTrue(Update.isNewer("2.1.4", "2.1.3"));
        assertTrue(Update.isNewer("2.2.0", "2.1.3"));
        assertTrue(Update.isNewer("3.0", "2.9.9"));
    }

    @Test
    public void comparesMultiDigitPartsNumerically() {
        assertTrue(Update.isNewer("2.1.10", "2.1.9"));
        assertFalse(Update.isNewer("2.2.0", "2.10.0"));
    }

    @Test
    public void ignoresPrefixesAndSuffixes() {
        assertTrue(Update.isNewer("Version 2.1.4", "2.1.3"));
        assertTrue(Update.isNewer("v2.1.4-BETA", "2.1.3"));
        assertFalse(Update.isNewer("v.2.1.3", "2.1.3"));
    }

    @Test
    public void treatsEqualOrOlderVersionsAsNotNewer() {
        assertFalse(Update.isNewer("2.1.3", "2.1.3"));
        assertFalse(Update.isNewer("2.1", "2.1.0"));
        assertFalse(Update.isNewer("2.0.9", "2.1.3"));
    }

    @Test
    public void ignoresUnparsableVersions() {
        assertFalse(Update.isNewer("latest", "2.1.3"));
        assertFalse(Update.isNewer(null, "2.1.3"));
    }
}
