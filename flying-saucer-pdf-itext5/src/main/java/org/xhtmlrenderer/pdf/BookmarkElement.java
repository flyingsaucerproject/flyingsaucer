/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
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

import java.awt.Point;

import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

public class BookmarkElement implements ITextReplacedElement {
    private Point _location = new Point(0, 0);
    private String _anchorName;
    
    public int getIntrinsicWidth() {
        return 0;
    }

    public int getIntrinsicHeight() {
        return 0;
    }

    public Point getLocation() {
        return _location;
    }

    public void setLocation(int x, int y) {
        _location = new Point(x, y);
    }
    
    public void detach(LayoutContext c) {
        c.removeBoxId(getAnchorName());
    }

    public String getAnchorName() {
        return _anchorName;
    }

    public void setAnchorName(String anchorName) {
        _anchorName = anchorName;
    }
    
    public boolean isRequiresInteractivePaint() {
        // N/A
        return false;
    }

    public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box) {
    }

	public int getBaseline() {
		return 0;
	}

	public boolean hasBaseline() {
		return false;
	}
}
