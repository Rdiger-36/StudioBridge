package rdiger36.StudioBridge.functions;

import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import rdiger36.StudioBridge.gui.DialogTwoButtons;
import rdiger36.StudioBridge.gui.MainMenu;
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
     * Initiates the application shutdown sequence while ensuring that all
     * unsaved profile data and user settings are correctly saved.
     *
     * <p>The method performs the following checks:</p>
     * <ul>
     *   <li>If the currently selected profile is a new, unsaved profile
     *       containing data, the user is prompted to save it.</li>
     *   <li>If the loaded profile has been modified, the user is prompted
     *       to save the changes.</li>
     *   <li>If imported profiles exist that have not yet been saved to the
     *       filesystem, the user is warned accordingly.</li>
     *   <li>Finally, user settings are saved and the application terminates.</li>
     * </ul>
     *
     * @param mainFrame the main application window used to display dialogs.
     * @param profiles a list of all profile names currently loaded in the UI.
     * @param printer the currently edited {@link Printer} object whose values
     *                may need to be saved before exiting.
     */
    public static void closeApp(JFrame mainFrame, ArrayList<String> profiles, Printer printer) {

        String importedProfileSaved = checkImportedProfiles(profiles);

        // Check if the selected profile is "New profile" and has unsaved data
        if (printer.getPrinterModel().equals("New profile") &&
            (printer.getIpAdress().length() > 0 || printer.getSerialNumber().length() > 0 || printer.getPrinterName().length() > 0)) {

            Config.saveUnsavedNewProfile(mainFrame, printer);
        }

        // Check if there are changes in the existing profile
        else if (MainMenu.mainPrinter != null) {
            if (!printer.getIpAdress().equals(MainMenu.mainPrinter.getIpAdress()) ||
                !printer.getSerialNumber().equals(MainMenu.mainPrinter.getSerialNumber()) ||
                !printer.getPrinterName().equals(MainMenu.mainPrinter.getPrinterName()) ||
                !printer.getPrinterModel().equals(MainMenu.mainPrinter.getPrinterModel())) {

                Config.saveUnsavedNewProfile(mainFrame, printer);
            }
        }

        // Check if there is exactly one unsaved imported profile
        else if (importedProfileSaved.equals("oneUnsaved")) {
            Config.saveUnsavedNewProfile(mainFrame, printer);
        }

        // Check if several imported profiles are unsaved
        else if (importedProfileSaved.equals("multipleUnsaved")) {
            int result = new DialogTwoButtons(
                    mainFrame,
                    null,
                    new ImageIcon(MainMenu.class.getResource("/achtung.png")),
                    "<html>Warning! There are several unsaved imported profiles!<br>Do you want to go back to save them?</html>",
                    "Yes",
                    "No"
            ).showDialog();

            if (result == 0) {
                return; // Abort exit
            }
        }

        // Save user settings before exiting
        Config.saveUserSettings(mainFrame);
        System.exit(0);
    }

    /**
     * Checks whether imported profiles listed in the profile overview
     * exist on disk as .sbp files. Profiles that do not have a corresponding
     * file are considered unsaved.
     *
     * <p>The result indicates how many profiles are unsaved:</p>
     * <ul>
     *   <li>{@code "multipleUnsaved"} – more than one unsaved profile</li>
     *   <li>{@code "oneUnsaved"} – exactly one unsaved profile</li>
     *   <li>empty string – all profiles are saved</li>
     * </ul>
     *
     * @param profiles a list of profile names displayed in the UI.
     * @return a status string describing the number of unsaved profiles.
     */
    private static String checkImportedProfiles(ArrayList<String> profiles) {

        // Directory where profiles are stored
        String profDir = MainMenu.ProfilesDir + System.getProperty("file.separator");

        int unsavedCounter = 0;

        for (String profile : profiles) {
            File profileFile = new File(profDir + profile + ".sbp");

            if (!profileFile.exists()) {
                unsavedCounter++;
            }
        }

        if (unsavedCounter > 1) {
            return "multipleUnsaved";
        } else if (unsavedCounter == 1) {
            return "oneUnsaved";
        }

        return "";
    }
}