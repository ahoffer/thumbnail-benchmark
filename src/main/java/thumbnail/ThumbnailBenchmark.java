package thumbnail;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

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
import org.openjdk.jmh.profile.NaiveHeapSizeProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;

import net.coobird.thumbnailator.Thumbnails;

//     Website with large ortho images: https://apollomapping.com/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ThumbnailBenchmark {

    String inputDir = "/Users/aaronhoffer/Downloads/sample-images/";

    String outputDir = inputDir + "output_2017-08-21/";

    BufferedImage lastThumbnail;

    String lastDescription;

    @Param({"256"})
    public int thumbSize;

    // LARGE FILES ( > 1 MB)
    //    @Param({"robot-pic-700kb.jpg", "building-30mb.jpg", "land-8mb.jpg", "mountains-20mb.jpg",
    //            "crowd-3mb.jpg", "australia-250mb.png", "salt-lake-340mb.jpg"})
    //    String filename;

    //    @Param({"baghdad-j2k-20mb.jp2"})
    //    String filename;

    // SMALL FILES ( < 1 MB)
    //    @Param({"unicorn-rainbow-57kb.gif", "land-100kb.jpg", "parliment-60kb.jpg", "city-300kb.jpg",
    //            "UN-bus-attack.jpg", "militants.jpg"})
    //    String filename;

    @Param({"unicorn-rainbow-57kb.gif", "land-8mb.jpg"})
    String filename;

    public static void main(String[] args) throws RunnerException {
        String simpleName = ThumbnailBenchmark.class.getSimpleName();
        Options opt = new OptionsBuilder().include(simpleName)
                .forks(0)
                .warmupIterations(1)
                .measurementIterations(1)
                .resultFormat(ResultFormatType.NORMALIZED_CSV)
                .addProfiler(NaiveHeapSizeProfiler.class)
                .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() throws FileNotFoundException {
        // Add a JPEG 2000 reader
        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());

    }

    @TearDown
    public void teardown() throws IOException {
        //Save thumbnail as a PNG in the output directory
        ImageIO.write(lastThumbnail, "png", new File(outputDir + lastDescription + "-" + filename));
        lastDescription = null;
        lastThumbnail = null;
    }

    @Benchmark
    public BufferedImage scalrSimple() throws IOException {
        lastDescription = "scalr";
        lastThumbnail = Scalr.resize(ImageIO.read(getSoureceFile()), thumbSize);
        return lastThumbnail;
    }

    @Benchmark
    public BufferedImage thumbnailatorSimple() throws IOException {
        lastDescription = "thumbnailator";
        lastThumbnail = Thumbnails.of(getSoureceFile())
                .height(thumbSize)
                .asBufferedImage();
        return lastThumbnail;
    }

    @Benchmark
    public BufferedImage subsamplingAutoThumbnailator() throws IOException {
        lastThumbnail = Thumbnails.of(SampledImageReader.of(getSoureceFile())
                .read())
                .height(thumbSize)
                .asBufferedImage();
        lastDescription = "subsamplingAUTO";
        return lastThumbnail;
    }

    @Benchmark
    public BufferedImage scalrTikaTransformer() throws IOException {
        lastDescription = "scalrTikaTransformer";
        Image source = ImageIO.read(getSoureceFile());
        BufferedImage output = new BufferedImage(source.getWidth(null),
                source.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        graphics.drawImage(source, null, null);
        graphics.dispose();
        lastThumbnail = Scalr.resize(output, thumbSize);
        return lastThumbnail;
    }

    public File getSoureceFile() {
        return new File(inputDir + filename);
    }
}