import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {
    // We want to check:
    /*
    * JS code
    * ELF file
    * DEX file
    * APK file
    * File Provider file
    * NSC file
    * DLL file
    * HTML file
    */

    private static class AppRecord {
        String packageName;
        String category;
        boolean aab = false;
        boolean usesFileProvider = false;
        boolean hasFileProvider = false;
        boolean nsc = false;
        boolean ct = false;

        List<String> dexFiles = new ArrayList<>();
        List<String> elfFiles = new ArrayList<>();
        List<String> apkFiles = new ArrayList<>();
        List<String> jsFiles = new ArrayList<>();
        List<String> dllFiles = new ArrayList<>();
        List<String> htmlFiles = new ArrayList<>();

        public String toCsv(String separator) {
            return Utils.quote(packageName) + separator + Utils.quote(category) + separator + aab + separator + usesFileProvider + separator + hasFileProvider + separator +
                    nsc + separator + ct + separator + Utils.listToString(dexFiles) + separator + Utils.listToString(elfFiles)
                    + separator + Utils.listToString(apkFiles) + separator + Utils.listToString(jsFiles) + separator + Utils.listToString(dllFiles) +
                    separator + Utils.listToString(htmlFiles);

        }

        public static String csvHeader(String separator) {
            return "packageName" + separator + "category" + separator + "aab" + separator + "usesFileProvider" + separator + "hasFileProvider" + separator + "nsc" + separator + "ct" + separator +
                    "dexFiles" + separator + "elfFiles" + separator + "apkFiles" + separator + "jsFiles" + separator + "dllFiles" + separator +
                    "htmlFiles";
        }
    }


    private static FileFilter regularFoldersFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() && !file.getName().startsWith(".");
        }
    };

    private static FileFilter apkFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return !file.isDirectory() && file.getName().toLowerCase().endsWith("apk");
        }
    };

    private static File getBaseApkFile(File[] apkFiles) {
        int shortestNameLength = Integer.MAX_VALUE;
        File shortestNameFile = null;

        for (File file : apkFiles) {
            if (file.getName().length() < shortestNameLength) {
                shortestNameLength = file.getName().length();
                shortestNameFile = file;
            }
        }
        
        return shortestNameFile;
    }

    private static boolean isRootPath(String path) {
        return !path.contains("/");
    }

    private static boolean isLibPath(String path) {
        return path.startsWith("lib/");
    }

    private static AppRecord checkApk(File apk, AppRecord record) {
        try {
            ZipFile zipFile = new ZipFile(apk);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.getName().length() == 0) continue;
                if (zipEntry.isDirectory()) continue;

                if (zipEntry.getName().endsWith("code_transparency_signed.jwt")) {
                    record.ct = true;
                    System.out.println("Has CT!");
                }

                if (zipEntry.getName().endsWith("BNDLTOOL.SF") || zipEntry.getName().endsWith("stamp-cert-sha256")) {
                    record.aab = true;
                    System.out.println("Generated from AAB!");
                }

                if (zipEntry.getName().equals("AndroidManifest.xml")) {
                    if (Utils.manifestContainsFileProvider(new ResettableZipEntryInputStream(zipFile, zipEntry))) {
                        record.usesFileProvider = true;
                        System.out.println("Uses file provider");
                    }
                    if (Utils.manifestContainsNetworkSecurityConfig(new ResettableZipEntryInputStream(zipFile, zipEntry))) {
                        record.nsc = true;
                        System.out.println("Found nsc");
                    }
                }

                if (Utils.isDexData(new ResettableZipEntryInputStream(zipFile, zipEntry))) {
                    if (!isRootPath(zipEntry.getName()) || (!zipEntry.getName().endsWith(".dex") || !zipEntry.getName().startsWith("classes"))) {
                        if (zipEntry.getName().endsWith(".dex")) {
                            System.out.println("Found dex file: " + zipEntry.getName());
                        } else {
                            System.out.println("Found hidden dex file: " + zipEntry.getName());
                        }
                        record.dexFiles.add(zipEntry.getName());
                    } else {
                        // One of the regular DEX files
                        if (Utils.dexHasFileProvider(new ResettableZipEntryInputStream(zipFile, zipEntry))) {
                            record.hasFileProvider = true;
                            System.out.println("Has file provider");
                        }
                    }
                }

                if (Utils.isElfData(new ResettableZipEntryInputStream(zipFile, zipEntry))) {
                    if (!isLibPath(zipEntry.getName()) || !zipEntry.getName().endsWith("so")) {
                        if (zipEntry.getName().endsWith(".so")) {
                            System.out.println("Found elf file: " + zipEntry.getName());
                        } else {
                            System.out.println("Found hidden elf file: " + zipEntry.getName());
                        }
                        record.elfFiles.add(zipEntry.getName());
                    }
                }

                if (zipEntry.getName().toLowerCase().endsWith(".apk") || Utils.isApkData(new ResettableZipEntryInputStream(zipFile, zipEntry))) {
                    if (zipEntry.getName().endsWith(".apk")) {
                        System.out.println("Found apk file: " + zipEntry.getName());
                    } else {
                        System.out.println("Found hidden apk file: " + zipEntry.getName());
                    }

                    record.apkFiles.add(zipEntry.getName());
                }


                if (zipEntry.getName().toLowerCase().endsWith(".js") || Utils.isJsData(new ResettableZipEntryInputStream(zipFile, zipEntry))) {
                    if (zipEntry.getName().toLowerCase().endsWith(".js")) {
                        System.out.println("Found js file: " + zipEntry.getName());
                    } else {
                        System.out.println("Found hidden js file: " + zipEntry.getName());
                    }

                    record.jsFiles.add(zipEntry.getName());
                }

                if (zipEntry.getName().toLowerCase().endsWith(".dll") || Utils.isDllData(new ResettableZipEntryInputStream(zipFile, zipEntry))) {
                    if (zipEntry.getName().toLowerCase().endsWith(".dll")) {
                        System.out.println("Found dll file: " + zipEntry.getName());
                    } else {
                        System.out.println("Found hidden dll file: " + zipEntry.getName());
                    }

                    record.dllFiles.add(zipEntry.getName());
                }

                if (zipEntry.getName().toLowerCase().endsWith(".html") || zipEntry.getName().toLowerCase().endsWith(".htm") || Utils.isHtmlData(new ResettableZipEntryInputStream(zipFile, zipEntry))) {
                    if (zipEntry.getName().toLowerCase().endsWith(".html") || zipEntry.getName().toLowerCase().endsWith(".htm")) {
                        System.out.println("Found html file: " + zipEntry.getName());
                    } else {
                        System.out.println("Found hidden html file: " + zipEntry.getName());
                    }
                    record.htmlFiles.add(zipEntry.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return record;
    }

    private static File[] getApkFiles(File appFolder) {
        if (appFolder.listFiles() == null || appFolder.listFiles().length == 0) return null;

        File[] apkFiles = appFolder.listFiles(apkFilter);
        if (apkFiles == null || apkFiles.length == 0) return null;
        return apkFiles;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: ");
            System.out.println("AppAnalyzer {app_set_path} {output_path}");
            return;
        }

        ScriptEngine engine  = new ScriptEngineManager().getEngineByName("JavaScript");
        if (engine == null) {
            System.out.println("Use a JRE that includes a JavaScript engine!");
            return;
        }


        String appsetPath = args[0];
        System.out.println("Using app set " + appsetPath);
        String outputPath = args[1];
        System.out.println("Using output path " + outputPath);

        File[] categories = new File(appsetPath).listFiles(regularFoldersFilter);
        if (categories == null) {
            System.out.println("App set is empty!");
            System.exit(-1);
        }
        Arrays.sort(categories);

        int count = 0;
        List<AppRecord> records = new ArrayList<>();

        for (File category : categories) {
            File[] apps = category.listFiles(regularFoldersFilter);
            Arrays.sort(apps);

            for (File app : apps) {
                System.out.println("[" + count + "] Starting app " + app.getName());
                AppRecord record = new AppRecord();
                record.category = category.getName();

                if (app.getName().contains("_")) {
                    record.packageName = app.getName().split("_", 2)[1];
                } else {
                    record.packageName = app.getName();
                }


                File[] apkFiles = getApkFiles(app);

                if (apkFiles == null) {
                    System.out.println("No apk files.");
                    continue;
                }

                File apkFile = getBaseApkFile(apkFiles);
                if (apkFile == null) {
                    continue;
                } else {
                    System.out.println("Analyzing " + app.getName());
                    count++;
                }

                checkApk(apkFile, record);
                records.add(record);
            }
        }

        writeResults(records, new File(outputPath, "results.csv"));
    }

    public static void writeResults(List<AppRecord> records, File file) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(AppRecord.csvHeader(";").getBytes());
            outputStream.write("\n".getBytes());

            for (AppRecord record : records) {
                outputStream.write(record.toCsv(";").getBytes());
                outputStream.write("\n".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
