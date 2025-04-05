package rdiger36.StudioBridge;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * A DocumentFilter that restricts the length of text in a document.
 * <p>
 * This filter ensures that the total length of the document does not exceed 
 * a specified maximum length when text is inserted or replaced.
 * </p>
 */
public class LengthRestrictedDocumentFilter extends DocumentFilter {
    private int maxLength;

    /**
     * Constructs a LengthRestrictedDocumentFilter with the specified maximum length.
     *
     * @param maxLength The maximum allowed length of the document.
     */
    public LengthRestrictedDocumentFilter(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Inserts the specified string at the given offset if it does not 
     * exceed the maximum length of the document.
     *
     * @param fb     The FilterBypass to use for the insert operation.
     * @param offset The offset at which to insert the string.
     * @param string The string to insert.
     * @param attr   The attributes to apply to the inserted string.
     * @throws BadLocationException If the insert position is invalid.
     */
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) {
            return;
        }

        // Ensure that the total length does not exceed the maximum length
        if ((fb.getDocument().getLength() + string.length()) <= maxLength) {
            super.insertString(fb, offset, string, attr);
        }
    }

    /**
     * Replaces text in the document at the specified offset if the result 
     * does not exceed the maximum length.
     *
     * @param fb     The FilterBypass to use for the replace operation.
     * @param offset The offset at which to start replacing text.
     * @param length The length of the text to be replaced.
     * @param string The string to insert in place of the replaced text.
     * @param attrs  The attributes to apply to the replaced string.
     * @throws BadLocationException If the replace position is invalid.
     */
    @Override
    public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
        if (string == null) {
            return;
        }

        // Check if the new total length exceeds the maximum length
        if ((fb.getDocument().getLength() + string.length() - length) <= maxLength) {
            super.replace(fb, offset, length, string, attrs);
        }
    }

    /**
     * Removes text from the document at the specified offset.
     *
     * @param fb     The FilterBypass to use for the remove operation.
     * @param offset The offset at which to start removing text.
     * @param length The length of the text to remove.
     * @throws BadLocationException If the remove position is invalid.
     */
    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);
    }
}