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

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;

public class PageBox {
    private Style _style;
    
    private int _top;
    private int _bottom;
    
    public int getWidth(CssContext cssCtx) {
        return (int)getStyle().getCalculatedStyle().getFloatPropertyProportionalTo(
                CSSName.FS_PAGE_WIDTH, 0, cssCtx);
    }
    
    public int getHeight(CssContext cssCtx) {
        return (int)getStyle().getCalculatedStyle().getFloatPropertyProportionalTo(
                CSSName.FS_PAGE_HEIGHT, 0, cssCtx);
    }
    
    public int getContentHeight(CssContext cssCtx) {
        RectPropertySet margin = getStyle().getMarginWidth(cssCtx);
        RectPropertySet border = getStyle().getCalculatedStyle().getBorder(cssCtx);
        RectPropertySet padding = getStyle().getPaddingWidth(cssCtx);
        
        return getHeight(cssCtx) 
            - (int)margin.top() - (int)border.top() - (int)padding.top()
            - (int)padding.bottom() - (int)border.bottom() - (int)margin.bottom();
    }
    
    public int getContentWidth(CssContext cssCtx) {
        RectPropertySet margin = getStyle().getMarginWidth(cssCtx);
        RectPropertySet border = getStyle().getCalculatedStyle().getBorder(cssCtx);
        RectPropertySet padding = getStyle().getPaddingWidth(cssCtx);
        
        return getWidth(cssCtx) 
            - (int)margin.left() - (int)border.left() - (int)padding.left()
            - (int)padding.right() - (int)border.right() - (int)margin.right();
    }
    
    public Style getStyle() {
        return _style;
    }

    public void setStyle(Style style) {
        _style = style;
    }

    public int getBottom() {
        return _bottom;
    }

    public int getTop() {
        return _top;
    }
    
    public void setTopAndBottom(CssContext cssCtx, int top) {
        _top = top;
        _bottom = top + getContentHeight(cssCtx);
    }
}
