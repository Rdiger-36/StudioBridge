package rdiger36.StudioBridge;

import java.awt.Image;
import javax.swing.ImageIcon;

public class Resize {

	/**
	 * Skaliert ein ImageIcon auf eine neue Breite und Höhe.
	 * 
	 * @param originalIcon Das ursprüngliche ImageIcon, das skaliert werden soll.
	 * @param width        Die neue Breite des skalierten Icons.
	 * @param height       Die neue Höhe des skalierten Icons.
	 * @return             Das skalierte ImageIcon.
	 */
	public static ImageIcon setNewImageIconSize(ImageIcon originalIcon, int width, int heigth)	{
		
		Image originalImage = originalIcon.getImage(); // Transformiere es in ein Image
		Image resizedImage = originalImage.getScaledInstance(width, heigth, Image.SCALE_SMOOTH); // Skaliere das Image
		ImageIcon resizedIcon = new ImageIcon(resizedImage); // Transformiere es zurück in ein ImageIcon
		
		return resizedIcon;
	}	
}