/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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

import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.xpath.XPathAPI;
import org.xhtmlrenderer.css.bridge.TBStyleReference;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.forms.AbsoluteLayoutManager;
import org.xhtmlrenderer.layout.BodyLayout;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.util.x;
import org.xml.sax.ErrorHandler;


/**
 * Description of the Class
 *
 * @author   Patrick Wright
 */
public class HTMLPanel extends JPanel implements ComponentListener {

    //private int html_height = -1;
    //private int max_width = -1;

    /** Description of the Field */
    protected Document doc;
    /** Description of the Field */
    protected Context c;
    /** Description of the Field */
    protected Box body_box = null;

    /** Description of the Field */
    protected ErrorHandler error_handler;
    /** Description of the Field */
    protected BodyLayout layout;

    /** Description of the Field */
    private Map documentListeners;
    /** Description of the Field */
    private JScrollPane enclosingScrollPane;
    /** Description of the Field */
    private Dimension intrinsic_size;

    /** Description of the Field */
    private boolean anti_aliased = true;

    /** Constructor for the HTMLPanel object */
    public HTMLPanel() {
        c = new Context();
        if ( true ) {
            // NOTE: currently context is externalized from StyleReference even
            // though it original design they had an ownership relationship (PWW 14/08/04)
            //c.css = new XRStyleReference( c );
        //} else {
            c.css = new TBStyleReference(new NaiveUserAgent());
        }
        XRLog.render( "Using CSS implementation from: " + c.css.getClass().getName() );

        layout = new BodyLayout();
        layout_thread = new LayoutThread(this);
        setLayout( new AbsoluteLayoutManager() );
        documentListeners = new HashMap();
    }
    
    public void setThreadedLayout(boolean threaded) {
        layout_thread.setThreadedLayout(threaded);
    }

    /**
     * Adds the specified Document listener to receive Document events from this
     * component. If listener l is null, no exception is thrown and no action is
     * performed.
     *
     * @param listener  Contains the DocumentListener for DocumentEvent data.
     */
    public void addDocumentListener( DocumentListener listener ) {
        this.documentListeners.put( listener, listener );
    }


    /** Description of the Method */
    public void resetScrollPosition() {
        if ( this.enclosingScrollPane != null ) {
            this.enclosingScrollPane.getVerticalScrollBar().setValue( 0 );
        }
    }


    /**
     * Overrides the default implementation to test for and configure any {@link
     * JScrollPane} parent.
     */
    public void addNotify() {
        super.addNotify();
        Container p = getParent();
        if ( p instanceof JViewport ) {
            Container vp = p.getParent();
            if ( vp instanceof JScrollPane ) {
                setEnclosingScrollPane( (JScrollPane)vp );
            }
        }
    }

    /**
     * Overrides the default implementation unconfigure any {@link JScrollPane}
     * parent.
     */
    public void removeNotify() {
        super.removeNotify();
        setEnclosingScrollPane( null );
    }


    /**
     * Description of the Method
     *
     * @param g  PARAM
     */
    public void paintComponent( Graphics g ) {
        if ( anti_aliased ) {
            ( (Graphics2D)g ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        }

        // if this is the first time painting this document, then calc layout
        if ( body_box == null ) {
            calcLayout( g );
        }

        if ( doc == null ) {
            return;
        }

        newContext( g );
        layout_thread.startRender(g);
    }


    /** Recalculate the layout of the panel. Normally
     * developers should never need to call this. Call
     * repaint or validate instead.
     */
    protected void calcLayout() {
        // set body box to null to trigger new layout
        body_box = null;
        /*
        calcLayout( this.getGraphics() );
        this.setOpaque( false );
        */
    }


    /**
     * Recalculate the layout of the panel. Only
     * called by paintComponent(). Use calcLayout() instead.
     *
     * @param g  PARAM
     */
    protected void calcLayout( Graphics g ) {
        layout_thread.startLayout(g);
    }


    protected LayoutThread layout_thread;
    protected void startLayout(Graphics g) {
        this.removeAll();
        if ( g == null ) {
            return;
        }
        if ( doc == null ) {
            return;
        }

        Element html = (Element)doc.getDocumentElement();
        //Element body = x.child( html, "body" );
        //body = html;

        // set up CSS
        newContext( g );
        c.setMaxWidth( 0 );
        //long start_time = new java.util.Date().getTime();
        //u.p("layout = " + layout);
        body_box = layout.layout( c, html );
        //u.p("after layout: " + body_box);
        //long end_time = new java.util.Date().getTime();

        if ( enclosingScrollPane != null ) {
            if ( this.body_box != null ) {
                this.enclosingScrollPane.getViewport().setBackground( body_box.background_color );
            }
        }

        intrinsic_size = new Dimension( c.getMaxWidth(), layout.contents_height );
        if(enclosingScrollPane != null) {
            //u.p("enclosing scroll pane = " + this.enclosingScrollPane);
            int view_height = this.enclosingScrollPane.getViewport().getHeight();
        
            // resize the outter most box incase it is too small for the viewport
            if(intrinsic_size.getHeight() < view_height) {
                if(body_box != null) {
                    body_box.height = view_height;
                }
            }
        }
        
        
        if ( !intrinsic_size.equals( this.getSize() ) ) {
            this.setPreferredSize( intrinsic_size );
            this.revalidate();
        }

        this.fireDocumentLoaded();
    }


    /* ========= The box finding routines. Should probably move out to another
     class */

    /**
     * Description of the Method
     *
     * @param x  PARAM
     * @param y  PARAM
     * @return   Returns
     */
    public Box findBox( int x, int y ) {
        return findBox( this.body_box, x, y );
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     * @param x    PARAM
     * @param y    PARAM
     * @return     Returns
     */
    public Box findBox( Box box, int x, int y ) {

        Iterator it = box.getChildIterator();
        while ( it.hasNext() ) {
            Box bx = (Box)it.next();
            int tx = x;
            int ty = y;
            tx -= bx.x;
            tx -= bx.totalLeftPadding();
            ty -= bx.y;
            ty -= bx.totalTopPadding();

            // test the contents
            Box retbox = null;
            retbox = findBox( bx, tx, ty );
            if ( retbox != null ) {
                return retbox;
            }

            // test the box itself
            int tty = y;
            if ( bx instanceof InlineBox ) {
                InlineBox ibx = (InlineBox)bx;
                LineBox lbx = (LineBox)box;
                int off = lbx.baseline + ibx.y - ibx.height;
                tty -= off;
            }

            if ( bx.contains( x - bx.x, tty - bx.y ) ) {
                return bx;
            }
        }

        return null;
    }


    /**
     * Description of the Method
     *
     * @param x  PARAM
     * @param y  PARAM
     * @return   Returns
     */
    public int findBoxX( int x, int y ) {
        return findBoxX( this.body_box, x, y );
    }


    /**
     * Description of the Method
     *
     * @param box  PARAM
     * @param x    PARAM
     * @param y    PARAM
     * @return     Returns
     */
    public int findBoxX( Box box, int x, int y ) {
        //u.p("findBox(" + box + " at ("+x+","+y+")");
        Iterator it = box.getChildIterator();

        while ( it.hasNext() ) {
            Box bx = (Box)it.next();
            int tx = x;
            int ty = y;
            tx -= bx.x;
            tx -= bx.totalLeftPadding();
            ty -= bx.y;
            ty -= bx.totalTopPadding();

            // test the contents
            int retbox = findBoxX( bx, tx, ty );
            if ( retbox != -1 ) {
                return retbox;
            }

            int tty = y;
            if ( bx instanceof InlineBox ) {
                InlineBox ibx = (InlineBox)bx;
                LineBox lbx = (LineBox)box;
                //u.p("inline = " + ibx);
                //u.p("inline y = " + ibx.y);
                //u.p("inline height = " + ibx.height);
                //u.p("line = " + lbx);
                int off = lbx.baseline + ibx.y - ibx.height;
                //u.p("off = " + off);
                tty -= off;
            }

            // test the box itself
            //u.p("bx test = " + bx + " " + x +","+y);
            if ( bx.contains( x - bx.x, tty - bx.y ) ) {
                return x - bx.x;
            }
        }

        return -1;
    }

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void componentHidden( ComponentEvent e ) { }

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void componentMoved( ComponentEvent e ) { }

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void componentResized( ComponentEvent e ) {
        calcLayout();
    }

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void componentShown( ComponentEvent e ) { }


    /** Description of the Method */
    public void printTree() {
        printTree( this.body_box, "" );
    }

    /**
     * Sets the documentRelative attribute of the HTMLPanel object
     *
     * @param filename       The new documentRelative value
     * @exception Exception  Throws
     */
    public void setDocumentRelative( String filename )
        throws Exception {
        if ( c != null && ( !filename.startsWith( "http" ) ) ) {
            URL base = new URL( c.getBaseURL(), filename );
            XRLog.load( "Loading URL " + base );
            Document dom = x.loadDocument( base );
            //URL base = new File(filename).toURL();
            setDocument( dom, base );
            return;
        }
        setDocument( x.loadDocument( filename ), new File( filename ).toURL() );
    }

    /**
     * Sets the errorHandler attribute of the HTMLPanel object
     *
     * @param error_handler  The new errorHandler value
     */
    public void setErrorHandler( ErrorHandler error_handler ) {
        this.error_handler = error_handler;
    }


    /**
     * Sets the document attribute of the HTMLPanel object
     *
     * @param filename       The new document value
     * @exception Exception  Throws
     */
    public void setDocument( String filename )
        throws Exception {
        URL url = new File( filename ).toURL();
        setDocument( loadDocument( url ), url );
    }


    /**
     * Sets the document attribute of the HTMLPanel object
     *
     * @param url            The new document value
     * @exception Exception  Throws
     */
    public void setDocument( URL url )
        throws Exception {
        setDocument( loadDocument( url ), url );
    }


    /**
     * Sets the document attribute of the HTMLPanel object
     *
     * @param doc  The new document value
     */
    public void setDocument( Document doc ) {
        setDocument( doc, null );
    }

    /**
     * Sets the document attribute of the HTMLPanel object
     *
     * @param doc  The new document value
     * @param url  The new document value
     */
    public void setDocument( Document doc, URL url) {
        resetScrollPosition();
        this.doc = doc;
        this.url = url;

        //have to do this first
        c.setBaseURL( url );
        u.p("doc = " + doc);
        u.p("Url = " + url);
        c.css.setDocumentContext(c, new XhtmlNamespaceHandler(), new StaticXhtmlAttributeResolver(), doc);
        //c.css.setDocumentContext(c, null, null, doc);

        calcLayout();
        repaint();
    }
    
    URL url;
    public URL getURL() {
        return this.url;
    }
    


    /**
     * Sets the antiAliased attribute of the HTMLPanel object
     *
     * @param anti_aliased  The new antiAliased value
     */
    public void setAntiAliased( boolean anti_aliased ) {
        this.anti_aliased = anti_aliased;
    }


    /**
     * Sets the size attribute of the HTMLPanel object
     *
     * @param d  The new size value
     */
    public void setSize( Dimension d ) {
        //u.p("set size called");
        super.setSize( d );
        this.calcLayout();
    }

    /**
     * Gets the rootBox attribute of the HTMLPanel object
     *
     * @return   The rootBox value
     */
    public Box getRootBox() {
        return body_box;
    }

    /**
     * Gets the context attribute of the HTMLPanel object
     *
     * @return   The context value
     */
    public Context getContext() {
        return c;
    }
    
    public Document getDocument() {
        return doc;
    }

    /**
     * Gets the documentTitle attribute of the HTMLPanel object
     *
     * @return   The documentTitle value
     */
    public String getDocumentTitle() {
        String title = "";
        try {
            Element root = this.doc.getDocumentElement();
            Node node =
                    (Node)XPathAPI.selectSingleNode( root, "//head/title/text()" );
            if ( node == null ) {
                XRLog.exception( "Apparently no title element for this document." );
                title = "TITLE UNKNOWN";
            } else {
                title = node.getNodeValue();
            }
        } catch ( Exception ex ) {
            XRLog.exception( "Error retrieving document title. " + ex.getMessage() );
            title = "";
        }
        return title;
    }

    /** Description of the Method */
    protected void fireDocumentLoaded() {
        Iterator it = this.documentListeners.keySet().iterator();
        while ( it.hasNext() ) {
            DocumentListener list = (DocumentListener)it.next();
            list.documentLoaded();
        }
    }


    /**
     * Description of the Method
     *
     * @param url            PARAM
     * @return               Returns
     * @exception Exception  Throws
     */
    private Document loadDocument( final URL url )
        throws Exception {
        /*
         * XRDocument xrDoc = XRDocumentFactory.loadDocument(null, url);
         * return xrDoc.getDOMDocument();
         */
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler( error_handler );
        return builder.parse( url.openStream() );
    }


    /**
     * Description of the Method
     *
     * @param g  PARAM
     */
    private void newContext( Graphics g ) {
        //u.p("new context begin");
        Point origin = new Point( 0, 0 );
        Point last = new Point( 0, 0 );
        c.canvas = this;
        c.graphics = g;
        // set up the dimensions of the html canvas
        //Rectangle dimensions = new Rectangle(this.getWidth(),this.getHeight());//layout.bwidth, layout.bheight);
        //c.canvas_graphics = g.create();
        //c.setExtents(new Rectangle(0,0,this.getWidth(),this.getHeight()));
        //u.p("viewport size = " + viewport.getSize());
        if ( enclosingScrollPane != null ) {
            Rectangle bnds = enclosingScrollPane.getViewportBorderBounds();
            c.setExtents( new Rectangle( 0,0, bnds.width, bnds.height) );
        } else {
            c.setExtents( new Rectangle( 200, 200 ) );
        }

        //c.setExtents(new Rectangle(0,0,viewport.getWidth(),viewport.getHeight()));
        c.viewport = this.enclosingScrollPane;
        c.cursor = last;
        c.setMaxWidth( 0 );
        //u.p("new context end");
        //u.p("c = " + c);
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     * @param tab  PARAM
     */
    private void printTree( Box box, String tab ) {
        //u.p( tab + "Box = " + box );
        Iterator it = box.getChildIterator();
        while ( it.hasNext() ) {
            Box bx = (Box)it.next();
            printTree( bx, tab + " " );
        }

        if ( box instanceof InlineBox ) {
            InlineBox ib = (InlineBox)box;
            if ( ib.sub_block != null ) {
                printTree( ib.sub_block, tab + " " );
            }
        }
    }

    /**
     * The method is invoked by {@link #addNotify} and {@link #removeNotify} to
     * ensure that any enclosing {@link JScrollPane} works correctly with this
     * panel. This method can be safely invoked with a <tt>null</tt> scrollPane.
     *
     * @param scrollPane  the enclosing {@link JScrollPane} or <tt>null</tt> if
     *      the panel is no longer enclosed in a {@link JScrollPane}.
     */
    private void setEnclosingScrollPane( JScrollPane scrollPane ) {
        // if a scrollpane is already installed we remove it.
        if ( enclosingScrollPane != null ) {
            enclosingScrollPane.removeComponentListener( this );
        }

        enclosingScrollPane = scrollPane;

        if ( enclosingScrollPane != null ) {
            enclosingScrollPane.addComponentListener( this );
        }
    }
    
    
}
class LayoutThread implements Runnable {
    private boolean done;
    private Graphics graphics;
    private HTMLPanel panel;
    private boolean threaded;
    public LayoutThread(HTMLPanel panel) {
        this.panel = panel;
        done = true;
        graphics = null;
        threaded = true;
    }
    
    public void setThreadedLayout(boolean threaded) {
        this.threaded = threaded;
    }
    
    public synchronized void startLayout(Graphics g) {
        if(isLayoutDone()) {
            //u.p("really starting new thread");
            done = false;
            graphics = g;
            if(threaded) {
                new Thread(this).start();
            } else {
                run();
            }
        } else {
            //u.p("layout already in progress. skipping layout");
        }
    }
    
    public void run() {
        // u.p("layout thread starting");
        // u.p("graphics = " + graphics);
        panel.startLayout(graphics);
        this.completeLayout();
    }
    
    // skip for now
    private synchronized void completeLayout() {
        // u.p("layout thread ending");
        done = true;
        graphics = null;
        panel.repaint();
        // u.p("body box = " + panel.body_box );
    }
    
    // always done because not really threaded yet
    public synchronized boolean isLayoutDone() {
        return done;
    }
    
    public synchronized void startRender(Graphics g) {
        g.setColor(Color.black);
        if(this.isLayoutDone()) {
            if(panel.body_box != null) {
                //u.p("really painting: " + panel.body_box);
                try {
                    // u.p("context = " + panel.c);
                    panel.layout.getRenderer().paint( panel.c, panel.body_box );
                } catch (Throwable thr) {
                    u.p("current thread = " + Thread.currentThread());
                    u.p(thr);
                    thr.printStackTrace();
                }
            } else {
                g.drawString("body box is null", 50,50);
                u.p("body box is null");
            }
        } else {
            g.drawString("still doing layout", 50,50);
            //u.p("still doing layout");
        }
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.22  2004/11/10 04:46:12  tobega
 * no message
 *
 * Revision 1.21  2004/11/07 23:24:19  joshy
 * added menu item to generate diffs
 * added diffs for multi-colored borders and inline borders
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.20  2004/11/05 23:59:10  tobega
 * "New" matching and styling
 *
 * Revision 1.19  2004/11/04 21:51:17  tobega
 * Preparation for new matching/styling code
 *
 * Revision 1.18  2004/11/03 23:54:34  joshy
 * added hamlet and tables to the browser
 * more support for absolute layout
 * added absolute layout unit tests
 * removed more dead code and moved code into layout factory
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.17  2004/11/01 14:24:19  joshy
 * added a boolean for turning off threading
 * fixed the diff tests
 * removed some dead code
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/10/28 14:18:23  joshy
 * cleaned up the htmlpanel and made more of the variables protected
 * fixed the bug where the body is too small for the viewport
 * fixed the bug where the screen isn't re-laid out when the window is resized
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/10/28 13:46:33  joshy
 * removed dead code
 * moved code about specific elements to the layout factory (link and br)
 * fixed form rendering bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/10/27 13:39:57  joshy
 * moved more rendering code out of the layouts
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/10/27 13:17:02  joshy
 * beginning to split out rendering code
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/10/27 04:08:44  joshy
 * removed debugging code
 *
 * Revision 1.11  2004/10/26 00:13:14  joshy
 * added threaded layout support to the HTMLPanel
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/10/23 13:51:54  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 * Revision 1.9  2004/10/18 12:12:26  pdoubleya
 * Changed to use XRLog for logging.
 *
 * Revision 1.8  2004/10/14 15:45:22  pdoubleya
 * Reformatted.
 *
 * Revision 1.7  2004/10/14 15:43:32  pdoubleya
 * Reads location of default.CSS from configuration file.
 *
 *
 */

