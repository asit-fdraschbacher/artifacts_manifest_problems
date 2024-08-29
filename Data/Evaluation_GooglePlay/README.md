# Evaluation_GooglePlay

Contains the results and intermediary files of our large-scale analysis of apps on Google Play. The files were generated using the code in GooglePlayCrawler.

## packageNames.json.zip
The result of AndroZooPackageNameCrawler.java, i.e. contains all package names of Google Play apps in the AndroZoo dataset. Compressed to fit within GitHub's 100 MB file size limit.

## CodeTransparency folder
Contains the CT and AAB status of all apps analysed here. 3 indicates that AAB and CT was used, while 1 indicates that only AAB was used. Generated using CodeTransparencyChecker.java

## Details folder
App metadata as obtained from Google Play. Generated using AppDetailDownloader.java