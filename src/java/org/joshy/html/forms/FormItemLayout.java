package org.joshy.html.forms;

import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JComponent;
import org.joshy.u;
import org.joshy.html.box.Box;
import org.joshy.html.*;
import org.w3c.dom.*;

public abstract class FormItemLayout extends CustomBlockLayout {
    
    abstract public JComponent createComponent(Element elem);
    
    public Box createBox(Context c, Node node) {
        Element elem = (Element)node;
        JComponent comp = createComponent(elem);
        c.canvas.add(comp);
        comp.setLocation(100,100);
        //u.p("added a component to the viewport: " + comp);
        InputBox box = new InputBox();
        box.node = node;
        box.component = comp;
        return box;
    }
    
    public Dimension getIntrinsicDimensions(Context c, Element elem) {
        //comp.setLocation(50,50);
        return new Dimension(50,50);
    }

    public void paintComponent(Context c, Box box) {
        InputBox ib = (InputBox)box;
        //int yoff = c.canvas.getLocation().y;
        //u.p("yoff = " + yoff);
        
        //u.p("current x = " + box.x);
        Point coords = absCoords(box);
        
        Point loc = ib.component.getLocation();
        if(loc.y != coords.y ||
            loc.x != coords.x) {
            //u.p("coords = " + coords);
            //u.p("loc = " + loc);
            //loc.y = coords.y;
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

    public Point absCoords(Box box) {
        //u.p("box = " + box);
        //u.p("x = " + box.x);
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
