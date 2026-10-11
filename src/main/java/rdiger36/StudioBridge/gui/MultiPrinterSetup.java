package rdiger36.StudioBridge.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import rdiger36.StudioBridge.functions.*;
import rdiger36.StudioBridge.objects.Printer;
import rdiger36.StudioBridge.functions.I18n;

import java.awt.Component;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.formdev.flatlaf.extras.FlatSVGIcon;
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

    // Index of the column with the delete icon
    private static final int COLUMN_DELETE = 6;

    // Saves edited rows and keeps the main window in step
    private final ProfileManager profiles;
    
    private volatile boolean stopThread = false;
    private volatile boolean printerSelected = false;
    
    private static ArrayList<String> errors = new ArrayList<>();

    /**
     * Constructor that initializes the MultiPrinterSetup dialog.
     * 
     * @param frmStudioBridge The parent JFrame for positioning the dialog.
     */
    public MultiPrinterSetup(JFrame frmStudioBridge, ProfileManager profiles) {
        this.profiles = profiles;
        // A modal dialog of the main window: its own questions and messages belong to it and can be
        // answered, the main window waits until it is closed
        JDialog dialInfo = new JDialog(frmStudioBridge, true);
        dialInfo.setTitle(I18n.t("multi.title"));
        dialInfo.setIconImage(Toolkit.getDefaultToolkit().getImage(MultiPrinterSetup.class.getResource("/icon.png")));
        dialInfo.setBounds(100, 100, 602, 482);
        dialInfo.toFront();
        dialInfo.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialInfo.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (!confirmClose(dialInfo)) return;
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
            new String[] { I18n.t("multi.columnProfile"), I18n.t("multi.columnName"), I18n.t("multi.columnIp"), I18n.t("multi.columnSerial"), I18n.t("multi.columnModel"), I18n.t("multi.columnSend"), I18n.t("multi.columnDelete") }
        ) {
            Class<?>[] columnTypes = new Class[] {
                Object.class, Object.class, Object.class, Object.class, Object.class, Boolean.class, Object.class
            };

            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            // The profile name identifies the row, the other values can be adjusted for this send
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != COLUMN_DELETE;
            }
        });

        // The model is picked from the same list as in the main window, not typed
        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new ModelComboBox()));
        
        // Shown with a drop-down arrow, in the colors of the table row, so it is clear the
        // model can be changed
        table.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
            private final JPanel cell = new JPanel(new BorderLayout());
            private final JLabel text = new JLabel();
            private final JLabel arrow = new JLabel(new FlatSVGIcon("icons/chevron.svg"));
            {
                text.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                arrow.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
                cell.add(text, BorderLayout.CENTER);
                cell.add(arrow, BorderLayout.EAST);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                text.setText(value == null ? "" : value.toString());
                text.setFont(table.getFont());
                text.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                cell.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return cell;
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
        table.getColumnModel().getColumn(COLUMN_DELETE).setPreferredWidth(70);

        // Delete column: a trash icon per row, a click asks and deletes that profile
        FlatSVGIcon deleteIcon = new FlatSVGIcon("icons/delete.svg");
        table.getColumnModel().getColumn(COLUMN_DELETE).setCellRenderer(new TableCellRenderer() {
            private final JLabel cell = new JLabel(deleteIcon, SwingConstants.CENTER);
            {
                cell.setOpaque(true);
                cell.setToolTipText(I18n.t("profile.deleteAction"));
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                cell.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return cell;
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0 || table.columnAtPoint(e.getPoint()) != COLUMN_DELETE) return;
                stopEditing();
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                String name = String.valueOf(model.getValueAt(row, 0));
                if (profiles.delete(name, dialInfo)) model.removeRow(row);
            }
        });
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
        dialInfo.getContentPane().add(btnSendPrinters, "cell 0 1,grow,split 2");

        JButton btnSaveChanges = new JButton(I18n.t("multi.saveChanges"));
        btnSaveChanges.addActionListener(e -> saveChanges(dialInfo, true));
        dialInfo.getContentPane().add(btnSaveChanges, "cell 0 1,grow");

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
	    	model.addRow(new Object[]{printer.getProfileName(), printer.getPrinterName(), printer.getIpAdress(), printer.getSerialNumber(), printer.getPrinterModel(), false, null});
        }
    }

    // The values of a table row as profile
    private Printer rowPrinter(DefaultTableModel model, int row) {
        return new Printer(String.valueOf(model.getValueAt(row, 0)), text(model.getValueAt(row, 1)), text(model.getValueAt(row, 2)), text(model.getValueAt(row, 3)), text(model.getValueAt(row, 4)));
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }

    // Whether a row differs from the profile as it is saved
    private static boolean changed(Printer row, Printer saved) {
        return saved == null
                || !row.getPrinterName().equals(saved.getPrinterName())
                || !row.getIpAdress().equals(saved.getIpAdress())
                || !row.getSerialNumber().equals(saved.getSerialNumber())
                || !row.getPrinterModel().equals(saved.getPrinterModel());
    }

    // Ends a cell edit that is still open, so its value counts
    private void stopEditing() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
    }

    // Whether any row differs from its saved profile
    private boolean hasChanges() {
        stopEditing();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            Printer row = rowPrinter(model, i);
            if (changed(row, MainMenu.printerMap.get(row.getProfileName()))) return true;
        }
        return false;
    }

    /**
     * Saves every changed row to its profile. Incomplete rows are not saved and named.
     *
     * @param dialInfo the dialog, parent of the messages
     * @param confirm whether to say so when everything was saved
     * @return true when every changed row was saved
     */
    private boolean saveChanges(JDialog dialInfo, boolean confirm) {
        stopEditing();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        StringBuilder incomplete = new StringBuilder();
        boolean allSaved = true;
        for (int i = 0; i < model.getRowCount(); i++) {
            Printer row = rowPrinter(model, i);
            if (!changed(row, MainMenu.printerMap.get(row.getProfileName()))) continue;
            if (row.getPrinterName().trim().isEmpty() || row.getIpAdress().trim().isEmpty()
                    || row.getSerialNumber().trim().isEmpty() || row.getPrinterModel().isEmpty()) {
                incomplete.append(row.getProfileName()).append("<br>");
                allSaved = false;
            } else if (!profiles.update(row)) {
                allSaved = false;
            }
        }
        if (incomplete.length() > 0) {
            new DialogOneButton(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("multi.incomplete", "profiles", incomplete), I18n.t("common.ok")).showDialog();
        } else if (allSaved && confirm) {
            new DialogOneButton(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/success.png")), I18n.t("multi.saved"), I18n.t("common.ok")).showDialog();
        }
        return allSaved;
    }

    // Asks about unsaved changes before closing; false keeps the dialog open
    private boolean confirmClose(JDialog dialInfo) {
        if (!hasChanges()) return true;
        int response = new DialogTwoButtons(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("multi.unsaved"), I18n.t("multi.saveChanges"), I18n.t("common.continueWithoutSaving")).showDialog();
        if (response == 1) return true;
        return response == 0 && saveChanges(dialInfo, false);
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
	    stopEditing();
	    progressBar.setString(I18n.t("multi.checking"));
	    progressBar.setVisible(true);

	    // Read the table here on the EDT, the thread only gets a copy
	    DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
	    List<Printer> selected = new ArrayList<>();
	    for (int i = 0; i < tableModel.getRowCount(); i++) {
	        Boolean shouldSend = (Boolean) tableModel.getValueAt(i, 5);
	        if (shouldSend != null && shouldSend) selected.add(rowPrinter(tableModel, i));
	    }

	    Thread sendingThread = new Thread(() -> {
	        printerSelected = false;
	        
			// Determine which UDP port (2021 or 1990) is in use, or set to 0 if none is available
	        int remoteUdpPort = UDPPackage.getAvailableUDPPort();
	
	        // Check if a valid port was assigned to remoteUdpPort
	        if (remoteUdpPort > 0) {
	
	            for (Printer printer : selected) {
	                if (stopThread) break;
	                printerSelected = true;
	                SwingUtilities.invokeLater(() -> progressBar.setString(I18n.t("multi.sending", "name", printer.getPrinterName())));
	                if (!UDPPackage.send(frmStudioBridge, printer, remoteUdpPort, true)) {
	                    errors.add(printer.getPrinterName() + " - " + printer.getIpAdress() + " - " + printer.getPrinterModel());
	                }
	            }
	
	            SwingUtilities.invokeLater(() -> {
	                progressBar.setVisible(false);
	                if (!stopThread) {
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
	                }
	            });
	            
	            
	            
	        } else {
	            SwingUtilities.invokeLater(() -> {
	                progressBar.setVisible(false);
	                if (!stopThread) {
	                    new DialogOneButton(null, dialInfo, new ImageIcon(MainMenu.class.getResource("/achtung.png")), I18n.t("send.slicerNotRunning"), I18n.t("common.ok")).showDialog();
	                }
	            });                    	
	        }
	    });
	
	    sendingThread.start();
	}
}