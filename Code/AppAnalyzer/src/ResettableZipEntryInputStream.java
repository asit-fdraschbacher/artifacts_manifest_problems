import java.io.FilterInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResettableZipEntryInputStream extends FilterInputStream {
    private final ZipEntry zipEntry;
    private final ZipFile zipFile;

    public ResettableZipEntryInputStream(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        super(zipFile.getInputStream(zipEntry));
        this.zipEntry = zipEntry;
        this.zipFile = zipFile;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (in != null) in.close();
        in = zipFile.getInputStream(zipEntry);
    }
}
