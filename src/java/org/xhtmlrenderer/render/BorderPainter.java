
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.x;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class BorderPainter {

    /**
     * Description of the Method
     *
     * @param ctx  PARAM
     * @param box  PARAM
     */
    public void paint( Context ctx, Box box ) {

        Graphics g = ctx.getGraphics();

        // TODO: color is per-side ((PWW 13/08/04))
        box.border_color = ctx.css.getBorderColor( box.getElement() ).topColor;

        // ASK: border-style is a shorthand property for 4 border sides
        // CSSBank/Accessor leave it as a single property, but XRStyleReference
        // explodes it to individual values. Which way to go? (PWW 13/08/04)
        box.border_style = ctx.css.getStringProperty( box.getElement(), "border-top-style" );


        Rectangle bounds = new Rectangle( box.x + box.margin.left,
                box.y + box.margin.top,
                box.width - box.margin.left - box.margin.right,
                box.height - box.margin.top - box.margin.bottom
                 );



        //u.p("border style = " + border_style);

        if ( box.border_style == null ) {
            box.border_style = "none";
        }


        // return if border = none
        if ( box.border_style.equals( "none" ) ) {
            return;
        }
        


        if ( box.border_style.equals( "ridge" ) ||
                box.border_style.equals( "groove" ) ) {
            Border bd2 = new Border();
            bd2.top = box.border.top / 2;
            bd2.bottom = box.border.bottom / 2;
            bd2.left = box.border.left / 2;
            bd2.right = box.border.right / 2;
            if ( box.border_style.equals( "ridge" ) ) {
                paintBevel( g, bounds, box.border, box.border_color.darker(), box.border_color.brighter() );
                paintBevel( g, bounds, bd2, box.border_color.brighter(), box.border_color.darker() );
            } else {
                paintBevel( g, bounds, box.border, box.border_color.brighter(), box.border_color.darker() );
                paintBevel( g, bounds, bd2, box.border_color.darker(), box.border_color.brighter() );
            }
            return;
        }


        if ( box.border_style.equals( "outset" ) ) {

            paintBevel( g, bounds, box.border, box.border_color.brighter(), box.border_color.darker() );
            return;
        }

        if ( box.border_style.equals( "inset" ) ) {

            paintBevel( g, bounds, box.border, box.border_color.darker(), box.border_color.brighter() );
            return;
        }

        if ( box.border_style.equals( "solid" ) ) {
            paintBevel( g, bounds, box.border, box.border_color, box.border_color );
        }
        
        if (box.border_style.equals ( "double" ) ) {
            // this may need to be modified to account for rounding errors
            // create a new border only 1/3 the thickness
            Border outer = new Border();
            outer.top = box.border.top / 3;
            outer.bottom = box.border.bottom / 3;
            outer.left = box.border.left / 3;
            outer.right = box.border.right / 3;
            Border center = new Border(outer);
            
            Border inner = new Border(outer);
            if(box.border.top == 1) { outer.top = 1; center.top = 0; }
            if(box.border.bottom == 1) { outer.bottom = 1; center.bottom = 0; }
            if(box.border.left == 1) { outer.left = 1; center.left = 0; }
            if(box.border.right == 1) { outer.right = 1; center.right = 0; }
            
            if(box.border.top == 2) { outer.top = 1; center.top = 0; inner.top = 1; }
            if(box.border.bottom == 2) { outer.bottom = 1; center.bottom = 0; inner.bottom = 1; }
            if(box.border.left == 2) { outer.left = 1; center.left = 0; inner.left = 1; }
            if(box.border.right == 2) { outer.right = 1; center.right = 0; inner.right = 1; }

            Rectangle b2 = shrinkRect(bounds,outer);
            b2 = shrinkRect(b2,center);
            // draw outer border
            paintSimpleBorder( (Graphics2D)g, bounds, outer, box.border_color);
            // draw inner border
            paintSimpleBorder( (Graphics2D)g, b2, inner, box.border_color);
        }

        if ( box.border_style.equals( "dashed" ) ) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
            paintRect( g2, bounds, box.border, box.border_color, new float[]{10.0f, 4.0f} );
            g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        }
        if ( box.border_style.equals( "dotted" ) ) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
            paintRect( g2, bounds, box.border, box.border_color, new float[]{box.border.top, box.border.top} );
            g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
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
    
    public void paintSimpleBorder( Graphics2D g2, Rectangle bounds, Border border, Color color) {
        g2.setColor(color);
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
    }

    /**
     * Description of the Method
     *
     * @param g2       PARAM
     * @param bounds   PARAM
     * @param border   PARAM
     * @param color    PARAM
     * @param pattern  PARAM
     */
    public void paintRect( Graphics2D g2, Rectangle bounds, Border border, Color color, float[] pattern ) {
        g2.setColor( color );
        Stroke old_stroke = g2.getStroke();
        int x = bounds.x;
        int y = bounds.y;
        int w = bounds.width;
        int h = bounds.height;

        g2.setStroke( new BasicStroke( border.top, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0 ) );
        g2.drawLine( x, y, x + w, y );
        g2.setStroke( new BasicStroke( border.left, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0 ) );
        g2.drawLine( x, y, x, y + h );
        g2.setStroke( new BasicStroke( border.right, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0 ) );
        g2.drawLine( x + w, y, x + w, y + h );
        g2.setStroke( new BasicStroke( border.bottom, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0 ) );
        g2.drawLine( x, y + h, x + w, y + h );

        g2.setStroke( old_stroke );
    }


    /**
     * Description of the Method
     *
     * @param g       PARAM
     * @param bounds  PARAM
     * @param border  PARAM
     * @param high    PARAM
     * @param low     PARAM
     */
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

}

/*
 * $Id$
 *
 * $Log$
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

