package rdiger36.StudioBridge;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Toolkit;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

public class DialogOneButton {
	private boolean response;

	private JDialog dial;
	
    public DialogOneButton(JFrame mainFrame, JDialog mainDial, ImageIcon icon, String message, String buttonText) {
        	
    	dial = new JDialog();
    	dial.setResizable(false);
    	dial.setTitle("StudioBridge");
    	dial.setIconImage(Toolkit.getDefaultToolkit().getImage(DialogOneButton.class.getResource("/icon.png")));
    	dial.setModal(true);
    	dial.setAlwaysOnTop(true);
    	dial.addWindowListener(new WindowAdapter() {
    		public void windowClosing(WindowEvent e) {
    			response = false;
    			dial.dispose();
    		}
    	});
        dial.setSize(276, 167);
        if (mainFrame != null) {
        	dial.setLocationRelativeTo(mainFrame);
        } else if (mainDial != null) {
        	dial.setLocationRelativeTo(mainDial);
        } else {
        	dial.setLocationRelativeTo(null);
        }
    	
        JLabel label = new JLabel(message);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        if (icon != null) {
        	label.setIconTextGap(15);
        	if (!icon.toString().endsWith(".gif")) {
        		label.setIcon(Resize.setNewImageIconSize(icon, 24, 24));
        	} else {
        		label.setIcon(icon);
        	}
        }
             
        dial.getContentPane().setLayout(new MigLayout("", "[grow][87px][][87px][grow]", "[14px,grow][10.00][23px,grow]"));
        dial.getContentPane().add(label, "cell 0 0 5 1,alignx center,aligny center");
        
        JButton btn1 = new JButton(buttonText);
        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = true;
                dial.dispose();
            }
        });
        dial.getContentPane().add(btn1, "cell 2 2,alignx center,aligny center");
        if (buttonText.contains("NoButton")) btn1.setVisible(false);
        
        dial.pack();
        dial.setLocationRelativeTo(mainFrame);
    }

    public boolean showDialog() {
        dial.setVisible(true); // Zeige den Dialog an (blockierend)
        return response; // Gebe den Zustand des Dialogs zurück
    }
}