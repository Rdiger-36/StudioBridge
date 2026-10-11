package rdiger36.StudioBridge.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;

import rdiger36.StudioBridge.functions.Config;
import rdiger36.StudioBridge.functions.I18n;
import rdiger36.StudioBridge.objects.Printer;

/**
 * Keeps a profile file, the profile list of the main window, {@link MainMenu#printerMap} and
 * {@link MainMenu#mainPrinter} in step.
 *
 * <p>Saving, sending, renaming, importing, deleting and closing the window all change a profile.
 * They used to do it each with its own copy of these steps, which drifted apart: a profile saved
 * while sending was missing from the map, an import overwrote an existing profile without asking,
 * renaming only by letter case deleted the profile on macOS and Windows. Every change goes
 * through here now.</p>
 */
public class ProfileManager {

    private final JFrame frame;
    private final JComboBox<String> list;

    /**
     * @param frame the main window, parent of every dialog (null in tests)
     * @param list the profile list of the main window
     */
    public ProfileManager(JFrame frame, JComboBox<String> list) {
        this.frame = frame;
        this.list = list;
    }

    /**
     * The listed profile whose name equals the given one, ignoring letter case, because the file
     * systems of macOS and Windows do too.
     *
     * @return the listed name, or null when there is none
     */
    public String find(String name) {
        for (int i = 0; i < list.getItemCount(); i++) {
            if (list.getItemAt(i).equalsIgnoreCase(name)) return list.getItemAt(i);
        }
        return null;
    }

    /**
     * Asks for a profile name until it is valid or the user cancels.
     *
     * @param suggestion the name shown in the field at first
     * @param buttonText the text of the confirm button
     * @return the trimmed name, or null when the user cancelled
     */
    public String askName(String suggestion, String buttonText) {
        String name = suggestion;
        while (true) {
            name = new SaveDialog(frame, name, buttonText).saveProfile();
            if (name == null) return null;
            name = name.trim();
            if (Config.isValidProfileName(name)) return name;
            warn(I18n.t("profile.invalidName"));
        }
    }

    /**
     * Asks before a profile would replace another listed profile of the same name.
     *
     * @param name the name the profile is saved under
     * @param own the name the profile had before, which may be replaced without asking; null for
     *            a new profile
     * @return true when nothing else is replaced or the user agreed
     */
    public boolean confirmOverwrite(String name, String own) {
        String existing = find(name);
        if (existing == null || existing.equals(own)) return true;
        int result = new DialogTwoButtons(frame, null, warningIcon(), I18n.t("profile.exists", "name", existing), I18n.t("common.yes"), I18n.t("common.no")).showDialog();
        return result == 0;
    }

    /**
     * Saves a profile and shows it as the selected profile.
     *
     * @param printer the profile to save, under its profile name
     * @param previousName the name it had before when it is renamed, else null
     * @return true when the profile was saved
     */
    public boolean store(Printer printer, String previousName) {
        String name = printer.getProfileName();
        boolean renamed = previousName != null && !previousName.equals(name);

        // A listed profile of the same name in other letter case is replaced (the user agreed)
        String replaced = find(name);
        if (replaced != null && !replaced.equals(name) && !replaced.equals(previousName)) {
            if (!deleteFile(replaced)) return false;
            removeFromList(replaced);
        }

        if (renamed && previousName.equalsIgnoreCase(name)) {
            // Only the letter case changes: on macOS and Windows both names are the same file, so
            // move it aside first, or writing the new name would keep the old spelling and
            // deleting the old name would delete the profile.
            Path old = file(previousName).toPath();
            Path aside = old.resolveSibling("." + previousName + ".renaming");
            try {
                Files.move(old, aside, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                warn(I18n.t("profile.saveFailed"));
                return false;
            }
            if (!Config.saveProfile(frame, printer)) {
                try {
                    Files.move(aside, old, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
            aside.toFile().delete();
        } else {
            if (!Config.saveProfile(frame, printer)) return false;
            // Only delete the old file after the profile was saved under its new name
            if (renamed && !deleteFile(previousName)) warn(I18n.t("profile.deleteFailed", "name", previousName));
        }

        if (renamed) {
            int index = indexOf(previousName);
            if (index >= 0 && indexOf(name) < 0) list.insertItemAt(name, index);
            MainMenu.printerMap.remove(previousName);
            removeFromList(previousName);
        }

        MainMenu.printerMap.put(name, printer);
        if (indexOf(name) < 0) list.addItem(name);
        MainMenu.mainPrinter = printer;
        list.setSelectedItem(name);
        if (frame != null) frame.pack();
        return true;
    }

    /**
     * Saves changed values of a listed profile without changing which profile the main window
     * shows. When it is the one shown, the main window is refreshed with the new values.
     *
     * @param printer the profile with its new values, under its existing profile name
     * @return true when it was saved
     */
    public boolean update(Printer printer) {
        if (!Config.saveProfile(frame, printer)) return false;
        String name = printer.getProfileName();
        MainMenu.printerMap.put(name, printer);
        if (name.equals(list.getSelectedItem())) {
            MainMenu.mainPrinter = printer;
            list.setSelectedItem(name); // fires the listener, which loads the new values
        }
        return true;
    }

    /**
     * Offers to save a new profile with data, asking for its name.
     *
     * @param printer the entered data; its profile name is set when it is saved
     * @return true when it was saved
     */
    public boolean offerSaveNew(Printer printer) {
        int response = new DialogTwoButtons(frame, null, warningIcon(), I18n.t("profile.unsavedNew"), I18n.t("profile.saveNew"), I18n.t("common.continueWithoutSaving")).showDialog();
        if (response != 0) return false;
        return saveNew(printer);
    }

    /**
     * Asks for a name and saves a new profile under it.
     *
     * @param printer the entered data; its profile name is set when it is saved
     * @return true when it was saved
     */
    public boolean saveNew(Printer printer) {
        String suggestion = printer.getPrinterName().trim().isEmpty() ? "" : printer.getPrinterName().trim();
        String name = askName(suggestion, I18n.t("common.save"));
        if (name == null || !confirmOverwrite(name, null)) return false;
        printer.setProfileName(name);
        return store(printer, null);
    }

    /**
     * Offers to save the changes of the selected profile.
     *
     * @return true when they were saved
     */
    public boolean offerSaveChanges(Printer printer) {
        int response = new DialogTwoButtons(frame, null, warningIcon(), I18n.t("profile.unsavedChanges"), I18n.t("profile.saveChanges"), I18n.t("common.continueWithoutSaving")).showDialog();
        return response == 0 && store(printer, null);
    }

    /** The answer to a question on closing StudioBridge. */
    public enum Choice { SAVED, DISCARD, CANCEL }

    /**
     * On closing: offers to save a new profile with data.
     *
     * @return SAVED, DISCARD for "continue without saving", CANCEL when the user closed a dialog
     *         or cancelled the name, so StudioBridge stays open and nothing is lost
     */
    public Choice offerSaveNewOnExit(Printer printer) {
        int response = new DialogTwoButtons(frame, null, warningIcon(), I18n.t("profile.unsavedNew"), I18n.t("profile.saveNew"), I18n.t("common.continueWithoutSaving")).showDialog();
        if (response == 1) return Choice.DISCARD;
        if (response == 0 && saveNew(printer)) return Choice.SAVED;
        return Choice.CANCEL;
    }

    /**
     * On closing: offers to save the changes of the selected profile.
     *
     * @return SAVED, DISCARD for "continue without saving", CANCEL when the dialog was closed or
     *         saving failed
     */
    public Choice offerSaveChangesOnExit(Printer printer) {
        int response = new DialogTwoButtons(frame, null, warningIcon(), I18n.t("profile.unsavedChanges"), I18n.t("profile.saveChanges"), I18n.t("common.continueWithoutSaving")).showDialog();
        if (response == 1) return Choice.DISCARD;
        if (response == 0 && store(printer, null)) return Choice.SAVED;
        return Choice.CANCEL;
    }

    /**
     * Imports a profile file under a name the user picks.
     *
     * @param source the chosen .sbp file
     */
    public void importProfile(File source) {
        Printer imported;
        try {
            imported = Config.readProfile(source);
        } catch (IOException e) {
            warn(I18n.t("profile.readFailed"));
            return;
        }
        String suggestion = source.getName().replaceFirst("(?i)\\.sbp$", "");
        String name = askName(suggestion, I18n.t("profile.setName"));
        if (name == null || !confirmOverwrite(name, null)) return;
        imported.setProfileName(name);
        store(imported, null);
    }

    /**
     * Deletes a profile after asking.
     *
     * @param name the profile to delete
     */
    public void delete(String name) {
        delete(name, null);
    }

    /**
     * Deletes a profile after asking. The main window keeps showing its profile unless it was
     * the deleted one, then it switches to a new profile.
     *
     * @param name the profile to delete
     * @param parent a dialog the questions belong to, or null for the main window
     * @return true when the profile was deleted
     */
    public boolean delete(String name, JDialog parent) {
        JFrame owner = parent == null ? frame : null;
        int response = new DialogTwoButtons(owner, parent, warningIcon(), I18n.t("profile.delete", "name", name), I18n.t("common.yes"), I18n.t("common.no")).showDialog();
        if (response != 0) return false;
        if (!deleteFile(name)) {
            new DialogOneButton(owner, parent, warningIcon(), I18n.t("profile.deleteFailed", "name", name), I18n.t("common.ok")).showDialog();
            return false;
        }
        boolean shown = name.equals(list.getSelectedItem());
        MainMenu.printerMap.remove(name);
        removeFromList(name);
        if (shown) list.setSelectedIndex(-1);
        if (frame != null) frame.pack();
        return true;
    }

    /** Removes a profile whose file can not be read any more from the list. */
    public void forget(String name) {
        MainMenu.printerMap.remove(name);
        removeFromList(name);
    }

    private boolean deleteFile(String name) {
        File file = file(name);
        return !file.exists() || file.delete();
    }

    private void removeFromList(String name) {
        int index = indexOf(name);
        if (index >= 0) list.removeItemAt(index);
    }

    private int indexOf(String name) {
        return ((DefaultComboBoxModel<String>) list.getModel()).getIndexOf(name);
    }

    private static File file(String name) {
        return new File(MainMenu.ProfilesDir, name + ".sbp");
    }

    private void warn(String message) {
        new DialogOneButton(frame, null, warningIcon(), message, I18n.t("common.ok")).showDialog();
    }

    private static ImageIcon warningIcon() {
        return new ImageIcon(MainMenu.class.getResource("/achtung.png"));
    }
}
