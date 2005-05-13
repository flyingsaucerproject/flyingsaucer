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
import org.xhtmlrenderer.layout.Context;
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
     * @param bounds PARAM
     * @param sides  PARAM
     * @param style
     * @param g
     * @param ctx
     */
    public static void paint(Rectangle bounds, int sides, CalculatedStyle style, Graphics g, RenderingContext ctx) {
        //Graphics g = c.getGraphics();
        //CalculatedStyle style = c.getCurrentStyle();

        BorderColor border_color = style.getBorderColor();
        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();
        Border border = style.getBorderWidth(width, height, ctx);
        IdentValue ident = null;
        if ((sides & TOP) == TOP) {
            ident = style.getIdent(CSSName.BORDER_STYLE_TOP);
            if (ident != IdentValue.NONE) {
                paintBorderSide(border, g, bounds, border_color, TOP, ident);
            }
        }
        if ((sides & LEFT) == LEFT) {
            ident = style.getIdent(CSSName.BORDER_STYLE_LEFT);
            if (ident != IdentValue.NONE) {
                paintBorderSide(border, g, bounds, border_color, LEFT, ident);
            }
        }
        if ((sides & BOTTOM) == BOTTOM) {
            ident = style.getIdent(CSSName.BORDER_STYLE_BOTTOM);
            if (ident != IdentValue.NONE) {
                paintBorderSide(border, g, bounds, border_color, BOTTOM, ident);
            }
        }
        if ((sides & RIGHT) == RIGHT) {
            ident = style.getIdent(CSSName.BORDER_STYLE_RIGHT);
            if (ident != IdentValue.NONE) {
                paintBorderSide(border, g, bounds, border_color, RIGHT, ident);
            }
        }
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
     * @param side            PARAM
     * @param borderSideStyle PARAM
     */
    private static void paintBorderSide(final Border border, final Graphics g, final Rectangle bounds, final BorderColor border_color, final int side, final IdentValue borderSideStyle) {
        if (borderSideStyle == IdentValue.RIDGE || borderSideStyle == IdentValue.GROOVE) {
            Border bd2 = new Border();
            bd2.top = border.top / 2;
            bd2.bottom = border.bottom / 2;
            bd2.left = border.left / 2;
            bd2.right = border.right / 2;
            if (borderSideStyle == IdentValue.RIDGE) {
                paintGoodBevel(g, bounds, border, border_color.darker(), border_color.brighter(), side);
                paintGoodBevel(g, bounds, bd2, border_color.brighter(), border_color.darker(), side);
            } else {
                paintGoodBevel(g, bounds, border, border_color.brighter(), border_color.darker(), side);
                paintGoodBevel(g, bounds, bd2, border_color.darker(), border_color.brighter(), side);
            }
            return;
        }

        if (borderSideStyle == IdentValue.OUTSET) {
            paintGoodBevel(g, bounds, border,
                    border_color.brighter(),
                    border_color.darker(), side);
            return;
        }

        if (borderSideStyle == IdentValue.INSET) {
            paintGoodBevel(g, bounds, border,
                    border_color.darker(),
                    border_color.brighter(), side);
            return;
        }

        if (borderSideStyle == IdentValue.SOLID) {
            paintSolid(g, bounds, border, border_color, side);
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
            paintSolid((Graphics2D) g, bounds, outer, border_color, side);
            // draw inner border
            paintSolid((Graphics2D) g, b2, inner, border_color, side);
        }

        if (borderSideStyle == IdentValue.DASHED) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            paintPatternedRect(g2, bounds, border, border_color, new float[]{10.0f, 4.0f}, side);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        if (borderSideStyle == IdentValue.DOTTED) {
            Graphics2D g2 = (Graphics2D) g;
            // turn off anti-aliasing or the dots will be all blurry
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            paintPatternedRect(g2, bounds, border, border_color, new float[]{border.top, border.top}, side);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    /**
     * Description of the Method
     *
     * @param g2      PARAM
     * @param bounds  PARAM
     * @param border  PARAM
     * @param color   PARAM
     * @param pattern PARAM
     * @param side
     */
    private static void paintPatternedRect(final Graphics2D g2, final Rectangle bounds, final Border border, final BorderColor color, final float[] pattern, final int side) {
        Stroke old_stroke = g2.getStroke();
        int x = bounds.x;
        int y = bounds.y;
        int w = bounds.width;
        int h = bounds.height;

        if (side == TOP) {
            g2.setColor(color.topColor);
            g2.setStroke(new BasicStroke(border.top, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
            g2.drawLine(x, y, x + w, y);
        }
        if (side == LEFT) {
            g2.setColor(color.leftColor);
            g2.setStroke(new BasicStroke(border.left, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
            g2.drawLine(x, y, x, y + h);
        }
        if (side == RIGHT) {
            g2.setColor(color.rightColor);
            g2.setStroke(new BasicStroke(border.right, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
            g2.drawLine(x + w, y, x + w, y + h);
        }
        if (side == BOTTOM) {
            g2.setColor(color.bottomColor);
            g2.setStroke(new BasicStroke(border.bottom, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
            g2.drawLine(x, y + h, x + w, y + h);
        }

        g2.setStroke(old_stroke);
    }


    /**
     * Description of the Method
     *
     * @param g      PARAM
     * @param bounds PARAM
     * @param border PARAM
     * @param high   PARAM
     * @param low    PARAM
     * @param side   PARAM
     */
    private static void paintGoodBevel(final Graphics g, final Rectangle bounds, final Border border, final BorderColor high, final BorderColor low, final int side) {
        Polygon poly;
        if (side == TOP) {
            poly = new Polygon();
            poly.addPoint(bounds.x, bounds.y);
            poly.addPoint(bounds.x + bounds.width, bounds.y);
            poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + border.top);
            poly.addPoint(bounds.x + border.left, bounds.y + border.top);
            g.setColor(high.topColor);
            g.fillPolygon(poly);
        }

        if (side == BOTTOM) {
            poly = new Polygon();
            poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + bounds.height - border.bottom);
            poly.addPoint(bounds.x + border.left, bounds.y + bounds.height - border.bottom);
            poly.addPoint(bounds.x, bounds.y + bounds.height);
            poly.addPoint(bounds.x + bounds.width, bounds.y + bounds.height);
            g.setColor(low.bottomColor);
            g.fillPolygon(poly);
        }

        if (side == RIGHT) {
            poly = new Polygon();
            poly.addPoint(bounds.x + bounds.width, bounds.y);
            poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + border.top);
            poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + bounds.height - border.bottom);
            poly.addPoint(bounds.x + bounds.width, bounds.y + bounds.height);
            g.setColor(low.rightColor);
            g.fillPolygon(poly);
        }

        if (side == LEFT) {
            poly = new Polygon();
            poly.addPoint(bounds.x, bounds.y);
            poly.addPoint(bounds.x + border.left, bounds.y + border.top);
            poly.addPoint(bounds.x + border.left, bounds.y + bounds.height - border.bottom);
            poly.addPoint(bounds.x, bounds.y + bounds.height);
            g.setColor(high.leftColor);
            g.fillPolygon(poly);
        }
    }

    /**
     * Description of the Method
     *
     * @param g      PARAM
     * @param bounds PARAM
     * @param border PARAM
     * @param color  PARAM
     * @param side   PARAM
     */
    private static void paintSolid(final Graphics g, final Rectangle bounds, final Border border, final BorderColor color, final int side) {
        Polygon poly;
        // CLEAN: duplicate, except for color, with paintGoodBevel() for BOTTOM (PWW 25-01-05)
        if (side == TOP) {
			// skip 0 border
			if(border.top == 0) { return; }

			// set the color
            g.setColor(color.topColor);
			
			// draw a 1px border with a line instead of a polygon
			if(border.top == 1) {
				g.drawLine(bounds.x, bounds.y, bounds.x + bounds.width -1, bounds.y);
				return;
			}
			// use polygons for borders over 1px wide
            poly = new Polygon();
            poly.addPoint(bounds.x, bounds.y);
            poly.addPoint(bounds.x + bounds.width, bounds.y);
            poly.addPoint(bounds.x + bounds.width - border.right+1, bounds.y + border.top-1);
            poly.addPoint(bounds.x + border.left-1, bounds.y + border.top-1);
            g.fillPolygon(poly);
			return;
        }

        // CLEAN: duplicate, except for color, with paintGoodBevel() for BOTTOM (PWW 25-01-05)
        if (side == BOTTOM) {
			if(border.bottom == 0) { return; }
            g.setColor(color.bottomColor);
			if(border.bottom == 1) {
				g.drawLine(bounds.x, bounds.y + bounds.height -1,
					bounds.x + bounds.width -1, bounds.y + bounds.height -1);
				return;
			}
            poly = new Polygon();
			// upper right
            poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + bounds.height - border.bottom+1);
			// upper left
            poly.addPoint(bounds.x + border.left, bounds.y + bounds.height - border.bottom+1);
			// lower left
            poly.addPoint(bounds.x, bounds.y + bounds.height);
			// lower right
            poly.addPoint(bounds.x + bounds.width, bounds.y + bounds.height);
            g.fillPolygon(poly);
			return;
        }

        // CLEAN: duplicate, except for color, with paintGoodBevel() for BOTTOM (PWW 25-01-05)
        if (side == RIGHT) {
			if(border.right == 0) { return; }
            g.setColor(color.rightColor);
			if(border.right == 1) {
				g.drawLine(bounds.x + bounds.width -1, bounds.y,
					bounds.x + bounds.width-1, bounds.y + bounds.height -1);
				return;
			}
            poly = new Polygon();
            poly.addPoint(bounds.x + bounds.width, bounds.y);
            poly.addPoint(bounds.x + bounds.width - border.right+1, bounds.y + border.top);
            poly.addPoint(bounds.x + bounds.width - border.right+1, bounds.y + bounds.height - border.bottom);
            poly.addPoint(bounds.x + bounds.width, bounds.y + bounds.height);
            g.fillPolygon(poly);
			return;
        }

        // CLEAN: duplicate, except for color, with paintGoodBevel() for BOTTOM (PWW 25-01-05)
        if (side == LEFT) {
			if(border.left == 0) { return; }
            g.setColor(color.leftColor);
			if(border.left == 1) {
				g.drawLine(bounds.x, bounds.y, 
					bounds.x, bounds.y + bounds.height -1);
				return;
			}
            poly = new Polygon();
            poly.addPoint(bounds.x, bounds.y);
            poly.addPoint(bounds.x + border.left-1, bounds.y + border.top);
            poly.addPoint(bounds.x + border.left-1, bounds.y + bounds.height - border.bottom);
            poly.addPoint(bounds.x, bounds.y + bounds.height);
            g.fillPolygon(poly);
			return;
        }
    }
	
	private static void p(Polygon poly) {
		System.out.println("poly = " + poly);
		for(int i=0; i<poly.npoints; i++) {
			System.out.print(" " + poly.xpoints[i] + "," + poly.ypoints[i]);
		}
		System.out.println("");
	}

}

/*
 * $Id$
 *
 * $Log$
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

