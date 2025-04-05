package rdiger36.StudioBridge;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import jnafilechooser.api.JnaFileChooser;
import jnafilechooser.api.JnaFileChooser.Mode;
import net.miginfocom.swing.MigLayout;

/**
 * The MainMenu class serves as the main graphical user interface (GUI) 
 * for the StudioBridge application. It allows users to configure printer settings, 
 * manage profiles, and send data packages to printers.
 */
public class MainMenu {
    
    // Default and current save paths for profiles
    static String defaultSavePath = System.getProperty("user.home") + System.getProperty("file.separator") + "StudioBridge";
    static String savePath = defaultSavePath;
    static String ProfilesDir = savePath + System.getProperty("file.separator") + "Profiles";
    
    // Printer information variables
    static String PrinterIP, PrinterName, PrinterSerial, PrinterType;
    
    // Application version
    static String version = "108";
    
    // Set boolean for checking updates on startup
    static boolean checkForUpdate = true;
    
    // Control over the use of the last selected profile
    static boolean rememberLastUsedProfile = false;
    
    // Contains the name of the last used profile
    static String lastUsedProfile;
    
    public static void main(String[] args) {
        boolean sendOnly = false;
        int argCount = args.length;
        
        for (int i = 0; i < argCount; i++) {
            if (args[i].equals("--noupdate")) {
                checkForUpdate = false;
            }
            if (args[i].startsWith("--sendonly")) {
                sendOnly = true;
                startNoGUI();
            }
        }
        
        if (!sendOnly) {
            startGUI();
        }
    }
    
    private static void startNoGUI() {
    	
    	System.out.println("*** StudioBridge by Rdiger-36 v." + MainMenu.version.substring(0, 1) + "." +
                MainMenu.version.substring(1, 2) + "." + MainMenu.version.substring(2, 3) + " ***\n");
    	
        String profilesDir = Config.customProfilePath(null);
        ArrayList<String> errors = new ArrayList<>();

        // Determine which UDP port (2021 or 1990) is in use, or set to 0 if none is available
        int remoteUdpPort = UDPPackage.getAvailableUDPPort();

        // Check if a valid port was assigned
        if (remoteUdpPort > 0) {
            File[] files = new File(profilesDir).listFiles();            
            if (files != null) {

                System.out.println("Found " + files.length + " profiles!");
                System.out.println("Try to send all of them to Bambu Studio\n");
            	
                for (File file : files) {
                    if (file.isFile()) {  // Process only files, not subdirectories
                        Properties properties = new Properties();
                        try (FileReader reader = new FileReader(file)) {
                            properties.load(reader);
                        } catch (IOException e) {
                            System.err.println("Error loading file: " + file.getName());
                            e.printStackTrace();
                            continue;  // Process next file on error
                        }

                        // Read values from the properties file
                        String ipAddress = properties.getProperty("IP-Address");
                        String printerSN = properties.getProperty("PrinterSN");
                        String printerType = properties.getProperty("PrinterType");
                        String printerName = properties.getProperty("PrinterName");

                        // Send the UDP package
                        boolean sendSuccess = UDPPackage.send(null, ipAddress, printerSN,
                        		MainMenu.getModel(MultiPrinterSetup.getModel(printerType)),
                                printerName, remoteUdpPort, false);
                        if (!sendSuccess) {
                            String errorMsg = "Error sending for " + printerName
                                    + " - " + ipAddress + " - " + MultiPrinterSetup.getModel(MainMenu.getModel(printerType));
                            errors.add(errorMsg);
                        } else {
                        	String successMsg = "Successfully sended " + printerName
                                    + " - " + ipAddress + " - " + MultiPrinterSetup.getModel(MainMenu.getModel(printerType));
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
                System.out.println("No folder found or the folder is empty, please check --> " + profilesDir +"");
            }
        } else {
            System.err.println("Warning! Bambu Studio is not running!");
        }
    }

	public static void startGUI() {
    	EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new MainMenu(Config.lastTheme(null), null);
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
        UI.changeLAF(darkmode); // Change the look and feel based on the dark mode setting
        
        ProfilesDir = Config.customProfilePath(null); // Set the custom profiles directory
        
        rememberLastUsedProfile = Config.rememberLastProfile(null);
        
        // Create the main application frame
        JFrame frmStudioBridge = new JFrame();
        frmStudioBridge.setResizable(false);
        frmStudioBridge.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        frmStudioBridge.setIconImage(Toolkit.getDefaultToolkit().getImage(MainMenu.class.getResource("/icon.png")));
        frmStudioBridge.setTitle("StudioBridge");
        frmStudioBridge.setBounds(100, 100, 299, 235);
        frmStudioBridge.setLocationRelativeTo(frame);
        frmStudioBridge.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frmStudioBridge.getContentPane().setLayout(new MigLayout("", "[76.00px][9.00px][][grow][right][right]", "[35px:n,grow][10px:n][][][][][][10px:n][35px:n,grow]"));

        // Add informational label
        JLabel lblInfo = new JLabel("<html>This program sends a specific data package<br>to Bambu Studio.\r\nThis allows the printer<br>to be used in different networks</html>");
        frmStudioBridge.getContentPane().add(lblInfo, "cell 0 0 6 1,growx,aligny center");
        
        // Separator
        JSeparator separator = new JSeparator();
        frmStudioBridge.getContentPane().add(separator, "cell 0 1 6 1,growx,aligny center");
        
        // Profile selection components
        JLabel lblProfile = new JLabel("Profile");
        frmStudioBridge.getContentPane().add(lblProfile, "flowy,cell 0 2,grow");
        
        JComboBox<String> cbxProfile = new JComboBox<>();
        cbxProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbxProfile.setModel(new DefaultComboBoxModel<String>(new String[] {"New profile", "Import profile", "---"}));
        frmStudioBridge.getContentPane().add(cbxProfile, "cell 2 2 2 1,growx");

        // Buttons for profile actions
        JButton btnDeleteProfile = new JButton("");
        btnDeleteProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDeleteProfile.setMargin(new Insets(2, 2, 2, 2));
        btnDeleteProfile.setContentAreaFilled(false);
        btnDeleteProfile.setRequestFocusEnabled(false);
        btnDeleteProfile.setIcon(Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/delete.png")), 16, 16));
        frmStudioBridge.getContentPane().add(btnDeleteProfile, "cell 5 2,alignx right");

        JButton btnSaveProfile = new JButton("");
        btnSaveProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSaveProfile.setRequestFocusEnabled(false);
        btnSaveProfile.setMargin(new Insets(2, 2, 2, 2));
        btnSaveProfile.setContentAreaFilled(false);
        btnSaveProfile.setIcon(Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/save.png")), 16, 16));
        frmStudioBridge.getContentPane().add(btnSaveProfile, "cell 4 2,alignx right");

        // Printer information labels
        JLabel lblIP = new JLabel("IP-Address");
        frmStudioBridge.getContentPane().add(lblIP, "cell 0 3,growx,aligny center");

        JLabel lblSerial = new JLabel("Serial number");
        frmStudioBridge.getContentPane().add(lblSerial, "flowx,cell 0 4,growx,aligny center");

        JLabel lblDevModel = new JLabel("Model");
        frmStudioBridge.getContentPane().add(lblDevModel, "flowx,cell 0 5,growx,aligny center");

        // Model selection combo box
        JComboBox<String> cbxModel = new JComboBox<>();
        cbxModel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbxModel.setMaximumRowCount(8);
        cbxModel.setModel(new DefaultComboBoxModel<>(new String[]{"A1", "A1 Mini", "P1P", "P1S", "X1", "X1C", "X1E", "H2D"}));
        frmStudioBridge.getContentPane().add(cbxModel, "cell 2 5 4 1,growx");
        cbxModel.setSelectedIndex(0);
        
        // Printer name input field
        JLabel lblDevName = new JLabel("Name");
        frmStudioBridge.getContentPane().add(lblDevName, "cell 0 6,growx,aligny center");

        JTextField txtName = new JTextField();
        txtName.setText("");
        txtName.setColumns(10);
        ((AbstractDocument) txtName.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(25));
        frmStudioBridge.getContentPane().add(txtName, "cell 2 6 4 1,growx");
        
        // Separator
        JSeparator separator_1 = new JSeparator();
        frmStudioBridge.getContentPane().add(separator_1, "cell 0 7 6 1,growx,aligny center");
        
        // Button to send package
        JButton btnSendPackage = new JButton("Send package");
        btnSendPackage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        frmStudioBridge.getContentPane().add(btnSendPackage, "cell 0 8 2 1");
        
        // Copyright button
        JButton btnCopy = new JButton("© Niklas (Rdiger-36)");
        btnCopy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCopy.setMargin(new Insets(0, 0, 0, 0));
        btnCopy.setBorderPainted(false);
        btnCopy.setContentAreaFilled(false);
        btnCopy.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        frmStudioBridge.getContentPane().add(btnCopy, "cell 3 8 3 1,alignx right,aligny bottom");

        // Input fields for IP and Serial number
        JTextField txtIP = new JTextField();
        frmStudioBridge.getContentPane().add(txtIP, "flowx,cell 2 3 4 1,growx");
        txtIP.setColumns(10);
        ((AbstractDocument) txtIP.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(15));
        
        JTextField txtSerial = new JTextField();
        txtSerial.setText("");
        txtSerial.setColumns(10);
        ((AbstractDocument) txtSerial.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(25));
        frmStudioBridge.getContentPane().add(txtSerial, "cell 2 4 4 1,growx");
        
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
                UI.changeLAF(cbxmntmDarkmode.isSelected());
                new MainMenu(cbxmntmDarkmode.isSelected(), frmStudioBridge);
                frmStudioBridge.setVisible(false);
            }
        });
        
        JCheckBoxMenuItem cbxmntmRememberLastProfile = new JCheckBoxMenuItem("Remember last used profile");
        cbxmntmRememberLastProfile.setSelected(rememberLastUsedProfile);
        mnSettings.add(cbxmntmRememberLastProfile);
        
        // Action listener for dark mode toggle
        cbxmntmRememberLastProfile.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rememberLastUsedProfile = cbxmntmRememberLastProfile.isSelected();
            }
        });
        
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
                    getAllProfiles(cbxProfile);
                    frmStudioBridge.pack();    
                }
            }
        });
        
        JMenuItem mntmMultiPrinterSetup = new JMenuItem("Multiple Printer Setup");
        mnSettings.add(mntmMultiPrinterSetup);
        
        mntmMultiPrinterSetup.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				 new MultiPrinterSetup(frmStudioBridge);
				
			}
		});
        
        // Action listener for copyright button
        btnCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ToolInfo(frmStudioBridge);
            }
        });
        
        // Action listener for sending the data package
        btnSendPackage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (txtIP.getText().equals("") || txtSerial.getText().equals("") || txtName.getText().equals("")) {
                    new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Attention! Please check your inputs!</html>", "Back").showDialog();
                    return;
                }

                if (cbxProfile.getSelectedItem().toString().equals("New profile")) {
                    String profileName = Config.saveUnsavedNewProfile(frame, cbxProfile, cbxModel, txtIP, txtSerial, txtName);
                    if (!profileName.equals("")) {
                        cbxProfile.addItem(profileName);
                        Config.saveProfile(frmStudioBridge, profileName, cbxProfile, cbxModel, txtIP, txtSerial, txtName);
                        cbxProfile.setSelectedItem(profileName);
                        frmStudioBridge.pack();    
                        
                        PrinterIP = txtIP.getText();
                        PrinterName = txtName.getText();
                        PrinterSerial = txtSerial.getText();
                        PrinterType = cbxModel.getSelectedItem().toString();
                    } 
                } else if (!txtIP.getText().equals(MainMenu.PrinterIP) || !txtSerial.getText().equals(MainMenu.PrinterSerial) || !txtName.getText().equals(MainMenu.PrinterName) || !cbxModel.getSelectedItem().toString().equals(MainMenu.PrinterType)) {
                    if (Config.saveUnsavedProfile(frmStudioBridge, cbxProfile, cbxModel, txtIP, txtSerial, txtName)) {
                        PrinterIP = txtIP.getText();
                        PrinterName = txtName.getText();
                        PrinterSerial = txtSerial.getText();
                        PrinterType = cbxModel.getSelectedItem().toString();
                    }
                } 
                                
        		// Determine which UDP port (2021 or 1990) is in use, or set to 0 if none is available
                int remoteUdpPort = UDPPackage.getAvailableUDPPort();

                // Check if a valid port was assigned to remoteUdpPort
                if (remoteUdpPort > 0) {
                	UDPPackage.send(frmStudioBridge, txtIP.getText(), txtSerial.getText(), getModel(cbxModel.getSelectedItem().toString()), txtName.getText(), remoteUdpPort, false);
                } else {
                    // Show a warning if Bambu Studio is not running
                	new DialogOneButton(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Bambu Studio is not running!</html>", "Ok").showDialog();
                }
            }                        
        });
        
        // Action listener for saving profiles
        btnSaveProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {            
                String profileName = cbxProfile.getSelectedItem().toString();
                
                if (profileName.equals("New profile")) {
                    profileName = new SaveDialog(frmStudioBridge, txtName.getText(), "Save").saveProfile();
                }
                
                if (profileName.length() > 0 && !profileName.equals("New profile")) {
                    if (!profileExists(cbxProfile, profileName)) {
                        cbxProfile.addItem(profileName);
                        Config.saveProfile(frmStudioBridge, profileName, cbxProfile, cbxModel, txtIP, txtSerial, txtName);
                        cbxProfile.setSelectedItem(profileName);
                        frmStudioBridge.pack();    
                    } else {
                        int result = new DialogTwoButtons(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "Warning! The profile: " + profileName + " already exists, do you want to overwrite it?", "Yes", "No").showDialog();
                        if (result == 0) {
                            Config.saveProfile(frmStudioBridge, profileName, cbxProfile, cbxModel, txtIP, txtSerial, txtName);
                        }
                    }
                    
                    PrinterIP = txtIP.getText();
                    PrinterName = txtName.getText();
                    PrinterSerial = txtSerial.getText();
                    PrinterType = cbxModel.getSelectedItem().toString();
                }
            }
        });
        
        // Action listener for deleting profiles
        btnDeleteProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
            	String profile = cbxProfile.getSelectedItem().toString();
            	
            	if (!profile.equals("New profile") && !profile.equals("Import profile") && !profile.equals("---")) {
            	
                    int response = new DialogTwoButtons(frmStudioBridge, null,new ImageIcon(MainMenu.class.getResource("/achtung.png")), "Attention! Do you want to delete the profile: \"" + cbxProfile.getSelectedItem() + "\"?", "Yes", "No").showDialog();
                    
                    if (response == 0) {
                        new File(ProfilesDir + System.getProperty("file.separator") + cbxProfile.getSelectedItem() + ".sbp").delete();
                        cbxProfile.removeItem(cbxProfile.getSelectedItem());
                        cbxProfile.setSelectedIndex(0);
                        
                        PrinterIP = txtIP.getText();
                        PrinterName = txtName.getText();
                        PrinterSerial = txtSerial.getText();
                        PrinterType = cbxModel.getSelectedItem().toString();
                        
                        frmStudioBridge.pack();    
                    }
            	}
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
                        	if (profileName.equals("")) profileName = "New imported profile";
                        	
                        	setProfile(profilePath);
                        	cbxProfile.addItem(profileName);
                        	cbxProfile.setSelectedItem(profileName);
                        } else {
                        	cbxProfile.setSelectedItem("New profile");
                        }
                		
                	} else if (cbxProfile.getSelectedItem().toString().equals("---")) {
                    	
                		if (MainMenu.lastUsedProfile != null) {
                		
                        	if (!MainMenu.lastUsedProfile.equals("New profile") && !MainMenu.lastUsedProfile.equals("Import profile") && !MainMenu.lastUsedProfile.equals("---")) {
                        		cbxProfile.setSelectedItem(MainMenu.lastUsedProfile);
                        	} else {
                        		cbxProfile.setSelectedItem("New profile");
                        	}
                			
                		} else {
                			cbxProfile.setSelectedItem("New profile");
                		}
                		
                		
                	} else {
                	    // Construct the path to the profile configuration file.
                	    String profilePath = MainMenu.ProfilesDir + System.getProperty("file.separator") + cbxProfile.getSelectedItem().toString() + ".sbp";   
                        setProfile(profilePath);
                	}
                }
                
                frmStudioBridge.pack();
            }
                        
            private void setProfile(String profile) {
            	
            	Config.loadProfile(frmStudioBridge, profile, cbxModel, txtIP, txtSerial, txtName);
                PrinterIP = txtIP.getText();
                PrinterName = txtName.getText();
                PrinterSerial = txtSerial.getText();
                PrinterType = cbxModel.getSelectedItem().toString();
                
	            MainMenu.lastUsedProfile = cbxProfile.getSelectedItem().toString();	
            }
            
        });
        
        // Window listener for handling close event
        frmStudioBridge.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Exit.closeApp(frmStudioBridge, cbxProfile, cbxModel, txtIP, txtSerial, txtName);
            }
        });
        
        getAllProfiles(cbxProfile); // Load all profiles into the combo box
                
        PrinterIP = txtIP.getText();
        PrinterName = txtName.getText();
        PrinterSerial = txtSerial.getText();
        PrinterType = cbxModel.getSelectedItem().toString();
        
        if(checkForUpdate) Update.checkVersion(frmStudioBridge); // Check for updates, if it is not disabled by --noupdate argument at startup
        
        frmStudioBridge.pack();    
        frmStudioBridge.setVisible(true);    
    }

    /**
     * Retrieves all profiles from the Profiles directory and adds them to the specified combo box.
     *
     * @param comboBox The combo box to which profiles will be added.
     */
    private void getAllProfiles(JComboBox<String> comboBox) {
        File directory = new File(ProfilesDir);

        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it does not exist
        }
        
        if (directory.exists() && directory.isDirectory()) {
            File[] filesList = directory.listFiles();
            if (filesList != null) {
                for (File file : filesList) {
                    if (file.isFile() && file.getName().endsWith(".sbp") && !file.getName().startsWith(".")) {
                        comboBox.addItem(file.getName().replace(".sbp", ""));
                    }
                }
            }
            
            if (rememberLastUsedProfile) {
            	lastUsedProfile = Config.lastUsedProfile(null);
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
     * Converts model names to a specific format for sending to the printer.
     *
     * @param model The model name as selected by the user.
     * @return The formatted model name.
     */
    public static String getModel(String model) {
        switch (model) {
            case "A1": model = "N2S"; break;
            case "A1 Mini": model = "N1"; break;
            case "P1P": model = "C11"; break;
            case "P1S": model = "C12"; break;
            case "X1": model = "3DPrinter-X1"; break;
            case "X1C": model = "3DPrinter-X1-Carbon"; break;
            case "X1E": model = "C13"; break;
            case "H2D": model = "O1D"; break;
            default: break;
        }
        return model;
    }
    
    /**
     * Checks if a profile already exists in the combo box.
     *
     * @param comboBox The combo box containing the profiles.
     * @param entry The profile name to check.
     * @return True if the profile exists, false otherwise.
     */
    private static boolean profileExists(JComboBox<String> comboBox, String entry) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).equals(entry)) {
                return true; // Entry exists
            }
        }
        return false; // Entry does not exist
    }
}