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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.*;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.xpath.XPathAPI;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.forms.AbsoluteLayoutManager;
import org.xhtmlrenderer.layout.BodyLayout;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.x;
import org.xml.sax.ErrorHandler;


/**
 * A Swing {@link javax.swing.JPanel} that encloses the Flying Saucer renderer
 * for easy integration into Swing applications.
 *
 * @author   Joshua Marinacci
 */
public abstract class BasicPanel extends JPanel implements ComponentListener {
    /** Description of the Field */
    public Element hovered_element = null;

    /** Description of the Field  */
    protected Document doc = null;

    /** Description of the Field  */
    protected Box body_box = null;

    /** Description of the Field  */
    protected ErrorHandler error_handler;

    /** Description of the Field  */
    protected BodyLayout layout;

    /** Description of the Field */
    protected RenderingContext ctx;

    /** Description of the Field */
    protected LayoutThread layout_thread;

    /** Description of the Field */
    protected URL url;

    /** Description of the Field */
    boolean inside = false;

    /** Description of the Field  */
    private Map documentListeners;

    /** Description of the Field  */
    private JScrollPane enclosingScrollPane;

    /** Description of the Field  */
    private Dimension intrinsic_size;

    /** Description of the Field  */
    private boolean anti_aliased = true;

    /** Constructor for the BasicPanel object  */
    public BasicPanel() {

        layout = new BodyLayout();
        layout_thread = new LayoutThread( this );
        setLayout( new AbsoluteLayoutManager() );
        documentListeners = new HashMap();
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


    /** Description of the Method  */
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
            // TODO:
            // ( (Graphics2D)g ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        }

        if ( doc == null ) {
            return;
        }

        // if this is the first time painting this document, then calc layout
        if ( body_box == null ) {
            calcLayout( g );
        }

        newContext( (Graphics2D)g );
        layout_thread.startRender( g );
    }

    /**
     * Description of the Method
     *
     * @param g  PARAM
     */
    public void startLayout( Graphics g ) {
        this.removeAll();
        if ( g == null ) {
            return;
        }
        if ( doc == null ) {
            return;
        }

        Element html = (Element)doc.getDocumentElement();
        
        // CLEAN
        //Element body = x.child( html, "body" );
        //body = html;

        // set up CSS
        newContext( (Graphics2D)g );
        getContext().setMaxWidth( 0 );
        
        // DEBUG
        //long start_time = new java.util.Date().getTime();
        XRLog.layout( Level.FINEST, "layout = " + layout);
        
        getRenderingContext().getTextRenderer().setupGraphics( getContext().getGraphics() );
        body_box = layout.layout( getContext(), html );
        
        XRLog.layout( Level.FINEST, "is a fixed child: " + body_box.isChildrenExceedBounds());
        
        // if there is a fixed child then we need to set opaque to false
        // so that the entire viewport will be repainted. this is slower
        // but that's the hit you get from using fixed layout
        if ( body_box.isChildrenExceedBounds() ) {
            setOpaque( false );
        } else {
            setOpaque( true );
        }

        getRenderingContext().root_box = body_box;
        
        XRLog.layout( Level.FINEST, "after layout: " + body_box);
        //long end_time = new java.util.Date().getTime();

        if ( enclosingScrollPane != null ) {
            if ( this.body_box != null ) {
                this.enclosingScrollPane.getViewport().setBackground( body_box.background_color );
            }
        }

        intrinsic_size = new Dimension( getContext().getMaxWidth(), layout.contents_height );
        if ( enclosingScrollPane != null ) {
            XRLog.layout( Level.FINEST, "enclosing scroll pane = " + this.enclosingScrollPane);
            int view_height = this.enclosingScrollPane.getViewport().getHeight();

            // resize the outter most box incase it is too small for the viewport
            if ( intrinsic_size.getHeight() < view_height ) {
                if ( body_box != null ) {
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

    /*
     * ========= The box finding routines. Should probably move out to another
     * class
     */
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

        if ( box == null ) {
            return null;
        }
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
        XRLog.layout( Level.FINEST, "findBox(" + box + " at ("+x+","+y+")");
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
                XRLog.layout( Level.FINEST, "inline = " + ibx);
                XRLog.layout( Level.FINEST, "inline y = " + ibx.y);
                XRLog.layout( Level.FINEST, "inline height = " + ibx.height);
                XRLog.layout( Level.FINEST, "line = " + lbx);
                int off = lbx.baseline + ibx.y - ibx.height;
                XRLog.layout( Level.FINEST, "off = " + off);
                tty -= off;
            }

            // test the box itself
            XRLog.layout( Level.FINEST, "bx test = " + bx + " " + x +","+y);
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


    /** Description of the Method  */
    public void printTree() {
        printTree( this.body_box, "" );
    }

    /**
     * Sets the threadedLayout attribute of the BasicPanel object
     *
     * @param threaded  The new threadedLayout value
     */
    public void setThreadedLayout( boolean threaded ) {
        layout_thread.setThreadedLayout( threaded );
    }

    /**
     * Sets the renderingContext attribute of the BasicPanel object
     *
     * @param ctx  The new renderingContext value
     */
    public void setRenderingContext( RenderingContext ctx ) {
        this.ctx = ctx;
    }

    /**
     * Sets the document attribute of the BasicPanel object
     *
     * @param doc  The new document value
     * @param url  The new document value
     */
    public void setDocument( Document doc, URL url ) {
        resetScrollPosition();
        this.doc = doc;
        this.url = url;

        //have to do this first
        getRenderingContext().setBaseURL( url );
        StaticXhtmlAttributeResolver ar =
            new StaticXhtmlAttributeResolver() {
                public boolean isHover( org.w3c.dom.Element e ) {
                    if ( e == hovered_element ) {
                        return true;
                    }
                    return false;
                }
            };
        getRenderingContext().getStyleReference().setDocumentContext( getContext(), new XhtmlNamespaceHandler(), ar, doc );

        calcLayout();
        repaint();
    }

    /**
     * Sets the errorHandler attribute of the BasicPanel object
     *
     * @param error_handler  The new errorHandler value
     */
    public void setErrorHandler( ErrorHandler error_handler ) {
        this.error_handler = error_handler;
    }


    /**
     * Sets the antiAliased attribute of the BasicPanel object
     *
     * @param anti_aliased  The new antiAliased value
     */
    public void setAntiAliased( boolean anti_aliased ) {
        this.anti_aliased = anti_aliased;
    }


    /**
     * Sets the size attribute of the BasicPanel object
     *
     * @param d  The new size value
     */
    public void setSize( Dimension d ) {
        XRLog.layout( Level.FINEST, "set size called");
        super.setSize( d );
        this.calcLayout();
    }

    /**
     * Gets the renderingContext attribute of the BasicPanel object
     *
     * @return   The renderingContext value
     */
    public RenderingContext getRenderingContext() {
        return ctx;
    }

    /**
     * Gets the intrinsicSize attribute of the BasicPanel object
     *
     * @return   The intrinsicSize value
     */
    public Dimension getIntrinsicSize() {
        return intrinsic_size;
    }

    /**
     * Gets the uRL attribute of the BasicPanel object
     *
     * @return   The uRL value
     */
    public URL getURL() {
        return this.url;
    }

    /**
     * Gets the rootBox attribute of the BasicPanel object
     *
     * @return   The rootBox value
     */
    public Box getRootBox() {
        return body_box;
    }

    /**
     * Gets the context attribute of the BasicPanel object
     *
     * @return   The context value
     */
    public Context getContext() {
        return getRenderingContext().getContext();
    }

    /**
     * Gets the document attribute of the BasicPanel object
     *
     * @return   The document value
     */
    public Document getDocument() {
        return doc;
    }

    /**
     * Gets the documentTitle attribute of the BasicPanel object
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

    /**
     * Gets the fixedRectangle attribute of the BasicPanel object
     *
     * @return   The fixedRectangle value
     */
    public Rectangle getFixedRectangle() {
        return enclosingScrollPane.getViewportBorderBounds();
    }


    /**
     * Recalculate the layout of the panel. Normally developers should never
     * need to call this. Call repaint or validate instead.
     */
    protected void calcLayout() {
        // set body box to null to trigger new layout
        body_box = null;
        /* CLEAN
         * calcLayout( this.getGraphics() );
         * this.setOpaque( false );
         */
    }


    /**
     * Recalculate the layout of the panel. Only called by paintComponent(). Use
     * calcLayout() instead.
     *
     * @param g  PARAM
     */
    protected void calcLayout( Graphics g ) {
        layout_thread.startLayout( g );
    }

    /** Description of the Method */
    protected void doRender() {
        layout.getRenderer().paint( getRenderingContext().getContext(),
                body_box );
    }

    /** Description of the Method  */
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
     * @param url         PARAM
     * @return            Returns
     * @throws Exception  Throws
     */
    protected Document loadDocument( final URL url )
        throws Exception {
        /* CLEAN
         * XRDocument xrDoc = XRDocumentFactory.loadDocument(null, url);
         * return xrDoc.getDOMDocument();
         */
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler( error_handler );
        return builder.parse( url.openStream() );
    }

    /**
     * Sets the documentRelative attribute of the BasicPanel object
     *
     * @param filename    The new documentRelative value
     * @throws Exception  Throws
     */
    protected void setDocumentRelative( String filename )
        throws Exception {
        if ( getContext() != null && ( !filename.startsWith( "http" ) ) ) {
            URL base = new URL( getRenderingContext().getBaseURL(), filename );
            XRLog.load( "Loading URL " + base );
            Document dom = x.loadDocument( base );
            
            // CLEAN
            // URL base = new File(filename).toURL();
            
            setDocument( dom, base );
            return;
        }
        setDocument( x.loadDocument( filename ), new File( filename ).toURL() );
    }


    /**
     * Description of the Method
     *
     * @param g  PARAM
     */
    private void newContext( Graphics2D g ) {
        XRLog.layout( Level.FINEST, "new context begin");
        
        // CLEAN
        //Point origin = new Point( 0, 0 );
        //Point last = new Point( 0, 0 );
        
        getContext().canvas = this;
        getContext().graphics = g;
        
        // CLEAN
        // set up the dimensions of the html canvas
        //Rectangle dimensions = new Rectangle(this.getWidth(),this.getHeight());//layout.bwidth, layout.bheight);
        //c.canvas_graphics = g.create();
        //c.setExtents(new Rectangle(0,0,this.getWidth(),this.getHeight()));
        //XRLog.layout( Level.FINEST, "viewport size = " + viewport.getSize());
        
        if ( enclosingScrollPane != null ) {
            Rectangle bnds = enclosingScrollPane.getViewportBorderBounds();
            getContext().setExtents( new Rectangle( 0, 0, bnds.width, bnds.height ) );
        } else {
            getContext().setExtents( new Rectangle( getWidth(), getHeight() ) );//200, 200 ) );
        }

        // CLEAN
        //getContext().setExtents(new Rectangle(0,0,viewport.getWidth(),viewport.getHeight()));
        
        getContext().viewport = this.enclosingScrollPane;
        
        // CLEAN
        //getContext().cursor = last;
        
        getContext().setMaxWidth( 0 );
        XRLog.layout( Level.FINEST, "new context end");
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     * @param tab  PARAM
     */
    private void printTree( Box box, String tab ) {
        XRLog.layout( Level.FINEST,  tab + "Box = " + box );
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

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/11/16 10:30:20  pdoubleya
 * Changed to use XRLog for logging.
 * Ran code formatter.
 * Marked blocks needed to CLEAN.
 *
 * Revision 1.2  2004/11/16 10:14:11  pdoubleya
 * Was not importing util.x, so was assuming that x was a member variable in the superclass. Added import.
 *
 * Revision 1.1  2004/11/16 07:25:13  tobega
 * Renamed HTMLPanel to BasicPanel
 *
 * Revision 1.33  2004/11/14 21:33:49  joshy
 * new font rendering interface support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.32  2004/11/12 20:54:08  joshy
 * fixed bug where setOpaque would become false for a page with fixed content and then never
 * become true again on another normal page
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.31  2004/11/12 18:51:01  joshy
 * fixed repainting issue for background-attachment: fixed
 * added static util methods and get minimum size to graphics 2d renderer
 * added test for graphics 2d renderer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.30  2004/11/12 17:05:25  joshy
 * support for fixed positioning
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.29  2004/11/12 02:54:38  joshy
 * removed more dead code
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.28  2004/11/12 02:50:59  joshy
 * finished moving base url
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.27  2004/11/12 02:23:59  joshy
 * added new APIs for rendering context, xhtmlpanel, and graphics2drenderer.
 * initial support for font mapping additions
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.26  2004/11/12 01:42:26  tobega
 * oops
 *
 * Revision 1.25  2004/11/12 01:39:07  tobega
 * no message
 *
 * Revision 1.24  2004/11/12 00:20:31  tobega
 * Set up the HoverListener to work properly. Colors are changing!
 *
 * Revision 1.23  2004/11/10 14:34:21  joshy
 * more hover support
 *
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
 * added threaded layout support to the BasicPanel
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

