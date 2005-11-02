package org.xhtmlrenderer.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.css.value.Border;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.XRRuntimeException;

//TODO: refactor so that BFC utilizes master's contentWidth, etc.

public class BlockFormattingContext {
    protected int x, y = 0;
    private final PersistentBFC persistentBFC;

    public BlockFormattingContext(PersistentBFC p) {
        persistentBFC = p;
    }

    public BlockFormattingContext(Box block, LayoutContext c) {
        persistentBFC = new PersistentBFC(block, c);
    }

    public Border getInsets() {
        return persistentBFC.insets;
    }

    public RectPropertySet getPadding() {
        return persistentBFC.padding;
    }

    /* ====== positioning stuff ======== */

    public int getX() {
        return persistentBFC.master.x + x;
    }

    public int getY() {
        return persistentBFC.master.y + y;
    }

    public Point getOffset(Box box) {
        return (Point) persistentBFC.offset_map.get(box);
    }

    public Point getOffset() {
        //return new Point(x, y);
        return new Point(x, y);
    }

    public void setWidth(int width) {
        persistentBFC.width = width;
    }

    public int getWidth() {
        return persistentBFC.width;
    }

    public int getHeight() {
        return persistentBFC.master.height;
    }

    // we want to preserve the block formatting contexts position
    // relative to the current block, so we do a reverse translate
    // of the graphics
    public void translate(int x, int y) {
        //Uu.p("trans : " + x + " " + y);
        this.x -= x;
        this.y -= y;
    }

    public void addLeftFloat(Box block) {
        //Uu.p("adding a left float: " + block);
        persistentBFC.left_floats.add(block);
        persistentBFC.offset_map.put(block, getOffset());
    }

    public void addRightFloat(Box block) {
        //Uu.p("adding a right float: " + block);
        persistentBFC.right_floats.add(block);
        persistentBFC.offset_map.put(block, getOffset());
    }

    public int getLeftFloatDistance(Box line) {
        return getFloatDistance(line, persistentBFC.left_floats, LEFT);
    }

    public Box getLeftFloatX(LayoutContext c, Box box) {
        return findOverlappingFloat(c, box, persistentBFC.left_floats);
    }

    public Box pushDownLeft(Box box) {
        return pushDownLeftRight(box, persistentBFC.left_floats);
    }

    private Box pushDownLeftRight(Box box, List floatList) {
        // make sure the box doesn't overlap any floats
        // Uu.p("pushing down box: " + box);
        for (int i = 0; i < floatList.size(); i++) {
            Box floater = (Box) floatList.get(i);
            // Uu.p("testing against: " + floater);
            if (floater.y >= box.y && (box.x >= floater.x &&
                    box.x < floater.x + floater.getWidth())) {
                // Uu.p("pushing down " + box);
                box.y = floater.y + floater.height;
            }
        }
        return box;
    }

    public Box pushDownRight(Box box) {
        //Uu.p("push Down Right : " + box);
        return pushDownLeftRight(box, persistentBFC.right_floats);
    }

    public Box getRightFloatX(LayoutContext c, Box box) {
        return findOverlappingFloat(c, box, persistentBFC.right_floats);
    }

    public int getLeftDownDistance(Box box) {
        return getDownDistance(box, persistentBFC.left_floats);
    }

    public int getRightDownDistance(Box box) {
        return getDownDistance(box, persistentBFC.right_floats);
    }

    private int getDownDistance(Box box, List list) {
        for (int i = 0; i < list.size(); i++) {
            Box floater = (Box) list.get(i);
            Point fpt = (Point) persistentBFC.offset_map.get(floater);
            if (floater.y + floater.height - fpt.y > box.y) {
                return floater.y + floater.height - fpt.y;
            }
        }
        return 0;
    }

    public int getRightFloatDistance(LineBox line) {
        ///Uu.p("get right float distance: " + line);
        return getFloatDistance(line, persistentBFC.right_floats, RIGHT);
    }

    public void addAbsoluteBottomBox(Box box) {
        persistentBFC.abs_bottom.add(box);
        persistentBFC.offset_map.put(box, getOffset());
    }

    public void doFinalAdjustments() {
        //Uu.p("Doing final adjustments: " + this);
        //Uu.p("Final height = " + getHeight());
        for (int i = 0; i < persistentBFC.abs_bottom.size(); i++) {
            Box box = (Box) persistentBFC.abs_bottom.get(i);
            //Uu.p("finishing up box: " + box);
            if (box.bottom_set) {
                Point off = (Point) persistentBFC.offset_map.get(box);
                //Uu.p("offset = " + off);
                box.y = getY() + getHeight() - box.height - box.top + off.y;
            }
        }
    }
    
    public void removeFloat(Box box) {
        persistentBFC.removeFloat(box);
    }
    
    public float getClearDelta(LayoutContext c, Box box, Box floater) {
        Rectangle fr = new Rectangle(floater.x, floater.y, floater.getWidth(), floater.height);
        Point floatOffset = getOffset(floater);
        fr.translate(-floatOffset.x, -floatOffset.y);
        
        Rectangle br = new Rectangle(box.x, box.y, box.getWidth(), box.height);
        Point boxOffset = getOffset();
        br.translate(-boxOffset.x, -boxOffset.y);
        
        return fr.y + fr.height - br.y;
    }
    
    /* The purpose of get float distance is to figure out how far to the left or
    right (the x offset) you will need to position the target box so that it
    will not overlap with any floats currently in the BFC. This is called from
    the BFC via the getleftdistance and getrightdistance methods.


        notes: first of all, are we only dealing with lines or other boxes.
        we are making the assumption that the box has valid dimensions. it looks like
        line boxes are being passed in when they don't have a height yet. the default height
        should be the line-height, i'm guessing, and then we can use that for the
        determination.
    */
    
    private static final int LEFT = 1;
    private static final int RIGHT = 2;

    private int getFloatDistance(Box line, List floatsList, int direction) {
        if (floatsList.size() == 0) {
            return 0;
        }

        int xoff = 0;
        // create a rectangle for the line we are attempting to adjust
        Rectangle lr = new Rectangle(line.x, line.y, line.contentWidth, line.height);

        // this is a hack to deal with lines w/o width or height. is this valid?
        // possibly, since the line doesn't know how long it should be until it's already
        // done float adjustments
        if (line.contentWidth == 0) {
            lr.width = 10;
        }
        if (line.height == 0) {
            lr.height = 10;
        }
        Point lpt = new Point(this.x, this.y);
        
        // convert to abs coords
        lr.translate(-lpt.x, -lpt.y);
        for (int i = 0; i < floatsList.size(); i++) {
            // get the current float
            Box floater = (Box) floatsList.get(i);
            // create a rect from the box
            Rectangle fr = new Rectangle(floater.x, floater.y, floater.getWidth(), floater.height);
            // get the point where the float was added
            Point fpt = getOffset(floater);
            // convert to abs coords
            fr.translate(-fpt.x, -fpt.y);
            // if the line is lower than bottom of the floater
            // josh: is this calc right? shouldn't floater.y be in there somewhere?
            if (lr.intersects(fr)) {
                //Uu.p("it intersects!");
                lr.translate(direction == LEFT ? fr.width : -fr.width, 0);
                xoff += fr.width;
                //Uu.p("new lr = " + lr);
            }
        }

        return xoff;
    }
    

    /*
        This method is called (currently) by the float util to set up the
        float itself. it should return the right most box on the left
    */
    private Box findOverlappingFloat(LayoutContext c, Box box, List floats) {
        Rectangle br = new Rectangle(box.x, box.y, box.getWidth(), box.height);
        Point offset = c.getBlockFormattingContext().getOffset();
        br.translate(-offset.x, -offset.y);
        for (int i = floats.size() - 1; i >= 0; i--) {
            Box floater = (Box) floats.get(i);

            Rectangle fr = new Rectangle(floater.x, floater.y, floater.getWidth(), floater.height);
            Point fpt = this.getOffset(floater);
            fr.translate(-fpt.x, -fpt.y);

            // skip if the box and the floater have the same element. this means they are really the same box
            // this is a hack to account for when the same float is run through twice. i don't know
            // why this is happening. hopefully we can fix it in the future.
            if (floater.element == box.element) {
                throw new XRRuntimeException("internal error");
            }
            if (br.intersects(fr)) {
                return floater;
            } 
        }
        return null;
    }    

    public String toString() {
        return "BFC: (" + x + "," + y + ") - " + persistentBFC.master + "";
    }

    public Box getMaster() {
        return persistentBFC.master;
    }
}
