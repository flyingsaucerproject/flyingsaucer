/*
 * {{{ header & license
 * Copyright (c) 2007 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General P.ublic License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

/**
 * <p>Writes out BufferedImages to some outputstream, like a file. Allows image writer parameters to be specified and
 * thus controlled. Uses the java ImageIO libraries--see {@link javax.imageio.ImageIO} and related classes,
 * especially {@link javax.imageio.ImageWriter}.</p>
 * <p/>
 * By default, FSImageWriter writes BufferedImages out in PNG format. The simplest possible usage is
 * <pre>
 * FSImageWriter writer = new FSImageWriter();
 * writer.write(img, new File("image.png"));
 * </pre>
 * <p/>
 * <p>You can set the image format in the constructore ({@link org.xhtmlrenderer.util.FSImageWriter#FSImageWriter(String)},
 * and can set compression settings using various setters; this lets you create writer to reuse across a number
 * of images, all output at the same compression level. Note that not all image formats support compression. For
 * those that do, you may need to set more than one compression setting, in combination, for it to work. For JPG,
 * it might look like this</p>
 * <pre>
 *      writer = new FSImageWriter("jpg");
 * 		writer.setWriteCompressionMode(ImageWriteParam.MODE_EXPLICIT);
 * 		writer.setWriteCompressionType("JPEG");
 * 		writer.setWriteCompressionQuality(.75f);
 * </pre>
 * <p>The method {@link #newJpegWriter(float)} creates a writer for JPG images; you just need to specify the
 * output quality. Note that for the JPG format, your image or BufferedImage shouldn't be ARGB.</p>
 */
public class FSImageWriter {
    private String imageFormat;
    private float writeCompressionQuality;
    private int writeCompressionMode;
    private String writeCompressionType;
    public static final String DEFAULT_IMAGE_FORMAT = "png";


    /**
     * New image writer for the PNG image format
     */
    public FSImageWriter() {
        this("png");
    }

    /**
     * New writer for a given image format, using the informal format name.
     *
     * @param imageFormat Informal image format name, e.g. "jpg", "png", "bmp"; usually the part that appears
     *                    as the file extension.
     */
    public FSImageWriter(String imageFormat) {
        this.imageFormat = imageFormat;
        this.writeCompressionMode = ImageWriteParam.MODE_COPY_FROM_METADATA;
        this.writeCompressionType = null;
        this.writeCompressionQuality = 1.0f;
    }

    /**
     * Convenience method for initializing a writer for the JPEG image format.
     *
     * @param quality level of compression, between 0 and 1; 0 is lowest, 1 is highest quality.
     * @return a writer for JPEG images
     */
    public static FSImageWriter newJpegWriter(float quality) {
        FSImageWriter writer = new FSImageWriter("jpg");
        writer.setWriteCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writer.setWriteCompressionType("JPEG");
        writer.setWriteCompressionQuality(quality);
        return writer;
    }

    /**
     * Writes the image out to the target file, creating the file if necessary, or overwriting if it already
     * exists.
     *
     * @param bimg     Image to write.
     * @param filePath Path for file to write. The extension for the file name is not changed; it is up to the
     *                 caller to make sure this corresponds to the image format.
     * @throws IOException If the file could not be written.
     */
    public void write(BufferedImage bimg, String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("File " + filePath + " exists already, and call to .delete() failed " +
                        "unexpectedly");
            }
        } else {
            if (!file.createNewFile()) {
                throw new IOException("Unable to create file at path " + filePath + ", call to .createNewFile() " +
                        "failed unexpectedly.");
            }
        }

        OutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
        try {
            write(bimg, fos);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Writes the image out to the target file, creating the file if necessary, or overwriting if it already
     * exists.
     *
     * @param bimg     Image to write.
     * @param os outputstream to write to
     * @throws IOException If the file could not be written.
     */
    public void write(BufferedImage bimg, OutputStream os) throws IOException {
        ImageWriter writer = null;
        ImageOutputStream ios = null;
        try {
            writer = lookupImageWriterForFormat(imageFormat);
            ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);
            ImageWriteParam iwparam = getImageWriteParameters(writer);

            writer.write(null, new IIOImage(bimg, null, null), iwparam);
        } finally {
            if (ios != null) {
                try {
                    ios.flush();
                } catch (IOException e) {
                    // ignore
                }
                try {
                    ios.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (writer != null) {
                writer.dispose();
            }
        }
    }

    /**
     * Returns the image output parameters to control the output image quality, compression, etc. By default
     * this uses the compression values set in this class. Override this method to get full control over the
     * ImageWriteParam used in image output.
     *
     * @param writer The ImageWriter we are going to use for image output.
     * @return ImageWriteParam configured for image output.
     */
    protected ImageWriteParam getImageWriteParameters(ImageWriter writer) {
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            if (writeCompressionMode != ImageWriteParam.MODE_COPY_FROM_METADATA) {
                param.setCompressionMode(writeCompressionMode);

                // see docs for IWP--only allowed to set type and quality if mode is EXPLICIT
                if (writeCompressionMode == ImageWriteParam.MODE_EXPLICIT) {
                    param.setCompressionType(writeCompressionType);
                    param.setCompressionQuality(writeCompressionQuality);
                }

            }
        }

        return param;
    }

    /**
     * Compression quality for images to be generated from this writer. See
     * {@link javax.imageio.ImageWriteParam#setCompressionQuality(float)} for a description of what this means
     * and valid range of values.
     *
     * @param q Compression quality for image output.
     */
    public void setWriteCompressionQuality(float q) {
        writeCompressionQuality = q;
    }

    /**
     * Compression mode for images to be generated from this writer. See
     * {@link javax.imageio.ImageWriteParam#setCompressionMode(int)}  for a description of what this means
     * and valid range of values.
     *
     * @param mode Compression mode for image output.
     */
    public void setWriteCompressionMode(int mode) {
        this.writeCompressionMode = mode;
    }

    /**
     * Compression type for images to be generated from this writer. See
     * {@link javax.imageio.ImageWriteParam#setCompressionType(String)} for a description of what this means
     * and valid range of values.
     *
     * @param type Type of compression for image output.
     */
    public void setWriteCompressionType(String type) {
        this.writeCompressionType = type;
    }

    /**
     * Utility method to find an imagewriter.
     *
     * @param imageFormat String informal format name, "jpg"
     * @return ImageWriter corresponding to that format, null if not found.
     */
    private ImageWriter lookupImageWriterForFormat(String imageFormat) {
        ImageWriter writer = null;
        Iterator iter = ImageIO.getImageWritersByFormatName(imageFormat);
		if (iter.hasNext()) {
			writer = (ImageWriter) iter.next();
		}
		return writer;
	}
}
