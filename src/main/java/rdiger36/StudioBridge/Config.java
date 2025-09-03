package rdiger36.StudioBridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatLaf;

/**
 * The Config class provides utility methods for managing user settings and profiles. 
 * It includes functionalities to save and load user preferences, such as dark mode status 
 * and custom profile paths, as well as to manage profile configurations.
 *
 * <p>This class serves as a central point for handling configuration-related operations 
 * in the application, ensuring that user settings are easily accessible and modifiable.</p>
 *
 * <h2>Methods:</h2>
 * <ul>
 *   <li><code>saveUserSettings()</code> - Saves user settings, including dark mode preference and custom profiles path.</li>
 *   <li><code>lastTheme()</code> - Retrieves the user's last theme preference (dark mode status) from the settings file.</li>
 *   <li><code>customProfilePath()</code> - Retrieves the path for custom profiles from the settings file.</li>
 *   <li><code>saveUnsavedNewProfile(JFrame, JComboBox, JComboBox, JTextField, JTextField, JTextField)</code> - Prompts the user to save a new profile if it has unsaved changes.</li>
 *   <li><code>saveUnsavedProfile(JFrame, JComboBox, JComboBox, JTextField, JTextField, JTextField)</code> - Prompts the user to save changes to the selected profile.</li>
 *   <li><code>saveProfile(String, JComboBox, JComboBox, JTextField, JTextField, JTextField)</code> - Saves a profile with the provided settings to a file.</li>
 *   <li><code>loadProfile(String, JComboBox, JTextField, JTextField, JTextField)</code> - Loads a profile's settings from a specified file.</li>
 *   <li><code>get_Conf(String, String, JComboBox, JTextField, JTextField, JTextField)</code> - Assigns configuration values to their respective variables based on the provided key.</li>
 *   <li><code>checkLength(String, int)</code> - Ensures that a given string does not exceed a specified length.</li>
 * </ul>
 */
public class Config {
	
	/**
	 * Displays a warning dialog if a new profile has not been saved yet and provides 
	 * options to either save the profile or continue without saving. If the user 
	 * chooses to save the profile, it invokes the save process and returns the 
	 * profile name. If the user opts to continue without saving, it returns an empty string.
	 *
	 * @param mainFrame The main application window (JFrame) to which the dialog will be attached.
	 * @param comboBox The combo box containing profile options.
	 * @param cbxModel The combo box containing the model options.
	 * @param txtIP The text field containing the IP address for the profile.
	 * @param txtSerial The text field containing the serial number for the profile.
	 * @param txtName The text field containing the name of the profile.
	 * @return The name of the saved profile, or an empty string if the profile is not saved.
	 */
	public static String saveUnsavedNewProfile(JFrame mainFrame, JComboBox<String> comboBox, JComboBox<String> cbxModel, JTextField txtIP, JTextField txtSerial, JTextField txtName) {
	    
	    // Display a dialog that warns the user about unsaved profile changes.
	    // The dialog has two buttons: "Save new profile" and "Continue without saving".
	    // The user's choice is stored in the variable 'response'.
	    int response = new DialogTwoButtons(mainFrame, null, 
	        new ImageIcon(MainMenu.class.getResource("/achtung.png")), 
	        "<html>Warning! The new profile has not been saved yet!</html>", 
	        "Save new profile", 
	        "Continue without saving").showDialog();
	    
	    // If the user chooses to save the profile (response == 0):
	    if (response == 0) {
	        // Get the profile name by showing a save dialog.
	        // The text from the 'txtName' field is used as the initial suggestion for the profile name.
	        String profileName = new SaveDialog(mainFrame, txtName.getText(), "Save").saveProfile().trim();
	        
	        // Save the profile with the entered name and associated details (model, IP, serial number), when the profile name is set.
	        if (!profileName.equals("")) saveProfile(mainFrame, profileName, comboBox, cbxModel, txtIP, txtSerial, txtName);
	        
	        // Return the name of the saved profile.
	        return profileName;
	    }
	    
	    // If the user chooses to continue without saving, return an empty string.
	    return "";
	}
	
	/**
	 * Displays a warning dialog if the selected profile contains unsaved changes and 
	 * provides options to either save the profile or continue without saving. If the 
	 * user chooses to save the profile, it invokes the save process and returns true. 
	 * If the user opts to continue without saving, it returns false.
	 *
	 * @param mainFrame The main application window (JFrame) to which the dialog will be attached.
	 * @param comboBox The combo box containing profile options.
	 * @param cbxModel The combo box containing the model options.
	 * @param txtIP The text field containing the IP address for the profile.
	 * @param txtSerial The text field containing the serial number for the profile.
	 * @param txtName The text field containing the name of the profile.
	 * @return {@code true} if the profile is saved, {@code false} if the user continues without saving.
	 */
	public static boolean saveUnsavedProfile(JFrame mainFrame, JComboBox<String> comboBox, JComboBox<String> cbxModel, JTextField txtIP, JTextField txtSerial, JTextField txtName) {
	    
	    // Display a dialog warning the user that the current profile contains unsaved changes.
	    // The dialog has two buttons: "Save profile" and "Continue without saving".
	    int response = new DialogTwoButtons(mainFrame, null, 
	        new ImageIcon(MainMenu.class.getResource("/achtung.png")), 
	        "<html>Warning! The selected profile contains changes!</html>", 
	        "Save profile", 
	        "Continue without saving").showDialog();
	    
	    // If the user chooses to save the profile (response == 0):
	    if (response == 0) {
	        // Get the name of the selected profile from the comboBox.
	        String profielName = comboBox.getSelectedItem().toString();
	        
	        // Save the profile using the selected name and the provided profile details.
	        saveProfile(mainFrame, profielName, comboBox, cbxModel, txtIP, txtSerial, txtName);
	        
	        // Return true indicating that the profile has been saved.
	        return true;
	    }
	    
	    // If the user chooses to continue without saving, return false.
	    return false;
	}
	
	/**
	 * Saves the profile information (profile name, IP address, printer serial number, printer type, and printer name) 
	 * into a configuration file. The file is saved in the profiles directory with the given profile name and a ".sbp" 
	 * file extension.
	 *
	 * @param ProfileName The name of the profile to be saved.
	 * @param comboBox The combo box containing the selected profile.
	 * @param cbxModel The combo box containing the model selection (used to save the printer type).
	 * @param txtIP The text field containing the IP address of the profile.
	 * @param txtSerial The text field containing the serial number of the printer.
	 * @param txtName The text field containing the name of the printer.
	 * @return {@code true} if the profile is successfully saved, {@code false} if an error occurs during the save process.
	 */
	public static boolean saveProfile(JFrame mainFrame, String ProfileName, JComboBox<String> comboBox, JComboBox<String> cbxModel, JTextField txtIP, JTextField txtSerial, JTextField txtName) {
	    
	    // Create a StringBuilder to build the profile configuration line by line.
	    StringBuilder config = new StringBuilder();
	    
	    // Add the profile name from the comboBox to the configuration.
	    config.append("ProfileName=" + comboBox.getSelectedItem());
	    config.append(System.getProperty("line.separator"));  // Add a new line.
	    
	    // Add the IP address from the txtIP field to the configuration.
	    config.append("IP-Address=" + txtIP.getText());
	    config.append(System.getProperty("line.separator"));  // Add a new line.
	    
	    // Add the printer serial number from the txtSerial field to the configuration.
	    config.append("PrinterSN=" + txtSerial.getText());
	    config.append(System.getProperty("line.separator"));  // Add a new line.
	    
	    // Add the printer model type (as index) from the cbxModel comboBox to the configuration.
	    config.append("PrinterType=" + cbxModel.getSelectedItem());
	    config.append(System.getProperty("line.separator"));  // Add a new line.
	    
	    // Add the printer name from the txtName field to the configuration.
	    config.append("PrinterName=" + txtName.getText());
	    
	    /** 
	     * Create a FileOutputStream to write the content of the config StringBuilder into a file. 
	     * The file is saved in the profiles directory defined by MainMenu.ProfilesDir, with the profile 
	     * name and ".sbp" file extension.
	     */
	    FileOutputStream fos;
	    try {
	        // Create and open a file at the specified path with the profile name.
	        fos = new FileOutputStream(MainMenu.ProfilesDir + System.getProperty("file.separator") + ProfileName + ".sbp");
	        
	        // Write the content of the config to the file as bytes.
	        fos.write(config.toString().getBytes());
	        
	        // Close the file stream after writing.
	        fos.close();
	    } catch (Exception e1) {
	    	// Show error if saving the file has failed
        	new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to save the profile!</html>", "Ok").showDialog();
        	return false;
	    }
	    
	    // Return true if the profile was successfully saved.
	    return true;
	}
	
	/**
	 * Loads a specified profile from a configuration file and populates the relevant UI components 
	 * (model combo box, IP address text field, serial number text field, and printer name text field) 
	 * with the values from the file.
	 *
	 * @param profile The name of the profile to be loaded.
	 * @param cbxModel The combo box for selecting the printer model.
	 * @param txtIP The text field for entering the IP address of the printer.
	 * @param txtSerial The text field for entering the serial number of the printer.
	 * @param txtName The text field for entering the name of the printer.
	 */
	public static void loadProfile(JFrame mainFrame, String profile, JComboBox<String> cbxModel, JTextField txtIP, JTextField txtSerial, JTextField txtName) {   
	    
	    // Create a File object representing the configuration file.
	    File configFile = new File(profile);        
	    
	    // Variable to hold each line read from the configuration file.
	    String line = "";
	    
	    // BufferedReader to read the contents of the configuration file.
	    BufferedReader inputStream;
	    
	    // Check if the configuration file exists.
	    if (!configFile.exists()) {
	        // If the file does not exist, clear the input fields.
	        txtIP.setText("");                     // Clear the IP address field.
	        txtSerial.setText("");                 // Clear the serial number field.
	        cbxModel.setSelectedIndex(0);          // Set the model combo box to the first item.
	        txtName.setText("");                   // Clear the printer name field.
	        return;                                // Exit the method.
	    } else {    
	        
	        try {
	            // Open the configuration file for reading.
	            inputStream = new BufferedReader(new FileReader(configFile));
	            
	            // Read each line of the file until the end.
	            while((line = inputStream.readLine()) != null) {
	                
	                /**
	                 * Use the get_Conf() method to assign the values from the configuration file 
	                 * to the appropriate variables (combo box and text fields). 
	                 * Each line is split into a key-value pair, where the key is on the left side of '='
	                 * and the value on the right.
	                 */
	                get_Conf(line.split("=")[0], line.split("=")[1], cbxModel, txtIP, txtSerial, txtName);
	            }    
	            
	            // Close the BufferedReader after reading all lines.
	            inputStream.close();
	        } catch (Exception e) {
	        	// Show error if reading the file has failed
	        	new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to read the profile file!</html>", "Ok").showDialog();
	        	return;
	        }
	    }
	}
	
	/**
	 * Assigns the given configuration type and corresponding value to the appropriate UI component 
	 * (IP address field, serial number field, model combo box, or printer name field) based on the configuration type.
	 *
	 * @param configType The type of configuration being processed (e.g., "IP-Address", "PrinterSN", etc.).
	 * @param wert The value associated with the configuration type (e.g., the actual IP address, serial number, etc.).
	 * @param cbxModel The combo box for selecting the printer model.
	 * @param txtIP The text field for entering the IP address of the printer.
	 * @param txtSerial The text field for entering the serial number of the printer.
	 * @param txtName The text field for entering the name of the printer.
	 */
	private static void get_Conf(String configType, String wert, JComboBox<String> cbxModel, JTextField txtIP, JTextField txtSerial, JTextField txtName) {
	    
	    // Switch statement to assign the value to the correct field based on the configuration type.
	    switch(configType) {
	        
	        // Set the IP address in the txtIP field, limiting the length to 15 characters.
	        case "IP-Address": 
	            txtIP.setText(checkLength(wert, 15)); 
	            break;
	        
	        // Set the printer serial number in the txtSerial field, limiting the length to 25 characters.
	        case "PrinterSN": 
	            txtSerial.setText(checkLength(wert, 25)); 
	            break;
	        
	        // Set the selected index of the printer model in the cbxModel combo box based on the value.
	        case "PrinterType": 
	            cbxModel.setSelectedItem(wert); 
	            break;
	        
	        // Set the printer name in the txtName field, limiting the length to 25 characters.
	        case "PrinterName": 
	            txtName.setText(checkLength(wert, 25)); 
	            break;
	        
	        // Default case if no known configuration type is found (does nothing).
	        default: 
	            break;
	    }        
	}


	/**
	 * Ensures that the given string does not exceed a specified length. 
	 * If the string is longer than the specified length, it is truncated.
	 *
	 * @param wert The string to be checked and possibly truncated.
	 * @param length The maximum allowed length for the string.
	 * @return The original string if it is within the length limit, or a truncated version if it exceeds the limit.
	 */
	private static String checkLength(String wert, int length) {
	    
	    // If the length of the string exceeds the specified maximum, truncate it.
	    if (wert.length() > length) {
	        wert = wert.substring(0, length);
	    }
	    
	    // Return the original or truncated string.
	    return wert;
	}

	
	/**
	 * Saves user settings, including the dark mode preference and the path for custom profiles, 
	 * to a configuration file. Each setting is written to a new line in the file.
	 *
	 * The settings are stored in a file named "settings" at the location specified by the savePath 
	 * in the MainMenu class.
	 */
	public static void saveUserSettings(JFrame mainFrame) {
	    // Create a StringBuilder to construct the configuration settings line by line.
	    StringBuilder config = new StringBuilder();
	    
	    // Append the dark mode setting to the configuration.
	    config.append("enableDarkmode=" + FlatLaf.isLafDark());
	    config.append(System.getProperty("line.separator")); // Add a new line.
	    
	    // Append the custom profiles path to the configuration.
	    config.append("customProfilesPath=" + MainMenu.ProfilesDir);
	    config.append(System.getProperty("line.separator")); // Add a new line.
	    	    
	    // Append the usage of the last used profile to the configuration.
	    config.append("rememberLastUsedProfile=" + MainMenu.rememberLastUsedProfile);
	    config.append(System.getProperty("line.separator")); // Add a new line.
	    
	    // If the option to remember the last used profile is enabled, store the name of the profile to the configuration
	    if (MainMenu.rememberLastUsedProfile) {
		    
		    // Append the last used profile to the configuration.
		    config.append("lastUsedProfile=" + MainMenu.lastUsedProfile);
		    config.append(System.getProperty("line.separator")); // Add a new line.
	    }
	    
	    /** 
	     * FileOutputStream to create and write the contents of the StringBuilder to 
	     * a file at the specified save path.
	     */
	    FileOutputStream fos;
	    try {
	        // Create a FileOutputStream for the settings file.
	        fos = new FileOutputStream(MainMenu.savePath + System.getProperty("file.separator") + "settings");
	        // Write the configuration settings to the file.
	        fos.write(config.toString().getBytes());
	        // Close the output stream.
	        fos.close();
	    } catch (Exception e1) {
	    	// Show error if saving the file has failed
        	new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to write the config to the config file!</html>", "Ok").showDialog();
        }    
	}
	
	/**
	 * Checks the user's saved settings to determine if dark mode is enabled. 
	 * The method reads the settings file and returns the value of the 
	 * "enableDarkmode" setting.
	 *
	 * @return true if dark mode is enabled, false otherwise.
	 */
	public static boolean lastTheme(JFrame mainFrame) {
	    // Define the path to the user settings file.
	    String userSettings = MainMenu.savePath + System.getProperty("file.separator") + "settings";
	    
	    // Initialize darkmode flag to false.
	    boolean darkmode = false;
	    
	    // Check if the settings file exists.
	    if (new File(userSettings).exists()) {
	        try {
	            String line;
	            // Open the settings file for reading using a BufferedReader.
	            try (BufferedReader inputStream = new BufferedReader(new FileReader(userSettings))) {
	                // Read each line of the file.
	                while((line = inputStream.readLine()) != null) {
	                    // Check if the line corresponds to the dark mode setting.
	                    if (line.split("=")[0].equals("enableDarkmode")) {                
	                        // Parse the value and set the darkmode flag accordingly.
	                        darkmode = Boolean.valueOf(line.split("=")[1]);
	                    }
	                }    
	                // The BufferedReader is automatically closed here due to the try-with-resources statement.
	            }
	        } catch (Exception e) {
	        	// Show error if reading the file has failed
	        	new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to read the config file!</html>", "Ok").showDialog();
	        }    
	    }
	    // Return the dark mode status.
	    return darkmode;
	}

	/**
	 * Checks the user's saved settings to determine if the option to
	 * remember the last used profile is enabled. 
	 * The method reads the settings file and returns the value of the 
	 * "rememberLastUsedProfile" setting.
	 *
	 * @return true if the remember option is enabled, false otherwise.
	 */
	public static boolean rememberLastProfile(JFrame mainFrame) {
	    // Define the path to the user settings file.
	    String userSettings = MainMenu.savePath + System.getProperty("file.separator") + "settings";
	    
	    // Initialize remember flag to false.
	    boolean remember = false;
	    
	    // Check if the settings file exists.
	    if (new File(userSettings).exists()) {
	        try {
	            String line;
	            // Open the settings file for reading using a BufferedReader.
	            try (BufferedReader inputStream = new BufferedReader(new FileReader(userSettings))) {
	                // Read each line of the file.
	                while((line = inputStream.readLine()) != null) {
	                    // Check if the line corresponds to the remember setting.
	                    if (line.split("=")[0].equals("rememberLastUsedProfile")) {                
	                        // Parse the value and set the darkmode flag accordingly.
	                    	remember = Boolean.valueOf(line.split("=")[1]);
	                    }
	                }    
	                // The BufferedReader is automatically closed here due to the try-with-resources statement.
	            }
	        } catch (Exception e) {
	        	// Show error if reading the file has failed
	        	new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to read the config file!</html>", "Ok").showDialog();
	        }    
	    }
	    // Return the remember status.
	    return remember;
	}
	
	/**
	 * Retrieves the custom profiles path from the user's settings file. 
	 * If the settings file exists and contains a valid custom profiles path,
	 * that path is returned. Otherwise, a default profiles path is returned.
	 *
	 * @return The custom profiles path as a string.
	 */
	public static String customProfilePath(JFrame mainFrame) {
	    // Define the path to the user settings file.
	    String userSettings = MainMenu.savePath + System.getProperty("file.separator") + "settings";
	    
	    // Initialize the custom profile path to the default location.
	    String customProfilePath = MainMenu.savePath + System.getProperty("file.separator") + "Profiles";
	    
	    // Check if the settings file exists.
	    if (new File(userSettings).exists()) {
	        try {
	            String line;
	            // Open the settings file for reading using a BufferedReader.
	            try (BufferedReader inputStream = new BufferedReader(new FileReader(userSettings))) {
	                // Read each line of the file.
	                while((line = inputStream.readLine()) != null) {
	                    // Check if the line corresponds to the custom profiles path setting.
	                    if (line.split("=")[0].equals("customProfilesPath")) {                
	                        // Update the custom profile path with the value from the settings file.
	                        customProfilePath = line.split("=")[1];
	                    }
	                }    
	                // The BufferedReader is automatically closed here.
	            }
	        } catch (Exception e) {
	            // Show error if reading the file has failed
	        	if (mainFrame != null) new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to read the config file!</html>", "Ok").showDialog();
	        }    
	    }
	    // Return the custom profile path.
	    return customProfilePath;
	}

	/**
	 * Retrieves the last used profile name from the user's settings file. 
	 * If the settings file exists and contains a valid profile name,
	 * that name is returned.
	 *
	 * @return The last used profile name as a string.
	 */
	public static String lastUsedProfile(JFrame mainFrame) {
	    // Define the path to the user settings file.
	    String userSettings = MainMenu.savePath + System.getProperty("file.separator") + "settings";
	    
	    // Initialize the profile string.
	    String profile = "";
	    
	    // Check if the settings file exists.
	    if (new File(userSettings).exists()) {
	        try {
	            String line;
	            // Open the settings file for reading using a BufferedReader.
	            try (BufferedReader inputStream = new BufferedReader(new FileReader(userSettings))) {
	                // Read each line of the file.
	                while((line = inputStream.readLine()) != null) {
	                    // Check if the line corresponds to the last used profile name setting.
	                    if (line.split("=")[0].equals("lastUsedProfile")) {                
	                        // Set the last used profile with the value from the settings file.
	                    	if (!line.split("=")[1].equals("Import profile")) profile = line.split("=")[1];
	                    }
	                }    
	                // The BufferedReader is automatically closed here.
	            }
	        } catch (Exception e) {
	            // Show error if reading the file has failed
	        	new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to read the config file!</html>", "Ok").showDialog();
	        }    
	    }
	    // Return the custom profile path.
	    return profile;
	}	
}