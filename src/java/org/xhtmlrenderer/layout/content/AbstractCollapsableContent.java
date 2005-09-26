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
package org.xhtmlrenderer.layout.content;

import org.xhtmlrenderer.layout.VerticalMarginCollapser;

public abstract class AbstractCollapsableContent extends AbstractCachingContent
        implements CollapsableContent {
    private boolean _topMarginCollapsed;
    private boolean _bottomMarginCollapsed;

    private boolean _marginsAdjoiningCalculated;
    private boolean _marginsAdjoining;

    private boolean _collapsed;

    private VerticalMarginCollapser.CollapsedMarginPair _marginToCollapse;

    public boolean isTopMarginCollapsed() {
        return _topMarginCollapsed;
    }

    public void setTopMarginCollapsed(boolean topMarginCollapsed) {
        _topMarginCollapsed = topMarginCollapsed;
    }

    public boolean isBottomMarginCollapsed() {
        return _bottomMarginCollapsed;
    }

    public void setBottomMarginCollapsed(boolean bottomMarginCollapsed) {
        _bottomMarginCollapsed = bottomMarginCollapsed;
    }

    public VerticalMarginCollapser.CollapsedMarginPair getMarginToCollapse() {
        return _marginToCollapse;
    }

    public void setMarginToCollapse(VerticalMarginCollapser.CollapsedMarginPair marginToCollapse) {
        _marginToCollapse = marginToCollapse;
    }

    public boolean isMarginsAdjoining() {
        return _marginsAdjoining;
    }

    public void setMarginsAdjoining(boolean marginsAdjoining) {
        _marginsAdjoining = marginsAdjoining;
    }

    public boolean isMarginsAdjoiningCalculated() {
        return _marginsAdjoiningCalculated;
    }

    public void setMarginsAdjoiningCalculated(boolean marginsAdjoiningCalculated) {
        _marginsAdjoiningCalculated = marginsAdjoiningCalculated;
    }

    public boolean isCollapsed() {
        return _collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        _collapsed = collapsed;
    }
}
