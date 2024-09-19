package rdiger36.StudioBridge;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class LengthRestrictedDocumentFilter extends DocumentFilter {
    private int maxLength;

    public LengthRestrictedDocumentFilter(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) {
            return;
        }

        // Stelle sicher, dass die Länge nicht überschritten wird
        if ((fb.getDocument().getLength() + string.length()) <= maxLength) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
        if (string == null) {
            return;
        }

        // Prüfen, ob die neue Gesamtlänge die maximale Länge überschreitet
        if ((fb.getDocument().getLength() + string.length() - length) <= maxLength) {
            super.replace(fb, offset, length, string, attrs);
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);
    }
}
