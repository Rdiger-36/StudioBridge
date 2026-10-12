package rdiger36.StudioBridge.functions;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Replaces the macOS app bundle with the one of a downloaded DMG and starts it.
 *
 * <p>{@link Installer} runs this from a copy of the StudioBridge JAR, with the Java runtime
 * of the old bundle, and ends StudioBridge. It waits for that, copies the new app next to the
 * old one, swaps the two by renaming and starts the new app. If anything fails before the
 * swap, the old app stays and is started again. What happened is written to
 * {@code update.log} next to the DMG.</p>
 *
 * <p>Arguments: the process id of StudioBridge, the DMG, the app bundle to replace.</p>
 */
public class Updater {

    private static PrintWriter log;

    public static void main(String[] args) throws IOException {
        long pid = Long.parseLong(args[0]);
        Path dmg = Paths.get(args[1]);
        Path app = Paths.get(args[2]);

        log = new PrintWriter(Files.newBufferedWriter(dmg.resolveSibling("update.log"), StandardCharsets.UTF_8), true);
        try {
            update(pid, dmg, app);
        } catch (Exception e) {
            e.printStackTrace(log);
        } finally {
            // The old app, or the new one if the swap worked
            try {
                run("open", app.toString());
            } catch (Exception e) {
                e.printStackTrace(log);
            }
            log.close();
        }
    }

    private static void update(long pid, Path dmg, Path app) throws Exception {
        log.println("Waiting for StudioBridge (" + pid + ") to end");
        ProcessHandle process = ProcessHandle.of(pid).orElse(null);
        if (process != null) process.onExit().get(2, TimeUnit.MINUTES);

        String name = app.getFileName().toString();
        Path staged = app.resolveSibling("." + name + ".new");
        Path old = app.resolveSibling("." + name + ".old");
        deleteRecursively(staged);
        deleteRecursively(old);

        try {
            // Copy the new app next to the old one, so the swap is a rename within one folder
            Path mount = Files.createTempDirectory(dmg.getParent(), "mount");
            run("hdiutil", "attach", "-nobrowse", "-readonly", "-noautoopen", "-mountpoint", mount.toString(), dmg.toString());
            try {
                run("ditto", findApp(mount).toString(), staged.toString());
            } finally {
                run("hdiutil", "detach", mount.toString(), "-force");
                Files.deleteIfExists(mount);
            }

            log.println("Replacing " + app);
            Files.move(app, old);
            try {
                Files.move(staged, app);
            } catch (IOException e) {
                Files.move(old, app);
                throw e;
            }
            deleteRecursively(old);
        } finally {
            deleteRecursively(staged);
        }
        Files.deleteIfExists(dmg);
        log.println("Updated");
    }

    /** The app bundle in the mounted DMG. */
    private static Path findApp(Path mount) throws IOException {
        try (Stream<Path> files = Files.list(mount)) {
            return files.filter(p -> p.getFileName().toString().endsWith(".app") && Files.isDirectory(p))
                    .findFirst()
                    .orElseThrow(() -> new IOException("No app in " + mount));
        }
    }

    /** Runs a command, logs its output and fails if its exit code is not 0. */
    private static void run(String... command) throws Exception {
        log.println("> " + String.join(" ", command));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        if (!output.isEmpty()) log.print(output);
        if (exitCode != 0) throw new IOException(command[0] + " ended with exit code " + exitCode);
    }

    /** Deletes a file or folder with its content, symbolic links are deleted, not followed. */
    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return;
        try (Stream<Path> walk = Files.walk(path)) {
            for (Path p : (Iterable<Path>) walk.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(p);
            }
        }
    }
}
