package org.xhtmlrenderer.forms;

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.CustomBlockLayout;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.*;
import java.awt.Point;

public class FormItemRenderer extends CustomBlockRenderer {

/**
     * Description of the Method
     *
     * @param c      PARAM
     * @param block  PARAM
     */
    public void doInlinePaint( Context c, InlineBox block ) {
        //u.p("FormItemLayout.doInlinePaint() : " + block);
        //u.p("sub = " + block.sub_block);

        // get the border and padding
        Border border = getBorder( c, block );
        Border padding = getPadding( c, block );
        Border margin = getMargin( c, block );

        // calculate the insets
        int top_inset = margin.top + border.top + padding.top;
        int left_inset = margin.left + border.left + padding.left;

        // shrink the bounds to be based on the contents
        c.getExtents().width = block.width;

        // do all of the painting
        paintBackground( c, block );
        //u.p("insets = " + left_inset  + " " + top_inset);
        c.getGraphics().translate( left_inset, top_inset );
        paintComponent( c, block.sub_block );
        c.getGraphics().translate( -left_inset, -top_inset );
        paintBorder( c, block );

        // move the origin down now that we are done painting (should move this later)
        c.getExtents().y = c.getExtents().y + block.height;
        /*
         *
         */
        //super.paint(c,block.sub_block);
    }


    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paint( Context c, Box box ) {
        if ( box instanceof InlineBox ) {
            InlineBox block = (InlineBox)box;
            //u.p("FormItemLayout.paint() box = " + block);
            //u.p("FormItemLayout.paint() sub = " + block.sub_block);
            doInlinePaint( c, block );
        } else {
            super.paint( c, box );
        }
        /*
         * // set the contents size
         * //Rectangle contents = layout(c,elem);
         * // get the border and padding
         * Border border = getBorder(c,block);
         * Border padding = getPadding(c,block);
         * Border margin = getMargin(c, block);
         * // calculate the insets
         * int top_inset = margin.top + border.top + padding.top;
         * int left_inset = margin.left + border.left + padding.left;
         * // shrink the bounds to be based on the contents
         * c.getExtents().width = block.width;
         * // do all of the painting
         * //paintBackground(c,block);
         * //u.p("insets = " + left_inset  + " " + top_inset);
         * c.getGraphics().translate(left_inset,top_inset);
         * //c.getExtents().translate(left_inset,top_inset);
         * paintComponent(c,block.sub_block);
         * c.getGraphics().translate(-left_inset,-top_inset);
         * //c.getExtents().translate(-left_inset,-top_inset);
         * //paintBorder(c,block);
         * // move the origin down now that we are done painting (should move this later)
         * c.getExtents().y = c.getExtents().y + block.height;
         */
    }

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paintComponent( Context c, Box box ) {
        //u.p("FormItemLayout.paintComponent() = " + box);
        InputBox ib = (InputBox)box;
        //u.p("left inset = " + box.totalLeftPadding());
        //u.p("comp dim = " + ib.component.getSize());
        //c.getGraphics().fillRect(box.x,box.y,box.width,box.height);

        //int yoff = c.canvas.getLocation().y;
        //u.p("yoff = " + yoff);

        //u.p("current x = " + box.x + " y " + box.y);
        Point coords = FormItemLayout.absCoords( box );

        // joshy: i don't know why we have to add the extra +5
        // i think it's because of the fact that this is a box
        // nested inside of an inline. when we redo the inline-block code
        // this should be fixed

        coords.x += box.totalLeftPadding() + box.getParent().totalLeftPadding();
        coords.y += box.totalTopPadding() + box.getParent().totalTopPadding();
        FormItemLayout.adjustVerticalAlign( coords, box );
        //u.p("abs coords = " + coords);
        //u.p("comp coords = " + ib.component.getLocation());

        Point loc = ib.component.getLocation();
        if ( loc.y != coords.y ||
                loc.x != coords.x ) {
            //u.p("coords = " + coords);
            //u.p("loc = " + loc);
            loc.y = coords.y;
            loc.x = coords.x;
            ib.component.setLocation( coords );
            ib.component.invalidate();
            //u.p("moved : " + ib.component + " to " + coords);
        }
        //Point pt = new Point(0,0);
        //comp.setLocation(pt);
        //comp.setSize(50,50);
        //comp.setLocation(50,50);
        //u.p("painting");
        //comp.paint(c.getGraphics());


    }

}
