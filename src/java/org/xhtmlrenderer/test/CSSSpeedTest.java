
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
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.DefaultCSSMarker;
import org.xhtmlrenderer.css.CSSBank;
import org.xhtmlrenderer.css.XRStyleSheet;
import org.xhtmlrenderer.css.bridge.XRStyleReference;
import org.xhtmlrenderer.layout.BodyLayout;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.util.x;


/**
 * Description of the Class
 *
 * @author   Patrick Wright
 */
public class CSSSpeedTest {

    /**
     * The main program for the CSSSpeedTest class
     *
     * @param args           The command line arguments
     * @exception Exception  Throws
     */
    public static void main( String[] args )
        throws Exception {

        // load doc

        String fileURL = Configuration.valueFor( "xr.test.files.hamlet" );
        InputStream is = GeneralUtil.openStreamFromClasspath( fileURL );
        if ( is == null ) {
            System.err.println( "Can't find test file on CLASSPATH: " + fileURL );
            return;
        }
        Document doc = x.loadDocument( is );

        Element html = (Element)doc.getDocumentElement();

        // create buffer

        BufferedImage buff = new BufferedImage( 500, 500, BufferedImage.TYPE_4BYTE_ABGR );

        // get graphics

        Graphics g = buff.getGraphics();

        // create context
        Context c = null;

        c = new Context();

        c.css = new XRStyleReference( c );

        runLoopTest( c, g, html );
    }


    /**
     * Description of the Method
     *
     * @param c              PARAM
     * @param g              PARAM
     * @param html           PARAM
     * @exception Exception  Throws
     */
    public static void runLoopTest( Context c, Graphics g, Element html )
        throws Exception {

        Element body = x.child( html, "body" );

        Point origin = new Point( 0, 0 );

        Point last = new Point( 0, 0 );

        Object marker = new DefaultCSSMarker();

        String defaultStyleSheetLocation = Configuration.valueFor( "xr.css.user-agent-default-css" );
        if ( marker.getClass().getResourceAsStream( defaultStyleSheetLocation ) != null ) {
            URL stream = marker.getClass().getResource( defaultStyleSheetLocation );
            String str = u.inputstream_to_string( stream.openStream() );
            c.css.parse( new StringReader( str ), XRStyleSheet.USER_AGENT );
        } else {
            System.err.println(
                    "Can't load default CSS from " + defaultStyleSheetLocation + "." +
                    "This file must be on your CLASSPATH. Please check before continuing." );
        }

        c.css.parseInlineStyles( html );

        c.graphics = g;

        c.setExtents( new Rectangle( 0, 0, 500, 500 ) );

        c.cursor = last;

        c.setMaxWidth( 0 );

        // create layout

        BodyLayout layout = new BodyLayout();

        int total = 0;
        int loop = 30;
        for ( int i = 0; i < loop; i++ ) {

            u.sleep( 100 );

            long start_time = new java.util.Date().getTime();

            // execute layout

            //u.p("body = " + body);

            Box body_box = layout.layout( c, body );

            long end_time = new java.util.Date().getTime();

            u.p( i + ") ending count = " + ( end_time - start_time ) + " msec" );

            if ( i > 0 ) {
                total += ( end_time - start_time );
            }
        }

        u.p( "avg = " + ( total / loop ) );

    }

}

