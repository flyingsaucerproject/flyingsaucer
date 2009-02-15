/*
 * Copyright (c) 2007 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the
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
 */


import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.simple.Graphics2DRenderer;
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
				renderer.setBufferedImageType(BufferedImage.TYPE_INT_RGB);
				BufferedImage image = renderer.getImage();

				FSImageWriter imageWriter = new FSImageWriter();
				String path = f.getAbsolutePath();
				path = path.substring(0, path.lastIndexOf("."));
				imageWriter.write(image, path + ".png");

				// compare to old
				BufferedImage img = Graphics2DRenderer.renderToImageAutoSize(f.toURI().toURL().toExternalForm(), 1024, BufferedImage.TYPE_INT_ARGB);
				ImageIO.write(img, "png", new File(path + "-G2DR.png"));

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
