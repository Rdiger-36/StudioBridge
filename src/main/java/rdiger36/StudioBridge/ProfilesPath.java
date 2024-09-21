package rdiger36.StudioBridge;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import jnafilechooser.api.JnaFileChooser;
import jnafilechooser.api.JnaFileChooser.Mode;
import net.miginfocom.swing.MigLayout;

/**
 * A dialog for selecting and setting the path for saving profiles.
 * <p>
 * This dialog allows the user to choose a directory for storing profiles 
 * and reset the path to a default value.
 * </p>
 */
public class ProfilesPath {

    private JDialog dial;
    private JTextField textField;
    private String response;
    private JButton btnReset;
    private JButton btnChooseDir;

    /**
     * Constructs a ProfilesPath dialog.
     *
     * @param mainFrame The parent frame to center the dialog.
     */
    public ProfilesPath(JFrame mainFrame) {

        dial = new JDialog();
        dial.setResizable(false);
        dial.setTitle("StudioBridge");
        dial.setIconImage(Toolkit.getDefaultToolkit().getImage(SaveDialog.class.getResource("/icon.png")));
        dial.setModal(true);
        dial.setAlwaysOnTop(true);
        dial.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                response = "";
                dial.dispose();
            }
        });
        dial.setSize(369, 103);
        if (mainFrame != null) {
            dial.setLocationRelativeTo(mainFrame);
        } else {
            dial.setLocationRelativeTo(null);
        }

        JLabel lblPath = new JLabel("Path:");
        lblPath.setHorizontalAlignment(SwingConstants.CENTER);

        dial.getContentPane().setLayout(new MigLayout("", "[grow][grow][200px,grow][200px,grow][grow]", "[14px,grow][23px,grow]"));
        dial.getContentPane().add(lblPath, "cell 0 0 2 1,alignx left,aligny center");

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = textField.getText().toString();
                dial.dispose();
            }
        });

        textField = new JTextField(MainMenu.ProfilesDir);
        dial.getContentPane().add(textField, "cell 2 0 2 1,grow");
        textField.setColumns(10);

        btnChooseDir = new JButton("...");
        dial.getContentPane().add(btnChooseDir, "cell 4 0,alignx right");
        dial.getContentPane().add(btnSave, "cell 2 1,alignx right,aligny center");

        btnReset = new JButton("Reset to default");
        dial.getContentPane().add(btnReset, "cell 3 1");

        btnReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textField.setText(MainMenu.defaultSavePath + System.getProperty("file.separator") + "Profiles");
            }
        });

        btnChooseDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JnaFileChooser fc = new JnaFileChooser();
                fc.setMode(Mode.Directories);
                fc.setTitle("Choose profile save directory");

                boolean returnValue = fc.showOpenDialog(dial);
                if (returnValue) {
                    textField.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        });

        btnSave.setMinimumSize(btnReset.getPreferredSize());

        dial.pack();
        dial.setLocationRelativeTo(mainFrame);
    }

    /**
     * Displays the dialog and returns the selected path.
     *
     * @return The selected path as a string.
     */
    public String changePath() {
        dial.setVisible(true); // Shows the dialog (blocking)
        return response; // Returns the state of the dialog
    }
}