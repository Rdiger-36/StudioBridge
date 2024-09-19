package rdiger36.StudioBridge;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Toolkit;

import javax.swing.*;
import javax.swing.text.AbstractDocument;

import net.miginfocom.swing.MigLayout;

public class SaveDialog {
	private JDialog dial;
	private JTextField textField;
	
	private String response;
	
    public SaveDialog(JFrame mainFrame, String profileName) {
        	
    	dial = new JDialog();
    	dial.setResizable(false);
    	dial.setTitle("StudioBridge");
    	dial.setIconImage(Toolkit.getDefaultToolkit().getImage(SaveDialog.class.getResource("/icon.png")));
    	dial.setModal(true);
    	dial.setAlwaysOnTop(true);
    	dial.addWindowListener(new WindowAdapter() {
    		public void windowClosing(WindowEvent e) {
    			response = "";
    			dial.dispose();
    		}
    	});
        dial.setSize(276, 167);
        if (mainFrame != null) {
        	dial.setLocationRelativeTo(mainFrame);
        } else {
        	dial.setLocationRelativeTo(null);
        }
    	
        JLabel lblProfilename = new JLabel("Profilename:");
        lblProfilename.setHorizontalAlignment(SwingConstants.CENTER);
             
        dial.getContentPane().setLayout(new MigLayout("", "[grow][87px][grow][87px][grow]", "[14px,grow][23px,grow]"));
        dial.getContentPane().add(lblProfilename, "cell 0 0 2 1,alignx center,aligny center");
        
        JButton btn1 = new JButton("Save");
        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = textField.getText().toString();
                dial.dispose();
            }
        });
        
        textField = new JTextField(profileName);
        dial.getContentPane().add(textField, "cell 2 0 2 1,growx");
        textField.setColumns(10);
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(25));
        dial.getContentPane().add(btn1, "cell 2 1,alignx center,aligny center");
        
        dial.pack();
        dial.setLocationRelativeTo(mainFrame);
    }

	public String saveProfile() {
        dial.setVisible(true); // Zeige den Dialog an (blockierend)
        return response; // Gebe den Zustand des Dialogs zurück
    }
}