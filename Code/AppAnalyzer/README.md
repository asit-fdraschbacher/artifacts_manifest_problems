# App Analyzer
This folder contains the Java source code for the tool we used in Sections 7.1.4 and 7.1.5 for evaluating the susceptibility of an app to attacks A4 and A5. It scans the APK file for files in the assets or resources that carry the file signature or extension of the ELF, DEX, APK, or DLL formats (A4). Additionally, all files in these folders that contain only printable characters are ran through the Esprima JavaScript syntax validator. Lastly, our tool checks the app’s DEX files for the presence of the FileProvider class of the Jetpack Core library (A5).

# Prerequisites:
* Working JDK installation and JRE that includes a JavaScript engine (we used Java 11)
* IntelliJ IDEA (e.g. the free community edition)

# Steps to run
1. Open this gradle project in IntelliJ IDEA
2. Open the Main.java file
3. Right-click the green triangle next to "public class Main" in line 200
4. Click on "Modify Run Configuration..."
5. Enter the input and output paths as the first and second argument in the Program arguments text field
   The input path must point to a valid AppSet folder structure (see AppSet_MostPopularGooglePlay for an example).
   Inside the root folder of the AppSet, a set of category folders contains one folder per application, which contains the APKs for that app. 
6. Click on Apply -> OK
7. Launch the run configuration through the green triangle in the top toolbar

The result of this process is a CSV file that contains these fields for each app:
* packageName: The package name of the app
* category: The app category as stored in the app set
* aab: Whether or not the app was compiled from an AAB
* usesFileProvider: The app's manifest exposes a FileProvider
* hasFileProvider: The app contains the FileProvider class in its DEX code
* nsc: The app contains a network configuration xml file referenced in its manifest
* ct: The APK contains a Code Transparency JWT file
* dexFiles: A list of dex files found in non-standard locations in the APK
* elfFiles: A list of elf files found in non-standard locations in the APK
* apkFiles: A list of APK files found in the APK
* jsFiles: A list of Javascript files found in the APK
* dllFiles: A list of DLL files found in the APK (used by apps written in C# or similar)
* htmlFiles: A list of html files found in the APK