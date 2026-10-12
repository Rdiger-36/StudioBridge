package rdiger36.StudioBridge.functions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.BooleanSupplier;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.json.JSONArray;
import org.json.JSONObject;

import net.miginfocom.swing.MigLayout;
import rdiger36.StudioBridge.gui.DialogTwoButtons;
import rdiger36.StudioBridge.gui.MainMenu;
import rdiger36.StudioBridge.objects.LocalMachine;

/**
 * Downloads the release file of this package, checks it against the SHA-256 digest GitHub
 * lists for it, and installs it.
 *
 * <p>Only the installed packages update themselves. Their jpackage launcher sets
 * {@code -Dstudiobridge.dist} to {@code msi}, {@code appimage} or {@code dmg}:</p>
 * <ul>
 *   <li>MSI: msiexec installs the new MSI over the old one, into the same folder, and
 *       StudioBridge is started again.</li>
 *   <li>AppImage: the new file replaces the running one under the same name, so shortcuts
 *       and autostart entries keep working, and is started.</li>
 *   <li>DMG: the app bundle can not replace itself while it runs, so {@link Updater} does
 *       it from a copy of this JAR after StudioBridge has ended, and starts the new app.</li>
 * </ul>
 */
public class Installer {

    /** The packages that install an update themselves. */
    enum Kind { MSI, APPIMAGE, DMG }

    private final Kind kind;
    private final String fileName;
    private final String url;
    private final String sha256;
    private final long size;

    private Installer(Kind kind, JSONObject asset, String sha256) {
        this.kind = kind;
        this.fileName = asset.getString("name");
        this.url = asset.getString("browser_download_url");
        this.sha256 = sha256;
        this.size = asset.optLong("size", -1);
    }

    /**
     * The installer for the package StudioBridge runs from.
     *
     * @param release the latest release from the GitHub API
     * @return the installer, or null if this package can not update itself (JAR, portable ZIP),
     *         the release has no file or digest for it, or the files can not be replaced
     */
    static Installer forThisPackage(JSONObject release) {
        Kind kind = currentKind();
        if (kind == null) return null;

        JSONObject asset = findAsset(release.optJSONArray("assets"), assetSuffix(kind, new LocalMachine().getOsArch()));
        if (asset == null) return null;

        String sha256 = sha256Of(asset);
        if (sha256 == null) return null;

        if (kind == Kind.APPIMAGE && !Files.isWritable(appImage().getParent())) return null;
        if (kind == Kind.DMG && (macBundle() == null || !Files.isWritable(macBundle().getParent()))) return null;

        return new Installer(kind, asset, sha256);
    }

    /**
     * The package StudioBridge runs from, as its launcher declares it.
     *
     * @return the package, or null for the JAR, the portable ZIP and anything unknown
     */
    private static Kind currentKind() {
        if (System.getProperty("jpackage.app-path") == null) return null;

        String dist = System.getProperty("studiobridge.dist", "");
        switch (dist) {
            case "msi":
                return Kind.MSI;
            case "appimage":
                // Set by the AppImage runtime, missing when the image was extracted
                return System.getenv("APPIMAGE") != null ? Kind.APPIMAGE : null;
            case "dmg":
                return Kind.DMG;
            default:
                return null;
        }
    }

    /**
     * The end of the release file name for a package and CPU architecture, as the release
     * workflow names them.
     *
     * @param kind the package
     * @param arch the architecture as {@link LocalMachine} normalizes it
     * @return the suffix, or null if no release file exists for this architecture
     */
    static String assetSuffix(Kind kind, String arch) {
        boolean x64 = "x86_64".equals(arch);
        boolean arm = "arm64".equals(arch);
        switch (kind) {
            case MSI:
                return x64 ? "_win_x86.msi" : arm ? "_win_arm64.msi" : null;
            case APPIMAGE:
                return x64 ? "-x86_64.AppImage" : arm ? "-aarch64.AppImage" : null;
            case DMG:
                return arm ? "_macOS_arm64.dmg" : null;
            default:
                return null;
        }
    }

    /**
     * Finds the release file whose name ends with the given suffix.
     *
     * @return the file of the GitHub API, or null if there is none
     */
    static JSONObject findAsset(JSONArray assets, String suffix) {
        if (assets == null || suffix == null) return null;
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.optJSONObject(i);
            if (asset != null && asset.optString("name", "").endsWith(suffix)) return asset;
        }
        return null;
    }

    /**
     * The SHA-256 checksum GitHub computed for a release file.
     *
     * @return the checksum in lower case hex, or null if the file has none
     */
    static String sha256Of(JSONObject asset) {
        String digest = asset.optString("digest", "");
        if (!digest.startsWith("sha256:")) return null;
        String hex = digest.substring("sha256:".length()).toLowerCase();
        return hex.matches("[0-9a-f]{64}") ? hex : null;
    }

    /** The AppImage file StudioBridge runs from. */
    private static Path appImage() {
        return Paths.get(System.getenv("APPIMAGE"));
    }

    /**
     * The app bundle StudioBridge runs from: the launcher is {@code StudioBridge.app/Contents/MacOS/StudioBridge}.
     *
     * @return the bundle, or null if the launcher is not inside one
     */
    private static Path macBundle() {
        Path bundle = Paths.get(System.getProperty("jpackage.app-path")).toAbsolutePath().getParent().getParent().getParent();
        return bundle != null && bundle.getFileName().toString().endsWith(".app") ? bundle : null;
    }

    /**
     * Downloads the update with a progress dialog and installs it.
     *
     * @param mainFrame the main window, parent of the dialogs
     * @param prepareExit offers to save unsaved data before StudioBridge closes, false if the user cancelled
     */
    void start(JFrame mainFrame, BooleanSupplier prepareExit) {
        JDialog dialog = new JDialog(mainFrame, "StudioBridge", true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JProgressBar progress = new JProgressBar(0, 100);
        progress.setIndeterminate(size <= 0);
        progress.setStringPainted(size > 0);
        JButton cancel = new JButton(I18n.t("update.cancel"));

        dialog.getContentPane().setLayout(new MigLayout("", "[300px,grow]", "[][][]"));
        dialog.getContentPane().add(new JLabel(I18n.t("update.downloading")), "cell 0 0");
        dialog.getContentPane().add(progress, "cell 0 1,growx");
        dialog.getContentPane().add(cancel, "cell 0 2,alignx right");

        SwingWorker<Path, Void> download = new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws Exception {
                return download(this::setProgress, this::isCancelled);
            }

            @Override
            protected void done() {
                dialog.dispose();
                Path file;
                try {
                    file = get();
                } catch (CancellationException e) {
                    return;
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    showFailure(mainFrame);
                    return;
                }
                install(file, mainFrame, prepareExit);
            }
        };
        download.addPropertyChangeListener(e -> {
            if ("progress".equals(e.getPropertyName())) progress.setValue((Integer) e.getNewValue());
        });
        cancel.addActionListener(e -> download.cancel(true));

        dialog.pack();
        dialog.setLocationRelativeTo(mainFrame);
        download.execute();
        dialog.setVisible(true);
    }

    /** Receives the download progress in percent. */
    private interface Progress { void set(int percent); }

    /** Tells the download to stop. */
    private interface Cancelled { boolean get(); }

    /**
     * Downloads the release file and checks its SHA-256 checksum.
     *
     * @return the downloaded file
     * @throws IOException if the download fails or the checksum does not match; the file is deleted then
     */
    private Path download(Progress progress, Cancelled cancelled) throws Exception {
        // The AppImage is downloaded next to the running one, so it can be moved over it in one step
        Path file = kind == Kind.APPIMAGE
                ? appImage().resolveSibling("." + fileName + ".download")
                : Files.createTempDirectory("StudioBridge-update").resolve(fileName);

        // GitHub redirects the download to its file storage
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Accept", "application/octet-stream")
                .build();

        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                response.body().close();
                throw new IOException("Download of " + url + " failed with status " + response.statusCode());
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = response.body(); OutputStream out = Files.newOutputStream(file)) {
                byte[] buffer = new byte[64 * 1024];
                long done = 0;
                int read;
                while ((read = in.read(buffer)) != -1) {
                    if (cancelled.get()) throw new CancellationException();
                    out.write(buffer, 0, read);
                    digest.update(buffer, 0, read);
                    done += read;
                    if (size > 0) progress.set((int) Math.min(100, done * 100 / size));
                }
            }

            String actual = toHex(digest.digest());
            if (!actual.equals(sha256)) {
                throw new IOException("Checksum of " + fileName + " is " + actual + ", expected " + sha256);
            }
            return file;
        } catch (Exception e) {
            Files.deleteIfExists(file);
            throw e;
        }
    }

    /**
     * Closes StudioBridge and installs the downloaded file.
     */
    private void install(Path file, JFrame mainFrame, BooleanSupplier prepareExit) {
        try {
            if (!prepareExit.getAsBoolean()) {
                Files.deleteIfExists(file);
                return;
            }

            switch (kind) {
                case MSI:
                    startMsiInstall(file);
                    break;
                case APPIMAGE:
                    Path target = appImage();
                    if (!file.toFile().setExecutable(true)) throw new IOException("Can not make " + file + " executable");
                    Files.move(file, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    start(new ProcessBuilder(target.toString()));
                    break;
                case DMG:
                    startUpdater(file);
                    break;
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            showFailure(mainFrame);
        }
    }

    /**
     * Installs the MSI with cmd.exe and starts StudioBridge again afterwards, also when the
     * installation failed or was declined in the UAC prompt. cmd.exe is not part of the
     * installation, so it locks none of the files the MSI replaces.
     *
     * <p>The paths go in as environment variables: they reach cmd.exe as Unicode, while the
     * script itself and its command line stay free of quoting and code page trouble.</p>
     */
    private static void startMsiInstall(Path msi) throws IOException {
        Path launcher = Paths.get(System.getProperty("jpackage.app-path")).toAbsolutePath();
        Path script = msi.resolveSibling("update.cmd");
        Files.write(script, Arrays.asList(
                "@echo off",
                // start /wait, a batch file does not wait for a GUI program like msiexec
                // Into the folder of this installation, the MSI would use the default folder
                "start \"\" /wait msiexec /i \"%SB_MSI%\" /passive INSTALLDIR=\"%SB_DIR%\"",
                "del \"%SB_MSI%\"",
                "start \"\" \"%SB_EXE%\""), StandardCharsets.US_ASCII);

        ProcessBuilder builder = new ProcessBuilder("cmd", "/c", script.toString());
        builder.environment().put("SB_MSI", msi.toString());
        builder.environment().put("SB_DIR", launcher.getParent().toString());
        builder.environment().put("SB_EXE", launcher.toString());
        start(builder);
    }

    /**
     * Starts {@link Updater} from a copy of this JAR with the Java runtime of the app bundle.
     * The copy keeps it running while the bundle, which holds the JAR, is replaced.
     */
    private static void startUpdater(Path dmg) throws Exception {
        Path ownJar = Paths.get(Installer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path updaterJar = dmg.resolveSibling("StudioBridge-updater.jar");
        Files.copy(ownJar, updaterJar, StandardCopyOption.REPLACE_EXISTING);

        String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        start(new ProcessBuilder(java,
                "-Djava.awt.headless=true",
                // The default launch mechanism runs a helper from the runtime, which is gone once the bundle is replaced
                "-Djdk.lang.Process.launchMechanism=FORK",
                "-cp", updaterJar.toString(),
                Updater.class.getName(),
                String.valueOf(ProcessHandle.current().pid()),
                dmg.toString(),
                macBundle().toString()));
    }

    /** Starts a process that outlives StudioBridge. */
    private static void start(ProcessBuilder builder) throws IOException {
        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
               .redirectError(ProcessBuilder.Redirect.DISCARD)
               .start();
    }

    /** Offers the release page after a failed download or installation. */
    private static void showFailure(JFrame mainFrame) {
        int response = new DialogTwoButtons(mainFrame, null,
                new ImageIcon(MainMenu.class.getResource("/achtung.png")),
                I18n.t("update.failed"),
                I18n.t("update.openReleases"),
                I18n.t("update.close")).showDialog();
        if (response == 0) Update.openReleasePage();
    }

    /** Lower case hex of the given bytes. */
    static String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return hex.toString();
    }
}
