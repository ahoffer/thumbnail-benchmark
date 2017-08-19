package thumbnail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

public class OutputWriter {

    private String inputDir;

    private String technique;

    private BufferedImage thumbnail;

    private String outputDir;

    private String sourceFilename;

    public static OutputWriter from(String inputDirectory, String outputDirectory) {
        OutputWriter object = new OutputWriter();
        object.inputDir = inputDirectory;
        object.outputDir = outputDirectory;
        return object;
    }

    public OutputWriter next() {
        return OutputWriter.from(inputDir, outputDir);
    }

    public OutputWriter setSourceFileAndLabel(String sourceFilename, String technique) {
        this.sourceFilename = sourceFilename;
        this.technique = technique;
        return this;
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

    public File getSoureceFile() throws FileNotFoundException {
        return new File(inputDir + sourceFilename);
    }

}
