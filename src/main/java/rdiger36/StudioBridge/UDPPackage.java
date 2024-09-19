package rdiger36.StudioBridge;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class UDPPackage {

	/** 
	 * Methode zum senden eines spezifischen UDP Daten Paketes an die loakle BambuStudio instanz.
	 * @param String PRINTER_IP, IP-Adresse des 3D Druckers
	 * @param String PRINTER_USN, Seriennummer des 3D Druckers
	 * @param String PRINTER_DEV_MODEL, Modell des 3D Druckers
	 * @param String PRINTER_DEV_NAME, Anzeigename des 3D Druckers
	 *  
	 */
    public static void send(JFrame frmBambuConnect, String PRINTER_IP, String PRINTER_USN, String PRINTER_DEV_MODEL, String PRINTER_DEV_NAME ) {
    	    
    	if (PRINTER_IP.equals("") || PRINTER_USN.equals("") || PRINTER_DEV_MODEL.equals("") || PRINTER_DEV_NAME.equals("")) {
    		 new DialogOneButton(frmBambuConnect, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Attention! Please check your inputs!</html>", "Back").showDialog();
    		 return;
    	}
    	
    	
        String TARGET_IP = "127.0.0.1";        // IP-Adresse des lokalen PCs
        String PRINTER_DEV_SIGNAL = "-44";     // WLAN-Signalqualität
        String PRINTER_DEV_CONNECT = "lan";    // Verbindungsmodus (LAN)
        String PRINTER_DEV_BIND = "free";      // Cloud-Status (nicht verbunden)

        int remoteUdpPort = 2021;              // UDP Port für BambuStudio
        int sourceUdpPort = 0;                 // Quellport (0 = vom System zugewiesen)
        
    	boolean isPortInUse = isUDPPortInUse(remoteUdpPort);

        if (isPortInUse) {
        
	        try {
	            // Aktuelles Datum für die HTTP-Header
	            String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").format(new Date());
	
	            // Erstellen der Nachricht
	            String message = "HTTP/1.1 200 OK\r\n" +
	                    "Server: Buildroot/2018.02-rc3 UPnP/1.0 ssdpd/1.8\r\n" +
	                    "Date: " + date + "\r\n" +
	                    "Location: " + PRINTER_IP + "\r\n" +
	                    "ST: urn:bambulab-com:device:3dprinter:1\r\n" +
	                    "EXT:\r\n" +
	                    "USN: " + PRINTER_USN + "\r\n" +
	                    "Cache-Control: max-age=1800\r\n" +
	                    "DevModel.bambu.com: " + PRINTER_DEV_MODEL + "\r\n" +
	                    "DevName.bambu.com: " + PRINTER_DEV_NAME + "\r\n" +
	                    "DevSignal.bambu.com: " + PRINTER_DEV_SIGNAL + "\r\n" +
	                    "DevConnect.bambu.com: " + PRINTER_DEV_CONNECT + "\r\n" +
	                    "DevBind.bambu.com: " + PRINTER_DEV_BIND + "\r\n\r\n";
	
	            // UDP-Client initialisieren
	            DatagramSocket udpClient = new DatagramSocket(sourceUdpPort);
	            byte[] byteBuffer = message.getBytes(StandardCharsets.US_ASCII);
	            InetAddress remoteIp = InetAddress.getByName(TARGET_IP);
	
	            // UDP-Nachricht senden
	            DatagramPacket packet = new DatagramPacket(byteBuffer, byteBuffer.length, remoteIp, remoteUdpPort);
	            udpClient.send(packet);
	
	            // Bestätigung der gesendeten Bytes
	            if (packet.getLength() != byteBuffer.length) {
	                new DialogOneButton(frmBambuConnect, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to send the package to Bambu Studio!</html>", "Ok").showDialog();
	            } else {
	                new DialogOneButton(frmBambuConnect, null, new ImageIcon(MainMenu.class.getResource("/success.png")), "<html>Package sent successfully to Bambu Studio</html>", "Ok").showDialog();
	            }
	
	            // UDP-Client schließen
	            udpClient.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	            new DialogOneButton(frmBambuConnect, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to send the package to Bambu Studio!</html>", "Ok").showDialog();
	        }
        } else {
        	 new DialogOneButton(frmBambuConnect, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Bambu Studio is not running!</html>", "Ok").showDialog();
        }
    }
    
    public static boolean isUDPPortInUse(int port) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
            return false; // Port ist verfügbar
        } catch (SocketException e) {
            return true; // Port ist bereits belegt
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Socket schließen, falls es erfolgreich geöffnet wurde
            }
        }
    }
}