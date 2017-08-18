package thumbnail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

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

    public BufferedImage getSubsampledImage(int samplePeriod, int thumbsize) throws IOException {
        return Subnail.of(getSoureceFile())
                .thumbSize(thumbsize)
                .samplePeriod(samplePeriod)
                .getImage();

    }

    public FileInputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(inputDir + sourceFilename);
    }
    public File getSoureceFile() throws FileNotFoundException {
        return new File(inputDir + sourceFilename);
    }

}
