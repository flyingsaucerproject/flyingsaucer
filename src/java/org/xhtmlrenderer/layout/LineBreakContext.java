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

public class LineBreakContext {
    private String master;
    private int start;
    private int end;
    private boolean unbreakable;;
    private boolean needsNewLine;
    
    private int width;
    
    public int getLast() {
    	return master.length();
    }
    
    public void reset() {
        this.width = 0;
        this.unbreakable = false;
        this.needsNewLine = false;
    }
    
    public int getEnd() {
        return end;
    }
    
    public void setEnd(int end) {
        this.end = end;
    }
    
    public String getMaster() {
        return master;
    }
    
    public void setMaster(String master) {
        this.master = master;
    }
    
    public int getStart() {
        return start;
    }
    
    public void setStart(int start) {
        this.start = start;
    }
    
    public String getStartSubstring() {
        return master.substring(start);
    }
    
    public String getCalculatedSubstring() {
        return master.substring(start, end);
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public boolean isNeedsNewLine() {
        return needsNewLine;
    }

    public void setNeedsNewLine(boolean needsLineBreak) {
        this.needsNewLine = needsLineBreak;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
    
    public boolean isFinished() {
        return this.end == getMaster().length();
    }
}
