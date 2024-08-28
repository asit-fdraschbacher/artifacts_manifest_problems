package artifact.acsac.googleplaycrawler;

import org.json.JSONObject;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VisitedIdRegistry {
    private static int MAX_CHUNK_ENTRIES = 50000;

    private final Set<String> visitedIds = new HashSet<>();

    private final File folder;
    private final String name;

    private int nextJsonChunkIndex = 1;
    private File jsonChunkFile;
    private JSONObject jsonChunk;

    public VisitedIdRegistry(File folder, String name) {
        this.folder = folder;
        this.name = name;

        jsonChunk = new JSONObject();
        jsonChunkFile = new File(folder, name + ".json");

        File nextChunkFile = jsonChunkFile;
        while (nextChunkFile.exists()) {
            jsonChunkFile = nextChunkFile;
            String data = Utils.loadStringFromFile(jsonChunkFile);
            jsonChunk  = new JSONObject(data);
            visitedIds.addAll(jsonChunk.keySet());
            nextChunkFile = new File(folder, name + "_" + (nextJsonChunkIndex++) + ".json");
        }
        nextJsonChunkIndex -= 1;

        if (jsonChunk.length() > MAX_CHUNK_ENTRIES) {
            jsonChunkFile = new File(folder, name + "_" + (nextJsonChunkIndex++) + ".json");
            jsonChunk = new JSONObject();
        }
    }

    public void addVisited(String packageName, boolean value) {
        synchronized (visitedIds) {
            synchronized (jsonChunk) {
                jsonChunk.put(packageName, value);
                saveToDisk();
            }
            visitedIds.add(packageName);
        }
    }

    public void addVisited(List<String> packageNames, boolean value) {
        synchronized (visitedIds) {
            synchronized (jsonChunk) {
                for (String packageName : packageNames) {
                    jsonChunk.put(packageName, value);
                }
                saveToDisk();
            }
            visitedIds.addAll(packageNames);
        }
    }

    public boolean hasVisited(String packageName) {
        synchronized (visitedIds) {
            return visitedIds.contains(packageName);
        }
    }

    public int getSize() {
        synchronized (visitedIds) {
            return visitedIds.size();
        }
    }

    private void saveToDisk() {
        Utils.saveStringToFile(jsonChunk.toString(), jsonChunkFile);

        if (jsonChunk.length() > MAX_CHUNK_ENTRIES) {
            jsonChunkFile = new File(folder, name + "_" + (nextJsonChunkIndex++) + ".json");
            jsonChunk = new JSONObject();
        }
    }
}
