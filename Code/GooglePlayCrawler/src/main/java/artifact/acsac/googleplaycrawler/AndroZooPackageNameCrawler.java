package artifact.acsac.googleplaycrawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AndroZooPackageNameCrawler {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage:");
            System.err.println("AndroZooPackageNameCrawler <androzoo_index_file> <output_folder>");
        }

        File indexFile = new File(args[0]);
        File outputFolder = new File(args[1]);

        AndroZooIndex index = new AndroZooIndex(indexFile);
        index.parse("play.google.com");

        List<String> packageNames = new ArrayList<>();

        for (Map.Entry<String, List<AndroZooIndex.Entry>> entry : index.getIndex().entrySet()) {
            packageNames.add(entry.getKey());
        }

        packageNames.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        VisitedIdRegistry registry = new VisitedIdRegistry(outputFolder, "packageNames");
        registry.addVisited(packageNames, true);
    }
}
