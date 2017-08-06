package thumbnail;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;

import org.imgscalr.Scalr;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;

import net.coobird.thumbnailator.Thumbnails;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ThumbnailBenchmark {

    @Param({"200"})
    public int thumbSize;

    public BufferedImage thumbnail;

    String lastTechnique;

    // Website with large ortho images: https://apollomapping.com/
    @Param({"land-100kb.jpg", "crowd-3mb.jpg"})

    //@Param({"land-100kb.jpg", "crowd-3mb.jpg", "land-8mb.jpg", "building-30mb.jpg",
            //"mountains-20mb.jpg", "australia-250mb.png", "salt-lake-340mb.jpg"})
            String filename;

    String inputDir = "/Users/aaronhoffer/Downloads/sample-images/";

    String outputDir = inputDir + "output/";

    public static void main(String[] args) throws RunnerException {
        String simpleName = ThumbnailBenchmark.class.getSimpleName();
        Options opt = new OptionsBuilder().include(simpleName)
                .forks(0)
                .warmupIterations(1)
                .measurementIterations(3)
                .jvmArgsAppend("-Xms4g")
                .resultFormat(ResultFormatType.NORMALIZED_CSV)
                .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() throws FileNotFoundException {

        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());
    }

    @TearDown
    public void teardown() {

        try {
            ImageIO.write(thumbnail, "png", new File(outputDir + lastTechnique + "." + filename));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lastTechnique = null;
            thumbnail = null;
        }
    }

    @Benchmark
    public BufferedImage scalrSimple() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(inputDir + filename));
        thumbnail = Scalr.resize(bufferedImage, Scalr.Method.SPEED, thumbSize);
        lastTechnique = "scalrSimple";
        return thumbnail;
    }

    @Benchmark
    public BufferedImage subsampling2() throws IOException {
        int samplingPeriod = 2;
        BufferedImage bufferedImage = getSubsampledImage(inputDir + filename, samplingPeriod);
        thumbnail = Scalr.resize(bufferedImage, Scalr.Method.SPEED, thumbSize);
        lastTechnique = "subsampling" + samplingPeriod;
        return thumbnail;
    }
/*
    @Benchmark
    public BufferedImage subsampling4() throws IOException {
        int samplingPeriod = 4;
        BufferedImage bufferedImage = getSubsampledImage(inputDir + filename, samplingPeriod);
        thumbnail = Scalr.resize(bufferedImage, Scalr.Method.SPEED, thumbSize);
        lastTechnique = "subsampling" + samplingPeriod;
        return thumbnail;
    }

    @Benchmark
    public BufferedImage subsampling8() throws IOException {
        int samplingPeriod = 8;
        BufferedImage bufferedImage = getSubsampledImage(inputDir + filename, samplingPeriod);
        thumbnail = Scalr.resize(bufferedImage, Scalr.Method.SPEED, thumbSize);
        lastTechnique = "subsampling" + samplingPeriod;
        return thumbnail;
    }

    @Benchmark
    public BufferedImage scalrTikaTransformer() throws IOException {
        Image image = ImageIO.read(new File(inputDir + filename));
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                image.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(image, null, null);
        graphics.dispose();
        thumbnail = Scalr.resize(bufferedImage, thumbSize);
        lastTechnique = "scalrTikaTransformer";
        return thumbnail;
    }

    @Benchmark
    public BufferedImage thumbnailator() throws IOException {
        thumbnail = Thumbnails.of(inputDir + filename)
                .height(thumbSize)
                .asBufferedImage();
        lastTechnique = "thumbnailator";
        return thumbnail;
    }

*/
    BufferedImage getSubsampledImage(String fullFilename, int period) throws IOException {
        int columnsSamplingPeriod = period;
        int rowSamplingPeriod = period;
        int columnOffset = 0;
        int rowOffset = 0;

        final File source = new File(fullFilename);
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
}

