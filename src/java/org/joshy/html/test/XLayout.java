package org.joshy.html.test;

import org.joshy.html.box.*;
import org.joshy.html.*;
import java.awt.Dimension;
import org.w3c.dom.*;
import org.joshy.u;
public class XLayout extends CustomBlockLayout {

    public Dimension getIntrinsicDimensions(Context c, Element elem) {
        return new Dimension(50,50);
    }
    
    public void paintComponent(Context c, Box box) {
        Dimension dim = box.getInternalDimension();
        u.p("dim = " + dim);
        c.getGraphics().drawLine(
            box.x,
            box.y,
            box.x+(int)dim.getWidth(),
            box.y+(int)dim.getHeight());
        c.getGraphics().drawLine(
            box.x,
            box.y+(int)dim.getHeight(),
            box.x+(int)dim.getWidth(),
            box.y);
    }

}

