package org.joshy.html;

import org.w3c.dom.*;
import java.awt.Color;
import org.joshy.u;
import org.joshy.html.box.Box;

public class ListLayout extends BoxLayout {

    public void paintChild(Context c, Box box, Layout layout) {
        /*u.p("list paint child");*/
        int y1 = c.getExtents().y;
        super.paintChild(c,box,layout);
        int y2 = c.getExtents().y;
        int y = (y1 + y2) / 2;
        c.getGraphics().fillOval(
            c.getExtents().x+c.getCursor().x-10,
            y-2,8,8);
    }
}
