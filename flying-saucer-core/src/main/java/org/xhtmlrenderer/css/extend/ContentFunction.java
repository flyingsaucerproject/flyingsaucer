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
package org.xhtmlrenderer.css.extend;

import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.RenderingContext;

/**
 * Interface for objects which implement a function which creates content
 * (e.g. <code>counter(pages)</code>)
 */
public interface ContentFunction {
    /**
     * Whether or not the function value can change at render time.  If true,
     * {@link #calculate(LayoutContext, String, TextContent)} will be called.  If false,
     * {@link #calculate(RenderingContext, String, TextContent)} will be called.
     */
    public boolean isStatic();
    
    public String calculate(LayoutContext c, FSFunction function);
    public String calculate(RenderingContext c, FSFunction function, InlineText text);
    
    /**
     * If a function value can change at render time (i.e. {@link #isStatic()} returns false)
     * use this text as an approximation at layout.
     */
    public String getLayoutReplacementText();
    
    public boolean canHandle(LayoutContext c, FSFunction function);
}
