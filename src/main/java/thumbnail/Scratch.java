package thumbnail;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.spi.IIORegistry;

import org.imgscalr.Scalr;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotMemoryProfiler;
import org.openjdk.jmh.profile.NaiveHeapSizeProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;

@State(Scope.Benchmark)
public class Scratch {

    @Param({"256"})
    public int thumbSize;

    // Website with large ortho images: https://apollomapping.com/
    @Param({"baghdad-j2k-20mb.jp2"})
    String filename;

    String inputDir = "/Users/aaronhoffer/Downloads/sample-images/";

    private TestRun testRun;

    public static void main(String[] args) throws RunnerException {
        String simpleName = Scratch.class.getSimpleName();
        Options opt = new OptionsBuilder().include(simpleName)
                .forks(0)
                .warmupIterations(0)
                .measurementIterations(1)
                .resultFormat(ResultFormatType.NORMALIZED_CSV)
                .addProfiler(GCProfiler.class)
                .output("scratch.csv")
                .addProfiler(HotspotMemoryProfiler.class)
                .addProfiler(NaiveHeapSizeProfiler.class)
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public BufferedImage jpeg2000() throws IOException {
        testRun.setSourceFileAndLabel(filename, "jpeg2000");
        BufferedImage output = Scalr.resize(testRun.getSubsampledImage(16), thumbSize);
        return testRun.setThumbnailAndReturn(output);
    }

    @Setup
    public void setup() throws FileNotFoundException {
        testRun = TestRun.from(inputDir, inputDir + "output/");

        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());

    }

    @TearDown
    public void teardown() {
        testRun.end();
    }

}
