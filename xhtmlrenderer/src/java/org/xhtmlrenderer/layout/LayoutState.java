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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.layout;

import java.util.LinkedList;

import org.xhtmlrenderer.render.MarkerData;

/**
 * A bean which captures all state necessary to lay out an arbitrary box.  
 * Mutable objects must be copied when provided to this class.  It is far too
 * expensive to maintain a bean of this class for each box.  
 * It is only created as needed.
 */
public class LayoutState {
    private StyleTracker _firstLines;
    private StyleTracker _firstLetters;
    
    private MarkerData _currentMarkerData;
    
    private LinkedList _BFCs;
    
    private String _pageName;
    private int _extraSpaceTop;
    private int _extraSpaceBottom;
    private int _noPageBreak;
    
    public LinkedList getBFCs() {
        return _BFCs;
    }

    public void setBFCs(LinkedList s) {
        _BFCs = s;
    }

    public MarkerData getCurrentMarkerData() {
        return _currentMarkerData;
    }

    public void setCurrentMarkerData(MarkerData currentMarkerData) {
        _currentMarkerData = currentMarkerData;
    }

    public StyleTracker getFirstLetters() {
        return _firstLetters;
    }

    public void setFirstLetters(StyleTracker firstLetters) {
        _firstLetters = firstLetters;
    }

    public StyleTracker getFirstLines() {
        return _firstLines;
    }

    public void setFirstLines(StyleTracker firstLines) {
        _firstLines = firstLines;
    }

    public String getPageName() {
        return _pageName;
    }

    public void setPageName(String pageName) {
        _pageName = pageName;
    }

    public int getExtraSpaceTop() {
        return _extraSpaceTop;
    }

    public void setExtraSpaceTop(int extraSpaceTop) {
        _extraSpaceTop = extraSpaceTop;
    }

    public int getExtraSpaceBottom() {
        return _extraSpaceBottom;
    }

    public void setExtraSpaceBottom(int extraSpaceBottom) {
        _extraSpaceBottom = extraSpaceBottom;
    }

    public int getNoPageBreak() {
        return _noPageBreak;
    }

    public void setNoPageBreak(int noPageBreak) {
        _noPageBreak = noPageBreak;
    }
}
