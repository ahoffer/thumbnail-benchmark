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

public class Subnail {

    public static final double SUBSAMPLING_HINT = Math.pow(2, 10);

    @FunctionalInterface
    public interface DimSupplier {
        int get() throws IOException;
    }

    @FunctionalInterface
    public interface IoFunction {
        BufferedImage apply(BufferedImage sourceImage) throws IOException;
    }

    long fileSizeHint;

    int imageIndex;

    //TODO: Convert sample period to Optional<Integer>
    int columnsSamplingPeriod = 0;

    int rowSamplingPeriod = 0;

    ImageReader reader;

    InputStream source;

    static public Subnail of(InputStream source) throws IOException {
        Subnail object = new Subnail();
        object.source = source;
        return object.init();
    }

    static Subnail of(File sourceFile) throws IOException {
        Subnail object = new Subnail();
        object.source = new FileInputStream(sourceFile);
        return object.fileSizeHint(sourceFile.length())
                .init();
    }

    protected Subnail init() throws IOException {
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(source);
        Iterator iter = ImageIO.getImageReaders(imageInputStream);
        reader = (ImageReader) iter.next();
        reader.setInput(imageInputStream);
        return this;
    }

    public Subnail fileSizeHint(long fileSize) {
        this.fileSizeHint = fileSize;
        return this;
    }

    public Subnail imageIndex(int index) {
        imageIndex = index;
        return this;
    }

    public Subnail autosetSamplePeriod() {
        rowSamplingPeriod = 0;
        columnsSamplingPeriod = 0;
        return this;
    }

    public Subnail samplePeriod(int period) {
        return rowSamplingPeriod(period).colulmnSamplePeriod(period);
    }

    private Subnail colulmnSamplePeriod(int period) {
        //TODO: validate > 0
        columnsSamplingPeriod = period;
        return this;
    }

    public Subnail rowSamplingPeriod(int period) {
        //TODO: validate > 0
        rowSamplingPeriod = period;
        return this;
    }

    public BufferedImage create(IoFunction resizeFunction) throws IOException {
        BufferedImage output;

        try {

            output = resizeFunction.apply(getImage());

        } finally {
            //TODO: validate they are not null;
            source.close();
            reader.dispose();
        }

        return output;
    }

    public int getRowSamplePeriod() {
        if (rowSamplingPeriod == 0) {
            rowSamplingPeriod = computeSubsamplingPeriod(() -> reader.getHeight(imageIndex));
        }
        return rowSamplingPeriod;
    }

    public int getColumnSamplePeriod() {
        if (columnsSamplingPeriod == 0) {

            columnsSamplingPeriod = computeSubsamplingPeriod(() -> reader.getWidth(imageIndex));
        }

        return columnsSamplingPeriod;

    }

    protected int computeSubsamplingPeriod(DimSupplier getDimension) {
        //TODO: validate input > 0
        // 2047/2048 = 0.9995 -> 1
        // 2048/2048 = 1 -> 1
        // 2049/2048 = 1.00049 -> 2
        int samplePeriod = 1;
        int dimension = 0;
        try {
            dimension = getDimension.get();
        } catch (IOException e) {
            if (fileSizeHint > 0) {
                dimension = (int) Math.sqrt(fileSizeHint);
            } else {
                return samplePeriod;
            }
        }
        samplePeriod = Math.toIntExact(Math.round(Math.ceil(dimension / SUBSAMPLING_HINT)));
        return samplePeriod;
    }

    public BufferedImage getImage() throws IOException {
        int columnOffset = 0;
        int rowOffset = 0;
        ImageReadParam imageParam = reader.getDefaultReadParam();
        imageParam.setSourceSubsampling(getColumnSamplePeriod(),
                getRowSamplePeriod(),
                columnOffset,
                rowOffset);
        return reader.read(imageIndex, imageParam);
    }
}
