package rdiger36.StudioBridge;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AbstractDocument;

import com.formdev.flatlaf.FlatLaf;

import net.miginfocom.swing.MigLayout;

public class ProfilesPath {

	private JDialog dial;
	private JTextField textField;
	
	private String response;
	private JButton btnReset;
	private JButton btnChooseDir;
	
    public ProfilesPath(JFrame mainFrame) {
        	
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
        dial.setSize(369, 103);
        if (mainFrame != null) {
        	dial.setLocationRelativeTo(mainFrame);
        } else {
        	dial.setLocationRelativeTo(null);
        }
    	
        JLabel lblPath = new JLabel("Path:");
        lblPath.setHorizontalAlignment(SwingConstants.CENTER);
             
        dial.getContentPane().setLayout(new MigLayout("", "[grow][grow][200px,grow][200px,grow][grow]", "[14px,grow][23px,grow]"));
        dial.getContentPane().add(lblPath, "cell 0 0 2 1,alignx left,aligny center");
        
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = textField.getText().toString();
                dial.dispose();
            }
        });
        
        textField = new JTextField(MainMenu.ProfilesDir);
        dial.getContentPane().add(textField, "cell 2 0 2 1,grow");
        textField.setColumns(10);
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(25));
        
        btnChooseDir = new JButton("...");
        dial.getContentPane().add(btnChooseDir, "cell 4 0,alignx right");
        dial.getContentPane().add(btnSave, "cell 2 1,alignx right,aligny center");
        
        btnReset = new JButton("Reset");
        dial.getContentPane().add(btnReset, "cell 3 1");
        
        btnChooseDir.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
//				new JnaFileChooser();
				
			}
		});
        
        dial.pack();
        dial.setLocationRelativeTo(mainFrame);
    }

	public String changePath() {
        dial.setVisible(true); // Zeige den Dialog an (blockierend)
        return response; // Gebe den Zustand des Dialogs zurück
    }
}