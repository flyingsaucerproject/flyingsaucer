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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import javax.imageio.ImageIO;

/**
 * Static utility methods for working with images. Meant to suggest "best practices" for the most straightforward
 * cases of working with images.
 *
 * @author pwright
 */
public class ImageUtil {

    private static final Map qual;

    static {
        qual = new HashMap();
        qual.put(DownscaleQuality.FAST, new OldScaler());
        qual.put(DownscaleQuality.HIGH_QUALITY, new HighQualityScaler());
        qual.put(DownscaleQuality.LOW_QUALITY, new FastScaler());
        qual.put(DownscaleQuality.AREA, new AreaAverageScaler());
    }

    /**
     * Sets the background of the image to the specified color
     *
     * @param image the image
     * @param bgColor the color
     */
    public static void clearImage(BufferedImage image, Color bgColor) {
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.dispose();
    }

    /**
     * Sets the background of the image to white.
     *
     * @param image the image
     */
    public static void clearImage(BufferedImage image) {
        clearImage(image, Color.WHITE);
    }

    public static BufferedImage makeCompatible(BufferedImage bimg) {
        BufferedImage cimg = null;
        if (GraphicsEnvironment.isHeadless()) {
            cimg = createCompatibleBufferedImage(bimg.getWidth(), bimg.getHeight(), bimg.getTransparency());
        } else {
            GraphicsConfiguration gc = getGraphicsConfiguration();
            if (bimg.getColorModel().equals(gc.getColorModel())) {
                return bimg;
            }
            cimg = gc.createCompatibleImage(bimg.getWidth(), bimg.getHeight(), bimg.getTransparency());
        }

        Graphics cg = cimg.getGraphics();
        cg.drawImage(bimg, 0, 0, null);
        cg.dispose();
        return cimg;
    }

    /**
     * Helper method to instantiate new BufferedImages; if the graphics environment is actually connected to real
     * screen devices (e.g. not in headless mode), the image will be compatible with the screen device allowing
     * for best performance. In a headless environment, simply creates a new BufferedImage. For non-headless
     * environments, this just sets up and calls
     * {@link java.awt.GraphicsConfiguration#createCompatibleImage(int,int,int)}. The image will not have anything
     * drawn to it, not even a white background; you must do this yourself. The {@link #clearBackground(BufferedImage)}
     * method will do this for you if you like.
     *
     * @param width  Target width for the image
     * @param height Target height for the image
     * @param biType Value from the {@link java.awt.image.BufferedImage} class; see docs for
     *               {@link java.awt.image.BufferedImage#BufferedImage(int,int,int)}. The actual type used will
     *               be the type specified in this parameter, if in headless mode, or the type most compatible with the screen, if
     *               in non-headless more.
     * @return A BufferedImage compatible with the screen (best fit).
     */
    public static BufferedImage createCompatibleBufferedImage(int width, int height, int biType) {
        BufferedImage bimage = null;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            bimage = new BufferedImage(width, height, biType);
        } else {
            GraphicsConfiguration gc = getGraphicsConfiguration();

            // TODO: check type using image type - can be sniffed; see Filthy Rich Clients
            int type = (biType == BufferedImage.TYPE_INT_ARGB || biType == BufferedImage.TYPE_INT_ARGB_PRE ?
					Transparency.TRANSLUCENT : Transparency.OPAQUE);

            bimage = gc.createCompatibleImage(width, height, type);
        }

        return bimage;
    }

    private static GraphicsConfiguration getGraphicsConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
        return gc;
    }

    /**
     * Creates a BufferedImage compatible with the local graphics environment; this is a helper method for a
     * common process and just sets up and calls
     * {@link java.awt.GraphicsConfiguration#createCompatibleImage(int,int,int)}. The image will support
     * transparent pixels.
     *
     * @param width  Target width for the image
     * @param height Target height for the image
     * @return A BufferedImage compatible with the screen (best fit) supporting transparent pixels.
     */
    public static BufferedImage createCompatibleBufferedImage(int width, int height) {
        return createCompatibleBufferedImage(width, height, Transparency.BITMASK);
    }

    /**
     * Scales an image to the requested width and height, assuming these are both >= 1; size given in pixels.
     * If either width or height is <=0, the current image width or height will be used. This method assumes
     * that, at the moment the method is called, the width and height of the image are available; it won't wait for
     * them. Therefore, the method should be called once the image has completely loaded and not before.
     * <p/>
     * Override this method in a subclass to optimize image scaling operations; note that the legacy
     * {@link java.awt.Image#getScaledInstance(int,int,int)} is considered to perform poorly compared to more
     * recent developed techniques.
     * <p/>
     * For a discussion of the options from a member of the Java2D team, see
     * http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
     *
     * @param orgImage The image to scale
     * @return The scaled image instance.
     */
    public static BufferedImage getScaledInstance(ScalingOptions opt, BufferedImage orgImage) {
        int w = orgImage.getWidth(null);
        int h = orgImage.getHeight(null);

        if (opt.sizeMatches(w, h)) {
            return orgImage;
        }

        w = (opt.getTargetWidth() <= 0 ? w : opt.getTargetWidth());
        h = (opt.getTargetHeight() <= 0 ? h : opt.getTargetHeight());

        Scaler scaler = (ImageUtil.Scaler) qual.get(opt.getDownscalingHint());
        opt.setTargetWidth(w);
        opt.setTargetHeight(h);

        return scaler.getScaledInstance(orgImage, opt);
    }

    /**
     * Scales an image to the requested width and height, assuming these are both >= 1; size given in pixels.
     * If either width or height is <=0, the current image width or height will be used. This method assumes       y
     * that, at the moment the method is called, the width and height of the image are available; it won't wait for
     * them. Therefore, the method should be called once the image has completely loaded and not before.
     * <p/>
     * Override this method in a subclass to optimize image scaling operations; note that the legacy
     * {@link java.awt.Image#getScaledInstance(int,int,int)} is considered to perform poorly compared to more
     * recent developed techniques.
     * <p/>
     * For a discussion of the options from a member of the Java2D team, see
     * http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
     *
     * @param orgImage	 The image to scale
     * @param targetWidth  The target width in pixels
     * @param targetHeight The target height in pixels
     * @return The scaled image instance.
     */
    public static BufferedImage getScaledInstance(BufferedImage orgImage, int targetWidth, int targetHeight) {
        String downscaleQuality = Configuration.valueFor("xr.image.scale", DownscaleQuality.HIGH_QUALITY.asString());
        DownscaleQuality quality = DownscaleQuality.forString(downscaleQuality, DownscaleQuality.HIGH_QUALITY);

        Object hint = Configuration.valueFromClassConstant("xr.image.render-quality",
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        ScalingOptions opt = new ScalingOptions(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB, quality, hint);

        return getScaledInstance(opt, orgImage);
    }

    /**
     * Scales one image to multiple dimensions, using the same ScalingOptions for each. The method follows the same
     * process for scaling as {@link #getScaledInstance(ScalingOptions,java.awt.Image)}.
     *
     * @param opt		Options to apply to control scaling process.
     * @param img		The original image to scale
     * @param dimensions List of dimensions to scale to; one output image will be produced for each dimension. Will
     *                   not check for duplicate dimensions.
     * @return List of buffered images in the given dimensions.
     */
    public static java.util.List scaleMultiple(ScalingOptions opt, BufferedImage img, java.util.List dimensions) {
        java.util.List scaledImages = new ArrayList(dimensions.size());

        Iterator iter = dimensions.iterator();
        while (iter.hasNext()) {
            Dimension dim = (Dimension) iter.next();
            opt.setTargetDimensions(dim);

            BufferedImage scaled = getScaledInstance(opt, img);

            scaledImages.add(scaled);
        }
        return scaledImages;
    }

    /**
     * Utility method to convert an AWT Image to a BufferedImage. Size is preserved, BufferedImage is compatible
     * with current display device.
     *
     * @param awtImg image to convert; if already a BufferedImage, returned unmodified
     * @param type   the type of BufferedImage to create; see
     *               {@link java.awt.image.BufferedImage#BufferedImage(int,int,int)}
     * @return BufferedImage with same content.
     */
    public static BufferedImage convertToBufferedImage(Image awtImg, int type) {
        BufferedImage bimg;
        if (awtImg instanceof BufferedImage) {
            bimg = (BufferedImage) awtImg;
        } else {
            bimg = createCompatibleBufferedImage(awtImg.getWidth(null), awtImg.getHeight(null), type);
            Graphics2D g = bimg.createGraphics();
            g.drawImage(awtImg, 0, 0, null, null);
            g.dispose();
        }
        return bimg;
    }

    public static BufferedImage createTransparentImage(int width, int height) {
        BufferedImage bi = createCompatibleBufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();

        // Make all filled pixels transparent
        Color transparent = new Color(0, 0, 0, 0);
        g2d.setColor(transparent);
        g2d.setComposite(AlphaComposite.Src);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return bi;
    }
    
    /**
     * Detect if an URI represents an embedded base 64 image.
     *
     * @param uri URI of the image
     * @return A boolean
     */
    public static boolean isEmbeddedBase64Image(String uri) {
        return (uri != null && uri.startsWith("data:image/"));
    }
    
    /**
     * Get the binary content of an embedded base 64 image.
     *
     * @param imageDataUri URI of the embedded image
     * @return The binary content
     */
    public static byte[] getEmbeddedBase64Image(String imageDataUri) {
        int b64Index = imageDataUri.indexOf("base64,");
        if (b64Index != -1) {
            String b64encoded = imageDataUri.substring(b64Index + "base64,".length());
            return Base64.getDecoder().decode(b64encoded);
        } else {
            XRLog.load(Level.SEVERE, "Embedded XHTML images must be encoded in base 64.");
        }
        return null;
    }
    
    /**
     * Get the BufferedImage of an embedded base 64 image.
     *
     * @param imageDataUri URI of the embedded image
     * @return The BufferedImage
     */
    public static BufferedImage loadEmbeddedBase64Image(String imageDataUri) {
        try {
            byte[] buffer = getEmbeddedBase64Image(imageDataUri);
            if (buffer != null) {
                return ImageIO.read(new ByteArrayInputStream(buffer));
            }
        } catch (IOException ex) {
            XRLog.exception("Can't read XHTML embedded image", ex);
        }
        return null;
    }
    
    interface Scaler {
        /**
         * Convenience method that returns a scaled instance of the
         * provided {@code BufferedImage}, taken from article on java.net by Chris Campbell
         * http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html. Expects the image
         * to be fully loaded (e.g. no need to wait for loading on requesting height or width.
         *
         * @param img		the original image to be scaled
         * @param imageType	type of image from {@link java.awt.image.BufferedImage} (values starting with TYPE)
         * @param hint		one of the rendering hints that corresponds to
         *                      {@code RenderingHints.KEY_INTERPOLATION} (e.g.
         *                      {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
         *                      {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
         *                      {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
         * @param higherQuality if true, this method will use a multi-step
         *                      scaling technique that provides higher quality than the usual
         *                      one-step technique (only useful in downscaling cases, where
         *                      {@code targetWidth} or {@code targetHeight} is
         *                      smaller than the original dimensions, and generally only when
         *                      the {@code BILINEAR} hint is specified)
         * @param targetWidth   the desired width of the scaled instance,
         *                      in pixels
         * @param targetHeight  the desired height of the scaled instance,
         *                      in pixels
         * @return a scaled version of the original {@code BufferedImage}
         */
        BufferedImage getScaledInstance(BufferedImage img, ScalingOptions opt);
    }

    abstract static class AbstractFastScaler implements Scaler {
        public BufferedImage getScaledInstance(BufferedImage img, ScalingOptions opt) {
            // target is always >= 1
            Image scaled = img.getScaledInstance(opt.getTargetWidth(), opt.getTargetHeight(), getImageScalingMethod());

            return ImageUtil.convertToBufferedImage(scaled, img.getType());
        }

        abstract protected int getImageScalingMethod();
    }

    /**
     * Old AWT-style scaling, poor quality
     */
    static class OldScaler extends AbstractFastScaler {
        protected int getImageScalingMethod() {
            return Image.SCALE_FAST;
        }
    }

    /**
     * AWT-style one-step scaling, using area averaging
     */
    static class AreaAverageScaler extends AbstractFastScaler {
        protected int getImageScalingMethod() {
            return Image.SCALE_AREA_AVERAGING;
        }
    }

    /**
     * Fast but decent scaling
     */
    static class FastScaler implements Scaler {
        public BufferedImage getScaledInstance(BufferedImage img, ScalingOptions opt) {
            int w, h;

            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = opt.getTargetWidth();
            h = opt.getTargetHeight();

            BufferedImage scaled = ImageUtil.createCompatibleBufferedImage(w, h, img.getType());
            Graphics2D g2 = scaled.createGraphics();
            opt.applyRenderingHints(g2);
            g2.drawImage(img, 0, 0, w, h, null);
            g2.dispose();

            return scaled;
        }
    }

    /**
     * Step-wise downscaling
     */
    static class HighQualityScaler implements Scaler {
        public BufferedImage getScaledInstance(BufferedImage img, ScalingOptions opt) {
            int w, h;
            int imgw = img.getWidth(null);
            int imgh = img.getHeight(null);

            // multi-pass only if higher quality requested and we are shrinking image
            if (opt.getTargetWidth() < imgw && opt.getTargetHeight() < imgh) {
                // Use multi-step technique: start with original size, then
                // scale down in multiple passes with drawImage()
                // until the target size is reached
                w = imgw;
                h = imgh;
            } else {
                // Use one-step technique: scale directly from original
                // size to target size with a single drawImage() call
                w = opt.getTargetWidth();
                h = opt.getTargetHeight();
            }

            BufferedImage scaled = img;

            do {
                if (w > opt.getTargetWidth()) {
                    w /= 2;
                    if (w < opt.getTargetWidth()) {
                        w = opt.getTargetWidth();
                    }
                }

                if (h > opt.getTargetHeight()) {
                    h /= 2;
                    if (h < opt.getTargetHeight()) {
                        h = opt.getTargetHeight();
                    }
                }

                BufferedImage tmp = ImageUtil.createCompatibleBufferedImage(w, h, img.getType());
                Graphics2D g2 = tmp.createGraphics();
                opt.applyRenderingHints(g2);
                g2.drawImage(scaled, 0, 0, w, h, null);
                g2.dispose();

                scaled = tmp;

            } while (w != opt.getTargetWidth() || h != opt.getTargetHeight());
            return scaled;
        }
    }
}
