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
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.render.AnonymousBlockBox;
import org.xhtmlrenderer.render.Box;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class AnonymousBoxLayout extends InlineLayout {

    /** Description of the Field */
    private Element parent;

    /** Description of the Field */
    private Node text;

    /** Constructor for the AnonymousBoxLayout object */
    public AnonymousBoxLayout() { }


    // use the passed in 'text'  since that's what we are

    // really laying out instead of the 'node', which is really the

    // parent element.

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param node  PARAM
     * @return      Returns
     */
    public Box createBox( Context c, Node node ) {

        AnonymousBlockBox block = new AnonymousBlockBox( text, c );

        return block;
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     * @param c    PARAM
     */
    public void prepareBox( Box box, Context c ) {

        box.border = new Border();

        box.padding = new Border();

        box.margin = new Border();

    }


    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param parent  PARAM
     * @param text    PARAM
     * @return        Returns
     */
    public Box layout( Context c, Element parent, Node text ) {

        this.parent = parent;

        this.text = text;

        //Box box = new AnonymousBlockBox(text);

        Box box = super.layout( c, parent );

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
    public Box layoutChildren( Context c, Box box ) {

        //u.p("AnonymousBoxLayout.layoutChildren() noop" + box);

        return super.layoutChildren( c, box );
        //return box;

    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/10/23 13:46:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

