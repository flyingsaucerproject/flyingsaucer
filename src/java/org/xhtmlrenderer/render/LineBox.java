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
package org.xhtmlrenderer.render;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.content.TextContent;
import org.xhtmlrenderer.layout.inline.WhitespaceStripper;


/**
 * Description of the Class
 *
 * @author empty
 */
public class LineBox extends Box {

    public LineBox() {
    }

    //public List inlines = new ArrayList();

    /**
     * Description of the Field
     */
    public int lineheight;// relative to Xx,y

    /**
     * Description of the Field
     */
    public int baseline;// relative to Xx,y

    /**
     * Adds a feature to the Child attribute of the Box object
     *
     * @param child The feature to be added to the Child attribute
     */
    public void addInlineChild(Context c, InlineBox child) {
        if (child == null) throw new NullPointerException("trying to add null child");
        if (getChildCount() == 0 && child.content instanceof TextContent) {//first box on line
            if (child.getSubstring().startsWith(WhitespaceStripper.SPACE)) {
                String whitespace = c.getCurrentStyle().getStringProperty(CSSName.WHITE_SPACE);
                if (whitespace.equals("normal") ||
                        whitespace.equals("nowrap") ||
                        whitespace.equals("pre-line"))
                    child.setSubstring(child.start_index + 1, child.end_index);
            }
            if (child.getSubstring().equals("")) return;
        }
        child.setParent(this);
        addChild(child);
        if (child.isChildrenExceedBounds()) {
            setChildrenExceedBounds(true);
        }
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {

        return "Line: (" + x + "," + y + ")Xx(" + width + "," + height + ")" + "  baseline = " + baseline;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2004/12/15 00:53:40  tobega
 * Started playing a bit with inline box, provoked a few nasties, probably created some, seems to work now
 *
 * Revision 1.4  2004/12/12 03:33:01  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.3  2004/12/10 06:51:05  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.2  2004/10/23 13:50:27  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

