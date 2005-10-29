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
package org.xhtmlrenderer.render;

import org.xhtmlrenderer.layout.Boxing;
import org.xhtmlrenderer.util.XRLog;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.LineMetrics;
import java.util.LinkedList;
import java.util.logging.Level;


/**
 * Description of the Class
 *
 * @author empty
 */
public class LineBox extends Box implements Renderable {

    /**
     * Description of the Field
     */
    public int ascent;
    /**
     * Description of the Field
     */
    public int descent;

    public LineMetrics blockLineMetrics;
    public boolean textAligned = false;
    public int renderIndex;

    /**
     * Constructor for the LineBox object
     */
    public LineBox() {
    }

    /**
     * Adds a feature to the InlineChild attribute of the LineBox object
     *
     * @param ib The feature to be added to the InlineChild attribute
     */
    public void addInlineChild(InlineBox ib) {
        if (ib == null) {
            throw new NullPointerException("trying to add null child");
        }
        if (getChildCount() == 0 && ib instanceof InlineTextBox) {//first box on line
            InlineTextBox child = (InlineTextBox) ib;
            if (child.getSubstring().equals("")) {
                child.contentWidth = 0;
                child.height = 0;
            }
        }
        addChild(ib);
        Boxing.checkExceeds(ib);
        propagateChildProperties(ib);
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {

        return "Line: (" + x + "," + y + ")x(" + getWidth() + "," + height + ")" + "  baseline = " + getBaseline();
    }

    /**
     * Gets the baseline attribute of the LineBox object
     *
     * @return The baseline value
     */
    public int getBaseline() {
        int leading = height - ascent - descent;
        if (leading < 0) {
            XRLog.layout(Level.SEVERE, "negative leading in line box");
        }
        return ascent + leading / 2;
    }

    public void moveToNextPage(PageContext c, Rectangle bounds) {
        if (getParent().getChildCount() == 1 && getParent().getChild(0) == this) {
            getParent().moveToNextPage(c, true);
        } else {
            double delta = getDistanceFromPageBreak(c, false);

            y += delta;
            bounds.height += delta;
        }
    }

    public int getIndex() {
        return renderIndex;
    }

    public void render(RenderingContext c, Graphics2D g2) {
        //HACK:
        g2.translate(absX - x, absY - y);
        InlineRendering.paintLine(c, this, new LinkedList());
        g2.translate(x - absX, y - absY);
    }

    public double getAbsTop() {
        return absY;
    }

    public double getAbsBottom() {
        return absY + height;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.23  2005/10/29 22:31:02  tobega
 * House-cleaning
 *
 * Revision 1.22  2005/10/27 00:09:04  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.21  2005/10/16 23:57:17  tobega
 * Starting experiment with flat representation of render tree
 *
 * Revision 1.20  2005/10/12 21:17:14  tobega
 * patch from Peter Brant
 *
 * Revision 1.19  2005/10/08 17:40:21  tobega
 * Patch from Peter Brant
 *
 * Revision 1.18  2005/10/06 03:20:23  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.17  2005/08/06 22:12:24  tobega
 * Fixed issue 110
 *
 * Revision 1.16  2005/07/14 22:25:17  joshy
 * major updates to float code. should fix *most* issues.
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2005/05/09 23:47:15  tobega
 * Cleaned up some getting of LineMetrics and optimized InlineRendering
 *
 * Revision 1.14  2005/05/08 13:02:41  tobega
 * Fixed a bug whereby styles could get lost for inline elements, notably if root element was inline. Did a few other things which probably has no importance at this moment, e.g. refactored out some unused stuff.
 *
 * Revision 1.13  2005/01/29 20:21:04  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.12  2005/01/24 22:46:42  pdoubleya
 * Added support for ident-checks using IdentValue instead of string comparisons.
 *
 * Revision 1.11  2005/01/10 01:58:37  tobega
 * Simplified (and hopefully improved) handling of vertical-align. Added support for line-height. As always, provoked a few bugs in the process.
 *
 * Revision 1.10  2005/01/09 13:32:35  tobega
 * Caching image components. Also fixed two bugs that were introduced fixing the last one. Code still too brittle...
 *
 * Revision 1.9  2005/01/09 00:29:28  tobega
 * Removed XPath usages from core classes. Also happened to find and fix a layout-bug that I introduced a while ago.
 *
 * Revision 1.8  2005/01/06 09:49:38  tobega
 * More cleanup, aiming to remove Content reference in box
 *
 * Revision 1.7  2005/01/05 01:10:16  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.6  2004/12/29 10:39:35  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
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

