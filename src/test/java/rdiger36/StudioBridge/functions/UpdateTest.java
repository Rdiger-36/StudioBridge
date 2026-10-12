package rdiger36.StudioBridge.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
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

    @Test
    public void takesTheVersionFromTheTagOrTheName() {
        assertEquals("v.2.3.0", Update.versionOf(new JSONObject().put("tag_name", "v.2.3.0").put("name", "StudioBridge")));
        assertEquals("Version 2.3.0", Update.versionOf(new JSONObject().put("tag_name", "latest").put("name", "Version 2.3.0")));
    }

    @Test
    public void preReleaseIsOlderThanItsVersion() {
        assertTrue(Update.isNewer("v.2.3.0", "2.3.0-beta.1"));
        assertFalse(Update.isNewer("v.2.3.0-beta.1", "2.3.0"));
        assertTrue(Update.isNewer("v.2.3.0-beta.1", "2.2.0"));
        assertFalse(Update.isNewer("v.2.2.1-beta.1", "2.3.0-beta.1"));
    }

    @Test
    public void comparesPreReleasesLikeSemanticVersioning() {
        assertTrue(Update.isNewer("v.2.3.0-beta.2", "2.3.0-beta.1"));
        assertTrue(Update.isNewer("v.2.3.0-beta.10", "2.3.0-beta.9"));
        assertTrue(Update.isNewer("v.2.3.0-rc.1", "2.3.0-beta.3"));
        assertTrue(Update.isNewer("v.2.3.0-beta.1", "2.3.0-beta"));
        assertFalse(Update.isNewer("v.2.3.0-beta.1", "2.3.0-beta.1"));
    }

    @Test
    public void displaysTheVersionWithoutPrefix() {
        assertEquals("2.3.0-beta.1", Update.display("v.2.3.0-beta.1"));
        assertEquals("2.2.0", Update.display("Version 2.2.0"));
    }

    @Test
    public void newestReleaseIncludesPreReleasesButNoDrafts() {
        JSONArray releases = new JSONArray()
                .put(new JSONObject().put("tag_name", "v.2.4.0").put("draft", true))
                .put(new JSONObject().put("tag_name", "v.2.3.0-beta.2").put("prerelease", true))
                .put(new JSONObject().put("tag_name", "v.2.2.0"))
                .put(new JSONObject().put("tag_name", "v.2.3.0-beta.1").put("prerelease", true));
        assertEquals("v.2.3.0-beta.2", Update.newest(releases).getString("tag_name"));

        releases.put(new JSONObject().put("tag_name", "v.2.3.0"));
        assertEquals("v.2.3.0", Update.newest(releases).getString("tag_name"));

        assertNull(Update.newest(new JSONArray()));
    }
}
