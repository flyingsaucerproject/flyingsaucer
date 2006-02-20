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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import java.awt.Rectangle;

import org.xhtmlrenderer.css.style.CssContext;

public class MarginBox extends BlockBox {
    private Rectangle bounds;
    
    public MarginBox(Rectangle bounds) {
        this.bounds = bounds;
    }
    
    public int getWidth() {
        return bounds.width;
    }
    
    public int getHeight() {
        return bounds.height;
    }
    
    public int getContentWidth() {
        return bounds.height;
    }
    
    public Rectangle getContentAreaEdge(int left, int top, CssContext cssCtx) {
        return new Rectangle(-bounds.x, -bounds.y, bounds.width, bounds.height);
    }
    
    public Rectangle getPaddingEdge(int left, int top, CssContext cssCtx) {
        return new Rectangle(-bounds.x, -bounds.y, bounds.width, bounds.height);
    }
}
