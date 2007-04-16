package org.xhtmlrenderer.simple;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;

import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.ImageUtil;


/**
 * <p/>
 * ImageRenderer supports headless rendering of XHTML documents, outputting
 * to an image format. There are two static utility methods, one for rendering
 * a {@link java.net.URL}, {@link #renderToImage(String,String)} and one
 * for rendering a {@link java.io.File}, {@link #renderToImage(java.io.File,String)}</p>
 * <p/>
 * <p>You can use this utility from the command line by passing in
 * the URL or file location as first parameter, and output file path as second
 * parameter:
 * <pre>
 * java -cp %classpath% org.xhtmlrenderer.simple.ImageRenderer <url> <img>
 * </pre>
 * <p>If the second parameters is not provided, a PNG-format image will be created
 * in the same directory as the source (if source is a file) or as a temp file
 * in the standard temp directory; the output file name will be printed out
 * in either case.</p>
 * <p>Image width must always be supplied; height is determined automatically.</p>
 *
 * @author Pete Brant
 * @author Patrick Wright
 */
public class ImageRenderer {
    private static final String DEFAULT_IMAGE_FORMAT = "png";

    /**
     * Renders the XML file at the given URL as an image file
     * at the target location.
     *
     * @param url     url for the XML file to render
     * @param outFile path to the PDF file to create
     * @throws java.io.IOException if the URL or PDF location is
     *                             invalid
     * @throws com.lowagie.text.DocumentException
     *                             if an error occurred
     *                             while building the Document.
     */
    public static BufferedImage renderToImage(String url, String outFile, int width)
            throws IOException {

        Java2DRenderer renderer = new Java2DRenderer(url, url);
        return doRenderToImage(renderer, outFile, width);
    }

    /**
     * Renders the XML file as an image file at the target location.
     *
     * @param inFile  XML file to render
     * @param outFile path to the image file to create
     * @throws java.io.IOException if the file or image location is invalid
     */
    public static BufferedImage renderToImage(File inFile, String outFile, int width)
            throws IOException {

        Java2DRenderer renderer = new Java2DRenderer(inFile);
        return doRenderToImage(renderer, outFile, width);
    }

    /**
     * Internal use, runs the render process
     */
    private static BufferedImage doRenderToImage(Java2DRenderer renderer, String img, int width)
            throws IOException {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(img));

            BufferedImage image = renderer.getImage(width);
            ImageIO.write(image, DEFAULT_IMAGE_FORMAT, os);

            os.close();
            os = null;
            return image;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Renders a file or URL to an image file. Command line use: first
     * argument is URL or file path, second argument is path to image file to generate.
     *
     * @param args see desc
     * @throws java.io.IOException if source could not be read, or if image path is invalid
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            usage("Incorrect argument list.");
        }
        String url = args[0];
        if (url.indexOf("://") == -1) {
            // maybe it's a file
            File f = new File(url);
            if (f.exists()) {
                String output = f.getAbsolutePath();
                output = output.substring(0, output.lastIndexOf(".")) + ".png";
                System.out.println("Saving image to " + output);
                BufferedImage image = renderToImage(f, output, 1024);
                thumbnails(image);
            } else {
                usage("File to render is not found: " + url);
            }
        } else {
            File out = File.createTempFile("fs", ".png");
            System.out.println("Saving image to " + out.getAbsolutePath());
            renderToImage(url, out.getAbsolutePath(), 1024);
        }
    }

    // TODO later: give users a way to specify target thumbnails to generate from the base image
    private static void thumbnails(BufferedImage image) throws IOException {
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        System.out.println("org " + w + " " + h);
        for ( int i=90; i >= 10; i -= 10 ) {
            int sw = (int) (w * i / 100);
            int sh = (int) (h * i / 100);
            System.out.println(sw + " " + sh + " " + i);
            BufferedImage i2 = (BufferedImage) ImageUtil.getScaledInstance(image, sw, sh);
            ImageIO.write(i2, "png", new File("x-" + i + ".png"));
        }
    }

    /**
     * prints out usage information, with optional error message
     */
    private static void usage(String err) {
        if (err != null && err.length() > 0) {
            System.err.println("==>" + err);
        }
        System.err.println("Usage: ... [url]");
        System.exit(1);
    }
}