package org.xhtmlrenderer.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: Apr 6, 2007
 * Time: 6:10:52 PM
 *
 * @author pwright
 */
public class ImageUtil {
    public static final String DOWNSCALE_HIGH_QUALITY = "HIGH";
    public static final String DOWNSCALE_LOW_QUALITY = "MED";
    public static final String DOWNSCALE_FAST = "LOW";

    private static final Map qual;

    static {
        qual = new HashMap();
        qual.put(DOWNSCALE_FAST, new OldScaler());
        qual.put(DOWNSCALE_HIGH_QUALITY, new HighQualityScaler());
        qual.put(DOWNSCALE_LOW_QUALITY, new FastScaler());
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
     * @param orgImage     The image to scale
     * @param targetWidth  The target width in pixels
     * @param targetHeight The target height in pixels
     * @return The scaled image instance.
     */
    public static Image getScaledInstance(Image orgImage, int targetWidth, int targetHeight) {
        int w = orgImage.getWidth(null);
        int h = orgImage.getHeight(null);
        if (w == targetWidth && h == targetHeight) return orgImage;

        w = (targetWidth <= 0 ? w : targetWidth);
        h = (targetHeight <= 0 ? h : targetHeight);

        String downscaleQuality = Configuration.valueFor("xr.image.scale", DOWNSCALE_HIGH_QUALITY);
        Scaler scaler = (ImageUtil.Scaler) qual.get(downscaleQuality);

        Object hint = Configuration.valueFromClassConstant("xr.image.render-quality",
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        Image tmp = scaler.getScaledInstance(
                orgImage,
                BufferedImage.TYPE_INT_ARGB,
                hint,
                targetWidth,
                targetHeight
        );

        return tmp;
    }

    interface Scaler {
        /**
         * Convenience method that returns a scaled instance of the
         * provided {@code BufferedImage}, taken from article on java.net by Chris Campbell
         * http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html. Expects the image
         * to be fully loaded (e.g. no need to wait for loading on requesting height or width.
         *
         * @param img           the original image to be scaled
         * @param imageType     type of image from {@link java.awt.image.BufferedImage} (values starting with TYPE)
         * @param hint          one of the rendering hints that corresponds to
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
        Image getScaledInstance(Image img,
                                int imageType,
                                Object hint,
                                int targetWidth,
                                int targetHeight);
    }

    /**
     * Old AWT-style scaling, poor quality
     */
    static class OldScaler implements Scaler {
        public Image getScaledInstance(Image img, int imageType, Object hint, int targetWidth, int targetHeight) {
            return img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_FAST);
        }
    }

    /**
     * Fast but decent scaling
     */
    static class FastScaler implements Scaler {
        public Image getScaledInstance(Image img, int imageType, Object hint, int targetWidth, int targetHeight) {
            /*int type = (img.getTransparency() == Transparency.OPAQUE) ?
                    BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;*/
            int w, h;
            int imgw = img.getWidth(null);
            int imgh = img.getHeight(null);

            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;

            Image scaled = img;

            BufferedImage tmp = new BufferedImage(w, h, imageType);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(scaled, 0, 0, w, h, null);
            g2.dispose();

            scaled = tmp;
            return scaled;
        }
    }

    /**
     * Step-wise downscaling
     */
    static class HighQualityScaler implements Scaler {
        public Image getScaledInstance(Image img, int imageType, Object hint, int targetWidth, int targetHeight) {
            int w, h;
            int imgw = img.getWidth(null);
            int imgh = img.getHeight(null);

            // multi-pass only if higher quality requested and we are shrinking image
            if (imgw < targetWidth && imgh < targetHeight) {
                // Use multi-step technique: start with original size, then
                // scale down in multiple passes with drawImage()
                // until the target size is reached
                w = imgw;
                h = imgh;
            } else {
                // Use one-step technique: scale directly from original
                // size to target size with a single drawImage() call
                w = targetWidth;
                h = targetHeight;
            }

            Image scaled = img;

            do {
                if (w > targetWidth) {
                    w /= 2;
                    if (w < targetWidth) {
                        w = targetWidth;
                    }
                }

                if (h > targetHeight) {
                    h /= 2;
                    if (h < targetHeight) {
                        h = targetHeight;
                    }
                }

                BufferedImage tmp = new BufferedImage(w, h, imageType);
                Graphics2D g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
                g2.drawImage(scaled, 0, 0, w, h, null);
                g2.dispose();

                scaled = tmp;

            } while (w != targetWidth || h != targetHeight);
            return scaled;

        }
    }
}
