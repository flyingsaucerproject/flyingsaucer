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
 * JScrollPane. The most common usage is to stuff a URL into it and then add it
 * to your JFrame. Ex:</p> 
  *<pre>
 * import org.xhtmlrenderer.simple.*;
 * .....
 *
 * public static void main(String[] args) {
 *
 * // set up the xhtml panel XHTMLPanel xhtml = new XHTMLPanel();
 * xhtml.setDocument(new URL("http://myserver.com/page.xhtml")); 
 *
 * JScrollPane scroll = new JScrollPane(xhtml); JFrame frame = new
 * JFrame("Demo"); frame.getContentPane().add(scroll); frame.pack();
 * frame.setSize(500,600); frame.show(); } 
 * </pre> 
 *
 * <p>
 *
 * XHTMLPanel also lets you make simple changes with simple methods like
 * setFontScale() and setMediaType(). If you want to make other changes you will
 * need to get the rendering context (getRenderingContext()) and call methods on
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
 * @webpage   http://xhtmlrenderer.dev.java.net/
 * @see       RenderingContext </p>
 */

public class XHTMLPanel extends BasicPanel {

    /** Constructor for the XHTMLPanel object */
    public XHTMLPanel() { }

    /**
     * Constructor for the XHTMLPanel object
     *
     * @param url            PARAM
     * @exception Exception  Throws
     */
    public XHTMLPanel( URL url )
        throws Exception {
        this();
        setDocument( url );
    }

    /**
     * Constructor for the XHTMLPanel object
     *
     * @param uac  PARAM
     */
    public XHTMLPanel( UserAgentCallback uac ) {
        super( uac );
        addMouseListener(new LinkListener(this));
    }

    /*
     * browser functions?
     */
    /** Description of the Method */
    public void relayout() {
        super.calcLayout();
        ctx.getContext().flushFonts();
    }

    /** Description of the Method */
    public void repaint() {
        super.repaint();
    }

    /*
     * various forms of setDocument()
     */
    /**
     * Sets the document attribute of the XHTMLPanel object
     *
     * @param filename       The new document value
     * @exception Exception  Throws
     */
    public void setDocument( String filename )
        throws Exception {
        URL url = new File( filename ).toURL();
        setDocument( url );
    }

    /**
     * Sets the document attribute of the XHTMLPanel object
     *
     * @param doc                        The new document value
     * @exception MalformedURLException  Throws
     */
    public void setDocument( Document doc )
        throws MalformedURLException {
        setDocument( doc, new File( "." ).toURL() );
    }

    /**
     * Sets the document attribute of the XHTMLPanel object
     *
     * @param url            The new document value
     * @exception Exception  Throws
     */
    public void setDocument( URL url )
        throws Exception {
        setDocument( loadDocument( url ), url );
    }

    /**
     * Sets the document attribute of the XHTMLPanel object
     *
     * @param doc  The new document value
     * @param url  The new document value
     */
    public void setDocument( Document doc, URL url ) {
        super.setDocument( doc, url, new XhtmlNamespaceHandler() );
    }

    /**
     * Sets the document attribute of the XHTMLPanel object
     *
     * @param stream         The new document value
     * @param url            The new document value
     * @exception Exception  Throws
     */
    public void setDocument( InputStream stream, URL url )
        throws Exception {
        super.setDocument( stream, url, new XhtmlNamespaceHandler() );
    }

    /**
     * Sets the renderingContext attribute of the XHTMLPanel object
     *
     * @param ctx  The new renderingContext value
     */
    public void setRenderingContext( RenderingContext ctx ) {
        super.setRenderingContext( ctx );
    }

    /*
     * accessor for rendering context
     */
    /**
     * Gets the renderingContext attribute of the XHTMLPanel object
     *
     * @return   The renderingContext value
     */
    public RenderingContext getRenderingContext() {
        return super.getRenderingContext();
    }

    /**
     * Gets the documentTitle attribute of the XHTMLPanel object
     *
     * @return   The documentTitle value
     */
    public String getDocumentTitle() {
        return super.getDocumentTitle();
    }

}

