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
package org.xhtmlrenderer.simple;

import java.awt.image.BufferedImage;
import java.io.*;

import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.FSImageWriter;


/**
 * <p/>
 * ImageRenderer supports rendering of XHTML documents to image formats, writing out the generated image to an outputstream
 * or a file in a given image formate. There are two static utility methods, one for rendering
 * a {@link java.net.URL}, {@link #renderToImage(String,String)} and one
 * for rendering a {@link java.io.File}, {@link #renderToImage(java.io.File,String)}</p>
 *
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
 *
 * <p>Image width must always be supplied; height is determined automatically.</p>
 *
 * @see org.xhtmlrenderer.simple.PDFRenderer
 * @author Pete Brant
 * @author Patrick Wright
 */
public class ImageRenderer {
	public static final int DEFAULT_WIDTH = 1024;

	/**
	 * Renders the XML file at the given URL as an image file at the target location. Width must be provided,
	 * height is determined automatically based on content and CSS.
	 *
	 * @param url	 url for the XML file to render
	 * @param path path to the PDF file to create
	 * @param width Width in pixels to which the document should be constrained.
	 *
	 * @throws java.io.IOException if the input URL, or output path location is invalid
	 */
	public static BufferedImage renderToImage(String url, String path, int width) throws IOException {
		return renderImageToOutput(url, new FSImageWriter(), path, width);
	}

	/**
	 * Renders the XML file at the given URL as an image file at the target location.
	 *
	 * @param url	 url for the XML file to render
	 * @param path path to the PDF file to create
	 * @param width Width in pixels to which the document should be constrained.
	 * @param height Height in pixels to which the document should be constrained.
	 *
	 * @throws java.io.IOException if the input URL, or output path location is invalid
	 */
	public static BufferedImage renderToImage(String url, String path, int width, int height) throws IOException {
		return renderImageToOutput(url, new FSImageWriter(), path, width);
	}

	/**
	 * Renders the XML file as an image file at the target location. Width must be provided, height is determined
	 * automatically based on content and CSS.
	 *
	 * @param inFile  XML file to render
	 * @param path path to the image file to create
	 * @param width Width in pixels to which the document should be constrained.
	 *
	 * @throws java.io.IOException if the input URL, or output path location is invalid
	 */
	public static BufferedImage renderToImage(File inFile, String path, int width) throws IOException {
		return renderToImage(inFile.toURI().toURL().toExternalForm(), path, width);
	}

	/**
	 * Renders the XML file as an image file at the target location. Width must be provided, height is determined
	 * automatically based on content and CSS.
	 *
	 * @param inFile  XML file to render
	 * @param path path to the image file to create
	 * @param width Width in pixels to which the document should be constrained.
	 * @param height Height in pixels to which the document should be constrained.
	 *
	 * @throws java.io.IOException if the input URL, or output path location is invalid
	 */
	public static BufferedImage renderToImage(File inFile, String path, int width, int height) throws IOException {
		return renderToImage(inFile.toURI().toURL().toExternalForm(), path, width, height);
	}

	/**
	 * Renders a document at a given URL and writes it out using the FSImageWriter provided (e.g. to a file
	 * or outputstream).
	 *
	 * @param url
	 * @param fsw
	 * @param path
	 * @param width
	 */
	public static BufferedImage renderImageToOutput(String url, FSImageWriter fsw, String path, int width)
			throws IOException {

		BufferedImage image;
		OutputStream os = null;
		try {
			Java2DRenderer renderer = new Java2DRenderer(url, url, width);

			os = new BufferedOutputStream(new FileOutputStream(path));

			image = renderer.getImage();
			fsw.write(image, os);

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
				renderToImage(f, output, DEFAULT_WIDTH);
			} else {
				usage("File to render is not found: " + url);
			}
		} else {
			File out = File.createTempFile("fs", ".png");
			System.out.println("Saving image to " + out.getAbsolutePath());
			renderToImage(url, out.getAbsolutePath(), DEFAULT_WIDTH);
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