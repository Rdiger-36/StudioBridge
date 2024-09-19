package rdiger36.StudioBridge;

import javax.swing.UIManager;

public class UI {
	
	public static void changeLAF(boolean mode) {
		
		try {
			if (mode) {
				UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
			} else {
				UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName());
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}
}
