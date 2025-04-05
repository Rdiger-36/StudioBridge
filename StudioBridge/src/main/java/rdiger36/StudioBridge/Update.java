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
        // Retrieve the release info (version and browser download URL)
        ReleaseInfo releaseInfo = getReleaseInfo();

        if (releaseInfo != null) {
            // Extract the version string from the release info
            String onlineVersionString = releaseInfo.getVersion().replace("Version ", "")
                                                               .replace(".", "")
                                                               .replace("BETA", "")
                                                               .trim();

            // Convert version strings to integers for comparison
            int onlineVersion = Integer.parseInt(onlineVersionString);
            int localVersion = Integer.parseInt(MainMenu.version);

            // Check if the local version is outdated
            if (localVersion < onlineVersion) {
                // Prompt user with update dialog
                int response = new DialogTwoButtons(mainFrame, null, 
                    new ImageIcon(MainMenu.class.getResource("/achtung.png")), 
                    "An update for StudioBridge is available!", 
                    "Download", 
                    "Continue without update").showDialog();
                
                // If the user chooses to download, open the browser download URL
                if (response == 0) {
                    try {
                        Desktop.getDesktop().browse(new URI(releaseInfo.getBrowserDownloadUrl()));
                    } catch (Exception e) {
                        e.printStackTrace(); // Print stack trace if an error occurs while opening the URL
                    }
                    System.exit(0); // Exit the application after opening the download link
                }
            }
        }
    }

    /**
     * Retrieves the latest release information (version and browser download URL) from the GitHub API.
     *
     * @return A ReleaseInfo object containing the version and browser download URL, or null if an error occurs.
     */
    private static ReleaseInfo getReleaseInfo() {
        try {
            // Create a URL object pointing to the GitHub API releases endpoint
            URL url = new URL("https://api.github.com/repos/Rdiger-36/StudioBridge/releases/latest");
            
            // Establish HTTPS connection
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            
            // Check the response code
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) { // Successful request
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;

                // Read the response line by line
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                // Close the streams
                in.close();
                conn.disconnect();
                
                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(content.toString());
                String releaseName = jsonResponse.getString("name"); // Extract the release name
                
                // Extract the browser download URL from the assets array
                String browserDownloadUrl = null;
                if (jsonResponse.has("assets")) {
                    for (Object assetObj : jsonResponse.getJSONArray("assets")) {
                        JSONObject asset = (JSONObject) assetObj;
                        if (asset.has("browser_download_url")) {
                            browserDownloadUrl = asset.getString("browser_download_url");
                            break; // Use the first download URL found
                        }
                    }
                }

                // Validate that browserDownloadUrl was found
                if (browserDownloadUrl == null) {
                    throw new Exception("No browser_download_url found in the response.");
                }

                // Return the parsed release info
                return new ReleaseInfo(releaseName, browserDownloadUrl);
            } else {
                return null; // Return null if the response code is not 200
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log any exceptions
            return null; // Return null if an exception occurs
        }
    }
}

/**
 * A simple class to encapsulate release information from the GitHub API.
 */
class ReleaseInfo {
    private final String version; // The version name of the release
    private final String browserDownloadUrl; // The browser download URL for the release

    /**
     * Constructs a ReleaseInfo object with the specified version and download URL.
     *
     * @param version The version name of the release.
     * @param browserDownloadUrl The browser download URL for the release.
     */
    public ReleaseInfo(String version, String browserDownloadUrl) {
        this.version = version;
        this.browserDownloadUrl = browserDownloadUrl;
    }

    /**
     * Gets the version name of the release.
     *
     * @return The version name.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the browser download URL for the release.
     *
     * @return The browser download URL.
     */
    public String getBrowserDownloadUrl() {
        return browserDownloadUrl;
    }
}
