/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjoern Gannholm 
 * Copyright (c) 2005 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
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
