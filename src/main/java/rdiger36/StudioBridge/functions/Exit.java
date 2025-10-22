package rdiger36.StudioBridge.functions;

import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import rdiger36.StudioBridge.gui.DialogTwoButtons;
import rdiger36.StudioBridge.gui.MainMenu;

/**
 * The Exit class provides functionality to handle application exit actions,
 * ensuring that unsaved profiles and user settings are properly saved before 
 * the application closes.
 *
 * <p>This class is responsible for managing the application's state and ensuring 
 * that user data is not lost during termination.</p>
 */
public class Exit {

    /**
     * Closes the application after saving unsaved profiles and user settings.
     *
     * <p>This method checks if there are unsaved changes in the profile fields. 
     * If the current profile is new and contains data, it saves the new profile. 
     * If the profile has been modified, it saves those changes. Finally, it saves 
     * the user settings and exits the application.</p>
     *
     * @param mainFrame The main application frame, used for displaying dialogs.
     * @param comboBox The JComboBox containing the list of profiles.
     * @param cbxModel The JComboBox representing the printer model.
     * @param txtIP The JTextField for the printer IP address.
     * @param txtSerial The JTextField for the printer serial number.
     * @param txtName The JTextField for the printer name.
     */
    public static void closeApp(JFrame mainFrame, JComboBox<String> comboBox, JComboBox<String> cbxModel, JTextField txtIP, JTextField txtSerial, JTextField txtName) {
        
    	String importedProfileSaved = checkImportedProfiles(comboBox); 
    	
    	// Check if the selected profile is "New profile" and has unsaved data
        if (comboBox.getSelectedItem().toString().equals("New profile") && 
            (txtIP.getText().length() > 0 || txtSerial.getText().length() > 0 || txtName.getText().length() > 0)) {
            Config.saveUnsavedNewProfile(mainFrame, comboBox, cbxModel, txtIP, txtSerial, txtName);
        } 
        // Check if there are changes in the existing profile
        else if (!txtIP.getText().equals(MainMenu.PrinterIP) || 
                 !txtSerial.getText().equals(MainMenu.PrinterSerial) || 
                 !txtName.getText().equals(MainMenu.PrinterName) || 
                 !cbxModel.getSelectedItem().toString().equals(MainMenu.PrinterType)) {
            Config.saveUnsavedProfile(mainFrame, comboBox, cbxModel, txtIP, txtSerial, txtName);
        } 
        // Check if there is an unsaved imported profile
        else if (importedProfileSaved.equals("oneUnsaved")){
        	Config.saveUnsavedNewProfile(mainFrame, comboBox, cbxModel, txtIP, txtSerial, txtName);	
        }
        // 	Check if there are several unsaved imported profiles
        else if (importedProfileSaved.equals("multipleUnsaved")) {
        	int result = new DialogTwoButtons(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! There are several unsaved imported profiles!<br>Do you want to go back to save them?</html>", "Yes", "No").showDialog();
            if (result == 0) {
                return;
            }
        }
        
        // Save user settings before exiting
        Config.saveUserSettings(mainFrame);
        System.exit(0); // Exit the application
    }

    /**
     * Checks if all imported profiles displayed in the JComboBox are saved
     * and returns an appropriate response.
     *
     * @param comboBox the JComboBox containing profile names.
     * @return "multipleUnsaved" if multiple profiles are not saved,
     *         "oneUnsaved" if exactly one profile is not saved,
     *         or an empty string if all profiles are saved.
     */
    private static String checkImportedProfiles(JComboBox<String> comboBox) {
        
        // Directory where the profiles are stored
        String profDir = MainMenu.ProfilesDir + System.getProperty("file.separator");

        // List to store all profiles except for the special entries
        ArrayList<String> profileList = new ArrayList<>();

        // Iterate through all items in the JComboBox
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            
            // Only add profiles that are not "New profile", "Import profile", or "---"
            if (!item.equals("New profile") && !item.equals("Import profile") && !item.equals("---")) {
                profileList.add(item);
            }
        }

        // Count how many profiles are not saved (i.e., do not have a corresponding .sbp file)
        int unsavedCounter = 0;
        
        for (String profile : profileList) {
            File profileFile = new File(profDir + profile + ".sbp");
            
            if (!profileFile.exists()) {
                unsavedCounter++;
            }
        }

        // Return based on the number of unsaved profiles
        if (unsavedCounter > 1) {
            return "multipleUnsaved";
        } else if (unsavedCounter == 1) {
            return "oneUnsaved";
        }

        // If all profiles are saved, return an empty string
        return "";
    }
}