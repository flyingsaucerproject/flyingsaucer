package org.joshy.html;



import java.awt.MediaTracker;

import java.util.ArrayList;

import java.awt.Color;

import java.awt.Point;

import java.awt.Rectangle;

import javax.swing.ImageIcon;



import org.w3c.dom.Element;

import org.w3c.dom.Node;



import org.joshy.u;

import org.joshy.x;

import org.joshy.html.box.BlockBox;

import org.joshy.html.box.Box;

import org.joshy.html.painter.BackgroundPainter;

import org.joshy.html.painter.BorderPainter;

import org.joshy.html.painter.ListItemPainter;

import org.joshy.html.util.GraphicsUtil;

import org.joshy.html.util.ImageUtil;



public class BoxLayout extends Layout {



    public BoxLayout() {

    }



    public Box createBox(Context c, Node node) {

        BlockBox block = new BlockBox();

        block.node = node;

        return block;

    }



    public void prepareBox(Box box, Context c) {

        Border border = getBorder(c,box);

        Border padding = getPadding(c,box);

        Border margin = getMargin(c,box);

    }

    

    public Box layout(Context c, Element elem) {
        // this is to keep track of when we are inside of a form
        if(elem.getNodeName().equals("form")) {
            if(elem.hasAttribute("name")) {
                String name = elem.getAttribute("name");
                String action = elem.getAttribute("action");
                c.setForm(name,action);
            }
        }

        //u.p("BoxLayout.layout() : " + elem);

        //u.p("box layout for: " + elem.getNodeName());

        BlockBox block = (BlockBox)createBox(c,elem);



        Rectangle oe = c.getExtents();

        //u.p("old extents = " + c.getExtents());

        c.setExtents(new Rectangle(oe));



        adjustWidth(c, block);

        adjustHeight(c, block);

        block.x = c.getExtents().x;

        block.y = c.getExtents().y;



        prepareBox(block,c);

        Border border = getBorder(c, block);

        Border padding = getPadding(c, block);

        Border margin = getMargin(c, block);

        getBackgroundColor(c,block);

        //u.p("margin on box block = " + block.margin);

        //u.p("border on box block = " + block.border);

        //u.p("padding on box block = " + block.padding);





        // do children's layout

        //u.p("avail space = " + block.width);

        layoutChildren(c, block);



        // calculate the inner width

        block.width = margin.left + border.left + padding.left + block.width +

                padding.right + border.right + margin.right;

        block.height = margin.top + border.top + padding.top + block.height +

                padding.bottom + border.bottom + margin.bottom;

        //u.p("final width = " + block.width);



        // if this is a fixed height, then set it explicitly

        /*if (!block.auto_height) {

            contents.height = block.height;

        }

        */



        //u.p("old extents = " + c.getExtents());

        //restore the extents

        c.setExtents(oe);



        // account for special positioning

        setupRelative(c, block);

        setupFixed(c, block);



        this.contents_height = block.height;

        if(elem.getNodeName().equals("form")) {
            if(elem.hasAttribute("name")) {
                c.setForm(null,null);
            }
        }

        return block;

    }



    

    // calculate the width based on css and available space

    public void adjustWidth(Context c, BlockBox block) {

        //c.getExtents().width = 150;

        //u.p("current width = " + c.getExtents().width);

        if(!block.isElement()) { return; }

        Element elem = block.getElement();

        if (c.css.hasProperty(elem, "width", false)) {

            float new_width = c.css.getFloatProperty(elem, "width", c.getExtents().width, false);

            c.getExtents().width = (int) new_width;

            block.width = (int) new_width;

            block.auto_width = false;

            //u.p("setting width: " + block.width);

        }

    }

    

    // calculate the height based on css and available space

    public void adjustHeight(Context c, BlockBox block) {

        if(!block.isElement()) { return; }

        Element elem = block.getElement();

        if (c.css.hasProperty(elem, "height")) {

            float new_height = c.css.getFloatProperty(elem, "height", c.getExtents().height);

            c.getExtents().height = (int) new_height;

            block.height = (int) new_height;

            block.auto_height = false;

        }

    }





    public void setupFixed(Context c, Box box) {

        if (isFixed(c, box)) {

            //System.out.println("setting fixed for box: " + box);

            box.fixed = true;

            if (c.css.hasProperty(box.node, "right", false)) {

                box.right = (int) c.css.getFloatProperty(box.node, "right", 0, false);

                box.right_set = true;

            }

            if (c.css.hasProperty(box.node, "bottom", false)) {

                box.bottom = (int) c.css.getFloatProperty(box.node, "bottom", 0, false);

                box.bottom_set = true;

            }

        }

    }







    public static void setupRelative(Context c, Box box) {

        String position = getPosition(c, box);

        if (position.equals("relative")) {

            if (c.css.hasProperty(box.node, "right", false)) {

                box.left = -(int) c.css.getFloatProperty(box.node, "right", 0, false);

            }

            if (c.css.hasProperty(box.node, "bottom", false)) {

                box.top = -(int) c.css.getFloatProperty(box.node, "bottom", 0, false);

            }

            if (c.css.hasProperty(box.node, "top", false)) {

                box.top = (int) c.css.getFloatProperty(box.node, "top", 0, false);

            }

            if (c.css.hasProperty(box.node, "left", false)) {

                box.left = (int) c.css.getFloatProperty(box.node, "left", 0, false);

            }

            box.relative = true;

        }

    }





    public Box layoutChildren(Context c, Box box) {

        //u.p("BoxLayout.layoutChildren("+box+")");

        BlockBox block = (BlockBox)box;

        c.shrinkExtents(block);

        super.layoutChildren(c, block);

        c.unshrinkExtents(block);

        //u.p("BoxLayout.layoutChildren() returning children layout of: " + rt);

        return block;

    }

    

    public boolean isListItem(Context c, Box box) {

        String display = c.css.getStringProperty((Element)box.node,"display",false);

        //u.p("display = " + display);

        if(display.equals("list-item")) {

            return true;

        }

        return false;

    }





    public void paint(Context c, Box box) {

        //u.p("BoxLayout.paint " + box);//+box.getElement().getNodeName()+") " + block);

        BlockBox block = (BlockBox)box;



        // copy the bounds to we don't mess it up

        Rectangle oldBounds = new Rectangle(c.getExtents());







        if (block.relative) {

            paintRelative(c,block);

        } else if (block.fixed) {

            paintFixed(c,block);

        } else {

            paintNormal(c,block);

        }



        //u.p("here it's : " + c.getListCounter());

        if(isListItem(c,box)) {

            paintListItem(c,box);

        }



        // move the origin down to account for the contents plus the margin, borders, and padding

        oldBounds.y = oldBounds.y + block.height;

        c.setExtents(oldBounds);

        

        if(c.debugDrawBoxes()) {

            GraphicsUtil.drawBox(c.getGraphics(),block,Color.red);

        }

    }

    

    

    

    public void paintNormal(Context c, BlockBox block) {

        paintBackground(c, block);

        

        c.translateInsets(block);

        paintComponent(c, block);

        paintChildren(c, block);

        c.untranslateInsets(block);

        

        paintBorder(c, block);

    }

    

    // adjustments for relative painting

    public void paintRelative(Context ctx, BlockBox block) {

        ctx.getGraphics().translate(block.left, block.top);

        paintNormal(ctx,block);

        ctx.getGraphics().translate(-block.left, -block.top);

    }

    

    // adjustments for fixed painting

    public void paintFixed(Context c, BlockBox block) {

        int xoff = 0;

        int yoff = 0;

        

        xoff = c.canvas.getWidth();

        yoff = c.canvas.getHeight();

        if (block.right_set) {

            xoff = xoff - block.width;

        }



        if (block.bottom_set) {

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



        c.translate(xoff, yoff);

        

        paintNormal(c,block);

        

        c.translate(-xoff, -yoff);

    }



    

    public void paintBackground(Context c, Box box) {

        Box block = box;

        // cache the background color

        getBackgroundColor(c, block);



        // get the css properties

        String back_image = c.css.getStringProperty(block.getElement(), "background-image", false);

        block.repeat = c.css.getStringProperty(block.getElement(), "background-repeat");

        block.attachment = c.css.getStringProperty(block.getElement(), "background-attachment",false);
        

        // handle image positioning issues

        // need to update this to support vert and horz, not just vert

        if(c.css.hasProperty(block.getElement(),"background-position",false)) {

            Point pt = c.css.getFloatPairProperty(block.getElement(),"background-position",false);

            block.background_position_horizontal = (int)pt.getX();

            block.background_position_vertical = (int)pt.getY();

        }



        // load the background image

        block.background_image = null;

        if (back_image != null && !"none".equals(back_image)) {

            try {
                block.background_image = ImageUtil.loadImage(c,back_image);

            } catch (Exception ex) {
                ex.printStackTrace();
                u.p(ex);

            }

            /*

            ImageIcon icon = new ImageIcon(back_image);

            if(icon.getImageLoadStatus() == MediaTracker.COMPLETE) {

                block.background_image = icon.getImage();

            }

            */

        }



        // actually paint the background

        BackgroundPainter.paint(c, block);

    }



    public void paintChildren(Context c, Box box) {

        BlockBox block = (BlockBox)box;

        c.getGraphics().translate(block.x,block.y);

        super.paintChildren(c, block);

        c.getGraphics().translate(-block.x,-block.y);

    }





    public void paintBorder(Context c, Box box) {

        Box block = box;

        // get the border parts



        // paint the border

        BorderPainter bp = new BorderPainter();



        // adjust to a fixed height, if necessary

        //if (!block.auto_height) {

            //bnds.y = block.height - block.margin.top - block.margin.bottom;

        //}

        

        bp.paint(c, block);

    }



    public void paintListItem(Context c, Box box) {
        ListItemPainter.paint(c,box);
    }

    

    // === caching accessors =========



    public Border getBorder(Context c, Box box) {

        if(box.isElement()) {

            if(box.border == null) {

                box.border = c.css.getBorderWidth(box.getElement());

            }

        }

        return box.border;

    }





    public Border getPadding(Context c, Box box) {

        if(box.isElement()) {

            if(box.padding == null) {

                box.padding = c.css.getPaddingWidth(box.getElement());
            }

        }

        return box.padding;

    }





    public Border getMargin(Context c, Box box) {

        if(box.isElement()) {

            if(box.margin == null) {

                box.margin = c.css.getMarginWidth(box.getElement());
            }

        }

        return box.margin;

    }



    private Color getBackgroundColor(Context c, Box box) {

        if(box.background_color == null) {
            Object obj = c.css.getProperty(box.getElement(),"background-color",false);
            //u.p("got : " + obj);
            if(obj.toString().equals("transparent")) {
                box.background_color = new Color(0,0,0,0);
                return box.background_color;
            }

            box.background_color = c.css.getBackgroundColor(box.getElement());

        }

        return box.background_color;

    }



}





