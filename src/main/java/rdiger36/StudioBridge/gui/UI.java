package rdiger36.StudioBridge.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import javax.swing.UIManager;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
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
        	
        	FlatLaf.setUseNativeWindowDecorations(true); // Use native decorations for windows
        	
            if (mode) {
                // Set the Look and Feel to a dark theme using FlatLaf
                
                if (MainMenu.localMachine.getOs().equals("macos")) { 
                	UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacDarkLaf");
                } else {
                	UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
                }

            } else {
                // Revert to the system's default Look and Feel
            	
                if (MainMenu.localMachine.getOs().equals("macos")) { 
                	UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacLightLaf");
                } else {
                	UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
                }
            }
            
            // Configure additional UI properties for the dark theme
//            UIManager.put("TitlePane.menuBarEmbedded", true);
//            UIManager.put("TitlePane.unifiedBackground", true);
            UIManager.put("MenuItem.selectionType", "underline");
            
            FlatLaf.updateUI(); // Refresh the UI to apply changes
            
        } catch (Exception e1) {
            // Print stack trace if an exception occurs during Look and Feel change
            e1.printStackTrace();
        }
    }

    /** The setting value that follows the light or dark mode of the operating system. */
    public static final String THEME_AUTO = "auto";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    /**
     * Whether a theme setting means dark mode.
     *
     * @param theme "auto", "light" or "dark"; anything else counts as "auto"
     */
    public static boolean isDark(String theme) {
        if (THEME_DARK.equals(theme)) return true;
        if (THEME_LIGHT.equals(theme)) return false;
        return isSystemDark();
    }

    /**
     * Asks the operating system whether it is in dark mode. Unknown systems and any
     * failure count as light, which is the default of every system.
     */
    public static boolean isSystemDark() {
        try {
            String os = MainMenu.localMachine.getOs();
            if (os.equals("macos")) {
                // Only set to "Dark" in dark mode, the key does not exist in light mode
                return "Dark".equalsIgnoreCase(run("defaults", "read", "-g", "AppleInterfaceStyle"));
            }
            if (os.equals("windows")) {
                return Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER,
                        "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                        "AppsUseLightTheme") == 0;
            }
            if (os.equals("linux")) {
                return isLinuxDark(run("gsettings", "get", "org.gnome.desktop.interface", "color-scheme"),
                        run("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme"));
            }
        } catch (Throwable e) {
            // No registry value, no gsettings or no JNA: treat as light
        }
        return false;
    }

    /**
     * Reads the dark mode from the GNOME settings: the color scheme of newer desktops, else a
     * theme name like "Adwaita-dark".
     */
    static boolean isLinuxDark(String colorScheme, String gtkTheme) {
        if (colorScheme != null && colorScheme.contains("prefer-dark")) return true;
        if (colorScheme != null && colorScheme.contains("prefer-light")) return false;
        return gtkTheme != null && gtkTheme.toLowerCase().contains("dark");
    }

    // Runs a command and returns its first output line, or null when it fails
    private static String run(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                return process.waitFor(3, TimeUnit.SECONDS) && process.exitValue() == 0 ? (line == null ? "" : line.trim()) : null;
            } finally {
                process.destroy();
            }
        } catch (Exception e) {
            return null;
        }
    }
}