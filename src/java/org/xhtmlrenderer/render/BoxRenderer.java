package org.xhtmlrenderer.render;

import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.util.*;
import org.xhtmlrenderer.css.Border;
import java.awt.*;

public class BoxRenderer extends DefaultRenderer {
    
    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paint( Context c, Box box ) {
        //u.p("BoxLayout.paint " + box);//+box.getElement().getNodeName()+") " + block);
        BlockBox block = (BlockBox)box;

        // copy the bounds to we don't mess it up
        Rectangle oldBounds = new Rectangle( c.getExtents() );


        if ( block.relative ) {
            paintRelative( c, block );
        } else if ( block.fixed ) {
            paintFixed( c, block );
        } else {
            paintNormal( c, block );
        }

        //u.p("here it's : " + c.getListCounter());
        if ( BoxLayout.isListItem( c, box ) ) {
            paintListItem( c, box );
        }

        // move the origin down to account for the contents plus the margin, borders, and padding
        oldBounds.y = oldBounds.y + block.height;
        c.setExtents( oldBounds );

        if ( c.debugDrawBoxes() ) {
            GraphicsUtil.drawBox( c.getGraphics(), block, Color.red );
        }
    }


    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param block  PARAM
     */
    public void paintNormal( Context c, BlockBox block ) {
        paintBackground( c, block );

        c.translateInsets( block );
        paintComponent( c, block );
        paintChildren( c, block );
        c.untranslateInsets( block );

        paintBorder( c, block );
    }

    // adjustments for relative painting
    /**
     * Description of the Method
     *
     * @param ctx    PARAM
     * @param block  PARAM
     */
    public void paintRelative( Context ctx, BlockBox block ) {
        ctx.getGraphics().translate( block.left, block.top );
        paintNormal( ctx, block );
        ctx.getGraphics().translate( -block.left, -block.top );
    }

    // adjustments for fixed painting
    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param block  PARAM
     */
    public void paintFixed( Context c, BlockBox block ) {
        int xoff = 0;
        int yoff = 0;

        xoff = c.canvas.getWidth();
        yoff = c.canvas.getHeight();
        if ( block.right_set ) {
            xoff = xoff - block.width;
        }

        if ( block.bottom_set ) {
            //joshy: this should really be block.height instead of bnds.y
            // need to fix the setting of block.height
            //joshy: need to do horizontal calcs too, inc scrolling
            //joshy: need to make the body paint the whole canvas.

            // start at the bottom of the viewport
            yoff = c.viewport.getHeight();

            // account for the width of the box
            yoff = yoff - block.height;
            // - bnds.y

            // account for the current y offset of the box
            yoff = yoff - c.getExtents().y;
            //orig.y;

            // account for the scrolling of the viewport
            yoff = yoff - c.canvas.getLocation().y;
        }

        c.translate( xoff, yoff );

        paintNormal( c, block );

        c.translate( -xoff, -yoff );
    }


    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paintBackground( Context c, Box box ) {
        Box block = box;
        // cache the background color
        getBackgroundColor( c, block );

        // get the css properties
        String back_image = c.css.getStringProperty( block.getElement(), "background-image", false );
        block.repeat = c.css.getStringProperty( block.getElement(), "background-repeat" );
        block.attachment = c.css.getStringProperty( block.getElement(), "background-attachment", false );
        // handle image positioning issues
        // need to update this to support vert and horz, not just vert
        if ( c.css.hasProperty( block.getElement(), "background-position", false ) ) {
            Point pt = c.css.getFloatPairProperty( block.getElement(), "background-position", false );
            block.background_position_horizontal = (int)pt.getX();
            block.background_position_vertical = (int)pt.getY();
        }

        // load the background image
        block.background_image = null;
        if ( back_image != null && !"none".equals( back_image ) ) {
            try {
                block.background_image = ImageUtil.loadImage( c, back_image );
            } catch ( Exception ex ) {
                ex.printStackTrace();
                u.p( ex );
            }
            /*
             * ImageIcon icon = new ImageIcon(back_image);
             * if(icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
             * block.background_image = icon.getImage();
             * }
             */
        }

        // actually paint the background
        BackgroundPainter.paint( c, block );
    }

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paintChildren( Context c, Box box ) {
        BlockBox block = (BlockBox)box;
        c.getGraphics().translate( block.x, block.y );
        super.paintChildren( c, block );
        c.getGraphics().translate( -block.x, -block.y );
    }


    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paintBorder( Context c, Box box ) {
        Box block = box;
        // get the border parts

        // paint the border
        BorderPainter bp = new BorderPainter();

        // adjust to a fixed height, if necessary
        //if (!block.auto_height) {
        //bnds.y = block.height - block.margin.top - block.margin.bottom;
        //}

        bp.paint( c, block );
    }

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paintListItem( Context c, Box box ) {
        ListItemPainter.paint( c, box );
    }

    public Border getBorder( Context c, Box box ) {
        return BoxLayout.getBorder(c,box);
    }
    
    public Border getPadding( Context c, Box box ) {
        return BoxLayout.getPadding(c,box);
    }
    public Color getBackgroundColor( Context c, Box box ) {
        return BoxLayout.getBackgroundColor(c,box);
    }
    public Border getMargin( Context c, Box box ) {
        return BoxLayout.getMargin(c,box);
    }


}
