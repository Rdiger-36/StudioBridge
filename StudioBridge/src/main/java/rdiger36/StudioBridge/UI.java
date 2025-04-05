package rdiger36.StudioBridge;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;

/**
 * A utility class for managing the Look and Feel (LAF) of the user interface.
 * This class provides methods to change the appearance of the application 
 * to a dark mode or revert to the system default.
 */
public class UI {

    /**
     * Changes the Look and Feel of the application based on the specified mode.
     *
     * @param mode A boolean value indicating whether to switch to dark mode (true) 
     *             or to revert to the system's default Look and Feel (false).
     */
    public static void changeLAF(boolean mode) {
        try {
            if (mode) {
                // Set the Look and Feel to a dark theme using FlatLaf
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
                // Configure additional UI properties for the dark theme
                UIManager.put("TitlePane.menuBarEmbedded", true);
                UIManager.put("TitlePane.unifiedBackground", true);
                UIManager.put("MenuItem.selectionType", "underline");
                FlatLaf.setUseNativeWindowDecorations(true); // Use native decorations for windows
                FlatLaf.updateUI(); // Refresh the UI to apply changes
            } else {
                // Revert to the system's default Look and Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e1) {
            // Print stack trace if an exception occurs during Look and Feel change
            e1.printStackTrace();
        }
    }
}