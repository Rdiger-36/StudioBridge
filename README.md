With StudioBridge it is possible to make Only LAN 3D printers from BambuLab visible in Bambu Studio.
Unfortunately, Bambu Studio does not offer the possibility to store printers from the network directly, which means that they cannot be used via Bambu Studio from other subnets or from accessible external networks (e.g. via VPN). This is because Bambulab adds its printers for Bambu Studio in the network via mDSN or SSDP. However, these services cannot be used across networks as they are not routed.
There are many scripts for all kinds of systems, but many users feel more comfortable with a GUI. Therefore I developed StudioBridge, which is basically based on the countless scripts.

StudioBridge sends a UDP request to Bambu Studio with the printer data (IP address, serial number, printer type, printer name).
Once all the data has been entered, it can be sent to BambuStudio, making the printer visible. Now the printer can be accessed via the LAN access code in BambuStudio. This means that all the usual functions are available.

With StudioBridge all printers of the Bambulab assortment can be added to Bambu Studio. It is also possible to create and save a profile for each printer.
