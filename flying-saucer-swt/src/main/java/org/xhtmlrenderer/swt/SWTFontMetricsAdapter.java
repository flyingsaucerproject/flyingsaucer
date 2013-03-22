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

import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.xhtmlrenderer.render.FSFontMetrics;

/**
 * Adapt SWT's font metrics to Flying Saucer's ones.
 * 
 * @author Vianney le Clément
 * 
 */
public class SWTFontMetricsAdapter implements FSFontMetrics {
    private final FontMetrics _fm;

    public SWTFontMetricsAdapter(SWTFontContext context, SWTFSFont font) {
        GC gc = ((SWTFontContext) context).getGC();
        gc.setFont(((SWTFSFont) font).getSWTFont());
        _fm = gc.getFontMetrics();
    }

    public float getAscent() {
        return _fm.getAscent() + _fm.getLeading();
    }

    public float getDescent() {
        return _fm.getDescent();
    }

    // FIXME better metrics!

    public float getStrikethroughOffset() {
        /*
         * Strike-through offset should be half an ex. We approximate an ex here
         * as half an em.
         */
        return -getAscent() / 4;
    }

    public float getStrikethroughThickness() {
        return Math.max(1, ((float) _fm.getHeight()) / 20);
    }

    public float getUnderlineOffset() {
        return 1;
    }

    public float getUnderlineThickness() {
        return Math.max(1, ((float) _fm.getHeight()) / 20);
    }

}
