/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System and  Patrick Wright
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

/**
 * <p>Renders an XML files, formatted with CSS, as an image. Input is a document in the form of file or URL,
 * and output is a BufferedImage. A Java2DRenderer is not intended to be re-used for multiple document
 * sources; just create new Java2DRenderers for each one you need. Java2DRenderer is not thread-safe.
 * Standard usage pattern is</p>
 *
 * <pre>
 * File xhtml = ...
 * Java2DRenderer rend = new Java2DRenderer(xhtml);
 * BufferedImage image = rend.getImage();
 * </pre>
 *
 * <p>The document is not loaded, and layout and render don't take place, until {@link #getImage()}  is called.
 * Subsequent calls to {@link #getImage()} don't result in a reload; create a new Java2DRenderer instance to do so.</p>
 *
 * <p>As with {@link org.xhtmlrenderer.swing.RootPanel}, you can access the
 * {@link org.xhtmlrenderer.layout.SharedContext} instance that will be used by this renderer and change settings
 * to control the rendering process; use {@link #getSharedContext()}.</p>
 *
 * <p>By default, this renderer will render to an RGB image which does not support transparency. To use another type
 * of BufferedImage, either set the image type in constructor before calling
 * {@link #getImage()}, or else override the {@link #createBufferedImage(int, int)} to have full control over
 * the image we render to.</p>
 *
 * <p>Not thread-safe.</p>
 */
public class Java2DRenderer {
    private static final int DEFAULT_HEIGHT = 1000;
    private static final int DEFAULT_DOTS_PER_POINT = 1;
    private static final int DEFAULT_DOTS_PER_PIXEL = 1;
    private static final int DEFAULT_IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;

    private SharedContext sharedContext;
    private Java2DOutputDevice outputDevice;

    private Document doc;
    private Box root;

    private BufferedImage outputImage;
    private final int bufferedImageType;

    /**
     * Whether we've completed rendering; image will only be rendered once.
     */
    private boolean rendered;
    private String sourceDocument;
    private String sourceDocumentBase;
    private final int width;
    private int height;
    private static final int NO_HEIGHT = -1;

    public Java2DRenderer(String url, String baseUrl, int width, int height) {
        this(url, baseUrl, width, height, DEFAULT_IMAGE_TYPE);
    }

    /**
     * Creates a new instance for a given URL. Does not render until {@link #getImage()} is called for
     * the first time.
     *
     * @param url The location of the document to be rendered.
     * @param baseUrl The base url for the document, against which  relative paths are resolved.
     * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
     * @param height Target height, in pixels, for the image; required to provide vertical bounds for the layout.
     */
    public Java2DRenderer(String url, String baseUrl, int width, int height, int bufferedImageType) {
        // bypass scaling routines based on DPI -- see PDFRenderer and compare--dotsPerPoint is not implemented
        // in all subordinate classes and interfaces for Java2D, so leaving it out
        // leaving this constructor call here as a TODO
        init();

        this.sourceDocument = url;
        this.sourceDocumentBase = baseUrl;
        this.width = width;
        this.height = height;
        this.bufferedImageType = bufferedImageType;
    }

    /**
     * Creates a new instance for a given File. Does not render until {@link #getImage()} is called for
     * the first time.
     *
     * @param file The file to be rendered.
     * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
     * @param height Target height, in pixels, for the image
     */
    public Java2DRenderer(File file, int width, int height) throws IOException {
        this(file.toURI().toURL().toExternalForm(), width, height);
    }

        /**
         * Creates a new instance pointing to the given Document. Does not render until {@link #getImage()} is called for
         * the first time.
         *
         * @param doc The document to be rendered.
         * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
         * @param height Target height, in pixels, for the image.
         */
        public Java2DRenderer(Document doc, int width, int height) {
            init();
            this.doc = doc;
            this.width = width;
            this.height = height;
            this.bufferedImageType = DEFAULT_IMAGE_TYPE;
        }

    public Java2DRenderer(Document doc, int width) {
        this(doc, width, NO_HEIGHT);
    }

        /**
         * Creates a new instance pointing to the given Document. Does not render until {@link #getImage()} is called for
         * the first time.
         *
         * @param doc The document to be rendered.
         * @param baseUrl The base url for the document, against which  relative paths are resolved.
         * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
         * @param height Target height, in pixels, for the image.
         */
        public Java2DRenderer(Document doc, String baseUrl, int width, int height) {
            this(doc, width, height);
            this.sourceDocumentBase = baseUrl;
        }

    /**
     * Creates a new instance for a given File. Does not render until {@link #getImage()} is called for
     * the first time.
     *
     * @param file The file to be rendered.
     * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
     * Height is calculated based on content
     */
    public Java2DRenderer(File file, int width) throws IOException {
        this(file.toURI().toURL().toExternalForm(), width, NO_HEIGHT);
    }

    public static Java2DRenderer fromFile(File file, int width, int bufferedImageType) throws IOException {
        String url = file.toURI().toURL().toExternalForm();
        return new Java2DRenderer(url, url, width, NO_HEIGHT, bufferedImageType);
    }

    /**
     * Renderer for a given URL (which is also used as the base) and a specified width; height is calculated
     * automatically.
     *
     * @param url The location of the document to be rendered.
     * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
     * Height is calculated based on content
     */
    public Java2DRenderer(String url, int width) {
        this(url, url, width, NO_HEIGHT);
    }

    /**
     * Renderer for a given URL and a specified width; height is calculated
     * automatically.
     *
     * @param url The location of the document to be rendered.
     * @param baseurl The base url for the document, against which  relative paths are resolved.
     * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
     * Height is calculated based on content
     */
    public Java2DRenderer(String url, String baseurl, int width) {
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
    public Java2DRenderer(String url, int width, int height) {
        this(url, url, width, height);
    }

    /**
     * Returns the SharedContext to be used by renderer. Is instantiated along with the class, so can be accessed
     * before {@link #getImage()} is called to tune the rendering process.
     *
     * @return the SharedContext instance that will be used by this renderer
     */
    public SharedContext getSharedContext() {
        return sharedContext;
    }

    /**
     * Renders the XML document if necessary and returns the resulting image. If already rendered, same image
     * reference will be returned.
     *
     * @return The XML rendered as a BufferedImage.
     */
    public BufferedImage getImage() {
        if (!rendered) {
            setDocument((doc == null ? loadDocument(sourceDocument) : doc), sourceDocumentBase, new XhtmlNamespaceHandler());

            layout(this.width);

            height = this.height == -1 ? root.getHeight() : this.height;
            outputImage = createBufferedImage(this.width, height);
            outputDevice = new Java2DOutputDevice(outputImage);
            Graphics2D newG = (Graphics2D) outputImage.getGraphics();

            RenderingContext rc = sharedContext.newRenderingContextInstance();
            rc.setFontContext(new Java2DFontContext(newG));
            rc.setOutputDevice(outputDevice);
            sharedContext.getTextRenderer().setup(rc.getFontContext());

            root.getLayer().paint(rc);

            newG.dispose();
            rendered = true;
        }

        return outputImage;
    }

    /**
     * Returns a BufferedImage using the specified width and height. By default, this returns an image compatible
     * with the screen (if not in "headless" mode) using the BufferedImage type specified in constructor,
     * or else RGB if none is specified.
     *
     * @param width target width
     * @param height target height
     * @return new BI
     */
    protected BufferedImage createBufferedImage(int width, int height) {
        BufferedImage image = ImageUtil.createCompatibleBufferedImage(width, height, this.bufferedImageType);
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
        sharedContext.setTemporaryCanvas(rect);
        LayoutContext c = newLayoutContext();
        BlockBox root = BoxBuilder.createRootBox(c, doc);
        root.setContainingBlock(new ViewportBox(rect));
        root.layout(c);
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

    private void init() {
        outputImage = ImageUtil.createCompatibleBufferedImage(DEFAULT_DOTS_PER_POINT, DEFAULT_DOTS_PER_POINT);
        outputDevice = new Java2DOutputDevice(outputImage);

        UserAgentCallback userAgent = new NaiveUserAgent();
        sharedContext = new SharedContext(userAgent);

        AWTFontResolver fontResolver = new AWTFontResolver();
        sharedContext.setFontResolver(fontResolver);

        SwingReplacedElementFactory replacedElementFactory = new SwingReplacedElementFactory();
        sharedContext.setReplacedElementFactory(replacedElementFactory);

        sharedContext.setTextRenderer(new Java2DTextRenderer());
        sharedContext.setDPI(72 * (float) Java2DRenderer.DEFAULT_DOTS_PER_POINT);
        sharedContext.setDotsPerPixel(Java2DRenderer.DEFAULT_DOTS_PER_PIXEL);
        sharedContext.setPrint(false);
        sharedContext.setInteractive(false);
    }

    private static final class NullUserInterface implements UserInterface {

        @Override
        public boolean isHover(Element e) {
            return false;
        }

        @Override
        public boolean isActive(Element e) {
            return false;
        }

        @Override
        public boolean isFocus(Element e) {
            return false;
        }
    }
}