package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.render.*;

public class BlockFormattingContext {
    
    public BlockFormattingContext() {
    }
    public void addLeftFloat(BlockBox block) {
    }
    public void addRightFloat(BlockBox block) {
    }
    public void translate(int x, int y) {
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
}
