package rdiger36.StudioBridge.functions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import rdiger36.StudioBridge.gui.MainMenu;

/**
 * Tests for the validation of profile names, which are used as file names.
 */
public class ConfigTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private String saveSettings() throws Exception {
        String oldSavePath = MainMenu.savePath;
        try {
            MainMenu.savePath = folder.getRoot().getAbsolutePath();
            Config.saveUserSettings(null);
            return new String(Files.readAllBytes(new File(folder.getRoot(), "settings").toPath()), StandardCharsets.UTF_8);
        } finally {
            MainMenu.savePath = oldSavePath;
        }
    }

    @Test
    public void startArgumentsAreNotSavedAsSettings() throws Exception {
        MainMenu.savedDirectMode = false;
        MainMenu.directMode = true;
        MainMenu.directModeFromArgs = true;
        MainMenu.savedProfilesDir = "/saved/profiles";
        MainMenu.ProfilesDir = "/from/args";
        MainMenu.useCustomProfilesDir = true;

        String settings = saveSettings();
        assertTrue(settings.contains("directMode=false"));
        assertTrue(settings.contains("customProfilesPath=/saved/profiles"));
    }

    @Test
    public void settingsChosenInTheGuiAreSaved() throws Exception {
        MainMenu.savedDirectMode = false;
        MainMenu.directMode = true;
        MainMenu.directModeFromArgs = false;
        MainMenu.savedProfilesDir = "/saved/profiles";
        MainMenu.ProfilesDir = "/chosen/in/menu";
        MainMenu.useCustomProfilesDir = false;

        String settings = saveSettings();
        assertTrue(settings.contains("directMode=true"));
        assertTrue(settings.contains("customProfilesPath=/chosen/in/menu"));
    }

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
