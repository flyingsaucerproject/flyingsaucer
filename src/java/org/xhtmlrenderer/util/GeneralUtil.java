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
        ClassLoader loader = new Object().getClass().getClassLoader();
        if ( loader == null ) {
            readStream = ClassLoader.getSystemResourceAsStream( resource );
        } else {
            readStream = loader.getResourceAsStream( resource );
        }
        return readStream;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2004/10/13 23:00:32  pdoubleya
 * Added to CVS.
 *
 */

