package artifact.acsac.googleplaycrawler;

import org.json.JSONObject;

import javax.net.ssl.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CodeTransparencyChecker {
    private static final String ANDROZOO_INDEX_PATH = ""; // Enter your index file path here
    private static final String ANDROZOO_API_KEY = ""; // Enter your API key here


    private static final int OUTPUT_CHUNK_ENTRIES = 50000;

    public static void main(String[] args) throws Exception {
        // For whatever reason the JRE doesn't accept AndroZoo's SSL cert.
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        if (args.length != 2) {
            System.err.println("Usage:");
            System.err.println("CodeTransparencyChecker <input_folder> <output_folder>");
        }

        List<App> apps = new ArrayList<>();
        java.io.File inputFolder = new java.io.File(args[0]);
        java.io.File outputFolder = new java.io.File(args[1]);

        int inputFileIndex = 0;
        java.io.File inputFile = new java.io.File(inputFolder, "details." + inputFileIndex++ + ".json");
        while (inputFile.exists()) {
            String inputData = Utils.loadStringFromFile(inputFile);
            JSONObject appIdsObject = new JSONObject(inputData);
            for (String key : appIdsObject.keySet()) {
                JSONObject object = appIdsObject.getJSONObject(key);
                if (object.has("packageName")) {
                    apps.add(App.fromJson(object));
                }
            }
            inputFile = new java.io.File(inputFolder, "details." + inputFileIndex++ + ".json");
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
        List<App> aabApps = new ArrayList<>();

        for (int i = 0; i < apps.size(); i++) {
            App app = apps.get(i);
            if (app.isAab()) {
                aabApps.add(app);
            }
        }

        apps = aabApps;
        System.out.println("Aab apps: " + apps.size());

        // Sort by installs
        apps.sort(new Comparator<App>() {
            @Override
            public int compare(App o1, App o2) {
                return Long.compare(o2.getInstalls(), o1.getInstalls());
            }
        });

        AndroZooInterface api = new AndroZooInterface("play.google.com",
                new java.io.File(ANDROZOO_INDEX_PATH),
                ANDROZOO_API_KEY);

        // 1 request takes about 3 seconds
        int threads = 20;
        for (int i = 0; i < threads; i++) {
            List<App> threadApps = new ArrayList<>();
            for (int j = i; j < apps.size(); j += threads) {
                threadApps.add(apps.get(j));
            }
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runThread(api, outputFolder, threadApps, finalI);
                }
            }).start();
        }
    }

    private static void runThread(AndroZooInterface api, java.io.File outputFolder, List<App> appsChunk, int threadIndex) {
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
                int usesCodeTransparency = checkCodeTransparencyAndroZoo(api, appsChunk.get(i));
                outputChunk.put(app.getPackageName(), usesCodeTransparency);
                System.out.println("[" + threadIndex + "/" + i + "/" + appsChunk.size() + "] CodeTransparency: " + usesCodeTransparency + " for " + app.getPackageName());
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

            /*try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }

    public static int checkCodeTransparencyAndroZoo(AndroZooInterface api, App app) throws Exception {
        String url = api.getApkUrl(app.getPackageName(), app.getVersionCode());
        if (url == null) {
            return -2;
        }

        long start = System.currentTimeMillis();
        byte[] data = Utils.downloadZipCentralDirectory(url);
        if (data == null) return -1;
        System.out.println("Took " + (System.currentTimeMillis() - start) + " to get central directory");

        String dataString = new String(data);
        return dataString.contains("META-INF/code_transparency_signed.jwt") ? 1 : 0;
    }
}
