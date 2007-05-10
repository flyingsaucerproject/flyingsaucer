import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;

import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.simple.ImageRenderer;
import org.xhtmlrenderer.util.FSImageWriter;

/**
 */
public class ImageRender {
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
				Java2DRenderer renderer = new Java2DRenderer(f, 1024);
				renderer.setBufferedImageType(BufferedImage.TYPE_INT_ARGB);
				BufferedImage image = renderer.getImage();

				FSImageWriter imageWriter = new FSImageWriter();
				String path = f.getAbsolutePath();
				path = path.substring(0, path.lastIndexOf(".")) + ".png";
				imageWriter.write(image, path);
			} else {
				usage("File to render is not found: " + url);
			}
		} else {
			// ignore for now
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
