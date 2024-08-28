import org.json.JSONObject;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class CodeTransparencyChecker {
    private static final int OUTPUT_CHUNK_ENTRIES = 50000;

    public static void main(String[] args) throws Exception {
        List<App> apps = new ArrayList<>();

        if (args.length < 2) {
            System.err.println("Usage:");
            System.err.println("CodeTransparencyChecker <input_folder_1> <input_folder_2> ... <input_folder_n> <output_folder>");
        }

        java.io.File outputFolder = new java.io.File(args[args.length - 1]);

        for (int i = 0; i < args.length - 1; i++) {
            java.io.File inputFolder = new java.io.File(args[i]);
            int inputFileIndex = 1;
            java.io.File inputFile = new java.io.File(inputFolder, "details." + inputFileIndex++ + ".json");
            while (inputFile.exists()) {
                String inputData = Utils.loadStringFromFile(inputFile);
                JSONObject appIdsObject = new JSONObject(inputData);
                for (String key : appIdsObject.keySet()) {
                    apps.add(App.fromJson(appIdsObject.getJSONObject(key)));
                }
                inputFile = new java.io.File(inputFolder, "details." + inputFileIndex++ + ".json");
            }
        }

        System.out.println("Total apps: " + apps.size());

        for (int i = 0; i < apps.size(); i++) {
            App app = apps.get(i);
            if (!app.isFree()) {
                apps.remove(i);
                i--;
            }
        }

        System.out.println("Free apps: " + apps.size());

        int threads = 50;
        for (int i = 0; i < threads; i++) {
            List<App> threadApps = new ArrayList<>();
            for (int j = i; j < apps.size(); j += threads) {
                threadApps.add(apps.get(j));
            }
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runThread(outputFolder, threadApps, finalI);
                }
            }).start();
        }
    }

    private static void runThread(java.io.File outputFolder, List<App> appsChunk, int threadIndex) {
        int backoffMs = 500;
        JSONObject outputChunk = new JSONObject();
        int offset = 0;
        int outputChunkIndex = 0;
        java.io.File outputChunkFile = new java.io.File(outputFolder, "results_" + threadIndex + "." + outputChunkIndex++ + ".json");
        java.io.File nextOutputChunkFile = outputChunkFile;
        while (nextOutputChunkFile.exists()) {
            outputChunkFile = nextOutputChunkFile;
            String data = Utils.loadStringFromFile(outputChunkFile);
            outputChunk = new JSONObject(data);
            offset += outputChunk.length();
            nextOutputChunkFile = new java.io.File(outputFolder, "results_" + threadIndex + "." + outputChunkIndex++ + ".json");
        }
        outputChunkIndex--;

        for (int i = offset; i < appsChunk.size(); i++) {
            App app = appsChunk.get(i);
            try {
                byte[] centralDirectory = downloadZipCentralDirectory(appsChunk.get(i));
                boolean usesCodeTransparency = false;
                boolean usesAab = false;

                if (centralDirectory != null) {
                    usesCodeTransparency = checkCodeTransparencyHuawei(centralDirectory);
                    usesAab = checkAabHuawei(centralDirectory);
                }
                outputChunk.put(app.getPackageName(), usesCodeTransparency ? 3 : usesAab ? 1 : 0);
                System.out.println("[" + threadIndex + "/" + i + "/" + appsChunk.size() + "] AAB: " + usesAab + ", CodeTransparency: " + usesCodeTransparency + " for " + app.getPackageName());
                backoffMs = 500;
            } catch (SocketException | SocketTimeoutException e) {
                // AndroZoo rate limit
                System.out.println("Rate limit! Waiting " + backoffMs + "ms and trying again");
                backoffMs *= 2;
                i--;
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ignored) {
                }
            } catch (UnknownHostException | SSLHandshakeException e) {
                System.out.println("Connection issues: " + e + ". Waiting " + backoffMs + "ms and trying again");
                backoffMs *= 2;
                i--;
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ignored) {
                }
            } catch (Exception e) {
                e.printStackTrace();
                outputChunk.put(app.getPackageName(), -1);
            }

            Utils.saveStringToFile(outputChunk.toString(2), outputChunkFile);

            if (outputChunk.length() >= OUTPUT_CHUNK_ENTRIES) {
                outputChunk = new JSONObject();
                outputChunkFile = new java.io.File(outputFolder, "results_" + threadIndex + "." + outputChunkIndex++ + ".json");
            }
        }
    }

    public CodeTransparencyChecker() {
    }

    public static byte[] downloadZipCentralDirectory(App app) throws IOException {
        String url = "https://appgallery.cloud.huawei.com/appdl/" + app.getExtras().get("appid");
        long start = System.currentTimeMillis();
        byte[] data = Utils.downloadZipCentralDirectory(url);
        if (data == null) return null;
        System.out.println("Took " + (System.currentTimeMillis() - start) + " to get central directory");

        return data;
    }

    public static boolean checkCodeTransparencyHuawei(byte[] centralDirectory) {
        String dataString = new String(centralDirectory);
        return dataString.contains("META-INF/code_transparency_signed.jwt");
    }

    public static boolean checkAabHuawei(byte[] centralDirectory) {
        String dataString = new String(centralDirectory);
        return dataString.contains("META-INF/BNDLTOOL.RSA");
    }
}
