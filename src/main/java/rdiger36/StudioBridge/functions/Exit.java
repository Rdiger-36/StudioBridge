package rdiger36.StudioBridge.functions;

import javax.swing.JFrame;

import rdiger36.StudioBridge.gui.MainMenu;
import rdiger36.StudioBridge.gui.ProfileManager;
import rdiger36.StudioBridge.objects.Printer;

/**
 * The {@code Exit} class provides functionality for performing a clean shutdown
 * of the application. Before termination, it ensures that unsaved profiles and
 * user settings are properly handled to prevent data loss.
 *
 * <p>The class checks whether the currently edited or imported profiles contain
 * unsaved changes and triggers the necessary save dialogs if required before
 * the application closes.</p>
 */
public class Exit {

    /**
     * Closes StudioBridge after offering to save what is entered and not saved yet.
     *
     * <p>A new profile with data offers to save it under a name, an edited profile offers to save
     * the changes. Closing such a dialog with the window button keeps StudioBridge open.
     * Finally the settings are saved and the application ends.</p>
     *
     * @param mainFrame the main window, parent of the dialogs
     * @param current the profile name and the values entered right now
     * @param profiles saves the profile and keeps the profile list in step
     */
    public static void closeApp(JFrame mainFrame, Printer current, ProfileManager profiles) {
        switch (unsavedState(current, MainMenu.mainPrinter)) {
            case NEW_PROFILE:
                if (profiles.offerSaveNewOnExit(current) == ProfileManager.Choice.CANCEL) return;
                break;
            case CHANGED_PROFILE:
                if (profiles.offerSaveChangesOnExit(current) == ProfileManager.Choice.CANCEL) return;
                break;
            default:
                break;
        }

        // Save user settings before exiting
        Config.saveUserSettings(mainFrame);
        System.exit(0);
    }

    /** What would be lost when StudioBridge closes now. */
    public enum UnsavedState { NONE, NEW_PROFILE, CHANGED_PROFILE }

    /**
     * Compares what is entered in the main window with the saved state of the selected profile.
     *
     * @param current the profile name and the values entered right now
     * @param saved the state of the selected profile as it was loaded or last saved, or null
     * @return NEW_PROFILE for an unsaved new profile with data, CHANGED_PROFILE for a saved profile
     *         whose values were edited, NONE otherwise
     */
    public static UnsavedState unsavedState(Printer current, Printer saved) {
        if (MainMenu.notAllowedProfileNames.contains(current.getProfileName())) {
            boolean hasData = !current.getIpAdress().trim().isEmpty()
                    || !current.getSerialNumber().trim().isEmpty()
                    || !current.getPrinterName().trim().isEmpty();
            return hasData ? UnsavedState.NEW_PROFILE : UnsavedState.NONE;
        }
        if (saved == null || !current.getProfileName().equals(saved.getProfileName())) {
            return UnsavedState.NONE;
        }
        boolean changed = !current.getIpAdress().equals(saved.getIpAdress())
                || !current.getSerialNumber().equals(saved.getSerialNumber())
                || !current.getPrinterName().equals(saved.getPrinterName())
                // An empty model means the saved one is not in the model list (an old profile),
                // the user can not clear the selection, so it is not a change
                || (!current.getPrinterModel().isEmpty() && !current.getPrinterModel().equals(saved.getPrinterModel()));
        return changed ? UnsavedState.CHANGED_PROFILE : UnsavedState.NONE;
    }

}