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

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.value.BorderColor;
import org.xhtmlrenderer.layout.Context;

import java.awt.*;


/**
 * Description of the Class
 *
 * @author empty
 */
public class BorderPainter {

    /**
     * Description of the Method
     *
     * @param ctx PARAM
     * @param box PARAM
     */
    public void paint(Context ctx, Box box) {
        //u.p("checking: " + box);
        //u.p("hashcode = " + box.hashCode());
        if (box.border == null) return;
        Graphics g = ctx.getGraphics();

        // TODO: color is per-side ((PWW 13/08/04))
        //u.p("border = " + ctx.css.getBorderColor(box.getRealElement()).bottomColor);
        box.border_color = ctx.css.getStyle(box.getRealElement()).getBorderColor().topColor;
        if (box.getBorderColor() == null) {
            box.setBorderColor(ctx.css.getStyle(box.getRealElement()).getBorderColor());
        }
        //u.p("set the border colors to: " + box.getBorderColor());

        // ASK: border-style is a shorthand property for 4 border sides
        // CSSBank/Accessor leave it as a single property, but XRStyleReference
        // explodes it to individual values. Which way to go? (PWW 13/08/04)
        if (box.border_style == null) {
            box.border_style = ctx.css.getStyle(box.getRealElement()).getStringProperty(CSSName.BORDER_STYLE_TOP);
        }


        Rectangle bounds = new Rectangle(box.x + box.margin.left,
                box.y + box.margin.top,
                box.width - box.margin.left - box.margin.right,
                box.height - box.margin.top - box.margin.bottom);
        //u.p("box border style = " + box.border_style);


        //u.p("border style = " + border_style);

        if (box.border_style == null) {
            box.border_style = "none";
        }


        // return if border = none
        if (box.border_style.equals("none")) {
            return;
        }


        if (box.border_style.equals("ridge") ||
                box.border_style.equals("groove")) {
            Border bd2 = new Border();
            bd2.top = box.border.top / 2;
            bd2.bottom = box.border.bottom / 2;
            bd2.left = box.border.left / 2;
            bd2.right = box.border.right / 2;
            if (box.border_style.equals("ridge")) {
                paintGoodBevel(g, bounds, box.border, box.getBorderColor().darker(), box.getBorderColor().brighter());
                paintGoodBevel(g, bounds, bd2, box.getBorderColor().brighter(), box.getBorderColor().darker());
            } else {
                paintGoodBevel(g, bounds, box.border, box.getBorderColor().brighter(), box.getBorderColor().darker());
                paintGoodBevel(g, bounds, bd2, box.getBorderColor().darker(), box.getBorderColor().brighter());
            }
            return;
        }


        if (box.border_style.equals("outset")) {
            paintGoodBevel(g, bounds, box.border,
                    box.getBorderColor().brighter(),
                    box.getBorderColor().darker());
            return;
        }

        if (box.border_style.equals("inset")) {
            paintGoodBevel(g, bounds, box.border,
                    box.getBorderColor().darker(),
                    box.getBorderColor().brighter());
            return;
        }

        if (box.border_style.equals("solid")) {
            paintSolid(g, bounds, box.border, box.getBorderColor());
        }

        if (box.border_style.equals("double")) {
            // this may need to be modified to account for rounding errors
            // create a new border only 1/3 the thickness
            Border outer = new Border();
            outer.top = box.border.top / 3;
            outer.bottom = box.border.bottom / 3;
            outer.left = box.border.left / 3;
            outer.right = box.border.right / 3;
            Border center = new Border(outer);

            Border inner = new Border(outer);
            if (box.border.top == 1) {
                outer.top = 1;
                center.top = 0;
            }
            if (box.border.bottom == 1) {
                outer.bottom = 1;
                center.bottom = 0;
            }
            if (box.border.left == 1) {
                outer.left = 1;
                center.left = 0;
            }
            if (box.border.right == 1) {
                outer.right = 1;
                center.right = 0;
            }

            if (box.border.top == 2) {
                outer.top = 1;
                center.top = 0;
                inner.top = 1;
            }
            if (box.border.bottom == 2) {
                outer.bottom = 1;
                center.bottom = 0;
                inner.bottom = 1;
            }
            if (box.border.left == 2) {
                outer.left = 1;
                center.left = 0;
                inner.left = 1;
            }
            if (box.border.right == 2) {
                outer.right = 1;
                center.right = 0;
                inner.right = 1;
            }

            Rectangle b2 = shrinkRect(bounds, outer);
            b2 = shrinkRect(b2, center);
            // draw outer border
            paintSolid((Graphics2D) g, bounds, outer, box.getBorderColor());
            // draw inner border
            paintSolid((Graphics2D) g, b2, inner, box.getBorderColor());
        }

        if (box.border_style.equals("dashed")) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            paintPatternedRect(g2, bounds, box.border, box.getBorderColor(), new float[]{10.0f, 4.0f});
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        if (box.border_style.equals("dotted")) {
            Graphics2D g2 = (Graphics2D) g;
            // turn off anti-aliasing or the dots will be all blurry
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            paintPatternedRect(g2, bounds, box.border, box.getBorderColor(), new float[]{box.border.top, box.border.top});
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }


    public Rectangle shrinkRect(Rectangle rect, Border border) {
        Rectangle r2 = new Rectangle();
        r2.x = rect.x + border.left;
        r2.width = rect.width - border.left - border.right;
        r2.y = rect.y + border.top;
        r2.height = rect.height - border.top - border.bottom;
        return r2;
    }

    /*
public void paintSimpleBorder( Graphics2D g, Rectangle bounds, Border border, BorderColor color) {
    g2.setColor(color.topColor);
    Polygon poly = new Polygon();
    poly.addPoint( bounds.x, bounds.y );
    poly.addPoint( bounds.x + bounds.width, bounds.y );
    poly.addPoint( bounds.x + bounds.width - border.right, bounds.y + border.top );
    poly.addPoint( bounds.x + border.left, bounds.y + border.top );
    poly.addPoint( bounds.x + border.left, bounds.y + bounds.height - border.bottom );
    poly.addPoint( bounds.x, bounds.y + bounds.height );
    g2.fillPolygon( poly );
    poly = new Polygon();
    poly.addPoint( bounds.x + bounds.width, bounds.y );
    poly.addPoint( bounds.x + bounds.width - border.right, bounds.y + border.top );
    poly.addPoint( bounds.x + bounds.width - border.right, bounds.y + bounds.height - border.bottom );
    poly.addPoint( bounds.x + border.left, bounds.y + bounds.height - border.bottom );
    poly.addPoint( bounds.x, bounds.y + bounds.height );
    poly.addPoint( bounds.x + bounds.width, bounds.y + bounds.height );
    g2.fillPolygon( poly );


    Polygon poly;
    // draw top
    poly = new Polygon();
    poly.addPoint( bounds.x, bounds.y );
    poly.addPoint( bounds.x + bounds.width, bounds.y );
    poly.addPoint( bounds.x + bounds.width - border.right, bounds.y + border.top );
    poly.addPoint( bounds.x + border.left, bounds.y + border.top );
    g.setColor(color.topColor);
    g.fillPolygon( poly );

    // draw bottom
    poly = new Polygon();
    poly.addPoint( bounds.x + bounds.width - border.right, bounds.y + bounds.height - border.bottom );
    poly.addPoint( bounds.x + border.left, bounds.y + bounds.height - border.bottom );
    poly.addPoint( bounds.x, bounds.y + bounds.height );
    poly.addPoint( bounds.x + bounds.width, bounds.y + bounds.height );
    g.setColor( color.bottomColor );
    g.fillPolygon( poly );

    // draw right
    poly = new Polygon();
    poly.addPoint( bounds.x + bounds.width, bounds.y );
    poly.addPoint( bounds.x + bounds.width - border.right, bounds.y + border.top );
    poly.addPoint( bounds.x + bounds.width - border.right, bounds.y + bounds.height - border.bottom );
    poly.addPoint( bounds.x + bounds.width, bounds.y + bounds.height );
    g.setColor( color.rightColor );
    g.fillPolygon( poly );


    // draw left
    poly = new Polygon();
    poly.addPoint( bounds.x, bounds.y );
    poly.addPoint( bounds.x + border.left, bounds.y + border.top );
    poly.addPoint( bounds.x + border.left, bounds.y + bounds.height - border.bottom );
    poly.addPoint( bounds.x, bounds.y + bounds.height );
    g.setColor( color.leftColor );
    g.fillPolygon( poly );
}
*/

    /**
     * Description of the Method
     *
     * @param g2      PARAM
     * @param bounds  PARAM
     * @param border  PARAM
     * @param color   PARAM
     * @param pattern PARAM
     */
    public void paintPatternedRect(Graphics2D g2, Rectangle bounds, Border border, BorderColor color, float[] pattern) {
        Stroke old_stroke = g2.getStroke();
        int x = bounds.x;
        int y = bounds.y;
        int w = bounds.width;
        int h = bounds.height;

        g2.setColor(color.topColor);
        g2.setStroke(new BasicStroke(border.top, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
        g2.drawLine(x, y, x + w, y);
        g2.setColor(color.leftColor);
        g2.setStroke(new BasicStroke(border.left, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
        g2.drawLine(x, y, x, y + h);
        g2.setColor(color.rightColor);
        g2.setStroke(new BasicStroke(border.right, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
        g2.drawLine(x + w, y, x + w, y + h);
        g2.setColor(color.bottomColor);
        g2.setStroke(new BasicStroke(border.bottom, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
        g2.drawLine(x, y + h, x + w, y + h);

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
     */
    /*
   public void paintBevel( Graphics g, Rectangle bounds, Border border, Color high, Color low ) {
       g.setColor( high );
       Polygon poly = new Polygon();
       poly.addPoint( bounds.x, bounds.y );
       poly.addPoint( bounds.x + bounds.width, bounds.y );
       poly.addPoint( bounds.x + bounds.width - border.right, bounds.y + border.top );
       poly.addPoint( bounds.x + border.left, bounds.y + border.top );
       poly.addPoint( bounds.x + border.left, bounds.y + bounds.height - border.bottom );
       poly.addPoint( bounds.x, bounds.y + bounds.height );
       g.fillPolygon( poly );

       g.setColor( low );
       poly = new Polygon();
       poly.addPoint( bounds.x + bounds.width, bounds.y );
       poly.addPoint( bounds.x + bounds.width - border.right, bounds.y + border.top );
       poly.addPoint( bounds.x + bounds.width - border.right, bounds.y + bounds.height - border.bottom );
       poly.addPoint( bounds.x + border.left, bounds.y + bounds.height - border.bottom );
       poly.addPoint( bounds.x, bounds.y + bounds.height );
       poly.addPoint( bounds.x + bounds.width, bounds.y + bounds.height );
       g.fillPolygon( poly );
   }
   */

    public void paintGoodBevel(Graphics g, Rectangle bounds, Border border, BorderColor high, BorderColor low) {
        Polygon poly;
        // draw top
        poly = new Polygon();
        poly.addPoint(bounds.x, bounds.y);
        poly.addPoint(bounds.x + bounds.width, bounds.y);
        poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + border.top);
        poly.addPoint(bounds.x + border.left, bounds.y + border.top);
        g.setColor(high.topColor);
        g.fillPolygon(poly);
        
        // draw bottom
        poly = new Polygon();
        poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + bounds.height - border.bottom);
        poly.addPoint(bounds.x + border.left, bounds.y + bounds.height - border.bottom);
        poly.addPoint(bounds.x, bounds.y + bounds.height);
        poly.addPoint(bounds.x + bounds.width, bounds.y + bounds.height);
        g.setColor(low.bottomColor);
        g.fillPolygon(poly);
        
        // draw right
        poly = new Polygon();
        poly.addPoint(bounds.x + bounds.width, bounds.y);
        poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + border.top);
        poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + bounds.height - border.bottom);
        poly.addPoint(bounds.x + bounds.width, bounds.y + bounds.height);
        g.setColor(low.rightColor);
        g.fillPolygon(poly);
        
        
        // draw left
        poly = new Polygon();
        poly.addPoint(bounds.x, bounds.y);
        poly.addPoint(bounds.x + border.left, bounds.y + border.top);
        poly.addPoint(bounds.x + border.left, bounds.y + bounds.height - border.bottom);
        poly.addPoint(bounds.x, bounds.y + bounds.height);
        g.setColor(high.leftColor);
        g.fillPolygon(poly);
    }

    public void paintSolid(Graphics g, Rectangle bounds, Border border, BorderColor color) {
        Polygon poly;
        // draw top
        poly = new Polygon();
        poly.addPoint(bounds.x, bounds.y);
        poly.addPoint(bounds.x + bounds.width, bounds.y);
        poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + border.top);
        poly.addPoint(bounds.x + border.left, bounds.y + border.top);
        g.setColor(color.topColor);
        g.fillPolygon(poly);
        
        // draw bottom
        poly = new Polygon();
        poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + bounds.height - border.bottom);
        poly.addPoint(bounds.x + border.left, bounds.y + bounds.height - border.bottom);
        poly.addPoint(bounds.x, bounds.y + bounds.height);
        poly.addPoint(bounds.x + bounds.width, bounds.y + bounds.height);
        g.setColor(color.bottomColor);
        g.fillPolygon(poly);
        
        // draw right
        poly = new Polygon();
        poly.addPoint(bounds.x + bounds.width, bounds.y);
        poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + border.top);
        poly.addPoint(bounds.x + bounds.width - border.right, bounds.y + bounds.height - border.bottom);
        poly.addPoint(bounds.x + bounds.width, bounds.y + bounds.height);
        g.setColor(color.rightColor);
        g.fillPolygon(poly);
        
        
        // draw left
        poly = new Polygon();
        poly.addPoint(bounds.x, bounds.y);
        poly.addPoint(bounds.x + border.left, bounds.y + border.top);
        poly.addPoint(bounds.x + border.left, bounds.y + bounds.height - border.bottom);
        poly.addPoint(bounds.x, bounds.y + bounds.height);
        g.setColor(color.leftColor);
        g.fillPolygon(poly);
    }

}

/*
 * $Id$
 *
 * $Log$
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

