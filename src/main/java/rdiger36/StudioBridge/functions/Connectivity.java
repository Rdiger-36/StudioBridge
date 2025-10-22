package rdiger36.StudioBridge.functions;

import javax.net.ssl.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Utility class that provides methods to check network connectivity,
 * including TLS handshake validation and reachability tests.
 */
public class Connectivity {

    /**
     * Attempts to establish a TLS connection to the specified host on port 6000.
     * <p>
     * This method:
     * <ul>
     *   <li>Creates a custom SSLContext that trusts all certificates (insecure, for testing purposes).</li>
     *   <li>Opens a TLS socket to the host and performs a handshake.</li>
     *   <li>Sends a basic HTTP GET request to test communication.</li>
     * </ul>
     *
     * @param host The hostname or IP address of the target server.
     * @return {@code true} if the TLS handshake was successful (or if data exchange
     *         succeeded after handshake failure); {@code false} otherwise.
     */
    public static boolean checkTLSHandshake(String host) {
        int port = 6000;
        SSLSocket socket = null;
        boolean handshakeDone = false;

        try {
            // Create an SSL context that trusts all certificates (not recommended for production use)
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }}, new SecureRandom());

            // Create an SSL socket
            SSLSocketFactory factory = sc.getSocketFactory();
            socket = (SSLSocket) factory.createSocket();

            // Set connection timeout
            socket.setSoTimeout(5000);

            // Connect to host and start TLS handshake
            socket.connect(new InetSocketAddress(host, port), 5000);
            socket.startHandshake();
            handshakeDone = true;

            // Send a simple HTTP GET request to check data transmission
            String http = "GET / HTTP/1.1\r\n" +
                    "Host: " + host + ":" + port + "\r\n" +
                    "User-Agent: java-ssl-probe\r\n" +
                    "Accept: */*\r\n" +
                    "Connection: close\r\n\r\n";
            OutputStream out = socket.getOutputStream();
            out.write(http.getBytes(StandardCharsets.US_ASCII));
            out.flush();

            // Read one byte to verify response from server
            InputStream in = socket.getInputStream();
            byte[] one = in.readNBytes(1);
            boolean gotResponse = one.length > 0;
            return handshakeDone && gotResponse;

        } catch (SSLHandshakeException e) {
            // TLS handshake failed (likely invalid certificate or protocol mismatch)
            return false;

        } catch (Exception e) {
            // If handshake was already done but a later error occurred, still return true
            if (handshakeDone) {
                return true;
            }
            return false;

        } finally {
            // Clean up the socket
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ignore) {}
            }
        }
    }

    /**
     * Attempts to checkPort 6000 of the printer to check network reachability.
     *
     * @param host The hostname or IP address to ping.
     * @return {@code true} if the host is reachable within 5 seconds,
     *         {@code false} otherwise.
     */
    public static boolean pingPrinter(String host) {
        try (java.net.Socket s = new java.net.Socket()) {
            s.connect(new java.net.InetSocketAddress(host, 8883), 5000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
