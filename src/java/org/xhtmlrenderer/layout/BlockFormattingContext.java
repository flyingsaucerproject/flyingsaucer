package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.util.*;
import java.util.*;
import java.awt.Point;

public class BlockFormattingContext {
    private Box master = null;
    private int x, y = 0;
    private int width;
    private List left_floats, right_floats;
    private Map offset_map;
    public BlockFormattingContext(Box master) {
        this.master = master;
        left_floats = new ArrayList();
        right_floats = new ArrayList();
        offset_map = new HashMap();
    }
    public Box getMaster() {
        return master;
    }

    /* ====== positioning stuff ======== */

    public int getX() {
        return master.x + x;
    }
    public int getY() {
        return master.y + y;
    }
    public Point getOffset() {
        return new Point(x,y);
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getWidth() {
        return this.width;
        //return master.width - master.totalHorizontalPadding();
    }
    public int getHeight() {
        return master.height;
    }
    
    // we want to preserve the block formatting contexts position
    // relative to the current block, so we do a reverse translate
    // of the graphics
    public void translate(int x, int y) {
        //u.p("trans : " + x + " " + y);
        this.x -= x;
        this.y -= y;
    }
    


    /* ====== float stuff ========= */

    public void addLeftFloat(Box block) {
        //u.p("current offset at time of add: " + getOffset());
        left_floats.add(block);
        offset_map.put(block,getOffset());
    }
    
    public void addRightFloat(Box block) {
        right_floats.add(block);
    }
    
    // joshy: these line boxes may not be valid
    // the inline layout may not have a line box available yet, in fact
    /*
    public boolean isLeftFloatPresent(LineBox line) {
        return false;
    }
    
    public boolean isRightFloatPresent(LineBox line) {
        return false;
    }
    */
    
    public int getLeftFloatDistance(LineBox line) {
        //u.p("doing get left float dist. line = " + line);
        //u.p("line y = " + line.y);
        int xoff = 0;
        int yoff = 0;
        
        if(left_floats.size() == 0) {
            return 0;
        }
        
        // we only handle floats inside the same parent
        Box last_float = (Box)left_floats.get(left_floats.size()-1);
        //u.p("last float parent = " + last_float.getParent());
        //u.p("line parent = " + line.getParent());
        if(line.getParent() != last_float.getParent().getParent()) {
            //u.p("last float = " + last_float);
            Point fpt = (Point)offset_map.get(last_float);
            //u.p("float origin = " + fpt);
            //u.p("current offset = " + this.x + " " + this.y);
            Point lpt = new Point(this.x,this.y);
            //Point lpt = getAbsoluteCoords(line);
            //u.p("line origin = " + lpt);
            //u.p("line = " + line);
            lpt.y-=line.y;
            //u.p("line origin = " + lpt);
            //u.p("float bottom = " + (fpt.y-last_float.height));
            if(lpt.y > fpt.y-last_float.height) {
                //u.p("returning; " + last_float.width);
                return last_float.width;
            } else {
                return 0;
            }
        }
        for(int i=0; i<left_floats.size(); i++) {
            Box floater = (Box)left_floats.get(i);
            xoff += floater.width;
            yoff += floater.height;
            //u.p("yoff = " + yoff);
        }
        if(line.y > yoff) {
            return 0;
        }
        return xoff;
    }
    
    public int getRightFloatDistance(LineBox line) {
        //u.p("doing get left float dist. line = " + line);
        //u.p("line y = " + line.y);
        int xoff = 0;
        int yoff = 0;
        
        if(left_floats.size() == 0) {
            return 0;
        }
        
        // we only handle floats inside the same parent
        Box last_float = (Box)left_floats.get(left_floats.size()-1);
        //u.p("last float parent = " + last_float.getParent());
        //u.p("line parent = " + line.getParent());
        if(line.getParent() != last_float.getParent().getParent()) {
            return 0;
        }
        for(int i=0; i<left_floats.size(); i++) {
            Box floater = (Box)left_floats.get(i);
            xoff += floater.width;
            yoff += floater.height;
            //u.p("yoff = " + yoff);
        }
        if(line.y > yoff) {
            return 0;
        }
        //u.p("returning: " + xoff);
        return xoff;
    }
    
    public int getBottomFloatDistance(LineBox line) {
        return 0;
    }
    
    
    private Point getAbsoluteCoords(Box box) {
        //u.p("get abs : " + box);
        Point pt = new Point(box.x,box.y);
        if(box.getParent() != null) {
            if(box.getParent().getElement() != null) {
                if(box.getParent().getElement().getNodeName().equals("body")) {
                    return pt;
                }
            }
        }
        Point ptp = getAbsoluteCoords(box.getParent());
        return new Point(pt.x+ptp.x,pt.y+ptp.y);
    }
    
    public String toString() {
        return "BFC: ("+x+","+y+") - "+master+"";
    }
    
    
}
