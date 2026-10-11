-----------------------------------------------------------------------------------------------
Version 2.1.4
   - Breaking:
      - "Send packages directly" and --direct now do what they say. Since 2.0.0 the setting was inverted: with the checkbox ticked the package went out as a broadcast to 255.255.255.255, and with the default setting it was sent to localhost. Now the checkbox and --direct send to Bambu Studio on this computer, and the default is the broadcast to the network. With the default setting StudioBridge therefore switches from localhost to broadcast. If your printer no longer shows up, tick "Send packages directly" in the settings or start the CLI with --direct (PR #41)
   - New Features:
      - The profile list only holds your profiles. "New profile", "Import profile" and the "---" separator were entries of the list, so picking one by accident opened a file dialog or jumped back. They moved into a menu button next to the list, together with renaming and deleting a profile, each with its own icon; saving stays a button of its own. The profile icons are sharp on high resolution screens now. With no profile selected, the list shows "New profile" as placeholder (PR #54)
      - Importing a profile and choosing the profiles folder use the file dialog of the operating system on macOS and Linux too (the Finder dialog on macOS, the GTK dialog on Linux). Until now only Windows had its native dialog, the others got the Java one. The import dialog only offers .sbp files. The macOS app now declares its languages, so these system dialogs appear in German on a German Mac instead of in English (PR #54)
      - The GUI speaks German. Pick the language under Settings → Language: "Automatic (system language)", the default, follows the operating system and falls back to English, or choose English or Deutsch. The new language is used after a restart. The command line output stays English (PR #52)
      - --broadcast sends the UDP package as broadcast for this run, also when "Send packages directly" is ticked in the settings. Together with --direct it can not be combined (PR #50)
      - --version prints the version of StudioBridge (PR #50)
      - --sendonly ends with exit code 1 when a package could not be sent, Bambu Studio is not running or no profile was found, and with 0 when every package was sent, so a script or an autostart can tell whether it worked (PR #50)
   - Fixes:
      - Closing StudioBridge asks again before entered data is lost. The check compared the saved profile with itself, so a new profile that was filled in but never saved, or changes to a saved profile, were dropped without a question. Now a new profile with data offers to save it, and an edited profile offers to save the changes (PR #53)
      - --direct, --broadcast and --customProfilesDir only apply to the run they are given for. Until now they were written into the settings when the GUI was closed, so a single start with --direct switched the sending mode for good (PR #50)
      - --selectedPrinters without --sendonly is reported as error instead of being ignored (PR #50)
      - Profiles sent with --sendonly are read as UTF-8 like in the GUI (PR #50)
      - The send button sends the data currently entered. Until now it sent the last saved state of the profile, so a changed IP address was ignored after "Continue without saving", and declining to save a new profile sent an empty printer (PR #40)
      - Closing the "Save profile" dialog with the window button no longer crashes StudioBridge, and a new profile is no longer saved twice (PR #40)
      - Renaming a profile to its own name no longer deletes it. The old profile file is only removed once the profile has been saved under its new name, and renaming onto another existing profile asks before overwriting it (PR #44)
      - Profile names that can not be used as a file name are refused with a message: empty names, the reserved entries of the profile list, names starting with a dot and names containing \ / : * ? " < > | (PR #44)
      - Saving and importing a profile only add it to the profile list after the file was written (PR #44)
      - Printer names with umlauts and other non-ASCII characters arrive unchanged in Bambu Studio. Profiles, settings and the UDP package are read and written as UTF-8 (PR #44)
      - Pressing send again for the same printer replaces the running broadcast instead of starting a second one next to it (PR #44)
      - The update check compares versions part by part, so 2.1.10 counts as newer than 2.1.9. A release name without a version number no longer crashes the check, and the check gives up after a few seconds when GitHub can not be reached (PR #42)
      - --customProfilesDir and --selectedPrinters without a value are reported as unknown argument instead of crashing. Values may contain "=" and surrounding quotes, as shown in --help (PR #43)
   - Maintenance:
      - The version shown in StudioBridge is taken from pom.xml, so a release only bumps it in one place (PR #42)
      - Typos in dialogs and CLI output fixed, outdated code documentation corrected, unused files removed (PR #45)

-----------------------------------------------------------------------------------------------
Version 2.1.3
   - Fixes:
      - The profiles path is kept in sync with the migrated data directory. Thanks to @Sugarsons (PR #35)
   - Maintenance:
      - GitHub Actions bumped to v5 and the artifact actions to v7 for the Node 24 runtime (PR #36, PR #37)

-----------------------------------------------------------------------------------------------
Version 2.1.2
   - Breaking:
      - First release with a native Windows ARM64 installer. Windows can not upgrade an x86 installation to ARM64: uninstall the old x86 version under Settings → Apps once, then install StudioBridge-2.1.2_win_arm64.msi. Later ARM64 updates upgrade automatically
   - New Features:
      - Support for the Bambu Lab A2L (PR #34)
   - Maintenance:
      - Full cross-platform build and release workflow for Windows (x86, ARM64), Linux (x86_64, aarch64) and macOS (PR #28, PR #29, PR #30, PR #31)
      - macOS Intel (x86) builds are no longer provided (PR #32)
      - Dependencies updated and pom.xml version fixed (PR #27, PR #33)

-----------------------------------------------------------------------------------------------
Version 2.1.1
   - New Features:
      - Support for the Bambu Lab X2D
      - The program configuration moved to a new folder, and the last profile is selected again if it still exists. Thanks to @fast-potat0 (PR #23)
         - If you use the standard profiles path and your profiles are not loaded, open Settings, "Set custom profiles path" and click "Reset to default". After a restart of StudioBridge your profiles are loaded
   - Maintenance:
      - Automatic build of the project. Thanks to @fabian-may (PR #21)

-----------------------------------------------------------------------------------------------
Version 2.1.0
   - New Features:
      - Support for the Bambu Lab H2C. Bambu uses two model numbers for it, O1C and O1C2, so try which one works for your printer
      - The multi printer setup shows the profile name
      - Profile names can be edited
      - A profile imported from the file system is stored in your profiles directory and its name can be edited
      - New CLI arguments --customProfilesDir and --selectedPrinters
   - Maintenance:
      - New printer objects for improved handling
      - Improved parsing of the configuration files
      - Improved exception and crash handling

-----------------------------------------------------------------------------------------------
Version 2.0.0
   - New Features:
      - Native builds tailored to each operating system and CPU architecture
      - Improved graphical user interface
      - Support for the Bambu Lab P2S
      - Help arguments for each operating system
      - CLI only version
   - Maintenance:
      - Revamped project structure

-----------------------------------------------------------------------------------------------
Version 1.1.1
   - New Features:
      - Option to choose how the UDP package is sent
      - Option to check whether the printer is reachable from your computer

-----------------------------------------------------------------------------------------------
Version 1.1.0
   - Breaking:
      - New printer configuration: the printer model is mapped independently of the selection box. Every printer configuration has to be updated accordingly
   - New Features:
      - The UDP package matches the broadcasts of current printers (X1, P1, A1, H2), which Bambu Studio 2.02 and later require because of their stricter SSDP parsing:
         - HOST changed from 255.255.255.255:2021 to 239.255.255.250:1900
         - Server simplified to UPnP/1.0
         - New fields DevVersion.bambu.com (firmware version) and DevCap.bambu.com (capabilities)
      - The package is sent to both ports 2021 and 1990, every 5 seconds like a real printer
   - Fixes:
      - getAvailableUDPPort() returned an occupied port instead of a free one
   - Maintenance:
      - Refactored printer model handling
      - Packet content and broadcast logic separated

-----------------------------------------------------------------------------------------------
Version 1.0.9
   - New Features:
      - Support for the Bambu Lab H2D Pro and H2S
   - Maintenance:
      - Dependencies updated

-----------------------------------------------------------------------------------------------
Version 1.0.8
   - New Features:
      - Support for Bambu Studio 2

-----------------------------------------------------------------------------------------------
Version 1.0.7
   - New Features:
      - Support for the Bambu Lab H2D
   - Fixes:
      - --sendonly sent the wrong printer model

-----------------------------------------------------------------------------------------------
Version 1.0.6
   - New Features:
      - --sendonly argument, which only sends the profiles already set up to Bambu Studio

-----------------------------------------------------------------------------------------------
Version 1.0.5
   - New Features:
      - Multiple printer support: send more than one printer to Bambu Studio or Orca Slicer
      - A standardised light mode design for all operating systems
   - Fixes:
      - The "New profile" and "Import profile" entries of the profile selection could be deleted
      - Improved stability

-----------------------------------------------------------------------------------------------
Version 1.0.4
   - New Features:
      - Improved update process

-----------------------------------------------------------------------------------------------
Version 1.0.3
   - New Features:
      - --noupdate argument, which skips the update check
   - Maintenance:
      - jnafilechooser is included as Maven dependency instead of an external library

-----------------------------------------------------------------------------------------------
Version 1.0.2
   - Fixes:
      - Improved sending of the package to Bambu Studio on macOS

-----------------------------------------------------------------------------------------------
Version 1.0.1
   - New Features:
      - Importing profiles
   - Fixes:
      - Behaviour of the dialogs adjusted, e.g. saving a profile
      - Minor bugfixes

-----------------------------------------------------------------------------------------------
Version 1.0.0
   - New Features:
      - First official version of StudioBridge
      - Custom path for profiles, UNC paths included
      - Update check when StudioBridge starts
      - Option "Remember last used profile"
      - Dialog windows adapted and extended

-----------------------------------------------------------------------------------------------
Version 0.1 BETA
   - New Features:
      - First build of StudioBridge
   - Known issues:
      - Some dialogs are not laid out perfectly
      - Small issues when saving profiles
