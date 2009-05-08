/*
 * {{{ header & license
 * Copyright (c) 2006 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.extend.UserInterface;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.ViewportBox;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * <p>Renders an XML files, formatted with CSS, as an Box. Input is a document in the form of file or URL,
 * and output is the root of the Box model. A BoxRenderer is not intended to be re-used for multiple document
 * sources; just create new BoxRenderers for each one you need. BoxRenderer is not thread-safe.
 * Standard usage pattern is</p>
 *
 * <pre>
 * File xhtml = ...
 * BoxRenderer rend = new BoxRenderer(xhtml);
 * Box rootBox = rend.render();
 * </pre>
 *
 * <p>The document is not loaded, and layout and render don't take place, until {@link #render()}  is called.
 * Subsequent calls to {@link #render()} don't result in a reload; create a new BoxRenderer instance to do so.</p>
 *
 * <p>As with {@link RootPanel}, you can access the
 * {@link org.xhtmlrenderer.layout.SharedContext} instance that will be used by this renderer and change settings
 * to control the rendering process; use {@link #getSharedContext()}.</p>
 *
 *
 *
 * <p>Not thread-safe.</p>
 *
 * @see org.xhtmlrenderer.pdf.ITextRenderer
 */
public class BoxRenderer {
	private static final int DEFAULT_HEIGHT = 1000;
	private static final int DEFAULT_DOTS_PER_POINT = 1;
	private static final int DEFAULT_DOTS_PER_PIXEL = 1;

    private SharedContext sharedContext;
	private Java2DOutputDevice outputDevice;

	private Document doc;
	private Box root;

	private float dotsPerPoint;


    /**
	 * Whether we've completed rendering; image will only be rendered once.
	 */
	private boolean rendered;
	private String sourceDocument;
	private String sourceDocumentBase;
	private int width;
	private int height;
	private static final int NO_HEIGHT = -1;
	private Map renderingHints;
    private LayoutContext layoutContext;


    /**
	 * Base constructor
	 */
	private BoxRenderer() {
    }

	/**
	 * Creates a new instance with specific scaling paramters; these are currently ignored.
	 *
	 * @param dotsPerPoint Layout XML at so many dots per point
	 * @param dotsPerPixel Layout XML at so many dots per pixel
	 */
	private BoxRenderer(float dotsPerPoint, int dotsPerPixel) {
		this();
		init(dotsPerPoint, dotsPerPixel);
	}

	/**
	 * Creates a new instance for a given URL. Does not render until {@link #render()} is called for
	 * the first time.
	 *
	 * @param url The location of the document to be rendered.
	 * @param baseUrl The base url for the document, against which  relative paths are resolved.
	 * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
	 */
	public BoxRenderer(String url, String baseUrl, int width, int height) {
		// bypass scaling routines based on DPI -- see PDFRenderer and compare--dotsPerPoint is not implemented
		// in all subordinate classes and interfaces for Java2D, so leaving it out
		// leaving this constructor call here as a TODO
		this(DEFAULT_DOTS_PER_POINT, DEFAULT_DOTS_PER_PIXEL);

		this.sourceDocument = url;
		this.sourceDocumentBase = baseUrl;
		this.width = width;
		this.height = height;
	}

	/**
	 * Creates a new instance for a given File. Does not render until {@link #render()} is called for
	 * the first time.
	 *
	 * @param file The file to be rendered.
	 * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
	 * @param height Target height, in pixels, for the image
	 */
	public BoxRenderer(File file, int width, int height) throws IOException {
		this(file.toURI().toURL().toExternalForm(), width, height);
	}

	/**
	 * Creates a new instance for a given File. Does not render until {@link #render()} is called for
	 * the first time.
	 *
	 * @param file The file to be rendered.
	 * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
	 * Heght is calculated based on content
	 */
	public BoxRenderer(File file, int width) throws IOException {
		this(file.toURI().toURL().toExternalForm(), width);
	}


	/**
	 * Renderer for a given URL (which is also used as the base) and a specified width; height is calculated
	 * automatically.
	 *
	 * @param url The location of the document to be rendered.
	 * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
	 * Heght is calculated based on content
	 */
	public BoxRenderer(String url, int width) {
		this(url, url, width, NO_HEIGHT);
	}

	/**
	 * Renderer for a given URL and a specified width; height is calculated
	 * automatically.
	 *
	 * @param url The location of the document to be rendered.
	 * @param baseurl The base url for the document, against which  relative paths are resolved.
	 * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
	 * Heght is calculated based on content
	 */
	public BoxRenderer(String url, String baseurl, int width) {
		this(url, baseurl, width, NO_HEIGHT);
	}

	/**
	 * Renderer for a given URL and a specified width; height is calculated
	 * automatically.
	 *
	 * @param url The location of the document to be rendered.
	 * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
	 * @param height Target height, in pixels, for the image
	 */
	public BoxRenderer(String url, int width, int height) {
		this(url, url, width, height);
	}

	/**
	 * Sets the rendering hints to apply to the Graphics2D instance used by the renderer; see
	 * {@link java.awt.Graphics2D#setRenderingHints(java.util.Map)}. The Map need not specify values for all
	 * properties; any settings in this map will be applied as override to the default settings, and will
	 * not replace the entire Map for the Graphics2D instance.
	 *
	 * @param hints values to override in default rendering hints for Graphics2D we are rendering to
	 */
	public void setRenderingHints(Map hints) {
		renderingHints = hints;
	}

	/**
	 * Returns the SharedContext to be used by renderer. Is instantiated along with the class, so can be accessed
	 * before {@link #render()} is called to tune the rendering process.
	 *
	 * @return the SharedContext instance that will be used by this renderer
	 */
	public SharedContext getSharedContext() {
		return sharedContext;
	}

    /**
     * Returns the LayoutContext to be used by renderer. Is instantiated along with the class, so can be accessed
     * before {@link #render()} is called to tune the rendering process.
     *
     * @return the LayoutContext instance that will be used by this renderer
     */
    public LayoutContext getLayoutContext() {
        return layoutContext;
    }

    /**
	 * Renders the XML document if necessary and returns the root Box. If already rendered, same Box
	 * reference will be returned.
	 *
	 * @return The XML rendered as a Box.
	 */
	public Box render() {
		if (!rendered) {
			setDocument(loadDocument(sourceDocument), sourceDocumentBase, new XhtmlNamespaceHandler());

			layout(this.width);

			height = this.height == -1 ? root.getHeight() : this.height;
			BufferedImage outputImage = createBufferedImage(this.width, height);
			outputDevice = new Java2DOutputDevice(outputImage);
			Graphics2D newG = (Graphics2D) outputImage.getGraphics();
            try {
                if ( renderingHints != null ) {
                    newG.getRenderingHints().putAll(renderingHints);
                }

                RenderingContext rc = sharedContext.newRenderingContextInstance();
                rc.setFontContext(new Java2DFontContext(newG));
                rc.setOutputDevice(outputDevice);
                sharedContext.getTextRenderer().setup(rc.getFontContext());

                root.getLayer().paint(rc);
            } finally {
                if (newG != null) newG.dispose();
            }

			rendered = true;
		}

		return root;
	}

	/**
	 * Returns a BufferedImage using the specified width and height. By default this returns an image compatible
	 * with the screen (if not in "headless" mode) formatted for RGB.
	 *
	 * @param width target width
	 * @param height target height
	 * @return new BI
	 */
	protected BufferedImage createBufferedImage(int width, int height) {
		BufferedImage image = ImageUtil.createCompatibleBufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		ImageUtil.clearImage(image);
		return image;
	}

	private void setDocument(Document doc, String url, NamespaceHandler nsh) {
		this.doc = doc;

		sharedContext.reset();
		if (Configuration.isTrue("xr.cache.stylesheets", true)) {
			sharedContext.getCss().flushStyleSheets();
		} else {
			sharedContext.getCss().flushAllStyleSheets();
		}
		sharedContext.setBaseURL(url);
		sharedContext.setNamespaceHandler(nsh);
		sharedContext.getCss().setDocumentContext(
				sharedContext,
				sharedContext.getNamespaceHandler(),
				doc,
				new NullUserInterface()
		);
	}

	private void layout(int width) {
		Rectangle rect = new Rectangle(0, 0, width, DEFAULT_HEIGHT);
		sharedContext.set_TempCanvas(rect);
		BlockBox root = BoxBuilder.createRootBox(layoutContext, doc);
		root.setContainingBlock(new ViewportBox(rect));
		root.layout(layoutContext);
		this.root = root;
	}

	private Document loadDocument(final String uri) {
		return sharedContext.getUac().getXMLResource(uri).getDocument();
	}

	private LayoutContext newLayoutContext() {
		LayoutContext result = sharedContext.newLayoutContextInstance();
		result.setFontContext(new Java2DFontContext(outputDevice.getGraphics()));

		sharedContext.getTextRenderer().setup(result.getFontContext());

		return result;
	}

	private void init(float dotsPerPoint, int dotsPerPixel) {
		this.dotsPerPoint = dotsPerPoint;

		BufferedImage outputImage = ImageUtil.createCompatibleBufferedImage(DEFAULT_DOTS_PER_POINT, DEFAULT_DOTS_PER_POINT);
		outputDevice = new Java2DOutputDevice(outputImage);

		UserAgentCallback userAgent = new NaiveUserAgent();
        sharedContext = newSharedContext(dotsPerPixel, userAgent);
        layoutContext = newLayoutContext();
	}

    private SharedContext newSharedContext(int dotsPerPixel, UserAgentCallback userAgent) {
        SharedContext context = new SharedContext(userAgent);

        AWTFontResolver fontResolver = new AWTFontResolver();
        context.setFontResolver(fontResolver);

        SwingReplacedElementFactory replacedElementFactory = new SwingReplacedElementFactory();
        context.setReplacedElementFactory(replacedElementFactory);

        context.setTextRenderer(new Java2DTextRenderer());
        context.setDPI(72 * this.dotsPerPoint);
        context.setDotsPerPixel(dotsPerPixel);
        context.setPrint(false);
        context.setInteractive(false);
        return context;
    }

    private static final class NullUserInterface implements UserInterface {

		public boolean isHover(Element e) {
			return false;
		}

		public boolean isActive(Element e) {
			return false;
		}

		public boolean isFocus(Element e) {
			return false;
		}
	}
}