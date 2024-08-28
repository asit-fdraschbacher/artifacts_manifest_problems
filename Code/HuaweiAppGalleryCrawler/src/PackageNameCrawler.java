import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PackageNameCrawler {
    public static String getInterfaceCode() throws IOException {
        String urlString = "https://web-dre.hispace.dbankcloud.com/webedge/getInterfaceCode";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setDoOutput(true);
        String content = "{\"params\":{},\"zone\":\"\",\"locale\":\"en\"}";
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Origin", "https://appgallery.huawei.com");
        connection.setRequestProperty("Referer", "https://appgallery.huawei.com/");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15");

        connection.getOutputStream().write(content.getBytes());

        int contentLength = connection.getContentLength();
        InputStream inputStream = connection.getInputStream();
        byte[] buffer = new byte[contentLength];
        int bufferOffset = 0;
        int count = 0;

        while ((count = inputStream.read(buffer, bufferOffset, contentLength - bufferOffset)) > 0) {
            bufferOffset += count;
            if (bufferOffset >= contentLength) break;
        }
        inputStream.close();

        return new String(buffer).replace("\"", "");
    }

    public static App getAppDetails(String interfaceCode, String id) throws IOException {
        String urlString = " https://web-dre.hispace.dbankcloud.com/uowap/index?method=internal.getTabDetail&uri=app%7C" + id + "&locale=en";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("Interface-Code", interfaceCode + "_" + System.currentTimeMillis());
        connection.setRequestProperty("Origin", "https://appgallery.huawei.com");
        connection.setRequestProperty("Referer", "https://appgallery.huawei.com/");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15");

        InputStream inputStream = connection.getInputStream();
        byte[] buffer = new byte[4096];
        int count = 0;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while ((count = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, count);
        }
        inputStream.close();

        String data = outputStream.toString();
        if (data.contains("No longer available")) return null;

        JSONObject details1 = new JSONObject(data).getJSONArray("layoutData").getJSONObject(0).getJSONArray("dataList").getJSONObject(0);
        JSONObject details2 = new JSONObject(data).getJSONArray("layoutData").getJSONObject(1).getJSONArray("dataList").getJSONObject(0);
        JSONObject details4 = new JSONObject(data).getJSONArray("layoutData").getJSONObject(3).getJSONArray("dataList").getJSONObject(0);

        App app = new App();
        app.setPackageName(details1.getString("package"));
        app.setTargetSdk(details1.optInt("targetSDK"));
        app.setVersionName(details1.optString("versionName"));
        app.setVersionCode(details1.optInt("versionCode"));
        app.setDisplayName(details1.optString("name"));
        app.setSize(details1.optInt("size"));
        app.setDeveloperName(details4.optString("developer"));
        app.setPrice("" + details1.optInt("price"));
        app.setFree(details1.optInt("price") == 0);
        app.setExtras(new HashMap<>());
        app.getExtras().put("ctype", details1.optInt("ctype"));
        app.getExtras().put("bundleSize", details1.optInt("bundleSize"));
        app.getExtras().put("downurl", details1.optString("downurl"));
        app.getExtras().put("shellApkVer", details1.optInt("shellApkVer"));
        app.getExtras().put("nonAdaptType", details1.optInt("nonAdaptType"));
        app.getExtras().put("appoid", details1.optString("appoid"));
        app.getExtras().put("packingType", details1.optInt("packingType"));
        app.getExtras().put("appid", details1.optString("appid"));
        app.getExtras().put("releaseDate", details4.optString("releaseDate"));

        return app;
    }

    static AtomicInteger tried = new AtomicInteger(0);
    static AtomicInteger found = new AtomicInteger(0);

    // We want to check 20 million different ids starting at C100000000
    static int idRange =   20000000;
    static int idOffset = 100000000;

    public static void run(File outputFolder, int threadId, int totalThreads) {
        int idsPerThread = (idRange / totalThreads) + 1;
        String code = null;
        int errors = 0;

        File jsonFile = new File(outputFolder, "details." + threadId + ".json");
        JSONObject json = new JSONObject();
        int haveAlready = 0;

        if (jsonFile.exists()) {
            String data = Utils.loadStringFromFile(jsonFile);
            json = new JSONObject(data);

            for (String key : json.keySet()) {
                App app = App.fromJson(json.getJSONObject(key));
                if (app.getExtras() != null && app.getExtras().get("appid") != null) {
                    String appId = (String) app.getExtras().get("appid");
                    appId = appId.replace("C10", "");
                    appId = appId.replace("C", "");
                    if (Integer.parseInt(appId) > haveAlready) {
                        haveAlready = Integer.parseInt(appId);
                    }
                }
            }
        }

        for (int i = haveAlready; i < idsPerThread; i++) {
            int id = (idsPerThread * threadId) + i;
            if (i % 10000 == 0) {
                try {
                    code = getInterfaceCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (i % 50 == 0) {
                Utils.saveStringToFile(json.toString(), jsonFile);
            }

            try {
                App app = getAppDetails(code, "C" + String.format("%d", id + idOffset));
                if (app != null) {
                    json.put(app.getPackageName(), app.toJson());
                    found.incrementAndGet();
                }
                tried.incrementAndGet();
                errors = 0;
            } catch (Exception e) {
                errors++;
                if (errors < 10) {
                    i--;
                } else {
                    e.printStackTrace();
                    tried.incrementAndGet();
                    errors = 0;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            System.err.println("Usage:");
            System.err.println("PackageNameCrawler <output_folder>");
        }

        AtomicInteger threads = new AtomicInteger(0);
        File outputFolder = new File(args[0]);

        int thread_count = 50;
        for (int i = 0; i < thread_count; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int id = threads.incrementAndGet();
                    PackageNameCrawler.run(outputFolder, id, thread_count);
                    threads.decrementAndGet();
                }
            }).start();
        }

        Thread.sleep(1000);

        while (threads.get() > 0) {
            Thread.sleep(1000);
            System.out.println("Tried: " + tried.get());
            System.out.println("Found: " + found.get());
        }
    }
}
