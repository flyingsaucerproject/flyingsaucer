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
import org.xhtmlrenderer.render.Renderer;


/**
 * Description of the Interface
 *
 * @author empty
 */
public interface Layout {

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content PARAM
     * @return Returns
     */
    public Box layout(Context c, Content content);

    /**
     * @deprecated find another way, box type maybe?
     */
    public Renderer getRenderer();
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.11  2004/12/29 10:39:33  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.10  2004/12/12 03:32:58  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.9  2004/12/09 00:11:51  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.8  2004/11/12 00:20:29  tobega
 * Set up the HoverListener to work properly. Colors are changing!
 *
 * Revision 1.7  2004/11/09 15:53:49  joshy
 * initial support for hover (currently disabled)
 * moved justification code into it's own class in a new subpackage for inline
 * layout (because it's so blooming complicated)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/10/28 02:13:41  joshy
 * finished moving the painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/10/27 13:17:01  joshy
 * beginning to split out rendering code
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

