package rdiger36.StudioBridge;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class Exit {
		
	public static void closeApp(JFrame mainFrame, JComboBox<String> comboBox, JComboBox<String> cbxModel, JTextField txtIP, JTextField txtSerial, JTextField txtName) {
		if (comboBox.getSelectedItem().toString().equals("New profile")) Config.saveUnsavedProfile(mainFrame, comboBox, cbxModel, txtIP, txtSerial, txtName);
		Config.saveUserSettings();
		System.exit(0);
	}
}
