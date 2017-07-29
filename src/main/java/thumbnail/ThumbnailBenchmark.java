package thumbnail;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.imgscalr.Scalr;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ThumbnailBenchmark {

    @Param({"200"})
    public int thumbSize;

    public BufferedImage thumbnail;

    String lastTechnique;

    @Param({"land-100kb.jpg", "crowd-3mb.jpg", "land-8mb.jpg", "building-30mb.jpg",
            "mountains-20mb.jpg", "australia-250mb.png"})
    String filename;

    String dir = "/Users/aaronhoffer/Downloads/sample-images/";

    public static void main(String[] args) throws RunnerException {
        String simpleName = ThumbnailBenchmark.class.getSimpleName();
        Options opt = new OptionsBuilder().include(simpleName)
                .forks(1)
                .warmupIterations(1)
                .measurementIterations(2)
                .jvmArgsAppend("-Xms4096m")
                .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }

    BufferedImage getBufferedImageFromDisk() {
        try {
            return ImageIO.read(new File(dir + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TearDown
    public void teardown() {

        try {
            ImageIO.write(thumbnail, "png", new File(dir + lastTechnique + "." + filename));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lastTechnique = null;
            thumbnail = null;
        }
    }

//    @Benchmark
    public BufferedImage scalarSimple() {

        thumbnail = Scalr.resize(getBufferedImageFromDisk(), thumbSize);
        lastTechnique = "scalarSimple()";
        return thumbnail;
    }

    @Benchmark
    public BufferedImage subsampling() throws IOException {
        ImageReadParam imageParam = new ImageReadParam();
        int columnsSamplingPeriod = 4;
        int rowSamplingPeriod = 4;
        int columnOffset = 0;
        int rowOffset = 0;
        imageParam.setSourceSubsampling(columnsSamplingPeriod,
                rowSamplingPeriod,
                columnOffset,
                rowOffset);

        final File source = new File(dir + filename);
        //Create seekable input stream for use by image readers
        final ImageInputStream imageInputStream = ImageIO.createImageInputStream(source);
        // Find all image readers that recognize the image format
        final Iterator iter = ImageIO.getImageReaders(imageInputStream);
        // Use the first reader. Throw exception if no reader exists.
        final ImageReader reader = (ImageReader) iter.next();
        reader.setInput(imageInputStream);
        BufferedImage bufferedImage = reader.read(0, imageParam);
        thumbnail = Scalr.resize(bufferedImage, thumbSize);
        lastTechnique = "subsampling()";
        return thumbnail;
    }

//    @Benchmark
    public BufferedImage scalarTikaTransformer() {
        final BufferedImage bufferedImage = getBufferedImageFromDisk();
        BufferedImage copy = new BufferedImage(bufferedImage.getWidth(null),
                bufferedImage.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(copy, null, null);
        graphics.dispose();
        thumbnail = Scalr.resize(copy, thumbSize);
        lastTechnique = "scalarTikaTransformer()";
        return thumbnail;
    }




  /*

  ImageInputStream iis = ImageIO.createImageInputStream(o);

    // Find all image readers that recognize the image format
    Iterator iter = ImageIO.getImageReaders(iis);
    if (!iter.hasNext()) {
        // No readers found
        return null;
    }

    // Use the first reader
    ImageReader reader = (ImageReader)iter.next();
    From : http://www.exampledepot.com/egs/javax.imageio/DiscType.html

    One you have the ImageReader you can get the aspect ration by calling reader.getAspectRatio()

    I'm not sure how you'd go from an ImageReader to a thumbnail though.

            shareeditflag
    answered May 3 '11 at 19:56

    Karthik Ramachandran
7,56183345


    Excellent. Works very well using ImageReadParam.getSourceSubSampling(wratio, hratio, 0, 0) to scale it before getting the BufferedImage with ImageReader.read(0, ImageReadParam). – Johannes Keinestam May 4 '11 at 17:37


    @SWEn0thing. Cool I'll have to remember that for how to get a thumbnail. – Karthik Ramachandran May 4 '11 at 17:57
            3

    I needed to add call the following too. ImageReadParam params = reader.getDefaultReadParam(); reader.setInput(iis, true, true); params.setSourceSubsampling(width, height, 0, 0); – Neil Wightman May 31 '12 at 8:07
    */

}

