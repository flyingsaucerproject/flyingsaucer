package org.joshy.html;

import java.util.*;
import java.awt.Dimension;
import org.joshy.u;
import org.joshy.html.box.*;
import org.w3c.dom.*;

public class CustomBlockLayout extends BoxLayout {
    public Box createBox(Context c, Node node) {
        BlockBox box = new BlockBox();
        box.node = node;
        return box;
    }

    /** override this method to return the proper dimensions of your
    custom page element.
    */
    public Dimension getIntrinsicDimensions(Context c, Element elem) {
        return new Dimension(50,50);
    }
    
    public Box layout(Context c, Element elem) {
        BlockBox block = (BlockBox)createBox(c,elem);
        // load the image
        
        Border border = getBorder(c, block);
        Border padding = getPadding(c, block);
        Border margin = getMargin(c, block);

        Dimension dim = this.getIntrinsicDimensions(c,elem);
        
        // calculate new contents
        block.width = (int)dim.getWidth();
        block.height = (int)dim.getHeight();
        
        // calculate the inner width
        block.width = margin.left + border.left + padding.left + block.width +
                padding.right + border.right + margin.right;
        block.height = margin.top + border.top + padding.top + block.height +
                padding.bottom + border.bottom + margin.bottom;
        
        block.x = c.getExtents().x;
        block.y = c.getExtents().y;
        return block;
    }

    /** override this to paint your component
    */
    public void paintComponent(Context c, Box box) {
        u.p("Custom components must override paintComponent");
    }

}
