package artifact.acsac.googleplaycrawler;

import com.aurora.gplayapi.data.models.*;
import com.aurora.gplayapi.data.providers.DeviceInfoProvider;
import com.aurora.gplayapi.helpers.AppDetailsHelper;
import com.aurora.gplayapi.helpers.AuthHelper;
import com.aurora.gplayapi.helpers.CategoryHelper;
import com.aurora.gplayapi.helpers.SearchHelper;
import com.google.gson.Gson;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This accesses the REST API at https://android.clients.google.com/fdfe, like the Play Store app
// Requires user authentication, though some anonymous possibility seems to exist (See Aurora Store)
public class GooglePlayInterface {
    private static final Pattern CATEGORY_REGEX = Pattern.compile("cat=([^&]*)");

    private AuthData authData;

    private App parseApp(com.aurora.gplayapi.data.models.App app) {
        App parsedApp = new App();
        parsedApp.setCategory(app.getCategoryName());
        parsedApp.setContainsAds(app.getContainsAds());
        parsedApp.setDescription(app.getDescription());
        parsedApp.setDeveloperName(app.getDeveloperName());
        parsedApp.setDisplayName(app.getDisplayName());
        parsedApp.setFree(app.isFree());
        parsedApp.setId(app.getId());
        parsedApp.setInPlayStore(app.getInPlayStore());
        parsedApp.setInstalls(app.getInstalls());
        parsedApp.setOfferType(app.getOfferType());
        parsedApp.setPackageName(app.getPackageName());
        parsedApp.setPermissions(app.getPermissions());
        parsedApp.setPrice(app.getPrice());
        parsedApp.setRating(app.getRating().getAverage());
        return parsedApp;
    }

    private static PlayResponse postAuth(String url, byte[] body) throws IOException {
        RequestBody requestBody = RequestBody.create(body, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .header(
                        "User-Agent",
                        "com.aurora.store-4.1.1-41"
                )
                .method("POST", requestBody)
                .build();
        Call call = new OkHttpClient().newCall(request);
        Response response = call.execute();
        return new PlayResponse(
                response.body() != null ? response.body().bytes() : new byte[]{},
                new byte[]{},
                !response.isSuccessful() ? response.message() : "",
                response.isSuccessful(),
                response.code());
    }

    private static PlayResponse getAuth(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header(
                        "User-Agent",
                        "${BuildConfig.APPLICATION_ID}-${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}"
                )
                .method("GET", null)
                .build();
        Call call = new OkHttpClient().newCall(request);
        Response response = call.execute();
        return new PlayResponse(
                response.body() != null ? response.body().bytes() : new byte[]{},
                new byte[]{},
                !response.isSuccessful() ? response.message() : "",
                response.isSuccessful(),
                response.code());
    }


    public static AuthData getAnonymousAuthData(Properties deviceProperties, boolean insecure) throws Exception {
        if (!insecure) {
            PlayResponse playResponse = postAuth("https://auroraoss.com/api/auth", new Gson().toJson(deviceProperties).getBytes());

            if (playResponse.isSuccessful()) {
                return new Gson().fromJson(new String(playResponse.getResponseBytes()), AuthData.class);
            } else {
                if (playResponse.getCode() == 404) {
                    throw new Exception("Server unreachable");
                } else if (playResponse.getCode() == 429) {
                    throw new Exception("Rate limit!");
                } else {
                    throw new Exception(playResponse.getErrorString());
                }
            }
        } else {
            PlayResponse playResponse = getAuth("https://auroraoss.com/api/auth");

            if (playResponse.isSuccessful()) {
                String json = new String(playResponse.getResponseBytes());
                JSONObject insecureAuth = new JSONObject(json);
                DeviceInfoProvider deviceInfoProvider = new DeviceInfoProvider(deviceProperties, Locale.getDefault().toString());
                return AuthHelper.INSTANCE.build(insecureAuth.getString("email"), insecureAuth.getString("auth"),
                        AuthHelper.Token.AUTH, true, deviceInfoProvider.getProperties(), Locale.getDefault());
            } else {
                if (playResponse.getCode() == 404) {
                    throw new Exception("Server unreachable");
                } else if (playResponse.getCode() == 429) {
                    throw new Exception("Rate limit!");
                } else {
                    throw new Exception(playResponse.getErrorString());
                }
            }
        }
    }

    public GooglePlayInterface(AuthData authData) {
        this.authData = authData;
    }

    public List<String> getCategories() {
        List<String> categoryStrings = new ArrayList<>();

        try {
            List<Category> categories = new CategoryHelper(authData).getAllCategories(Category.Type.APPLICATION);
            for (Category c : categories) {
                Matcher matcher = CATEGORY_REGEX.matcher(c.getBrowseUrl());
                if (matcher.find()) {
                    String category = matcher.group(1);
                    categoryStrings.add(category);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categoryStrings;
    }
    
    public App getDetailsForApp(String packageName) {
        AppDetailsHelper helper = new AppDetailsHelper(authData);
        try {
            com.aurora.gplayapi.data.models.App app = helper.getAppByPackageName(packageName);
            return parseApp(app);
        } catch (Exception e) {}

        return null;
    }

    public List<App> getDetailsForApps(List<String> packageNames) {
        AppDetailsHelper helper = new AppDetailsHelper(authData);
        try {
            List<com.aurora.gplayapi.data.models.App> apps = helper.getAppByPackageName(packageNames);
            List<App> parsedApps = new ArrayList<>();
            for (com.aurora.gplayapi.data.models.App app : apps) {
                parsedApps.add(parseApp(app));
            }
            return parsedApps;
        } catch (Exception e) {}

        return null;
    }

    public List<App> getAppsForDeveloper(String developer) {
        List<App> apps = new ArrayList<>();

        SearchHelper helper = new SearchHelper(authData);
        try {
            SearchBundle searchBundle = helper.searchResults(developer, "");
            while (searchBundle.getAppList().size() > 0) {
                for (com.aurora.gplayapi.data.models.App app : searchBundle.getAppList()) {
                    apps.add(parseApp(app));
                }
                searchBundle = helper.next(searchBundle.getSubBundles());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return apps;
    }

    public List<App> getSimilarAppsForApp(String packageName) {
        List<App> apps = new ArrayList<>();

        AppDetailsHelper helper = new AppDetailsHelper(authData);
        try {
            com.aurora.gplayapi.data.models.App app = helper.getAppByPackageName(packageName);
            if (app.getDetailsStreamUrl() != null) {
                StreamBundle streamBundle = helper.getDetailsStream(app.getDetailsStreamUrl());
                for (StreamCluster cluster : streamBundle.getStreamClusters().values()) {
                    do {
                        for (com.aurora.gplayapi.data.models.App clusterApp : cluster.getClusterAppList()) {
                            apps.add(parseApp(clusterApp));
                        }
                        cluster = helper.getNextStreamCluster(cluster.getClusterNextPageUrl());
                    } while (cluster.hasNext());
                }
            }
            return apps;
        } catch (Exception e) {}
        return null;
    }
}
