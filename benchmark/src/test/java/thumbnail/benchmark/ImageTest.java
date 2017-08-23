package thumbnail.benchmark;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;

import org.junit.Assert;
import org.junit.Before;

import com.github.jaiimageio.jpeg2000.J2KImageReadParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReadParamJava;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;

import net.coobird.thumbnailator.Thumbnails;

public class ImageTest {

    @Before
    public void setup() throws FileNotFoundException {

        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());
    }

    //    @Test
    public void thumbnailator() throws IOException {
        for (int i = 0; i < 1000; ++i) {
            Thumbnails.of("/Users/aaronhoffer/Downloads/sample-images/land-8mb.jpg")
                    .height(200)
                    .toFile("/Users/aaronhoffer/Downloads/sample-images/output/land.jpg");
            Assert.assertNotNull(new Random().nextInt());
        }
    }

    //    @Test
    public void loadJ2() throws IOException {
        for (int i = 0; i < 1000; ++i) {
            ImageInputStream inputStream = ImageIO.createImageInputStream(new File(
                    "/Users/aaronhoffer/Downloads/sample-images/baghdad-j2k-20mb.jp2"));
            Iterator iter = ImageIO.getImageReaders(inputStream);
            ImageReader reader = (ImageReader) iter.next();
            J2KImageReadParam param = new J2KImageReadParamJava(reader.getDefaultReadParam());
            //            param.setDestination(new BufferedImage(1, 1, 1));
            //        param.setSourceSubsampling(32, 32, 0, 0);
            //            param.setSourceRegion(new Rectangle(0, 0, 1, 128));
            reader.setInput(inputStream, true, true);
            BufferedImage output = reader.read(0, param);
            //        System.err.println(String.format("HEIGHT=%s WIDTH=%s",
            //                output.getHeight(),
            //                output.getWidth()));
            Assert.assertTrue(Objects.nonNull(output));
        }
    }

}
