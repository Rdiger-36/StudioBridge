package rdiger36.StudioBridge.functions;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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
    private static final String API_URL = "https://api.github.com/repos/Rdiger-36/StudioBridge/releases/latest";

    // Matches the first dotted version number, e.g. "2.1.3" in "Version 2.1.3" or "v2.1.3-BETA"
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)*)");

    /**
     * Checks if a new version of StudioBridge is available online.
     *
     * @param mainFrame The parent JFrame used for displaying dialogs, or null for console output.
     */
    public static void checkVersion(JFrame mainFrame) {
        // Retrieve only the release version info
        String onlineVersion = getOnlineVersion();

        if (onlineVersion == null || !isNewer(onlineVersion, MainMenu.version)) {
            return;
        }

        if (mainFrame != null) {
            // Dialogs must be created on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
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
            });
        } else {
            System.out.println("Attention! A new update for StudioBridge is available:\r\n");
            System.out.println(RELEASES_URL);
            System.out.println();
        }
    }

    /**
     * Reads the version of this build from the filtered version.properties resource.
     *
     * @return The version, e.g. "2.1.3", or "0.0.0" if it is not available (e.g. when started from the IDE).
     */
    public static String readLocalVersion() {
        try (InputStream in = Update.class.getResourceAsStream("/version.properties")) {
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);
                String version = properties.getProperty("version", "");
                if (VERSION_PATTERN.matcher(version).lookingAt()) {
                    return version;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0.0.0";
    }

    /**
     * Compares two version strings part by part.
     *
     * @param online The version of the latest release, may contain a prefix like "Version " or "v".
     * @param local The version of this build.
     * @return true if the online version is newer than the local one, false otherwise or if a version can not be parsed.
     */
    static boolean isNewer(String online, String local) {
        int[] onlineParts = parse(online);
        int[] localParts = parse(local);
        if (onlineParts == null || localParts == null) return false;

        for (int i = 0; i < Math.max(onlineParts.length, localParts.length); i++) {
            int o = i < onlineParts.length ? onlineParts[i] : 0;
            int l = i < localParts.length ? localParts[i] : 0;
            if (o != l) return o > l;
        }
        return false;
    }

    /**
     * Extracts the numeric parts of the first version number in the given text.
     *
     * @return The version parts, or null if the text contains no version number.
     */
    private static int[] parse(String text) {
        if (text == null) return null;
        Matcher matcher = VERSION_PATTERN.matcher(text);
        if (!matcher.find()) return null;

        String[] parts = matcher.group(1).split("\\.");
        int[] numbers = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                numbers[i] = Integer.parseInt(parts[i]);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return numbers;
    }

    /**
     * Retrieves the latest release version from the GitHub API.
     *
     * @return The tag (or name) of the latest release, or null if an error occurs.
     */
    private static String getOnlineVersion() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                String tag = jsonResponse.optString("tag_name", "");
                return parse(tag) != null ? tag : jsonResponse.optString("name", null);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
