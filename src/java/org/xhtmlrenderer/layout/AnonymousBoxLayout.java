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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.layout.content.BlockContent;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.AnonymousBlockBox;
import org.xhtmlrenderer.render.Box;


/**
 * Description of the Class
 *
 * @author empty
 */
public class AnonymousBoxLayout extends InlineLayout {

    /**
     * Description of the Field
     */
    private Element parent;

    /**
     * Description of the Field
     */
    private Node text;

    /**
     * Constructor for the AnonymousBoxLayout object
     */
    public AnonymousBoxLayout() {
    }


    // use the passed in 'text'  since that's what we are

    // really laying out instead of the 'node', which is really the

    // parent element.

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content
     * @return Returns
     */
    public Box createBox(Context c, Content content) {

        AnonymousBlockBox block = new AnonymousBlockBox(text, c);

        return block;
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     * @param c    PARAM
     */
    /*
   public void prepareBox( Box box, Context c ) {

       box.border = new Border();

       box.padding = new Border();

       box.margin = new Border();

   }
   */


    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param parent PARAM
     * @param text   PARAM
     * @return Returns
     */
    public Box layout(Context c, Element parent, Node text) {//called by BoxLayout.layoutChildren
        this.parent = parent;
        this.text = text;
        //Box box = new AnonymousBlockBox(text);
        //TODO: temporary hack
        Box box = super.layout(c, new BlockContent(parent, c.css.getStyle(parent)));//BoxLayout
        //u.p("AnonymousBoxLayout.layout: returning: " + box);
        return box;
    }

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     Returns
     */
    /*
   public Box layoutChildren( Context c, Box box ) {
       //u.p("AnonymousBoxLayout.layoutChildren() noop" + box);
       return super.layoutChildren( c, box );
       //return box;
   }*/

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.7  2004/12/09 21:18:52  tobega
 * precaution: code still works
 *
 * Revision 1.6  2004/12/09 00:11:51  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.5  2004/12/08 00:42:34  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation. Also fixed 2 irritating bugs!
 *
 * Revision 1.4  2004/11/18 18:49:48  joshy
 * fixed the float issue.
 * commented out more dead code
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 13:46:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

