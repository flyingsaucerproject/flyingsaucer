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
package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.Box;


/**
 * Description of the Class
 *
 * @author empty
 */
public class NullLayout extends DefaultLayout {

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content PARAM
     * @return Returns
     */
    public Box layout(SharedContext c, Content content) {

        return new Box(0, 0, 0, 0);
    }

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paint(Context c, Box box) {
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.6  2004/12/29 10:39:33  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.5  2004/12/09 00:11:52  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.4  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

