
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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.*;

/**
 * Description of the Class
 *
 * @author   empty
 */
public class BackgroundPainter {
    private static final Color transparent = new Color(0,0,0,0);


    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param block  PARAM
     */
    public static void paint( Context c, Box block ) {
        
        // don't draw if the backgrounds are turned off
        if(!Configuration.isTrue("xr.renderer.draw.backgrounds",true)) {
            return;
        }

        
        if(block.border == null) return;
        Rectangle box = new Rectangle(
                block.x + block.margin.left + block.border.left,
                block.y + block.margin.top + block.border.top,
                block.width - block.margin.left - block.margin.right - block.border.left - block.border.right,
                block.height - block.margin.top - block.border.top - block.border.bottom - block.margin.bottom
                 );
                 
        // paint the background
        if ( block.background_color != null ) {
            // skip transparent background
            if(!block.background_color.equals(transparent)) {
                //TODO. make conf controlled u.p("filling a background");
                c.getGraphics().setColor( block.background_color );
                c.getGraphics().fillRect( box.x, box.y, box.width, box.height );
            }
        }

        int xoff = 0;
        int yoff = 0;

        if ( block.attachment != null && block.attachment.equals( "fixed" ) ) {
            yoff = c.canvas.getLocation().y;
            //TODO. make conf controlled u.p("setting the clip rect for fixed background");
            c.graphics.setClip( c.canvas.getVisibleRect() );
        }

        if ( block.background_image != null ) {
            int left_insets = box.x;
            int top_insets = box.y;
            int back_width = box.width;
            int back_height = box.height;
            Rectangle2D oldclip = (Rectangle2D)c.getGraphics().getClip();
            Rectangle new_clip = new Rectangle( left_insets, top_insets, back_width, back_height );
            c.getGraphics().setClip( oldclip.createIntersection( new_clip ) );

            // calculate repeat indecies
            int repeatx = 1;
            int repeaty = 1;

            if ( block.repeat == null ) {
                repeatx = 1;
                repeaty = 1;
            } else if ( block.repeat.equals( "repeat-x" ) ) {
                repeatx = back_width;
            } else if ( block.repeat.equals( "repeat-y" ) ) {
                repeaty = back_height;
            } else if ( block.repeat.equals( "repeat" ) ) {
                repeatx = back_width;
                repeaty = back_height;
            }

            double iwd = block.background_image.getWidth( null );
            double ihd = block.background_image.getHeight( null );
            int iw = block.background_image.getWidth( null );
            int ih = block.background_image.getHeight( null );

            // handle image position offsets
            /* KEEP JMM (11/16)
             * xoff = block width - image width * pos
             * pos = 0
             * block width = 300
             * image width = 100
             * if pos = 0 then
             * xoff = 0
             * if pos = 1 then
             * xoff = 200
             */
            xoff += (int)( (double)( back_width - iw ) * (double)( (double)block.background_position_horizontal / (double)100 ) );
            yoff -= (int)( (double)( back_height - ih ) * (double)( (double)block.background_position_vertical / (double)100 ) );

            // calculations for fixed tile images
            int starty = (int)Math.ceil( (double)( top_insets + yoff ) / ih );
            int endy = (int)Math.ceil( (double)( back_height + top_insets + yoff ) / ih );
            int startx = (int)Math.ceil( (double)( left_insets ) / iw );
            int endx = (int)Math.ceil( (double)( back_width + left_insets ) / iw );
            // tile the image as appropriate

            // do fixed tile image

            boolean horiz = false;
            boolean vert = false;
            if ( block.repeat.equals( "repeat-x" ) ) {
                horiz = true;
                vert = false;
            }
            if ( block.repeat.equals( "repeat-y" ) ) {
                horiz = false;
                vert = true;
            }
            if ( block.repeat.equals( "repeat" ) ) {
                horiz = true;
                vert = true;
            }

            //TODO. make conf controlled u.p("filling background with an image");
            // fixed tiled image
            if ( block.attachment != null && block.attachment.equals( "fixed" ) ) {
                tileFill( c.getGraphics(), block.background_image,
                        new Rectangle( left_insets, top_insets, back_width, back_height ),
                        xoff, -yoff, horiz, vert );
            } else {
                // do normal tile image
                tileFill( c.getGraphics(), block.background_image,
                        new Rectangle( left_insets, top_insets, back_width, back_height ),
                        xoff, -yoff, horiz, vert );
            }
            //TODO. make conf controlled u.p("setting the clip rect");
            c.getGraphics().setClip( oldclip );
        }
    }
    

    /**
     * Description of the Method
     *
     * @param g      PARAM
     * @param img    PARAM
     * @param rect   PARAM
     * @param xoff   PARAM
     * @param yoff   PARAM
     * @param horiz  PARAM
     * @param vert   PARAM
     */
    private static void tileFill( Graphics g, Image img, Rectangle rect, int xoff, int yoff, boolean horiz, boolean vert ) {
        int iwidth = img.getWidth( null );
        int iheight = img.getHeight( null );
        int rwidth = rect.width;
        int rheight = rect.height;

        if ( !horiz ) {
            rwidth = iwidth;
        }
        if ( !vert ) {
            rheight = iheight;
        }

        if ( horiz ) {
            xoff = xoff % iwidth - iwidth;
            rwidth += iwidth;
        }
        if ( vert ) {
            yoff = yoff % iheight - iheight;
            rheight += iheight;
        }

        for ( int i = 0; i < rwidth; i += iwidth ) {
            for ( int j = 0; j < rheight; j += iheight ) {
                g.drawImage( img, i + rect.x + xoff, j + rect.y + yoff, null );
            }
        }

    }


}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.7  2004/11/16 15:38:44  joshy
 * removed background printing which speeds it up considerably
 * added boolean in conf to turn off backgrounds for testing
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/10 17:28:55  joshy
 * initial support for anti-aliased text w/ minium
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
 * Revision 1.4  2004/10/27 14:03:38  joshy
 * added initial viewport repainting support
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

