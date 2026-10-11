package rdiger36.StudioBridge.functions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import rdiger36.StudioBridge.functions.Exit.UnsavedState;
import rdiger36.StudioBridge.gui.MainMenu;
import rdiger36.StudioBridge.objects.Printer;

/**
 * Tests for the check on exit whether entered data would be lost.
 */
public class ExitTest {

    private static final Printer SAVED = new Printer("Workshop", "X1C", "192.168.1.50", "01S00A000000001", "X1C");

    @Test
    public void newProfileWithDataIsUnsaved() {
        Printer current = new Printer(MainMenu.NEW_PROFILE, "My printer", "", "", "");
        assertEquals(UnsavedState.NEW_PROFILE, Exit.unsavedState(current, null));
    }

    @Test
    public void emptyNewProfileIsNothingToSave() {
        Printer current = new Printer(MainMenu.NEW_PROFILE, " ", "", "", "");
        assertEquals(UnsavedState.NONE, Exit.unsavedState(current, null));
    }

    @Test
    public void editedProfileIsChanged() {
        Printer current = new Printer("Workshop", "X1C", "192.168.1.51", "01S00A000000001", "X1C");
        assertEquals(UnsavedState.CHANGED_PROFILE, Exit.unsavedState(current, SAVED));
        current = new Printer("Workshop", "X1C", "192.168.1.50", "01S00A000000001", "P1S");
        assertEquals(UnsavedState.CHANGED_PROFILE, Exit.unsavedState(current, SAVED));
    }

    @Test
    public void unchangedProfileIsNothingToSave() {
        Printer current = new Printer("Workshop", "X1C", "192.168.1.50", "01S00A000000001", "X1C");
        assertEquals(UnsavedState.NONE, Exit.unsavedState(current, SAVED));
    }

    @Test
    public void profileWithoutSavedStateIsNotCompared() {
        Printer current = new Printer("Other", "X1C", "192.168.1.50", "01S00A000000001", "X1C");
        assertEquals(UnsavedState.NONE, Exit.unsavedState(current, SAVED));
        assertEquals(UnsavedState.NONE, Exit.unsavedState(current, null));
    }

    @Test
    public void modelMissingFromTheListIsNoChange() {
        Printer saved = new Printer("Old", "X1C", "192.168.1.50", "01S00A000000001", "5");
        Printer current = new Printer("Old", "X1C", "192.168.1.50", "01S00A000000001", "");
        assertEquals(UnsavedState.NONE, Exit.unsavedState(current, saved));
    }
}
