import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    int id = 0;

    String packageName = null;
    int versionCode = 0;

    String versionName = null;
    String category = null;

    String displayName = null;
    String description = null;
    String developerName = null;
    String shortDescription = null;

    int offerType = 0;
    long installs = 0;
    boolean isFree = true;
    String price = null;
    boolean containsAds = false;

    long releaseTime = 0;
    long updateTime = 0;
    boolean inPlayStore = true;
    List<String> permissions = new ArrayList<>();
    float rating = 0;
    int targetSdk = 0;
    long size = 0;

    boolean aab = false;

    public static App fromJson(JSONObject object) {
        App app = new App();

        for (String key : object.keySet()) {
            if (key.equals("id")) {
                app.setId(object.getInt(key));
            } else if (key.equals("packageName")) {
                app.setPackageName(object.getString(key));
            } else if (key.equals("versionCode")) {
                app.setVersionCode(object.getInt(key));
            } else if (key.equals("versionName")) {
                app.setVersionName(object.getString(key));
            } else if (key.equals("category")) {
                app.setCategory(object.getString(key));
            } else if (key.equals("displayName") || key.equals("name")) {
                app.setDisplayName(object.getString(key));
            } else if (key.equals("description")) {
                app.setDescription(object.getString(key));
            } else if (key.equals("developerName")) {
                app.setDeveloperName(object.getString(key));
            } else if (key.equals("shortDescription")) {
                app.setShortDescription(object.getString(key));
            } else if (key.equals("offerType")) {
                app.setOfferType(object.getInt(key));
            } else if (key.equals("installs")) {
                app.setInstalls(object.getInt(key));
            } else if (key.equals("free") || key.equals("isFree")) {
                app.setFree(object.getBoolean(key));
            } else if (key.equals("price")) {
                app.setPrice(object.getString(key));
            } else if (key.equals("containsAds")) {
                app.setContainsAds(object.getBoolean(key));
            } else if (key.equals("releaseTime")) {
                app.setReleaseTime(object.getLong(key));
            } else if (key.equals("updateTime")) {
                app.setUpdateTime(object.getLong(key));
            } else if (key.equals("inPlayStore")) {
                app.setInPlayStore(object.getBoolean(key));
            } else if (key.equals("rating")) {
                app.setRating(object.getFloat(key));
            } else if (key.equals("targetSdk")) {
                app.setTargetSdk(object.getInt(key));
            } else if (key.equals("size")) {
                app.setSize(object.getLong(key));
            } else if (key.equals("aab")) {
                app.setAab(object.getBoolean(key));
            } else if (key.equals("extras")) {
                JSONObject extras = object.getJSONObject(key);
                for (String key2 : extras.keySet()) {
                    app.extras.put(key2, extras.get(key2));
                }
            }
        }

        return app;
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("packageName", packageName);
        object.put("versionCode", versionCode);

        object.put("versionName", versionName);
        object.put("category", category);

        object.put("displayName", displayName);
        object.put("description", description);
        object.put("developerName", developerName);
        object.put("shortDescription", shortDescription);

        object.put("offerType", offerType);
        object.put("installs", installs);
        object.put("isFree", isFree);
        object.put("price", price);
        object.put("containsAds", containsAds);

        object.put("releaseTime", releaseTime);
        object.put("updateTime", updateTime);
        object.put("inPlayStore", inPlayStore);
        object.put("rating", rating);
        object.put("targetSdk", targetSdk);
        object.put("size", size);

        JSONArray jsonPermissions = new JSONArray();
        for (int i = 0; i < permissions.size(); i++) {
            jsonPermissions.put(permissions.get(i));
        }
        object.put("permissions", jsonPermissions);

        JSONObject jsonExtras = new JSONObject();
        for (Map.Entry<String, Object> entry : extras.entrySet()) {
            try {
                jsonExtras.put(entry.getKey(), entry.getValue());
            } catch (Exception ignored){}
        }
        object.put("extras", jsonExtras);

        object.put("aab", aab);

        return object;
    }

    Map<String, Object> extras = new HashMap<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public int getOfferType() {
        return offerType;
    }

    public void setOfferType(int offerType) {
        this.offerType = offerType;
    }

    public long getInstalls() {
        return installs;
    }

    public void setInstalls(long installs) {
        this.installs = installs;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public boolean isContainsAds() {
        return containsAds;
    }

    public void setContainsAds(boolean containsAds) {
        this.containsAds = containsAds;
    }

    public long getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(long releaseTime) {
        this.releaseTime = releaseTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isInPlayStore() {
        return inPlayStore;
    }

    public void setInPlayStore(boolean inPlayStore) {
        this.inPlayStore = inPlayStore;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getTargetSdk() {
        return targetSdk;
    }

    public void setTargetSdk(int targetSdk) {
        this.targetSdk = targetSdk;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, Object> extras) {
        this.extras = extras;
    }

    public boolean isAab() {
        return aab;
    }

    public void setAab(boolean aab) {
        this.aab = aab;
    }
}
