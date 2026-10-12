package rdiger36.StudioBridge.functions;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import rdiger36.StudioBridge.gui.DialogOneButton;
import rdiger36.StudioBridge.gui.DialogTwoButtons;
import rdiger36.StudioBridge.gui.MainMenu;

/**
 * A utility class for checking and managing software updates for StudioBridge.
 * This class retrieves the latest version of the application from GitHub
 * and prompts the user to update if a new version is available.
 *
 * <p>Beta versions are published as pre-releases on GitHub. They are only offered when
 * "Include beta versions" is ticked in the settings.</p>
 *
 * <p>The installed packages (MSI, AppImage, DMG) download and install the update
 * themselves, see {@link Installer}. The JAR and the portable ZIP open the release page.</p>
 */
public class Update {

    private static final String RELEASES_URL = "https://github.com/Rdiger-36/StudioBridge/releases/latest";
    private static final String RELEASES_PAGE_URL = "https://github.com/Rdiger-36/StudioBridge/releases";
    // The latest release, pre-releases are left out
    private static final String API_URL = "https://api.github.com/repos/Rdiger-36/StudioBridge/releases/latest";
    // All releases, newest first, pre-releases included
    private static final String API_ALL_URL = "https://api.github.com/repos/Rdiger-36/StudioBridge/releases?per_page=30";

    // Matches the first dotted version number with its pre-release part, e.g. "2.1.3" in "Version 2.1.3"
    // or "2.3.0" and "beta.1" in "v.2.3.0-beta.1"
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)*)(?:-([0-9A-Za-z.-]+))?");

    /**
     * Checks if a new version of StudioBridge is available online.
     *
     * <p>With beta versions enabled in the settings, pre-releases count as well.</p>
     *
     * @param mainFrame The parent JFrame used for displaying dialogs, or null for console output.
     * @param prepareExit Offers to save unsaved data and saves the settings before an update closes
     *                    StudioBridge; returns false if the user cancelled. Only used with a mainFrame.
     * @param manual true when the user asked for the check, who then also hears that StudioBridge
     *               is up to date or that the check failed
     */
    public static void checkVersion(JFrame mainFrame, BooleanSupplier prepareExit, boolean manual) {
        JSONObject release = getLatestRelease(MainMenu.betaUpdates);
        String onlineVersion = release == null ? null : versionOf(release);

        if (onlineVersion == null || !isNewer(onlineVersion, MainMenu.version)) {
            if (manual && mainFrame != null) {
                String message = release == null
                        ? I18n.t("update.checkFailed")
                        : I18n.t("update.upToDate", "version", MainMenu.version);
                SwingUtilities.invokeLater(() -> new DialogOneButton(mainFrame, null,
                        new ImageIcon(MainMenu.class.getResource(release == null ? "/achtung.png" : "/success.png")),
                        message, I18n.t("common.ok")).showDialog());
            }
            return;
        }

        boolean preRelease = release.optBoolean("prerelease", false);
        String releaseUrl = release.optString("html_url", preRelease ? RELEASES_PAGE_URL : RELEASES_URL);

        if (mainFrame != null) {
            // Null when this package can not install an update itself
            Installer installer = Installer.forThisPackage(release);

            // Dialogs must be created on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                int response = new DialogTwoButtons(mainFrame, null,
                    new ImageIcon(MainMenu.class.getResource("/achtung.png")),
                    I18n.t("update.available", "version", display(onlineVersion)),
                    I18n.t(installer != null ? "update.install" : "update.openReleases"),
                    I18n.t("update.continue")).showDialog();

                if (response != 0) return;

                if (installer != null) {
                    installer.start(mainFrame, prepareExit);
                } else {
                    openPage(releaseUrl);
                }
            });
        } else {
            System.out.println("Attention! A new update for StudioBridge is available: " + display(onlineVersion) + "\r\n");
            System.out.println(releaseUrl);
            System.out.println();
        }
    }

    /**
     * Opens the page of the latest release in the browser.
     */
    static void openReleasePage() {
        openPage(RELEASES_URL);
    }

    private static void openPage(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
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
     * Compares two version strings part by part. A pre-release is older than its version
     * ("2.3.0-beta.1" before "2.3.0"), pre-releases compare like in Semantic Versioning
     * ("beta.2" before "beta.10" before "rc.1").
     *
     * @param online The version of the latest release, may contain a prefix like "Version " or "v".
     * @param local The version of this build.
     * @return true if the online version is newer than the local one, false otherwise or if a version can not be parsed.
     */
    static boolean isNewer(String online, String local) {
        Matcher onlineMatch = match(online);
        Matcher localMatch = match(local);
        if (onlineMatch == null || localMatch == null) return false;

        int[] onlineParts = numbers(onlineMatch.group(1));
        int[] localParts = numbers(localMatch.group(1));
        if (onlineParts == null || localParts == null) return false;

        for (int i = 0; i < Math.max(onlineParts.length, localParts.length); i++) {
            int o = i < onlineParts.length ? onlineParts[i] : 0;
            int l = i < localParts.length ? localParts[i] : 0;
            if (o != l) return o > l;
        }
        return comparePreRelease(onlineMatch.group(2), localMatch.group(2)) > 0;
    }

    /**
     * Compares the pre-release parts of two equal version numbers.
     *
     * @param a the pre-release part, e.g. "beta.1", or null for a release
     * @param b the pre-release part, or null for a release
     * @return less than, equal to or greater than 0 as a is older than, equal to or newer than b
     */
    private static int comparePreRelease(String a, String b) {
        if (a == null || b == null) return a == null ? (b == null ? 0 : 1) : -1;

        String[] as = a.split("\\.");
        String[] bs = b.split("\\.");
        for (int i = 0; i < Math.min(as.length, bs.length); i++) {
            boolean aNumber = as[i].matches("\\d+");
            boolean bNumber = bs[i].matches("\\d+");
            int result;
            if (aNumber && bNumber) {
                result = Long.compare(Long.parseLong(as[i]), Long.parseLong(bs[i]));
            } else if (aNumber || bNumber) {
                // A number is older than a word
                result = aNumber ? -1 : 1;
            } else {
                result = as[i].compareToIgnoreCase(bs[i]);
            }
            if (result != 0) return result;
        }
        return Integer.compare(as.length, bs.length);
    }

    /**
     * Finds the first version number in the given text.
     *
     * @return The match with the number as group 1 and the pre-release part as group 2, or null if there is none.
     */
    private static Matcher match(String text) {
        if (text == null) return null;
        Matcher matcher = VERSION_PATTERN.matcher(text);
        return matcher.find() ? matcher : null;
    }

    /**
     * The numeric parts of a dotted version number.
     *
     * @return The version parts, or null if a part is too large.
     */
    private static int[] numbers(String version) {
        String[] parts = version.split("\\.");
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
     * The version without prefix, e.g. "2.3.0-beta.1" for the tag "v.2.3.0-beta.1".
     */
    static String display(String version) {
        Matcher matcher = match(version);
        return matcher == null ? version : matcher.group();
    }

    /**
     * The version of a release.
     *
     * @return The tag of the release, or its name if the tag contains no version number.
     */
    static String versionOf(JSONObject release) {
        String tag = release.optString("tag_name", "");
        return match(tag) != null ? tag : release.optString("name", null);
    }

    /**
     * Retrieves the latest release, with its files, from the GitHub API.
     *
     * @param includePreReleases true to take the newest of all releases, pre-releases included
     * @return The release, or null if an error occurs.
     */
    private static JSONObject getLatestRelease(boolean includePreReleases) {
        try {
            String body = get(includePreReleases ? API_ALL_URL : API_URL);
            if (body == null) return null;
            return includePreReleases ? newest(new JSONArray(body)) : new JSONObject(body);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * The release with the highest version, drafts are left out.
     *
     * @return The release, or null if the list has none.
     */
    static JSONObject newest(JSONArray releases) {
        JSONObject newest = null;
        for (int i = 0; i < releases.length(); i++) {
            JSONObject release = releases.optJSONObject(i);
            if (release == null || release.optBoolean("draft", false) || versionOf(release) == null) continue;
            if (newest == null || isNewer(versionOf(release), versionOf(newest))) newest = release;
        }
        return newest;
    }

    /**
     * Sends a GET request to the GitHub API.
     *
     * @return The body of the response, or null if the status is not 200.
     */
    private static String get(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 ? response.body() : null;
    }

}
