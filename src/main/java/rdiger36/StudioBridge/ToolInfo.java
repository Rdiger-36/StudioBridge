package rdiger36.StudioBridge;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.awt.Toolkit;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dialog.ModalExclusionType;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.Cursor;
import java.awt.Desktop;
import javax.swing.LayoutStyle.ComponentPlacement;

public class ToolInfo {
	
	public ToolInfo(JFrame frmBambuConnect) {
		
		JDialog dialInfo = new JDialog();
		dialInfo.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		dialInfo.setModal(true);
		dialInfo.setAlwaysOnTop(true);
		dialInfo.setTitle("StudioBridge  Info");
		dialInfo.setResizable(false);
		dialInfo.setIconImage(Toolkit.getDefaultToolkit().getImage(ToolInfo.class.getResource("/icon.png")));
		dialInfo.setBounds(100, 100, 386, 250);
		dialInfo.toFront();
		dialInfo.addWindowListener(new WindowAdapter() {	
			public void windowClosing(WindowEvent e) {
				dialInfo.dispose();
			}
		});
				
		JEditorPane dtrpnDeveloper = new JEditorPane("text/html", "<html><body>\r\nDeveloper: Niklas (Rdiger-36)<br>\r\nGitHub: <a href=\"https://github.com/Rdiger-36/StudioBridge\" >StudioBridge Repo</a><br>\r\n<br>\r\nIcon Source: <a href=\"https://www.flaticon.com/de/kostenlose-icons/3d-drucker\">Freepik - Flaticon</a>\r\n<br>\r\n<br>\r\n</body></html>");
        dtrpnDeveloper.setEditable(false);
        dtrpnDeveloper.setOpaque(false);
        dtrpnDeveloper.setBorder(null);
        dtrpnDeveloper.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		dtrpnDeveloper.setCaretColor(dtrpnDeveloper.getBackground());
        dtrpnDeveloper.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                	dtrpnDeveloper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                	dtrpnDeveloper.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                	try {
                		 String clickedText = dtrpnDeveloper.getDocument().getText(e.getSourceElement().getStartOffset(),
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
		
		JLabel lbueberschrift = new JLabel("StudioBridge");
		lbueberschrift.setHorizontalAlignment(SwingConstants.CENTER);
		lbueberschrift.setIconTextGap(25);
		lbueberschrift.setForeground(new Color(229, 152, 67));
		lbueberschrift.setFont(new Font("Segoe UI", Font.BOLD, 16));
		ImageIcon icon = Resize.setNewImageIconSize(new ImageIcon(ToolInfo.class.getResource("/icon.png")), 64, 64);
		lbueberschrift.setIcon(icon);
		
		JSeparator separator = new JSeparator();
		
		GroupLayout groupLayout = new GroupLayout(dialInfo.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(lbueberschrift, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
						.addComponent(separator, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(10)
							.addComponent(dtrpnDeveloper, GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lbueberschrift, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(dtrpnDeveloper)
					.addPreferredGap(ComponentPlacement.RELATED))
		);
		
		dialInfo.requestFocus();
		dialInfo.getContentPane().setLayout(groupLayout);
		dialInfo.pack();
		dialInfo.setLocationRelativeTo(frmBambuConnect);
		dialInfo.setVisible(true);
	}
}