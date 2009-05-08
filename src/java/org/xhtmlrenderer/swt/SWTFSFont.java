/*
 * {{{ header & license
 * Copyright (c) 2007 Vianney le Clément
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
package org.xhtmlrenderer.swt;

import org.eclipse.swt.graphics.Font;
import org.xhtmlrenderer.render.FSFont;

/**
 * 
 * @author Vianney le Clément
 * 
 */
public class SWTFSFont implements FSFont {
    private final Font _font;
    private final float _size;
    private final boolean _noDispose;

    public SWTFSFont(Font font, float size) {
        this(font, size, false);
    }

    public SWTFSFont(Font font, float size, boolean noDispose) {
        _font = font;
        _size = size;
        _noDispose = noDispose;
    }

    public float getSize2D() {
        // return _font.getFontData()[0].getHeight();
        return _size;
    }

    public Font getSWTFont() {
        return _font;
    }

    public void dispose() {
        if (!_noDispose) {
            _font.dispose();
        }
    }

    protected void finalize() throws Throwable {
        if (!_noDispose && !_font.isDisposed()) {
            _font.dispose();
        }
    }

}
