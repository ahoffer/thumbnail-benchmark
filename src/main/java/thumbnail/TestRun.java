package thumbnail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class TestRun {

    private String inputDir;

    private String technique;

    private BufferedImage thumbnail;

    private String outputDir;

    private String sourceFilename;

    public static TestRun from(String inputDirectory, String outputDirectory) {
        TestRun object = new TestRun();
        object.inputDir = inputDirectory;
        object.outputDir = outputDirectory;
        return object;
    }

    public static BufferedImage getSubsampledImage(File source, int period) throws IOException {
        int columnsSamplingPeriod = period;
        int rowSamplingPeriod = period;
        int columnOffset = 0;
        int rowOffset = 0;
        //Create seekable input stream for use by image readers
        final ImageInputStream imageInputStream = ImageIO.createImageInputStream(source);
        // Find all image readers that recognize the image format
        final Iterator iter = ImageIO.getImageReaders(imageInputStream);
        // Use the first reader. Throw exception if no reader exists.
        final ImageReader reader = (ImageReader) iter.next();
        ImageReadParam imageParam = reader.getDefaultReadParam();
        imageParam.setSourceSubsampling(columnsSamplingPeriod,
                rowSamplingPeriod,
                columnOffset,
                rowOffset);
        reader.setInput(imageInputStream);
        return reader.read(0, imageParam);
    }

    public void setSourceFileAndLabel(String sourceFilename, String technique) {
        this.sourceFilename = sourceFilename;
        this.technique = technique;
    }

    public void end() {

        try {
            ImageIO.write(thumbnail, "png", new File(outputDir + technique + "-" + sourceFilename));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            technique = null;
            thumbnail = null;
            sourceFilename = null;
        }
    }

    public BufferedImage setThumbnailAndReturn(BufferedImage bufferedImage) {
        thumbnail = bufferedImage;
        return bufferedImage;
    }

    public BufferedImage getSubsampledImage(int samplePeriod) throws IOException {
        return TestRun.getSubsampledImage(new File(inputDir + sourceFilename), samplePeriod);

    }

}
