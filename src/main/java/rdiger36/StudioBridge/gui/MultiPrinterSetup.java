package rdiger36.StudioBridge.gui;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import rdiger36.StudioBridge.functions.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JProgressBar;

/**
 * Class responsible for managing the setup of multiple printers within a GUI.
 * Provides functionality to import printers, select/deselect them, and send configuration packages.
 */
public class MultiPrinterSetup {
    private JTable table;
    
    private volatile boolean stopThread = false;
    private volatile boolean printerSelected = false;
    
    private static ArrayList<String> errors = new ArrayList<>();

    /**
     * Constructor that initializes the MultiPrinterSetup dialog.
     * 
     * @param frmStudioBridge The parent JFrame for positioning the dialog.
     */
    @SuppressWarnings("serial")
    public MultiPrinterSetup(JFrame frmStudioBridge) {
        JDialog dialInfo = new JDialog();
        dialInfo.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        dialInfo.setModal(true);
        dialInfo.setAlwaysOnTop(true);
        dialInfo.setTitle("Multiple Printer Setup");
        dialInfo.setIconImage(Toolkit.getDefaultToolkit().getImage(MultiPrinterSetup.class.getResource("/icon.png")));
        dialInfo.setBounds(100, 100, 602, 482);
        dialInfo.toFront();
        dialInfo.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                stopThread = true;
                dialInfo.dispose();
            }
        });

        JScrollPane scrollPane = new JScrollPane();

        table = new JTable();
        table.setFocusTraversalKeysEnabled(false);
        table.setFocusable(false);
        table.setModel(new DefaultTableModel(
            new Object[][] {},
            new String[] { "Name", "IP-Address", "Serial", "Model", "Send Package" }
        ) {
            Class<?>[] columnTypes = new Class[] {
                Object.class, Object.class, Object.class, Object.class, Boolean.class
            };

            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }
        });

        // Set preferred column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(164);
        table.getColumnModel().getColumn(2).setPreferredWidth(159);
        table.getColumnModel().getColumn(3).setPreferredWidth(108);
        table.getColumnModel().getColumn(4).setPreferredWidth(93);
        scrollPane.setViewportView(table);

        dialInfo.getContentPane().setLayout(new MigLayout("", "[188.00,grow][223.00,grow][390.00][]", "[grow][bottom][]"));
        dialInfo.getContentPane().add(scrollPane, "cell 0 0 4 1,grow");

        importPrinters(table);

        // Adjust scroll pane size based on content
        int maxVisibleRows = 10;
        int rowHeight = table.getRowHeight();
        int tableHeaderHeight = table.getTableHeader().getPreferredSize().height;
        int visibleRows = Math.min(table.getRowCount(), maxVisibleRows);
        int preferredHeight = (rowHeight * visibleRows) + tableHeaderHeight + 5;
        int totalColumnWidth = 0;

        for (int col = 0; col < table.getColumnCount(); col++) {
            totalColumnWidth += table.getColumnModel().getColumn(col).getPreferredWidth();
        }

        int padding = 30;
        scrollPane.setPreferredSize(new Dimension(totalColumnWidth + padding, preferredHeight));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        dialInfo.getContentPane().add(progressBar, "cell 1 1,grow");

        JButton btnSendPrinters = new JButton("Send selected Printers");
        btnSendPrinters.addActionListener(e -> sendSelectedPrinters(frmStudioBridge, dialInfo, progressBar));
        dialInfo.getContentPane().add(btnSendPrinters, "cell 0 1,grow");

        JButton btnSelAll = new JButton("Select All");
        btnSelAll.addActionListener(e -> setAllBooleans(true));
        dialInfo.getContentPane().add(btnSelAll, "cell 2 1,alignx right");

        JButton btnDeSelAll = new JButton("Deselect All");
        btnDeSelAll.addActionListener(e -> setAllBooleans(false));
        dialInfo.getContentPane().add(btnDeSelAll, "cell 3 1,alignx left");

        dialInfo.requestFocus();
        dialInfo.pack();
        dialInfo.setLocationRelativeTo(frmStudioBridge);
        dialInfo.setVisible(true);
    }

    /**
     * Imports printer configurations from files and populates the JTable.
     * 
     * @param table The JTable to populate with printer data.
     */
    private void importPrinters(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        File folder = new File(MainMenu.ProfilesDir);

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.toString().endsWith("sbp")) {
                        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                            String line, ipAddress = "", printerSN = "", printerType = "", printerName = "";

                            while ((line = br.readLine()) != null) {
                                String[] parts = line.split("=", 2);
                                if (parts.length == 2) {
                                    switch (parts[0].trim()) {
                                        case "IP-Address": ipAddress = parts[1].trim(); break;
                                        case "PrinterSN": printerSN = parts[1].trim(); break;
                                        case "PrinterType": printerType = Models.getNameFromModel(parts[1].trim()); break;
                                        case "PrinterName": printerName = parts[1].trim(); break;
                                    }
                                }
                            }
                            model.addRow(new Object[]{printerName, ipAddress, printerSN, printerType, false});

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Selects or deselects all printers in the table.
     * 
     * @param value true to select all, false to deselect all.
     */
    private void setAllBooleans(boolean value) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(value, i, 4);
        }
    }

    /**
     * Handles the process of sending selected printers' data.
     * 
     * @param frmStudioBridge The parent JFrame.
     * @param dialInfo        The dialog interface.
     * @param progressBar     The progress bar to indicate sending status.
     */
    private void sendSelectedPrinters(JFrame frmStudioBridge, JDialog dialInfo, JProgressBar progressBar) {
        // This method handles the sending logic for selected printers.

	    stopThread = false;
	    errors.clear();
	    Thread sendingThread = new Thread(() -> {
	        progressBar.setString("Check Printers...");
	        progressBar.setVisible(true);
	
	        printerSelected = false;
	        
			// Determine which UDP port (2021 or 1990) is in use, or set to 0 if none is available
	        int remoteUdpPort = UDPPackage.getAvailableUDPPort();
	
	        // Check if a valid port was assigned to remoteUdpPort
	        if (remoteUdpPort > 0) {
	
	            DefaultTableModel model = (DefaultTableModel) table.getModel();
	            int rowCount = model.getRowCount();
	            	                    
	            for (int i = 0; i < rowCount && !stopThread; i++) {
	                Boolean shouldSend = (Boolean) model.getValueAt(i, 4);
	                if (shouldSend != null && shouldSend) {
	                	
	                	printerSelected = true;
	                	
	                    String NAME = (String) model.getValueAt(i, 0);
	                    String IP_ADRESS = (String) model.getValueAt(i, 1);
	                    String SERIAL = (String) model.getValueAt(i, 2);
	                    String MODEL = (String) model.getValueAt(i, 3);
	
	                    SwingUtilities.invokeLater(() -> progressBar.setString("Send Package for " + NAME));
	
	                    if (!UDPPackage.send(frmStudioBridge, IP_ADRESS, SERIAL, Models.getModelFromName(MODEL), NAME, remoteUdpPort, true)) {
	                    	errors.add(NAME + " - " + IP_ADRESS + " - " + MODEL);
	                    } 
	
	                    try {
	                        Thread.sleep(1000); // Simulierte Verzögerung
	                    } catch (InterruptedException e) {
	                        // Thread wurde unterbrochen, daher beenden wir den Prozess
	                        Thread.currentThread().interrupt();
	                        break;
	                    }
	                }
	            }
	
	            SwingUtilities.invokeLater(() -> {
	                progressBar.setVisible(false);
	                if (!stopThread) {
	                    dialInfo.setModal(false);
	                    if (printerSelected) {
	                    	if (!errors.isEmpty()) {
	                    		String errorsOn = "";
	                    		
	                    		for (String error : errors) {
	                    			errorsOn = errorsOn + error + "<br>";
	                    		}
	                    		
	                    		new DialogOneButton(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Attention! Following printers could not sent :<br>" + errorsOn + "</html>", "Ok").showDialog();
	                    	} else {
	                    		new DialogOneButton(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/success.png")), "<html>Packages sent successfully.<br>The Printer(s) will appear in the next 60 seconds in Bambu Studio.<br>You can close this Programm when your printer(s) appear in Bambu Studio</html>", "Ok").showDialog();
	                    	}
	                    } else {
	                    	new DialogOneButton(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Attention! Please select at least one printer!</html>", "Back").showDialog();
	                    }
	                    dialInfo.setModal(true);
	                }
	            });
	            
	            
	            
	        } else {
	            SwingUtilities.invokeLater(() -> {
	                progressBar.setVisible(false);
	                if (!stopThread) {
	                    dialInfo.setModal(false);
	                    new DialogOneButton(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Bambu Studio is not running!</html>", "Ok").showDialog();
	                    dialInfo.setModal(true);
	                }
	            });                    	
	        }
	    });
	
	    sendingThread.start();
	}
}