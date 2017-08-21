package thumbnail;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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

    @State(Scope.Thread)
    public static class ThreadState {
        volatile ImageOutputWriter imageWriter = ImageOutputWriter.from(
                "/Users/aaronhoffer/Downloads/sample-images/",
                "/Users/aaronhoffer/Downloads/sample-images/output")
                .turnOffWriting();
    }

    @Param({"256"})
    public int thumbSize;

    // LARGE FILES ( > 1 MB)
    @Param({"building-30mb.jpg", "land-8mb.jpg", "robot-pic.jpg", "mountains-20mb.jpg",
            "crowd-3mb.jpg", "australia-250mb.png", "salt-lake-340mb.jpg"})
    String filename;

    // SMALL FILES ( < 1 MB)
    //    @Param({"unicorn-rainbow-57kb.gif", "land-100kb.jpg", "parliment-60kb.jpg", "city-300kb.jpg",
    //            "UN-bus-attack.jpg", "militants.jpg"})
    //    String filename;

    public static void main(String[] args) throws RunnerException {
        String simpleName = ThumbnailBenchmark.class.getSimpleName();
        Options opt = new OptionsBuilder().include(simpleName)
                .forks(2)
                .warmupIterations(1)
                .measurementIterations(4)
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

    }

    @TearDown
    public void teardown(ThreadState state) {
        state.imageWriter.end();
    }

    //    @Benchmark
    public BufferedImage subsamplingAggressiveThumbnailator(ThreadState state) throws IOException {
        Function<Integer, Integer> periodFunction = (dim) -> Math.toIntExact(Math.round(Math.ceil(
                dim / Math.pow(2, 8))));
        return state.imageWriter.setSourceFileAndLabel(filename, "subsamplingAGGRESSIVE")
                .setThumbnailAndReturn(Thumbnails.of(SampledImageReader.of(state.imageWriter.getSoureceFile())
                        .periodFromDimension(periodFunction)
                        .read())
                        .height(thumbSize)
                        .asBufferedImage());
    }

    //    @Benchmark
    //    public BufferedImage scalrSimple() throws IOException {
    //        outputWriter.setSourceFileAndLabel(filename, "scalr");
    //        return outputWriter.setThumbnailAndReturn(Scalr.resize(ImageIO.read(outputWriter.getSoureceFile()),
    //                thumbSize));
    //    }

    //    @Benchmark
    //    public BufferedImage thumbnailatorSimple() throws IOException {
    //        return outputWriter.next()
    //                .setSourceFileAndLabel(filename, "thumbnailator")
    //                .setThumbnailAndReturn(Thumbnails.of(inputDir + filename)
    //                        .height(thumbSize)
    //                        .asBufferedImage());
    //    }

    //    @Benchmark
    public BufferedImage subsamplingAutoThumbnailator(ThreadState state) throws IOException {
        return state.imageWriter.setSourceFileAndLabel(filename, "subsamplingAUTO")
                .setThumbnailAndReturn(Thumbnails.of(SampledImageReader.of(state.imageWriter.getSoureceFile())
                        .read())
                        .height(thumbSize)
                        .asBufferedImage());
    }

    //    @Benchmark
    //    public BufferedImage subsamplingAutoScalr() throws IOException {
    //        return outputWriter.next()
    //                .setSourceFileAndLabel(filename, "subsamplingAUTOscalr")
    //                .setThumbnailAndReturn(Scalr.resize(SampledImageReader.of(outputWriter.getSoureceFile())
    //                        .read(), thumbSize));
    //    }

    //    @Benchmark
    //    public BufferedImage subsampling16Scalr() throws IOException {
    //        return outputWriter.next()
    //                .setSourceFileAndLabel(filename, "subsampling16Scalr")
    //                .setThumbnailAndReturn(Scalr.resize(SampledImageReader.of(outputWriter.getSoureceFile())
    //                        .samplePeriod(16)
    //                        .read(), thumbSize));
    //    }

    @Benchmark
    public BufferedImage subsampling16Thumnbailator(ThreadState state) throws IOException {
        return state.imageWriter.setSourceFileAndLabel(filename, "subsampling16Thumbnailator")
                .setThumbnailAndReturn(Thumbnails.of(SampledImageReader.of(state.imageWriter.getSoureceFile())
                        .samplePeriod(16)
                        .read())
                        .height(thumbSize)
                        .asBufferedImage());
    }

    //    @Benchmark
    public BufferedImage scalrTikaTransformer(ThreadState state) throws IOException {
        state.imageWriter.setSourceFileAndLabel(filename, "scalrTikaTransformer");
        Image source = ImageIO.read(state.imageWriter.getSoureceFile());
        BufferedImage output = new BufferedImage(source.getWidth(null),
                source.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        graphics.drawImage(source, null, null);
        graphics.dispose();
        return state.imageWriter.setThumbnailAndReturn(Scalr.resize(output, thumbSize));
    }

}