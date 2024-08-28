package artifact.acsac.googleplaycrawler;

import com.aurora.gplayapi.DeviceManager;
import com.aurora.gplayapi.data.models.AuthData;
import com.aurora.gplayapi.helpers.AppDetailsHelper;
import com.aurora.gplayapi.data.models.App;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class AppDetailDownloader {
    private static final int BATCH_SIZE = 1000;
    private static final int BATCHES_PER_CHUNK = 50;

    private static JSONObject appToJson(App app) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", app.getId());
        jsonObject.put("packageName", app.getPackageName());

        jsonObject.put("name", app.getDisplayName());
        jsonObject.put("offerType", app.getOfferType());
        jsonObject.put("size", app.getSize());
        jsonObject.put("versionCode", app.getVersionCode());
        jsonObject.put("versionName", app.getVersionName());
        jsonObject.put("updatedOn", app.getUpdatedOn());

        jsonObject.put("shortDescription", app.getShortDescription());

        jsonObject.put("installs", app.getInstalls());
        jsonObject.put("free", app.isFree());
        jsonObject.put("price", app.getPrice());

        jsonObject.put("developerName", app.getDeveloperName());
        jsonObject.put("categoryId", app.getCategoryId());
        jsonObject.put("categoryName", app.getCategoryName());

        jsonObject.put("aab", false);
        for (com.aurora.gplayapi.data.models.File file : app.getFileList()) {
            if (file.getType() == com.aurora.gplayapi.data.models.File.FileType.SPLIT) {
                jsonObject.put("aab", true);
            }
        }

        jsonObject.put("targetSdk", app.getTargetSdk());
        jsonObject.put("containsAds", app.getContainsAds());
        jsonObject.put("inPlayStore", app.getInPlayStore());
        jsonObject.put("isSystem", app.isSystem());

        for (String key : app.getAppInfo().getAppInfoMap().keySet()) {
            jsonObject.put(key, app.getAppInfo().getAppInfoMap().get(key));
        }

        return jsonObject;
    }
    
    private static File getFileForAppId(File location, String appId) {
        return new File(location.getAbsolutePath() + "/" + appId.replace(".", "/") + "/" + appId + ".json");
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage:");
            System.err.println("AppDetailDownloader <input_folder> <output_folder>");
        }

        File inputFolder = new File(args[0]);
        File outputFolder = new File(args[1]);

        Set<String> appIdsSet = new HashSet<>();

        int inputFileIndex = 1;
        File inputFile = new File(inputFolder, "packageNames.json");
        while (inputFile.exists()) {
            String inputData = Utils.loadStringFromFile(inputFile);
            JSONObject appIdsObject = new JSONObject(inputData);
            appIdsSet.addAll(appIdsObject.keySet());
            inputFile = new File(inputFolder, "packageNames_" + inputFileIndex++ + ".json");
        }

        List<String> appIdsList = new ArrayList<>(appIdsSet);
        appIdsList.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        System.out.println("Unique apps: " + appIdsList.size());

        Properties deviceProperties = DeviceManager.INSTANCE.loadProperties("px_3a.properties");
        AuthData authData = GooglePlayInterface.getAnonymousAuthData(deviceProperties, false);
        AppDetailsHelper helper = new AppDetailsHelper(authData);

        int chunkIndex = 0;
        int chunkBatches = 0;
        JSONObject chunk = new JSONObject();

        while (new File(outputFolder, "details." + chunkIndex + ".json").exists()) {
            chunkIndex++;
        }

        for (int i = (chunkIndex * BATCHES_PER_CHUNK) * BATCH_SIZE; i < appIdsList.size(); i += BATCH_SIZE) {
            List<String> appIds = appIdsList.subList(i, Math.min(i + BATCH_SIZE, appIdsList.size()));
            try {
                List<App> details = helper.getAppByPackageName(appIds);
                for (int j = 0; j < details.size(); j++) {
                    App app = details.get(j);

                    if (app.getId() == 0) {
                        chunk.put(appIds.get(j), new JSONObject());
                        continue;
                    } else {
                        chunk.put(appIds.get(j), appToJson(app));
                    }

                    System.out.println("[" + (i + j) + "/" + appIdsList.size() + "] Loaded " + app.getPackageName());
                }
                chunkBatches++;
                if (chunkBatches == BATCHES_PER_CHUNK) {
                    System.out.println("Writing chunk " + chunkIndex);
                    File outputFile = new File(outputFolder, "details." + chunkIndex + ".json");
                    Utils.saveStringToFile(chunk.toString(2), outputFile);
                    chunk = new JSONObject();
                    chunkBatches = 0;
                    chunkIndex++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(3);
            } catch (InterruptedException ignored) {}
        }

    }
}
