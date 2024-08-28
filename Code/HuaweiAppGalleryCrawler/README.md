# Huawei App Gallery Crawler and CT Checker
This folder contains the Java source code for the tool we used in Section 7.1.1 for scanning Huawei AppGallery for apps that use the AAB distribution format and apps that use Code Transparency. 
Since no official list of apps on Huawei AppGallery exists, it first brute-forces app identifiers through Huawei's Rest API to collect a list of known package names.
Next, all these apps are checked for whether they were compiled from an Android Application Bundle, and whether they contain a Code Transparency JWT file. The tool only obtains the ZIP Central Directory of each APK from Huawei's servers to speed up the analysis. 

# Prerequisites:
* Working JDK installation
* IntelliJ IDEA (e.g. the free community edition)

# Steps to run
## A. Brute-force application identifiers
1. Open this gradle project in IntelliJ IDEA
2. Open the PackageNameCrawler.java file
2b. Adapt id ranges (see below)
3. Right-click the green triangle next to "public class PackageNameCrawler" in line 12
4. Click on "Modify Run Configuration..."
5. Enter the output path (where you wish results to be placed) in the Program arguments text field
6. Click on Apply -> OK
7. Launch the run configuration through the green triangle in the top toolbar

The result of this process is a set of JSON files that contain metadata of all apps on Huawei AppGallery.
Please note we ran the brute-forcing multiple times, covering different identifier ranges (see below).

## B. Check Code Transparency of apps
1. Open the CodeTransparencyChecker.java file
2. Right-click the green triangle next to "public class CodeTransparencyChecker" in line 11
3. Click on "Modify Run Configuration..."
4. Set the input and output paths in the Program arguments text field
   The last argument is the output paths
   The first n-1 arguments are the input paths
   Make sure each input path matches the output path of a PackageNameCrawler runs
   Multiple PackageNameCrawler runs are needed to cover multiple valid app identifier ranges (see below).
5. Click on Apply -> OK
6. Launch the run configuration through the green triangle in the top toolbar

The result of this process is a set of JSON files that contain AAB and CT status for all apps on Huawei AppGallery.
Every file contains a JSON object whose keys are package names for applications. The value for each key is the AAB and CT status.
1 indicates that the app was compiled from an AAB, while 3 indicates that it additionally uses CT.

# A note on Huawei AppGallery app identifiers
App identifiers start with letter C, followed by a number that is either 4-6 or 9 digits long.
Not all numbers following this pattern are valid app identifiers.
It seems there are multiple different ranges of valid identifiers within this total space of identifiers.
For our evaluation, we identified these ranges through trial-and-error, and ran the PackageNameCrawler once for each range, 
by adapting the idRange and idOffset static fields. We then fed all the results folders into CodeTransparencyChecker.

The different ranges we identified are (broadly):
Low: C2955-C100940
Most: C100308949-C109981511
High: C110027587-C111000491

