/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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

import org.xhtmlrenderer.layout.Context;

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Description of the Class
 *
 * @author empty
 */
public class ImageUtil {

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param src PARAM
     * @return Returns
     *         //* @throws MalformedURLException Throws
     */
    public static Image loadImage(Context c, String src)
            /*throws MalformedURLException*/ {

        Image img = null;

        //XRLog.load("Getting image for src = " + src);

        //TODO: make sure that the src uri is already resolved before calling this method!
        //HACK:meanwhile...
        if (!src.startsWith("http") && !src.startsWith("file")) {

            URL base = c.getRenderingContext().getBaseURL();

            if (base != null) {
                try {
                    src = new URL(base, src).toString();
                } catch (MalformedURLException e) {
                    XRLog.exception("Bad image url", e);
                }
            } else {
                XRLog.init("No base URL set!");
            }
        }

        img = c.getCtx().getUac().getImage(src);

        if (img != null && img.getWidth(null) == -1) {

            return null;
        }

        return img;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.11  2005/02/02 12:14:27  pdoubleya
 * .
 *
 * Revision 1.10  2005/01/29 20:21:08  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.9  2005/01/10 23:24:47  tobega
 * Created image cache
 *
 * Revision 1.8  2005/01/08 22:05:16  tobega
 * Now saves form components.
 *
 * Revision 1.7  2005/01/08 15:56:56  tobega
 * Further work on extensibility interfaces. Documented it - see website.
 *
 * Revision 1.6  2004/12/29 10:39:37  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.5  2004/12/12 03:33:04  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.4  2004/11/12 02:50:59  joshy
 * finished moving base url
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 14:06:57  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

