package rdiger36.StudioBridge;

import java.awt.Toolkit;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.JButton;

public class PrinterCheck {
	
	boolean isRunning = false;

	public PrinterCheck(JFrame frmStudioBridge, String IPAddress) {
		JDialog dialPrinter = new JDialog();
		dialPrinter.setResizable(false);
		dialPrinter.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		dialPrinter.setModal(true);
		dialPrinter.setAlwaysOnTop(true);
		dialPrinter.setTitle("Check Printer Connectivity");
		dialPrinter.setIconImage(Toolkit.getDefaultToolkit().getImage(MultiPrinterSetup.class.getResource("/icon.png")));
		dialPrinter.setBounds(100, 100, 602, 482);
		dialPrinter.toFront();
		dialPrinter.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dialPrinter.dispose();
            }
		});
		
		dialPrinter.getContentPane().setLayout(new MigLayout("", "[][]", "[][10px:10px:10px][][10px:10px:10px][][][][]"));
		
		JLabel lblInfo = new JLabel("<html>Here you can verify whether your printer is reachable from your machine and whether<br>it accepts TCP connections on port 6000. If both tests succeed, everything is working correctly.<br>If either test fails, you should review your network settings. If the TCP handshake fails specifically,<br>Bambu Studio often displays a \"Connection Failed Error –1\" message.</html>");
		dialPrinter.getContentPane().add(lblInfo, "cell 0 0 2 1");
		
		JSeparator separator = new JSeparator();
		dialPrinter.getContentPane().add(separator, "cell 0 1 2 1,growx,aligny center");
		
		JLabel lblInfoIP = new JLabel("Connectivity check for the printer witc the IP-Address: " + IPAddress);
		dialPrinter.getContentPane().add(lblInfoIP, "cell 0 2 2 1,alignx center,aligny top");
		
		JLabel lblPing = new JLabel("Is printer reachable?");
		dialPrinter.getContentPane().add(lblPing, "cell 0 4");
		
		JLabel lblPingState = new JLabel("Please wait...");
		lblPingState.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MultiPrinterSetup.class.getResource("/load.gif"))));
		lblPingState.setHorizontalTextPosition(SwingConstants.LEFT);
		lblPingState.setVerticalTextPosition(SwingConstants.CENTER);
		dialPrinter.getContentPane().add(lblPingState, "cell 1 4");
		
		JLabel lblHandshake = new JLabel("TCP handshake ");
		dialPrinter.getContentPane().add(lblHandshake, "cell 0 5");
		
		JLabel lblHandshakeState = new JLabel("Please wait...");
		lblHandshakeState.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MultiPrinterSetup.class.getResource("/load.gif"))));
		lblHandshakeState.setHorizontalTextPosition(SwingConstants.LEFT);
		lblHandshakeState.setVerticalTextPosition(SwingConstants.CENTER);
		dialPrinter.getContentPane().add(lblHandshakeState, "cell 1 5");
		
		JButton btnRetry = new JButton("Retry");
		dialPrinter.getContentPane().add(btnRetry, "cell 0 7 2 1,alignx center");
				        
		dialPrinter.addWindowListener(new WindowAdapter() {
		    @Override public void windowOpened(WindowEvent e) {
		        new SwingWorker<Void, Void>() {
		            @Override protected Void doInBackground() {
		                startScan(IPAddress, lblPingState, lblHandshakeState); // macht invokeLater intern schon
		                return null;
		            }
		        }.execute();
		    }
		});
		
		btnRetry.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!isRunning) new Thread(() -> startScan(IPAddress, lblPingState, lblHandshakeState)).start();	
			}
		});
		
		dialPrinter.requestFocus();
		dialPrinter.pack();
		dialPrinter.setLocationRelativeTo(frmStudioBridge);
        dialPrinter.setVisible(true);
     
	}

	private void startScan(String IPAddress, JLabel lblPingState, JLabel lblHandshakeState) {
        	
		isRunning = true;
		
		 SwingUtilities.invokeLater(() -> {
    		lblPingState.setText("Please wait...");
    		lblPingState.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MultiPrinterSetup.class.getResource("/load.gif"))));
    		
    		lblHandshakeState.setText("Please wait...");
			lblHandshakeState.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MultiPrinterSetup.class.getResource("/load.gif"))));
         });
    	
    	if (Connectivity.pingPrinter(IPAddress)) {
            SwingUtilities.invokeLater(() -> {
            	lblPingState.setText("Printer is reachable from your computer!");
            	lblPingState.setIcon(Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/success.png")), 16, 16));
            });
    		
    		if (Connectivity.checkTLSHandshake(IPAddress)) {
                SwingUtilities.invokeLater(() -> {
	    			lblHandshakeState.setText("Handshake was sucessfully between computer and printer!");
	    			lblHandshakeState.setIcon(Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/success.png")), 16, 16));
                });
    		} else {
                SwingUtilities.invokeLater(() -> {
	        		lblHandshakeState.setText("Error on Handshake between computer and printer!");
	    			lblHandshakeState.setIcon(Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/delete.png")), 16, 16));
                });
    		}
    	} else {
            SwingUtilities.invokeLater(() -> {
	    		lblPingState.setText("Printer is not reachable from your computer!");
	    		lblPingState.setIcon(Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/delete.png")), 16, 16));
	    		
	    		lblHandshakeState.setText("Printer is not reachable from your computer!");
				lblHandshakeState.setIcon(Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/delete.png")), 16, 16));
            });
    	}
    	
		isRunning = false;
	}
}
