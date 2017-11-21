package thumbnail.benchmark;

import com.github.jaiimageio.jpeg2000.J2KImageReadParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReadParamJava;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.collections4.MapUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ImageTest {

  @Before
  public void setup() throws FileNotFoundException {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  @Ignore
  @Test
  public void mapDebugPrint() {
    Map<String, String> map = new HashMap<>();
    map.put("foo", "bar");
    MapUtils.debugPrint(System.out, "test", map);
  }

  @Ignore
  @Test
  public void mapVerbosePrint() {
    Map<String, String> map = new HashMap<>();
    map.put("foo", "bar");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps;
    try {
      ps = new PrintStream(baos, true, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    MapUtils.verbosePrint(ps, "aaron", map);
    String message = new String(baos.toByteArray(), StandardCharsets.UTF_8);
  }

  @Ignore
  @Test
  public void thumbnailator() throws IOException {
    for (int i = 0; i < 1000; ++i) {
      Thumbnails.of("/Users/aaronhoffer/Downloads/sample-images/land-8mb.jpg")
          .height(200)
          .toFile("/Users/aaronhoffer/Downloads/sample-images/output/land.jpg");
      Assert.assertNotNull(new Random().nextInt());
    }
  }

  @Ignore
  @Test
  public void loadJ2() throws IOException {
    for (int i = 0; i < 1000; ++i) {
      ImageInputStream inputStream =
          ImageIO.createImageInputStream(
              new File("/Users/aaronhoffer/Downloads/sample-images/baghdad-j2k-20mb.jp2"));
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
