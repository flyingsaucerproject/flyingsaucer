
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

package org.xhtmlrenderer.forms;

import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JComponent;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.css.*;
import org.w3c.dom.*;

public abstract class FormItemLayout extends CustomBlockLayout {
    
    abstract public JComponent createComponent(Context c, Element elem);
    
    private JComponent comp;
    
    public Box createBox(Context c, Node node) {
        Element elem = (Element)node;
        comp = createComponent(c,elem);
        c.canvas.add(comp);
        comp.setLocation(100,100);
        //u.p("added a component to the viewport: " + comp);
        //u.p("pref size = " + comp.getPreferredSize());
        InputBox box = new InputBox();
        box.node = node;
        box.component = comp;
        
        // this is so the context has a reference to all forms, fields,
        // and components of those fields
        if(elem.hasAttribute("name")) {
            c.addInputField(elem.getAttribute("name"),elem,comp);
        }
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
        
        coords.x += box.totalLeftPadding()+box.getParent().totalLeftPadding();
        coords.y += box.totalTopPadding()+box.getParent().totalTopPadding();
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
            //u.p("box = " + box);
            //u.p("margin = " + box.margin);
            //u.p("border = " + box.border);
            //u.p("padding = " + box.padding);
            //u.p("ib = " + ib);
            //u.p("margin = " + ib.margin);
            //u.p("border = " + ib.border);
            //u.p("padding = " + ib.padding);
            //u.p("lb = " + lb);
            int off = lb.baseline - (ib.height ) + 5;
            coords.x += 5;
            coords.y += off;
            
            coords.x -= box.margin.left;
            coords.x -= box.border.left;
            coords.x -= box.padding.left;
            coords.y -= box.margin.top;
            coords.y -= box.border.top;
            coords.y -= box.padding.top;
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

    protected void commonPrep(JComponent comp, Element elem) {
        if(elem.hasAttribute("disabled") &&
            elem.getAttribute("disabled").equals("disabled")) {
            comp.setEnabled(false);
        }
    }
}
