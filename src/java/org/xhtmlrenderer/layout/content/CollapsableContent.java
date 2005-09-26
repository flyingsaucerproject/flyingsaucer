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

public interface CollapsableContent extends CachingContent {
    public void setTopMarginCollapsed(boolean b);

    public boolean isTopMarginCollapsed();

    public void setBottomMarginCollapsed(boolean b);

    public boolean isBottomMarginCollapsed();

    public boolean mayCollapseInto();

    public VerticalMarginCollapser.CollapsedMarginPair getMarginToCollapse();

    public void setMarginToCollapse(VerticalMarginCollapser.CollapsedMarginPair marginToCollapse);

    public void setMarginsAdjoiningCalculated(boolean b);

    public boolean isMarginsAdjoiningCalculated();

    public void setMarginsAdjoining(boolean b);

    public boolean isMarginsAdjoining();

    public void setCollapsed(boolean b);

    public boolean isCollapsed();
}
