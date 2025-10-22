package rdiger36.StudioBridge.objects;

public class localMachine {
    private final String os;
    private final String osArch;
    private final String osVersion;
    private final String dataModel;

    public localMachine() {
        this.os	       = simplifyOS(System.getProperty("os.name"));
        this.osArch    = normalizeArch(System.getProperty("os.arch"));
        this.osVersion = System.getProperty("os.version");
        this.dataModel = System.getProperty("sun.arch.data.model", "?");
    }

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
                return lower; // fallback
        }
    }
    
    private String simplifyOS(String raw) {
        if (raw == null) return "unknown";
        
		String lower = raw.toLowerCase();
		if (lower.startsWith("win"))       return "windows";
		else if (lower.startsWith("mac"))  return "macos";
		else if (lower.startsWith("linux"))return "linux";
		else                               return lower; // fallback
    }

    public String getOs() {
        return os;
    }

    public String getOsArch() {
        return osArch;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getDataModel() {
        return dataModel;
    }

    @Override
    public String toString() {
        return String.format("OS=%s %s, ARCH=%s, Model=%s-bit",
                os, osVersion, osArch, dataModel);
    }
}