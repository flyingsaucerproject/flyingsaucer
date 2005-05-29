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

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.value.BorderColor;
import org.xhtmlrenderer.extend.RenderingContext;

import java.awt.*;


/**
 * Description of the Class
 *
 * @author empty
 */
public class BorderPainter {
    /**
     * Description of the Field
     */
    public static int TOP = 1;
    /**
     * Description of the Field
     */
    public static int LEFT = 2;
    /**
     * Description of the Field
     */
    public static int BOTTOM = 4;
    /**
     * Description of the Field
     */
    public static int RIGHT = 8;
    /**
     * Description of the Field
     */
    public static int ALL = TOP + LEFT + BOTTOM + RIGHT;

    /**
     * Description of the Method
     *
     * @param bounds  PARAM
     * @param sides   PARAM
     * @param style
     * @param g
     * @param ctx
     * @param xOffset for determining starting point for patterns
     */
    public static void paint(Rectangle bounds, int sides, CalculatedStyle style, Graphics g, RenderingContext ctx, int xOffset) {
        //Graphics g = c.getGraphics();
        //CalculatedStyle style = c.getCurrentStyle();

        BorderColor border_color = style.getBorderColor();
        //int width = (int) bounds.getWidth();
        //int height = (int) bounds.getHeight();
        Border border = style.getBorderWidth(ctx);
        IdentValue topStyle = null;
        IdentValue leftStyle = null;
        IdentValue bottomStyle = null;
        IdentValue rightStyle = null;
        if ((sides & TOP) == TOP) {
            topStyle = style.getIdent(CSSName.BORDER_STYLE_TOP);
            if (topStyle == IdentValue.NONE || border.top == 0) {
                sides -= TOP;
            }
        }
        if ((sides & LEFT) == LEFT) {
            leftStyle = style.getIdent(CSSName.BORDER_STYLE_LEFT);
            if (leftStyle == IdentValue.NONE || border.left == 0) {
                sides -= LEFT;
            }
        }
        if ((sides & BOTTOM) == BOTTOM) {
            bottomStyle = style.getIdent(CSSName.BORDER_STYLE_BOTTOM);
            if (bottomStyle == IdentValue.NONE || border.bottom == 0) {
                sides -= BOTTOM;
            }
        }
        if ((sides & RIGHT) == RIGHT) {
            rightStyle = style.getIdent(CSSName.BORDER_STYLE_RIGHT);
            if (rightStyle == IdentValue.NONE || border.right == 0) {
                sides -= RIGHT;
            }
        }

        //Now paint!
        if ((sides & TOP) == TOP)
            paintBorderSide(border, g, bounds, border_color, sides, TOP, topStyle, xOffset);
        if ((sides & LEFT) == LEFT)
            paintBorderSide(border, g, bounds, border_color, sides, LEFT, leftStyle, xOffset);
        if ((sides & BOTTOM) == BOTTOM)
            paintBorderSide(border, g, bounds, border_color, sides, BOTTOM, bottomStyle, xOffset);
        if ((sides & RIGHT) == RIGHT)
            paintBorderSide(border, g, bounds, border_color, sides, RIGHT, rightStyle, xOffset);
    }


    /**
     * Description of the Method
     *
     * @param rect   PARAM
     * @param border PARAM
     * @return Returns
     */
    public static Rectangle shrinkRect(final Rectangle rect, final Border border) {
        Rectangle r2 = new Rectangle();
        r2.x = rect.x + border.left;
        r2.width = rect.width - border.left - border.right;
        r2.y = rect.y + border.top;
        r2.height = rect.height - border.top - border.bottom;
        return r2;
    }

    /**
     * Description of the Method
     *
     * @param border          PARAM
     * @param g               PARAM
     * @param bounds          PARAM
     * @param border_color    PARAM
     * @param sides           PARAM
     * @param currentSide
     * @param borderSideStyle PARAM
     * @param xOffset
     */
    private static void paintBorderSide(final Border border, final Graphics g, final Rectangle bounds, final BorderColor border_color, final int sides, int currentSide, final IdentValue borderSideStyle, int xOffset) {
        Graphics2D g2 = (Graphics2D) g;
        if (borderSideStyle == IdentValue.RIDGE || borderSideStyle == IdentValue.GROOVE) {
            Border bd2 = new Border();
            bd2.top = border.top / 2;
            bd2.bottom = border.bottom / 2;
            bd2.left = border.left / 2;
            bd2.right = border.right / 2;
            if (borderSideStyle == IdentValue.RIDGE) {
                paintGoodBevel(g2, bounds, border, border_color.darker(), border_color.brighter(), sides, currentSide);
                paintGoodBevel(g2, bounds, bd2, border_color.brighter(), border_color.darker(), sides, currentSide);
            } else {
                paintGoodBevel(g2, bounds, border, border_color.brighter(), border_color.darker(), sides, currentSide);
                paintGoodBevel(g2, bounds, bd2, border_color.darker(), border_color.brighter(), sides, currentSide);
            }
            return;
        }

        if (borderSideStyle == IdentValue.OUTSET) {
            paintGoodBevel(g2, bounds, border,
                    border_color.brighter(),
                    border_color.darker(), sides, currentSide);
            return;
        }

        if (borderSideStyle == IdentValue.INSET) {
            paintGoodBevel(g2, bounds, border,
                    border_color.darker(),
                    border_color.brighter(), sides, currentSide);
            return;
        }

        if (borderSideStyle == IdentValue.SOLID) {
            paintSolid(g2, bounds, border, border_color, sides, currentSide);
            return;
        }

        if (borderSideStyle == IdentValue.DOUBLE) {
            // this may need to be modified to account for rounding errors
            // create a new border only 1/3 the thickness
            Border outer = new Border();
            outer.top = border.top / 3;
            outer.bottom = border.bottom / 3;
            outer.left = border.left / 3;
            outer.right = border.right / 3;
            Border center = new Border(outer);

            Border inner = new Border(outer);
            if (border.top == 1) {
                outer.top = 1;
                center.top = 0;
            }
            if (border.bottom == 1) {
                outer.bottom = 1;
                center.bottom = 0;
            }
            if (border.left == 1) {
                outer.left = 1;
                center.left = 0;
            }
            if (border.right == 1) {
                outer.right = 1;
                center.right = 0;
            }

            if (border.top == 2) {
                outer.top = 1;
                center.top = 0;
                inner.top = 1;
            }
            if (border.bottom == 2) {
                outer.bottom = 1;
                center.bottom = 0;
                inner.bottom = 1;
            }
            if (border.left == 2) {
                outer.left = 1;
                center.left = 0;
                inner.left = 1;
            }
            if (border.right == 2) {
                outer.right = 1;
                center.right = 0;
                inner.right = 1;
            }

            Rectangle b2 = shrinkRect(bounds, outer);
            b2 = shrinkRect(b2, center);
            // draw outer border
            paintSolid((Graphics2D) g, bounds, outer, border_color, sides, currentSide);
            // draw inner border
            paintSolid((Graphics2D) g, b2, inner, border_color, sides, currentSide);
            return;
        }

        int thickness = 0;
        if (currentSide == TOP) thickness = border.top;
        if (currentSide == BOTTOM) thickness = border.bottom;
        if (currentSide == RIGHT) thickness = border.right;
        if (currentSide == LEFT) thickness = border.left;

        if (borderSideStyle == IdentValue.DASHED) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            paintPatternedRect(g2, bounds, border, border_color, new float[]{8.0f + thickness * 2, 4.0f + thickness}, sides, currentSide, xOffset);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        if (borderSideStyle == IdentValue.DOTTED) {
            // turn off anti-aliasing or the dots will be all blurry
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            paintPatternedRect(g2, bounds, border, border_color, new float[]{thickness, thickness}, sides, currentSide, xOffset);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    /**
     * Gets the polygon to be filled for the border
     *
     * @param bounds       PARAM
     * @param border       PARAM
     * @param sides        PARAM
     * @param currentSide
     * @param isClipRegion
     */
    private static Polygon getBevelledPolygon(final Rectangle bounds, final Border border, final int sides, int currentSide, boolean isClipRegion) {
        //adjust for bug in polygon filling
        final int adjust;
        //adjust inside corners to make sides fit snugly
        final int snuggle;
        if (isClipRegion) {
            adjust = 0;
            snuggle = 1;
        } else {
            adjust = 1;
            snuggle = 0;
        }

        int rightCorner = (((sides & RIGHT) == RIGHT) ? border.right : adjust);
        int leftCorner = (((sides & LEFT) == LEFT) ? border.left - adjust : 0);
        int topCorner = (((sides & TOP) == TOP) ? border.top - adjust : 0);
        int bottomCorner = (((sides & BOTTOM) == BOTTOM) ? border.bottom : adjust);
        Polygon poly = null;
        if (currentSide == TOP) {
            if (border.top != 1) {
                // use polygons for borders over 1px wide
                poly = new Polygon();
                poly.addPoint(bounds.x, bounds.y);
                poly.addPoint(bounds.x + bounds.width - adjust, bounds.y);
                poly.addPoint(bounds.x + bounds.width - rightCorner, bounds.y + border.top - adjust);
                poly.addPoint(bounds.x + leftCorner, bounds.y + border.top - adjust);
            }
        } else if (currentSide == BOTTOM) {
            if (border.bottom != 1) {
                poly = new Polygon();
                // upper right
                poly.addPoint(bounds.x + bounds.width - rightCorner, bounds.y + bounds.height - border.bottom);
                // upper left
                poly.addPoint(bounds.x + leftCorner, bounds.y + bounds.height - border.bottom);
                // lower left
                poly.addPoint(bounds.x, bounds.y + bounds.height - adjust);
                // lower right
                poly.addPoint(bounds.x + bounds.width - adjust, bounds.y + bounds.height - adjust);
            }
        } else if (currentSide == RIGHT) {
            if (border.right != 1) {
                poly = new Polygon();
                poly.addPoint(bounds.x + bounds.width - adjust, bounds.y);
                poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + topCorner - snuggle);
                poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + bounds.height - bottomCorner + snuggle);
                poly.addPoint(bounds.x + bounds.width - adjust, bounds.y + bounds.height - adjust);
            }
        } else if (currentSide == LEFT) {
            if (border.left != 1) {
                poly = new Polygon();
                poly.addPoint(bounds.x, bounds.y);
                poly.addPoint(bounds.x + border.left - adjust, bounds.y + topCorner - snuggle);
                poly.addPoint(bounds.x + border.left - adjust, bounds.y + bounds.height - bottomCorner + snuggle);
                poly.addPoint(bounds.x, bounds.y + bounds.height - adjust);
            }
        }
        return poly;
    }

    /**
     * Description of the Method
     *
     * @param g2          PARAM
     * @param bounds      PARAM
     * @param border      PARAM
     * @param color       PARAM
     * @param pattern     PARAM
     * @param sides
     * @param currentSide
     * @param xOffset     for inline borders, to determine dash_phase of top and bottom
     */
    private static void paintPatternedRect(final Graphics2D g2, final Rectangle bounds, final Border border, final BorderColor color, final float[] pattern, final int sides, final int currentSide, int xOffset) {
        Polygon clip = getBevelledPolygon(bounds, border, sides, currentSide, true);
        Shape old_clip = g2.getClip();
        if (clip != null) g2.clip(clip);
        Stroke old_stroke = g2.getStroke();
        int x = bounds.x;
        int y = bounds.y;
        int w = bounds.width;
        int h = bounds.height;

        if (currentSide == TOP) {
            g2.setColor(color.topColor);
            g2.setStroke(new BasicStroke(border.top, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, xOffset));
            g2.drawLine(x, y + border.top / 2, x + w - 1, y + border.top / 2);
        } else if (currentSide == LEFT) {
            g2.setColor(color.leftColor);
            g2.setStroke(new BasicStroke(border.left, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
            g2.drawLine(x + border.left / 2, y, x + border.left / 2, y + h - 1);
        } else if (currentSide == RIGHT) {
            g2.setColor(color.rightColor);
            g2.setStroke(new BasicStroke(border.right, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
            g2.drawLine(x + w - border.right / 2, y, x + w - border.right / 2, y + h);
        } else if (currentSide == BOTTOM) {
            g2.setColor(color.bottomColor);
            g2.setStroke(new BasicStroke(border.bottom, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, xOffset));
            g2.drawLine(x, y + h - border.bottom / 2, x + w, y + h - border.bottom / 2);
        }

        g2.setStroke(old_stroke);
        g2.setClip(old_clip);
    }


    /**
     * Description of the Method
     *
     * @param g2          PARAM
     * @param bounds      PARAM
     * @param border      PARAM
     * @param high        PARAM
     * @param low         PARAM
     * @param sides       PARAM
     * @param currentSide
     */
    private static void paintGoodBevel(final Graphics2D g2, final Rectangle bounds, final Border border, final BorderColor high, final BorderColor low, final int sides, int currentSide) {
        if (currentSide == TOP) {
            paintSolid(g2, bounds, border, high, sides, currentSide);
        } else if (currentSide == BOTTOM) {
            paintSolid(g2, bounds, border, low, sides, currentSide);
        } else if (currentSide == RIGHT) {
            paintSolid(g2, bounds, border, low, sides, currentSide);
        } else if (currentSide == LEFT) {
            paintSolid(g2, bounds, border, high, sides, currentSide);
        }
    }

    /**
     * Description of the Method
     *
     * @param g2          PARAM
     * @param bounds      PARAM
     * @param border      PARAM
     * @param color       PARAM
     * @param sides       PARAM
     * @param currentSide
     */
    private static void paintSolid(final Graphics2D g2, final Rectangle bounds, final Border border, final BorderColor color, final int sides, int currentSide) {
        //Bug in polygon painting paints an extra pixel to the right and bottom
        //But clipping works fine!
        Polygon poly = getBevelledPolygon(bounds, border, sides, currentSide, false);
        //Shape old_clip = g2.getClip();
        //if (poly != null) g2.clip(poly);
        if (currentSide == TOP) {
            g2.setColor(color.topColor);

            // draw a 1px border with a line instead of a polygon
            if (border.top == 1) {
                g2.drawLine(bounds.x, bounds.y, bounds.x + bounds.width - 1, bounds.y);
            } else {
                // use polygons for borders over 1px wide
                g2.fillPolygon(poly);
            }
        } else if (currentSide == BOTTOM) {
            g2.setColor(color.bottomColor);
            if (border.bottom == 1) {
                g2.drawLine(bounds.x, bounds.y + bounds.height - 1,
                        bounds.x + bounds.width - 1, bounds.y + bounds.height - 1);
            } else {
                g2.fillPolygon(poly);
            }
        } else if (currentSide == RIGHT) {
            g2.setColor(color.rightColor);
            if (border.right == 1) {
                g2.drawLine(bounds.x + bounds.width - 1, bounds.y,
                        bounds.x + bounds.width - 1, bounds.y + bounds.height - 1);
            } else {
                g2.fillPolygon(poly);
            }
        } else if (currentSide == LEFT) {
            g2.setColor(color.leftColor);
            if (border.left == 1) {
                g2.drawLine(bounds.x, bounds.y,
                        bounds.x, bounds.y + bounds.height - 1);
            } else {
                g2.fillPolygon(poly);
            }
        }
        //g2.setClip(old_clip);
    }

    /*private static void p(Polygon poly) {
        System.out.println("poly = " + poly);
        for (int i = 0; i < poly.npoints; i++) {
            System.out.print(" " + poly.xpoints[i] + "," + poly.ypoints[i]);
        }
        System.out.println("");
    }*/

}

/*
 * $Id$
 *
 * $Log$
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
 * Separated current state Context into ContextImpl and the rest into SharedContext.
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

