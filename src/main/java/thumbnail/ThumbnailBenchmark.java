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

    @Param({"256"})
    public int thumbSize;

    @Param({"building-30mb.jpg", "crowd-3mb.jpg", "land-100kb.jpg", "australia-250mb.png"})
    String filename;

    String inputDir = "/Users/aaronhoffer/Downloads/sample-images/";

    //    @Param({"land-100kb.jpg", "crowd-3mb.jpg", "land-8mb.jpg",
    //            "building-30mb.jpg", "mountains-20mb.jpg", "baghdad-j2k-20mb.jp2", "olso-j2k-19mb.jp2",
    //            "australia-250mb.png", "salt-lake-340mb.jpg"})
    //    String filename;

    private TestRun testRun;

    public static void main(String[] args) throws RunnerException {
        String simpleName = ThumbnailBenchmark.class.getSimpleName();
        Options opt = new OptionsBuilder().include(simpleName)
                .forks(1)
                .warmupIterations(1)
                .measurementIterations(4)
                //                .jvmArgsAppend("-Xms2g")
                .resultFormat(ResultFormatType.NORMALIZED_CSV)
                .addProfiler(NaiveHeapSizeProfiler.class)
                .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() throws FileNotFoundException {
        //         Add a JPEG 2000 reader
        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());
        testRun = TestRun.from(inputDir, inputDir + "output/");
    }

    @TearDown
    public void teardown() {
        testRun.end();
    }

    //    @Benchmark
    public BufferedImage scalrSimple() throws IOException {
        testRun.setSourceFileAndLabel(filename, "scalr");
        return testRun.setThumbnailAndReturn(Scalr.resize(ImageIO.read(testRun.getSoureceFile()),
                thumbSize));
    }

    @Benchmark
    public BufferedImage thumbnailatorSimple() throws IOException {
        testRun.setSourceFileAndLabel(filename, "thumbnailator");
        return testRun.setThumbnailAndReturn(Thumbnails.of(inputDir + filename)
                .height(thumbSize)
                .asBufferedImage());
    }

    @Benchmark
    public BufferedImage subsamplingAutoThumbnailator() throws IOException {
        testRun.setSourceFileAndLabel(filename, "subsamplingAUTOthumbnailator");
        return testRun.setThumbnailAndReturn(Thumbnails.of(SampledImageReader.of(testRun.getSoureceFile())
                .read())
                .height(thumbSize)
                .asBufferedImage());
    }

    //    @Benchmark
    public BufferedImage subsamplingAutoScalr() throws IOException {
        testRun.setSourceFileAndLabel(filename, "subsamplingAUTOscalr");
        return testRun.setThumbnailAndReturn(Scalr.resize(SampledImageReader.of(testRun.getSoureceFile())
                .read(), thumbSize));
    }

    //    @Benchmark
    public BufferedImage subsampling16Scalr() throws IOException {
        testRun.setSourceFileAndLabel(filename, "subsampling16Scalr");
        return testRun.setThumbnailAndReturn(Scalr.resize(SampledImageReader.of(testRun.getSoureceFile())
                .samplePeriod(16)
                .read(), thumbSize));
    }

    @Benchmark
    public BufferedImage subsampling16Thumnbailator() throws IOException {
        testRun.setSourceFileAndLabel(filename, "subsampling16Thumbnailator");
        return testRun.setThumbnailAndReturn(Thumbnails.of(SampledImageReader.of(testRun.getSoureceFile())
                .samplePeriod(16)
                .read())
                .height(thumbSize)
                .asBufferedImage());
    }

    @Benchmark
    public BufferedImage scalrTikaTransformer() throws IOException {
        testRun.setSourceFileAndLabel(filename, "scalrTikaTransformer");
        Image source = ImageIO.read(new File(inputDir + filename));
        BufferedImage output = new BufferedImage(source.getWidth(null),
                source.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        graphics.drawImage(source, null, null);
        graphics.dispose();
        return testRun.setThumbnailAndReturn(Scalr.resize(output, thumbSize));
    }
}