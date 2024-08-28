package artifact.acsac.googleplaycrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroZooIndex {
    private File indexFile;
    Map<String, List<Entry>> index;

    public class Entry {
        int versionCode;
        String hash;

        public Entry(int versionCode, String hash) {
            this.versionCode = versionCode;
            this.hash = hash;
        }
    }

    private Map<String, List<Entry>> parseIndex(String source) throws IOException {
        Map<String, List<Entry>> entries = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(indexFile));

        String line;
        line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            String packageName = values[5].replace("\"", "");

            if (source == null || values[10].equals(source)) {
                try {
                    int versionCode = Integer.parseInt(values[6]);
                    List<Entry> packageNameEntries = entries.get(packageName);
                    if (packageNameEntries == null) {
                        packageNameEntries = new ArrayList<>();
                    }
                    String hash = values[0];
                    packageNameEntries.add(new Entry(versionCode, hash));
                    entries.put(packageName, packageNameEntries);
                } catch (Exception ignored){}
            }
        }

        return entries;
    }

    public List<Entry> find(String packageName) {
        return index.get(packageName);
    }

    public Map<String, List<Entry>> getIndex() {
        return index;
    }

    public AndroZooIndex(File indexFile) {
        this.indexFile = indexFile;
    }

    public void parse(String source) throws IOException {
        this.index = parseIndex(source);
    }
}
