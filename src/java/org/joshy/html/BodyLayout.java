package org.joshy.html;

import java.awt.*;
import org.w3c.dom.*;
import org.joshy.*;
import org.joshy.html.box.*;

public class BodyLayout extends BoxLayout {
    public Box layout(Context c, Element elem) {
        c.setLeftTab(new Point(0,0));
        c.setRightTab(new Point(0,0));
        return super.layout(c,elem);
    }

    public void paintBackground(Context c, Box box) {
        c.getGraphics().fillRect(0,0,c.canvas.getWidth(),c.canvas.getHeight());
        super.paintBackground(c,box);
    }

}

