package org.joshy.html.box;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.*;

public class CellBox extends BlockBox {
    public Box sub_box;
    public CellBox(int x, int y, int width, int height) {
        super(x,y,width,height);
    }
}
