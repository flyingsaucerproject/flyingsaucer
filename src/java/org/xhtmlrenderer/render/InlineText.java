/*
 * {{{ header & license
 * Copyright (c) 2005 Joshua Marinacci, Wisconsin Court System
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
package org.xhtmlrenderer.render;

import org.xhtmlrenderer.util.Uu;

public class InlineText {
    private InlineBox parent;
    
    private int x;
    
    private String masterText;
    private int start;
    private int end;
    
    private int width;
    
    public boolean isEmpty() {
        return start == end;
    }
    
    public String getSubstring() {
        if (getMasterText() != null) {
            if (start == -1 || end == -1) {
                throw new RuntimeException("negative index in InlineBox");
            }
            if (end < start) {
                throw new RuntimeException("end is less than setStartStyle");
            }
            return getMasterText().substring(start, end);
        } else {
            throw new RuntimeException("No master text set!");
        }
    }
    
    public void setSubstring(int start, int end) {
        if (end < start) {
            Uu.p("setting substring to: " + start + " " + end);
            throw new RuntimeException("set substring length too long: " + this);
        } else if (end < 0 || start < 0) {
            throw new RuntimeException("Trying to set negative index to inline box");
        }
        this.start = start;
        this.end = end;
    }

    public String getMasterText() {
        return masterText;
    }

    public void setMasterText(String masterText) {
        this.masterText = masterText;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
    
    public void paint(RenderingContext c) {
        c.getOutputDevice().drawText(c, this);
    }

    public InlineBox getParent() {
        return parent;
    }

    public void setParent(InlineBox parent) {
        this.parent = parent;
    }    
}
