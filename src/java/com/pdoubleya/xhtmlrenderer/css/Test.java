/*
 * {{{ header & license
 * Test.java
 * Copyright (c) 2004 Patrick Wright
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
package com.pdoubleya.xhtmlrenderer.css;
import java.io.File;


import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.w3c.css.sac.InputSource;

import org.w3c.dom.css.CSSStyleSheet;

import com.pdoubleya.xhtmlrenderer.css.impl.XRStyleSheetImpl;

import com.steadystate.css.parser.CSSOMParser;


/**
 * New class
 *
 * @author    Patrick Wright
 *
 */
public class Test {

    /**
     * Description of the Method
     *
     * @param args  PARAM
     */
    public static void main( String args[] ) {
        try {
            String cssname = "/resources/css/default.css";
            if ( args.length > 0 ) {
                cssname = args[0];
            }

            File cssfile = new File( cssname );
            if ( cssfile.exists() ) {
                new Test().run( cssfile );
            } else {
                System.err.println( "No such CSS file: " + cssname );
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        System.out.println( "Done. Exiting." );
    }


    /**
     * Main processing method for the Test object
     *
     * @param cssfile  PARAM
     */
    public void run( File cssfile ) {
        try {
            //XRStyleSheet defaultSheet = loadSheet(new File("default.css"), 0);
            XRStyleSheet xrSheet = loadSheet( cssfile, 1 );

            StringBuffer sb = new StringBuffer();
            xrSheet.dump( sb );
            System.out.println( sb.toString() );

            /*
             * Iterator iter = null;
             * Iterator props = null;
             * XRProperty prop = null;
             * XRRule derivedStyle = null;
             * XRElement parentElement = new XRElementImpl(null, null, null, null);
             * parentElement.addMatchedStyle(xrSheet.ruleBySelector("H1"));
             * XRElement element = new XRElementImpl(null, null, parentElement, null);
             * element.addMatchedStyle(defaultSheet.ruleBySelector("*"));
             * element.addMatchedStyle(xrSheet.ruleBySelector("EM"));
             * derivedStyle = element.derivedStyle();
             * props = derivedStyle.derivedProperties();
             * while ( props.hasNext()) {
             * prop = (XRProperty)props.next();
             * if ( prop != null ) System.out.println(prop);
             * }
             */
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }


    /**
     * Description of the Method
     *
     * @param cssfile   PARAM
     * @param sheetSeq  PARAM
     * @return          Returns
     */
    private XRStyleSheet loadSheet( File cssfile, int sheetSeq ) {
        XRStyleSheet sheet = null;
        try {
            InputStream stream = null;
            Reader isReader = null;

            stream = new FileInputStream( cssfile );
            isReader = new InputStreamReader( stream );
            CSSOMParser parser = new CSSOMParser();

            InputSource is = new InputSource( isReader );

            CSSStyleSheet style = parser.parseStyleSheet( is );

            sheet = new XRStyleSheetImpl( style, sheetSeq, XRStyleSheet.USER_AGENT );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        return sheet;
    }
}

