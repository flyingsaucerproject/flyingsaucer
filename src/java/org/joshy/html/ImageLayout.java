package org.joshy.html;

import org.joshy.html.box.BlockBox;
import org.joshy.html.box.Box;
import org.joshy.html.box.InlineBox;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Rectangle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.net.URL;
import org.joshy.u;

public class ImageLayout extends BoxLayout {
    //Image img;
    public Box createBox(Context c, Node node) {
        BlockBox box = new BlockBox();
        box.node = node;
        return box;
    }
    
    private Image getImage(Context c, Node node) {
        if(node.getNodeType() != node.ELEMENT_NODE) {
            return null;
        }
        String src = ((Element)node).getAttribute("src");
        Image img = null;
        try {
            img = ImageUtil.loadImage(c,src);
        } catch (Exception ex) {
            u.p(ex);
        }
        return img;
    }
    public Box layout(Context c, Element elem) {
        BlockBox block = (BlockBox)createBox(c,elem);
        // load the image
        
        Border border = getBorder(c, block);
        Border padding = getPadding(c, block);
        Border margin = getMargin(c, block);

        
        Image img = getImage(c,elem);
        // calculate new contents
        if(img != null) {
            block.width = img.getWidth(null);
            block.height = img.getHeight(null);
        } else {
            block.width = 50;
            block.height = 50;
        }
        //Rectangle contents = new Rectangle(0,0,img.getWidth(null),img.getHeight(null));
                /*
        block.width = this.getMargin(c,elem).left + this.getBorder(c,elem).left + this.getPadding(c,elem).left +
            block.width +
            this.getMargin(c,elem).right + this.getBorder(c,elem).right + this.getPadding(c,elem).right;
        block.height = this.getMargin(c,elem).top + this.getBorder(c,elem).top + this.getPadding(c,elem).top +
            block.height +
            this.getMargin(c,elem).bottom + this.getBorder(c,elem).bottom + this.getPadding(c,elem).bottom;
        */
        // calculate the inner width
        block.width = margin.left + border.left + padding.left + block.width +
                padding.right + border.right + margin.right;
        block.height = margin.top + border.top + padding.top + block.height +
                padding.bottom + border.bottom + margin.bottom;
        // create the new block box
        //block = new BlockBox();
        block.x = c.getExtents().x;
        block.y = c.getExtents().y;
        //block.width = contents.width;
        //block.height = contents.height;
        return block;
    }
    

        /*
    public void paint(Context c, InlineBox box) {
        // save the old extents
        Rectangle oldExtents = new Rectangle(c.getExtents());
        
        Element elem = (Element)box.node;

        // set the contents size
        Rectangle contents = layout(c,elem);
        
        // get the border and padding
        Border border = getBorder(c,elem);
        Border padding = getPadding(c,elem);
        Border margin = c.css.getMarginWidth(elem);

        // calculate the insets
        int top_inset = margin.top + border.top + padding.top;
        int left_inset = margin.left + border.left + padding.left;

        // shrink the bounds to be based on the contents
        c.getExtents().width = contents.width;
        
        // do all of the painting
        // set the origin to the origin of our box
        c.getExtents().y = box.y;
        c.getExtents().x = box.x;
        
        
        paintBackground(c,elem,contents);
        
        // move the contents in to account for the insets
        c.getExtents().translate(left_inset,top_inset);
        paintComponent(c,elem,contents);
        c.getExtents().translate(-left_inset,-top_inset);
        
        paintBorder(c,elem,contents);
        
        // restore the old extents
        c.setExtents(oldExtents);        
    }
    */
    
    public void paint(Context c, Box box) {
        InlineBox block = (InlineBox)box;
        // set the contents size
        //Rectangle contents = layout(c,elem);
        
        // get the border and padding
        Border border = getBorder(c,block);
        Border padding = getPadding(c,block);
        Border margin = getMargin(c, block);

        // calculate the insets
        int top_inset = margin.top + border.top + padding.top;
        int left_inset = margin.left + border.left + padding.left;

        // shrink the bounds to be based on the contents
        c.getExtents().width = block.width;
        
        // do all of the painting
        paintBackground(c,block);
        //u.p("insets = " + left_inset  + " " + top_inset);
        c.getGraphics().translate(left_inset,top_inset);
        //c.getExtents().translate(left_inset,top_inset);
        paintComponent(c,block);
        c.getGraphics().translate(-left_inset,-top_inset);
        //c.getExtents().translate(-left_inset,-top_inset);
        paintBorder(c,block);
        
        // move the origin down now that we are done painting (should move this later)
        c.getExtents().y = c.getExtents().y + block.height;
    }
    
    public void paintComponent(Context c, Box box) {
        Image img = getImage(c,box.node);
        if(img != null) {
            c.getGraphics().drawImage(img,box.x,box.y,null);
        }
    }

    /*
    public void paintComponent(Context c, Element elem, InlineBox box) {
        c.getGraphics().drawImage(img,box.x,box.y,null);
    }
    */


}
