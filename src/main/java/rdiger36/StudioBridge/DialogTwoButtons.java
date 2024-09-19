package rdiger36.StudioBridge;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Toolkit;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class DialogTwoButtons {
	private int response;

	private JDialog dial;
	
    public DialogTwoButtons(JFrame mainFrame, JDialog mainDial, ImageIcon icon, String message, String button1Text, String button2Text) {
    	
    	dial = new JDialog();
    	dial.setResizable(false);
    	dial.setTitle("StudioBridge");
    	dial.setIconImage(Toolkit.getDefaultToolkit().getImage(DialogTwoButtons.class.getResource("/icon.png")));
    	dial.setModal(true);
    	dial.setAlwaysOnTop(true);
    	dial.addWindowListener(new WindowAdapter() {
    		public void windowClosing(WindowEvent e) {
    			response = 99;
    			dial.dispose();
    		}
    	});
        dial.setSize(276, 167);
    	
        JLabel label = new JLabel(message);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        if (icon != null) {
        	label.setIconTextGap(15);
        	label.setIcon(Resize.setNewImageIconSize(icon, 24, 24));
        }
        
        JButton btn1 = new JButton(button1Text);
        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = 0;
                dial.dispose();
            }
        });
             
        JButton btn2 = new JButton(button2Text);
        btn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = 1; 
                dial.dispose();
            }
        });
        dial.getContentPane().setLayout(new MigLayout("", "[grow][87px][87px][grow]", "[14px,grow][10.00][23px,grow]"));
        dial.getContentPane().add(label, "cell 0 0 4 1,alignx center,aligny center");
        dial.getContentPane().add(btn1, "cell 1 2,alignx center,aligny center");
        dial.getContentPane().add(btn2, "cell 2 2,alignx center,aligny center");

        dial.pack();
        
        if (mainFrame != null) {
        	dial.setLocationRelativeTo(mainFrame);
        } else if (mainDial != null) {
        	dial.setLocationRelativeTo(mainDial);
        } else {
        	dial.setLocationRelativeTo(null);
        }
    }

    public int showDialog() {
        dial.setVisible(true); // Zeige den Dialog an (blockierend)
        return response; // Gebe den Zustand des Dialogs zurück
    }
}