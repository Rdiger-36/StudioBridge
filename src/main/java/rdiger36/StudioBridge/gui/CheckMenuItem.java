package rdiger36.StudioBridge.gui;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * A menu entry that is switched on and off like a check box, and shows its check mark as icon.
 *
 * <p>The menu bar of macOS draws the check mark of a check box entry in a column of its own,
 * left of the icons of the other entries. As icon, the check mark stands in the same column as
 * those icons, on every platform. Switched off, an empty icon keeps the text in line.</p>
 */
public class CheckMenuItem extends JMenuItem {

    private static final long serialVersionUID = 1L;

    private boolean checked;

    /**
     * @param text the text of the entry
     * @param checked whether the entry is switched on
     */
    public CheckMenuItem(String text, boolean checked) {
        super(text);
        setChecked(checked);
    }

    /** Whether the entry is switched on. */
    public boolean isChecked() {
        return checked;
    }

    /** Switches the entry on or off without telling the listeners. */
    public void setChecked(boolean checked) {
        this.checked = checked;
        setIcon(new FlatSVGIcon(checked ? "icons/check.svg" : "icons/blank.svg"));
    }

    /** A click switches the entry before the listeners hear of it, so they read the new state. */
    @Override
    protected void fireActionPerformed(ActionEvent event) {
        setChecked(!checked);
        super.fireActionPerformed(event);
    }
}
