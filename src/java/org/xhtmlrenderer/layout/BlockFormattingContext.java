package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.util.*;

public class BlockFormattingContext {
    private Box master = null;
    private int x, y = 0;
    private int width;
    public BlockFormattingContext(Box master) {
        this.master = master;
    }
    public Box getMaster() {
        return master;
    }
    public void addLeftFloat(BlockBox block) {
    }
    public void addRightFloat(BlockBox block) {
    }
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
    
    public boolean isLeftFloatPresent(LineBox line) {
        return false;
    }
    public boolean isRightFloatPresent(LineBox line) {
        return false;
    }
    public int getLeftFloatDistance(LineBox line) {
        return 0;
    }
    public int getRightFloatDistance(LineBox line) {
        return 0;
    }
    public int getBottomFloatDistance(LineBox line) {
        return 0;
    }
    
    public String toString() {
        return "BFC: ("+x+","+y+") - "+master+"";
    }
}
