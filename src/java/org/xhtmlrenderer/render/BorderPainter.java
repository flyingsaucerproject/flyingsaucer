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

import java.awt.BasicStroke;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;

import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.extend.OutputDevice;

public class BorderPainter {
    public static final int TOP = 1;
    public static final int LEFT = 2;
    public static final int BOTTOM = 4;
    public static final int RIGHT = 8;
    public static final int ALL = TOP + LEFT + BOTTOM + RIGHT;
    
    /**
     * @param xOffset for determining starting point for patterns
     */
    public static void paint(Rectangle bounds, int sides, BorderPropertySet border, RenderingContext ctx, int xOffset) {
        if ((sides & BorderPainter.TOP) == BorderPainter.TOP && border.noTop()) {
            sides -= BorderPainter.TOP;
        }
        if ((sides & BorderPainter.LEFT) == BorderPainter.LEFT && border.noLeft()) {
            sides -= BorderPainter.LEFT;
        }
        if ((sides & BorderPainter.BOTTOM) == BorderPainter.BOTTOM && border.noBottom()) {
            sides -= BorderPainter.BOTTOM;
        }
        if ((sides & BorderPainter.RIGHT) == BorderPainter.RIGHT && border.noRight()) {
            sides -= BorderPainter.RIGHT;
        }

        //Now paint!
        if ((sides & BorderPainter.TOP) == BorderPainter.TOP && ! border.topColor().equals(OutputDevice.TRANSPARENT)) {
            paintBorderSide(ctx.getOutputDevice(), 
                    border, bounds, sides, BorderPainter.TOP, border.topStyle(), xOffset);
        }
        if ((sides & BorderPainter.LEFT) == BorderPainter.LEFT && ! border.leftColor().equals(OutputDevice.TRANSPARENT)) {
            paintBorderSide(ctx.getOutputDevice(), 
                    border, bounds, sides, BorderPainter.LEFT, border.leftStyle(), xOffset);
        }
        if ((sides & BorderPainter.BOTTOM) == BorderPainter.BOTTOM && ! border.bottomColor().equals(OutputDevice.TRANSPARENT)) {
            paintBorderSide(ctx.getOutputDevice(), 
                    border, bounds, sides, BorderPainter.BOTTOM, border.bottomStyle(), xOffset);
        }
        if ((sides & BorderPainter.RIGHT) == BorderPainter.RIGHT && ! border.rightColor().equals(OutputDevice.TRANSPARENT)) {
            paintBorderSide(ctx.getOutputDevice(), 
                    border, bounds, sides, BorderPainter.RIGHT, border.rightStyle(), xOffset);
        }
    }

    private static Rectangle shrinkRect(final Rectangle rect, final BorderPropertySet border, int sides) {
        Rectangle r2 = new Rectangle();
        r2.x = rect.x + ((sides & BorderPainter.LEFT) == 0 ? 0 : (int) border.left());
        r2.width = rect.width - ((sides & BorderPainter.LEFT) == 0 ? 0 : (int) border.left()) - ((sides & BorderPainter.RIGHT) == 0 ? 0 : (int) border.right());
        r2.y = rect.y + ((sides & BorderPainter.TOP) == 0 ? 0 : (int) border.top());
        r2.height = rect.height - ((sides & BorderPainter.TOP) == 0 ? 0 : (int) border.top()) - ((sides & BorderPainter.BOTTOM) == 0 ? 0 : (int) border.bottom());
        return r2;
    }

    private static void paintBorderSide(OutputDevice outputDevice, 
            final BorderPropertySet border, final Rectangle bounds, final int sides, 
            int currentSide, final IdentValue borderSideStyle, int xOffset) {
        if (borderSideStyle == IdentValue.RIDGE || borderSideStyle == IdentValue.GROOVE) {
            BorderPropertySet bd2 = new BorderPropertySet((int) (border.top() / 2),
                    (int) (border.right() / 2),
                    (int) (border.bottom() / 2),
                    (int) (border.left() / 2));
            if (borderSideStyle == IdentValue.RIDGE) {
                paintGoodBevel(outputDevice, bounds, border, border.darker(borderSideStyle), border.brighter(borderSideStyle), sides, currentSide);
                paintGoodBevel(outputDevice, bounds, bd2, border.brighter(borderSideStyle), border.darker(borderSideStyle), sides, currentSide);
            } else {
                paintGoodBevel(outputDevice, bounds, border, border.brighter(borderSideStyle), border.darker(borderSideStyle), sides, currentSide);
                paintGoodBevel(outputDevice, bounds, bd2, border.darker(borderSideStyle), border.brighter(borderSideStyle), sides, currentSide);
            }
            return;
        }

        if (borderSideStyle == IdentValue.OUTSET) {
            paintGoodBevel(outputDevice, bounds, border,
                    border.brighter(borderSideStyle),
                    border.darker(borderSideStyle), sides, currentSide);
            return;
        }

        if (borderSideStyle == IdentValue.INSET) {
            paintGoodBevel(outputDevice, bounds, border,
                    border.darker(borderSideStyle),
                    border.brighter(borderSideStyle), sides, currentSide);
            return;
        }

        if (borderSideStyle == IdentValue.SOLID) {
            paintSolid(outputDevice, bounds, border, border, sides, currentSide);
            return;
        }

        if (borderSideStyle == IdentValue.DOUBLE) {
            // this may need to be modified to account for rounding errors
            // create a new border only 1/3 the thickness
            BorderPropertySet outer = new BorderPropertySet((int) (border.top() / 3),
                    (int) (border.right() / 3),
                    (int) (border.bottom() / 3),
                    (int) (border.left() / 3));
            BorderPropertySet center = new BorderPropertySet(outer);

            BorderPropertySet inner = new BorderPropertySet(outer);
            if ((int) border.top() == 1) {
                outer.setTop(1f);
                center.setTop(0f);
            }
            if ((int) border.bottom() == 1) {
                outer.setBottom(1f);
                center.setBottom(0f);
            }
            if ((int) border.left() == 1) {
                outer.setLeft(1f);
                center.setLeft(0f);
            }
            if ((int) border.right() == 1) {
                outer.setRight(1f);
                center.setRight(0f);
            }

            if ((int) border.top() == 2) {
                outer.setTop(1f);
                center.setTop(0f);
                inner.setTop(1f);
            }
            if ((int) border.bottom() == 2) {
                outer.setBottom(1f);
                center.setBottom(0f);
                inner.setBottom(1f);
            }
            if ((int) border.left() == 2) {
                outer.setLeft(1f);
                center.setLeft(0f);
                inner.setLeft(1f);
            }
            if ((int) border.right() == 2) {
                outer.setRight(1f);
                center.setRight(0f);
                inner.setRight(1f);
            }

            Rectangle b2 = shrinkRect(bounds, outer, sides);
            b2 = shrinkRect(b2, center, sides);
            // draw outer border
            paintSolid(outputDevice, bounds, outer, border, sides, currentSide);
            // draw inner border
            paintSolid(outputDevice, b2, inner, border, sides, currentSide);
            return;
        }

        int thickness = 0;
        if (currentSide == BorderPainter.TOP) thickness = (int) border.top();
        if (currentSide == BorderPainter.BOTTOM) thickness = (int) border.bottom();
        if (currentSide == BorderPainter.RIGHT) thickness = (int) border.right();
        if (currentSide == BorderPainter.LEFT) thickness = (int) border.left();
        if (borderSideStyle == IdentValue.DASHED) {
            outputDevice.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            paintPatternedRect(outputDevice, bounds, border, border, new float[]{8.0f + thickness * 2, 4.0f + thickness}, sides, currentSide, xOffset);
            outputDevice.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        if (borderSideStyle == IdentValue.DOTTED) {
            // turn off anti-aliasing or the dots will be all blurry
            outputDevice.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            paintPatternedRect(outputDevice, bounds, border, border, new float[]{thickness, thickness}, sides, currentSide, xOffset);
            outputDevice.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    /**
     * Gets the polygon to be filled for the border
     */
    private static Polygon getBevelledPolygon(final Rectangle bounds, 
            final BorderPropertySet border, final int sides, 
            int currentSide, boolean isClipRegion) {
        int rightCorner = (((sides & BorderPainter.RIGHT) == BorderPainter.RIGHT) ? (int) border.right() : 0);
        int leftCorner = (((sides & BorderPainter.LEFT) == BorderPainter.LEFT) ? (int) border.left() : 0);
        int topCorner = (((sides & BorderPainter.TOP) == BorderPainter.TOP) ? (int) border.top() : 0);
        int bottomCorner = (((sides & BorderPainter.BOTTOM) == BorderPainter.BOTTOM) ? (int) border.bottom() : 0);
        Polygon poly = null;
        if (currentSide == BorderPainter.TOP) {
            if ((int) border.top() != 1) {
                // use polygons for borders over 1px wide
                poly = new Polygon();
                poly.addPoint(bounds.x, bounds.y);
                poly.addPoint(bounds.x + bounds.width, bounds.y);
                poly.addPoint(bounds.x + bounds.width - rightCorner, bounds.y + (int) border.top() - 0);
                poly.addPoint(bounds.x + leftCorner, bounds.y + (int) border.top() - 0);
            }
        } else if (currentSide == BorderPainter.BOTTOM) {
            if ((int) border.bottom() != 1) {
                poly = new Polygon();
                // upper right
                poly.addPoint(bounds.x + bounds.width - rightCorner, bounds.y + bounds.height - (int) border.bottom());
                // upper left
                poly.addPoint(bounds.x + leftCorner, bounds.y + bounds.height - (int) border.bottom());
                // lower left
                poly.addPoint(bounds.x, bounds.y + bounds.height);
                // lower right
                poly.addPoint(bounds.x + bounds.width, bounds.y + bounds.height - 0);
            }
        } else if (currentSide == BorderPainter.RIGHT) {
            if ((int) border.right() != 1) {
                poly = new Polygon();
                poly.addPoint(bounds.x + bounds.width, bounds.y);
                poly.addPoint(bounds.x + bounds.width - (int) border.right(), bounds.y + topCorner);
                poly.addPoint(bounds.x + bounds.width - (int) border.right(), bounds.y + bounds.height - bottomCorner);
                poly.addPoint(bounds.x + bounds.width, bounds.y + bounds.height);
            }
        } else if (currentSide == BorderPainter.LEFT) {
            if ((int) border.left() != 1) {
                poly = new Polygon();
                poly.addPoint(bounds.x, bounds.y);
                poly.addPoint(bounds.x + (int) border.left(), bounds.y + topCorner);
                poly.addPoint(bounds.x + (int) border.left(), bounds.y + bounds.height - bottomCorner);
                poly.addPoint(bounds.x, bounds.y + bounds.height);
            }
        }
        return poly;
    }

    /**
     * @param xOffset     for inline borders, to determine dash_phase of top and bottom
     */
    private static void paintPatternedRect(OutputDevice outputDevice, 
            final Rectangle bounds, final BorderPropertySet border, 
            final BorderPropertySet color, final float[] pattern, 
            final int sides, final int currentSide, int xOffset) {
        Stroke old_stroke = outputDevice.getStroke();

        if (currentSide == BorderPainter.TOP) {
            outputDevice.setColor(color.topColor());
            outputDevice.setStroke(new BasicStroke((int) border.top(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, xOffset));
            outputDevice.drawBorderLine(
                    bounds, BorderPainter.TOP, (int)border.top(), false);
        } else if (currentSide == BorderPainter.LEFT) {
            outputDevice.setColor(color.leftColor());
            outputDevice.setStroke(new BasicStroke((int) border.left(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
            outputDevice.drawBorderLine(
                    bounds, BorderPainter.LEFT, (int)border.left(), false);
        } else if (currentSide == BorderPainter.RIGHT) {
            outputDevice.setColor(color.rightColor());
            outputDevice.setStroke(new BasicStroke((int) border.right(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
            outputDevice.drawBorderLine(
                    bounds, BorderPainter.RIGHT, (int)border.right(), false);
        } else if (currentSide == BorderPainter.BOTTOM) {
            outputDevice.setColor(color.bottomColor());
            outputDevice.setStroke(new BasicStroke((int) border.bottom(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, xOffset));
            outputDevice.drawBorderLine(
                    bounds, BorderPainter.BOTTOM, (int)border.bottom(), false);
        }

        outputDevice.setStroke(old_stroke);
    }

    private static void paintGoodBevel(OutputDevice outputDevice, 
            final Rectangle bounds, final BorderPropertySet border, 
            final BorderPropertySet high, final BorderPropertySet low, 
            final int sides, int currentSide) {
        if (currentSide == BorderPainter.TOP) {
            paintSolid(outputDevice, bounds, border, high, sides, currentSide);
        } else if (currentSide == BorderPainter.BOTTOM) {
            paintSolid(outputDevice, bounds, border, low, sides, currentSide);
        } else if (currentSide == BorderPainter.RIGHT) {
            paintSolid(outputDevice, bounds, border, low, sides, currentSide);
        } else if (currentSide == BorderPainter.LEFT) {
            paintSolid(outputDevice, bounds, border, high, sides, currentSide);
        }
    }

    private static void paintSolid(OutputDevice outputDevice, 
            final Rectangle bounds, final BorderPropertySet border, 
            final BorderPropertySet bcolor, final int sides, int currentSide) {
        Polygon poly = getBevelledPolygon(bounds, border, sides, currentSide, true);

        if (currentSide == BorderPainter.TOP) {
            outputDevice.setColor(bcolor.topColor());

            // draw a 1px border with a line instead of a polygon
            if ((int) border.top() == 1) {
                outputDevice.drawBorderLine(bounds, BorderPainter.TOP, 
                        (int)border.top(), true);
            } else {
                // use polygons for borders over 1px wide
                outputDevice.fill(poly);
            }
        } else if (currentSide == BorderPainter.BOTTOM) {
            outputDevice.setColor(bcolor.bottomColor());
            if ((int) border.bottom() == 1) {
                outputDevice.drawBorderLine(bounds, BorderPainter.BOTTOM, 
                        (int)border.bottom(), true);
            } else {
                outputDevice.fill(poly);
            }
        } else if (currentSide == BorderPainter.RIGHT) {
            outputDevice.setColor(bcolor.rightColor());
            if ((int) border.right() == 1) {
                outputDevice.drawBorderLine(bounds, BorderPainter.RIGHT, 
                        (int)border.right(), true);
            } else {
                outputDevice.fill(poly);
            }
        } else if (currentSide == BorderPainter.LEFT) {
            outputDevice.setColor(bcolor.leftColor());
            if ((int) border.left() == 1) {
                outputDevice.drawBorderLine(bounds, BorderPainter.LEFT, 
                        (int)border.left(), true);
            } else {
                outputDevice.fill(poly);
            }
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.45  2007/02/07 16:33:25  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.44  2007/01/17 17:50:54  peterbrant
 * Clean out unused code
 *
 * Revision 1.43  2006/02/21 18:09:17  peterbrant
 * Fix call to BorderPropertySet constructor
 *
 * Revision 1.42  2006/02/01 01:30:14  peterbrant
 * Initial commit of PDF work
 *
 * Revision 1.41  2006/01/27 01:15:36  peterbrant
 * Start on better support for different output devices
 *
 * Revision 1.40  2005/12/15 20:04:13  peterbrant
 * Don't paint transparent borders
 *
 * Revision 1.39  2005/11/08 20:02:14  peterbrant
 * Fix off by one errors for borders with an odd width
 *
 * Revision 1.38  2005/10/27 00:09:02  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.37  2005/10/21 18:49:44  pdoubleya
 * Fixed border painting bug.
 *
 * Revision 1.36  2005/10/21 18:10:52  pdoubleya
 * Support for cachable borders. Still buggy on some pages, but getting there.
 *
 * Revision 1.35  2005/06/22 23:48:45  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.34  2005/06/04 16:04:12  tobega
 * just playing with border colors a bit more
 *
 * Revision 1.33  2005/06/04 14:47:43  tobega
 * Just for fun: took more control over darkening/brightening colors. Looks nice, though.
 *
 * Revision 1.32  2005/06/03 01:08:58  tobega
 * Fixed bug in painting double borders
 *
 * Revision 1.31  2005/05/29 23:49:15  tobega
 * Did it right, this time, so that inline borders also look nice
 *
 * Revision 1.30  2005/05/29 23:43:28  tobega
 * Removed tendency for white diagonal line in corners
 *
 * Revision 1.29  2005/05/29 20:13:20  tobega
 * Cleaned up duplicate code
 *
 * Revision 1.28  2005/05/29 19:37:58  tobega
 * Fixed up using different style borders.
 * Fixed patterned borders to work right.
 *
 * Revision 1.27  2005/05/16 08:07:09  tobega
 * Border painting for inlines works beautifully (tested only solid borders)
 *
 * Revision 1.26  2005/05/13 11:49:59  tobega
 * Started to fix up borders on inlines. Got caught up in refactoring.
 * Boxes shouldn't cache borders and stuff unless necessary. Started to remove unnecessary references.
 * Hover is not working completely well now, might get better when I'm done.
 *
 * Revision 1.25  2005/05/13 08:46:17  tobega
 * A line is drawn to the right and below the coordinate. Needed to adjust when drawing lines for 1-pixel borders
 *
 * Revision 1.24  2005/05/12 06:24:16  joshy
 * more very minor border and background tweaks
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.23  2005/05/12 04:55:57  joshy
 * fix for issues 76
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.22  2005/05/08 14:36:58  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.21  2005/01/29 20:24:23  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.20  2005/01/25 10:56:56  pdoubleya
 * Added warning on possible duplicate code.
 *
 * Revision 1.19  2005/01/25 10:55:15  pdoubleya
 * Added warning on possible duplicate code.
 *
 * Revision 1.18  2005/01/24 22:46:42  pdoubleya
 * Added support for ident-checks using IdentValue instead of string comparisons.
 *
 * Revision 1.17  2005/01/24 14:36:34  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.16  2005/01/09 15:22:49  tobega
 * Prepared improved handling of margins, borders and padding.
 *
 * Revision 1.15  2004/12/29 10:39:34  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.14  2004/12/27 09:40:48  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.13  2004/12/27 07:43:32  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.12  2004/12/13 02:12:53  tobega
 * Borders are working again
 *
 * Revision 1.11  2004/12/12 04:18:57  tobega
 * Now the core compiles at least. Now we must make it work right. Table layout is one point that really needs to be looked over
 *
 * Revision 1.10  2004/12/12 03:33:00  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.9  2004/12/11 23:36:49  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.8  2004/12/05 00:48:58  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
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
 * Revision 1.6  2004/11/07 16:23:18  joshy
 * added support for lighten and darken to bordercolor
 * added support for different colored sides
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/06 22:49:52  joshy
 * cleaned up alice
 * initial support for inline borders and backgrounds
 * moved all of inlinepainter back into inlinerenderer, where it belongs.
 *
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/11/02 17:14:00  joshy
 * implemented double borders
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 13:50:26  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

