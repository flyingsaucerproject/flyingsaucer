package org.xhtmlrenderer.layout;

import java.awt.Point;
import java.util.List;

import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FloatedBlockBox;
import org.xhtmlrenderer.render.LineBox;

public class BlockFormattingContext {
    protected int x, y = 0;
    private final PersistentBFC persistentBFC;

    public BlockFormattingContext(PersistentBFC p) {
        persistentBFC = p;
    }

    public BlockFormattingContext(Box block, LayoutContext c) {
        persistentBFC = new PersistentBFC(block, c);
    }

    /* ====== positioning stuff ======== */

    public Point getOffset() {
        //return new Point(x, y);
        return new Point(x, y);
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
    
    public int getLeftFloatDistance(CssContext cssCtx, LineBox line, int containingBlockWidth) {
        return getFloatManager().getLeftFloatDistance(cssCtx, this, line, containingBlockWidth);
    }
    
    public int getRightFloatDistance(CssContext cssCtx, LineBox line, int containingBlockWidth) {
        return getFloatManager().getRightFloatDistance(cssCtx, this, line, containingBlockWidth);
    }
    
    public int getFloatDistance(CssContext cssCtx, LineBox line, int containingBlockWidth) {
        return getLeftFloatDistance(cssCtx, line, containingBlockWidth) +
                    getRightFloatDistance(cssCtx, line, containingBlockWidth);
    }
    
    public void floatBox(LayoutContext c, FloatedBlockBox floated) {
        getFloatManager().floatBox(c, c.getLayer(), this, floated);
    }
    
    public void floatPending(LayoutContext c, List pending) {
        getFloatManager().floatPending(c, c.getLayer(), this, pending);
    }
    
    public void clear(LayoutContext c, Box current) {
        getFloatManager().clear(c, this, current);
    }
    
    public String toString() {
        return "BFC: (" + x + "," + y + ")";
    }
}
