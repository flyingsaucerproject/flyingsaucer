/*
 * {{{ header & license
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
 * }}}
 */
package com.pdoubleya.xhtmlrenderer.css;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.*;
import org.w3c.css.sac.*;

import org.w3c.dom.*;
import org.w3c.dom.css.*;

import com.pdoubleya.xhtmlrenderer.css.constants.*;
import com.pdoubleya.xhtmlrenderer.css.impl.*;

import com.steadystate.css.parser.*;

/**
 * New class
 *
 * @author    Patrick Wright
 * @created   August 4, 2004
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
      List sheets = loadSheets( listFile );

      Iterator sheetIter = sheets.iterator();

      List allRules = new ArrayList();
      Iterator iter = null;

      XRElement parentElement = new XRElementImpl(null, null, null);
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
        parentElement.addMatchedStyle(styleRule);
      }
      
      System.out.println("\n\n");
      System.out.println("Properties after derivation/cascade:" );
      XRRuleSet derived = parentElement.derivedStyle();
      Iterator props = derived.listXRProperties();
      while ( props.hasNext()) {
        System.out.println(props.next());
      }
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
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
   * @param sheet  PARAM
   */
  private void dumpSheet( XRStyleSheet sheet ) {
    StringBuffer sb = new StringBuffer();
    sheet.dump( sb );
    System.out.println( sb.toString() );
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
      InputStream stream = null;
      Reader isReader = null;

      stream = new FileInputStream( cssfile );
      isReader = new InputStreamReader( stream );
      CSSOMParser parser = new CSSOMParser();

      InputSource is = new InputSource( isReader );

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
  
  private int getOrigin(String originDesc) {
    if ( originDesc.toUpperCase().equals("USER_AGENT")) {
      return XRStyleSheet.USER_AGENT; 
    } else if ( originDesc.toUpperCase().equals("USER")) {
      return XRStyleSheet.USER; 
    } else if ( originDesc.toUpperCase().equals("AUTHOR")) {
      return XRStyleSheet.AUTHOR; 
    } else {
      return XRStyleSheet.USER_AGENT; 
    }
  }
}

