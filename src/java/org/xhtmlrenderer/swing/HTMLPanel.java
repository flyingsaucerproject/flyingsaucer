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
import org.xhtmlrenderer.DefaultCSSMarker;
import org.xhtmlrenderer.css.CSSBank;
import org.xhtmlrenderer.css.XRStyleSheet;
import org.xhtmlrenderer.css.bridge.XRStyleReference;
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
    public Document doc;
    /** Description of the Field */
    public Context c;
    /** Description of the Field */
    public Box body_box = null;

    /** Description of the Field */
    protected ErrorHandler error_handler;
    /** Description of the Field */
    BodyLayout layout;

    /** Description of the Field */
    Map documentListeners;
    /** Description of the Field */
    private JScrollPane enclosingScrollPane;
    /** Description of the Field */
    private Dimension intrinsic_size;

    /** Description of the Field */
    private boolean anti_aliased = true;

    /** Constructor for the HTMLPanel object */
    public HTMLPanel() {
        c = new Context();
        layout = new BodyLayout();
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
        //g.setColor(Color.blue);
        //g.drawLine(0,0,50,50);
        //u.p("paint() size = " + this.getSize());
        //u.p("viewport size = " + this.viewport.getSize());
        //u.p("w/o scroll = " + this.viewport.getViewportBorderBounds());
        if ( anti_aliased ) {
            ( (Graphics2D)g ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        }
        doPaint( g );
    }

    //public static long timestamp;

    /**
     * Description of the Method
     *
     * @param g  PARAM
     */
    public void doPaint( Graphics g ) {
        //u.p("paint");

        //long start_time = new java.util.Date().getTime();

        if ( body_box == null ) {
            calcLayout( g );
        }

        if ( doc == null ) {
            return;
        }

        newContext( g );
        //u.p("paint");
        layout.paint( c, body_box );
        //long end_time = new java.util.Date().getTime();
        //u.p("dist = " + (end_time-start_time));
    }


    /** Description of the Method */
    public void calcLayout() {
        calcLayout( this.getGraphics() );
        this.setOpaque( false );
    }


    /**
     * Description of the Method
     *
     * @param g  PARAM
     */
    public void calcLayout( Graphics g ) {
        //u.p("calcLayout()");
        this.removeAll();
        //u.p("this = ");
        //u.dump_stack();
        if ( g == null ) {
            //u.p("bad first graphics");
            return;
        }
        //u.p("layout");
        //u.p("size = " + this.getSize());
        //u.dump_stack();
        if ( doc == null ) {
            return;
        }

        Element html = (Element)doc.getDocumentElement();
        Element body = x.child( html, "body" );
        body = html;

        newContext( g );
        // set up CSS
        // the last added is used first
        // start painting
        c.setMaxWidth( 0 );
        long start_time = new java.util.Date().getTime();
        //u.p("starting count");
        body_box = layout.layout( c, body );
        long end_time = new java.util.Date().getTime();
        //u.p("ending count = " + (end_time-start_time) + " msec");

        if ( enclosingScrollPane != null ) {
            if ( this.body_box != null ) {
                // CLN
                //u.p("bcolor = " + body_box.background_color);
                //u.p("body box = " + body_box.hashCode());
                this.enclosingScrollPane.getViewport().setBackground( body_box.background_color );
            }
        }

        //u.p("calced height = " + layout.contents_height);//this.html_height);
        //u.p("max width = " + c.getMaxWidth());
        intrinsic_size = new Dimension( c.getMaxWidth(), layout.contents_height );
        //u.p("intrinsic size = " + intrinsic_size);
        //u.p("real size = " + this.getSize());
        if ( !intrinsic_size.equals( this.getSize() ) ) {
            //u.dump_stack();
            this.setPreferredSize( intrinsic_size );
            //u.p("setting preferred to : " + this.getPreferredSize());
            //this.setSize(intrinsic_size);
            this.revalidate();
            //this.repaint();
            //this.setPreferredSize(intrinsic_size);
        }

        //this.html_height = layout.contents_height;
        /*
         * if(c.getMaxWidth() > this.getSize().getWidth()) {
         * this.max_width = c.getMaxWidth();
         * }
         * //u.p("html height = " + this.html_height);
         * if(c.getMaxWidth() > this.getSize().getWidth()) {
         * this.setPreferredSize(new Dimension((int)c.getMaxWidth(),this.html_height));
         * this.max_width = c.getMaxWidth();
         * this.setMinimumSize(this.getPreferredSize());
         * } else {
         * this.setPreferredSize(new Dimension((int)this.getSize().getWidth(),this.html_height));
         * this.max_width = (int)this.getSize().getWidth();
         * }
         */
        /*
         * u.p("size = " + this.getSize());
         * u.p("pref size = " + this.getPreferredSize());
         * if(!this.getSize().equals(this.getPreferredSize())) {
         * u.p("need a repaint");
         * u.p("size = " + this.getSize());
         * u.p("pref size = " + this.getPreferredSize());
         * super.setSize(this.getPreferredSize());
         * repaint();
         * }
         */
        //c.getGraphics().setColor(Color.blue);
        //c.getGraphics().drawLine(0,0,50,50);
        this.fireDocumentLoaded();
    }


    /*
     * === scrollable implementation ===
     */
    /*
     * public Dimension getPreferredScrollableViewportSize() {
     * u.p("get pref scrll view called");
     * //u.dump_stack();
     * //u.p("size of viewport = " + viewport.getSize());
     * //u.p("size of intrinsic = " + intrinsic_size);
     * u.dump_stack();
     * if(intrinsic_size == null) {
     * return new Dimension(400,400);
     * }
     * if(intrinsic_size.getWidth() > viewport.getWidth()) {
     * //u.p("intrinsic  = " + this.intrinsic_size);
     * return new Dimension((int)intrinsic_size.getWidth(),400);
     * }
     * return null;
     * }
     * public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
     * return 20;
     * }
     * public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
     * return 100;
     * }
     * public boolean getScrollableTracksViewportWidth() {
     * return false;
     * }
     * public boolean getScrollableTracksViewportHeight() {
     * return false;
     * }
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

        if ( box instanceof LineBox || box instanceof InlineBox ) {
            //u.p("findBox(" + box + " at ("+x+","+y+")");
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
            //u.p("bx test = " + bx + " " + x +","+y);
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
    public void setDocument( Document doc, URL url ) {
        resetScrollPosition();
        this.doc = doc;

        Element html = (Element)doc.getDocumentElement();

        if ( 1 == 0 ) {
            c.css = new CSSBank();
        } else {
            // NOTE: currently context is externalized from StyleReference even
            // though it original design they had an ownership relationship (PWW 14/08/04)
            c.css = new XRStyleReference( c );
        }
        XRLog.render( "Using CSS implementation from: " + c.css.getClass().getName() );

        c.setBaseURL( url );

        try {
            Object marker = new DefaultCSSMarker();

            String defaultStyleSheetLocation = Configuration.valueFor( "xr.css.user-agent-default-css" );
            if ( marker.getClass().getResourceAsStream( defaultStyleSheetLocation ) != null ) {
                URL stream = marker.getClass().getResource( defaultStyleSheetLocation );
                String str = u.inputstream_to_string( stream.openStream() );
                c.css.parse( new StringReader( str ),
                            XRStyleSheet.USER_AGENT );
            } else {
                XRLog.exception(
                            "Can't load default CSS from " + defaultStyleSheetLocation + "." +
                            "This file must be on your CLASSPATH. Please check before continuing." );
            }

            c.css.parseDeclaredStylesheets( html );
            c.css.parseLinkedStyles( html );
            c.css.parseInlineStyles( html );
        } catch ( Exception ex ) {
            XRLog.exception("Could not parse CSS in the XHTML source: declared, linked or inline.", ex);
        }

        this.body_box = null;
        calcLayout();
        repaint();
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
            c.setExtents( new Rectangle( enclosingScrollPane.getViewportBorderBounds() ) );
        } else {
            c.setExtents( new Rectangle( 200, 200 ) );
        }

        //c.setExtents(new Rectangle(0,0,viewport.getWidth(),viewport.getHeight()));
        c.viewport = this.enclosingScrollPane;
        c.cursor = last;
        c.setMaxWidth( 0 );
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     * @param tab  PARAM
     */
    private void printTree( Box box, String tab ) {
        u.p( tab + "Box = " + box );
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

