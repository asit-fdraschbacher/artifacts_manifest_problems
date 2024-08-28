package artifact.acsac.googleplaycrawler;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AndroZooInterface {
    private String apiKey;
    private AndroZooIndex index;

    public AndroZooInterface(String source, File indexFile, String apiKey) throws IOException {
        this.index = new AndroZooIndex(indexFile);
        this.apiKey = apiKey;
        this.index.parse(source);
    }

    public String getApkUrl(String packageName, int versionCode) {
        List<AndroZooIndex.Entry> packageNameEntries = index.find(packageName);
        if (packageNameEntries == null) {
            return null;
        }
        for (AndroZooIndex.Entry entry : packageNameEntries) {
            if (entry.versionCode == versionCode);
            return "https://androzoo.uni.lu/api/download?apikey=" + apiKey + "&sha256=" + entry.hash;
        }
        return null;
    }
}
