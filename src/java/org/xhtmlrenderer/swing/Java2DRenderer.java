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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.context.AWTFontResolver;
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

/**
 * Renders XML files formatted with CSS as images. Input is a document in the form of file or URL,
 * and output is a BufferedImage. A Java2DRenderer is not intended to be re-used for multiple document
 * sources; just create new Java2DRenderers for each one you need. Java2DRenderer is not thread-safe.
 * Standard usage pattern is
 * <pre>
 * File xhtml = ...
 * Java2DRenderer rend = new Java2DRenderer(xhtml);
 * BufferedImage image = rend.getImage();
 * </pre>
 * The document is not loaded, and layout and render don't take place, until render is called. Subsequent calls
 * to getImage() don't result in a reload; create a new Java2DRenderer instance to do so.
 *
 * @see ITextRenderer
 */
public class Java2DRenderer {
    private static final int DEFAULT_HEIGHT = 1000;
    private static final int DEFAULT_DOTS_PER_POINT = 1;
    private static final int DEFAULT_DOTS_PER_PIXEL = 1;

    private SharedContext _sharedContext;
    private Java2DOutputDevice _outputDevice;

    private Document _doc;
    private Box _root;

    private float _dotsPerPoint;
    private BufferedImage _outputImage;

    /**
     * Whether we've completed rendering; image will only be rendered once.
     */
    private boolean rendered;
    private String sourceDocument;
    private String sourceDocumentBase;

    /**
     * Creates a new instance with specific scaling paramters; these are currently ignored.
     * @param dotsPerPoint Layout XML at so many dots per point
     * @param dotsPerPixel Layout XML at so many dots per pixel
     */
    private Java2DRenderer(float dotsPerPoint, int dotsPerPixel) {
        init(dotsPerPoint, dotsPerPixel);
    }

    /**
     * Creates a new instance for a given URL. Does not render until {@link #getImage(int)} is called for
     * the first time.
     * @param url The location of the document to be rendered.
     */
    public Java2DRenderer(String url, String baseUrl) {
        // bypass scaling routines based on DPI -- see PDFRenderer and compare--dotsPerPoint is not implemented
        // in all subordinate classes and interfaces for Java2D, so leaving it out
        // leaving this constructor call here as a TODO
        this(DEFAULT_DOTS_PER_POINT, DEFAULT_DOTS_PER_PIXEL);

        this.sourceDocument = url;
        this.sourceDocumentBase = baseUrl;
    }

    /**
     * Creates a new instance for a given File. Does not render until {@link #getImage(int)} is called for
     * the first time.
     * @param file The file to be rendered.
     */
    public Java2DRenderer(File file) throws IOException {
        this(file.toURI().toURL().toExternalForm(), file.getParentFile().toURI().toURL().toExternalForm());
    }

    /**
     * Renders the XML document if necessary and returns the resulting image. If already rendered, same image
     * reference will be returned.
     *
     * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout. Height
     * is determined automatically based on content.
     * @return The XML rendered as a BufferedImage.
     */
    public BufferedImage getImage(int width) {
        if (! rendered) {
            setDocument(loadDocument(sourceDocument), sourceDocumentBase, new XhtmlNamespaceHandler());

            layout(width);
            _outputImage = ImageUtil.createCompatibleBufferedImage(width, _root.getHeight());
            _outputDevice = new Java2DOutputDevice(_outputImage);
            Graphics2D newG = (Graphics2D) _outputImage.getGraphics();
            RenderingContext rc = _sharedContext.newRenderingContextInstance();
            rc.setFontContext(new Java2DFontContext(newG));
            rc.setOutputDevice(_outputDevice);
            _sharedContext.getTextRenderer().setup(rc.getFontContext());
            try {
                _root.getLayer().paint(rc, 0, 0);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            rendered = true;
        }

        return _outputImage;
    }

    private void setDocument(Document doc, String url, NamespaceHandler nsh) {
        _doc = doc;

        _sharedContext.reset();
        if (Configuration.isTrue("xr.cache.stylesheets", true)) {
            _sharedContext.getCss().flushStyleSheets();
        } else {
            _sharedContext.getCss().flushAllStyleSheets();
        }
        _sharedContext.setBaseURL(url);
        _sharedContext.setNamespaceHandler(nsh);
        _sharedContext.getCss().setDocumentContext(
                _sharedContext,
                _sharedContext.getNamespaceHandler(),
                doc,
                new NullUserInterface()
        );
    }

    private void layout(int width) {
        Rectangle rect = new Rectangle(0, 0, width, DEFAULT_HEIGHT);
        _sharedContext.set_TempCanvas(rect);
        LayoutContext c = newLayoutContext();
        BlockBox root = BoxBuilder.createRootBox(c, _doc);
        root.setContainingBlock(new ViewportBox(rect));
        root.layout(c);
        _root = root;
    }

    private Document loadDocument(final String uri) {
        return _sharedContext.getUac().getXMLResource(uri).getDocument();
    }

    private LayoutContext newLayoutContext() {
        LayoutContext result = _sharedContext.newLayoutContextInstance();
        result.setFontContext(new Java2DFontContext(_outputDevice.getGraphics()));

        _sharedContext.getTextRenderer().setup(result.getFontContext());

        return result;
    }

    private void init(float dotsPerPoint, int dotsPerPixel) {
        _dotsPerPoint = dotsPerPoint;

        _outputImage = ImageUtil.createCompatibleBufferedImage(DEFAULT_DOTS_PER_POINT, DEFAULT_DOTS_PER_POINT);
        _outputDevice = new Java2DOutputDevice(_outputImage);

        UserAgentCallback userAgent = new NaiveUserAgent();
        _sharedContext = new SharedContext(userAgent);

        AWTFontResolver fontResolver = new AWTFontResolver();
        _sharedContext.setFontResolver(fontResolver);

        SwingReplacedElementFactory replacedElementFactory = new SwingReplacedElementFactory();
        _sharedContext.setReplacedElementFactory(replacedElementFactory);

        _sharedContext.setTextRenderer(new Java2DTextRenderer());
        _sharedContext.setDPI(72 * _dotsPerPoint);
        _sharedContext.setDotsPerPixel(dotsPerPixel);
        _sharedContext.setPrint(false);
        _sharedContext.setInteractive(false);
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