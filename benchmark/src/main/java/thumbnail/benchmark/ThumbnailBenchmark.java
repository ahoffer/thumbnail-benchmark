package thumbnail.benchmark;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

import org.im4java.core.IM4JavaException;
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

import com.github.ahoffer.image.SampledImageReader;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;

import net.coobird.thumbnailator.Thumbnails;

// Website with large ortho images: https://apollomapping.com/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ThumbnailBenchmark {

    String inputDir = "/Users/aaronhoffer/Downloads/sample-images/";

    String outputDir = inputDir + "output_2017-08-21/";

    BufferedImage lastThumbnail;

    String lastDescription;

    Path tmpDir;

    @Param({"256"})
    public int thumbSize;

    // LARGE FILES ( > 1 MB)
    //    @Param({"robot-pic-700kb.jpg", "building-30mb.jpg", "land-8mb.jpg", "mountains-20mb.jpg",
    //            "crowd-3mb.jpg", "australia-250mb.png", "salt-lake-340mb.jpg"})
    //    String filename;

    //    @Param({"tank.jpg"})
    //    String filename;

    // SMALL FILES ( < 1 MB)
    //    @Param({"unicorn-rainbow-57kb.gif", "land-100kb.jpg", "parliment-60kb.jpg", "city-300kb.jpg",
    //            "UN-bus-attack.jpg", "militants.jpg"})
    //    String filename;

    // JPEG2000 FILES
    @Param({"baghdad-j2k-20mb.jp2", "carrots-j2k-8mb.j2k", "olso-j2k-19mb.jp2"})
    String filename;

    public static void main(String[] args) throws RunnerException {
        String simpleName = ThumbnailBenchmark.class.getSimpleName();
        Options opt = new OptionsBuilder().include(simpleName)
                .forks(1)
                .warmupIterations(1)
                .measurementIterations(3)
                .resultFormat(ResultFormatType.NORMALIZED_CSV)
                .addProfiler(NaiveHeapSizeProfiler.class)
                .addProfiler(GCProfiler.class)
                .include("imageMagick.*")
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() throws IOException {
        // Add a JPEG 2000 reader
        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());
        tmpDir = Files.createTempDirectory("image");
    }

    @TearDown
    public void teardown() throws IOException {
        //Save thumbnail as a PNG in the output directory
        ImageIO.write(lastThumbnail, "png", new File(outputDir + lastDescription + "-" + filename));
        lastDescription = null;
        lastThumbnail = null;
    }

    //    @Benchmark
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

    //    @Benchmark
    public BufferedImage subsamplingAutoThumbnailator() throws IOException {
        lastThumbnail = Thumbnails.of(SampledImageReader.of(getSoureceFile())
                .read())
                .height(thumbSize)
                .asBufferedImage();
        lastDescription = "subsamplingAUTO";
        return lastThumbnail;
    }

    //    @Benchmark
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

    @Benchmark
    public BufferedImage imageMagick() throws IOException, IM4JavaException, InterruptedException {
        //TODO: use stdin and stdout for IPC?
        lastDescription = "imageMagick";

        //        File tempSource = null;
        File tempOutput = null;

        Process proc;
        try {

            //tempSource = File.createTempFile("source", "");
            //copyFileUsingFileChannels(getSoureceFile(), tempSource);
            tempOutput = File.createTempFile("output", "");

            proc = new ProcessBuilder("/opt/local/bin/convert",
                    "-sample",
                    "1024x1024",
                    "-thumbnail",
                    "256x256",
                    // tempSource.getCanonicalPath(),
                    getSoureceFile().getCanonicalPath(),
                    tempOutput.getCanonicalPath()).start();

            proc.waitFor();
            lastThumbnail = ImageIO.read(tempOutput);

        } finally {
            //if (Objects.nonNull(tempSource)) {
            //                tempSource.delete();
            //            }
            if (Objects.nonNull(tempOutput)) {
                tempOutput.delete();
            }
        }
        return lastThumbnail;
    }

    public File getSoureceFile() {
        return new File(inputDir + filename);
    }

    //Parameters:
    //
    //          -ImgDir <directory>
    //              Image file Directory path
    //
    //          -OutFor <PBM|PGM|PPM|PNM|PAM|PGX|PNG|BMP|TIF|RAW|RAWL|TGA>
    //              REQUIRED only if -ImgDir is used
    //              Output format for decompressed images.
    //
    //          -i <compressed file>
    //              REQUIRED only if an Input image directory is not specified
    //              Currently accepts J2K-files, JP2-files and JPT-files. The file type
    //              is identified based on its suffix.
    //
    //          -o <decompressed file>
    //              REQUIRED
    //              Currently accepts formats specified above (see OutFor option)
    //              Binary data is written to the file (not ascii). If a PGX
    //              filename is given, there will be as many output files as there are
    //              components: an index starting from 0 will then be appended to the
    //              output filename, just before the "pgx" extension. If a PGM filename
    //              is given and there are more than one component, only the first component
    //              will be written to the file.
    //
    //          -r <reduce factor>
    //              Set the number of highest resolution levels to be discarded. The
    //              image resolution is effectively divided by 2 to the power of the
    //              number of discarded levels. The reduce factor is limited by the
    //              smallest total number of decomposition levels among tiles.
    //
    //          -l <number of quality layers to decode>
    //              Set the maximum number of quality layers to decode. If there are
    //              less quality layers than the specified number, all the quality layers
    //              are decoded.
    //          -x  Create an index file *.Idx (-x index_name.Idx)
    //
    //          -d <x0,y0,x1,y1>
    //              OPTIONAL
    //              Decoding area
    //              By default all the image is decoded.
    //
    //          -t <tile_number>
    //              OPTIONAL
    //              Set the tile number of the decoded tile. Follow the JPEG2000 convention from left-up to bottom-up
    //              By default all tiles are decoded.
    //
    //          -p <comp 0 precision>[C|S][,<comp 1 precision>[C|S][,...]]
    //              OPTIONAL
    //              Force the precision (bit depth) of components.
    //              There shall be at least 1 value. Theres no limit on the number of values (comma separated, last values ignored if too much values).
    //              If there are less values than components, the last value is used for remaining components.
    //              If 'C' is specified (default), values are clipped.
    //              If 'S' is specified, values are scaled.
    //              A 0 value can be specified (meaning original bit depth).
    //
    //          -force-rgb
    //              Force output image colorspace to RGB
    //
    //          -upsample
    //              Downsampled components will be upsampled to image size
    //
    //          -split-pnm
    //              Split output components to different files when writing to PNM
    //
    //          -threads <num_threads>
    //              Number of threads to use for decoding.
    //
    //          -quiet
    //            Disable output from the library and other output.

    void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }
}