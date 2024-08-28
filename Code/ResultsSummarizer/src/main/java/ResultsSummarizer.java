import org.json.JSONObject;

public class ResultsSummarizer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: ResultsSummarizer <input_folder>");
        }

        int aab = 0;
        int codeTransparency = 0;

        java.io.File inputFolder = new java.io.File(args[0]);
        int inputFileIndex = 0;
        java.io.File inputFile = new java.io.File(inputFolder, "results_" + inputFileIndex++ + ".0.json");
        while (inputFile.exists()) {
            String inputData = Utils.loadStringFromFile(inputFile);
            JSONObject appIdsObject = new JSONObject(inputData);
            for (String key : appIdsObject.keySet()) {
                int value = appIdsObject.getInt(key);
                if (value == 3) {
                    codeTransparency++;
                } else if (value == 1) {
                    aab++;
                }
            }
            inputFile = new java.io.File(inputFolder, "details." + inputFileIndex++ + ".json");
        }

        System.out.println("CodeTransparency: " + codeTransparency);
        System.out.println("AAB: " + aab);
    }
}
