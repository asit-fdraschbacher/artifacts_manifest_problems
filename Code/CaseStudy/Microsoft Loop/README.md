# Microsoft Loop Case Study

This folder contains all the code (and binaries) needed for reproducing the Microsoft Loop case study in paper Section 7.2.1. As documented in the paper, the attack injects a dependency on a static library into the manifest of the Microsoft Loop app. As a result, the classes of the static library are added to the app's class path at app launch, which effectively grants the attacker code execution in the context of the victim app. The static library contains a class named identical to the Microsoft Loop app's main activity. When the manipulated Loop app is launched, the class from the static library takes precedence over the app's original main activity. Our fake main activity displays a simply login screen that illustrates the potential to use this attack to extract login credentials.

## Prerequisites
* Install bundletool and make sure it is in your PATH, e.g. by following the instructions from https://stackoverflow.com/a/67332078

## The experiment

1. Check the Code Transparency of the original APKs in the Original_APKs folder:
   
    `../../Scripts/check_ct.sh Original_APKs/base.apk`

Output:

    ...
    Code transparency signature is valid. SHA-256 fingerprint of the code transparency key certificate (must be compared with the developer's public key manually): 52 D4 B1 8E CA 3F 78 62 FF 6D D9 C8 7B 10 DF CD 6A 3F 2A 85 36 15 3D D2 52 08 89 54 40 B7 C2 02
    ...

2. Check the Code Transparency of the patched APKs in the Patched_APKs folder:
   
    `../../Scripts/check_ct.sh Patched_APKs/com.microsoft.loop/base.apk-signed.apk`

Output:

    ...
    Code transparency signature is valid. SHA-256 fingerprint of the code transparency key certificate (must be compared with the developer's public key manually): 52 D4 B1 8E CA 3F 78 62 FF 6D D9 C8 7B 10 DF CD 6A 3F 2A 85 36 15 3D D2 52 08 89 54 40 B7 C2 02
    ...

**Please Note:** If you get an exception that says: "Invalid CEN header (invalid zip64 extra data field size)", please make sure you use a JRE that includes a fix for https://bugs.openjdk.org/browse/JDK-8313765 !

3. Install the static library on a development-enabled Android device connected via USB (we used a Google Pixel 3 running Android 12):
   
   `adb install static_library.apk`

4. Install the patched APKs for the Microsoft Loop app:
   
    `adb install-multiple Patched_APKs/*.apk`

5. Launch the Microsoft Loop app on the Android device

6. You are greeted with the fake Login screen from the static library (see Fake_Login_Screen.png). Despite the attacker having code execution in the context of the Microsoft Loop app, the CT still validates as if the app was untampered.

**Please Note:** We do not have access to the private key for the APK’s app signing certificate, so we cannot entirely accurately simulate a supply chain attacker. In a real-world attack, the manipulated APK would be signed with the developer’s correct app signing certificate. As explained in paper Section 3, the AAB scheme requires the developer to share the app signing key with the distributor.

## Building the individual artifacts

### Prerequisites

* Download and extract the A2P2 Patching Pipeline distribution from https://extgit.iaik.tugraz.at/fdraschbacher/a2p2. The most recent distribution bundle as of this writing is https://extgit.iaik.tugraz.at/fdraschbacher/a2p2/-/blob/main/a2p2_distribution_v1.0.1.zip?ref_type=heads.
* IntelliJ IDEA (Community Edition)

### Building the A2P2 stage

1. Open the A2P2_Stage_Sources project in IntelliJ IDEA (Community Edition)
2. Ensure the A2P2 path in build.gradle points to the extracted A2P2 distribution on your system
3. In the gradle pane, double-click on JAR to build the JAR file
4. The resulting JAR is located at `build/libs/stages.jar` 

### Building the Static Library

1. Open the Static_Library_Sources folder in Android Studio
2. Build like you would any other Android project

### Patching the Microsoft Loop app

1. In the A2P2 distribution folder, create a folder named "stages"

2. Copy the A2P2 stage JAR file ("a2p2_stage_inject_static_library.jar") to that folder

3. Run A2P2:
   
   ```bash
   mkdir output 
   java -jar ~/SDKs/A2P2/a2p2.jar {./Original_APKs} ! unpack ! injectstaticlibrary com.loop.patch.library 1 3D2225686339F019C49C8111333ECBF7B877A158E17BD439B3E899AB42F6DBCF ! pack ! sign ! ./output
   ```

4. The patched APKs will be located in `output/com.microsoft.loop/`
