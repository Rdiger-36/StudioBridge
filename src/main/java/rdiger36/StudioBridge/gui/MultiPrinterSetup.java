package rdiger36.StudioBridge.gui;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import rdiger36.StudioBridge.functions.*;
import rdiger36.StudioBridge.objects.Printer;
import rdiger36.StudioBridge.functions.I18n;

import java.awt.Component;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.table.TableCellRenderer;
import javax.swing.DefaultCellEditor;
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
    public MultiPrinterSetup(JFrame frmStudioBridge) {
        JDialog dialInfo = new JDialog();
        dialInfo.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        dialInfo.setModal(true);
        dialInfo.setAlwaysOnTop(true);
        dialInfo.setTitle(I18n.t("multi.title"));
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
            new String[] { I18n.t("multi.columnProfile"), I18n.t("multi.columnName"), I18n.t("multi.columnIp"), I18n.t("multi.columnSerial"), I18n.t("multi.columnModel"), I18n.t("multi.columnSend") }
        ) {
            Class<?>[] columnTypes = new Class[] {
                Object.class, Object.class, Object.class, Object.class, Object.class, Boolean.class
            };

            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            // The profile name identifies the row, the other values can be adjusted for this send
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        });

        // The model is picked from the same list as in the main window, not typed
        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new ModelComboBox()));
        
        // Shown as a combo box before it is clicked, so it is clear the model can be changed
        table.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
            private final JComboBox<String> box = new JComboBox<>();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                box.removeAllItems();
                box.addItem(value == null ? "" : value.toString());
                return box;
            }
        });
        table.setRowHeight(Math.max(table.getRowHeight(), new JComboBox<String>().getPreferredSize().height));

        // Set preferred column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(164);
        table.getColumnModel().getColumn(3).setPreferredWidth(159);
        table.getColumnModel().getColumn(4).setPreferredWidth(170);
        table.getColumnModel().getColumn(5).setPreferredWidth(93);
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

        JButton btnSendPrinters = new JButton(I18n.t("multi.sendSelected"));
        btnSendPrinters.addActionListener(e -> sendSelectedPrinters(frmStudioBridge, dialInfo, progressBar));
        dialInfo.getContentPane().add(btnSendPrinters, "cell 0 1,grow");

        JButton btnSelAll = new JButton(I18n.t("multi.selectAll"));
        btnSelAll.addActionListener(e -> setAllBooleans(true));
        dialInfo.getContentPane().add(btnSelAll, "cell 2 1,alignx right");

        JButton btnDeSelAll = new JButton(I18n.t("multi.deselectAll"));
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
        
	    List<Printer> printers = new ArrayList<>(MainMenu.printerMap.values());
	    printers.sort((a, b) -> a.getProfileName().compareToIgnoreCase(b.getProfileName()));
	    for (Printer printer : printers) {
	    	model.addRow(new Object[]{printer.getProfileName(), printer.getPrinterName(), printer.getIpAdress(), printer.getSerialNumber(), printer.getPrinterModel(), false});
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
            model.setValueAt(value, i, 5);
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
	        progressBar.setString(I18n.t("multi.checking"));
	        progressBar.setVisible(true);
	
	        printerSelected = false;
	        
			// Determine which UDP port (2021 or 1990) is in use, or set to 0 if none is available
	        int remoteUdpPort = UDPPackage.getAvailableUDPPort();
	
	        // Check if a valid port was assigned to remoteUdpPort
	        if (remoteUdpPort > 0) {
	
	            DefaultTableModel model = (DefaultTableModel) table.getModel();
	            int rowCount = model.getRowCount();
	            	                    
	            for (int i = 0; i < rowCount && !stopThread; i++) {
	                Boolean shouldSend = (Boolean) model.getValueAt(i, 5);
	                if (shouldSend != null && shouldSend) {
	                	
	                	printerSelected = true;
	                	
	                	Printer printer = new Printer((String) model.getValueAt(i, 0), (String) model.getValueAt(i, 1), (String) model.getValueAt(i, 2), (String) model.getValueAt(i, 3), (String) model.getValueAt(i, 4));
	
	                    SwingUtilities.invokeLater(() -> progressBar.setString(I18n.t("multi.sending", "name", printer.getPrinterName())));
	
	                    if (!UDPPackage.send(frmStudioBridge, printer, remoteUdpPort, true)) {
	                    	errors.add(printer.getPrinterName() + " - " + printer.getIpAdress() + " - " + printer.getPrinterModel());
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
	                    		
	                    		new DialogOneButton(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("multi.failed", "printers", errorsOn), I18n.t("common.ok")).showDialog();
	                    	} else {
	                    		new DialogOneButton(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/success.png")), I18n.t("multi.success"), I18n.t("common.ok")).showDialog();
	                    	}
	                    } else {
	                    	new DialogOneButton(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("multi.noneSelected"), I18n.t("common.back")).showDialog();
	                    }
	                    dialInfo.setModal(true);
	                }
	            });
	            
	            
	            
	        } else {
	            SwingUtilities.invokeLater(() -> {
	                progressBar.setVisible(false);
	                if (!stopThread) {
	                    dialInfo.setModal(false);
	                    new DialogOneButton(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("send.slicerNotRunning"), I18n.t("common.ok")).showDialog();
	                    dialInfo.setModal(true);
	                }
	            });                    	
	        }
	    });
	
	    sendingThread.start();
	}
}