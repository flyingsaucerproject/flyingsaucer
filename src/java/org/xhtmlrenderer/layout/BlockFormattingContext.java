package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.css.value.Border;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;

import java.awt.Point;
import java.util.List;

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

    /* not used public Box getMaster() {
        return master;
    }*/

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
        //return master.width - master.totalHorizontalPadding();
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
    


    /* ====== float stuff ========= */

    //private FloatManager _float_manager = new FloatManager();

    /*public FloatManager getFloatManager() {
        return this._float_manager;
    }*/

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

    /*public Point getRightAddPoint(Box block) {
        return (Point) offset_map.get(block);
    }*/

    public int getLeftFloatDistance(Box line) {
        return getFloatDistance(line, persistentBFC.left_floats);
    }

    private int getFloatDistance(Box line, List float_list) {
        return FloatManager.getFloatDistance(line, float_list, this);
    }

    /* not used public Box getLeftFloatX(Box box) {
        //Uu.p("in old bfc.getLeftFloatX( " + box + " ) ");
        // count backwards through the floats
        int x = 0;
        for (int i = left_floats.size() - 1; i >= 0; i--) {
            Box floater = (Box) left_floats.get(i);
            //Uu.p("box = " + box);
            // Uu.p("testing against float = " + floater);
            x = floater.x + floater.width;
            if (floater.y + floater.height > box.y) {
                // Uu.p("float["+i+"] blocks the box vertically");
                return floater;
            } else {
                // Uu.p("float["+i+"] doesn't block. moving to next");
            }
        }
        //Uu.p("returning null");
        return null;
    }*/

    public Box newGetLeftFloatX(Box box) {
        return FloatManager.newGetLeftFloatX(box, persistentBFC.left_floats, this);
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

    public Box getRightFloatX(Box box) {
        //Uu.p("get right float x : " + box);
        // count backwards through the floats
        int x = 0;
        for (int i = persistentBFC.right_floats.size() - 1; i >= 0; i--) {
            Box floater = (Box) persistentBFC.right_floats.get(i);
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
        return getFloatDistance(line, persistentBFC.right_floats);
    }

    /*public int getBottomFloatDistance(LineBox line) {
        return 0;
    }*/

    /* -- end flaot stuff --- */


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

    public String toString() {
        return "BFC: (" + x + "," + y + ") - " + persistentBFC.master + "";
    }
}
