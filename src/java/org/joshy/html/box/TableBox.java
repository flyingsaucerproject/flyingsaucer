package org.joshy.html.box;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.*;

public class TableBox extends BlockBox {
    public List rows = new ArrayList();
    public Element elem;
    public TableBox() {
        super();
    }
    public TableBox(int x, int y, int width, int height) {
        super(x,y,width,height);
    }
    public Point spacing;
}

