package org.joshy.html.box;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.*;
import org.joshy.u;

public class CellBox extends BlockBox {
    public Box sub_box;
    private boolean virtual = false;
    private CellBox real_box = null;
    public CellBox(int x, int y, int width, int height) {
        super(x,y,width,height);
    }
    
    public boolean isReal() {
        return !virtual;
    }
    public CellBox getReal() {
        return real_box;
    }
    
    public static CellBox createVirtual(CellBox real) {
        if(real == null) {
            u.p("WARNING: real is null!!!");
        }
        CellBox cb = new CellBox(0,0,0,0);
        cb.virtual = true;
        cb.real_box = real;
        return cb;
    }
    
    public RowBox rb;
        
}
