//-{{{ header & license
/*
 * TestCascade.java
 * Copyright (c) 2004 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
//}}}

package com.pdoubleya.xhtmlrenderer.css;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleSheet;

import com.steadystate.css.parser.CSSOMParser;

import org.joshy.html.Context;

import com.pdoubleya.xhtmlrenderer.css.impl.XRElementImpl;
import com.pdoubleya.xhtmlrenderer.css.impl.XRStyleRuleImpl;
import com.pdoubleya.xhtmlrenderer.css.impl.XRStyleSheetImpl;



/**
 * New class
 *
 * @author    Patrick Wright
 *
 */
public class TestCascade {

    /**
     * Description of the Method
     *
     * @param args  PARAM
     */
    public static void main( String args[] ) {
        try {
            new TestCascade().run( args[0] );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        System.out.println( "Done. Exiting." );
    }


    /**
     * Main processing method for the TestCascade object
     *
     * @param listFile  PARAM
     */
    public void run( String listFile ) {
        try {
            Context context = null;
            Document doc = loadDocument( new File( "demos\\one-line-with-h1.xhtml" ).toURL() );
            Element root = doc.getDocumentElement();
            Element h1 = findByTagName( root, "h1" );
            if ( h1 == null ) {
                System.out.println( "No h1 element in that html file" );
                return;
            }
            XRElement parentElement = new XRElementImpl( root, null );
            XRElement targetElement = new XRElementImpl( h1, parentElement );

            List sheets = loadSheets( listFile );
            List allRules = new ArrayList();
            Iterator iter = null;

            Iterator sheetIter = sheets.iterator();
            while ( sheetIter.hasNext() ) {
                XRStyleSheet sheet = (XRStyleSheet)sheetIter.next();

                iter = sheet.styleRules();
                while ( iter.hasNext() ) {
                    allRules.add( iter.next() );
                }
            }

            Collections.sort( allRules, XRStyleRuleImpl.STYLE_RULE_COMPARATOR );
            iter = allRules.iterator();
            while ( iter.hasNext() ) {
                XRStyleRule styleRule = (XRStyleRule)iter.next();
                System.out.println( styleRule );
                targetElement.addMatchedStyle( styleRule );
            }

            System.out.println( "\n" );
            System.out.println( "Properties after derivation/cascade:" );

            XRRule derived = targetElement.derivedStyle();
            Iterator props = derived.listXRProperties();
            while ( props.hasNext() ) {
                XRProperty prp = (XRProperty)props.next();
                System.out.println( "Property: " + prp.propertyName() );
                XRValue val = derived.propertyByName( context, prp.propertyName() ).actualValue();
                System.out.println( "  " + prp.propertyName() + " = " + val );
                System.out.println( "    as float: " + val.asFloat() );
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }


    /**
     * Description of the Method
     *
     * @param node     PARAM
     * @param tagName  PARAM
     * @return         Returns
     */
    private Element findByTagName( Node node, String tagName ) {
        if ( node instanceof Element ) {
            if ( node instanceof Element && ( (Element)node ).getTagName().equals( tagName ) ) {
                return (Element)node;
            }
        }

        NodeList nl = node.getChildNodes();
        Element found = null;
        for ( int i = 0, len = nl.getLength(); i < len && found == null; i++ ) {
            found = findByTagName( nl.item( i ), tagName );
        }
        return found;
    }


    /**
     * Description of the Method
     *
     * @param fileList  PARAM
     * @return          Returns
     */
    private List loadSheets( String fileList ) {
        List sheets = new ArrayList();
        try {
            File sheetList = new File( fileList );
            if ( !sheetList.exists() ) {
                System.out.println( "can't find file list " + fileList );
                return null;
            }

            LineNumberReader lnr = new LineNumberReader( new FileReader( sheetList ) );
            String cssfile = null;
            int seq = 0;
            while ( ( cssfile = lnr.readLine() ) != null ) {
                String detl[] = cssfile.split( "," );
                String filename = detl[0].trim();
                int origin = getOrigin( detl[1].trim() );
                sheets.add( loadSheet( new File( filename ), seq++, origin ) );
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        return sheets;
    }


    /**
     * Description of the Method
     *
     * @param cssfile   PARAM
     * @param sheetSeq  PARAM
     * @param origin    PARAM
     * @return          Returns
     */
    private XRStyleSheet loadSheet( File cssfile, int sheetSeq, int origin ) {
        XRStyleSheet sheet = null;
        try {
            System.out.println( "Loading CSS: " + cssfile.getAbsolutePath() );

            InputStream stream = null;
            Reader isReader = null;

            stream = new FileInputStream( cssfile );
            isReader = new InputStreamReader( stream );
            CSSOMParser parser = new CSSOMParser();

            org.w3c.css.sac.InputSource is = new org.w3c.css.sac.InputSource( isReader );

            CSSStyleSheet style = parser.parseStyleSheet( is );

            switch ( origin ) {
                case XRStyleSheet.USER_AGENT:
                    sheet = XRStyleSheetImpl.newUserAgentStyleSheet( style, sheetSeq );
                    break;
                case XRStyleSheet.AUTHOR:
                    sheet = XRStyleSheetImpl.newAuthorStyleSheet( style, sheetSeq );
                    break;
                case XRStyleSheet.USER:
                    sheet = XRStyleSheetImpl.newUserStyleSheet( style, sheetSeq );
                    break;
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        return sheet;
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

        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = fact.newDocumentBuilder();

        builder.setErrorHandler( null );

        return builder.parse( url.openStream() );
    }


    /**
     * Gets the origin attribute of the TestCascade object
     *
     * @param originDesc  PARAM
     * @return            The origin value
     */
    private int getOrigin( String originDesc ) {
        if ( originDesc.toUpperCase().equals( "USER_AGENT" ) ) {
            return XRStyleSheet.USER_AGENT;
        } else if ( originDesc.toUpperCase().equals( "USER" ) ) {
            return XRStyleSheet.USER;
        } else if ( originDesc.toUpperCase().equals( "AUTHOR" ) ) {
            return XRStyleSheet.AUTHOR;
        } else {
            return XRStyleSheet.USER_AGENT;
        }
    }

}

