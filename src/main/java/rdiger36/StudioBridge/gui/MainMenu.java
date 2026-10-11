package rdiger36.StudioBridge.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.DefaultListCellRenderer;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.MigLayout;
import rdiger36.StudioBridge.functions.*;
import rdiger36.StudioBridge.objects.*;

/**
 * The MainMenu class serves as the main graphical user interface (GUI) 
 * for the StudioBridge application. It allows users to configure printer settings, 
 * manage profiles, and send data packages to printers.
 */
public class MainMenu {

    // Default and current save paths for profiles
    public static String defaultSavePath = System.getProperty("user.home") + System.getProperty("file.separator") + ".studio-bridge";
    public static String savePath = defaultSavePath;
    public static String ProfilesDir = savePath + System.getProperty("file.separator") + "Profiles";
    
    // Application Name
    public static String title = "StudioBridge";
    
    // Application version, taken from the Maven project version
    public static String version = Update.readLocalVersion();
    
    // Destination for UDPPackage
    public static String destionation4UDP = "localhost";
    
    // Direct mode setting: if true than send UDP package directly to Bambu Studio, else send it via broadcast
    public static boolean directMode = false;
    
    // Set boolean for checking updates on startup
    public static boolean checkForUpdate = true;
    
    // Theme setting: "auto" follows the light or dark mode of the operating system, else "light" or "dark"
    public static String theme = UI.THEME_AUTO;
    
    // Whether the GUI is shown dark, worked out from the theme setting at startup
    public static boolean darkmode = false;
    
    // Control over the use of the last selected profile
    public static boolean rememberLastUsedProfile = false;
    
    // For internal use to run as native jar
    public static boolean runningAsJar = true;
    
    // Set specific profiles directory via cmdline
    public static boolean useCustomProfilesDir = false;
    
    // Set when --direct or --broadcast was given, which then only applies to this run
    public static boolean directModeFromArgs = false;
    
    // Values of the settings file, written back instead of values that were only set by start arguments
    public static boolean savedDirectMode = false;
    public static String savedProfilesDir;
    
    // Set specific profile via cmdline
    public static boolean useSpecificProfiles = false;
    
    // Set visibility of jar version start
    public static boolean jarInfo = true;
    
    // Contains the name of the last used profile
    public static String lastUsedProfile;
    
    // Contains the names of profiles which are not allowed to set
    // Names a profile can not have: empty, and the placeholder for a new profile in every language
    public static List<String> notAllowedProfileNames = new ArrayList<String>(List.of("New profile", ""));
    
    // Profile name of a new, not yet saved profile, also shown as placeholder of the empty profile list
    public static String NEW_PROFILE = "New profile";
    
    // Language setting: a language code, or "auto" for the language of the operating system
    public static String language = I18n.AUTO;
    
    // Contains the profiles which were set by CLI option
    public static List<String> profilesListCLI = new ArrayList<String>();
    
    // Create LocalMachine Object from localhost
    public static LocalMachine localMachine = new LocalMachine();
    
    // Create static Printer Object, will be used as selected printer for GUI
    public static Printer mainPrinter;
    
    // Create static HashMap containing printer objects with profilename as key, will be used for multi printer setup
    public static HashMap<String, Printer> printerMap;
    
    /**
     * Applies the language setting to the texts of the GUI. The placeholder name of a new profile
     * is reserved in every language, so a last used profile stored under another language is never
     * taken for a real profile.
     */
    static void applyLanguage() {
    	I18n.setLanguage(language);
    	NEW_PROFILE = I18n.t("profile.new");
    	for (String code : I18n.LANGUAGES) {
    		String name = I18n.raw(code, "profile.new");
    		if (name != null && !notAllowedProfileNames.contains(name)) notAllowedProfileNames.add(name);
    	}
    }
    
    // Whether every field a printer needs is filled in
    private static boolean isComplete(Printer printer) {
    	return !printer.getIpAdress().trim().isEmpty() && !printer.getSerialNumber().trim().isEmpty()
    			&& !printer.getPrinterName().trim().isEmpty() && !printer.getPrinterModel().isEmpty();
    }
    
    // The selected printer model, or "" when none is selected
    private static String selectedModel(JComboBox<String> cbxModel) {
    	Object selected = cbxModel.getSelectedItem();
    	return selected == null ? "" : selected.toString();
    }
    
    // The selected profile, or NEW_PROFILE when the list has no selection
    private static String selectedProfile(JComboBox<String> cbxProfile) {
    	Object selected = cbxProfile.getSelectedItem();
    	return selected == null ? NEW_PROFILE : selected.toString();
    }
    
    public static void main(String[] args) {
    	
    	// Get running OS
    	if (System.getProperty("jpackage.app-path") != null) {
            runningAsJar = false;
            jarInfo = false;
    	}
    	
        boolean sendOnly = false;
        boolean showHelp = false;
        boolean showVersion = false;
        boolean forceDirect = false;
        boolean forceBroadcast = false;

        migrateOldDataDirectory();
        Config.loadAppSettings();
        migrateLegacyProfilesPathSetting();
        applyLanguage();
        
        savedDirectMode = directMode;
        savedProfilesDir = ProfilesDir;

        final Map<String, String> valid = new LinkedHashMap<>();

        valid.put("Starting options:", "");
        valid.put("--noupdate", "Skip search for latest updates on startup");
        valid.put("--sendonly", "Start with no GUI, only send data from all printers to Bambu Studio");
        valid.put("--direct", "Send the UDP package directly to Bambu Studio on this computer");
        valid.put("--broadcast", "Send the UDP package as broadcast to the network");
        valid.put("--version", "Show the version of StudioBridge");
        valid.put("--help", "Show help for StudioBridge");

        valid.put("", "");

        valid.put("Profile options:", "");
        valid.put("--customProfilesDir='/path/to/profiles/'",
                  "Load the profiles from this directory (only for this run)");
        valid.put("--selectedPrinters='/path/to/profile.sbp','/path/to/other/profile2.sbp'",
                  "Send only these profiles, comma-separated (requires --sendonly)");


        // Initialize arguments
        List<String> unknown = new ArrayList<>();

        for (String arg : args) {
            if ("--help".equals(arg)) {
                showHelp = true;
                break;
            }
            if ("--version".equals(arg)) {
                showVersion = true;
                continue;
            }
            if ("--noupdate".equals(arg)) {
                checkForUpdate = false;
                continue;
            }
            
            if ("--direct".equals(arg)) {
                forceDirect = true;
                continue;
            }
            
            if ("--broadcast".equals(arg)) {
                forceBroadcast = true;
                continue;
            }
            
            if ("--sendonly".equals(arg)) {
                sendOnly = true;
                continue;
            }
            
            if (arg.startsWith("--customProfilesDir=")) {
            	String dir = argValue(arg);
            	if (!dir.isEmpty()) {
            		ProfilesDir = dir;
            		useCustomProfilesDir = true;
            		continue;
            	}
            }
            
            if (arg.startsWith("--selectedPrinters=")) {
            	String argProfiles = arg.substring(arg.indexOf('=') + 1);
            	
            	for (String profile : argProfiles.split(",")) {
            		profile = stripQuotes(profile.trim());
            		if (!profile.isEmpty()) profilesListCLI.add(profile);
            	}
            	
            	if (!profilesListCLI.isEmpty()) {
            		useSpecificProfiles = true;
            		continue;
            	}
            }
            
            unknown.add(arg);
        }

        if (showHelp) {
            printHeader();
            printUsage(valid);
            return;
        }

        if (!unknown.isEmpty()) {
            printHeader();
            System.out.println("Unknown argument: " + unknown);
            System.out.println();
            printUsage(valid);
            System.exit(1);
        }
        
        if (showVersion) {
            System.out.println(title + " " + version);
            return;
        }
        
        String conflict = null;
        if (forceDirect && forceBroadcast) {
            conflict = "--direct and --broadcast can not be combined.";
        } else if (useSpecificProfiles && !sendOnly) {
            conflict = "--selectedPrinters only works together with --sendonly.";
        }
        if (conflict != null) {
            printHeader();
            System.out.println("Error! " + conflict);
            System.out.println();
            printUsage(valid);
            System.exit(1);
        }
        
        if (forceDirect || forceBroadcast) {
            directMode = forceDirect;
            directModeFromArgs = true;
        }
        
        if (sendOnly) {
            System.exit(startNoGUI());
        } else {
        	
        	if (localMachine.getOs().equals("macos")) {
        		System.setProperty( "apple.awt.application.name", title );
        		System.setProperty( "apple.awt.application.appearance", "system" );
        		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
        	}
        	darkmode = UI.isDark(theme);
        	UI.changeLAF(darkmode); // Change the look and feel based on the theme setting
            startGUI();
        }
    }
    
    // Returns the value of an argument like --name=value, without surrounding quotes
    static String argValue(String arg) {
    	return stripQuotes(arg.substring(arg.indexOf('=') + 1).trim());
    }
    
    // Removes one pair of surrounding single or double quotes
    private static String stripQuotes(String value) {
    	if (value.length() >= 2 && (value.startsWith("'") && value.endsWith("'") || value.startsWith("\"") && value.endsWith("\""))) {
    		return value.substring(1, value.length() - 1);
    	}
    	return value;
    }
    
    // print help as console output
    private static void printUsage(Map<String, String> valid) {

        String prog = "./StudioBridge.jar";

        if (!runningAsJar) {
            if (localMachine.getOs().equals("windows")) {
                prog = "./StudioBridgeCLI.exe";
            } else if (localMachine.getOs().equals("linux")) {
                prog = "./StudioBridge.AppImage";
            } else if (localMachine.getOs().equals("macos")) {
                prog = "/Applications/StudioBridge.app/Contents/MacOS/StudioBridgeCLI";
            }
        }

        System.out.println("Usage:");
        System.out.println("  " + prog + " [OPTIONS]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  Options can be combined, except --direct with --broadcast");
        System.out.println();

        final int indent = 6;
        final int maxInline = 22;

        for (Map.Entry<String, String> e : valid.entrySet()) {
            String key = e.getKey();
            String desc = e.getValue();

            if (key.endsWith(":")) {
                System.out.println(key);
                continue;
            }

            if (key.trim().isEmpty()) {
                System.out.println();
                continue;
            }

            if (key.length() > maxInline) {
                System.out.println("  " + key);
                if (!desc.isEmpty()) {
                    System.out.println(" ".repeat(indent) + desc);
                }
            } else {
                System.out.printf("  %-"+maxInline+"s %s%n", key, desc);
            }
        }

        System.out.println();
        System.out.println("Example:");
        System.out.println("  " + prog + " --help");
        System.out.println("  " + prog + " --sendonly --noupdate --direct");

        // different output for different OS
        if (localMachine.getOs().equals("windows")) {
        	System.out.println("  " + prog + " --sendonly --customProfilesDir='C:\\Users\\rdiger-36\\Documents\\StudioBridgeProfiles'");
           	System.out.println("  " + prog + " --sendonly --selectedPrinters='C:\\Users\\rdiger-36\\Documents\\My P1S.sbp','C:\\Users\\rdiger-36\\Desktop\\H2D-Pro Work.sbp'");
        } else if (localMachine.getOs().equals("linux")) {
        	System.out.println("  " + prog + " --sendonly --customProfilesDir='/home/rdiger-36/Documents/StudioBridgeProfiles'");
           	System.out.println("  " + prog + " --sendonly --selectedPrinters='/home/rdiger-36/Documents/My P1S.sbp','/home/rdiger-36/Desktop/H2D-Pro Work.sbp'");
        } else if (localMachine.getOs().equals("macos")) {
        	System.out.println("  " + prog + " --sendonly --customProfilesDir='/Users/rdiger-36/Documents/StudioBridgeProfiles'");
           	System.out.println("  " + prog + " --sendonly --selectedPrinters='/Users/rdiger-36/Documents/My P1S.sbp','/Users/rdiger-36/Desktop/Profiles/H2D-Pro Work.sbp'");
        }
    }


    private static void printHeader() {
        System.out.println("*** " + title + " by Rdiger-36 v." + version + " ***\n");
        
        if (runningAsJar) {
        	System.out.println("You are currently running this application in raw mode (via .jar)!");
        	System.out.println("For the official version for your OS and CPU architecture, please visit the GitHub repository.");
        	System.out.println("https://github.com/Rdiger-36/StudioBridge/\n");
        }
    	
    }


    private static void migrateOldDataDirectory() {
        String sep = System.getProperty("file.separator");
        File oldDir = new File(System.getProperty("user.home") + sep + "StudioBridge");
        File newDir = new File(defaultSavePath);

        if (!oldDir.exists() || !oldDir.isDirectory()) {
            return;
        }

        newDir.mkdirs();
        mergeDirectories(oldDir, newDir);
        deleteDirectory(oldDir);
    }

    /**
     * Repairs a profiles path that was read from the settings file but still points into
     * the old data directory that {@link #migrateOldDataDirectory()} has already moved.
     *
     * <p>The settings file always contains an absolute {@code customProfilesPath}, even for
     * users who never picked one, so everybody who used StudioBridge before the data
     * directory was renamed has the old location stored there. Because the settings are
     * loaded after the directory migration, that stored value overrides the migrated path
     * and the profiles moved to the new directory are never found again.</p>
     *
     * <p>This runs on every start and does not require the old directory to still exist,
     * because {@code getAllProfiles()} recreates the missing directory as an empty one.</p>
     */
    private static void migrateLegacyProfilesPathSetting() {
        String legacyDir = System.getProperty("user.home") + System.getProperty("file.separator") + "StudioBridge";
        ProfilesDir = remapLegacyProfilesPath(ProfilesDir, legacyDir, defaultSavePath);
    }

    /**
     * Moves a path that lies inside the old data directory to the same relative location
     * inside the current one. Paths outside the old directory are returned unchanged, so a
     * directory the user deliberately selected somewhere else is kept as it is.
     *
     * @param profilesDir the profiles path as loaded from the settings file.
     * @param legacyDir the data directory used before the rename.
     * @param currentDir the data directory used today.
     * @return the remapped path, or the unchanged path if no remapping is needed.
     */
    static String remapLegacyProfilesPath(String profilesDir, String legacyDir, String currentDir) {
        if (profilesDir == null || legacyDir == null || currentDir == null) {
            return profilesDir;
        }

        if (profilesDir.equals(legacyDir)) {
            return currentDir;
        }

        // Compare including the separator so that siblings like "StudioBridgeBackup" are not matched
        if (profilesDir.startsWith(legacyDir + System.getProperty("file.separator"))) {
            return currentDir + profilesDir.substring(legacyDir.length());
        }

        return profilesDir;
    }

    private static void mergeDirectories(File src, File dest) {
        File[] files = src.listFiles();
        if (files == null) return;

        for (File file : files) {
            File target = new File(dest, file.getName());
            if (file.isDirectory()) {
                target.mkdirs();
                mergeDirectories(file, target);
            } else if (!target.exists()) {
                file.renameTo(target);
            }
        }
    }

    private static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    // CLI mode, no GUI
    /**
     * Sends all profiles without GUI and waits until the broadcasts are finished.
     *
     * @return the exit code: 0 if every package was sent, 1 otherwise.
     */
    private static int startNoGUI() {
    	
    	printHeader();
    	
    	String sendOverMethod = "broadcast";
    	
    	if (directMode) sendOverMethod = "localhost";
    	
    	if (checkForUpdate) Update.checkVersion(null);
    	
    	// prepare profiles or profile directory
        String profilesDir = ProfilesDir;
        ArrayList<String> errors = new ArrayList<>();

        List<File> profilesToUse = new ArrayList<>();

        if (useSpecificProfiles) {
            for (String path : profilesListCLI) {
                File f = new File(path);

                if (!f.exists()) {
                    System.err.println("Profile does not exist: " + path);
                    errors.add("Profile does not exist: " + path);
                    continue;
                }

                if (!f.getName().toLowerCase().endsWith(".sbp")) {
                    System.err.println("Invalid profile format (must end with .sbp): " + path);
                    errors.add("Invalid profile format (must end with .sbp): " + path);
                    continue;
                }

                profilesToUse.add(f);
            }

        } else {
            File dir = new File(profilesDir);

            File[] files = dir.listFiles((d, name) ->
                    name.toLowerCase().endsWith(".sbp"));

            if (files != null) {
                profilesToUse.addAll(Arrays.asList(files));
            }
        }

        if (!profilesToUse.isEmpty()) {
        
	        // Determine which UDP port (2021 or 1990) is in use, or set to 0 if none is available
	        int remoteUdpPort = UDPPackage.getAvailableUDPPort();
	
	        // Check if a valid port was assigned
	        if (remoteUdpPort > 0) {
	        	
	            System.out.println("Found " + profilesToUse.size() + " profile(s)!");
	            System.out.println("Try to send all of them to Bambu Studio\n");
	        	
	            for (File file : profilesToUse) {
	                if (file.isFile()) {  // Process only files, not subdirectories
	                	System.out.println("Loading profile " + file.toString());
	                    // Read like the GUI does: UTF-8, values taken as written
	                    Printer printer;
	                    try {
	                        printer = Config.readProfile(file);
	                    } catch (IOException e) {
	                        System.err.println("Error loading file: " + file.getName());
	                        errors.add("Error loading file: " + file.getName() + "\n");
	                        continue;  // Process next file on error
	                    }
	
	                    if (!isComplete(printer)) {
	                        String errorMsg = "Error! Incomplete profile, IP-Address, serial number, name and model are needed: " + file.getName() + "\n";
	                        System.err.println(errorMsg);
	                        errors.add(errorMsg);
	                        continue;
	                    }
	
	                    // Send the UDP package
	                    boolean sendSuccess = UDPPackage.send(null, printer, remoteUdpPort, false);
	                    if (!sendSuccess) {
	                        String errorMsg = "Error! Could not send package via " + sendOverMethod + " to Bambu Studio: " + printer.getPrinterName()
	                                + " - " + printer.getIpAdress() + " - " + printer.getPrinterModel() + "\n";
	                        errors.add(errorMsg);
	                    } else {
	                    	String successMsg = "Successfully sent package via " + sendOverMethod + " to Bambu Studio: " + printer.getPrinterName()
	                                + " - " + printer.getIpAdress() + " - " + printer.getPrinterModel() + "\n";
	                        System.out.println((successMsg));
	                    }
	                }
	
	            }
	
	            // Log errors if any
	            if (!errors.isEmpty()) {
	                StringBuilder errorLog = new StringBuilder("Attention! The following printers could not send to Bambu Studio:\n");
	                for (String error : errors) {
	                    errorLog.append(error).append("\n");
	                }
	                System.err.println(errorLog.toString());
	            }
	
	            // Overall message if everything went well
	            if (errors.isEmpty()) {
	                System.out.println("All Packages successfully sent. The printer(s) will appear in the next 60 seconds in Bambu Studio.");
	            }
	            System.out.println("The program keeps sending for about 50 seconds and will exit automatically.");
	            
	            UDPPackage.awaitBroadcasts();
	            return errors.isEmpty() ? 0 : 1;
	
	        } else {
	            System.err.println("Warning! Bambu Studio is not running!");
	        }
        } else {
            if (useSpecificProfiles) {
                System.err.println("Warning! None of the selected profiles could be used.");
            } else {
                System.err.println("Warning! There are no usable profiles in your profiles directory:");
                System.err.println(profilesDir);
                System.err.println("Create a profile in the StudioBridge window first, or point to your profiles with --customProfilesDir or --selectedPrinters.");
            }
        }
        return 1;
    }

	/**
	 * @wbp.parser.entryPoint
	 */
	public static void startGUI() {
    	EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new MainMenu(darkmode, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Constructs the main menu and initializes the GUI components.
     *
     * @param darkmode A boolean indicating whether dark mode should be enabled.
     * @param frame The parent JFrame to center the main menu.
     */
    public MainMenu(boolean darkmode, JFrame frame) {
//        UI.changeLAF(darkmode); // Change the look and feel based on the dark mode setting
        
        printerMap = new HashMap<String, Printer>();
        
        // Create the main application frame
        JFrame frmStudioBridge = new JFrame();
        frmStudioBridge.setResizable(false);
        frmStudioBridge.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        URL iconUrl = MainMenu.class.getClassLoader().getResource("icon.png");
        frmStudioBridge.setIconImage(Toolkit.getDefaultToolkit().getImage(iconUrl));
        frmStudioBridge.setTitle(title);
        frmStudioBridge.setSize(299, 235);
        frmStudioBridge.setLocationRelativeTo(frame);
        frmStudioBridge.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frmStudioBridge.getContentPane().setLayout(new MigLayout("", "[76.00px][]", "[35px:n,grow][10px:10px:10px][][][][][][10px:n][35px:n,grow]"));

        // Add informational label
        JLabel lblInfo = new JLabel(I18n.t("main.info"));
        frmStudioBridge.getContentPane().add(lblInfo, "cell 0 0 2 1,growx,aligny center");
        
        // Separator
        JSeparator separator = new JSeparator();
        frmStudioBridge.getContentPane().add(separator, "cell 0 1 2 1,growx,aligny center");
        
        // Profile selection components
        JLabel lblProfile = new JLabel(I18n.t("main.profile"));
        frmStudioBridge.getContentPane().add(lblProfile, "flowy,cell 0 2,grow");
        
        JComboBox<String> cbxProfile = new JComboBox<>();
        cbxProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Only real profiles are listed, no selection means a new profile
        cbxProfile.setModel(new DefaultComboBoxModel<String>());
        cbxProfile.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index == -1 && value == null) {
                    lbl.setText(NEW_PROFILE);
                    lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
                }
                return lbl;
            }
        });
        frmStudioBridge.getContentPane().add(cbxProfile, "flowx,cell 1 2,growx");
        
        // Keeps profile files, this list, printerMap and mainPrinter in step
        ProfileManager profiles = new ProfileManager(frmStudioBridge, cbxProfile);

        // Printer information labels
        JLabel lblIP = new JLabel(I18n.t("main.ipAddress"));
        frmStudioBridge.getContentPane().add(lblIP, "cell 0 3,growx,aligny center");

        JLabel lblSerial = new JLabel(I18n.t("main.serialNumber"));
        frmStudioBridge.getContentPane().add(lblSerial, "flowx,cell 0 4,growx,aligny center");

        JLabel lblDevModel = new JLabel(I18n.t("main.model"));
        frmStudioBridge.getContentPane().add(lblDevModel, "flowx,cell 0 5,growx,aligny center");

        String[] items = {
        	    "A-Series", "A1", "A1 Mini", "A2L",
        	    "H-Series", "H2C", "H2C (new revision)", "H2D", "H2D Pro", "H2S",
        	    "P-Series", "P1P", "P1S", "P2S",
        	    "X-Series", "X1", "X1C", "X1E", "X2D"
        	};
        
        // Model selection combo box
        JComboBox<String> cbxModel = new JComboBox<>(items);
        cbxModel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbxModel.setMaximumRowCount(20);
        
    	// Rendering Categories bold
        cbxModel.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );
                
                if (index == -1 && cbxModel.getSelectedIndex() == -1) {
                    lbl.setText(I18n.t("main.selectModel"));
                    return lbl;
                }

                String text = value.toString();
                boolean isCategory = text.endsWith("-Series");
                
                // The series entries keep their English value, only the shown text is translated
                if (isCategory) lbl.setText(I18n.t("main.modelSeries", "series", text.substring(0, text.indexOf('-'))));

                if (index >= 0) {  
                    if (isCategory) {
                        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                        lbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                    } else {
                        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                        lbl.setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 4));
                    }
                } else { 
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                    lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                }

                return lbl;
            }
        });
        frmStudioBridge.getContentPane().add(cbxModel, "cell 1 5,growx");
        cbxModel.setSelectedIndex(-1);
        
        
        // Setting up preparation for combobox model for mouse and arrow keys 
        AtomicInteger lastDirection = new AtomicInteger(0); 

	    cbxModel.addKeyListener(new KeyAdapter() {
	        @Override
	        public void keyPressed(KeyEvent e) {
	            if (e.getKeyCode() == KeyEvent.VK_UP) {
	                lastDirection.set(-1);
	            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
	                lastDirection.set(1);
	            }
	        }
	    });
	
	    cbxModel.addPopupMenuListener(new PopupMenuListener() {
	        @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
	        @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	            lastDirection.set(0);
	        }
	        @Override public void popupMenuCanceled(PopupMenuEvent e) {}
	    });
	
	    cbxModel.addActionListener(arg -> {
	
	    	int idx = cbxModel.getSelectedIndex();
	    	if (idx < 0) return;
	
	        String selected = cbxModel.getItemAt(idx);
	
	        if (selected.endsWith("-Series")) {
	
	            if (idx == 0) {
	                cbxModel.setSelectedIndex(idx + 1);
	                return;
	            }
	
	            if (lastDirection.get() == 0) {
	                cbxModel.setSelectedIndex(idx + 1);
	                return;
	            }

	            if (lastDirection.get() == 1) {
	                cbxModel.setSelectedIndex(idx + 1);
	                return;
	            }
	
	            if (lastDirection.get() == -1) {
	                cbxModel.setSelectedIndex(idx - 1);
	            }
	        }
	    });

        
        // Printer name input field
        JLabel lblDevName = new JLabel(I18n.t("main.name"));
        frmStudioBridge.getContentPane().add(lblDevName, "cell 0 6,growx,aligny center");

        JTextField txtName = new JTextField();
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, I18n.t("hint.name"));
        txtName.setText("");
        txtName.setColumns(10);
        ((AbstractDocument) txtName.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(25));
        frmStudioBridge.getContentPane().add(txtName, "cell 1 6,growx");
        
        // Separator
        JSeparator separator_1 = new JSeparator();
        frmStudioBridge.getContentPane().add(separator_1, "cell 0 7 2 1,growx,aligny center");
        
        // Bottom row: copyright on the left, the actions on the right
        JPanel pnlBottom = new JPanel(new MigLayout("insets 0", "[]push[][]", "[]"));
        frmStudioBridge.getContentPane().add(pnlBottom, "cell 0 8 2 1,growx");
        
        // Button to send package
        JButton btnSendPackage = new JButton(I18n.t("main.sendPackage"));
        btnSendPackage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JButton btnCheckPrinter = new JButton(I18n.t("main.checkPrinter"));
        btnCheckPrinter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Input fields for IP and Serial number
        JTextField txtIP = new JTextField();
        txtIP.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, I18n.t("hint.ip"));
        frmStudioBridge.getContentPane().add(txtIP, "flowx,cell 1 3,growx");
        txtIP.setColumns(10);
        ((AbstractDocument) txtIP.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(15));
        
        JTextField txtSerial = new JTextField();
        txtSerial.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, I18n.t("hint.serial"));
        txtSerial.setText("");
        txtSerial.setColumns(10);
        ((AbstractDocument) txtSerial.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(25));
        frmStudioBridge.getContentPane().add(txtSerial, "cell 1 4,growx");
        
        // Menu bar and settings options
        JMenuBar menuBar = new JMenuBar();
        frmStudioBridge.setJMenuBar(menuBar);
        
        JMenu mnSettings = new JMenu(I18n.t("menu.settings"));
        menuBar.add(mnSettings);
        
        // Theme menu: automatic (light or dark mode of the operating system), light or dark
        JMenu mnTheme = new JMenu(I18n.t("menu.theme"));
        mnSettings.add(mnTheme);
        ButtonGroup themeGroup = new ButtonGroup();
        String[][] themeChoices = {
        	{UI.THEME_AUTO, I18n.t("menu.themeAuto")},
        	{UI.THEME_LIGHT, I18n.t("menu.themeLight")},
        	{UI.THEME_DARK, I18n.t("menu.themeDark")}
        };
        for (String[] choice : themeChoices) {
        	JRadioButtonMenuItem item = new JRadioButtonMenuItem(choice[1]);
        	item.setSelected(choice[0].equals(theme) || (UI.THEME_AUTO.equals(choice[0]) && !UI.THEME_LIGHT.equals(theme) && !UI.THEME_DARK.equals(theme)));
        	item.addActionListener(arg -> {
        		theme = choice[0];
        		boolean dark = UI.isDark(theme);
        		if (dark == MainMenu.darkmode) return;
        		MainMenu.darkmode = dark;

        		Point loc = frmStudioBridge.getLocationOnScreen();
        		int state = frmStudioBridge.getExtendedState();

        		UI.changeLAF(dark);
        		SwingUtilities.updateComponentTreeUI(frmStudioBridge);
        		frmStudioBridge.pack();
        		frmStudioBridge.setLocation(loc);
        		frmStudioBridge.setExtendedState(state);
        		frmStudioBridge.revalidate();
        		frmStudioBridge.repaint();
        	});
        	themeGroup.add(item);
        	mnTheme.add(item);
        }
        
        JCheckBoxMenuItem cbxmntmRememberLastProfile = new JCheckBoxMenuItem(I18n.t("menu.rememberLastProfile"));
        cbxmntmRememberLastProfile.setSelected(rememberLastUsedProfile);
        mnSettings.add(cbxmntmRememberLastProfile);
        
        cbxmntmRememberLastProfile.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rememberLastUsedProfile = cbxmntmRememberLastProfile.isSelected();
            }
        });
        
        JCheckBoxMenuItem cbxmntmSetSendingMode = new JCheckBoxMenuItem(I18n.t("menu.sendDirectly"));
        cbxmntmSetSendingMode.setSelected(directMode);
        mnSettings.add(cbxmntmSetSendingMode);
        
        cbxmntmSetSendingMode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
            	
            	if (cbxmntmSetSendingMode.isSelected()) {
            		new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("mode.direct"), I18n.t("common.ok")).showDialog();
            	} else {
            		new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("mode.broadcast"), I18n.t("common.ok")).showDialog();
            	}
            	
                directMode = cbxmntmSetSendingMode.isSelected();
                directModeFromArgs = false; // A choice in the menu is saved again
            }
        });
        
        // Language menu: automatic (language of the operating system) or one of the shipped languages
        JMenu mnLanguage = new JMenu(I18n.t("menu.language"));
        mnSettings.add(mnLanguage);
        ButtonGroup languageGroup = new ButtonGroup();
        List<String> languageChoices = new ArrayList<>();
        languageChoices.add(I18n.AUTO);
        languageChoices.addAll(I18n.LANGUAGES);
        for (String code : languageChoices) {
        	JRadioButtonMenuItem item = new JRadioButtonMenuItem(I18n.AUTO.equals(code) ? I18n.t("menu.languageAuto") : I18n.name(code));
        	item.setSelected(code.equals(language) || (!I18n.LANGUAGES.contains(language) && I18n.AUTO.equals(code)));
        	item.addActionListener(arg -> {
        		if (code.equals(language)) return;
        		language = code;
        		// The texts are set when the window is built, so a new language needs a restart
        		new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("language.restart"), I18n.t("common.ok")).showDialog();
        	});
        	languageGroup.add(item);
        	mnLanguage.add(item);
        }
        
        mnSettings.add(new JSeparator());
        
        JMenuItem mntmChangeProfilesPath = new JMenuItem(I18n.t("menu.profilesPath"));
        mnSettings.add(mntmChangeProfilesPath);
        
        // Action listener for changing the profiles path
        mntmChangeProfilesPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newProfilesDir = new ProfilesPath(frmStudioBridge).changePath();
                
                if (newProfilesDir.length() > 0 && new File(newProfilesDir).exists()) {
                	// The list is replaced, so offer to save what is entered first
                	Printer current = new Printer(selectedProfile(cbxProfile), txtName.getText(), txtIP.getText(), txtSerial.getText(), selectedModel(cbxModel));
                	Exit.UnsavedState unsaved = Exit.unsavedState(current, mainPrinter);
                	if (unsaved == Exit.UnsavedState.NEW_PROFILE && profiles.offerSaveNewOnExit(current) == ProfileManager.Choice.CANCEL) return;
                	if (unsaved == Exit.UnsavedState.CHANGED_PROFILE && profiles.offerSaveChangesOnExit(current) == ProfileManager.Choice.CANCEL) return;
                	
                    ProfilesDir = newProfilesDir;
                    useCustomProfilesDir = false; // A choice in the menu is saved again
                    cbxProfile.removeAllItems();
                    getAllProfiles(frmStudioBridge, cbxProfile);
                    if (cbxProfile.getItemCount() == 0) {
                        cbxProfile.setSelectedIndex(-1);
                    }
                    frmStudioBridge.pack();    
                }
            }
        });
        
        JMenuItem mntmMultiPrinterSetup = new JMenuItem(I18n.t("menu.multiPrinter"));
        mnSettings.add(mntmMultiPrinterSetup);
        
        mntmMultiPrinterSetup.addActionListener(arg -> new MultiPrinterSetup(frmStudioBridge));
        
        // Action listener for sending the data package
        btnSendPackage.addActionListener(arg -> {
            	
        	// Set new printer object from GUI data
        	Printer tempPrinter = new Printer(selectedProfile(cbxProfile), txtName.getText(), txtIP.getText(), txtSerial.getText(), selectedModel(cbxModel));
        	
            if (!isComplete(tempPrinter)) {
                new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("input.check"), I18n.t("common.back")).showDialog();
                return;
            }

            // Offer to save a new or changed profile, the data is sent either way
            if (tempPrinter.getProfileName().equals(NEW_PROFILE)) {
            	profiles.offerSaveNew(tempPrinter);
            } else if (Exit.unsavedState(tempPrinter, mainPrinter) == Exit.UnsavedState.CHANGED_PROFILE) {
            	profiles.offerSaveChanges(tempPrinter);
            }
            
            // Looking for the slicer can take a few seconds on macOS (lsof), so not on the EDT
            btnSendPackage.setEnabled(false);
            new SwingWorker<Integer, Void>() {
            	@Override protected Integer doInBackground() {
            		return UDPPackage.getAvailableUDPPort();
            	}
            	@Override protected void done() {
            		btnSendPackage.setEnabled(true);
            		int remoteUdpPort = 0;
            		try {
            			remoteUdpPort = get();
            		} catch (Exception e) {
            			e.printStackTrace();
            		}
            		if (remoteUdpPort > 0) {
            			// Send the data as currently entered, regardless of whether it was saved
            			UDPPackage.send(frmStudioBridge, tempPrinter, remoteUdpPort, false);
            		} else {
            			// Show a warning if Bambu Studio is not running
            			new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("send.slicerNotRunning"), I18n.t("common.ok")).showDialog();
            		}
            	}
            }.execute();
        });
        
        // Action listener for opening new GUI to check the printer stats
        btnCheckPrinter.addActionListener(arg -> {
				
			if (txtIP.getText().equals("")) {
				new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("input.ipRequired"), I18n.t("common.ok")).showDialog();
			} else {
				new PrinterCheck(frmStudioBridge, txtIP.getText());
			}
			       	
        });
        
        // Action listener for profile selection
        cbxProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                            	
            	if (cbxProfile.getSelectedItem() == null) {
            		// No selection: a new profile
            		setProfile(null);
            	} else {
            		// Construct the path to the profile configuration file.
            		String profilePath = MainMenu.ProfilesDir + System.getProperty("file.separator") + cbxProfile.getSelectedItem().toString() + ".sbp";
            		setProfile(profilePath);
            	}
                
                frmStudioBridge.pack();
            }
                        
            private void setProfile(String profile) {
            	
            	if (profile != null) {
            		Printer loaded = Config.loadProfile(frmStudioBridge, profile);
            		if (loaded == null) {
            			// The file is gone or unreadable: drop it from the list and start a new profile.
            			// Changing the list from inside its own listener does not fire, so do it afterwards.
            			String name = cbxProfile.getSelectedItem().toString();
            			SwingUtilities.invokeLater(() -> {
            				profiles.forget(name);
            				cbxProfile.setSelectedIndex(-1);
            			});
            			return;
            		}
                   	mainPrinter = loaded;
                	MainMenu.lastUsedProfile = mainPrinter.getProfileName();
                	txtName.setText(mainPrinter.getPrinterName());
                	txtIP.setText(mainPrinter.getIpAdress());
                	txtSerial.setText(mainPrinter.getSerialNumber());
                	// A model that is not in the list (old or empty profile) shows no model, not the previous one
                	cbxModel.setSelectedIndex(-1);
                	cbxModel.setSelectedItem(mainPrinter.getPrinterModel());
            	} else {
                   	mainPrinter = null;
                	MainMenu.lastUsedProfile = NEW_PROFILE;
                	txtName.setText("");
                	txtIP.setText("");
                	txtSerial.setText("");
                	cbxModel.setSelectedIndex(-1);
            	}
            }
        });
        
        // Window listener for handling close event
        frmStudioBridge.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	
            	String profile = selectedProfile(cbxProfile);

            	String name = txtName.getText();
            	String ip = txtIP.getText();
            	String serial = txtSerial.getText();

            	String model = (cbxModel.getSelectedItem() != null)
            	        ? cbxModel.getSelectedItem().toString()
            	        : "";
            	
            	// What is entered right now, compared on exit with the saved state in mainPrinter
            	Printer current = new Printer(profile, name, ip, serial, model);
            	
                Exit.closeApp(frmStudioBridge, current, profiles);
            }
        });
        
        getAllProfiles(frmStudioBridge, cbxProfile); // Load all profiles into the combo box
        
        JButton btnSaveProfile = new JButton("");
        btnSaveProfile.setToolTipText(I18n.t("profile.saveChanges"));
        btnSaveProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSaveProfile.setRequestFocusEnabled(false);
        btnSaveProfile.setMargin(new Insets(2, 2, 2, 2));
        btnSaveProfile.setContentAreaFilled(false);
        btnSaveProfile.setIcon(new FlatSVGIcon("icons/save.svg"));
        frmStudioBridge.getContentPane().add(btnSaveProfile, "cell 1 2,alignx right");
                
        // Menu with the other profile actions, so the profile list only holds real profiles
        JPopupMenu mnProfileActions = new JPopupMenu();
        JMenuItem miNewProfile = new JMenuItem(I18n.t("profile.new"), new FlatSVGIcon("icons/new.svg"));
        JMenuItem miImportProfile = new JMenuItem(I18n.t("profile.import"), new FlatSVGIcon("icons/import.svg"));
        JMenuItem miRenameProfile = new JMenuItem(I18n.t("profile.rename"), new FlatSVGIcon("icons/edit.svg"));
        JMenuItem miDeleteProfile = new JMenuItem(I18n.t("profile.deleteAction"), new FlatSVGIcon("icons/delete.svg"));
        mnProfileActions.add(miNewProfile);
        mnProfileActions.add(miImportProfile);
        mnProfileActions.addSeparator();
        mnProfileActions.add(miRenameProfile);
        mnProfileActions.add(miDeleteProfile);
        
        // Looks like the save button: a plain icon without a button frame
        JButton btnProfileActions = new JButton(new FlatSVGIcon("icons/menu.svg"));
        btnProfileActions.setToolTipText(I18n.t("profile.actions"));
        btnProfileActions.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnProfileActions.setMargin(new Insets(2, 2, 2, 2));
        btnProfileActions.setContentAreaFilled(false);
        btnProfileActions.setRequestFocusEnabled(false);
        btnProfileActions.addActionListener(arg -> {
        	// Renaming and deleting need a saved profile
        	boolean profileSelected = cbxProfile.getSelectedItem() != null;
        	miRenameProfile.setEnabled(profileSelected);
        	miDeleteProfile.setEnabled(profileSelected);
        	// Right-aligned to the button, so the menu opens inside the window
        	mnProfileActions.show(btnProfileActions, btnProfileActions.getWidth() - mnProfileActions.getPreferredSize().width, btnProfileActions.getHeight());
        });
        frmStudioBridge.getContentPane().add(btnProfileActions, "cell 1 2,alignx right");
        
        // New profile: clear the selection, the listener empties the fields
        miNewProfile.addActionListener(arg -> cbxProfile.setSelectedIndex(-1));
        
        // Import a profile file: it is stored in the profiles directory under a name the user picks
        miImportProfile.addActionListener(arg -> {
        	File importFile = FileChooser.chooseProfile(frmStudioBridge, I18n.t("profile.importTitle"));
        	if (importFile != null) profiles.importProfile(importFile);
        });
        
        // Copyright button
        JButton btnCopy = new JButton("© Niklas (Rdiger-36)");
        btnCopy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCopy.setMargin(new Insets(0, 0, 0, 0));
        btnCopy.setBorderPainted(false);
        btnCopy.setContentAreaFilled(false);
        btnCopy.setHorizontalAlignment(SwingConstants.LEFT);
        btnCopy.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        
        // Action listener for copyright button
        btnCopy.addActionListener(arg -> new ToolInfo(frmStudioBridge));
        pnlBottom.add(btnCopy, "aligny center");
        pnlBottom.add(btnSendPackage);
        pnlBottom.add(btnCheckPrinter);
        
        // Action listener for deleting profiles
        miDeleteProfile.addActionListener(arg -> {
        	String profile = selectedProfile(cbxProfile);
        	if (!notAllowedProfileNames.contains(profile)) profiles.delete(profile);
        });
                
        // Action listener for saving profiles
        btnSaveProfile.addActionListener(arg -> {
        	Printer tempPrinter = new Printer(selectedProfile(cbxProfile), txtName.getText(), txtIP.getText(), txtSerial.getText(), selectedModel(cbxModel));
        	if (!isComplete(tempPrinter)) {
        		new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("input.check"), I18n.t("common.back")).showDialog();
        		return;
        	}
        	if (notAllowedProfileNames.contains(tempPrinter.getProfileName())) {
        		profiles.saveNew(tempPrinter);
        	} else {
        		// Saving the selected profile replaces its own file, no need to ask
        		profiles.store(tempPrinter, null);
        	}
        });
        
        miRenameProfile.addActionListener(arg -> {
        	String oldProfileName = selectedProfile(cbxProfile);
        	if (notAllowedProfileNames.contains(oldProfileName)) return;
        	
        	Printer tempPrinter = new Printer(oldProfileName, txtName.getText(), txtIP.getText(), txtSerial.getText(), selectedModel(cbxModel));
        	String newName = profiles.askName(oldProfileName, I18n.t("common.save"));
        	if (newName == null) return;
        	
        	if (newName.equals(oldProfileName)) {
        		profiles.store(tempPrinter, null);
        	} else if (profiles.confirmOverwrite(newName, oldProfileName)) {
        		tempPrinter.setProfileName(newName);
        		profiles.store(tempPrinter, oldProfileName);
        	}
        });
        

        if(checkForUpdate) new Thread(() -> Update.checkVersion(frmStudioBridge), "update-check").start(); // Check for updates, if it is not disabled by --noupdate argument at startup
        
        if (jarInfo) {
        
	        int response = new DialogTwoButtons(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("jar.info"), I18n.t("jar.openGithub"), I18n.t("jar.neverShow")).showDialog();
	        
	        if (response == 0) {
	            try {
	                Desktop.getDesktop().browse(new URI("https://github.com/Rdiger-36/StudioBridge/"));
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        } else if (response == 1) {
	        	jarInfo = false;
	        } 
        }
        
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler( e -> {
            	new ToolInfo(frmStudioBridge);
            } );
        }
        
        frmStudioBridge.pack();    
        frmStudioBridge.setVisible(true);    
        
        // First start: no profile yet, so explain what to enter
        if (printerMap.isEmpty()) {
        	SwingUtilities.invokeLater(() -> new DialogOneButton(frmStudioBridge, null, Resize.setNewImageIconSize(new ImageIcon(MainMenu.class.getResource("/icon.png")), 48, 48), I18n.t("start.noProfiles"), I18n.t("common.ok")).showDialog());
        }
    }

    /**
     * Retrieves all profiles from the Profiles directory and adds them to the specified combo box.
     *
     * @param comboBox The combo box to which profiles will be added.
     */
    private void getAllProfiles(JFrame mainFrame, JComboBox<String> comboBox) {
        File directory = new File(ProfilesDir);

        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it does not exist
        }
        
        if (directory.exists() && directory.isDirectory()) {
            File dir = new File(ProfilesDir);
            File[] filesList = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".sbp"));
            
            // Filling the empty list selects its first entry, whose listener overwrites lastUsedProfile
            String remembered = lastUsedProfile;
            if (filesList != null) {
            	
            	printerMap.clear();
            	Arrays.sort(filesList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            	
                for (File file : filesList) {
                    if (file.isFile() && !file.getName().startsWith(".")) {
                    	
                    	Printer printer = Config.loadProfile(mainFrame, file.toString());
                    	if (printer == null) continue; // unreadable, already reported
                    	
                    	printerMap.put(printer.getProfileName(), printer);
                    	
                        comboBox.addItem(printer.getProfileName());
                    }
                }
            }
            
            if (rememberLastUsedProfile) {
            	if (((DefaultComboBoxModel<String>)comboBox.getModel()).getIndexOf(remembered) != -1) {
            		comboBox.setSelectedItem(remembered);
            	}
            }
            // Without a remembered profile the list keeps the first profile, which it selects when filled
            
        } else {
            new DialogOneButton(null, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("profile.directoryFailed", "path", ProfilesDir), I18n.t("common.close")).showDialog();
            System.exit(0);
        }
    }
    
}