package org.joshy.html;

import java.awt.*;
import org.w3c.dom.*;
import org.joshy.*;
import org.joshy.html.box.*;

public class NullLayout extends Layout {
    public Box layout(Context c, Element elem) {
        return new Box(0,0,0,0);
    }
    public void paint(Context c, Box box) {
    }
}

