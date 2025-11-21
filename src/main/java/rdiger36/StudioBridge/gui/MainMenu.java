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
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
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
    public static String defaultSavePath = System.getProperty("user.home") + System.getProperty("file.separator") + "StudioBridge";
    public static String savePath = defaultSavePath;
    public static String ProfilesDir = savePath + System.getProperty("file.separator") + "Profiles";
    
    // Application Name
    public static String title = "StudioBridge";
    
    // Application version
    public static String version = "210";
    
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
    
    // Set specific profile via cmdline
    public static boolean useSpecificProfiles = false;
    
    // Set visibility of jar version start
    public static boolean jarInfo = true;
    
    // Contains the name of the last used profile
    public static String lastUsedProfile;
    
    // Contains the names of profiles which are not allowed to set
    public static List<String> notAllowedProfileNames = new ArrayList<String>(List.of("New profile", "Import profile", "---", ""));
    
    // Contains the profiles which were set by CLI option
    public static List<String> profilesListCLI = new ArrayList<String>();
    
    // Create LocalMachine Object from localhost
    public static LocalMachine localMachine = new LocalMachine();
    
    // Create static Printer Object, will be used as selected printer for GUI
    public static Printer mainPrinter;
    
    // Create static HashMap containing printer objects with profilename as key, will be used for multi printer setup
    public static HashMap<String, Printer> printerMap;
    
    public static void main(String[] args) {
    	
    	// Get running OS
    	if (System.getProperty("jpackage.app-path") != null) {
            runningAsJar = false;
            jarInfo = false;
    	}
    	
        boolean sendOnly = false;
        boolean showHelp = false;

        Config.loadAppSettings();

        final Map<String, String> valid = new LinkedHashMap<>();

        valid.put("Starting options:", "");
        valid.put("--noupdate", "Skip search for latest updates on startup");
        valid.put("--sendonly", "Start with no GUI, only send data from all printers to Bambu Studio");
        valid.put("--direct", "Send UPD package directly to Bambu Studio, not over broadcast");
        valid.put("--help", "Show help for StudioBridge");

        valid.put("", "");

        valid.put("Profile options:", "");
        valid.put("--customProfilesDir='/path/to/profiles/'",
                  "Set custom profiles directory which will be loaded from this for StudioBridgeCLI");
        valid.put("--selectedPrinters='/path/to/profile.sbp/','/path/to/other/profile2.sbp/'",
                  "Set single profiles which will be used for StudioBridgeCLI (single and multi profiles comma-separated)");


        // Initialize arguments
        List<String> unknown = new ArrayList<>();

        for (String arg : args) {
            if ("--help".equals(arg)) {
                showHelp = true;
                break;
            }
            if ("--noupdate".equals(arg)) {
                checkForUpdate = false;
                continue;
            }
            
            if ("--direct".equals(arg)) {
                directMode = true;
                continue;
            }
            
            if ("--sendonly".equals(arg)) {
                sendOnly = true;
                continue;
            }
            
            if (arg.startsWith("--customProfilesDir")) {
            	ProfilesDir = arg.split("=")[1];
            	useCustomProfilesDir = true;
                continue;
            }
            
            if (arg.startsWith("--selectedPrinters")) {
            	String argProfiles = arg.split("=")[1];
            	String[] profiles = argProfiles.split(",");
            	
            	for (String profile : profiles) {
            		profilesListCLI.add(profile);
            	}
            	
            	useSpecificProfiles = true;
                continue;
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
        
        if (sendOnly) {
            startNoGUI();
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
    
    // print help as console output
    private static void printUsage(Map<String, String> valid) {

        String prog = "./StudioBridge.jar";

        if (!runningAsJar) {
            if (localMachine.getOs().equals("windows")) {
                prog = "./StudioBridgeCLI.exe";
            } else if (localMachine.getOs().equals("linux")) {
                prog = "./StudioBridge.appimage";
            } else if (localMachine.getOs().equals("macos")) {
                prog = "/Applications/StudioBridge.app/Contents/MacOS/StudioBridgeCLI";
            }
        }

        System.out.println("Usage:");
        System.out.println("  " + prog + " [OPTIONS]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  You can combine every option with each other");
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
           	System.out.println("  " + prog + " --sendonly --selectedPrinters='/Users/rdiger-36/Documents/My P1S.sbp','/Users/rdiger-36/Desktop/Profiles/H2D-Pro Work.sbp.sbp'");
        }
    }


    private static void printHeader() {
        String version = MainMenu.version.substring(0, 1) + "." + MainMenu.version.substring(1, 2) + "." + MainMenu.version.substring(2, 3);
        System.out.println("*** " + title + " by Rdiger-36 v." + version + " ***\n");
        
        if (runningAsJar) {
        	System.out.println("You are currently running this application in raw mode (via .jar)!");
        	System.out.println("For the official version for your OS and CPU architecture, please visit the GitHub repository.");
        	System.out.println("https://github.com/Rdiger-36/StudioBridge/\n");
        }
    	
    }


    // CLI mode, no GUI
    private static void startNoGUI() {
    	
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
	                    try (FileReader reader = new FileReader(file)) {
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
	                    	String successMsg = "Successfully sended package via " + sendOverMethod + " to Bambu Studio: " + printer.getPrinterName()
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
	                System.out.println("All Packages successfully sent. The printers(s) will appear in the nect 60 seconds in Bambu Studio.");
	            }
	            System.out.println("The programm stays online for the next 60 seconds and will exit automatically.");
	
	        } else {
	            System.err.println("Warning! Bambu Studio is not running!");
	        }
        } else {
            System.err.println("Warning! There are no useable profiles in your profiles directory:");
            System.err.println(profilesDir);
        }
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
        frmStudioBridge.setIconImage(Toolkit.getDefaultToolkit().getImage(MainMenu.class.getResource("/icon.png")));
        frmStudioBridge.setTitle(title);
        frmStudioBridge.setSize(299, 235);
        frmStudioBridge.setLocationRelativeTo(frame);
        frmStudioBridge.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frmStudioBridge.getContentPane().setLayout(new MigLayout("", "[76.00px][]", "[35px:n,grow][10px:10px:10px][][][][][][10px:n][35px:n,grow]"));

        // Add informational label
        JLabel lblInfo = new JLabel("<html>This program sends a specific data package to Bambu<br>Studio. This allows the printer to be used in different networks</html>");
        frmStudioBridge.getContentPane().add(lblInfo, "cell 0 0 2 1,growx,aligny center");
        
        // Separator
        JSeparator separator = new JSeparator();
        frmStudioBridge.getContentPane().add(separator, "cell 0 1 2 1,growx,aligny center");
        
        // Profile selection components
        JLabel lblProfile = new JLabel("Profile");
        frmStudioBridge.getContentPane().add(lblProfile, "flowy,cell 0 2,grow");
        
        JComboBox<String> cbxProfile = new JComboBox<>();
        cbxProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbxProfile.setModel(new DefaultComboBoxModel<String>(new String[] {"New profile", "Import profile", "---"}));
        frmStudioBridge.getContentPane().add(cbxProfile, "flowx,cell 1 2,growx");

        // Printer information labels
        JLabel lblIP = new JLabel("IP-Address");
        frmStudioBridge.getContentPane().add(lblIP, "cell 0 3,growx,aligny center");

        JLabel lblSerial = new JLabel("Serial number");
        frmStudioBridge.getContentPane().add(lblSerial, "flowx,cell 0 4,growx,aligny center");

        JLabel lblDevModel = new JLabel("Model");
        frmStudioBridge.getContentPane().add(lblDevModel, "flowx,cell 0 5,growx,aligny center");

        String[] items = {
        	    "A-Series", "A1", "A1 Mini",
        	    "H-Series", "H2C", "H2C (new revision)", "H2D", "H2D Pro", "H2S",
        	    "P-Series", "P1P", "P1S", "P2S",
        	    "X-Series", "X1", "X1C", "X1E"
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
                    lbl.setText("Please select a printer model");
                    return lbl;
                }

                String text = value.toString();
                boolean isCategory = text.endsWith("-Series");

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
        JLabel lblDevName = new JLabel("Name");
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
        JButton btnSendPackage = new JButton("Send package");
        btnSendPackage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        frmStudioBridge.getContentPane().add(btnSendPackage, "cell 0 8");
        
        JButton btnCheckPrinter = new JButton("Check Printer");
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
        
        JMenu mnSettings = new JMenu("Settings");
        menuBar.add(mnSettings);
        
        JCheckBoxMenuItem cbxmntmDarkmode = new JCheckBoxMenuItem("Darkmode");
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
        
        JCheckBoxMenuItem cbxmntmRememberLastProfile = new JCheckBoxMenuItem("Remember last used profile");
        cbxmntmRememberLastProfile.setSelected(rememberLastUsedProfile);
        mnSettings.add(cbxmntmRememberLastProfile);
        
        cbxmntmRememberLastProfile.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rememberLastUsedProfile = cbxmntmRememberLastProfile.isSelected();
            }
        });
        
        JCheckBoxMenuItem cbxmntmSetSendingMode = new JCheckBoxMenuItem("Send packages directly");
        cbxmntmSetSendingMode.setSelected(directMode);
        mnSettings.add(cbxmntmSetSendingMode);
        
        cbxmntmSetSendingMode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
            	
            	if (cbxmntmSetSendingMode.isSelected()) {
            		new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>The mode for sending UDP packets has been changed to localhost!<br>Data will now be sent directly to Bambu Studio on your local machine.</html>", "Ok").showDialog();
            	} else {
            		new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>The mode for sending UDP packets has been changed to broadcast!<br>Data will now be sent to the broadcast address of your current network.</html>", "Ok").showDialog();
            	}
            	
                directMode = cbxmntmSetSendingMode.isSelected();
            }
        });
        
        mnSettings.add(new JSeparator());
        
        JMenuItem mntmChangeProfilesPath = new JMenuItem("Set custom profiles path");
        mnSettings.add(mntmChangeProfilesPath);
        
        // Action listener for changing the profiles path
        mntmChangeProfilesPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newProfilesDir = new ProfilesPath(frmStudioBridge).changePath();
                
                if (newProfilesDir.length() > 0 && new File(newProfilesDir).exists()) {
                    ProfilesDir = newProfilesDir;
                    cbxProfile.removeAllItems();
                    cbxProfile.addItem("New profile");
                    cbxProfile.addItem("Import profile");
                    cbxProfile.addItem("---");
                    getAllProfiles(frmStudioBridge, cbxProfile);
                    cbxProfile.setSelectedIndex(0);
                    mainPrinter = new Printer("New profile", "", "", "", "No model");
                    frmStudioBridge.pack();    
                }
            }
        });
        
        JMenuItem mntmMultiPrinterSetup = new JMenuItem("Multiple Printer Setup");
        mnSettings.add(mntmMultiPrinterSetup);
        
        mntmMultiPrinterSetup.addActionListener(arg -> new MultiPrinterSetup(frmStudioBridge));
        
        // Action listener for sending the data package
        btnSendPackage.addActionListener(arg -> {
            	
        	// Set new printer object from GUI data
        	Printer tempPrinter = new Printer(cbxProfile.getSelectedItem().toString(), txtName.getText(), txtIP.getText(), txtSerial.getText(), cbxModel.getSelectedItem().toString());
        	
            if (tempPrinter.getIpAdress().equals("") || tempPrinter.getSerialNumber().equals("") || tempPrinter.getPrinterName().equals("")) {
                new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Attention! Please check your inputs!</html>", "Back").showDialog();
                return;
            }

            // Check if the input data is changed or new
            if (tempPrinter.getProfileName().equals("New profile")) {
            	tempPrinter.setProfileName(Config.saveUnsavedNewProfile(frame, tempPrinter));
                if (!tempPrinter.getProfileName().equals("")) {
                    cbxProfile.addItem(tempPrinter.getProfileName());
                    Config.saveProfile(frame, tempPrinter);
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
            	UDPPackage.send(frmStudioBridge, mainPrinter, remoteUdpPort, false);
            } else {
                // Show a warning if Bambu Studio is not running
            	new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Bambu Studio is not running!</html>", "Ok").showDialog();
            }                      
        });
        
        // Action listener for opening new GUI to check the printer stats
        btnCheckPrinter.addActionListener(arg -> {
				
			if (txtIP.getText().equals("")) {
				new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>You must enter a valid IP-Address!</html>", "Ok").showDialog();
			} else {
				new Thread(() -> new PrinterCheck(frmStudioBridge, txtIP.getText())).start();
			}
			       	
        });
        
        // Action listener for profile selection
        cbxProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                            	
            	if (cbxProfile.getSelectedItem() != null) {
                                	
                	if (cbxProfile.getSelectedItem().toString().equals("Import profile")) {
                	                		
                		JnaFileChooser fc = new JnaFileChooser();
                        fc.setMode(Mode.Files);
                        fc.setTitle("Choose profile save directory");

                        boolean returnValue = fc.showOpenDialog(frame);
                        if (returnValue) {
                        	String profilePath = fc.getSelectedFile().getAbsolutePath();
                        	String profileName = fc.getSelectedFile().getName().replace(".sbp", "");
                    		
                        	profileName = new SaveDialog(frmStudioBridge, "New imported profile", "Set profile name").saveProfile();
                        	
                        	if (profileName != null) {
                            	if (profileName.equals("")) profileName = "New imported profile";
                            	
                            	setProfile(profilePath);
                            	cbxProfile.addItem(profileName);
                            	cbxProfile.setSelectedItem(profileName);
                            	mainPrinter.setProfileName(profileName);
                            	Config.saveProfile(frmStudioBridge, mainPrinter);
                            	
                        	}
                        	
                        } else {
                        	cbxProfile.setSelectedItem("New profile");
                        }
                		
                	} else if (cbxProfile.getSelectedItem().toString().equals("---")) {
                    	
                		if (MainMenu.lastUsedProfile != null) {
                		
                        	if (!notAllowedProfileNames.contains(MainMenu.lastUsedProfile)) {
                        		cbxProfile.setSelectedItem(MainMenu.lastUsedProfile);
                        	} else {
                        		cbxProfile.setSelectedItem("New profile");
                        		cbxModel.setSelectedIndex(-1);
                        	}
                			
                		} else {
                			cbxProfile.setSelectedItem("New profile");
                			cbxModel.setSelectedIndex(-1);
                		}
                		
                		
                	} else if (cbxProfile.getSelectedItem().toString().equals("New profile")) {
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
                	MainMenu.lastUsedProfile = "New profile";
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
                		if (mainPrinter == null) mainPrinter = new Printer("New profile", "", "", "", "no model");
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
            	
                    int response = new DialogTwoButtons(frmStudioBridge, null,new ImageIcon(MainMenu.class.getResource("/achtung.png")), "Attention! Do you want to delete the profile: \"" + cbxProfile.getSelectedItem() + "\"?", "Yes", "No").showDialog();
                    
                    if (response == 0) {
                        new File(ProfilesDir + System.getProperty("file.separator") + cbxProfile.getSelectedItem() + ".sbp").delete();
                        cbxProfile.removeItem(cbxProfile.getSelectedItem());
                        cbxProfile.setSelectedIndex(0);
                        mainPrinter = new Printer("New profile", "", "", "", "no model");
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
	                
	                if (tempPrinter.getProfileName().equals("New profile")) {
	                	
	                	String profileName = new SaveDialog(frmStudioBridge, tempPrinter.getPrinterName(), "Save").saveProfile();
	                	
	                	if (profileName == null) {
	                		return;
	                	} else {
	                		tempPrinter.setProfileName(profileName);
	                	}
	                }
	                
	                if (!notAllowedProfileNames.contains(tempPrinter.getProfileName())) {
	                    if (!profileExists(tempPrinter.getProfileName())) {
	                        cbxProfile.addItem(tempPrinter.getProfileName());
	                        Config.saveProfile(frmStudioBridge, tempPrinter);
	                        cbxProfile.setSelectedItem(tempPrinter.getProfileName());
	                        frmStudioBridge.pack();    
	                    } else {
	                        int result = new DialogTwoButtons(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "Warning! The profile: " + tempPrinter.getProfileName() + " already exists, do you want to overwrite it?", "Yes", "No").showDialog();
	                        if (result == 0) {
	                            Config.saveProfile(frmStudioBridge, tempPrinter);
	                        }
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
        		int oldProfilePosition = cbxProfile.getSelectedIndex();
        		
        		if (!notAllowedProfileNames.contains(oldProfileName)) {
        		
	            	Printer tempPrinter = new Printer(cbxProfile.getSelectedItem().toString(), txtName.getText(), txtIP.getText(), txtSerial.getText(), cbxModel.getSelectedItem().toString());
	                	
	            	String profileName = new SaveDialog(frmStudioBridge, tempPrinter.getProfileName(), "Save").saveProfile();
	            	
	            	if (profileName == null) {
	            		return;
	            	} else {
	            		tempPrinter.setProfileName(profileName);
	            	}
	                
	                if (!notAllowedProfileNames.contains(tempPrinter.getProfileName())) {
	                    
	                    cbxProfile.addItem(tempPrinter.getProfileName());
	                    Config.saveProfile(frmStudioBridge, tempPrinter);
	                    cbxProfile.setSelectedItem(tempPrinter.getProfileName());
	                    
	                    cbxProfile.removeItemAt(oldProfilePosition);
	                    
	                    printerMap.remove(oldProfileName);
	                    printerMap.put(tempPrinter.getProfileName(), tempPrinter);
	                    
	                    new File(ProfilesDir + System.getProperty("file.separator") + oldProfileName + ".sbp").delete();
	                    
	                    mainPrinter = tempPrinter;
	                    
	                    frmStudioBridge.pack();    
	                }
        		}
                
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
        });
        

        if(checkForUpdate) new Thread(() -> Update.checkVersion(frmStudioBridge)).start(); // Check for updates, if it is not disabled by --noupdate argument at startup
        
        if (jarInfo) {
        
	        int response = new DialogTwoButtons(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>You are currently running this application in raw mode (via .jar).<br>"
	        		+ "For the official version for your OS and CPU architecture, please visit the GitHub repository.\r\n"
	        		+ "", "Open GitHub", "Close and never show again").showDialog();
	        
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
            
        } else {
            new DialogOneButton(null, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "Error! Directory " + ProfilesDir + " could not be created!", "Close").showDialog();
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