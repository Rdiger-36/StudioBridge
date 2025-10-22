package rdiger36.StudioBridge.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import rdiger36.StudioBridge.functions.Resize;

/**
 * A dialog displaying information about the StudioBridge application.
 * <p>
 * This class creates a modal dialog that presents the developer's information,
 * GitHub link, and icon source.
 * </p>
 */
public class ToolInfo {

    /**
     * Constructs a ToolInfo dialog.
     *
     * @param frmBambuConnect The parent JFrame for positioning the dialog.
     */
    public ToolInfo(JFrame frmBambuConnect) {
        JDialog dialInfo = new JDialog();
        dialInfo.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        dialInfo.setModal(true);
        dialInfo.setAlwaysOnTop(true);
        dialInfo.setTitle("StudioBridge Info");
        dialInfo.setResizable(false);
        dialInfo.setIconImage(Toolkit.getDefaultToolkit().getImage(ToolInfo.class.getResource("/icon.png")));
        dialInfo.setBounds(100, 100, 386, 250);
        dialInfo.toFront();
        dialInfo.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dialInfo.dispose();
            }
        });

        JEditorPane dtrpnDeveloper = new JEditorPane("text/html", "<html><body>\r\n" +
                "Developer: Niklas (Rdiger-36)<br>\r\n" +
                "GitHub: <a href=\"https://github.com/Rdiger-36/StudioBridge\">StudioBridge Repo</a><br>\r\n" +
                "<br>\r\n" +
                "Icon Source: <a href=\"https://www.flaticon.com/de/kostenlose-icons/3d-drucker\">Freepik - Flaticon</a>\r\n" +
                "<br>\r\n" +
                "<br>\r\n" +
                "</body></html>");
        dtrpnDeveloper.setEditable(false);
        dtrpnDeveloper.setOpaque(false);
        dtrpnDeveloper.setBorder(null);
        dtrpnDeveloper.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dtrpnDeveloper.setCaretColor(dtrpnDeveloper.getBackground());
        
        // Hyperlink listener for handling clicks on links in the JEditorPane
        dtrpnDeveloper.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    dtrpnDeveloper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    dtrpnDeveloper.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        String clickedText = dtrpnDeveloper.getDocument().getText(
                                e.getSourceElement().getStartOffset(),
                                e.getSourceElement().getEndOffset() - e.getSourceElement().getStartOffset());

                        if (clickedText.equals("StudioBridge Repo")) {
               			 clickedText = "https://github.com/Rdiger-36/StudioBridge";
               		 } else if (clickedText.equals("Freepik - Flaticon")) {
               			 clickedText = "https://www.flaticon.com/de/kostenlose-icons/3d-drucker";
               		 }
               		 
   					Desktop.getDesktop().browse(new URI(clickedText));
   				} catch (Exception e1) {
   					e1.printStackTrace();
   				}
                }
            }
        });

        JLabel lbueberschrift = new JLabel("StudioBridge v" + MainMenu.version.substring(0, 1) + "." +
                MainMenu.version.substring(1, 2) + "." + MainMenu.version.substring(2, 3));
        lbueberschrift.setHorizontalAlignment(SwingConstants.CENTER);
        lbueberschrift.setIconTextGap(25);
        lbueberschrift.setForeground(new Color(229, 152, 67));
        lbueberschrift.setFont(new Font("Segoe UI", Font.BOLD, 16));
        ImageIcon icon = Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/icon.png")), 64, 64);
        lbueberschrift.setIcon(icon);

        JSeparator separator = new JSeparator();

        GroupLayout groupLayout = new GroupLayout(dialInfo.getContentPane());
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(lbueberschrift, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                                        .addComponent(separator, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addGap(10)
                                                .addComponent(dtrpnDeveloper, GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lbueberschrift, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dtrpnDeveloper)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED))
        );

        dialInfo.requestFocus();
        dialInfo.getContentPane().setLayout(groupLayout);
        dialInfo.pack();
        dialInfo.setLocationRelativeTo(frmBambuConnect);
        dialInfo.setVisible(true);
    }
}