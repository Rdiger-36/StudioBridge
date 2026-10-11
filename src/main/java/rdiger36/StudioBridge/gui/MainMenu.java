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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AbstractDocument;
import jnafilechooser.api.JnaFileChooser;
import jnafilechooser.api.JnaFileChooser.Mode;
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
    
    // Set boolean for darkmode for GUI
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
    public static List<String> notAllowedProfileNames = new ArrayList<String>(List.of("New profile", "Import profile", "---", ""));
    
    // The entries of the profile list that are not profiles, in the language of the GUI
    public static String NEW_PROFILE = "New profile";
    public static String IMPORT_PROFILE = "Import profile";
    
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
     * Applies the language setting to the texts of the GUI. The special entries of the profile
     * list are reserved in every language, so a last used profile stored under another language
     * is never taken for a real profile.
     */
    static void applyLanguage() {
    	I18n.setLanguage(language);
    	NEW_PROFILE = I18n.t("profile.new");
    	IMPORT_PROFILE = I18n.t("profile.import");
    	for (String code : I18n.LANGUAGES) {
    		for (String key : new String[] {"profile.new", "profile.import"}) {
    			String name = I18n.raw(code, key);
    			if (name != null && !notAllowedProfileNames.contains(name)) notAllowedProfileNames.add(name);
    		}
    	}
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
        valid.put("--selectedPrinters='/path/to/profile.sbp/','/path/to/other/profile2.sbp/'",
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
        	UI.changeLAF(darkmode); // Change the look and feel based on the dark mode setting
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
                    continue;
                }

                if (!f.getName().toLowerCase().endsWith(".sbp")) {
                    System.err.println("Invalid profile format (must end with .sbp): " + path);
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
	                    Properties properties = new Properties();
	                    try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
	                        properties.load(reader);
	                    } catch (IOException e) {
	                        System.err.println("Error loading file: " + file.getName());
	                        e.printStackTrace();
	                        continue;  // Process next file on error
	                    }
	
	                    Printer printer = new Printer(file.getName() ,properties.getProperty("PrinterName"), properties.getProperty("IP-Address"), properties.getProperty("PrinterSN"), properties.getProperty("PrinterType"));
	
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
	
	                try {
	                    Thread.sleep(1000); // Simulated delay
	                } catch (InterruptedException e) {
	                    Thread.currentThread().interrupt();
	                    break;
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
        cbxProfile.setModel(new DefaultComboBoxModel<String>(new String[] {NEW_PROFILE, IMPORT_PROFILE, "---"}));
        frmStudioBridge.getContentPane().add(cbxProfile, "flowx,cell 1 2,growx");

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
        txtName.setText("");
        txtName.setColumns(10);
        ((AbstractDocument) txtName.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(25));
        frmStudioBridge.getContentPane().add(txtName, "cell 1 6,growx");
        
        // Separator
        JSeparator separator_1 = new JSeparator();
        frmStudioBridge.getContentPane().add(separator_1, "cell 0 7 2 1,growx,aligny center");
        
        // Button to send package
        JButton btnSendPackage = new JButton(I18n.t("main.sendPackage"));
        btnSendPackage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        frmStudioBridge.getContentPane().add(btnSendPackage, "cell 0 8");
        
        JButton btnCheckPrinter = new JButton(I18n.t("main.checkPrinter"));
        btnCheckPrinter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));  
        frmStudioBridge.getContentPane().add(btnCheckPrinter, "flowx,cell 1 8");

        // Input fields for IP and Serial number
        JTextField txtIP = new JTextField();
        frmStudioBridge.getContentPane().add(txtIP, "flowx,cell 1 3,growx");
        txtIP.setColumns(10);
        ((AbstractDocument) txtIP.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(15));
        
        JTextField txtSerial = new JTextField();
        txtSerial.setText("");
        txtSerial.setColumns(10);
        ((AbstractDocument) txtSerial.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(25));
        frmStudioBridge.getContentPane().add(txtSerial, "cell 1 4,growx");
        
        // Menu bar and settings options
        JMenuBar menuBar = new JMenuBar();
        frmStudioBridge.setJMenuBar(menuBar);
        
        JMenu mnSettings = new JMenu(I18n.t("menu.settings"));
        menuBar.add(mnSettings);
        
        JCheckBoxMenuItem cbxmntmDarkmode = new JCheckBoxMenuItem(I18n.t("menu.darkmode"));
        mnSettings.add(cbxmntmDarkmode);
        cbxmntmDarkmode.setSelected(darkmode);
        
        // Action listener for dark mode toggle
        cbxmntmDarkmode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
            	
            	boolean dark = cbxmntmDarkmode.isSelected();

            	Point loc = frmStudioBridge.getLocationOnScreen();
            	int state = frmStudioBridge.getExtendedState();

            	UI.changeLAF(dark);
            	SwingUtilities.updateComponentTreeUI(frmStudioBridge);
            	frmStudioBridge.pack();
            	frmStudioBridge.setLocation(loc);
            	frmStudioBridge.setExtendedState(state);
            	frmStudioBridge.revalidate();
            	frmStudioBridge.repaint();
            }
        });
        
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
                    ProfilesDir = newProfilesDir;
                    useCustomProfilesDir = false; // A choice in the menu is saved again
                    cbxProfile.removeAllItems();
                    cbxProfile.addItem(NEW_PROFILE);
                    cbxProfile.addItem(IMPORT_PROFILE);
                    cbxProfile.addItem("---");
                    getAllProfiles(frmStudioBridge, cbxProfile);
                    if (cbxProfile.getItemCount() <= 3) {
                        cbxProfile.setSelectedIndex(0);
                        mainPrinter = new Printer(NEW_PROFILE, "", "", "", "No model");
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
        	Printer tempPrinter = new Printer(cbxProfile.getSelectedItem().toString(), txtName.getText(), txtIP.getText(), txtSerial.getText(), cbxModel.getSelectedItem().toString());
        	
            if (tempPrinter.getIpAdress().equals("") || tempPrinter.getSerialNumber().equals("") || tempPrinter.getPrinterName().equals("")) {
                new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("input.check"), I18n.t("common.back")).showDialog();
                return;
            }

            // Check if the input data is changed or new
            if (tempPrinter.getProfileName().equals(NEW_PROFILE)) {
            	tempPrinter.setProfileName(Config.saveUnsavedNewProfile(frame, tempPrinter));
                if (!tempPrinter.getProfileName().equals("")) {
                    cbxProfile.addItem(tempPrinter.getProfileName());
                    cbxProfile.setSelectedItem(tempPrinter.getProfileName());
                    frmStudioBridge.pack();    
                    mainPrinter = tempPrinter;
                } 
            } else if (!tempPrinter.getIpAdress().equals(mainPrinter.getIpAdress()) || !tempPrinter.getSerialNumber().equals(mainPrinter.getSerialNumber()) || !tempPrinter.getPrinterName().equals(mainPrinter.getPrinterName()) || !tempPrinter.getPrinterModel().equals(mainPrinter.getPrinterModel())) {
                if (Config.saveUnsavedProfile(frame, tempPrinter)) {
                	mainPrinter = tempPrinter;
                }
            } 
                            
    		// Determine which UDP port (2021 or 1990) is in use, or set to 0 if none is available
            int remoteUdpPort = UDPPackage.getAvailableUDPPort();

            // Check if a valid port was assigned to remoteUdpPort
            if (remoteUdpPort > 0) {
            	// Send the data as currently entered, regardless of whether it was saved
            	UDPPackage.send(frmStudioBridge, tempPrinter, remoteUdpPort, false);
            } else {
                // Show a warning if Bambu Studio is not running
            	new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("send.slicerNotRunning"), I18n.t("common.ok")).showDialog();
            }                      
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
                            	
            	if (cbxProfile.getSelectedItem() != null) {
                                	
                	if (cbxProfile.getSelectedItem().toString().equals(IMPORT_PROFILE)) {
                	                		
                		JnaFileChooser fc = new JnaFileChooser();
                        fc.setMode(Mode.Files);
                        fc.setTitle(I18n.t("profile.importTitle"));

                        boolean returnValue = fc.showOpenDialog(frame);
                        if (returnValue) {
                        	String profilePath = fc.getSelectedFile().getAbsolutePath();
                        	String profileName = fc.getSelectedFile().getName().replace(".sbp", "");
                    		
                        	profileName = new SaveDialog(frmStudioBridge, I18n.t("profile.importedName"), I18n.t("profile.setName")).saveProfile();
                        	
                        	if (profileName != null) {
                            	profileName = profileName.trim();
                            	if (profileName.equals("")) profileName = I18n.t("profile.importedName");
                            	
                            	setProfile(profilePath);
                            	mainPrinter.setProfileName(profileName);
                            	
                            	// Only list the imported profile if it could be saved
                            	if (Config.saveProfile(frmStudioBridge, mainPrinter)) {
                            		if (!profileExists(profileName)) cbxProfile.addItem(profileName);
                            		cbxProfile.setSelectedItem(profileName);
                            		printerMap.put(profileName, mainPrinter);
                            	} else {
                            		cbxProfile.setSelectedItem(NEW_PROFILE);
                            	}
                            	
                        	}
                        	
                        } else {
                        	cbxProfile.setSelectedItem(NEW_PROFILE);
                        }
                		
                	} else if (cbxProfile.getSelectedItem().toString().equals("---")) {
                    	
                		if (MainMenu.lastUsedProfile != null) {
                		
                        	if (!notAllowedProfileNames.contains(MainMenu.lastUsedProfile)) {
                        		cbxProfile.setSelectedItem(MainMenu.lastUsedProfile);
                        	} else {
                        		cbxProfile.setSelectedItem(NEW_PROFILE);
                        		cbxModel.setSelectedIndex(-1);
                        	}
                			
                		} else {
                			cbxProfile.setSelectedItem(NEW_PROFILE);
                			cbxModel.setSelectedIndex(-1);
                		}
                		
                		
                	} else if (cbxProfile.getSelectedItem().toString().equals(NEW_PROFILE)) {
                		String profilePath = null;  
                        setProfile(profilePath);
            		} else {
                	    // Construct the path to the profile configuration file.
                	    String profilePath = MainMenu.ProfilesDir + System.getProperty("file.separator") + cbxProfile.getSelectedItem().toString() + ".sbp";   
                        setProfile(profilePath);
                	}
                }
                
                frmStudioBridge.pack();
            }
                        
            private void setProfile(String profile) {
            	
            	if (profile != null) {
                   	mainPrinter = Config.loadProfile(frmStudioBridge, profile);
                	MainMenu.lastUsedProfile = mainPrinter.getProfileName();
                	txtName.setText(mainPrinter.getPrinterName());
                	txtIP.setText(mainPrinter.getIpAdress());
                	txtSerial.setText(mainPrinter.getSerialNumber());
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
            	
            	String profile = (cbxProfile.getSelectedItem() != null)
            	        ? cbxProfile.getSelectedItem().toString()
            	        : "";

            	String name = txtName.getText().trim();
            	String ip = txtIP.getText().trim();
            	String serial = txtSerial.getText().trim();

            	String model = (cbxModel.getSelectedItem() != null)
            	        ? cbxModel.getSelectedItem().toString()
            	        : "";
            	
            	boolean isValid =
            	        !profile.isEmpty() &&
            	        !name.isEmpty() &&
            	        !ip.isEmpty() &&
            	        !serial.isEmpty() &&
            	        !model.isEmpty();

            	if (mainPrinter == null) {
                	if (isValid) {
                		if (mainPrinter == null) mainPrinter = new Printer(cbxProfile.getSelectedItem().toString()
                    			,txtName.getText()
                    			,txtIP.getText()
                    			,txtSerial.getText()
                    			,cbxModel.getSelectedItem().toString());
                	} else {
                		if (mainPrinter == null) mainPrinter = new Printer(NEW_PROFILE, "", "", "", "no model");
                	}
            	}
	            	
                // List to store all profiles except for the special entries
                ArrayList<String> profileList = new ArrayList<>();

                // Iterate through all items in the JComboBox
                for (int i = 0; i < cbxProfile.getItemCount(); i++) {
                    String item = cbxProfile.getItemAt(i);
                    
                    // Only add profiles that are not "New profile", "Import profile", or "---"
                    if (!notAllowedProfileNames.contains(item)) {
                        profileList.add(item);
                    }
                }
            	
                Exit.closeApp(frmStudioBridge, profileList, mainPrinter);
            }
        });
        
        getAllProfiles(frmStudioBridge, cbxProfile); // Load all profiles into the combo box
        
        JButton btnEditProfile = new JButton("");
        btnEditProfile.setRequestFocusEnabled(false);
        btnEditProfile.setMargin(new Insets(2, 2, 2, 2));
        btnEditProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEditProfile.setContentAreaFilled(false);
        btnEditProfile.setIcon(Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/edit.png")), 16, 16));
        frmStudioBridge.getContentPane().add(btnEditProfile, "cell 1 2,alignx right");
        
        JButton btnSaveProfile = new JButton("");
        btnSaveProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSaveProfile.setRequestFocusEnabled(false);
        btnSaveProfile.setMargin(new Insets(2, 2, 2, 2));
        btnSaveProfile.setContentAreaFilled(false);
        btnSaveProfile.setIcon(Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/save.png")), 16, 16));
        frmStudioBridge.getContentPane().add(btnSaveProfile, "cell 1 2,alignx right");
                
        // Buttons for profile actions
        JButton btnDeleteProfile = new JButton("");
        btnDeleteProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDeleteProfile.setMargin(new Insets(2, 2, 2, 2));
        btnDeleteProfile.setContentAreaFilled(false);
        btnDeleteProfile.setRequestFocusEnabled(false);
        btnDeleteProfile.setIcon(Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/delete.png")), 16, 16));
        frmStudioBridge.getContentPane().add(btnDeleteProfile, "cell 1 2,alignx right");
        
        // Copyright button
        JButton btnCopy = new JButton("© Niklas (Rdiger-36)");
        btnCopy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCopy.setMargin(new Insets(0, 0, 0, 0));
        btnCopy.setBorderPainted(false);
        btnCopy.setContentAreaFilled(false);
        btnCopy.setHorizontalAlignment(SwingConstants.RIGHT);
        btnCopy.setVerticalAlignment(SwingConstants.BOTTOM);
        btnCopy.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        
        // Action listener for copyright button
        btnCopy.addActionListener(arg -> new ToolInfo(frmStudioBridge));
        frmStudioBridge.getContentPane().add(btnCopy, "cell 1 8,alignx right");
        
        // Action listener for deleting profiles
        btnDeleteProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
            	String profile = cbxProfile.getSelectedItem().toString();
            	
            	if (!notAllowedProfileNames.contains(profile)) {
            	
                    int response = new DialogTwoButtons(frmStudioBridge, null,new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("profile.delete", "name", cbxProfile.getSelectedItem()), I18n.t("common.yes"), I18n.t("common.no")).showDialog();
                    
                    if (response == 0) {
                        new File(ProfilesDir + System.getProperty("file.separator") + cbxProfile.getSelectedItem() + ".sbp").delete();
                        cbxProfile.removeItem(cbxProfile.getSelectedItem());
                        cbxProfile.setSelectedIndex(0);
                        mainPrinter = new Printer(NEW_PROFILE, "", "", "", "no model");
                        printerMap.remove(profile);
                        cbxModel.setSelectedIndex(-1);
                        frmStudioBridge.pack();    
                    }
            	}
            }
        });
                
        // Action listener for saving profiles
        btnSaveProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {          
            	
            	try {
            	
	            	Printer tempPrinter = new Printer(cbxProfile.getSelectedItem().toString(), txtName.getText(), txtIP.getText(), txtSerial.getText(), cbxModel.getSelectedItem().toString());
	                
	                if (tempPrinter.getProfileName().equals(NEW_PROFILE)) {
	                	
	                	String profileName = new SaveDialog(frmStudioBridge, tempPrinter.getPrinterName(), I18n.t("common.save")).saveProfile();
	                	
	                	if (profileName == null) {
	                		return;
	                	} else {
	                		tempPrinter.setProfileName(profileName.trim());
	                	}
	                }
	                
	                if (!notAllowedProfileNames.contains(tempPrinter.getProfileName())) {
	                    if (!profileExists(tempPrinter.getProfileName())) {
	                        if (!Config.saveProfile(frmStudioBridge, tempPrinter)) return;
	                        cbxProfile.addItem(tempPrinter.getProfileName());
	                        cbxProfile.setSelectedItem(tempPrinter.getProfileName());
	                        frmStudioBridge.pack();    
	                    } else {
	                        int result = new DialogTwoButtons(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("profile.exists", "name", tempPrinter.getProfileName()), I18n.t("common.yes"), I18n.t("common.no")).showDialog();
	                        if (result != 0 || !Config.saveProfile(frmStudioBridge, tempPrinter)) return;
	                    }
	                    
	                    mainPrinter = tempPrinter;
	                    
	                    if (printerMap.containsKey(mainPrinter.getProfileName())) {
	                    	printerMap.replace(mainPrinter.getProfileName(), mainPrinter);
	                    } else {
	                    	printerMap.put(mainPrinter.getProfileName(), tempPrinter);
	                    }
	                    
	                }
	            } catch (Exception ex) {}
            	
            }
        });
        
        btnEditProfile.addActionListener(arg -> {
        	try {
            	
        		String oldProfileName = cbxProfile.getSelectedItem().toString();
        		
        		if (!notAllowedProfileNames.contains(oldProfileName)) {
        		
	            	Printer tempPrinter = new Printer(cbxProfile.getSelectedItem().toString(), txtName.getText(), txtIP.getText(), txtSerial.getText(), cbxModel.getSelectedItem().toString());
	                	
	            	String profileName = new SaveDialog(frmStudioBridge, tempPrinter.getProfileName(), I18n.t("common.save")).saveProfile();
	            	
	            	if (profileName == null) {
	            		return;
	            	} else {
	            		tempPrinter.setProfileName(profileName.trim());
	            	}
	                
	                if (!notAllowedProfileNames.contains(tempPrinter.getProfileName())) {
	                	
	                	boolean renamed = !tempPrinter.getProfileName().equals(oldProfileName);
	                	
	                	// Do not silently overwrite another existing profile
	                	if (renamed && profileExists(tempPrinter.getProfileName())) {
	                		int result = new DialogTwoButtons(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("profile.exists", "name", tempPrinter.getProfileName()), I18n.t("common.yes"), I18n.t("common.no")).showDialog();
	                		if (result != 0) return;
	                	}
	                	
	                	// Keep the old profile if the new one could not be saved
	                	if (!Config.saveProfile(frmStudioBridge, tempPrinter)) return;
	                	
	                	if (renamed) {
	                		if (profileExists(tempPrinter.getProfileName())) cbxProfile.removeItem(tempPrinter.getProfileName());
	                		cbxProfile.insertItemAt(tempPrinter.getProfileName(), cbxProfile.getSelectedIndex());
	                		cbxProfile.removeItem(oldProfileName);
	                		cbxProfile.setSelectedItem(tempPrinter.getProfileName());
	                		
	                		printerMap.remove(oldProfileName);
	                		
	                		// Only delete the old file after the profile was saved under its new name
	                		new File(ProfilesDir + System.getProperty("file.separator") + oldProfileName + ".sbp").delete();
	                	}
	                	
	                	printerMap.put(tempPrinter.getProfileName(), tempPrinter);
	                    
	                    mainPrinter = tempPrinter;
	                    
	                    frmStudioBridge.pack();    
	                }
        		}
                
            } catch (Exception ex) {
            	ex.printStackTrace();
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
            if (filesList != null) {
            	
            	printerMap.clear();
            	
                for (File file : filesList) {
                    if (file.isFile() && !file.getName().startsWith(".")) {
                    	
                    	Printer printer = Config.loadProfile(mainFrame, file.toString());
                    	
                    	printerMap.put(printer.getProfileName(), printer);
                    	
                        comboBox.addItem(printer.getProfileName());
                    }
                }
            }
            
            if (rememberLastUsedProfile) {
            	if (((DefaultComboBoxModel<String>)comboBox.getModel()).getIndexOf(lastUsedProfile) != -1) {
            		comboBox.setSelectedItem(lastUsedProfile);
            	}
            }
            // If still on "New profile" and real profiles exist, default to the first one
            if (NEW_PROFILE.equals(comboBox.getSelectedItem()) && comboBox.getItemCount() > 3) {
            	comboBox.setSelectedIndex(3);
            }
            
        } else {
            new DialogOneButton(null, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("profile.directoryFailed", "path", ProfilesDir), I18n.t("common.close")).showDialog();
            System.exit(0);
        }
    }
    
    /**
     * Checks if a profile already exists in the combo box.
     *
     * @param comboBox The combo box containing the profiles.
     * @param entry The profile name to check.
     * @return True if the profile exists, false otherwise.
     */
    private static boolean profileExists(String entry) {
    	if (MainMenu.printerMap.containsKey(entry)) {
    		return true; // Entry exists
    	} else {
    		 return false; // Entry does not exist
    	}
    }
}