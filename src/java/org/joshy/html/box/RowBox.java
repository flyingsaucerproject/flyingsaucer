package org.joshy.html.box;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.*;

public class RowBox extends Box {
    public List cells = new ArrayList();
    public Element elem;
    public RowBox(int x, int y, int width, int height) {
        super(x,y,width,height);
    }
}

