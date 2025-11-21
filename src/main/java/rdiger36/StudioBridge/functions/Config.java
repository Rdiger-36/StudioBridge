package rdiger36.StudioBridge.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.formdev.flatlaf.FlatLaf;

import rdiger36.StudioBridge.gui.DialogOneButton;
import rdiger36.StudioBridge.gui.DialogTwoButtons;
import rdiger36.StudioBridge.gui.MainMenu;
import rdiger36.StudioBridge.gui.SaveDialog;
import rdiger36.StudioBridge.objects.Printer;

/**
 * The {@code Config} class provides utility methods for managing application settings
 * and printer profiles. It includes functionality for saving and loading user
 * preferences (e.g., theme, profile directories, last used profile) as well as
 * reading and writing printer profile configuration files.
 *
 * <p>This class serves as a central configuration handler for the application and
 * ensures consistent access to settings and profile data.</p>
 */

public class Config {
	
	/**
	 * Displays a warning dialog if the current printer profile is new and contains
	 * unsaved data. The user may choose to save the profile or continue without saving.
	 *
	 * <p>If the user chooses to save, a dialog is shown to enter a profile name.
	 * The profile is then written to disk and the given {@link Printer} object is
	 * updated accordingly.</p>
	 *
	 * @param mainFrame the window used for dialog positioning.
	 * @param printer the printer object containing the unsaved profile data.
	 * @return the name of the saved profile, or an empty string if the user chose
	 *         not to save.
	 */
	public static String saveUnsavedNewProfile(JFrame mainFrame, Printer printer) {
	    
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
	        String profileName = new SaveDialog(mainFrame, printer.getProfileName(), "Save").saveProfile().trim();
	        
	        // Save the profile with the entered name and associated details (model, IP, serial number), when the profile name is set.
	        if (!profileName.equals("")) {
	        	printer.setProfileName(profileName);
	        	saveProfile(mainFrame, printer);
	        }
	        
	        // Return the name of the saved profile.
	        return profileName;
	    }
	    
	    // If the user chooses to continue without saving, return an empty string.
	    return "";
	}
	
	/**
	 * Displays a dialog when an existing profile contains unsaved changes. The user
	 * may choose to save or continue without saving.
	 *
	 * <p>If the user chooses to save, the changes are written to the profile file.</p>
	 *
	 * @param mainFrame the window used for dialog positioning.
	 * @param printer the printer object representing the modified profile.
	 * @return {@code true} if the profile was saved, {@code false} if the user chose
	 *         not to save.
	 */
	public static boolean saveUnsavedProfile(JFrame mainFrame, Printer printer) {
	    
	    // Display a dialog warning the user that the current profile contains unsaved changes.
	    // The dialog has two buttons: "Save profile" and "Continue without saving".
	    int response = new DialogTwoButtons(mainFrame, null, 
	        new ImageIcon(MainMenu.class.getResource("/achtung.png")), 
	        "<html>Warning! The selected profile contains changes!</html>", 
	        "Save profile", 
	        "Continue without saving").showDialog();
	    
	    // If the user chooses to save the profile (response == 0):
	    if (response == 0) {
	        
	        // Save the profile using the selected name and the provided profile details.
	        saveProfile(mainFrame, printer);
	        
	        // Return true indicating that the profile has been saved.
	        return true;
	    }
	    
	    // If the user chooses to continue without saving, return false.
	    return false;
	}
	
	/**
	 * Writes the contents of a {@link Printer} profile to a configuration file.
	 * The file is stored inside the application's profile directory and saved
	 * using the profile name with ".sbp" as extension.
	 *
	 * @param mainFrame the window used for displaying error dialogs.
	 * @param printer the printer object whose data should be stored.
	 * @return {@code true} if the profile was saved successfully,
	 *         {@code false} if an error occurred.
	 */
	public static boolean saveProfile(JFrame mainFrame, Printer printer) {
	    
	    // Create a StringBuilder to build the profile configuration line by line.
	    StringBuilder config = new StringBuilder();
	    
	    // Add the profile name from the comboBox to the configuration.
	    config.append("ProfileName=" + printer.getProfileName());
	    config.append(System.getProperty("line.separator"));  // Add a new line.
	    
	    // Add the IP address from the txtIP field to the configuration.
	    config.append("IP-Address=" + printer.getIpAdress());
	    config.append(System.getProperty("line.separator"));  // Add a new line.
	    
	    // Add the printer serial number from the txtSerial field to the configuration.
	    config.append("PrinterSN=" + printer.getSerialNumber());
	    config.append(System.getProperty("line.separator"));  // Add a new line.
	    
	    // Add the printer model type (as index) from the cbxModel comboBox to the configuration.
	    config.append("PrinterType=" + printer.getPrinterModel());
	    config.append(System.getProperty("line.separator"));  // Add a new line.
	    
	    // Add the printer name from the txtName field to the configuration.
	    config.append("PrinterName=" + printer.getPrinterName());
	    
	    /** 
	     * Create a FileOutputStream to write the content of the config StringBuilder into a file. 
	     * The file is saved in the profiles directory defined by MainMenu.ProfilesDir, with the profile 
	     * name and ".sbp" file extension.
	     */
	    FileOutputStream fos;
	    try {
	        // Create and open a file at the specified path with the profile name.
	        fos = new FileOutputStream(MainMenu.ProfilesDir + System.getProperty("file.separator") + printer.getProfileName() + ".sbp");
	        
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
	 * Loads the contents of a printer profile file and constructs a {@link Printer}
	 * object based on its data.
	 *
	 * @param mainFrame the frame used for error dialog display.
	 * @param profile the absolute file path of the profile to load.
	 * @return a {@link Printer} object with the loaded data,
	 *         or {@code null} if the file could not be read.
	 */
	public static Printer loadProfile(JFrame mainFrame, String profile) {   
	    
	    // Create a File object representing the configuration file.
	    File configFile = new File(profile);        
	    
	    // Variable to hold each line read from the configuration file.
	    String line = "";
	    
	    // BufferedReader to read the contents of the configuration file.
	    BufferedReader inputStream;
	    
	    // Check if the configuration file exists.
	    if (configFile.exists()) {  
	    	Printer printer = new Printer(configFile.getName().replace(".sbp", ""), "", "", "", "");
	        
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
	                getPrinterConf(line.split("=")[0], line.split("=")[1], printer);
	            }    
	            
	            // Close the BufferedReader after reading all lines.
	            inputStream.close();
	        } catch (Exception e) {
	        	// Show error if reading the file has failed
	        	new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to read the profile file!</html>", "Ok").showDialog();
	        	return null;
	        }
            return printer;
	    } else {
	    	return null;
	    }
	}
	
	/**
	 * Parses a configuration key/value pair and assigns the corresponding value
	 * to the given {@link Printer} object's fields.
	 *
	 * @param configType the configuration key (e.g., "IP-Address").
	 * @param wert the configuration value.
	 * @param printer the printer object that receives the configuration data.
	 */
	private static void getPrinterConf(String configType, String wert, Printer printer) {
	    
	    // Switch statement to assign the value to the correct field based on the configuration type.
	    switch(configType) {
	        
	        // Set the IP address in the txtIP field, limiting the length to 15 characters.
	        case "IP-Address": 
	        	printer.setIpAdress(checkLength(wert, 15));
	        	break;
	        
	        // Set the printer serial number in the txtSerial field, limiting the length to 25 characters.
	        case "PrinterSN": 
	        	printer.setSerialNumber(checkLength(wert, 25));
	        	break;
	        
	        // Set the selected index of the printer model in the cbxModel combo box based on the value.
	        case "PrinterType": 
	        	printer.setPrinterModel(checkLength(wert, 25));
	        	break;
	        
	        // Set the printer name in the txtName field, limiting the length to 25 characters.
	        case "PrinterName": 
	        	printer.setPrinterName(checkLength(wert, 25));
	        	break;
	        
	        // Default case if no known configuration type is found (does nothing).
	        default: 
	        	break;
	    }        
	}


	/**
	 * Ensures that a string does not exceed a maximum length. If the string is
	 * longer than the allowed value, it is truncated.
	 *
	 * @param wert the string to check.
	 * @param length the maximum allowed length.
	 * @return the original string if within limits, otherwise a truncated version.
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
	 * Writes the user's application settings to the "settings" file in the
	 * application configuration directory.
	 *
	 * <p>The following settings are stored:</p>
	 * <ul>
	 *     <li>Dark mode state</li>
	 *     <li>Custom profile directory</li>
	 *     <li>Remember last used profile</li>
	 *     <li>Last used profile name (optional)</li>
	 *     <li>Direct mode flag</li>
	 *     <li>Show JAR info flag</li>
	 * </ul>
	 *
	 * @param mainFrame the frame used to display possible error dialogs.
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
	    
	    // Append the usage of the mode for sending UDP packages to the configuration.
	    config.append("directMode=" + MainMenu.directMode);
	    config.append(System.getProperty("line.separator")); // Add a new line.
	    
	    // Append the usage of the mode to show the jar info to the configuration.
	    config.append("showJarInfo=" + MainMenu.jarInfo);
	    config.append(System.getProperty("line.separator")); // Add a new line.
	    
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
	    	e1.printStackTrace();
        	new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to write the config to the config file!</html>", "Ok").showDialog();
        }    
	}
	
	/**
	 * Loads the application settings from the "settings" file and assigns the
	 * values to the corresponding fields in {@link MainMenu}.
	 *
	 * <p>If the settings file does not exist, no values are loaded.</p>
	 */
	public static void loadAppSettings() {
	    // Define the path to the user settings file.
	    String userSettings = MainMenu.savePath + System.getProperty("file.separator") + "settings";
	    
	    
	    // Check if the settings file exists.
	    if (new File(userSettings).exists()) {
	        try {
	            String line;
	            // Open the settings file for reading using a BufferedReader.
	            try (BufferedReader inputStream = new BufferedReader(new FileReader(userSettings))) {
	                // Read each line of the file.
	                while((line = inputStream.readLine()) != null) {
	                	
	                	getAppConf(line.split("=")[0], line.split("=")[1]);
	                }    
	                // The BufferedReader is automatically closed here due to the try-with-resources statement.
	            }
	        } catch (Exception e) {
	        	// Show error if reading the file has failed
	        	e.printStackTrace();
	        	new DialogOneButton(null, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to read the config file!</html>", "Ok").showDialog();
	        }    
	    }
	}
	
	/**
	 * Assigns a setting key/value pair from the settings file to the appropriate
	 * application field in {@link MainMenu}.
	 *
	 * @param configType the settings key.
	 * @param wert the settings value.
	 */
	private static void getAppConf(String configType, String wert) {
	    
	    // Switch statement to assign the value to the correct field based on the configuration type.
	    switch(configType) {
	        
	    	// Set the enableDarkmode setting
	        case "enableDarkmode": 
	        	MainMenu.darkmode = Boolean.valueOf(wert);
	        	break;
	        
	        // Set the customProfilesPath setting
	        case "customProfilesPath": 
	        	MainMenu.ProfilesDir = wert;
	        	break;
	        
        	// Set the rememberLastUsedProfile setting
	        case "rememberLastUsedProfile": 
	        	MainMenu.rememberLastUsedProfile = Boolean.valueOf(wert);
	        	break;
	        
	        // Set the lastUsedProfile setting
	        case "lastUsedProfile": 
	        	MainMenu.lastUsedProfile = wert;
	        	break;
	        	
	        // Set the directMode setting
	        case "directMode": 
	        	MainMenu.directMode = Boolean.valueOf(wert);
	        	break;
	        	
        	// Set the showJarInfo setting
	        case "showJarInfo": 
	        	MainMenu.jarInfo = Boolean.valueOf(wert);
	        	break;
	        
	        // Default case if no known configuration type is found (does nothing).
	        default: 
	        	break;
	    }        
	}
}