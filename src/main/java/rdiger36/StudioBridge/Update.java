package rdiger36.StudioBridge;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.json.JSONObject;

/**
 * A utility class for checking and managing software updates for StudioBridge.
 * This class retrieves the latest version of the application from GitHub 
 * and prompts the user to update if a new version is available.
 */
public class Update {

    /**
     * Checks if a new version of StudioBridge is available online.
     *
     * @param mainFrame The parent JFrame used for displaying dialogs.
     */
    public static void checkVersion(JFrame mainFrame) {
        // Retrieve and process the online version string
        String onlineVersionString = getVersion().replace("Version ", "")
                                                  .replace(".", "")
                                                  .replace("BETA", "")
                                                  .trim();
        
        // If the online source is not available, skip the update process
        if (!onlineVersionString.equals("error")) {
        
	        // Convert version strings to integers for comparison
	        int onlineVersion = Integer.parseInt(onlineVersionString);
	        int localVersion = Integer.parseInt(MainMenu.version);
	        
	        // Check if the local version is less than the online version
	        if (localVersion < onlineVersion) {
	            // Prompt user with update dialog
	            int response = new DialogTwoButtons(mainFrame, null, 
	                new ImageIcon(MainMenu.class.getResource("/achtung.png")), 
	                "An update for StudioBridge is available!", 
	                "Download", 
	                "Continue without update").showDialog();
	            
	            // If user chooses to download, open the download link
	            if (response == 0) {
	                try {
	                    Desktop.getDesktop().browse(new URI("https://github.com/Rdiger-36/StudioBridge/releases/download/latest/StudioBridge.jar"));
	                } catch (Exception e) {
	                    // Print stack trace if an error occurs while opening the URL
	                    e.printStackTrace();
	                }
	                System.exit(0); // Exit the application after opening the download link
	            }
	        }
	    }
    }

    /**
     * Retrieves the latest release version name from the GitHub API.
     *
     * @return The release version name or "error" if an exception occurs.
     */
    private static String getVersion() {
        try {
            // Create a URL object pointing to the GitHub API releases endpoint
            @SuppressWarnings("deprecation") //This variant must be used so that the project is compatible with Java 1.8
            URL url = new URL("https://api.github.com/repos/Rdiger-36/StudioBridge/releases");
            
            // Establish HTTPS connection
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            
            // Check response code
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) { // Successful request
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                
                // Read the response from the input stream
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                
                // Close the streams
                in.close();
                conn.disconnect();
                
                // Clean up the response content
                content.delete(0, 1); // Remove the first character (often a bracket)
                content.delete(content.length() - 1, content.length()); // Remove the last character (often a bracket)
                
                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(content.toString());
                String releaseName = jsonResponse.getString("name"); // Get the release name
                return releaseName; // Return the release name
            } else {
                return "error"; // Return error if the response code is not 200
            }
        } catch (Exception e) {
            return "error"; // Return error if an exception occurs
        }
    }
}