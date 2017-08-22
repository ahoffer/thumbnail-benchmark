package thumbnail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class SampledImageReader {

    public static final double SUBSAMPLING_HINT = Math.pow(2, 10);

    private static long fileSizeBytes;

    protected int samplePeriod;

    @FunctionalInterface
    public interface DimSupplier {
        int get() throws IOException;
    }

    @FunctionalInterface
    public interface IoFunction {
        BufferedImage apply(BufferedImage sourceImage) throws IOException;
    }

    int imageIndex;

    ImageReader reader;

    InputStream source;

    static public SampledImageReader of(InputStream source) throws IOException {
        SampledImageReader object = new SampledImageReader();
        object.source = source;
        return object.init();
    }

    static public SampledImageReader of(File sourceFile) throws IOException {
        SampledImageReader object = new SampledImageReader();
        object.source = new FileInputStream(sourceFile);
        fileSizeBytes = sourceFile.length();
        return object.init();
    }

    protected SampledImageReader init() throws IOException {
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(source);
        Iterator iter = ImageIO.getImageReaders(imageInputStream);
        reader = (ImageReader) iter.next();
        reader.setInput(imageInputStream);
        return this;
    }

    public SampledImageReader imageIndex(int index) {
        imageIndex = index;
        return this;
    }

    public SampledImageReader samplePeriod(int period) {
        samplePeriod = period;
        return this;
    }

    public int computeSamplingPeriod() {
        if (samplePeriod == 0) {
            try {
                // S
                int longestDimensionSize = Math.max(reader.getWidth(imageIndex),
                        reader.getHeight(imageIndex));
                return Math.toIntExact(Math.round(Math.ceil(
                        longestDimensionSize / SUBSAMPLING_HINT)));

            } catch (IOException e) {
                // Heuristic. Set sampling rate based on file size.
                return (int) Math.round(Math.sqrt(fileSizeBytes));
            }
        }
        return samplePeriod;
    }

    public BufferedImage read() throws IOException {
        int columnOffset = 0;
        int rowOffset = 0;
        // Use the same sampling period for both rows and columns to preserve images's
        // aspect ratio.
        int columnSamplingPeriod = computeSamplingPeriod();
        int rowSamplingPeriod = computeSamplingPeriod();
        ImageReadParam imageParam = reader.getDefaultReadParam();
        imageParam.setSourceSubsampling(columnSamplingPeriod,
                rowSamplingPeriod,
                columnOffset,
                rowOffset);
        return reader.read(imageIndex, imageParam);
    }
}
