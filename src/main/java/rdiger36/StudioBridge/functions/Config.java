package rdiger36.StudioBridge.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import rdiger36.StudioBridge.gui.DialogOneButton;
import rdiger36.StudioBridge.gui.MainMenu;
import rdiger36.StudioBridge.gui.UI;
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
	    // Refuse names that can not be used as a file name
	    if (!isValidProfileName(printer.getProfileName())) {
	    	new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("profile.invalidName"), I18n.t("common.ok")).showDialog();
	    	return false;
	    }
	    
	    // Create and open a file at the specified path with the profile name, it is closed automatically.
	    try (OutputStream fos = new FileOutputStream(MainMenu.ProfilesDir + System.getProperty("file.separator") + printer.getProfileName() + ".sbp")) {
	        
	        // Write the content of the config to the file as bytes.
	        fos.write(config.toString().getBytes(StandardCharsets.UTF_8));
	    } catch (Exception e1) {
	    	// Show error if saving the file has failed
        	new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("profile.saveFailed"), I18n.t("common.ok")).showDialog();
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
	    File configFile = new File(profile);
	    if (!configFile.exists()) return null;
	    try {
	        return readProfile(configFile);
	    } catch (IOException e) {
	        // Show error if reading the file has failed
	        new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("profile.readFailed"), I18n.t("common.ok")).showDialog();
	        return null;
	    }
	}
	
	/**
	 * Reads a profile file without showing anything, for the GUI and the command line alike.
	 * The profile name is the file name without ".sbp"; every line is a key=value pair.
	 *
	 * @param configFile the profile file
	 * @return the profile
	 * @throws IOException if the file can not be read
	 */
	public static Printer readProfile(File configFile) throws IOException {
	    Printer printer = new Printer(configFile.getName().replaceFirst("(?i)\\.sbp$", ""), "", "", "", "");
	    try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8))) {
	        String line;
	        while ((line = inputStream.readLine()) != null) {
	            String[] parts = line.split("=", 2);
	            getPrinterConf(parts[0], parts.length > 1 ? parts[1] : "", printer);
	        }
	    }
	    return printer;
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
	 *     <li>Language</li>
	 *     <li>Beta updates flag</li>
	 * </ul>
	 *
	 * @param mainFrame the frame used to display possible error dialogs.
	 */
	public static void saveUserSettings(JFrame mainFrame) {
	    // Create a StringBuilder to construct the configuration settings line by line.
	    StringBuilder config = new StringBuilder();
	    
	    // Append the dark mode setting to the configuration.
	    config.append("theme=" + MainMenu.theme);
	    config.append(System.getProperty("line.separator")); // Add a new line.
	    
	    // Append the custom profiles path to the configuration.
	    // A directory given by --customProfilesDir only applies to that run, keep the saved one
	    config.append("customProfilesPath=" + (MainMenu.useCustomProfilesDir ? MainMenu.savedProfilesDir : MainMenu.ProfilesDir));
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
	    // A mode given by --direct or --broadcast only applies to that run, keep the saved one
	    config.append("directMode=" + (MainMenu.directModeFromArgs ? MainMenu.savedDirectMode : MainMenu.directMode));
	    config.append(System.getProperty("line.separator")); // Add a new line.
	    
	    // Append the usage of the mode to show the jar info to the configuration.
	    config.append("showJarInfo=" + MainMenu.jarInfo);
	    config.append(System.getProperty("line.separator")); // Add a new line.
	    
	    // Append the language of the GUI ("auto" follows the operating system) to the configuration.
	    config.append("language=" + MainMenu.language);
	    config.append(System.getProperty("line.separator")); // Add a new line.
	    
	    // Append whether beta versions (pre-releases) are offered as updates to the configuration.
	    config.append("betaUpdates=" + MainMenu.betaUpdates);
	    config.append(System.getProperty("line.separator")); // Add a new line.
	    
	    /** 
	     * FileOutputStream to create and write the contents of the StringBuilder to 
	     * a file at the specified save path.
	     */
	    try (OutputStream fos = new FileOutputStream(MainMenu.savePath + System.getProperty("file.separator") + "settings")) {
	        // Write the configuration settings to the file.
	        fos.write(config.toString().getBytes(StandardCharsets.UTF_8));
	    } catch (Exception e1) {
	    	// Show error if saving the file has failed
	    	e1.printStackTrace();
        	new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("settings.writeFailed"), I18n.t("common.ok")).showDialog();
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
	            try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(userSettings), StandardCharsets.UTF_8))) {
	                // Read each line of the file.
	                while((line = inputStream.readLine()) != null) {
	                	
	                	String[] parts = line.split("=", 2);
	                	getAppConf(parts[0], parts.length > 1 ? parts[1] : "");
	                }    
	                // The BufferedReader is automatically closed here due to the try-with-resources statement.
	            }
	        } catch (Exception e) {
	        	// Show error if reading the file has failed
	        	e.printStackTrace();
	        	new DialogOneButton(null, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("settings.readFailed"), I18n.t("common.ok")).showDialog();
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
	// Whether the settings file has a theme entry, which wins over the older enableDarkmode
	private static boolean themeLoaded = false;
	
	private static void getAppConf(String configType, String wert) {
	    
	    // Switch statement to assign the value to the correct field based on the configuration type.
	    switch(configType) {
	        
	    	// Set the enableDarkmode setting
	        case "enableDarkmode": 
	        	// Setting of older versions, kept as fixed choice until a theme is stored
	        	if (!themeLoaded) MainMenu.theme = Boolean.valueOf(wert) ? UI.THEME_DARK : UI.THEME_LIGHT;
	        	break;
	        
	        // Set the theme: auto, light or dark
	        case "theme":
	        	MainMenu.theme = wert;
	        	themeLoaded = true;
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
	        
	        // Set the language of the GUI
	        case "language":
	        	MainMenu.language = wert;
	        	break;
	        
	        // Set whether beta versions are offered as updates
	        case "betaUpdates":
	        	MainMenu.betaUpdates = Boolean.valueOf(wert);
	        	break;
	        
	        // Default case if no known configuration type is found (does nothing).
	        default: 
	        	break;
	    }        
	}
	
	/**
	 * Checks whether a profile name can be used as file name for a profile.
	 *
	 * @param name the profile name to check.
	 * @return true if the name is not empty, not reserved and contains no path or special characters.
	 */
	public static boolean isValidProfileName(String name) {
		if (name == null || name.trim().isEmpty() || !name.equals(name.trim())) return false;
		if (MainMenu.notAllowedProfileNames.contains(name)) return false;
		if (name.startsWith(".")) return false;
		for (char c : name.toCharArray()) {
			if (c < 32 || "\\/:*?\"<>|".indexOf(c) >= 0) return false;
		}
		return true;
	}
}