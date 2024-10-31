package rdiger36.StudioBridge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    public static void send(JFrame mainFrame, String printerIP, String PrinterSN, String PrinterModel, String PrinterName) {
        String localhost = "127.0.0.1";         // IP address of the local PC
        String PrinterSignal = "-44";           // WiFi signal quality
        String PrinterConnection = "lan";       // Connection mode (LAN)
        String PrinterBindCloud = "free";       // Cloud status (not connected)
        
        // Determine which UDP port (2021 or 1990) is in use, or set to 0 if none is available
        int remoteUdpPort = getAvailableUDPPort();
        int sourceUdpPort = 0;                  // Source port (0 = system assigned)

        // Check if a valid port was assigned to remoteUdpPort
        if (remoteUdpPort > 0) {
            try {
                // Current date for HTTP headers
                String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").format(new Date());

                // Create the message to be sent
                String message = "HTTP/1.1 200 OK\r\n" +
                        "Server: Buildroot/2018.02-rc3 UPnP/1.0 ssdpd/1.8\r\n" +
                        "Date: " + date + "\r\n" +
                        "Location: " + printerIP + "\r\n" +
                        "ST: urn:bambulab-com:device:3dprinter:1\r\n" +
                        "EXT:\r\n" +
                        "USN: " + PrinterSN + "\r\n" +
                        "Cache-Control: max-age=1800\r\n" +
                        "DevModel.bambu.com: " + PrinterModel + "\r\n" +
                        "DevName.bambu.com: " + PrinterName + "\r\n" +
                        "DevSignal.bambu.com: " + PrinterSignal + "\r\n" +
                        "DevConnect.bambu.com: " + PrinterConnection + "\r\n" +
                        "DevBind.bambu.com: " + PrinterBindCloud + "\r\n\r\n";

                // Initialize the UDP client
                DatagramSocket udpClient = new DatagramSocket(sourceUdpPort);
                byte[] byteBuffer = message.getBytes(StandardCharsets.US_ASCII);
                InetAddress remoteIp = InetAddress.getByName(localhost);

                // Send the UDP message
                DatagramPacket packet = new DatagramPacket(byteBuffer, byteBuffer.length, remoteIp, remoteUdpPort);
                udpClient.send(packet);

                // Confirmation of sent bytes
                if (packet.getLength() != byteBuffer.length) {
                    new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to send the package to Bambu Studio!</html>", "Ok").showDialog();
                } else {
                    new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/success.png")), "<html>Package sent successfully to Bambu Studio</html>", "Ok").showDialog();
                }

                // Close the UDP client
                udpClient.close();
            } catch (Exception e) {
                // Show a warning that the package could not be sent to Bambu Studio
                new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Failed to send the package to Bambu Studio!</html>", "Ok").showDialog();
            }
        } else {
            // Show a warning if Bambu Studio is not running
            new DialogOneButton(mainFrame, null, new ImageIcon(MainMenu.class.getResource("/achtung.png")), "<html>Warning! Bambu Studio is not running!</html>", "Ok").showDialog();
        }
    }
    
    /**
     * Retrieves the first available UDP port from a list of specified ports (2021 or 1990).
     * Uses `lsof` on macOS, and Java socket binding on other operating systems.
     *
     * @return the port number if available, or 0 if none are available.
     */
    private static int getAvailableUDPPort() {
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
}