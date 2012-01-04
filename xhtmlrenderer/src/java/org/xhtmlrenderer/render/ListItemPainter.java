/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2005 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import java.awt.RenderingHints;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.FSImage;

/**
 * A utility class to paint list markers (all types).
 * @see MarkerData 
 */
public class ListItemPainter {
    public static void paint(RenderingContext c, BlockBox box) {
        if (box.getMarkerData() == null) {
            return;
        }
        
        MarkerData markerData = box.getMarkerData();
        
        if (markerData.getImageMarker() != null) {
            drawImage(c, box, markerData);
        } else {
            CalculatedStyle style = box.getStyle();
            IdentValue listStyle = style.getIdent(CSSName.LIST_STYLE_TYPE);
            
            c.getOutputDevice().setColor(style.getColor());
    
            if (markerData.getGlyphMarker() != null) {
                drawGlyph(c, box, style, listStyle);
            } else if (markerData.getTextMarker() != null){
                drawText(c, box, listStyle);
            }
        }
    }

    private static void drawImage(RenderingContext c, BlockBox box, MarkerData markerData) {
        FSImage img = null;
        MarkerData.ImageMarker marker = markerData.getImageMarker();
        img = marker.getImage();
        if (img != null) {
            StrutMetrics strutMetrics = box.getMarkerData().getStructMetrics();
            int x = getReferenceX(c, box);
            // FIXME: findbugs possible loss of precision, cf. int / (float)2
            x += -marker.getLayoutWidth() +
                    (marker.getLayoutWidth() / 2 - img.getWidth() / 2);
            c.getOutputDevice().drawImage(img, 
                    x,
                    (int)(getReferenceBaseline(c, box)
                        - strutMetrics.getAscent() / 2 - img.getHeight() / 2));
        }
    }
    
    private static int getReferenceX(RenderingContext c, BlockBox box) {
        MarkerData markerData = box.getMarkerData();
        
        if (markerData.getReferenceLine() != null) {
            return markerData.getReferenceLine().getAbsX();
        } else {
            return box.getAbsX() + (int)box.getMargin(c).left();
        }
    }
    
    private static int getReferenceBaseline(RenderingContext c, BlockBox box) {
        MarkerData markerData = box.getMarkerData();
        StrutMetrics strutMetrics = box.getMarkerData().getStructMetrics();
        
        if (markerData.getReferenceLine() != null) {
            return markerData.getReferenceLine().getAbsY() + strutMetrics.getBaseline();
        } else {
            return box.getAbsY() + box.getTy() + strutMetrics.getBaseline();
        }
    }

    private static void drawGlyph(RenderingContext c, BlockBox box, 
            CalculatedStyle style, IdentValue listStyle) {
        // save the old AntiAliasing setting, then force it on
        Object aa_key = c.getOutputDevice().getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        c.getOutputDevice().setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // calculations for bullets
        StrutMetrics strutMetrics = box.getMarkerData().getStructMetrics();
        MarkerData.GlyphMarker marker = box.getMarkerData().getGlyphMarker();
        int x = getReferenceX(c, box);
        x += -marker.getLayoutWidth();
        int y = getReferenceBaseline(c, box) 
            - (int)strutMetrics.getAscent() / 2 - marker.getDiameter() / 2;
        if (listStyle == IdentValue.DISC) {
            c.getOutputDevice().fillOval(x, y, marker.getDiameter(), marker.getDiameter());
        } else if (listStyle == IdentValue.SQUARE) {
            c.getOutputDevice().fillRect(x, y, marker.getDiameter(), marker.getDiameter());
        } else if (listStyle == IdentValue.CIRCLE) {
            c.getOutputDevice().drawOval(x, y, marker.getDiameter(), marker.getDiameter());
        }

        // restore the old AntiAliasing setting
        c.getOutputDevice().setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                aa_key == null ? RenderingHints.VALUE_ANTIALIAS_DEFAULT : aa_key);
    }

    private static void drawText(RenderingContext c, BlockBox box, IdentValue listStyle) {
        MarkerData.TextMarker text = box.getMarkerData().getTextMarker();
        
        int x = getReferenceX(c, box);
        x += -text.getLayoutWidth();
        int y = getReferenceBaseline(c, box);
        
        c.getOutputDevice().setColor(box.getStyle().getColor());
        c.getOutputDevice().setFont(box.getStyle().getFSFont(c));
        c.getTextRenderer().drawString(
                c.getOutputDevice(), text.getText(), x, y);
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.39  2009/05/09 15:13:11  pdoubleya
 * FindBugs: FIXME for possible loss of precision in float, int arithmetic
 *
 * Revision 1.38  2008/09/06 18:33:53  peterbrant
 * Make list marker display more like Safari and FF (patch from Mykola Gurov)
 *
 * Revision 1.37  2007/03/12 21:11:19  peterbrant
 * Documentation update
 *
 * Revision 1.36  2007/02/07 16:33:22  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.35  2006/08/30 18:25:41  peterbrant
 * Further refactoring / Bug fix for problem reported by Mike Curtis
 *
 * Revision 1.34  2006/08/29 17:29:12  peterbrant
 * Make Style object a thing of the past
 *
 * Revision 1.33  2006/02/02 02:47:34  peterbrant
 * Support non-AWT images
 *
 * Revision 1.32  2006/02/01 01:30:13  peterbrant
 * Initial commit of PDF work
 *
 * Revision 1.31  2006/01/27 01:15:33  peterbrant
 * Start on better support for different output devices
 *
 * Revision 1.30  2006/01/11 22:16:28  peterbrant
 * Fix NPE with PdfGraphics2D
 *
 * Revision 1.29  2005/12/13 20:46:06  peterbrant
 * Improve list support (implement list-style-position: inside, marker "sticks" to first line box even if there are other block boxes in between, plus other minor fixes) / Experimental support for optionally extending text decorations to box edge vs line edge
 *
 * Revision 1.28  2005/12/13 02:41:33  peterbrant
 * Initial implementation of vertical-align: top/bottom (not done yet) / Minor cleanup and optimization
 *
 * Revision 1.27  2005/12/10 00:59:38  peterbrant
 * Fix compile error
 *
 * Revision 1.26  2005/12/10 00:58:48  peterbrant
 * Marker position should take left margin into account
 *
 * Revision 1.25  2005/12/09 17:59:29  peterbrant
 * Make text and glyph positioning closer to Firefox
 *
 * Revision 1.24  2005/12/05 00:13:53  peterbrant
 * Improve list-item support (marker positioning is now correct) / Start support for relative inline layers
 *
 * Revision 1.23  2005/11/08 22:53:46  tobega
 * added getLineHeight method to CalculatedStyle and hacked in some list-item support
 *
 * Revision 1.22  2005/10/27 00:09:04  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.21  2005/10/18 20:57:07  tobega
 * Patch from Peter Brant
 *
 * Revision 1.20  2005/06/25 17:23:34  tobega
 * first refactoring of UAC: ImageResource
 *
 * Revision 1.19  2005/06/22 23:48:46  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.18  2005/06/01 21:36:40  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.17  2005/05/30 01:04:00  tobega
 * Fixed a regression on list-item numbering (basically getting rid of unnecessary style-pushes and style-pops)
 *
 * Revision 1.16  2005/01/29 20:21:04  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.15  2005/01/24 22:46:42  pdoubleya
 * Added support for ident-checks using IdentValue instead of string comparisons.
 *
 * Revision 1.14  2005/01/24 19:01:03  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.13  2005/01/10 01:58:37  tobega
 * Simplified (and hopefully improved) handling of vertical-align. Added support for line-height. As always, provoked a few bugs in the process.
 *
 * Revision 1.12  2004/12/29 10:39:35  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.11  2004/12/28 02:15:19  tobega
 * More cleaning.
 *
 * Revision 1.10  2004/12/12 04:18:58  tobega
 * Now the core compiles at least. Now we must make it work right. Table layout is one point that really needs to be looked over
 *
 * Revision 1.9  2004/12/12 03:33:01  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.8  2004/12/05 14:35:40  tobega
 * Cleaned up some usages of Node (and removed unused stuff) in layout code. The goal is to pass "better" objects than Node wherever possible in an attempt to shake out the bugs in tree-traversal (probably often unnecessary tree-traversal)
 *
 * Revision 1.7  2004/12/05 05:18:02  joshy
 * made bullets be anti-aliased
 * fixed bug in link listener that caused NPEs
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/12/05 00:48:59  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.5  2004/11/27 15:46:40  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/11/10 16:28:53  joshy
 * fixes for list items
 * bullets and numbers are sized correctly now
 * numbers have periods after them
 * adjusted ul/ol margins to use ems
 * positioned bullets and numbers on the top line
 * adjusted splash page
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 13:50:27  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

