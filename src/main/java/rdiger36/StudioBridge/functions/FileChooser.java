package rdiger36.StudioBridge.functions;

import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;

import jnafilechooser.api.JnaFileChooser;
import jnafilechooser.api.JnaFileChooser.Mode;

/**
 * Shows the file dialogs of the operating system.
 *
 * <p>JnaFileChooser is native on Windows only and falls back to the Swing JFileChooser
 * everywhere else. On macOS and Linux the AWT FileDialog is used instead, which is the Finder
 * dialog on macOS and the GTK dialog on Linux. The GTK dialog can not pick a folder, so folders
 * on Linux still use JnaFileChooser.</p>
 */
public class FileChooser {

    private static final String OS = System.getProperty("os.name", "").toLowerCase();
    private static final boolean WINDOWS = OS.startsWith("win");
    private static final boolean MAC = OS.startsWith("mac");

    /**
     * Lets the user pick a profile file.
     *
     * @param parent the window the dialog belongs to
     * @param title the title of the dialog
     * @return the chosen file, or null when the dialog was cancelled
     */
    public static File chooseProfile(Window parent, String title) {
        if (WINDOWS) {
            JnaFileChooser fc = new JnaFileChooser();
            fc.setMode(Mode.Files);
            fc.setTitle(title);
            fc.addFilter("StudioBridge (*.sbp)", "sbp");
            return fc.showOpenDialog(parent) ? fc.getSelectedFile() : null;
        }
        FileDialog dialog = createDialog(parent, title);
        dialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".sbp"));
        return show(dialog);
    }

    /**
     * Lets the user pick a folder.
     *
     * @param parent the window the dialog belongs to
     * @param title the title of the dialog
     * @return the chosen folder, or null when the dialog was cancelled
     */
    public static File chooseDirectory(Window parent, String title) {
        if (MAC) {
            // Makes the Finder dialog pick folders instead of files, only while it is open
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            try {
                return show(createDialog(parent, title));
            } finally {
                System.setProperty("apple.awt.fileDialogForDirectories", "false");
            }
        }
        JnaFileChooser fc = new JnaFileChooser();
        fc.setMode(Mode.Directories);
        fc.setTitle(title);
        return fc.showOpenDialog(parent) ? fc.getSelectedFile() : null;
    }

    private static FileDialog createDialog(Window parent, String title) {
        if (parent instanceof Dialog) return new FileDialog((Dialog) parent, title, FileDialog.LOAD);
        return new FileDialog(parent instanceof Frame ? (Frame) parent : null, title, FileDialog.LOAD);
    }

    private static File show(FileDialog dialog) {
        dialog.setVisible(true); // blocks until the user chose or cancelled
        if (dialog.getFile() == null) return null;
        return new File(dialog.getDirectory(), dialog.getFile());
    }
}
