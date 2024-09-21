package rdiger36.StudioBridge;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

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
        // Save user settings before exiting
        Config.saveUserSettings(mainFrame);
        System.exit(0); // Exit the application
    }
}