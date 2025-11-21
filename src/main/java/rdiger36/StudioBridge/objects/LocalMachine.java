package rdiger36.StudioBridge.objects;

/**
 * Represents information about the local machine, such as operating system,
 * architecture, OS version, and data model (32/64 bit). The values are normalized
 * for consistent use across the application.
 */
public class LocalMachine {
    private final String os;
    private final String osArch;
    private final String osVersion;
    private final String dataModel;

    /**
     * Creates a new {@code LocalMachine} instance by gathering system properties
     * and normalizing them for unified representation.
     */
    public LocalMachine() {
        // Normalize OS name
        this.os	       = simplifyOS(System.getProperty("os.name"));
        // Normalize OS architecture
        this.osArch    = normalizeArch(System.getProperty("os.arch"));
        // Raw OS version
        this.osVersion = System.getProperty("os.version");
        // JVM data model (e.g., 32 or 64)
        this.dataModel = System.getProperty("sun.arch.data.model", "?");
    }

    /**
     * Converts raw architecture strings into a consistent format.
     *
     * @param raw the architecture string from system properties
     * @return normalized architecture identifier
     */
    private String normalizeArch(String raw) {
        if (raw == null) return "unknown";

        String lower = raw.toLowerCase();

        switch (lower) {
            case "amd64":
            case "x86_64":
                return "x86_64";
            case "aarch64":
            case "arm64":
                return "arm64";
            case "x86":
            case "i386":
            case "i686":
                return "x86";
            default:
                return lower; // fallback for uncommon architectures
        }
    }

    /**
     * Maps OS names to simplified identifiers (windows, macos, linux).
     *
     * @param raw the OS name from system properties
     * @return normalized OS identifier
     */
    private String simplifyOS(String raw) {
        if (raw == null) return "unknown";

        String lower = raw.toLowerCase();

        if (lower.startsWith("win"))        return "windows";
        else if (lower.startsWith("mac"))   return "macos";
        else if (lower.startsWith("linux")) return "linux";
        else                                return lower; // fallback
    }

    /**
     * @return simplified OS name (windows, macos, linux, …)
     */
    public String getOs() {
        return os;
    }

    /**
     * @return normalized CPU architecture (x86, x86_64, arm64, …)
     */
    public String getOsArch() {
        return osArch;
    }

    /**
     * @return raw OS version string
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * @return data model (typically 32 or 64)
     */
    public String getDataModel() {
        return dataModel;
    }

    /**
     * Returns a formatted summary of the machine information.
     */
    @Override
    public String toString() {
        return String.format("OS=%s %s, ARCH=%s, Model=%s-bit",
                os, osVersion, osArch, dataModel);
    }
}