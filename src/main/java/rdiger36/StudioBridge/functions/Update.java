package rdiger36.StudioBridge.functions;

import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.json.JSONObject;

import rdiger36.StudioBridge.gui.DialogTwoButtons;
import rdiger36.StudioBridge.gui.MainMenu;

/**
 * A utility class for checking and managing software updates for StudioBridge.
 * This class retrieves the latest version of the application from GitHub 
 * and prompts the user to update if a new version is available.
 */
public class Update {

    private static final String RELEASES_URL = "https://github.com/Rdiger-36/StudioBridge/releases/latest";

    /**
     * Checks if a new version of StudioBridge is available online.
     *
     * @param mainFrame The parent JFrame used for displaying dialogs.
     */
    public static void checkVersion(JFrame mainFrame) {
        // Retrieve only the release version info
        String onlineVersion = getOnlineVersion();

        if (onlineVersion != null) {

            String onlineVersionString = onlineVersion.replace("Version ", "")
                                                     .replace(".", "")
                                                     .replace("BETA", "")
                                                     .trim();

            // Convert version strings to integers for comparison
            int online = Integer.parseInt(onlineVersionString);
            int local = Integer.parseInt(MainMenu.version);

            if (local < online) {
                if (mainFrame != null) {
                    // Prompt user with update dialog
                    int response = new DialogTwoButtons(mainFrame, null, 
                        new ImageIcon(MainMenu.class.getResource("/achtung.png")), 
                        "An update for StudioBridge is available!", 
                        "Open releases page", 
                        "Continue without update").showDialog();

                    if (response == 0) {
                        try {
                            Desktop.getDesktop().browse(new URI(RELEASES_URL));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    System.out.println("Attention! A new update for StudioBridge is available:\r\n");
                    System.out.println(RELEASES_URL);
                    System.out.println();
                }
            }
        }
    }

    /**
     * Retrieves the latest release version from the GitHub API.
     *
     * @return The version name of the latest release, or null if an error occurs.
     */
    private static String getOnlineVersion() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/Rdiger-36/StudioBridge/releases/latest"))
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                return jsonResponse.getString("name");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
