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
package org.xhtmlrenderer.forms;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;

import javax.swing.*;
import java.awt.*;


/**
 * Description of the Class
 *
 * @author empty
 */
public abstract class FormItemLayout/* extends CustomBlockLayout*/ {

    /**
     * Description of the Field
     */
    private JComponent comp;

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param elem PARAM
     * @return Returns
     */
    public abstract JComponent createComponent(Context c, Element elem);

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content
     * @return Returns
     */
    public Box createBox(Context c, Content content) {
        Element elem = content.getElement();
        comp = createComponent(c, elem);
        c.getCanvas().add(comp);
        comp.setLocation(100, 100);
        //Uu.p("added a component to the viewport: " + comp);
        //Uu.p("pref size = " + comp.getPreferredSize());
        InputBox box = new InputBox();
        box.content = content;
        box.component = comp;

        // this is so the context has a reference to all forms, fields,
        // and components of those fields
        if (elem.hasAttribute("name")) {
            c.addInputField(elem.getAttribute("name"), elem, comp);
        }
        return box;
    }


    /**
     * Description of the Method
     *
     * @param coords PARAM
     * @param box    PARAM
     */
    public static void adjustVerticalAlign(Context c, Point coords, Box box) {
        if (box.getParent() instanceof InlineBox) {
            InlineBox ib = (InlineBox) box.getParent();
            LineBox lb = (LineBox) ib.getParent();
            //Uu.p("box = " + box);
            //Uu.p("margin = " + box.margin);
            //Uu.p("border = " + box.border);
            //Uu.p("padding = " + box.padding);
            //Uu.p("ib = " + ib);
            //Uu.p("margin = " + ib.margin);
            //Uu.p("border = " + ib.border);
            //Uu.p("padding = " + ib.padding);
            //Uu.p("lb = " + lb);
            int off = lb.baseline - (ib.height) + 5;
            coords.x += 5;
            coords.y += off;

            Border border = LayoutUtil.getBorder(box, c.getCurrentStyle());
            Border margin = c.getCurrentStyle().getMarginWidth();
            Border padding = c.getCurrentStyle().getPaddingWidth();
            coords.x -= margin.left;
            coords.x -= border.left;
            coords.x -= padding.left;
            coords.y -= margin.top;
            coords.y -= border.top;
            coords.y -= padding.top;
        }
    }


    /**
     * Description of the Method
     *
     * @param box PARAM
     * @return Returns
     */
    public static Point absCoords(Box box) {
        //Uu.p("box = " + box);
        //Uu.p("Xx = " + box.Xx + " y = " + box.y);
        //Uu.p("Parent = " + box.getParent());
        Point pt = new Point(0, 0);
        pt.x += box.x;
        pt.y += box.y;

        if (box.getParent() != null) {
            Point pt_parent = absCoords(box.getParent());
            pt.x += pt_parent.x;
            pt.y += pt_parent.y;
            //return box.Xx + absX(box.getParent());
        }
        return pt;
    }

    /**
     * Gets the intrinsicDimensions attribute of the FormItemLayout object
     *
     * @param c    PARAM
     * @param elem PARAM
     * @return The intrinsicDimensions value
     */
    public Dimension getIntrinsicDimensions(Context c, Element elem) {
        //comp.setLocation(50,50);
        Dimension dim = comp.getPreferredSize();
        //return new Dimension(10,10);
        //Uu.p("get intrinsic = " + dim);
        return dim;
    }

    /**
     * Description of the Method
     *
     * @param comp PARAM
     * @param elem PARAM
     */
    protected void commonPrep(JComponent comp, Element elem) {
        if (elem.hasAttribute("disabled") &&
                elem.getAttribute("disabled").equals("disabled")) {
            comp.setEnabled(false);
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.17  2005/01/02 12:22:14  tobega
 * Cleaned out old layout code
 *
 * Revision 1.16  2005/01/01 23:38:37  tobega
 * Cleaned out old rendering code
 *
 * Revision 1.15  2004/12/29 15:06:40  tobega
 * Referencing Context instead of SharedContext where it was wrongly set before.
 *
 * Revision 1.14  2004/12/29 10:39:28  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.13  2004/12/29 07:35:37  tobega
 * Prepared for cloned Context instances by encapsulating fields
 *
 * Revision 1.12  2004/12/28 01:48:22  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
 * Revision 1.11  2004/12/27 09:40:46  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.10  2004/12/27 07:43:29  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.9  2004/12/12 03:32:55  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.8  2004/12/12 02:57:51  tobega
 * Making progress
 *
 * Revision 1.7  2004/12/09 21:18:51  tobega
 * precaution: code still works
 *
 * Revision 1.6  2004/12/05 00:48:55  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.5  2004/10/28 13:46:31  joshy
 * removed dead code
 * moved code about specific elements to the layout factory (link and br)
 * fixed form rendering bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/28 02:13:38  joshy
 * finished moving the painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 13:40:27  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

