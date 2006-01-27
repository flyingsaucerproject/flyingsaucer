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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import java.awt.Font;
import java.awt.Graphics2D;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.layout.SharedContext;

public class Java2DFSFontContext implements FontContext {
    private Graphics2D _graphics;
    private Font _font;
    
    private SharedContext _sharedContext;
    
    public Java2DFSFontContext(SharedContext sharedContext, Graphics2D graphics) {
        _sharedContext = sharedContext;
        _graphics = graphics;
    }

    public Java2DFSFontContext() {
    }
    
    public Font getFont() {
        return _font;
    }
    
    public void setFont(Font font) {
        _font = font;
    }
    
    public Graphics2D getGraphics() {
        return _graphics;
    }
    
    public void setGraphics(Graphics2D graphics) {
        _graphics = graphics;
    }
    
    public void configureFor(CssContext cssCtx, CalculatedStyle style, boolean drawing) {
        _font = ((AWTFSFont)style.getFSFont(cssCtx)).getAWTFont();
        if (drawing) {
            _graphics.setFont(_font);
            _graphics.setColor(style.getColor());
        }
    }
    
    public void configureFor(FontSpecification spec) {
        _font = _sharedContext.getFontResolver().resolveFont(_sharedContext, spec);
    }
    
    public FSFont getFont(FontSpecification spec) {
        return new AWTFSFont(_sharedContext.getFontResolver().resolveFont(_sharedContext, spec));
    }
}
