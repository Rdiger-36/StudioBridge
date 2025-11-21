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

![image](https://github.com/user-attachments/assets/d7f51a53-35d4-4e5d-b77d-9bf5a77eaf36) ![image](https://github.com/user-attachments/assets/8a1b0062-bc4d-4b25-8247-d3349b8b6b74)


Darkmode

![image](https://github.com/user-attachments/assets/216cb8b1-8913-4d71-87af-b4356de9c614) ![image](https://github.com/user-attachments/assets/a41a6a8b-a3ae-4baf-92a9-1069df042c4c)

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

*** StudioBridge by Rdiger-36 v.2.1.0 ***

Usage:
  /Applications/StudioBridge.app/Contents/MacOS/StudioBridgeCLI [OPTIONS]

Options:
  You can combine every option with each other

Starting options:
  --noupdate             Skip search for latest updates on startup
  --sendonly             Start with no GUI, only send data from all printers to Bambu Studio
  --direct               Send UPD package directly to Bambu Studio, not over broadcast
  --help                 Show help for StudioBridge

Profile options:
  --customProfilesDir='/path/to/profiles/'
      Set custom profiles directory which will be loaded from this for StudioBridgeCLI
  --selectedPrinters='/path/to/profile.sbp/','/path/to/other/profile2.sbp/'
      Set single profiles which will be used for StudioBridgeCLI (single and multi profiles comma-separated)

Example:
  /Applications/StudioBridge.app/Contents/MacOS/StudioBridgeCLI --help
  /Applications/StudioBridge.app/Contents/MacOS/StudioBridgeCLI --sendonly --noupdate --direct
  /Applications/StudioBridge.app/Contents/MacOS/StudioBridgeCLI --sendonly --customProfilesDir='/Users/rdiger-36/Documents/StudioBridgeProfiles'
  /Applications/StudioBridge.app/Contents/MacOS/StudioBridgeCLI --sendonly --selectedPrinters='/Users/rdiger-36/Documents/My P1S.sbp','/Users/rdiger-36/Desktop/Profiles/H2D-Pro Work.sbp.sbp'
```

#### Windows
You can find the StudioBridgeCLI.exe in your installation directory, standard: "C:\Program Files\StudioBridge"
```bash
.\StudioBridgeCLI.exe --help

*** StudioBridge by Rdiger-36 v.2.1.0 ***

You are currently running this application in raw mode (via .jar)!
For the official version for your OS and CPU architecture, please visit the GitHub repository.
https://github.com/Rdiger-36/StudioBridge/

Usage:
  ./StudioBridgeCLI.exe [OPTIONS]

Options:
  You can combine every option with each other

Starting options:
  --noupdate             Skip search for latest updates on startup
  --sendonly             Start with no GUI, only send data from all printers to Bambu Studio
  --direct               Send UPD package directly to Bambu Studio, not over broadcast
  --help                 Show help for StudioBridge

Profile options:
  --customProfilesDir='/path/to/profiles/'
      Set custom profiles directory which will be loaded from this for StudioBridgeCLI
  --selectedPrinters='/path/to/profile.sbp/','/path/to/other/profile2.sbp/'
      Set single profiles which will be used for StudioBridgeCLI (single and multi profiles comma-separated)

Example:
  ./StudioBridgeCLI.exe --help
  ./StudioBridgeCLI.exe --sendonly --noupdate --direct
  ./StudioBridgeCLI.exe --sendonly --customProfilesDir='C:\Users\rdiger-36\Documents\StudioBridgeProfiles'
  ./StudioBridgeCLI.exe --sendonly --selectedPrinters='C:\Users\rdiger-36\Documents\My P1S.sbp','C:\Users\rdiger-36\Desktop\H2D-Pro Work.sbp'
```

#### Linux
```bash
.\StudioBridge.AppImage --help

*** StudioBridge by Rdiger-36 v.2.1.0 ***

You are currently running this application in raw mode (via .jar)!
For the official version for your OS and CPU architecture, please visit the GitHub repository.
https://github.com/Rdiger-36/StudioBridge/

Usage:
  ./StudioBridge.AppImage [OPTIONS]

Options:
  You can combine every option with each other

Starting options:
  --noupdate             Skip search for latest updates on startup
  --sendonly             Start with no GUI, only send data from all printers to Bambu Studio
  --direct               Send UPD package directly to Bambu Studio, not over broadcast
  --help                 Show help for StudioBridge

Profile options:
  --customProfilesDir='/path/to/profiles/'
      Set custom profiles directory which will be loaded from this for StudioBridgeCLI
  --selectedPrinters='/path/to/profile.sbp/','/path/to/other/profile2.sbp/'
      Set single profiles which will be used for StudioBridgeCLI (single and multi profiles comma-separated)

Example:
  ./StudioBridge.AppImage --help
  ./StudioBridge.AppImage --sendonly --noupdate --direct
  ./StudioBridge.AppImage --sendonly --customProfilesDir='/home/rdiger-36/Documents/StudioBridgeProfiles'
  ./StudioBridge.AppImage --sendonly --selectedPrinters='/home/rdiger-36/Documents/My P1S.sbp','/home/rdiger-36/Desktop/H2D-Pro Work.sbp'
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

## FAQ
Q: My App won't start on macOS. There is a Error Message that says that this app courrupted and has to be deleted.

A: This is asaftey feature from macOS Gatekeeper. To disable this Error Message just run this command in your Terminal:

`xattr -dr com.apple.quarantine /Applications/StudioBridge.app`

# Support Me
[![Buy Me a Coffee](https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png)](https://www.buymeacoffee.com/Rdiger36)
