package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.util.*;
import java.util.*;

public class BlockFormattingContext {
    private Box master = null;
    private int x, y = 0;
    private int width;
    private List left_floats, right_floats;
    public BlockFormattingContext(Box master) {
        this.master = master;
        left_floats = new ArrayList();
        right_floats = new ArrayList();
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
        left_floats.add(block);
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
        return xoff;
    }
    
    public int getBottomFloatDistance(LineBox line) {
        return 0;
    }
    
    public String toString() {
        return "BFC: ("+x+","+y+") - "+master+"";
    }
}
