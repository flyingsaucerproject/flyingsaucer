/*
 * PropertyDeclaration.java
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
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
 *
 */
package org.xhtmlrenderer.swing;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import javax.imageio.ImageIO;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * @author   Torbjörn Gannholm
 */
public class NaiveUserAgent implements org.xhtmlrenderer.extend.UserAgentCallback {

  /** Description of the Field */
  private HashMap imageCache;

  /** Creates a new instance of NaiveUserAgent */
  public NaiveUserAgent() { }

  /**
   * Gets the stylesheet attribute of the NaiveUserAgent object
   *
   * @param uri  PARAM
   * @return     The stylesheet value
   */
  public java.io.Reader getStylesheet( String uri ) {
    java.io.InputStream is = null;
    Reader reader = null;
    try {
      is = ( new java.net.URL( uri ) ).openStream();
      reader = new BufferedReader( new java.io.InputStreamReader( is ) );
    } catch ( java.net.MalformedURLException e ) {
      XRLog.exception( "bad URL given: " + uri, e );
    } catch ( java.io.IOException e ) {
      XRLog.exception( "IO problem for " + uri, e );
    }
    return reader;
  }

  /**
   * Gets the image attribute of the NaiveUserAgent object
   *
   * @param uri  PARAM
   * @return     The image value
   */
  public Image getImage( String uri ) {
    java.io.InputStream is = null;
    Image img = null;
    if ( imageCache != null ) {
      SoftReference ref = (SoftReference)imageCache.get( uri );
      if ( ref != null ) {
        img = (Image)ref.get();
      }
      if ( img != null ) {
        return img;
      }
    }
    try {
      is = ( new java.net.URL( uri ) ).openStream();
    } catch ( FileNotFoundException ex ) {
      XRLog.exception( "Can't find image file for URI: '" + uri + "'; skipping." );
    } catch ( java.net.MalformedURLException e ) {
      XRLog.exception( "Bad URI given for image file: '" + uri + "'" );
    } catch ( java.io.IOException e ) {
      XRLog.exception( "Can't load image file; IO problem for URI '" + uri + "'", e );
    }

    if ( is != null ) {
      try {
        img = ImageIO.read( is );
        if ( imageCache == null ) {
          imageCache = new HashMap();
        }
        imageCache.put( uri, new SoftReference( img ) );
      } catch ( Exception e ) {
        XRLog.exception( "Can't read image file; unexpected problem for URI '" + uri + "'", e );
      }
    }
    return img;
  }

  /**
   * Gets the visited attribute of the NaiveUserAgent object
   *
   * @param uri  PARAM
   * @return     The visited value
   */
  public boolean isVisited( String uri ) {
    return false;
  }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.7  2005/03/28 14:24:22  pdoubleya
 * Remove stack trace on loading images.
 *
 * Revision 1.6  2005/02/02 12:14:01  pdoubleya
 * Clean, format, buffer reader.
 *
 *
 */

