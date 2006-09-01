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

/**
 * A dummy box representing the viewport
 */
public class ViewportBox extends BlockBox {
    private Rectangle viewport;
    
    public ViewportBox(Rectangle viewport) {
        this.viewport = viewport;
    }
    
    public int getWidth() {
        return viewport.width;
    }
    
    public int getHeight() {
        return viewport.height;
    }
    
    public int getContentWidth() {
        return viewport.width;
    }
    
    public Rectangle getContentAreaEdge(int left, int top, CssContext cssCtx) {
        return new Rectangle(-viewport.x, -viewport.y, viewport.width, viewport.height);
    }
    
    public Rectangle getPaddingEdge(int left, int top, CssContext cssCtx) {
        return new Rectangle(-viewport.x, -viewport.y, viewport.width, viewport.height);
    }
}
