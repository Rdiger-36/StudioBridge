package rdiger36.StudioBridge.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import rdiger36.StudioBridge.functions.Config;
import rdiger36.StudioBridge.objects.Printer;

/**
 * Tests that saving and renaming keep the profile file, the list, printerMap and mainPrinter in
 * step. These paths need no dialog, so they run without a window.
 */
public class ProfileManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private String oldProfilesDir;
    private JComboBox<String> list;
    private ProfileManager profiles;

    @Before
    public void setUp() {
        oldProfilesDir = MainMenu.ProfilesDir;
        MainMenu.ProfilesDir = folder.getRoot().getAbsolutePath();
        MainMenu.printerMap = new HashMap<>();
        MainMenu.mainPrinter = null;
        list = new JComboBox<>(new DefaultComboBoxModel<>());
        profiles = new ProfileManager(null, list);
    }

    @After
    public void tearDown() {
        MainMenu.ProfilesDir = oldProfilesDir;
    }

    private static Printer printer(String profile) {
        return new Printer(profile, "X1C", "192.168.1.50", "01S00A000000001", "X1C");
    }

    private List<String> files() {
        return Arrays.asList(folder.getRoot().list());
    }

    @Test
    public void storeSavesListsAndSelectsTheProfile() throws Exception {
        assertTrue(profiles.store(printer("Workshop"), null));

        assertTrue(files().contains("Workshop.sbp"));
        assertEquals("Workshop", list.getSelectedItem());
        assertTrue(MainMenu.printerMap.containsKey("Workshop"));
        assertEquals("Workshop", MainMenu.mainPrinter.getProfileName());
        assertEquals("192.168.1.50", Config.readProfile(new File(folder.getRoot(), "Workshop.sbp")).getIpAdress());
    }

    @Test
    public void storingTwiceListsTheProfileOnce() {
        profiles.store(printer("Workshop"), null);
        profiles.store(printer("Workshop"), null);
        assertEquals(1, list.getItemCount());
    }

    @Test
    public void renameReplacesFileListEntryAndMapKey() {
        profiles.store(printer("Workshop"), null);
        assertTrue(profiles.store(printer("Office"), "Workshop"));

        assertEquals(Arrays.asList("Office.sbp"), files());
        assertEquals(1, list.getItemCount());
        assertEquals("Office", list.getSelectedItem());
        assertFalse(MainMenu.printerMap.containsKey("Workshop"));
        assertTrue(MainMenu.printerMap.containsKey("Office"));
    }

    @Test
    public void renameOnlyByLetterCaseKeepsTheProfile() {
        profiles.store(printer("printer"), null);
        assertTrue(profiles.store(printer("Printer"), "printer"));

        // On macOS and Windows both names are the same file; it must survive with the new spelling
        assertEquals(Arrays.asList("Printer.sbp"), files());
        assertEquals("Printer", list.getSelectedItem());
        assertEquals(1, list.getItemCount());
    }

    @Test
    public void findIgnoresLetterCase() {
        profiles.store(printer("Workshop"), null);
        assertEquals("Workshop", profiles.find("WORKSHOP"));
        assertNull(profiles.find("Office"));
    }

    @Test
    public void forgetRemovesTheProfileFromListAndMap() {
        profiles.store(printer("Workshop"), null);
        profiles.forget("Workshop");
        assertEquals(0, list.getItemCount());
        assertFalse(MainMenu.printerMap.containsKey("Workshop"));
    }
}
