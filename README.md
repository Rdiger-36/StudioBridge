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
- Linux Ubuntu 24.04 LTS/24.10
- MacOS 14.7/15.1

# Skip update check on startup
To skip the update process, you can simply add the "--noupdate" argument
```bash
  java -jar ./StudioBridge.jar --noupdate
```

# Requirements
- min. Java 1.8

# Preview
Lightmode

![image](https://github.com/user-attachments/assets/c7e1ac4c-47da-4d14-9214-1cc2d14e23a0) ![image](https://github.com/user-attachments/assets/02de45f4-6833-4ba3-9153-ecb579104f77)

Darkmode

![image](https://github.com/user-attachments/assets/215f3f50-676c-4d53-805c-7010c3879ecd) ![image](https://github.com/user-attachments/assets/a958b601-67c7-4de7-bbbf-c0678b7c1930)


# Demo

![StudioBridgeDemo](https://github.com/user-attachments/assets/e5e197ab-54bf-4a6d-bc40-dd46607597f8)

# Installing Java and Running the JAR File

StudioBridge is built in Java, so a Java Runtime Environment (JRE) is required to run the program. Below are simple instructions for installing Java on Windows, macOS, and Linux, as well as for launching the StudioBridge GUI.

Java Installation

Windows:

	1.	Visit the official Java website and download the latest Java Runtime Environment (JRE) for Windows.
	2.	Run the downloaded installer file and follow the on-screen instructions to complete the installation.
	3.	To confirm that Java is installed, you can search for “Java” in the Start menu. You should see options like “Java Control Panel” if installation was successful.

macOS:

	1.	Visit the official Java website and download the latest Java Runtime Environment (JRE) for macOS.
	2.	Open the downloaded .dmg file and follow the instructions to install Java by dragging it into the Applications folder.
	3.	After installation, open “System Preferences,” then “Java” to verify Java is installed and configured correctly.

Linux:

	1.	Most Linux distributions offer Java in their software repositories. Open the Software Center or Package Manager for your distribution and search for “OpenJDK” or “Java Runtime Environment.”
	2.	Select the version you need (Java 8 or later) and install it.
	3.	You can check if Java is installed by searching for it in the applications menu or checking for “Java” in system settings.

Running the StudioBridge GUI

	1.	Download the StudioBridge.jar file from the GitHub releases page.
	2.	Locate the StudioBridge.jar file in your Downloads or specified folder.
	3.	Double-click the .jar file to launch StudioBridge. This should open the GUI, allowing you to configure and use your 3D printers with Bambu Studio over LAN.

If the .jar file does not open directly, ensure Java is properly installed, and check your system’s default settings for opening .jar files.
