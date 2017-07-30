package thumbnail;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.spi.IIORegistry;

import org.imgscalr.Scalr;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;

@State(Scope.Benchmark)
public class Scratch {

    public static void main(String[] args) throws RunnerException {
        String simpleName = Scratch.class.getSimpleName();
        Options opt = new OptionsBuilder().include(simpleName)
                .forks(1)
                .warmupIterations(0)
                .measurementIterations(1)
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() throws FileNotFoundException {

        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());
    }

    @Benchmark
    public BufferedImage test() throws IOException {
        BufferedImage bufferedImage = new ThumbnailBenchmark().getSubsampledImage(
                "/Users/aaronhoffer/Downloads/sample-images/salt-lake-340mb.jp2",
                128);
        return Scalr.resize(bufferedImage, 200);

    }
}
