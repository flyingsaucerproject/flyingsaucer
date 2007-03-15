/*
 * {{{ header & license
 * Copyright (c) 2005 Joshua Marinacci
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
package org.xhtmlrenderer.render;

import org.xhtmlrenderer.layout.FunctionData;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.WhitespaceStripper;
import org.xhtmlrenderer.util.Uu;

/**
 * A lightweight object which contains a chunk of text from an inline element.  
 * It will never extend across a line break nor will it extend across an element 
 * nested within its inline element.
 */
public class InlineText {
    private InlineLayoutBox _parent;
    
    private int _x;
    
    private String _masterText;
    private int _start;
    private int _end;
    
    private int _width;
    
    private FunctionData _functionData;
    
    private boolean _containedLF = false;
    
    public void trimTrailingSpace(LayoutContext c) {
        if (! isEmpty() && _masterText.charAt(_end-1) == ' ') {
            _end--;
            setWidth(c.getTextRenderer().getWidth(c.getFontContext(), 
                    getParent().getStyle().getFSFont(c),
                    getSubstring()));
        } 
    }
    
    public boolean isEmpty() {
        return _start == _end && ! _containedLF;
    }
    
    public String getSubstring() {
        if (getMasterText() != null) {
            if (_start == -1 || _end == -1) {
                throw new RuntimeException("negative index in InlineBox");
            }
            if (_end < _start) {
                throw new RuntimeException("end is less than setStartStyle");
            }
            return getMasterText().substring(_start, _end);
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
        _start = start;
        _end = end;
        
        if (_end > 0 && _masterText.charAt(_end-1) == WhitespaceStripper.EOLC) {
            _containedLF = true;
            _end--;
        }
    }

    public String getMasterText() {
        return _masterText;
    }

    public void setMasterText(String masterText) {
        _masterText = masterText;
    }

    public int getX() {
        return _x;
    }

    public void setX(int x) {
        _x = x;
    }

    public int getWidth() {
        return _width;
    }

    public void setWidth(int width) {
        _width = width;
    }
    
    public void paint(RenderingContext c) {
        c.getOutputDevice().drawText(c, this);
    }

    public InlineLayoutBox getParent() {
        return _parent;
    }

    public void setParent(InlineLayoutBox parent) {
        _parent = parent;
    }

    public boolean isDynamicFunction() {
        return _functionData != null;
    }

    public FunctionData getFunctionData() {
        return _functionData;
    }

    public void setFunctionData(FunctionData functionData) {
        _functionData = functionData;
    }
    
    public void updateDynamicValue(RenderingContext c) {
        String value = _functionData.getContentFunction().calculate(
                c, _functionData.getFunction(), this);
        _start = 0;
        _end = value.length();
        _masterText = value;
        _width = c.getTextRenderer().getWidth(
                c.getFontContext(), getParent().getStyle().getFSFont(c),
                value);
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("InlineText: ");
        if (_containedLF || isDynamicFunction()) {
            result.append("(");
            if (_containedLF) {
                result.append('L');
            }
            if (isDynamicFunction()) {
                result.append('F');
            }
            result.append(") ");
        }
        result.append('(');
        result.append(getSubstring());
        result.append(')');
        
        return result.toString();
    }
}
