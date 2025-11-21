package rdiger36.StudioBridge.objects;

/**
 * Represents a printer profile containing the printer's identifying information
 * such as profile name, printer name, IP address, serial number and model.
 *
 * <p>This class is used throughout the application to store profile data and
 * synchronize UI fields with configuration files.</p>
 */
public class Printer {

    // Basic profile properties
    private String profileName;
    private String printerName;
    private String ipAdress;
    private String serialNumber;
    private String printerModel;

    /**
     * Creates a new {@code Printer} instance with the provided profile data.
     *
     * @param profileName   the name of the printer profile
     * @param printerName   the human-readable name of the printer
     * @param ipAdress      the printer's network IP address
     * @param serialNumber  the unique printer serial number
     * @param printerModel  the printer model identifier
     */
    public Printer(String profileName, String printerName,
                   String ipAdress, String serialNumber, String printerModel) {

        this.profileName = profileName;
        this.printerName = printerName;
        this.ipAdress = ipAdress;
        this.serialNumber = serialNumber;
        this.printerModel = printerModel;
    }

    // ------------------------ Getter / Setter ------------------------

    /** @return the name of the printer profile */
    public String getProfileName() { return profileName; }

    /** Sets the profile name. */
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    /** @return the human-readable name of the printer */
    public String getPrinterName() { return printerName; }

    /** Sets the printer's display name. */
    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    /** @return the printer's IP address */
    public String getIpAdress() { return ipAdress; }

    /** Sets the printer's IP address. */
    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    /** @return the printer's serial number */
    public String getSerialNumber() { return serialNumber; }

    /** Sets the printer's serial number. */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /** @return the model identifier of the printer */
    public String getPrinterModel() { return printerModel; }

    /** Sets the model identifier of the printer. */
    public void setPrinterModel(String printerModel) {
        this.printerModel = printerModel;
    }

    /**
     * Returns a formatted string representing the printer profile.
     */
    @Override
    public String toString() {
        return String.format("Profile=%s, Name=%s, IP-Address=%s, Serialnumber=%s, Model=%s",
                profileName, printerName, ipAdress, serialNumber, printerModel);
    }
}