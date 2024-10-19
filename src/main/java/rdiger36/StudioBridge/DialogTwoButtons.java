package rdiger36.StudioBridge;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Toolkit;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * The DialogTwoButtons class creates a modal dialog with two buttons for user confirmation.
 * It allows users to make a choice between two options.
 *
 * <p>This class provides a simple way to present decisions to users, enhancing interaction 
 * with the application.</p>
 */
public class DialogTwoButtons {
    private int response; // Indicates which button was pressed
    private JDialog dial; // The dialog window

    /**
     * Constructs a new DialogTwoButtons instance.
     *
     * @param mainFrame The main frame of the application, used to center the dialog.
     * @param mainDial An optional main dialog to center the new dialog relative to.
     * @param icon An optional icon to display in the dialog.
     * @param message The message to display in the dialog.
     * @param button1Text The text for the first button.
     * @param button2Text The text for the second button.
     */
    public DialogTwoButtons(JFrame mainFrame, JDialog mainDial, ImageIcon icon, String message, String button1Text, String button2Text) {
        dial = new JDialog();
        dial.setResizable(false);
        dial.setTitle("StudioBridge");
        dial.setIconImage(Toolkit.getDefaultToolkit().getImage(DialogTwoButtons.class.getResource("/icon.png")));
        dial.setModal(true);
        dial.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dial.setAlwaysOnTop(true);

        // Close action for the dialog
        dial.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                response = 99; // Set response to a default value when closed
                dial.dispose();
            }
        });

        dial.setSize(276, 167);

        // Create and configure the message label
        JLabel label = new JLabel(message);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        if (icon != null) {
            label.setIconTextGap(15);
            label.setIcon(Resize.setNewImageIconSize(icon, 24, 24));
        }

        // Button 1 configuration
        JButton btn1 = new JButton(button1Text);
        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = 0; // Set response to 0 for the first button
                dial.dispose(); // Close the dialog
            }
        });

        // Button 2 configuration
        JButton btn2 = new JButton(button2Text);
        btn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = 1; // Set response to 1 for the second button
                dial.dispose(); // Close the dialog
            }
        });

        // Layout configuration using MigLayout
        dial.getContentPane().setLayout(new MigLayout("", "[grow][87px][87px][grow]", "[14px,grow][10.00][23px,grow]"));
        dial.getContentPane().add(label, "cell 0 0 4 1,alignx center,aligny center");
        dial.getContentPane().add(btn1, "cell 1 2,alignx center,aligny center");
        dial.getContentPane().add(btn2, "cell 2 2,alignx center,aligny center");

        // Ensure both buttons have the same minimum size
        if (btn1.getText().length() > btn2.getText().length()) {
            btn2.setMinimumSize(btn1.getPreferredSize());
        } else {
            btn1.setMinimumSize(btn2.getPreferredSize());
        }

        dial.pack(); // Adjust dialog size to fit components

        // Center dialog relative to the main frame or another dialog
        if (mainFrame != null) {
            dial.setLocationRelativeTo(mainFrame);
        } else if (mainDial != null) {
            dial.setLocationRelativeTo(mainDial);
        } else {
            dial.setLocationRelativeTo(null);
        }
    }

    /**
     * Displays the dialog and waits for the user to respond.
     *
     * @return An integer indicating which button was pressed: 
     *         0 for the first button, 1 for the second button, 
     *         or 99 if the dialog was closed without selection.
     */
    public int showDialog() {
        dial.setVisible(true); // Show the dialog (blocking)
        return response; // Return the state of the dialog
    }
}