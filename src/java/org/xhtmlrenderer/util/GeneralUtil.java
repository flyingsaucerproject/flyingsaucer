/*
 * {{{ header & license
 * GeneralUtil.java
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
package org.xhtmlrenderer.util;

import java.io.*;
import java.net.*;
import org.xhtmlrenderer.*;


/**
 * Description of the Class
 *
 * @author   Patrick Wright
 */
public class GeneralUtil {
    /**
     * Description of the Method
     *
     * @param resource  PARAM
     * @return          Returns
     */
    public static InputStream openStreamFromClasspath( String resource ) {
        InputStream readStream = null;
        try {
            ClassLoader loader = new DefaultCSSMarker().getClass().getClassLoader();
            if ( loader == null ) {
                readStream = ClassLoader.getSystemResourceAsStream( resource );
            } else {
                readStream = loader.getResourceAsStream( resource );
            }
            if ( readStream == null ) {
                URL stream = resource.getClass().getResource( resource );
                readStream = stream.openStream();
            }
        } catch ( Exception ex ) {
            XRLog.exception("Could not open stream from CLASSPATH: " + resource, ex );
        }
        return readStream;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/10/18 23:43:02  joshy
 * final updates today
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.2  2004/10/18 17:10:13  pdoubleya
 * Added additional condition, and error check.
 *
 * Revision 1.1  2004/10/13 23:00:32  pdoubleya
 * Added to CVS.
 *
 */

