/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package org.xhtmlrenderer.render;

public class BoxDimensions {
    private int _leftMBP;
    private int _rightMBP;
    private int _contentWidth;
    private int _height;
    
    public BoxDimensions() {
    }
    
    public int getContentWidth() {
        return _contentWidth;
    }
    
    public void setContentWidth(int contentWidth) {
        _contentWidth = contentWidth;
    }
    
    public int getHeight() {
        return _height;
    }
    
    public void setHeight(int height) {
        _height = height;
    }
    
    public int getLeftMBP() {
        return _leftMBP;
    }
    
    public void setLeftMBP(int leftMBP) {
        _leftMBP = leftMBP;
    }
    
    public int getRightMBP() {
        return _rightMBP;
    }
    
    public void setRightMBP(int rightMBP) {
        _rightMBP = rightMBP;
    }
}
