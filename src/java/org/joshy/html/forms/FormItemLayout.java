package org.joshy.html.forms;

import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JComponent;
import org.joshy.u;
import org.joshy.html.box.Box;
import org.joshy.html.box.*;
import org.joshy.html.*;
import org.w3c.dom.*;

public abstract class FormItemLayout extends CustomBlockLayout {
    
    abstract public JComponent createComponent(Element elem);
    
    private JComponent comp;
    
    public Box createBox(Context c, Node node) {
        Element elem = (Element)node;
        comp = createComponent(elem);
        c.canvas.add(comp);
        comp.setLocation(100,100);
        //u.p("added a component to the viewport: " + comp);
        //u.p("pref size = " + comp.getPreferredSize());
        InputBox box = new InputBox();
        box.node = node;
        box.component = comp;
        return box;
    }
    
    public Dimension getIntrinsicDimensions(Context c, Element elem) {
        //comp.setLocation(50,50);
        Dimension dim = comp.getPreferredSize();
        //return new Dimension(10,10);
        //u.p("get intrinsic = " + dim);
        return dim;
    }
    
    public void doInlinePaint(Context c, InlineBox block) {
        //u.p("FormItemLayout.doInlinePaint() : " + block);
        //u.p("sub = " + block.sub_block);
        
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
        paintComponent(c,block.sub_block);
        c.getGraphics().translate(-left_inset,-top_inset);
        paintBorder(c,block);
        
        // move the origin down now that we are done painting (should move this later)
        c.getExtents().y = c.getExtents().y + block.height;
/*        */
        //super.paint(c,block.sub_block);
    }
    
    
    public void paint(Context c, Box box) {
        if(box instanceof InlineBox) {
            InlineBox block = (InlineBox)box;
            //u.p("FormItemLayout.paint() box = " + block);
            //u.p("FormItemLayout.paint() sub = " + block.sub_block);
            doInlinePaint(c,block);
        } else {
            super.paint(c,box);
        }
        /*
        
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
        //paintBackground(c,block);
        //u.p("insets = " + left_inset  + " " + top_inset);
        c.getGraphics().translate(left_inset,top_inset);
        //c.getExtents().translate(left_inset,top_inset);
        
        paintComponent(c,block.sub_block);
        
        c.getGraphics().translate(-left_inset,-top_inset);
        //c.getExtents().translate(-left_inset,-top_inset);
        //paintBorder(c,block);
        
        // move the origin down now that we are done painting (should move this later)
        c.getExtents().y = c.getExtents().y + block.height;
        
        */
    }

    public void paintComponent(Context c, Box box) {
        //u.p("FormItemLayout.paintComponent() = " + box);
        InputBox ib = (InputBox)box;
        //u.p("left inset = " + box.totalLeftPadding());
        //u.p("comp dim = " + ib.component.getSize());
        //c.getGraphics().fillRect(box.x,box.y,box.width,box.height);
        
        //int yoff = c.canvas.getLocation().y;
        //u.p("yoff = " + yoff);
        
        //u.p("current x = " + box.x + " y " + box.y);
        Point coords = absCoords(box);
        
        // joshy: i don't know why we have to add the extra +5
        // i think it's because of the fact that this is a box
        // nested inside of an inline. when we redo the inline-block code
        // this should be fixed
        
        coords.x += box.totalLeftPadding()+box.getParent().totalLeftPadding()+2;
        coords.y += box.totalTopPadding()+box.getParent().totalTopPadding()+2;
        adjustVerticalAlign(coords,box);
        //u.p("abs coords = " + coords);
        //u.p("comp coords = " + ib.component.getLocation());
        
        Point loc = ib.component.getLocation();
        if(loc.y != coords.y ||
            loc.x != coords.x) {
            //u.p("coords = " + coords);
            //u.p("loc = " + loc);
            loc.y = coords.y;
            loc.x = coords.x;
            ib.component.setLocation(coords);
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
    
    public void adjustVerticalAlign(Point coords, Box box) {
        if(box.getParent() instanceof InlineBox) {
            InlineBox ib = (InlineBox) box.getParent();
            LineBox lb = (LineBox) ib.getParent();
            //u.p("box = " + box + " parent = " + box.getParent());
            int off = lb.baseline - ib.height;
            //u.p("off = " + off);
            coords.y += off;
        }
    }


    public Point absCoords(Box box) {
        //u.p("box = " + box);
        //u.p("x = " + box.x + " y = " + box.y);
        //u.p("Parent = " + box.getParent());
        Point pt = new Point(0,0);
        pt.x += box.x;
        pt.y += box.y;
        
        if(box.getParent() != null) {
            Point pt_parent = absCoords(box.getParent());
            pt.x += pt_parent.x;
            pt.y += pt_parent.y;
            //return box.x + absX(box.getParent());
        }
        return pt;
    }

    
}
