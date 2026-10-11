package rdiger36.StudioBridge.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import rdiger36.StudioBridge.functions.I18n;

/**
 * The printer model selection: the models grouped under bold series headers, which can not be
 * selected themselves. Used in the main window and in the table of "Send all printers".
 */
public class ModelComboBox extends JComboBox<String> {

    private static final long serialVersionUID = 1L;

    /** The models in the order of the list, each series starts with its header ("X-Series"). */
    public static final String[] ITEMS = {
        "A-Series", "A1", "A1 Mini", "A2L",
        "H-Series", "H2C", "H2C (new revision)", "H2D", "H2D Pro", "H2S",
        "P-Series", "P1P", "P1S", "P2S",
        "X-Series", "X1", "X1C", "X1E", "X2D"
    };

    // Direction of the last arrow key, so a header is skipped in the direction the user moves
    private final AtomicInteger lastDirection = new AtomicInteger(0);

    public ModelComboBox() {
        super(ITEMS);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMaximumRowCount(20);

        // Series headers bold, models indented below them
        setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (index == -1 && getSelectedIndex() == -1) {
                    lbl.setText(I18n.t("main.selectModel"));
                    return lbl;
                }

                String text = value == null ? "" : value.toString();
                boolean isCategory = isSeries(text);

                // The series entries keep their English value, only the shown text is translated
                if (isCategory) lbl.setText(I18n.t("main.modelSeries", "series", text.substring(0, text.indexOf('-'))));

                if (index >= 0) {
                    if (isCategory) {
                        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                        lbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                    } else {
                        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                        lbl.setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 4));
                    }
                } else {
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                    lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                }
                return lbl;
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    lastDirection.set(-1);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    lastDirection.set(1);
                }
            }
        });

        addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                lastDirection.set(0);
            }
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        // A series header can not be selected: move on to the next model, or back when moving up
        addActionListener(arg -> {
            int idx = getSelectedIndex();
            if (idx < 0 || !isSeries(getItemAt(idx))) return;
            if (idx > 0 && lastDirection.get() == -1) {
                setSelectedIndex(idx - 1);
            } else {
                setSelectedIndex(idx + 1);
            }
        });

        setSelectedIndex(-1);
    }

    /** Whether an entry is a series header and not a model. */
    public static boolean isSeries(String item) {
        return item != null && item.endsWith("-Series");
    }
}
