/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FloatedBlockBox;
import org.xhtmlrenderer.render.LineBox;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FloatManager {
    private static final int LEFT = 1;
    private static final int RIGHT = 2;

    private List leftFloats = new ArrayList();
    private List rightFloats = new ArrayList();

    private Box master;

    public void floatBox(LayoutContext c, Layer layer, BlockFormattingContext bfc, FloatedBlockBox box) {
        box.y = (int) c.getFloatingY();
        if (box.getStyle().isFloatedLeft()) {
            position(c, bfc, box, LEFT);
            save(box, layer, bfc, LEFT);
        } else if (box.getStyle().isFloatedRight()) {
            position(c, bfc, box, RIGHT);
            save(box, layer, bfc, RIGHT);
        }
    }

    public void clear(CssContext cssCtx, BlockFormattingContext bfc, Box box) {
        if (box.getStyle().isClearLeft()) {
            moveClear(cssCtx, bfc, box, getFloats(LEFT));
        }
        if (box.getStyle().isClearRight()) {
            moveClear(cssCtx, bfc, box, getFloats(RIGHT));
        }
    }

    public void floatPending(LayoutContext c, Layer layer, BlockFormattingContext bfc, List pendingFloats) {
        if (pendingFloats.size() > 0) {
            for (int i = 0; i < pendingFloats.size(); i++) {
                if (i != 0) {
                    c.setFloatingY(0d);
                }
                FloatedBlockBox block = (FloatedBlockBox) pendingFloats.get(i);
                floatBox(c, layer, bfc, block);
                block.setPending(false);
            }
            pendingFloats.clear();
        }
    }

    private void save(FloatedBlockBox current, Layer layer, BlockFormattingContext bfc, int direction) {
        Point p = bfc.getOffset();
        getFloats(direction).add(new BoxOffset(current, p.x, p.y));
        layer.addFloat(current);
    }

    private void position(CssContext cssCtx, BlockFormattingContext bfc,
                          FloatedBlockBox current, int direction) {
        moveAllTheWayOver(current, direction);

        alignToLastOpposingFloat(cssCtx, bfc, current, direction);
        alignToLastFloat(cssCtx, bfc, current, direction);

        if (!fitsInContainingBlock(current) ||
                overlaps(cssCtx, bfc, current, getFloats(direction))) {
            moveAllTheWayOver(current, direction);
            moveClear(cssCtx, bfc, current, getFloats(direction));
        }

        if (overlaps(cssCtx, bfc, current, getOpposingFloats(direction))) {
            moveAllTheWayOver(current, direction);
            moveClear(cssCtx, bfc, current, getFloats(direction));
            moveClear(cssCtx, bfc, current, getOpposingFloats(direction));
        }

        if (current.getStyle().isCleared()) {
            if (current.getStyle().isClearLeft() && direction == LEFT) {
                moveAllTheWayOver(current, LEFT);
            } else if (current.getStyle().isClearRight() && direction == RIGHT) {
                moveAllTheWayOver(current, RIGHT);
            }
            clear(cssCtx, bfc, current);
        }
    }

    private List getFloats(int direction) {
        return direction == LEFT ? leftFloats : rightFloats;
    }

    private List getOpposingFloats(int direction) {
        return direction == LEFT ? rightFloats : leftFloats;
    }

    private void alignToLastFloat(CssContext cssCtx,
                                  BlockFormattingContext bfc, FloatedBlockBox current, int direction) {

        List floats = getFloats(direction);
        if (floats.size() > 0) {
            Point offset = bfc.getOffset();
            BoxOffset lastOffset = (BoxOffset) floats.get(floats.size() - 1);
            FloatedBlockBox last = (FloatedBlockBox) lastOffset.getBox();

            Rectangle currentBounds = current.getBounds(cssCtx, -offset.x, -offset.y);

            Rectangle lastBounds = last.getBounds(cssCtx, -lastOffset.getX(), -lastOffset.getY());

            boolean moveOver = false;

            if (currentBounds.y < lastBounds.y) {
                currentBounds.translate(0, lastBounds.y - currentBounds.y);
                moveOver = true;
            }

            if (currentBounds.y >= lastBounds.y && currentBounds.y < lastBounds.y + lastBounds.height) {
                moveOver = true;
            }

            if (moveOver) {
                if (direction == LEFT) {
                    currentBounds.x = lastBounds.x + last.getWidth();
                } else if (direction == RIGHT) {
                    currentBounds.x = lastBounds.x - current.getWidth();
                }

                currentBounds.translate(offset.x, offset.y);

                current.x = currentBounds.x;
                current.y = currentBounds.y;
            }
        }
    }

    private void alignToLastOpposingFloat(CssContext cssCtx,
                                          BlockFormattingContext bfc, FloatedBlockBox current, int direction) {

        List floats = getOpposingFloats(direction);
        if (floats.size() > 0) {
            Point offset = bfc.getOffset();
            BoxOffset lastOffset = (BoxOffset) floats.get(floats.size() - 1);

            Rectangle currentBounds = current.getBounds(cssCtx, -offset.x, -offset.y);

            Rectangle lastBounds = lastOffset.getBox().getBounds(cssCtx,
                    -lastOffset.getX(), -lastOffset.getY());

            if (currentBounds.y < lastBounds.y) {
                currentBounds.translate(0, lastBounds.y - currentBounds.y);

                currentBounds.translate(offset.x, offset.y);

                current.y = currentBounds.y;
            }
        }
    }

    private void moveAllTheWayOver(FloatedBlockBox current, int direction) {
        if (direction == LEFT) {
            current.x = 0;
        } else if (direction == RIGHT) {
            current.x = current.getContainingBlock().getContentWidth() - current.getWidth();
        }
    }

    private boolean fitsInContainingBlock(FloatedBlockBox current) {
        return current.x >= 0 &&
                (current.x + current.getWidth()) <= current.getContainingBlock().getContentWidth();
    }

    private int findLowestAbsoluteY(CssContext cssCtx, List floats) {
        int result = 0;

        for (Iterator i = floats.iterator(); i.hasNext();) {
            BoxOffset floater = (BoxOffset) i.next();

            Rectangle bounds = floater.getBox().getBounds(cssCtx,
                    -floater.getX(), -floater.getY());
            if (bounds.y + bounds.height > result) {
                result = bounds.y + bounds.height;
            }
        }

        return result;
    }

    private boolean overlaps(CssContext cssCtx, BlockFormattingContext bfc,
                             FloatedBlockBox current, List floats) {
        Point offset = bfc.getOffset();
        Rectangle bounds = current.getBounds(cssCtx, -offset.x, -offset.y);

        for (Iterator i = floats.iterator(); i.hasNext();) {
            BoxOffset floater = (BoxOffset) i.next();
            Rectangle floaterBounds = floater.getBox().getBounds(cssCtx,
                    -floater.getX(), -floater.getY());

            if (floaterBounds.intersects(bounds)) {
                return true;
            }
        }

        return false;
    }

    private void moveClear(CssContext cssCtx, BlockFormattingContext bfc,
                           Box current, List floats) {
        Point offset = bfc.getOffset();
        Rectangle bounds = current.getBounds(cssCtx, -offset.x, -offset.y);

        int y = findLowestAbsoluteY(cssCtx, floats);

        if (bounds.y < y) {
            bounds.y = y;

            bounds.translate(offset.x, offset.y);

            current.y = bounds.y;
        }
    }

    public void removeFloat(FloatedBlockBox floater) {
        for (Iterator i = leftFloats.iterator(); i.hasNext();) {
            BoxOffset boxOffset = (BoxOffset) i.next();
            if (boxOffset.getBox().equals(floater)) {
                i.remove();
            }
        }

        for (Iterator i = rightFloats.iterator(); i.hasNext();) {
            BoxOffset boxOffset = (BoxOffset) i.next();
            if (boxOffset.getBox().equals(floater)) {
                i.remove();
            }
        }
    }

    private void applyLineHeightHack(CssContext cssCtx, Box line, Rectangle bounds) {
        // this is a hack to deal with lines w/o width or height. is this valid?
        // possibly, since the line doesn't know how long it should be until it's already
        // done float adjustments
        if (line.height == 0) {
            bounds.height = (int)line.getStyle().getCalculatedStyle().getLineHeight(cssCtx);
        }
    }

    public int getLeftFloatDistance(CssContext cssCtx, BlockFormattingContext bfc, 
            LineBox line, int containingBlockContentWidth) {
        return getFloatDistance(cssCtx, bfc, line, containingBlockContentWidth, leftFloats, LEFT);
    }

    public int getRightFloatDistance(CssContext cssCtx, BlockFormattingContext bfc, 
            LineBox line, int containingBlockContentWidth) {
        return getFloatDistance(cssCtx, bfc, line, containingBlockContentWidth, rightFloats, RIGHT);
    }

    private int getFloatDistance(CssContext cssCtx, BlockFormattingContext bfc,
                                 LineBox line, int containingBlockContentWidth,
                                 List floatsList, int direction) {
        if (floatsList.size() == 0) {
            return 0;
        }

        Point offset = bfc.getOffset();
        Rectangle lineBounds = line.getBounds(cssCtx, -offset.x, -offset.y);
        lineBounds.width = containingBlockContentWidth;
        
        int farthestOver = direction == LEFT ? lineBounds.x : lineBounds.x + lineBounds.width;

        applyLineHeightHack(cssCtx, line, lineBounds);

        for (int i = 0; i < floatsList.size(); i++) {
            BoxOffset floater = (BoxOffset) floatsList.get(i);
            Rectangle fr = floater.getBox().getBounds(cssCtx, -floater.getX(), -floater.getY());
            if (lineBounds.intersects(fr)) {
                if (direction == LEFT && fr.x + fr.width > farthestOver) {
                    farthestOver = fr.x + fr.width;
                } else if (direction == RIGHT && fr.x < farthestOver) {
                    farthestOver = fr.x;
                }
            }
        }
        
        if (direction == LEFT) {
            return farthestOver - lineBounds.x;
        } else {
            return lineBounds.x + lineBounds.width - farthestOver;
        }
    }

    public void setMaster(Box owner) {
        this.master = owner;
    }

    public Box getMaster() {
        return master;
    }

    private void update(FloatUpdater updater, List floats) {
        for (Iterator i = floats.iterator(); i.hasNext();) {
            BoxOffset boxOffset = (BoxOffset) i.next();
            Box box = boxOffset.getBox();

            box.setAbsX(box.x + getMaster().getAbsX() - boxOffset.getX());
            box.setAbsY(box.y + getMaster().getAbsY() - boxOffset.getY());
            
            updater.update(box);

            box.getPersistentBFC().getFloatManager().updateAbsoluteLocations(updater);
        }
    }

    public void updateAbsoluteLocations(FloatUpdater updater) {
        update(updater, leftFloats);
        update(updater, rightFloats);
    }

    private static class BoxOffset {
        private Box box;
        private int x;
        private int y;

        public BoxOffset(Box box, int x, int y) {
            this.box = box;
            this.x = x;
            this.y = y;
        }

        public Box getBox() {
            return box;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
    
    public interface FloatUpdater {
        public void update(Box floater);
    }
}

/*
 * $Id$
 */