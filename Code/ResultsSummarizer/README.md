# Results Summarizer
This folder contains the Java source code for the tool we used in Sections 7.1.4 and 7.1.5 for summarizing the results
of the CT and AAB analyses carried out in GooglePlayCrawler and HuaweiAppGalleryCrawler.

# Prerequisites:
* Working JDK installation
* IntelliJ IDEA (e.g. the free community edition)

# Steps to run
1. Open this gradle project in IntelliJ IDEA
2. Open the ResultsSummarizer.java file
3. Right-click the green triangle next to "public class ResultsSummarizer" in line 3
4. Click on "Modify Run Configuration..."
5. Enter the input path in the Program arguments text field
   The input path must point to the output folder of the CodeTransparencyChecker in GooglePlayCrawler or HuaweiAppGalleryCrawler.
6. Click on Apply -> OK
7. Launch the run configuration through the green triangle in the top toolbar

The program prints a summary of the results to stdout. The summary includes a total of apps that use AAB and of apps that use CT.