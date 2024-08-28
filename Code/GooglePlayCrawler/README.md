# Google Play Crawler and CT Checker
This folder contains the Java source code for the tool we used in Section 7.1.1 for scanning Google Play for apps that use the AAB distribution format and apps that use Code Transparency. 
Since no official list of apps on Google Play or API for downloading APKs exists, we use the AndroZoo dataset as an index and for obtaining APKs.
All apps are checked for whether they were compiled from an Android Application Bundle, and whether they contain a Code Transparency JWT file. For speeding up this task, only the ZIP Central Directory of each APK is actually retrieved from the AndroZoo repository server.

# Prerequisites:
* Working installation of JDK 17 or later
* IntelliJ IDEA (e.g. the free community edition)
* Downloaded and unpacked AndroZoo index file from https://androzoo.uni.lu/static/lists/latest.csv.gz

# Steps to run
## A. Assemble list of apps
1. Open this gradle project in IntelliJ IDEA
2. Open the AndroZooPackageNameCrawler.java file
3. Right-click the green triangle next to "public class AndroZooPackageNameCrawler" in line 10
4. Click on "Modify Run Configuration..."
5. Enter the full path of the unpacked (csv) AndroZoo index file as the first argument in the Program arguments text field
6. Enter your desired output path (where you wish results to be placed) as the second argument in the Program arguments text field
7. Click on Apply -> OK
8. Launch the run configuration through the green triangle in the top toolbar

The result of this process is a JSON file that contains the package names for all Google Play apps in the AndroZoo dataset.

## B. Retrieve app metadata from Google Play
1. Open this gradle project in IntelliJ IDEA
2. Open the AppDetailDownloader.java file
3. Right-click the green triangle next to "public class AppDetailDownloader" in line 12
4. Click on "Modify Run Configuration..."
5. Enter the output path of the AndroZooPackageNameCrawler as the first argument in the Program arguments text field
6. Enter your desired output path (where you wish results to be placed) as the second argument in the Program arguments text field
7. Click on Apply -> OK
8. Launch the run configuration through the green triangle in the top toolbar
9. Expect this to run a few hours

The result of this process is a set of JSON files that contain metadata for all Google Play apps in the AndroZoo dataset.

## C. Check Code Transparency of apps
1. Open the CodeTransparencyChecker.java file
2. Set the ANDROZOO_INDEX_PATH static field to point to your AndroZoo index file (latest.csv)
3. Right-click the green triangle next to "public class CodeTransparencyChecker" in line 11
4. Click on "Modify Run Configuration..."
5. Set the input and output paths in the Program arguments text field
   Make sure the input path matches the output path of the AppDetailDownloader run
6. Click on Apply -> OK
7. Launch the run configuration through the green triangle in the top toolbar
8. Expect this to run a few days

The result of this process is a set of JSON files that contain AAB and CT status for all apps on Google Play.
Every file contains a JSON object whose keys are package names for applications. The value for each key is the AAB and CT status.
1 indicates that the app was compiled from an AAB, while 3 indicates that it additionally uses CT.