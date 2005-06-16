/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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
package org.xhtmlrenderer.simple;

import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.HoverListener;
import org.xhtmlrenderer.swing.LinkListener;

import java.io.InputStream;
import java.net.URL;


/**
 * <p/>
 * <p/>
 * XHTMLPanel is a simple Swing component that renders valid XHTML content in a
 * Java program. It is scrolling aware so you can safely drop it into a
 * {@link javax.swing.JScrollPane}. The most common usage is to stuff a {@link URL}
 * into it and then add it to your JFrame. Ex:</p>
 * <pre>
 * import org.xhtmlrenderer.simple.*;
 * .....
 * <p/>
 * public static void main(String[] args) {
 * <p/>
 * // set up the xhtml panel XHTMLPanel xhtml = new XHTMLPanel();
 * xhtml.setDocument(new URL("http://myserver.com/page.xhtml"));
 * <p/>
 * JScrollPane scroll = new JScrollPane(xhtml);
 * JFrame frame = new JFrame("Demo");
 * frame.getContentPane().add(scroll);
 * frame.pack();
 * frame.setSize(500,600);
 * frame.show();
 * }
 * </pre>
 * <p/>
 * <p>XHTMLPanel renders XHTML and XML which can be loaded as valid {@link Document}
 * instances. You should make sure the document you want to render is valid. For XHTML,
 * there is always a default stylesheet available, even if no CSS is attached to the
 * XHTML you are loading. For XML, there is no default stylesheet, so you should have
 * one attached to your XML before trying to render it. XHTMLPanel has methods to load
 * documents from a uri ({@link #setDocument(String uri)}),
 * from a Document instance ({@link #setDocument(Document)}) or from an InputStream
 * ({@link org.xhtmlrenderer.swing.BasicPanel#setDocument(java.io.InputStream,String)}).</p>
 * <p/>
 * <p/>
 * <p/>
 * XHTMLPanel also lets you make simple changes with simple methods like
 * {@link #setFontScalingFactor(float)}. If you want to make other changes you will
 * need to get the rendering context ({@link #getRenderingContext()}) and call methods on
 * that. Ex: </p> <p/>
 * <p/>
 * <pre>
 * XHTMLPanel xhtml = new XHTMLPanel();
 * RenderingContext ctx = xhtml.getRenderingContext();
 * ctx.setLogging(true); // turn on logging
 * ctx.setValidating(true); // turn on doctype validation
 * ctx.addFont(fnt,"Arial"); // redefine a font
 * ctx.setDomImplementation("com.cooldom.DomImpl");
 * </pre>
 * <p/>
 * <p>XHTMLPanel comes with a pre-installed MouseListener which handles :hover events used for rollovers
 * ( @see org.xhtmlrenderer.swing.HoverListener ). XHTMLPanel also comes with a pre-installed LinkListener
 * used to follow links.  ( @see org.xhtmlrenderer.swing.LinkListener )
 * If you want to disable these for some reason you can
 * get the list of mouse listeners and remove them all.
 * </p>
 *
 * @author Joshua Marinacci (joshy@joshy.net)
 * @see <a href="http://xhtmlrenderer.dev.java.net">The Flying Saucer Home Page</a>
 * @see RenderingContext
 */
public class XHTMLPanel extends BasicPanel {
    private float fontScalingFactor;

    /**
     * Instantiates an XHTMLPanel with no {@link Document} loaded by default.
     */
    public XHTMLPanel() {
        fontScalingFactor = 1.2F;
        setupListeners();
    }

    /**
     * Instantiates a panel, rendering a {@link Document} read from the specified
     * {@link URL}.
     *
     * @param url URL to read the Document from.
     */
    public XHTMLPanel(String url) {
        this();
        setDocument(url);
    }

    /**
     * Instantiates a panel with a custom {@link org.xhtmlrenderer.extend.UserAgentCallback}
     * implementation.
     *
     * @param uac The custom UserAgentCallback implementation.
     */
    public XHTMLPanel(UserAgentCallback uac) {
        super(uac);
        setupListeners();
    }

    private void setupListeners() {
        // install a default link listener
        addMouseListener(new LinkListener(this));
        // install a default hover listener
        HoverListener hov = new HoverListener(this);
        addMouseListener(hov);
        addMouseMotionListener(hov);
    }

    /**
     * Lays out the current document again, and re-renders.
     */
    public void relayout() {
        super.calcLayout();
        ctx.getContext().flushFonts();
    }

    /**
     * Loads and renders a Document given a uri.
     * The uri is resolved by the UserAgentCallback
     *
     * @param uri
     */
    public void setDocument(String uri) {
        setDocument(loadDocument(uri), uri);
    }

    /**
     * Renders an XML Document instance.
     * Make sure that no relative resources are needed
     *
     * @param doc The document to render.
     */
    public void setDocument(Document doc) {
        setDocument(doc, "");
    }

    /**
     * Renders a Document using a URL as a base URL for relative
     * paths.
     *
     * @param doc The new document value
     * @param url The new document value
     */
    public void setDocument(Document doc, String url) {
        super.setDocument(doc, url, new XhtmlNamespaceHandler());
    }

    /**
     * Renders a Document read from an InputStream using a URL
     * as a base URL for relative paths.
     *
     * @param stream The stream to read the Document from.
     * @param url    The URL used to resolve relative path references.
     */
    public void setDocument(InputStream stream, String url)
            throws Exception {
        super.setDocument(stream, url, new XhtmlNamespaceHandler());
    }

    /**
     * Sets the {@link RenderingContext} attribute of the XHTMLPanel object. Generally
     * you should not use this unless you have a heavily customized context to
     * use. To modify just some rendering behavior, consider using
     * {@link #getRenderingContext()} to retrieve the current context, and using
     * mutators to change its behavior.
     *
     * @param ctx A new RenderingContext to use for rendering.
     */
    public void setRenderingContext(RenderingContext ctx) {
        super.setRenderingContext(ctx);
    }

    /**
     * Sets the scaling factor used by {@link #incrementFontSize()} and
     * {@link #decrementFontSize()}--both scale the font up or down by this
     * scaling factor. The scaling roughly modifies the font size as a multiplier
     * or divisor. A scaling factor of 1.2 applied against a font size of 10pt
     * results in a scaled font of 12pt. The default scaling factor is
     * 1.2F.
     */
    public void setFontScalingFactor(float scaling) {
        fontScalingFactor = scaling;
    }

    /**
     * Increments all rendered fonts on the current document by the current
     * scaling factor for the panel. Scaling applies culmulatively, which means that
     * multiple calls to this method scale fonts larger and larger by applying the
     * current scaling factor against itself. You can modify the scaling factor by
     * {@link #setFontScalingFactor(float)}, and reset to the document's specified
     * font size with {@link #resetFontSize()}.
     */
    public void incrementFontSize() {
        scaleFont(fontScalingFactor);
    }

    /**
     * Resets all rendered fonts on the current document to the font size
     * specified in the document's styling instructions.
     */
    public void resetFontSize() {
        RenderingContext rc = getRenderingContext();
        rc.getTextRenderer().setFontScale(1.0F);
        relayout();
        repaint();
    }

    /**
     * Decrements all rendered fonts on the current document by the current
     * scaling factor for the panel. Scaling applies culmulatively, which means that
     * multiple calls to this method scale fonts smaller and smaller by applying the
     * current scaling factor against itself. You can modify the scaling factor by
     * {@link #setFontScalingFactor(float)}, and reset to the document's specified
     * font size with {@link #resetFontSize()}.
     */
    public void decrementFontSize() {
        scaleFont(1 / fontScalingFactor);
    }

    /**
     * Applies a change in scale for fonts using the rendering context's text
     * renderer.
     */
    private void scaleFont(float scaleBy) {
        RenderingContext rc = getRenderingContext();
        rc.getTextRenderer().setFontScale(rc.getTextRenderer().getFontScale() * scaleBy);
        relayout();
        repaint();
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.19  2005/06/16 07:24:52  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.18  2005/06/15 10:56:14  tobega
 * cleaned up a bit of URL mess, centralizing URI-resolution and loading to UserAgentCallback
 *
 * Revision 1.17  2005/06/09 22:34:56  joshy
 * This makes the hover listener be added to the xhtml panel by default.
 * Also improves the box searching code by testing if the parent of the deepest
 * box is hoverable in the case where the deepest box is not.
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2005/06/01 21:36:43  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.15  2005/03/22 13:55:29  pdoubleya
 * Fixed missing throws declarations.
 *
 * Revision 1.14  2005/03/22 12:28:14  pdoubleya
 * Updated JavaDocs.
 *
 *
 */
