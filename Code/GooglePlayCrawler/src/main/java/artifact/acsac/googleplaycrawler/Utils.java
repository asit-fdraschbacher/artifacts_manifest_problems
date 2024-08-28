package artifact.acsac.googleplaycrawler;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static String unescapeUnicode(String string) {
        try {
            Properties p = new Properties();
            p.load(new StringReader("key=" + string));
            return p.getProperty("key");
        } catch (Exception e) {
            return string;
        }
    }

    public static String jsonQuoteString(String s) {
        String escaped = JSONObject.quote(s);
        return escaped;
    }

    public static byte[] downloadFileData(String urlString, int length) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (connection.getContentLength() < length) length = connection.getContentLength();

            InputStream inputStream = connection.getInputStream();

            byte[] buffer = new byte[length];
            int bufferOffset = 0;
            int count = 0;

            while ((count = inputStream.read(buffer, bufferOffset, length - bufferOffset)) > 0) {
                bufferOffset += count;
                if (bufferOffset >= length) break;
            }
            inputStream.close();

            if (bufferOffset != length) return null;
            return buffer;
    }

    public static long downloadFileSize(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Java");
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("HEAD");
        connection.connect();
        return connection.getContentLength();
    }

    private static OkHttpClient okHttpClient = new OkHttpClient();
    static {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            okHttpClient = new OkHttpClient.Builder().sslSocketFactory(sc.getSocketFactory(), (X509TrustManager) trustAllCerts[0]).build();
        } catch (Exception e) {
        }
    }

    public static byte[] downloadFileSuffix(String urlString, int length) throws IOException {
        int bufferLength = length;
        byte[] buffer = new byte[bufferLength];

        URL url = new URL(urlString);
        Request request = new Request.Builder().url(url).addHeader("Range", "bytes=" + "-" + bufferLength).build();
        Response response = okHttpClient.newCall(request).execute();
        InputStream inputStream = response.body().byteStream();

        int bufferOffset = 0;
        int count = 0;
        while ((count = inputStream.read(buffer, bufferOffset, bufferLength - bufferOffset)) > 0) {
            bufferOffset += count;
            if (bufferOffset >= bufferLength) break;
        }
        inputStream.close();

        return Arrays.copyOf(buffer, bufferOffset);
    }

    public static byte[] downloadFileSuffix(String urlString, Map<String,String> headers, byte[] postData, int length) throws IOException {
        int bufferLength = length;
        byte[] buffer = new byte[bufferLength];

        URL url = new URL(urlString);
        Request.Builder builder = new Request.Builder().url(url).addHeader("Range", "bytes=" + "-" + bufferLength);
        builder.post(RequestBody.create(postData));
        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
        Request request = builder.build();
        Response response = okHttpClient.newCall(request).execute();
        InputStream inputStream = response.body().byteStream();

        int bufferOffset = 0;
        int count = 0;
        while ((count = inputStream.read(buffer, bufferOffset, bufferLength - bufferOffset)) > 0) {
            bufferOffset += count;
            if (bufferOffset >= bufferLength) break;
        }
        inputStream.close();

        return Arrays.copyOf(buffer, bufferOffset);
    }

    public static byte[] downloadZipCentralDirectory(String urlString, Map<String,String> headers, byte[] postData) throws IOException {
        byte[] buffer = downloadFileSuffix(urlString, headers, postData, 512 * 1024);
        int bufferLength = buffer.length;

        int endOfCentralDirectoryOffset = -1;
        byte[] endOfCentralDirectorySignature = new byte[]{0x50, 0x4b, 0x05, 0x06};
        for (int i = bufferLength - endOfCentralDirectorySignature.length; i >= 0; i--) {
            if (buffer[i] == endOfCentralDirectorySignature[0] &&
                    buffer[i + 1] == endOfCentralDirectorySignature[1] &&
                    buffer[i + 2] == endOfCentralDirectorySignature[2] &&
                    buffer[i + 3] == endOfCentralDirectorySignature[3]) {
                endOfCentralDirectoryOffset = i;
                break;
            }
        }
        if (endOfCentralDirectoryOffset == -1) return null;
        int centralDirectorySize =
                buffer[endOfCentralDirectoryOffset + 15] << 24 |
                        (buffer[endOfCentralDirectoryOffset + 14] & 0xFF) << 16 |
                        (buffer[endOfCentralDirectoryOffset + 13] & 0xFF) << 8 |
                        (buffer[endOfCentralDirectoryOffset + 12] & 0xFF);

        if (centralDirectorySize > endOfCentralDirectoryOffset) {
            // We need to download additional data
            int remainingSize = centralDirectorySize - endOfCentralDirectoryOffset;
            buffer = downloadFileSuffix(urlString, headers, postData, remainingSize + bufferLength);
            bufferLength = buffer.length;
            endOfCentralDirectoryOffset += remainingSize;
        }

        byte[] centralDirectory = Arrays.copyOfRange(buffer, endOfCentralDirectoryOffset - centralDirectorySize, endOfCentralDirectoryOffset);
        if (centralDirectory[0] == 0x50 && centralDirectory[1] == 0x4b &&
                centralDirectory[2] == 0x01 && centralDirectory[3] == 0x02) {
            // Sanity check passed
            return centralDirectory;
        } else {
            System.out.println("Central directory sanity check failed!");
        }

        return null;
    }

    public static byte[] downloadZipCentralDirectory(String urlString) throws IOException {
        byte[] buffer = downloadFileSuffix(urlString, 512 * 1024);
        int bufferLength = buffer.length;

        int endOfCentralDirectoryOffset = -1;
        byte[] endOfCentralDirectorySignature = new byte[]{0x50, 0x4b, 0x05, 0x06};
        for (int i = bufferLength - endOfCentralDirectorySignature.length; i >= 0; i--) {
            if (buffer[i] == endOfCentralDirectorySignature[0] &&
                buffer[i + 1] == endOfCentralDirectorySignature[1] &&
                buffer[i + 2] == endOfCentralDirectorySignature[2] &&
                buffer[i + 3] == endOfCentralDirectorySignature[3]) {
                endOfCentralDirectoryOffset = i;
                break;
            }
        }
        if (endOfCentralDirectoryOffset == -1) return null;
        int centralDirectorySize =
                buffer[endOfCentralDirectoryOffset + 15] << 24 |
                (buffer[endOfCentralDirectoryOffset + 14] & 0xFF) << 16 |
                (buffer[endOfCentralDirectoryOffset + 13] & 0xFF) << 8 |
                (buffer[endOfCentralDirectoryOffset + 12] & 0xFF);

        if (centralDirectorySize > endOfCentralDirectoryOffset) {
            // We need to download additional data
            int remainingSize = centralDirectorySize - endOfCentralDirectoryOffset;
            buffer = downloadFileSuffix(urlString, remainingSize + bufferLength);
            bufferLength = buffer.length;
            endOfCentralDirectoryOffset += remainingSize;
        }

        byte[] centralDirectory = Arrays.copyOfRange(buffer, endOfCentralDirectoryOffset - centralDirectorySize, endOfCentralDirectoryOffset);
        if (centralDirectory[0] == 0x50 && centralDirectory[1] == 0x4b &&
                centralDirectory[2] == 0x01 && centralDirectory[3] == 0x02) {
            // Sanity check passed
            return centralDirectory;
        } else {
            System.out.println("Central directory sanity check failed!");
        }

        return null;
    }

    public static List<String> loadWordList(File file) {
        List<String> words = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(new FileReader(file));
            while((scanner.hasNextLine())){
                words.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return words;
    }

    public static void saveStringToFile(String string, File file) {
        File tempFile = new File(file.getParentFile(), file.getName() + ".tmp");

        try {
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            outputStream.write(string.getBytes());
            outputStream.flush();
            outputStream.close();
            file.delete();
            tempFile.renameTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public static String runProcess(int timeoutMs, String... commands) {
        try {
            ProcessBuilder builder = new ProcessBuilder(commands).redirectErrorStream(true);

            StringBuilder output = new StringBuilder();
            Process process = builder.start();

            Thread readThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            output.append(line).append("\n");
                        }
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            });
            readThread.start();

            if (timeoutMs > 0) {
                process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            } else {
                process.waitFor();
            }

            process.destroyForcibly();
            readThread.join();

            return output.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
