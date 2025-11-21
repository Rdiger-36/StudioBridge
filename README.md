# StudioBridge

With StudioBridge it is possible to make Bambu Lab 3D Printers visible in Bambu Studio or Orca Slicer, that can not be automatically added by them.  
It is also possible to conenct printers from other subnets or from accessible external networks (e.g. via VPN) or in your normal home network.
There are many scripts for all kinds of systems, but many users feel more comfortable with a GUI. Therefore I developed StudioBridge, which is basically based on the scripts i found in countless forums.

# How it works
StudioBridge sends a UDP request to your Slicer (Bambu Studio or Orca Slicer) with the printer data (IP address, serial number, printer type, printer name).
Once all the data has been entered, it can be sent to your Slicer, making the printer visible. Now the printer can be accessed via the LAN access code. This means that all the usual functions are available.

This Programm has nothing to do with Bambu Connect and it also doesn't replace it. It's only for the connection in your Slicer.

With StudioBridge all printers of the Bambu Lab assortment can be added. It is also possible to create and save a profile for each printer.

You have the option to send the package via broadcast or directly to Bambu Studio.

You can check if the printer would connect correctly in Bambu Studio with the function "Check Printer".

Successfully tested on:
- Windows 10/11 ARM and x86
- Linux Ubuntu 25.10 ARM and x86
- MacOS Intel and M-Series

# Requirements
- min. Java 21 (only for .jar compiled release)

# Preview
Lightmode

![image](https://github.com/user-attachments/assets/c7e1ac4c-47da-4d14-9214-1cc2d14e23a0) ![image](https://github.com/user-attachments/assets/02de45f4-6833-4ba3-9153-ecb579104f77)

Darkmode

![image](https://github.com/user-attachments/assets/215f3f50-676c-4d53-805c-7010c3879ecd) ![image](https://github.com/user-attachments/assets/a958b601-67c7-4de7-bbbf-c0678b7c1930)

Multiple Printer Setup

![Bildschirmfoto 2025-02-05 um 00 04 00](https://github.com/user-attachments/assets/3a295a9c-2ca2-4d1d-ad1e-8e68909f9f84)
![Bildschirmfoto 2025-02-05 um 00 03 04](https://github.com/user-attachments/assets/62ed0dd8-000d-4682-8399-c5b6229204ed)

Printer Check

![Printer_Check](https://github.com/user-attachments/assets/ccb139b0-e737-49a5-9b8c-0faf00316892)

If TCP Handshake is not working, then it helps to restart your printer.

# Demo

![StudioBridgeDemo](https://github.com/user-attachments/assets/e5e197ab-54bf-4a6d-bc40-dd46607597f8)


# Arguments

To use StudioBridge via Terminal/CMD/PowerShell you have to run the StudioBridgeCLI on MacOS and Windows

#### MacOS
```bash

/Applications/StudioBridge.app/Contents/MacOS/StudioBridgeCLI --help

*** StudioBridge by Rdiger-36 v.2.0.0 ***

Usage:
  /Applications/StudioBridge.app/Contents/MacOS/StudioBridgeCLI [OPTIONS]

Options:
  You can combine every option with each other
  --noupdate   Skip search for latest updates on startup
  --sendonly   Start with no GUI, only send data from all printers to Bambu Studio
  --direct     Send UPD package directly to Bambu Studio, not over broadcast
  --help       Show help for StudioBridge

Example:
  /Applications/StudioBridge.app/Contents/MacOS/StudioBridgeCLI --help
  /Applications/StudioBridge.app/Contents/MacOS/StudioBridgeCLI --sendonly --noupdate --direct
```

#### Windows
You can find the StudioBridgeCLI.exe in your installation directory, standard: "C:\Program Files\StudioBridge"
```bash
.\StudioBridgeCLI.exe --help

*** StudioBridge by Rdiger-36 v.2.0.0 ***

Usage:
  .\StudioBridgeCLI.exe [OPTIONS]

Options:
  You can combine every option with each other
  --noupdate   Skip search for latest updates on startup
  --sendonly   Start with no GUI, only send data from all printers to Bambu Studio
  --direct     Send UPD package directly to Bambu Studio, not over broadcast
  --help       Show help for StudioBridge

Example:
  .\StudioBridgeCLI.exe --help
  .\StudioBridgeCLI.exe --sendonly --noupdate --direct
```

#### Linux
```bash
.\StudioBridge.AppImage --help

*** StudioBridge by Rdiger-36 v.2.0.0 ***

Usage:
  .\StudioBridge.AppImage [OPTIONS]

Options:
  You can combine every option with each other
  --noupdate   Skip search for latest updates on startup
  --sendonly   Start with no GUI, only send data from all printers to Bambu Studio
  --direct     Send UPD package directly to Bambu Studio, not over broadcast
  --help       Show help for StudioBridge

Example:
  .\StudioBridge.AppImage --help
  .\StudioBridge.AppImage --sendonly --noupdate --direct
```

If you use --sendonly the output will look like this:
```bash
*** StudioBridge by Rdiger-36 ***

Found 2 profiles!
Try to send all of them to Bambu Studio

Successfully sended Test Bambu X1E - 192.168.XXX.XXX - X1E to Bambu Studio
Successfully sended Bambu Lab P1S - 192.168.XXX.XXX - P1S to Bambu Studio
All Packages successfully sent to Bambu Studio
```
# Installing Java and Running the JAR File (Only for using .jar!)

StudioBridge.jar is built in Java, so a Java Runtime Environment (JRE) is required to run the program. Below are simple instructions for installing Java on Windows, macOS, and Linux, as well as for launching the StudioBridge GUI.

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

# Support Me
[![Buy Me a Coffee](https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png)](https://www.buymeacoffee.com/Rdiger36)
