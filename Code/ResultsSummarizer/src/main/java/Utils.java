import java.io.*;

public class Utils {
    public static String loadStringFromFile(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;

            while ((count = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
