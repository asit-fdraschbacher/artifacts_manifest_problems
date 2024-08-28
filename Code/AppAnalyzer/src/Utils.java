import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {
    public static String loadString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 16];

        int count = 0;
        while ((count = inputStream.read(buffer, 0, buffer.length)) > 0) {
            outputStream.write(buffer, 0, count);
        }

        return outputStream.toString();
    }

    public static byte[] loadData(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 16];

        int count = 0;
        while ((count = inputStream.read(buffer, 0, buffer.length)) > 0) {
            outputStream.write(buffer, 0, count);
        }

        return outputStream.toByteArray();
    }

    public static void saveStringToFile(String string, File file) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isPrintableChar(char c) {
        if (c == 9 || c == 10 || c == 11 || c == 13) return true;
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }

    public static boolean isTextData(byte[] data) {
        return isPrintableChar((char) data[0]) && isPrintableChar((char) data[1]) &&
                isPrintableChar((char) data[2]) && isPrintableChar((char) data[3]);
    }

    public static boolean isDexData(InputStream inputStream) {
        byte[] buffer = new byte[4];
        try (inputStream) {
            if (4 != inputStream.read(buffer)) return false;

            if (buffer[0] == 'd' && buffer[1] == 'e' && buffer[2] == 'x') return true;
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    public static boolean isElfData(InputStream inputStream) {
        byte[] buffer = new byte[4];
        try (inputStream) {
            if (4 != inputStream.read(buffer)) return false;

            if (buffer[1] == 'E' && buffer[2] == 'L' && buffer[3] == 'F') return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isJsDataSimple(InputStream inputStream) {
        byte[] buffer = new byte[4];
        try (inputStream) {
            if (4 != inputStream.read(buffer)) return false;

            if (!isTextData(buffer)) return false;
        } catch (IOException e) {
            return false;
        }

        try {
            inputStream.reset();
            String content = loadString(inputStream);
            return Pattern.compile("(var|const|function|return)").matcher(content).find();
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isJsData(InputStream inputStream) {
        if (!isJsDataSimple(inputStream)) return false;

        try {
            inputStream.reset();
            String content = loadString(inputStream);
            return JsSyntaxChecker.getInstance().checkSyntaxValid(content);
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isHtmlData(InputStream inputStream) {
        byte[] buffer = new byte[4];
        try (inputStream) {
            if (4 != inputStream.read(buffer)) return false;

            if (!isTextData(buffer)) return false;
        } catch (IOException e) {
            return false;
        }

        try {
            String content = loadString(inputStream);
            return content.contains("<html>");
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isApkData(InputStream inputStream) {
        byte[] buffer = new byte[4];
        try (inputStream) {
            if (4 != inputStream.read(buffer)) return false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (buffer[0] != 'P' || buffer[1] != 'K' || buffer[2] != 3 || buffer[3] != 4) return false;

        try {
            String content = loadString(inputStream);
            return content.contains("classes.dex");
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isDllData(InputStream inputStream) {
        byte[] buffer = new byte[4];
        try (inputStream) {
            if (4 != inputStream.read(buffer)) return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer[0] == 'M' && buffer[1] == 'Z';
    }

    private static boolean containsSubArray(byte[] in, byte[] find) {
        boolean done = false;
        for(int i = 0; i < in.length; i++) {
            if(in[i] == find[0]) {
                for(int ii = 1; ii < find.length; ii++) {
                    if (in[i+ii] != find[ii]) break;
                    else if (ii==find.length-1) done = true;
                }
                if (done) return true;
            }
        }
        return false;
    }

    public static boolean dexHasFileProvider(InputStream inputStream) {
        try {
            String content = loadString(inputStream);
            return content.contains("Landroidx/core/content/FileProvider") || content.contains("Landroid/support/v4/content/FileProvider");
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean manifestContainsFileProvider(InputStream inputStream) {
        try {
            byte[] data = loadData(inputStream);
            String s = new String(data).replaceAll("\\x00", "");
            return containsSubArray(data, new byte[]{'a', 0, 'n', 0, 'd', 0, 'r', 0, 'o', 0, 'i', 0, 'd', 0, '.', 0, 's', 0,
                    'u', 0, 'p', 0, 'p', 0, 'o', 0, 'r', 0, 't', 0, '.', 0, 'F', 0, 'I', 0, 'L', 0, 'E', 0, '_', 0, 'P', 0, 'R', 0,
                    'O', 0, 'V', 0, 'I', 0, 'D', 0, 'E', 0, 'R', 0, '_', 0, 'P', 0, 'A', 0, 'T', 0, 'H', 0, 'S'});
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean manifestContainsNetworkSecurityConfig(InputStream inputStream) {
        try {
            byte[] data = loadData(inputStream);
            return containsSubArray(data, new byte[]{'n', 0, 'e', 0, 't', 0, 'w', 0, 'o', 0, 'r', 0, 'k', 0, 'S', 0, 'e', 0,
                    'c', 0, 'u', 0, 'r', 0, 'i', 0, 't', 0, 'y', 0, 'C', 0, 'o', 0, 'n', 0, 'f', 0, 'i', 0, 'g'});
        } catch (IOException e) {
            return false;
        }
    }

    public static String quote(String string) {
        return "\"" + string + "\"";
    }

    public static String listToString(List<String> list) {
        return quote(Arrays.toString(list.toArray()));
    }
}
