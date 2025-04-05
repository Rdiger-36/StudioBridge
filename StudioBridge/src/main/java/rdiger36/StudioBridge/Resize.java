package rdiger36.StudioBridge;

import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * A utility class for resizing image icons.
 * <p>
 * This class provides methods to scale image icons to specified dimensions.
 * </p>
 */
public class Resize {

    /**
     * Scales an ImageIcon to a new width and height.
     *
     * @param originalIcon The original ImageIcon to be scaled.
     * @param width        The new width of the scaled icon.
     * @param height       The new height of the scaled icon.
     * @return            The scaled ImageIcon.
     */
    public static ImageIcon setNewImageIconSize(ImageIcon originalIcon, int width, int height) {
        
        Image originalImage = originalIcon.getImage(); // Transform the ImageIcon to an Image
        Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH); // Scale the image
        ImageIcon resizedIcon = new ImageIcon(resizedImage); // Transform back to an ImageIcon
        
        return resizedIcon;
    }   
}