package org.joshy.html.box;

import org.w3c.dom.Node;
import java.util.ArrayList;
import java.util.List;

public class LineBox extends Box{
    //public List inlines = new ArrayList();
    public int lineheight; // relative to x,y
    public int baseline;   // relative to x,y
    public String toString() {
        return "Line: (" + x + ","+y+")x("+width+","+height+")" + "  baseline = " + baseline;
    }
}
