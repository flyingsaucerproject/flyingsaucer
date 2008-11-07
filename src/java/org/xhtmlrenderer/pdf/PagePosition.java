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
package org.xhtmlrenderer.pdf;

public class PagePosition {
    private String _id;
    private int _pageNo;
    private float _x;
    private float _width;
    private float _y;
    private float _height;
    
    public int getPageNo() {
        return _pageNo;
    }
    
    public void setPageNo(int pageNo) {
        _pageNo = pageNo;
    }
    
    public float getX() {
        return _x;
    }
    
    public void setX(float x) {
        _x = x;
    }
    
    public float getWidth() {
        return _width;
    }
    
    public void setWidth(float width) {
        _width = width;
    }
    
    public float getY() {
        return _y;
    }
    
    public void setY(float y) {
        _y = y;
    }
    
    public float getHeight() {
        return _height;
    }
    
    public void setHeight(float height) {
        _height = height;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }
}
