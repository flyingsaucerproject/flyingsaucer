package org.xhtmlrenderer.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.css.value.Border;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FloatedBlockBox;
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
    
    public FloatManager getFloatManager() {
        return persistentBFC.getFloatManager();
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
    
    public int getLeftFloatDistance(CssContext cssCtx, LineBox line) {
        return getFloatManager().getLeftFloatDistance(cssCtx, this, line);
    }
    
    public int getRightFloatDistance(CssContext cssCtx, LineBox line) {
        return getFloatManager().getRightFloatDistance(cssCtx, this, line);
    }
    
    public void floatBox(LayoutContext c, FloatedBlockBox floated) {
        getFloatManager().floatBox(c, this, floated);
    }
    
    public void floatPending(LayoutContext c, List pending) {
        getFloatManager().floatPending(c, this, pending);
    }
    
    public void clear(LayoutContext c, Box current) {
        getFloatManager().clear(c, this, current);
    }
    
    public String toString() {
        return "BFC: (" + x + "," + y + ") - " + persistentBFC.master + "";
    }

    public Box getMaster() {
        return persistentBFC.master;
    }
}
