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
package org.xhtmlrenderer.test;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.DefaultCSSMarker;
import org.xhtmlrenderer.css.CSSBank;
import org.xhtmlrenderer.layout.BodyLayout;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.util.x;

/**
 * Description of the Class
 *
 * @author   empty
 */
public class SpeedTest {

    /**
     * The main program for the SpeedTest class
     *
     * @param args           The command line arguments
     * @exception Exception  Throws
     */
    public static void main( String[] args )
        throws Exception {

        // load doc

        Document doc = x.loadDocument( "demos/paragraph.xhtml" );

        Element html = (Element)doc.getDocumentElement();

        Element body = x.child( html, "body" );

        // create buffer

        BufferedImage buff = new BufferedImage( 500, 500, BufferedImage.TYPE_4BYTE_ABGR );

        // get graphics

        Graphics g = buff.getGraphics();


        // create context

        Context c = new Context();

        Point origin = new Point( 0, 0 );

        Point last = new Point( 0, 0 );

        //c.canvas = this;

        c.css = new CSSBank();
        
        c.css.setDocumentContext(c,  null, null,  html.getOwnerDocument());

        /*Object marker = new DefaultCSSMarker();

        //u.p("getting: " + marker.getClass().getResource("default.css"));

        InputStream stream = marker.getClass().getResourceAsStream( "default.css" );

        c.css.parse( new InputStreamReader( stream ) );

        c.css.parseInlineStyles( html );*/

        c.graphics = g;

        c.setExtents( new Rectangle( 0, 0, 500, 500 ) );

        //c.viewport = this.viewport;

        c.cursor = last;

        c.setMaxWidth( 0 );


        // create layout

        BodyLayout layout = new BodyLayout();


        int total = 0;

        for ( int i = 0; i < 10; i++ ) {

            u.sleep( 100 );

            long start_time = new java.util.Date().getTime();

            // execute layout

            //u.p("body = " + body);

            Box body_box = layout.layout( c, body );

            long end_time = new java.util.Date().getTime();

            u.p( "ending count = " + ( end_time - start_time ) + " msec" );

            total += ( end_time - start_time );

        }

        u.p( "avg = " + ( total / 10 ) );

    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2004/11/04 21:51:49  tobega
 * Preparation for new matching/styling code
 *
 * Revision 1.4  2004/10/23 14:01:42  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

