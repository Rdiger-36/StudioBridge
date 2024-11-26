package rdiger36.StudioBridge;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

/**
 * The DialogOneButton class creates a modal dialog with a single button 
 * for user confirmation or information display. It can be used to present 
 * messages to the user and capture their response.
 *
 * <p>This class is designed to simplify the creation of dialogs in the 
 * application, providing a consistent look and feel.</p>
 */
public class DialogOneButton {

    private boolean response; // Indicates whether the dialog was confirmed or canceled
    private JDialog dial;     // The dialog window

    /**
     * Constructs a new DialogOneButton instance.
     *
     * @param mainFrame The main frame of the application, used to center the dialog.
     * @param mainDial An optional main dialog to center the new dialog relative to.
     * @param icon An optional icon to display in the dialog.
     * @param message The message to display in the dialog.
     * @param buttonText The text for the button that closes the dialog.
     */
    public DialogOneButton(JFrame mainFrame, JDialog mainDial, ImageIcon icon, String message, String buttonText) {
        dial = new JDialog();
        dial.setResizable(false);
        dial.setTitle("StudioBridge");
        dial.setIconImage(Toolkit.getDefaultToolkit().getImage(DialogOneButton.class.getResource("/icon.png")));
        dial.setModal(true);
        dial.setAlwaysOnTop(true);
        
        // Close action for the dialog
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

        // Create and configure the message label
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

        // Layout configuration using MigLayout
        dial.getContentPane().setLayout(new MigLayout("", "[grow][87px][][87px][grow]", "[14px,grow][10.00][23px,grow]"));
        dial.getContentPane().add(label, "cell 0 0 5 1,alignx center,aligny center");

        // Button to close the dialog
        JButton btn1 = new JButton(buttonText);
        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = true; // Set response to true when button is clicked
                dial.dispose();  // Close the dialog
            }
        });
        dial.getContentPane().add(btn1, "cell 2 2,alignx center,aligny center");
        if (buttonText.contains("NoButton")) btn1.setVisible(false); // Optionally hide button

        dial.pack(); // Adjust dialog size to fit components
        dial.setLocationRelativeTo(mainFrame); // Center dialog relative to the main frame
    }

    /**
     * Displays the dialog and waits for the user to respond.
     *
     * @return true if the dialog was confirmed, false otherwise.
     */
    public boolean showDialog() {
        dial.setVisible(true); // Show the dialog (blocking)
        return response; // Return the state of the dialog
    }
}