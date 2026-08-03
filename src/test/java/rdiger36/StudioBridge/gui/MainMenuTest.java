package rdiger36.StudioBridge.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for the remapping of profile paths that were stored in the settings file
 * before the data directory was renamed from "StudioBridge" to ".studio-bridge".
 */
public class MainMenuTest {

    private static final String SEP = System.getProperty("file.separator");
    private static final String LEGACY_DIR = SEP + "home" + SEP + "user" + SEP + "StudioBridge";
    private static final String CURRENT_DIR = SEP + "home" + SEP + "user" + SEP + ".studio-bridge";

    /**
     * The default profiles directory of an old installation has to end up in the new
     * data directory. This is the case reported in issue #25.
     */
    @Test
    public void remapsDefaultProfilesDirectoryOfOldInstallations() {
        assertEquals(CURRENT_DIR + SEP + "Profiles",
                MainMenu.remapLegacyProfilesPath(LEGACY_DIR + SEP + "Profiles", LEGACY_DIR, CURRENT_DIR));
    }

    /**
     * A path pointing at the old data directory itself is remapped to the new one.
     */
    @Test
    public void remapsLegacyDirectoryItself() {
        assertEquals(CURRENT_DIR,
                MainMenu.remapLegacyProfilesPath(LEGACY_DIR, LEGACY_DIR, CURRENT_DIR));
    }

    /**
     * Sub directories keep their relative position, because the directory migration
     * merges them recursively.
     */
    @Test
    public void keepsRelativePositionOfNestedDirectories() {
        assertEquals(CURRENT_DIR + SEP + "My" + SEP + "Profiles",
                MainMenu.remapLegacyProfilesPath(LEGACY_DIR + SEP + "My" + SEP + "Profiles", LEGACY_DIR, CURRENT_DIR));
    }

    /**
     * A directory the user selected somewhere else must not be touched.
     */
    @Test
    public void keepsCustomDirectoriesOutsideTheLegacyDirectory() {
        String custom = SEP + "home" + SEP + "user" + SEP + "Documents" + SEP + "Profiles";

        assertEquals(custom, MainMenu.remapLegacyProfilesPath(custom, LEGACY_DIR, CURRENT_DIR));
    }

    /**
     * A directory that only shares its prefix with the old data directory is not a match.
     */
    @Test
    public void doesNotMatchSiblingDirectoriesSharingThePrefix() {
        String sibling = LEGACY_DIR + "Backup" + SEP + "Profiles";

        assertEquals(sibling, MainMenu.remapLegacyProfilesPath(sibling, LEGACY_DIR, CURRENT_DIR));
    }

    /**
     * Running the remapping again on an already migrated path changes nothing, so it is
     * safe to execute it on every start.
     */
    @Test
    public void isIdempotentForAlreadyMigratedPaths() {
        String migrated = CURRENT_DIR + SEP + "Profiles";

        assertEquals(migrated, MainMenu.remapLegacyProfilesPath(migrated, LEGACY_DIR, CURRENT_DIR));
    }

    /**
     * A missing path must not cause a failure during startup.
     */
    @Test
    public void keepsNullPathUntouched() {
        assertEquals(null, MainMenu.remapLegacyProfilesPath(null, LEGACY_DIR, CURRENT_DIR));
    }
}
