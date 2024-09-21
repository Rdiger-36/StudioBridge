package rdiger36.StudioBridge;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;
import java.awt.Insets;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

public class MainMenu {
		
	static String savePath = System.getProperty("user.home") + System.getProperty("file.separator") + "StudioBridge";
	static String ProfilesDir = savePath + System.getProperty("file.separator") + "Profiles";
	
	public static void main(String[] args) {
						
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new MainMenu(Config.lastTheme(), null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public MainMenu(boolean darkmode, JFrame frame) {
		
		UI.changeLAF(darkmode);

		ProfilesDir = Config.customProfilePath();
		
		JFrame frmStudioBridge = new JFrame();
		frmStudioBridge.setResizable(false);
		frmStudioBridge.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		frmStudioBridge.setIconImage(Toolkit.getDefaultToolkit().getImage(MainMenu.class.getResource("/icon.png")));
		frmStudioBridge.setTitle("StudioBridge");
		frmStudioBridge.setBounds(100, 100, 299, 235);
		frmStudioBridge.setLocationRelativeTo(frame);
		frmStudioBridge.getContentPane().setLayout(new MigLayout("", "[76.00px][9.00px][][grow][right][right]", "[35px:n,grow][10px:n][][][][][][10px:n][35px:n,grow]"));
		
		JLabel lblInfo = new JLabel("<html>This programm sends a specific data package<br>to Bambu Studio.\r\nThis allows the printer<br>to be used in different networks</html>");
		frmStudioBridge.getContentPane().add(lblInfo, "cell 0 0 6 1,growx,aligny center");
		
		JSeparator separator = new JSeparator();
		frmStudioBridge.getContentPane().add(separator, "cell 0 1 6 1,growx,aligny center");
		
		JLabel lblProfile = new JLabel("Profile");
		frmStudioBridge.getContentPane().add(lblProfile, "flowy,cell 0 2,grow");
		
		JComboBox<String> cbxProfile = new JComboBox<String>();
		cbxProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		cbxProfile.setModel(new DefaultComboBoxModel<String>(new String[] {"New profile"}));
		frmStudioBridge.getContentPane().add(cbxProfile, "cell 2 2 2 1,growx");
		
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
		
		JLabel lblIP = new JLabel("IP-Address");
		frmStudioBridge.getContentPane().add(lblIP, "cell 0 3,growx,aligny center");
		
		JLabel lblSerial = new JLabel("Serialnumber");
		frmStudioBridge.getContentPane().add(lblSerial, "flowx,cell 0 4,growx,aligny center");
		
		JLabel lblDevModel = new JLabel("Model");
		frmStudioBridge.getContentPane().add(lblDevModel, "flowx,cell 0 5,growx,aligny center");
		
		JComboBox<String> cbxModel = new JComboBox<String>();
		cbxModel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		cbxModel.setMaximumRowCount(7);
		cbxModel.setModel(new DefaultComboBoxModel<String>(new String[] {"A1", "A1 Mini", "P1P", "P1S", "X1", "X1C", "X1E"}));
		frmStudioBridge.getContentPane().add(cbxModel, "cell 2 5 4 1,growx");
		cbxModel.setSelectedIndex(0);
		
		JLabel lblDevName = new JLabel("Name");
		frmStudioBridge.getContentPane().add(lblDevName, "cell 0 6,growx,aligny center");
		
		JTextField txtName = new JTextField();
		txtName.setText("");
		txtName.setColumns(10);
		((AbstractDocument) txtName.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(25));
		frmStudioBridge.getContentPane().add(txtName, "cell 2 6 4 1,growx");
		
		JSeparator separator_1 = new JSeparator();
		frmStudioBridge.getContentPane().add(separator_1, "cell 0 7 6 1,growx,aligny center");
		
		JButton btnSendPackage = new JButton("Send package");
		btnSendPackage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		frmStudioBridge.getContentPane().add(btnSendPackage, "cell 0 8 2 1");
		
		JButton btnCopy = new JButton("© Niklas (Rdiger-36)");
		btnCopy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCopy.setMargin(new Insets(0, 0, 0, 0));
		btnCopy.setBorderPainted(false);
		btnCopy.setContentAreaFilled(false);
		btnCopy.setFont(new Font("Segoe UI", Font.PLAIN, 10));
		frmStudioBridge.getContentPane().add(btnCopy, "cell 3 8 3 1,alignx right,aligny bottom");
		
		JTextField txtIP = new JTextField();
		frmStudioBridge.getContentPane().add(txtIP, "flowx,cell 2 3 4 1,growx");
		txtIP.setColumns(10);
		((AbstractDocument) txtIP.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(15));
		
		JTextField txtSerial = new JTextField();
		txtSerial.setText("");
		txtSerial.setColumns(10);
		((AbstractDocument) txtSerial.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(25));
		frmStudioBridge.getContentPane().add(txtSerial, "cell 2 4 4 1,growx");
		
		JMenuBar menuBar = new JMenuBar();
		frmStudioBridge.setJMenuBar(menuBar);
		
		JMenu mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);
		
		JMenuItem mntmChangeProfilesPath = new JMenuItem("Set custom profiles path");
		mnSettings.add(mntmChangeProfilesPath);
		
		JMenu mnDesign = new JMenu("Design");
		mnSettings.add(mnDesign);
		
		JCheckBoxMenuItem cbxmntmDarkmode = new JCheckBoxMenuItem("Darkmode");
		mnDesign.add(cbxmntmDarkmode);
		cbxmntmDarkmode.setSelected(darkmode);
		
		mntmChangeProfilesPath.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new ProfilesPath(frmStudioBridge).changePath();
			}
		});
		
		cbxmntmDarkmode.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				UI.changeLAF(cbxmntmDarkmode.isSelected());
				new MainMenu(cbxmntmDarkmode.isSelected(), frmStudioBridge);
				frmStudioBridge.setVisible(false);
			}
		});
		
		btnCopy.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new ToolInfo(frmStudioBridge);
			}
		});
		
		btnSendPackage.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				int response = new DialogTwoButtons(frmStudioBridge, null,new ImageIcon(MainMenu.class.getResource("/achtung.png")), "Attention! Do you want to save the new profile ?", "Yes", "No").showDialog();
				
				if (response == 0) {
				
					String profileName = new SaveDialog(frmStudioBridge, txtName.getText()).saveProfile();
					
					if (profileName.length() > 0 && !profileName.equals("New profile")) {
						if (!profileExists(cbxProfile, profileName)) {
							cbxProfile.addItem(profileName);
							Config.saveProfile(profileName, cbxProfile, cbxModel, txtIP, txtSerial, txtName);
							cbxProfile.setSelectedItem(profileName);
							frmStudioBridge.pack();	
						} else {
							int result = new DialogTwoButtons(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "Warning! The profile: " + profileName + " already exists, do you want to overwrite it?", "Yes", "No").showDialog();
							
							if (result == 0) {
								Config.saveProfile(profileName, cbxProfile, cbxModel, txtIP, txtSerial, txtName);
							}
						}
					}
				}
				UDPPackage.send(frmStudioBridge, txtIP.getText(), txtSerial.getText(), getModel(cbxModel.getSelectedItem().toString()), txtName.getText());
			}
		});
		
		btnSaveProfile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String profileName = new SaveDialog(frmStudioBridge, txtName.getText()).saveProfile();
				
				if (profileName.length() > 0 && !profileName.equals("New profile")) {
					if (!profileExists(cbxProfile, profileName)) {
						cbxProfile.addItem(profileName);
						Config.saveProfile(profileName, cbxProfile, cbxModel, txtIP, txtSerial, txtName);
						cbxProfile.setSelectedItem(profileName);
						frmStudioBridge.pack();	
					} else {
						int result = new DialogTwoButtons(frmStudioBridge, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "Warning! The profile: " + profileName + " already exists, do you want to overwrite it?", "Yes", "No").showDialog();
						
						if (result == 0) {
							Config.saveProfile(profileName, cbxProfile, cbxModel, txtIP, txtSerial, txtName);
						}
					}
				}
			}
		});
		
		btnDeleteProfile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int response = new DialogTwoButtons(frmStudioBridge, null,new ImageIcon(MainMenu.class.getResource("/achtung.png")), "Attention! Do you want to delete the profile: " + cbxProfile.getSelectedItem() + " ?", "Yes", "No").showDialog();
				
				if (response == 0) {
					new File(ProfilesDir + System.getProperty("file.separator") + cbxProfile.getSelectedItem()).delete();
					cbxProfile.removeItem(cbxProfile.getSelectedItem());
					cbxProfile.setSelectedIndex(0);
					frmStudioBridge.pack();	
				}
				
			}
		});
		
		cbxProfile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Config.loadProfile(cbxProfile.getSelectedItem().toString(), cbxModel, txtIP, txtSerial, txtName);
			}
		});
		
		frmStudioBridge.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Exit.closeApp(frmStudioBridge, cbxProfile, cbxModel, txtIP, txtSerial, txtName);
			}
		});
		
		getAllProfiles(cbxProfile);
		
		frmStudioBridge.pack();	
		frmStudioBridge.setVisible(true);	
	}

	private void getAllProfiles(JComboBox<String> comboBox) {
		
		 // Ein File-Objekt für das Verzeichnis erstellen
        File directory = new File(ProfilesDir);

        if (!directory.exists()) {
        	directory.mkdirs();
        }
        
        // Überprüfen, ob das Verzeichnis existiert und ein Verzeichnis ist
        if (directory.exists() && directory.isDirectory()) {
            // Liste aller Dateien und Unterverzeichnisse im Verzeichnis abrufen
            File[] filesList = directory.listFiles();

            if (filesList != null) {
                for (File file : filesList) {
                    // Dateiname ausgeben
                    comboBox.addItem(file.getName());
                }
            } 
        } else {
        	new DialogOneButton(null, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "Error! Directory " + ProfilesDir + " could not be created!", "Close").showDialog();
        	System.exit(0);
        }
	}

	private String getModel(String model) {
		
		switch (model) {
			
			case "A1": model = "N2S"; break;
			case "A1 Mini": model = "N1"; break;
			case "P1P": model = "C11"; break;
			case "P1S": model = "C12"; break;
			case "X1": model = "3DPrinter-X1"; break;
			case "X1C": model = "3DPrinter-X1-Carbon"; break;
			case "X1E": model = "C13"; break;
			default: break;
	
		}
		return model;
	}
	
	 private static boolean profileExists(JComboBox<String> comboBox, String entry) {
	        for (int i = 0; i < comboBox.getItemCount(); i++) {
	            if (comboBox.getItemAt(i).equals(entry)) {
	                return true; // Eintrag existiert bereits
	            }
	        }
	        return false; // Eintrag ist nicht vorhanden
	    }
}