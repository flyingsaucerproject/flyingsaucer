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

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.LinkListener;


/**
 * <p>
 *
 * XHTMLPanel is a simple Swing component that renders valid XHTML content in a
 * Java program. It is scrolling aware so you can safely drop it into a
 * {@link javax.swing.JScrollPane}. The most common usage is to stuff a {@link URL} 
 * into it and then add it to your JFrame. Ex:</p> 
  *<pre>
 * import org.xhtmlrenderer.simple.*;
 * .....
 *
 * public static void main(String[] args) {
 *
 * // set up the xhtml panel XHTMLPanel xhtml = new XHTMLPanel();
 * xhtml.setDocument(new URL("http://myserver.com/page.xhtml")); 
 *
 * JScrollPane scroll = new JScrollPane(xhtml); 
 * JFrame frame = new JFrame("Demo"); 
 * frame.getContentPane().add(scroll); 
 * frame.pack();
 * frame.setSize(500,600); 
 * frame.show(); 
 * } 
 * </pre>
 * 
 * <p>XHTMLPanel renders XHTML and XML which can be loaded as valid {@link Document}
 * instances. You should make sure the document you want to render is valid. For XHTML,
 * there is always a default stylesheet available, even if no CSS is attached to the
 * XHTML you are loading. For XML, there is no default stylesheet, so you should have
 * one attached to your XML before trying to render it. XHTMLPanel has methods to load
 * documents from a file, by filename ({@link #setDocument(String filename)}),
 * from a URL ({@link #setDocument(URL)}), 
 * from a Document instance ({@link #setDocument(Document)}) or from an InputStream
 * ({@link #setDocument(InputStream,URL)}).</p> 
 *
 * <p>
 *
 * XHTMLPanel also lets you make simple changes with simple methods like
 * {@link #setFontScalingFactor(float)}. If you want to make other changes you will
 * need to get the rendering context ({@link #getRenderingContext()}) and call methods on
 * that. Ex: </p> <p/>
 *
 * <pre>
 * XHTMLPanel xhtml = new XHTMLPanel();
 * RenderingContext ctx = xhtml.getRenderingContext();
 * ctx.setLogging(true); // turn on logging
 * ctx.setValidating(true); // turn on doctype validation
 * ctx.addFont(fnt,"Arial"); // redefine a font
 * ctx.setDomImplementation("com.cooldom.DomImpl");
 * </pre>
 *
 * @author    Joshua Marinacci (joshy@joshy.net)
 * @see       <a href="http://xhtmlrenderer.dev.java.net">The Flying Saucer Home Page</a>
 * @see       RenderingContext 
 */
public class XHTMLPanel extends BasicPanel {
    private float fontScalingFactor;

    /** Instantiates an XHTMLPanel with no {@link Document} loaded by default. */
    public XHTMLPanel() { 
        fontScalingFactor = 1.2F;
    }

    /**
    * Instantiates a panel, rendering a {@link Document} read from the specified
    * {@link URL}.
     *
     * @param url            URL to read the Document from.
     * @exception Exception  Throws
     */
    public XHTMLPanel( URL url )
        throws Exception {
        this();
        setDocument( url );
    }

    /**
    * Instantiates a panel with a custom {@link org.xhtmlrenderer.extend.UserAgentCallback}
    * implementation.
     *
     * @param uac  The custom UserAgentCallback implementation.
     */
    public XHTMLPanel( UserAgentCallback uac ) {
        super( uac );
        addMouseListener(new LinkListener(this));
    }

    /** 
    * Lays out the current document again, and re-renders.
    */
    public void relayout() {
        super.calcLayout();
        ctx.getContext().flushFonts();
    }

    /** 
    * Repaints the current document using the layout as currently calculated. 
    */
    public void repaint() {
        super.repaint();
    }

    /**
     * Loads and renders a Document given a file name.
     *
     * @param filename       The file name & path for the document.
     */
    public void setDocument( String filename )
        throws Exception {
        URL url = new File( filename ).toURL();
        setDocument( url );
    }

    /**
     * Renders an XML Document instance. 
     *
     * @param doc The document to render.
     */
    public void setDocument( Document doc ) {
        setDocument( doc, new File( "." ).toURL() );
    }

    /**
     * Renders a Document given its URL. 
     *
     * @param url            The URL for the Document.
     */
    public void setDocument( URL url ) {
        setDocument( loadDocument( url ), url );
    }

    /**
     * Renders a Document using a URL as a base URL for relative 
     * paths.
     *
     * @param doc  The new document value
     * @param url  The new document value
     */
    public void setDocument( Document doc, URL url ) {
        super.setDocument( doc, url, new XhtmlNamespaceHandler() );
    }

    /**
     * Renders a Document read from an InputStream using a URL
     * as a base URL for relative paths.
     *
     * @param stream         The stream to read the Document from.
     * @param url            The URL used to resolve relative path references.
     */
    public void setDocument( InputStream stream, URL url )
        throws Exception {
        super.setDocument( stream, url, new XhtmlNamespaceHandler() );
    }

    /**
    * Sets the {@link RenderingContext} attribute of the XHTMLPanel object. Generally
     * you should not use this unless you have a heavily customized context to
     * use. To modify just some rendering behavior, consider using 
     * {@link #getRenderingContext()} to retrieve the current context, and using
     * mutators to change its behavior. 
     *
     * @param ctx  A new RenderingContext to use for rendering. 
     */
    public void setRenderingContext( RenderingContext ctx ) {
        super.setRenderingContext( ctx );
    }

    /**
    * Returns the {@link RenderingContext} used by this panel.
     *
     * @return   See desc.
     */
    public RenderingContext getRenderingContext() {
        return super.getRenderingContext();
    }

    /**
     * Returns the document title String after the document has been loaded.
     *
     * @return   The document title.
     */
    public String getDocumentTitle() {
        return super.getDocumentTitle();
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
        scaleFont( 1/ fontScalingFactor);
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
 * Revision 1.14  2005/03/22 12:28:14  pdoubleya
 * Updated JavaDocs.
 *
 *
 */
