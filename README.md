# StudioBridge

With StudioBridge it is possible to make Only LAN 3D printers from Bambu Lab visible in Bambu Studio.
Unfortunately, Bambu Studio does not offer the possibility to store printers from the network directly, which means that they cannot be used via Bambu Studio from other subnets or from accessible external networks (e.g. via VPN). This is because Bambu Lab adds its printers for Bambu Studio in the network via mDNS/SSDP. However, these services cannot be used across networks as they are not routed.
There are many scripts for all kinds of systems, but many users feel more comfortable with a GUI. Therefore I developed StudioBridge, which is basically based on the scripts i found in countless forums.

# How it works
StudioBridge sends a UDP request to Bambu Studio with the printer data (IP address, serial number, printer type, printer name).
Once all the data has been entered, it can be sent to BambuStudio, making the printer visible. Now the printer can be accessed via the LAN access code in BambuStudio. This means that all the usual functions are available.

With StudioBridge all printers of the Bambulab assortment can be added to Bambu Studio. It is also possible to create and save a profile for each printer.

It should work on all platforms with Java.
Successfully tested on:
- Windows 10/11
- Linux Ubuntu 24.04 LTS
- MacOS 14.7

# Requirements
- min. Java 1.8

# Preview
Lightmode

![image](https://github.com/user-attachments/assets/c7e1ac4c-47da-4d14-9214-1cc2d14e23a0) ![image](https://github.com/user-attachments/assets/02de45f4-6833-4ba3-9153-ecb579104f77)

Darkmode

![image](https://github.com/user-attachments/assets/215f3f50-676c-4d53-805c-7010c3879ecd) ![image](https://github.com/user-attachments/assets/a958b601-67c7-4de7-bbbf-c0678b7c1930)


# Demo

![StudioBridgeDemo](https://github.com/user-attachments/assets/e5e197ab-54bf-4a6d-bc40-dd46607597f8)
