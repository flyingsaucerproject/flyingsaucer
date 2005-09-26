package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.value.Border;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockFormattingContext {
    private Box master = null;
    protected int x, y = 0;
    private int width;
    private List left_floats, right_floats;
    private Map offset_map;
    private List abs_bottom;
    private Border insets;
    private Border padding;

    public BlockFormattingContext(Box master, Context c) {
        int parent_width = (int) c.getExtents().getWidth();
        CalculatedStyle style = c.getCurrentStyle();
        Border border = style.getBorderWidth(c.getCtx());
        //note: percentages here refer to width of containing block
        Border margin = master.getMarginWidth(c, parent_width);
        padding = style.getPaddingWidth(parent_width, parent_width, c.getCtx());
        insets = new Border(margin.top + border.top + padding.top,
                padding.right + border.right + margin.right,
                padding.bottom + border.bottom + margin.bottom,
                margin.left + border.left + padding.left);
        this.master = master;
        master.setBlockFormattingContext(this);
        left_floats = new ArrayList();
        right_floats = new ArrayList();
        offset_map = new HashMap();
        abs_bottom = new ArrayList();
    }

    public Box getMaster() {
        return master;
    }

    public Border getInsets() {
        return insets;
    }

    public Border getPadding() {
        return padding;
    }

    /* ====== positioning stuff ======== */

    public int getX() {
        return master.x + x;
    }

    public int getY() {
        return master.y + y;
    }

    public Point getOffset(Box box) {
        return (Point) offset_map.get(box);
    }

    public Point getOffset() {
        //return new Point(x, y);
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
        //Uu.p("trans : " + x + " " + y);
        this.x -= x;
        this.y -= y;
    }
    


    /* ====== float stuff ========= */

    private FloatManager _float_manager = new FloatManager();

    public FloatManager getFloatManager() {
        return this._float_manager;
    }

    public void addLeftFloat(Box block) {
        //Uu.p("adding a left float: " + block);
        left_floats.add(block);
        offset_map.put(block, getOffset());
    }

    public void addRightFloat(Box block) {
        //Uu.p("adding a right float: " + block);
        right_floats.add(block);
        offset_map.put(block, getOffset());
    }

    public Point getRightAddPoint(Box block) {
        return (Point) offset_map.get(block);
    }

    public int getLeftFloatDistance(Box line) {
        return getFloatDistance(line, left_floats);
    }

    private int getFloatDistance(Box line, List float_list) {
        return this._float_manager.getFloatDistance(line, float_list, this);
    }

    public Box getLeftFloatX(Box box) {
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
    }

    public Box newGetLeftFloatX(Box box) {
        return FloatManager.newGetLeftFloatX(box, left_floats, this);
    }

    public Box pushDownLeft(Box box) {
        return pushDownLeftRight(box, left_floats);
    }

    private Box pushDownLeftRight(Box box, List floatList) {
        // make sure the box doesn't overlap any floats
        // Uu.p("pushing down box: " + box);
        for (int i = 0; i < floatList.size(); i++) {
            Box floater = (Box) floatList.get(i);
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
        //Uu.p("push Down Right : " + box);
        return pushDownLeftRight(box, right_floats);
    }

    public Box getRightFloatX(Box box) {
        //Uu.p("get right float x : " + box);
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

    public int getLeftDownDistance(Box box) {
        return getDownDistance(box, left_floats);
    }

    public int getRightDownDistance(Box box) {
        return getDownDistance(box, right_floats);
    }

    private int getDownDistance(Box box, List list) {
        for (int i = 0; i < list.size(); i++) {
            Box floater = (Box) list.get(i);
            Point fpt = (Point) offset_map.get(floater);
            if (floater.y + floater.height - fpt.y > box.y) {
                return floater.y + floater.height - fpt.y;
            }
        }
        return 0;
    }

    public int getRightFloatDistance(LineBox line) {
        ///Uu.p("get right float distance: " + line);
        return getFloatDistance(line, right_floats);
    }

    public int getBottomFloatDistance(LineBox line) {
        return 0;
    }

    /* -- end flaot stuff --- */


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
