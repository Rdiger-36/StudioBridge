package rdiger36.StudioBridge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * The UDPPackage class provides functionality to send UDP packets
 * to a local Bambu Studio instance for 3D printers. It includes methods
 * to create and send a specific UDP message containing printer details.
 *
 * <p>This class facilitates communication between the application
 * and Bambu Studio, enabling the transmission of essential information
 * about the connected 3D printer.</p>
 *
 * <h2>Methods:</h2>
 * <ul>
 *   <li><code>send(JFrame, String, String, String, String)</code> - Sends a specific UDP data packet to the local Bambu Studio instance with the printer's details.</li>
 *   <li><code>getAvailableUDPPort()</code> - Retrieves the first available UDP port from the specified list of ports.</li>
 * </ul>
 */
public class UDPPackage {
    	
    /**
     * Sends a specific UDP data packet to the local Bambu Studio instance.
     *
     * @param mainFrame The main frame of the application, used to show dialogs.
     * @param printerIP The IP address of the 3D printer.
     * @param PrinterSN The serial number of the 3D printer.
     * @param PrinterModel The model of the 3D printer.
     * @param PrinterName The display name of the 3D printer.
     */
    public static boolean send(JFrame mainFrame, String printerIP, String printerSN, String printerModel, String printerName, int remoteUdpPort, boolean multiMode) {
        
        try {

            // Create the message to be sent
            String message = "NOTIFY * HTTP/1.1\r\n" +
                    "HOST: 255.255.255.255:2021\r\n" +
                    "Server: Buildroot/2018.02-rc3 UPnP/1.0 ssdpd/1.8\r\n" +
                    "Location: " + printerIP + "\r\n" +
                    "NT: urn:bambulab-com:device:3dprinter:1\r\n" +
                    "USN: " + printerSN + "\r\n" +
                    "Cache-Control: max-age=1800\r\n" +
                    "DevModel.bambu.com: " + printerModel + "\r\n" +
                    "DevName.bambu.com: " + printerName + "\r\n" +
                    "DevSignal.bambu.com: -40\r\n" +
                    "DevConnect.bambu.com: lan\r\n" +
                    "DevBind.bambu.com: free\r\n" +
                    "Devseclink.bambu.com: secure\r\n" +
                    "\r\n";

            UDPPackage.startBroadcast(printerIP, printerSN, printerIP, printerName, message);

            if (!multiMode && mainFrame != null) new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/success.png")), "<html>Package sent successfully.<br>The Printer will appear in the next 60 seconds in Bambu Studio.<br>You can close this Programm when your printer(s) appear in Bambu Studio</html>", "Ok").showDialog();

        } catch (Exception e) {
            // Show a warning that the package could not be sent to Bambu Studio
        	 if (!multiMode && mainFrame != null) new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to send the package!</html>", "Ok").showDialog();
        	 return false;
        }
        
        return true;
    }
    
    /**
     * Retrieves the first available UDP port from a list of specified ports (2021 or 1990).
     * Uses `lsof` on macOS, and Java socket binding on other operating systems.
     *
     * @return the port number if available, or 0 if none are available.
     */
    static int getAvailableUDPPort() {
        int[] portsToCheck = {2021, 1990};
        
        String os = System.getProperty("os.name").toLowerCase();
        for (int port : portsToCheck) {
            boolean isInUse;
            if (os.contains("mac")) {
                // On macOS, check port using lsof
                isInUse = isPortInUseWithLsof(port);
            } else {
                // On other systems, use socket-based port check
                isInUse = isUDPPortInUse(port);
            }
            if (isInUse) {
                return port; // Return the first available port
            }
        }
        return 0; // Return 0 if no port is available
    }

    /**
     * Checks if a specific UDP port is in use on macOS using the lsof command.
     *
     * @param port The UDP port number to check.
     * @return true if the port is in use, false otherwise.
     */
    private static boolean isPortInUseWithLsof(int port) {
        try {
            // Run lsof command to check if the port is in use
            Process process = new ProcessBuilder("lsof", "-i", "udp:" + port).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine() != null; // If output is not null, port is in use
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a specific UDP port is in use using Java socket binding.
     *
     * @param port The UDP port number to check.
     * @return true if the port is already in use, false otherwise.
     */
    private static boolean isUDPPortInUse(int port) {
        DatagramSocket socket = null;
        try {
            // Attempt to create a socket on the specified port
            socket = new DatagramSocket(port);
            return false; // Port is available
        } catch (SocketException e) {
            return true; // Port is already in use
        } finally {
            // Close the socket if it was successfully opened
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    public static void startBroadcast(String printerIP, String printerSN, String printerModel, String printerName, String message) {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);

                for (int i = 0; i < 10; i++) {
                    byte[] data = message.getBytes(StandardCharsets.US_ASCII);
                    InetAddress address = InetAddress.getByName("255.255.255.255");
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, 2021);
                    socket.send(packet);
                    Thread.sleep(6 * 1000);
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }    
}