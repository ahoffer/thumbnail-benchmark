package thumbnail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageOutputWriter {

    private String inputDir;

    private String description;

    private BufferedImage thumbnail;

    private String outputDir;

    private String sourceFilename;

    private boolean doNotWrite = false;

    public static ImageOutputWriter from(String inputDirectory, String outputDirectory) {
        ImageOutputWriter object = new ImageOutputWriter();
        object.inputDir = inputDirectory;
        object.outputDir = outputDirectory;
        return object;
    }

    public ImageOutputWriter turnOnWriting() {
        doNotWrite = false;
        return this;
    }

    public ImageOutputWriter turnOffWriting() {
        doNotWrite = true;
        return this;
    }

    public ImageOutputWriter next() {
        return ImageOutputWriter.from(inputDir, outputDir);
    }

    public ImageOutputWriter setSourceFileAndLabel(String source, String testDescription) {
        sourceFilename = source;
        this.description = testDescription;
        return this;
    }

    public void end() {

        try {
            if (!doNotWrite) {
                ImageIO.write(thumbnail,
                        "png",
                        new File(outputDir + description + "-" + sourceFilename));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            description = null;
            thumbnail = null;
            this.sourceFilename = null;
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
