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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;

/**
 * A class that manages all floated boxes in a given block formatting context.
 * It is responsible for positioning floats and calculating clearance for
 * non-floated (block) boxes.
 */
public class FloatManager {
    private static final int LEFT = 1;
    private static final int RIGHT = 2;

    private List _leftFloats = new ArrayList();
    private List _rightFloats = new ArrayList();

    private Box _master;

    public void floatBox(LayoutContext c, Layer layer, BlockFormattingContext bfc, BlockBox box) {
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

    private void save(BlockBox current, Layer layer, BlockFormattingContext bfc, int direction) {
        Point p = bfc.getOffset();
        getFloats(direction).add(new BoxOffset(current, p.x, p.y));
        layer.addFloat(current, bfc);
        current.getFloatedBoxData().setManager(this);

        current.calcCanvasLocation();
        current.calcChildLocations();
    }

    private void position(CssContext cssCtx, BlockFormattingContext bfc,
                          BlockBox current, int direction) {
        moveAllTheWayOver(current, direction);

        alignToLastOpposingFloat(cssCtx, bfc, current, direction);
        alignToLastFloat(cssCtx, bfc, current, direction);

        if (!fitsInContainingBlock(current) ||
                overlaps(cssCtx, bfc, current, getFloats(direction))) {
            moveAllTheWayOver(current, direction);
            moveFloatBelow(cssCtx, bfc, current, getFloats(direction));
        }

        if (overlaps(cssCtx, bfc, current, getOpposingFloats(direction))) {
            moveAllTheWayOver(current, direction);
            moveFloatBelow(cssCtx, bfc, current, getFloats(direction));
            moveFloatBelow(cssCtx, bfc, current, getOpposingFloats(direction));
        }

        if (current.getStyle().isCleared()) {
            if (current.getStyle().isClearLeft() && direction == LEFT) {
                moveAllTheWayOver(current, LEFT);
            } else if (current.getStyle().isClearRight() && direction == RIGHT) {
                moveAllTheWayOver(current, RIGHT);
            }
            moveFloatBelow(cssCtx, bfc, current, getFloats(direction));
        }
    }

    private List getFloats(int direction) {
        return direction == LEFT ? _leftFloats : _rightFloats;
    }

    private List getOpposingFloats(int direction) {
        return direction == LEFT ? _rightFloats : _leftFloats;
    }

    private void alignToLastFloat(CssContext cssCtx,
                                  BlockFormattingContext bfc, BlockBox current, int direction) {

        List floats = getFloats(direction);
        if (floats.size() > 0) {
            Point offset = bfc.getOffset();
            BoxOffset lastOffset = (BoxOffset) floats.get(floats.size() - 1);
            BlockBox last = lastOffset.getBox();

            Rectangle currentBounds = current.getMarginEdge(cssCtx, -offset.x, -offset.y);

            Rectangle lastBounds = last.getMarginEdge(cssCtx, -lastOffset.getX(), -lastOffset.getY());

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

                current.setX(currentBounds.x);
                current.setY(currentBounds.y);
            }
        }
    }

    private void alignToLastOpposingFloat(CssContext cssCtx,
                                          BlockFormattingContext bfc, BlockBox current, int direction) {

        List floats = getOpposingFloats(direction);
        if (floats.size() > 0) {
            Point offset = bfc.getOffset();
            BoxOffset lastOffset = (BoxOffset) floats.get(floats.size() - 1);

            Rectangle currentBounds = current.getMarginEdge(cssCtx, -offset.x, -offset.y);

            Rectangle lastBounds = lastOffset.getBox().getMarginEdge(cssCtx,
                    -lastOffset.getX(), -lastOffset.getY());

            if (currentBounds.y < lastBounds.y) {
                currentBounds.translate(0, lastBounds.y - currentBounds.y);

                currentBounds.translate(offset.x, offset.y);

                current.setY(currentBounds.y);
            }
        }
    }

    private void moveAllTheWayOver(BlockBox current, int direction) {
        if (direction == LEFT) {
            current.setX(0);
        } else if (direction == RIGHT) {
            current.setX(current.getContainingBlock().getContentWidth() - current.getWidth());
        }
    }

    private boolean fitsInContainingBlock(BlockBox current) {
        return current.getX() >= 0 &&
                (current.getX() + current.getWidth()) <= current.getContainingBlock().getContentWidth();
    }

    private int findLowestY(CssContext cssCtx, List floats) {
        int result = 0;

        for (Iterator i = floats.iterator(); i.hasNext();) {
            BoxOffset floater = (BoxOffset) i.next();

            Rectangle bounds = floater.getBox().getMarginEdge(
                    cssCtx, -floater.getX(), -floater.getY());
            if (bounds.y + bounds.height > result) {
                result = bounds.y + bounds.height;
            }
        }

        return result;
    }

    public int getClearDelta(CssContext cssCtx, int bfcRelativeY) {
        int lowestLeftY = findLowestY(cssCtx, getFloats(LEFT));
        int lowestRightY = findLowestY(cssCtx, getFloats(RIGHT));

        int lowestY = Math.max(lowestLeftY, lowestRightY);

        return lowestY - bfcRelativeY;
    }

    private boolean overlaps(CssContext cssCtx, BlockFormattingContext bfc,
                             BlockBox current, List floats) {
        Point offset = bfc.getOffset();
        Rectangle bounds = current.getMarginEdge(cssCtx, -offset.x, -offset.y);

        for (Iterator i = floats.iterator(); i.hasNext();) {
            BoxOffset floater = (BoxOffset) i.next();
            Rectangle floaterBounds = floater.getBox().getMarginEdge(cssCtx,
                    -floater.getX(), -floater.getY());

            if (floaterBounds.intersects(bounds)) {
                return true;
            }
        }

        return false;
    }

    private void moveFloatBelow(CssContext cssCtx, BlockFormattingContext bfc,
                                   Box current, List floats) {
        if (floats.size() == 0) {
            return;
        }

        Point offset = bfc.getOffset();
        int boxY = current.getY() - offset.y;
        int floatY = findLowestY(cssCtx, floats);

        if (floatY - boxY > 0) {
            current.setY(current.getY() + (floatY - boxY));
        }
    }

    private void moveClear(CssContext cssCtx, BlockFormattingContext bfc,
                           Box current, List floats) {
        if (floats.size() == 0) {
            return;
        }

        // Translate from box coords to BFC coords
        Point offset = bfc.getOffset();
        Rectangle bounds = current.getBorderEdge(
                current.getX()-offset.x, current.getY()-offset.y, cssCtx);

        int y = findLowestY(cssCtx, floats);

        if (bounds.y < y) {
            // Translate bottom margin edge of lowest float back to box coords
            // and set the box's border edge to that value
            bounds.y = y;

            bounds.translate(offset.x, offset.y);

            current.setY(bounds.y - (int)current.getMargin(cssCtx).top());
        }
    }

    public void removeFloat(BlockBox floater) {
        removeFloat(floater, getFloats(LEFT));
        removeFloat(floater, getFloats(RIGHT));
    }

    private void removeFloat(BlockBox floater, List floats) {
        for (Iterator i = floats.iterator(); i.hasNext();) {
            BoxOffset boxOffset = (BoxOffset) i.next();
            if (boxOffset.getBox().equals(floater)) {
                i.remove();
                floater.getFloatedBoxData().setManager(null);
            }
        }
    }

    public void calcFloatLocations() {
        calcFloatLocations(getFloats(LEFT));
        calcFloatLocations(getFloats(RIGHT));
    }

    private void calcFloatLocations(List floats) {
        for (Iterator i = floats.iterator(); i.hasNext();) {
            BoxOffset boxOffset = (BoxOffset) i.next();
            boxOffset.getBox().calcCanvasLocation();
            boxOffset.getBox().calcChildLocations();
        }
    }

    private void applyLineHeightHack(CssContext cssCtx, Box line, Rectangle bounds) {
        // this is a hack to deal with lines w/o width or height. is this valid?
        // possibly, since the line doesn't know how long it should be until it's already
        // done float adjustments
        if (line.getHeight() == 0) {
            bounds.height = (int)line.getStyle().getLineHeight(cssCtx);
        }
    }

    public int getNextLineBoxDelta(CssContext cssCtx, BlockFormattingContext bfc,
            LineBox line, int containingBlockContentWidth) {
        BoxDistance left = getFloatDistance(cssCtx, bfc, line, containingBlockContentWidth, _leftFloats, LEFT);
        BoxDistance right = getFloatDistance(cssCtx, bfc, line, containingBlockContentWidth, _rightFloats, RIGHT);

        int leftDelta;
        int rightDelta;

        if (left.getBox() != null) {
            leftDelta = calcDelta(cssCtx, line, left);
        } else {
            leftDelta = 0;
        }

        if (right.getBox() != null) {
            rightDelta = calcDelta(cssCtx, line, right);
        } else {
            rightDelta = 0;
        }

        return Math.max(leftDelta, rightDelta);
    }

    private int calcDelta(CssContext cssCtx, LineBox line, BoxDistance boxDistance) {
        BlockBox floated = boxDistance.getBox();
        Rectangle rect = floated.getBorderEdge(floated.getAbsX(), floated.getAbsY(), cssCtx);
        int bottom = rect.y + rect.height;
        return bottom - line.getAbsY();
    }

    public int getLeftFloatDistance(CssContext cssCtx, BlockFormattingContext bfc,
            LineBox line, int containingBlockContentWidth) {
        return getFloatDistance(cssCtx, bfc, line, containingBlockContentWidth, _leftFloats, LEFT).getDistance();
    }

    public int getRightFloatDistance(CssContext cssCtx, BlockFormattingContext bfc,
            LineBox line, int containingBlockContentWidth) {
        return getFloatDistance(cssCtx, bfc, line, containingBlockContentWidth, _rightFloats, RIGHT).getDistance();
    }

    private BoxDistance getFloatDistance(CssContext cssCtx, BlockFormattingContext bfc,
                                 LineBox line, int containingBlockContentWidth,
                                 List floatsList, int direction) {
        if (floatsList.size() == 0) {
            return new BoxDistance(null, 0);
        }

        Point offset = bfc.getOffset();
        Rectangle lineBounds = line.getMarginEdge(cssCtx, -offset.x, -offset.y);
        lineBounds.width = containingBlockContentWidth;

        int farthestOver = direction == LEFT ? lineBounds.x : lineBounds.x + lineBounds.width;

        applyLineHeightHack(cssCtx, line, lineBounds);
        BlockBox farthestOverBox = null;
        for (int i = 0; i < floatsList.size(); i++) {
            BoxOffset floater = (BoxOffset) floatsList.get(i);
            Rectangle fr = floater.getBox().getMarginEdge(cssCtx, -floater.getX(), -floater.getY());
            if (lineBounds.intersects(fr)) {
                if (direction == LEFT && fr.x + fr.width > farthestOver) {
                    farthestOver = fr.x + fr.width;
                } else if (direction == RIGHT && fr.x < farthestOver) {
                    farthestOver = fr.x;
                }
                farthestOverBox = floater.getBox();
            }
        }

        if (direction == LEFT) {
            return new BoxDistance(farthestOverBox, farthestOver - lineBounds.x);
        } else {
            return new BoxDistance(farthestOverBox,lineBounds.x + lineBounds.width - farthestOver);
        }
    }

    public void setMaster(Box owner) {
        _master = owner;
    }

    public Box getMaster() {
        return _master;
    }

    public Point getOffset(BlockBox floater) {
        // FIXME inefficient (but probably doesn't matter)
        return getOffset(floater,
                floater.getStyle().isFloatedLeft() ? getFloats(LEFT) : getFloats(RIGHT));
    }

    private Point getOffset(BlockBox floater, List floats) {
        for (Iterator i = floats.iterator(); i.hasNext();) {
            BoxOffset boxOffset = (BoxOffset) i.next();
            BlockBox box = boxOffset.getBox();

            if (box.equals(floater)) {
                return new Point(boxOffset.getX(), boxOffset.getY());
            }
        }

        return null;
    }

    private void performFloatOperation(FloatOperation op, List floats) {
        for (Iterator i = floats.iterator(); i.hasNext();) {
            BoxOffset boxOffset = (BoxOffset) i.next();
            BlockBox box = boxOffset.getBox();

            box.setAbsX(box.getX() + getMaster().getAbsX() - boxOffset.getX());
            box.setAbsY(box.getY() + getMaster().getAbsY() - boxOffset.getY());

            op.operate(box);
        }
    }

    public void performFloatOperation(FloatOperation op) {
        performFloatOperation(op, getFloats(LEFT));
        performFloatOperation(op, getFloats(RIGHT));
    }

    private static class BoxOffset {
        private BlockBox _box;
        private int _x;
        private int _y;

        public BoxOffset(BlockBox box, int x, int y) {
            _box = box;
            _x = x;
            _y = y;
        }

        public BlockBox getBox() {
            return _box;
        }

        public int getX() {
            return _x;
        }

        public int getY() {
            return _y;
        }
    }

    private static class BoxDistance {
        private BlockBox _box;
        private int _distance;

        public BoxDistance(BlockBox box, int distance) {
            _box = box;
            _distance = distance;
        }

        BlockBox getBox() {
            return _box;
        }

        int getDistance() {
            return _distance;
        }
    }

    public interface FloatOperation {
        public void operate(Box floater);
    }
}

/*
 * $Id$
 */