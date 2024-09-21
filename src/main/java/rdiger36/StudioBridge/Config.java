package rdiger36.StudioBridge;

import java.io.*;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatLaf;

public class Config {
	
	public static void saveUnsavedProfile(JFrame mainFrame, JComboBox<String> comboBox, JComboBox<String> cbxModel, JTextField txtIP, JTextField txtSerial, JTextField txtName) {
		
		int response = new DialogTwoButtons(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! The new profile has not been saved yet!</html>", "Save", "Exit").showDialog();
		
		if (response == 0) {
			String profielName = new SaveDialog(mainFrame, txtName.getText()).saveProfile();
			saveProfile(profielName, comboBox, cbxModel, txtIP, txtSerial, txtName);
		}
	}
	
	public static boolean saveProfile(String ProfileName, JComboBox<String> comboBox, JComboBox<String> cbxModel, JTextField txtIP, JTextField txtSerial, JTextField txtName) {

		// Erstellen eines StringBuilders, in welchem die Config Zeile für Zeile geschrieben wird
		StringBuilder config = new StringBuilder();
		
		config.append("ProfileName=" + comboBox.getSelectedItem());
		config.append(System.getProperty("line.separator"));
		config.append("IP-Address=" + txtIP.getText());
		config.append(System.getProperty("line.separator"));
		config.append("PrinterSN=" + txtSerial.getText());
		config.append(System.getProperty("line.separator"));
		config.append("PrinterType=" + cbxModel.getSelectedIndex());
		config.append(System.getProperty("line.separator"));
		config.append("PrinterName=" + txtName.getText());
				
		/** 
		 * FileOutputStream für das Erstellen und Schreiben des Inhaltes des StringBuilders config
		 * in die Datei unter dem Pfad DataPathConf
		 */
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(MainMenu.ProfilesDir + System.getProperty("file.separator") + ProfileName );
			fos.write(config.toString().getBytes());
			fos.close();
		} catch (Exception e1) {
			e1.getMessage();
			return false;
		}
		
		return true;
	}
	
	/** 
	 * Methode zum Einlesen der individuellen Profilen
	 */
	public static boolean loadProfile(String profile, JComboBox<String> cbxModel, JTextField txtIP, JTextField txtSerial, JTextField txtName) {
		
		// Initialisieren der benötigten Variablen
		String configPath = MainMenu.ProfilesDir + System.getProperty("file.separator") + profile;		
		File configFile = new File(configPath);		
		String line = "";
		
		// Initialiseren des BufferesReader zum Auslesen des InputStreams des Inhaltes aus der Config Datei
		BufferedReader inputStream;
		
		if (!configFile.exists()) {
			txtIP.setText("");
			txtSerial.setText("");
			cbxModel.setSelectedIndex(0);
			txtName.setText("");
			return false;
		} else {	
			
			try {

				inputStream = new BufferedReader(new FileReader(configFile));
				while((line = inputStream.readLine()) != null) {
					/** 
					 * Die Methode get_Conf() wird genutzt um die eingelesene Zeile
					 * der ensprechenden Variabeln zuzuordnen.
					 * Dazu wird die Zeile in ein Array getielt und die beiden
					 * Werte aus [0] und [1] übergeben.
					 */
					get_Conf(line.split("=")[0], line.split("=")[1], cbxModel, txtIP, txtSerial, txtName);
				}	
				inputStream.close();
				
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	/** 
	 * Die Methode get_conf() wird dazu genutzt, die übergebenen configTypen und dazu gehörigen Werte
	 * mit Hilfe einer switch Funktion den public Conf Variblen zu zuordnen
	 * @param String configType, den zum in die Zwischenablage speichernden String übergeben
	 * @param String wert, String für die anzuzeigende Erfolgs- bzw. Fehlermeldung 
	 */
	private static void get_Conf(String configType, String wert, JComboBox<String> cbxModel, JTextField txtIP, JTextField txtSerial, JTextField txtName) {
		
		switch(configType) {
		
			case "IP-Address": txtIP.setText(checkLength(wert, 15)); break;
			case "PrinterSN": txtSerial.setText(checkLength(wert, 25)); break;
			case "PrinterType": cbxModel.setSelectedIndex(Integer.parseInt(wert)); break;
			case "PrinterName": txtName.setText(checkLength(wert, 25)); break;
			default: break;
		
		}		
	}

	private static String checkLength(String wert, int length) {
		
		if (wert.length() > length) wert = wert.substring(0, length);
		
		return wert;
	}
	
	public static void saveUserSettings() {
		// Erstellen eines StringBuilders, in welchem die Config Zeile für Zeile geschrieben wird
		StringBuilder config = new StringBuilder();
		
		config.append("enableDarkmode=" + FlatLaf.isLafDark());
		config.append(System.getProperty("line.separator"));
		config.append("customProfilesPath=" + MainMenu.ProfilesDir);
		config.append(System.getProperty("line.separator"));
				
		/** 
		 * FileOutputStream für das Erstellen und Schreiben des Inhaltes des StringBuilders config
		 * in die Datei unter dem Pfad DataPathConf
		 */
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(MainMenu.savePath + System.getProperty("file.separator") + "settings");
			fos.write(config.toString().getBytes());
			fos.close();
		} catch (Exception e1) {
			e1.getMessage();
		}	
	}
	
	public static boolean lastTheme() {
		
		String userSettings = MainMenu.savePath + System.getProperty("file.separator") + "settings";
		
		boolean darkmode = false;
		
		if (new File(userSettings).exists()) {
		
			try {
				String line;
				try (BufferedReader inputStream = new BufferedReader(new FileReader(userSettings))) {
					while((line = inputStream.readLine()) != null) {
						if (line.split("=")[0].equals("enableDarkmode")) {				
							darkmode = Boolean.valueOf(line.split("=")[1]);
						}
					}	
					inputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		return darkmode;
	}
	
	public static String customProfilePath() {
		
		String userSettings = MainMenu.savePath + System.getProperty("file.separator") + "settings";
		
		String customProfilePath = MainMenu.savePath + System.getProperty("file.separator") + "Profiles";
		
		if (new File(userSettings).exists()) {
		
			try {
				String line;
				try (BufferedReader inputStream = new BufferedReader(new FileReader(userSettings))) {
					while((line = inputStream.readLine()) != null) {
						if (line.split("=")[0].equals("customProfilesPath")) {				
							customProfilePath = line.split("=")[1];
						}
					}	
					inputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		return customProfilePath;
		
	}
}