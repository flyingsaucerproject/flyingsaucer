package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.Uu;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockFormattingContext {
    private Box master = null;
    private int x, y = 0;
    private int width;
    private List left_floats, right_floats;
    private Map offset_map;
    private List abs_bottom;

    public BlockFormattingContext(Box master) {
        this.master = master;
        left_floats = new ArrayList();
        right_floats = new ArrayList();
        offset_map = new HashMap();
        abs_bottom = new ArrayList();
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
        return new Point(x, y);
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
        //Uu.p("trans : " + Xx + " " + y);
        this.x -= x;
        this.y -= y;
    }
    


    /* ====== float stuff ========= */

    public void addLeftFloat(Box block) {
        // Uu.p("adding a left float: " + block);
        //Uu.dump_stack();
        left_floats.add(block);
        offset_map.put(block, getOffset());
    }

    public void addRightFloat(Box block) {
        right_floats.add(block);
        offset_map.put(block, getOffset());
    }

    public Point getRightAddPoint(Box block) {
        return (Point) offset_map.get(block);
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
        int xoff = 0;
        int yoff = 0;

        if (left_floats.size() == 0) {
            return 0;
        }
        // Uu.p("left floats size = " + left_floats.size());
        // Uu.p("doing get left float dist. line = " + line);
        // Uu.p("line y = " + line.y);
        
        // we only handle floats inside the same parent
        Box last_float = (Box) left_floats.get(left_floats.size() - 1);
        // Uu.p("last float = " + last_float);
        // Uu.p("last float parent = " + last_float.getParent());
        // Uu.p("line parent = " + line.getParent());
        if (line.getParent() == null) {
            Uu.p("WARNING. In the BFC there is a line w/o a parent yet");
            return 0;
        }
        if (last_float.getParent() == null) {
            Uu.p("WARNING. In the BFC there is a last_float w/o a parent yet");
            return 0;
        }

        if (line.getParent() != last_float.getParent().getParent()) {
            //Uu.p("last float = " + last_float);
            Point fpt = (Point) offset_map.get(last_float);
            //Uu.p("float origin = " + fpt);
            //Uu.p("current offset = " + this.Xx + " " + this.y);
            Point lpt = new Point(this.x, this.y);
            //Point lpt = getAbsoluteCoords(line);
            //Uu.p("line origin = " + lpt);
            //Uu.p("line = " + line);
            lpt.y -= line.y;
            //Uu.p("line origin = " + lpt);
            //Uu.p("float bottom = " + (fpt.y-last_float.height));
            if (lpt.y > fpt.y - last_float.height) {
                //Uu.p("returning; " + last_float.width);
                return last_float.width;
            } else {
                return 0;
            }
        }


        for (int i = 0; i < left_floats.size(); i++) {
            Box floater = (Box) left_floats.get(i);
            xoff += floater.width;
            yoff += floater.height;
            // Uu.p("yoff = " + yoff);
        }
        if (line.y > yoff) {
            // Uu.p("returning 0");
            return 0;
        }
        // Uu.p("returnning : " + xoff);
        return xoff;
    }

    public Box getLeftFloatX(Box box) {
        // count backwards through the floats
        int x = 0;
        for (int i = left_floats.size() - 1; i >= 0; i--) {
            Box floater = (Box) left_floats.get(i);
            // Uu.p("box = " + box);
            // Uu.p("testing against float = " + floater);
            x = floater.x + floater.width;
            if (floater.y + floater.height > box.y) {
                // Uu.p("float["+i+"] blocks the box vertically");
                return floater;
            } else {
                // Uu.p("float["+i+"] doesn't block. moving to next");
            }
        }
        return null;
    }

    public Box pushDownLeft(Box box) {
        // make sure the box doesn't overlap any floats
        // Uu.p("pushing down box: " + box);
        for (int i = 0; i < left_floats.size(); i++) {
            Box floater = (Box) left_floats.get(i);
            // Uu.p("testing against: " + floater);
            if (floater.y >= box.y && (box.x >= floater.x &&
                    box.x < floater.x + floater.width)) {
                // Uu.p("pushing down " + box);
                box.y = floater.y + floater.height;
            }
        }
        return box;
    }

    public Box pushDownRight(Box box) {
        // make sure the box doesn't overlap any floats
        // Uu.p("pushing down box: " + box);
        for (int i = 0; i < right_floats.size(); i++) {
            Box floater = (Box) right_floats.get(i);
            // Uu.p("testing against: " + floater);
            if (floater.y >= box.y && (box.x >= floater.x &&
                    box.x < floater.x + floater.width)) {
                // Uu.p("pushing down " + box);
                box.y = floater.y + floater.height;
            }
        }
        return box;
    }

    public Box getRightFloatX(Box box) {
        // count backwards through the floats
        int x = 0;
        for (int i = right_floats.size() - 1; i >= 0; i--) {
            Box floater = (Box) right_floats.get(i);
            // Uu.p("box = " + box);
            // Uu.p("testing against float = " + floater);
            x = floater.x;
            if (floater.y + floater.height > box.y) {
                // Uu.p("float["+i+"] blocks the box vertically");
                return floater;
            } else {
                // Uu.p("float["+i+"] doesn't block. moving to next");
            }
        }
        return null;
    }
    
    // public Box getLastLeftFloat() {
    //     return (Box)left_floats.get(left_floats.size()-1);
    // }
    
    public int getRightFloatDistance(LineBox line) {
        //Uu.p("doing get right float dist. line = " + line);
        //Uu.p("line y = " + line.y);
        int xoff = 0;
        int yoff = 0;

        if (right_floats.size() == 0) {
            return 0;
        }
        
        /*
        // we only handle floats inside the same parent
        Box last_float = (Box)right_floats.get(right_floats.size()-1);
        //Uu.p("last float parent = " + last_float.getParent());
        //Uu.p("line parent = " + line.getParent());
        if(line.getParent() != last_float.getParent().getParent()) {
            //Uu.p("last float = " + last_float);
            Point fpt = (Point)offset_map.get(last_float);
            //Uu.p("float origin = " + fpt);
            //Uu.p("current offset = " + this.Xx + " " + this.y);
            Point lpt = new Point(this.Xx,this.y);
            //Point lpt = getAbsoluteCoords(line);
            //Uu.p("line origin = " + lpt);
            //Uu.p("line = " + line);
            lpt.y-=line.y;
            //Uu.p("line origin = " + lpt);
            //Uu.p("float bottom = " + (fpt.y-last_float.height));
            if(lpt.y > fpt.y-last_float.height) {
                //Uu.p("returning; " + last_float.width);
                return last_float.width;
            } else {
                return 0;
            }
        }
        */
        for (int i = 0; i < right_floats.size(); i++) {
            Box floater = (Box) right_floats.get(i);
            xoff += floater.width;
            yoff += floater.height;
            // Uu.p("yoff = " + yoff);
        }
        if (line.y > yoff) {
            // Uu.p("returnning 0");
            return 0;
        }
        // Uu.p("returnning: " + xoff);
        return xoff;
    }

    public int getBottomFloatDistance(LineBox line) {
        return 0;
    }


    public void addAbsoluteBottomBox(Box box) {
        abs_bottom.add(box);
        offset_map.put(box, getOffset());
    }


    public void doFinalAdjustments() {
        //Uu.p("Doing final adjustments: " + this);
        //Uu.p("Final height = " + getHeight());
        for (int i = 0; i < abs_bottom.size(); i++) {
            Box box = (Box) abs_bottom.get(i);
            //Uu.p("finishing up box: " + box);
            if (box.bottom_set) {
                Point off = (Point) offset_map.get(box);
                //Uu.p("offset = " + off);
                box.y = getY() + getHeight() - box.height - box.top + off.y;
            }
        }
    }

    public String toString() {
        return "BFC: (" + x + "," + y + ") - " + master + "";
    }


}
