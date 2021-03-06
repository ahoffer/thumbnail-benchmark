/**
 * This class wrap and image reader so that an image can be subsampled before
 * it is used. It uses a fluent interface. Creat an intance using the factory method
 * of(). It will compute a sampling rate if the image size can be determined. To override
 * the sampling rate, use the method samplePeriod() and pass it an integer. E.g. is sampling
 * period is set to 2, every second pixel will be sampled.
 * <p>
 * After calling read(), the image reader and stream are closed and the read() method cannot
 * be called again.
 */
package com.github.ahoffer.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class SampledImageReader {

    //TODO: Maybe change some of these fields to Optional value holders.
    protected int samplePeriod;

    int imageIndex;

    ImageReader reader;

    InputStream source;

    private int subsamplingHint = 256;

    static public SampledImageReader of(InputStream source) throws IOException {
        SampledImageReader object = new SampledImageReader();
        object.source = source;
        return object.init();
    }

    static public SampledImageReader of(File sourceFile) throws IOException {
        SampledImageReader object = new SampledImageReader();
        object.source = new FileInputStream(sourceFile);
        return object.init();
    }

    protected SampledImageReader init() throws IOException {
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(source);
        Iterator iter = ImageIO.getImageReaders(imageInputStream);
        reader = (ImageReader) iter.next();
        reader.setInput(imageInputStream);
        return this;
    }

    public SampledImageReader subsamplingHint(int hint) {
        subsamplingHint = hint;
        return this;
    }

    public SampledImageReader imageIndex(int index) {
        imageIndex = index;
        return this;
    }

    public SampledImageReader samplePeriod(int period) {
        samplePeriod = period;
        return this;
    }

    public int computeSamplingPeriod() {
        if (samplePeriod == 0) {
            try {
                int longestDimensionSize = Math.max(reader.getWidth(imageIndex),
                        reader.getHeight(imageIndex));
                samplePeriod = (int) (Math.round(Math.ceil(
                        longestDimensionSize / (double) subsamplingHint)));

            } catch (IOException e) {
                //Give up. Do not sub-sample the image.
                samplePeriod = 1;
            }
        }
        return samplePeriod;
    }

    public BufferedImage read() throws IOException {
        BufferedImage image;
        int columnOffset = 0;
        int rowOffset = 0;
        // Use the same sampling period for both rows and columns to preserve images's
        // aspect ratio.
        int columnSamplingPeriod = computeSamplingPeriod();
        int rowSamplingPeriod = computeSamplingPeriod();
        ImageReadParam imageParam = reader.getDefaultReadParam();
        try {
            imageParam.setSourceSubsampling(columnSamplingPeriod,
                    rowSamplingPeriod,
                    columnOffset,
                    rowOffset);
            image = reader.read(imageIndex, imageParam);
        } finally {
            source.close();
            reader.dispose();
        }

        return image;
    }
}
