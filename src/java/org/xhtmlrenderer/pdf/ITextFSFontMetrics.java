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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.pdf;

import org.xhtmlrenderer.render.FSFontMetrics;

public class ITextFSFontMetrics implements FSFontMetrics {
    private float _ascent;
    private float _descent;
    private float _strikethroughOffset;
    private float _strikethroughThickness;
    private float _underlineOffset;
    private float _underlineThickness;
    
    public float getAscent() {
        return _ascent;
    }
    
    public void setAscent(float ascent) {
        _ascent = ascent;
    }
    public float getDescent() {
        return _descent;
    }
    
    public void setDescent(float descent) {
        _descent = descent;
    }
    
    public float getStrikethroughOffset() {
        return _strikethroughOffset;
    }
    
    public void setStrikethroughOffset(float strikethroughOffset) {
        _strikethroughOffset = strikethroughOffset;
    }
    
    public float getStrikethroughThickness() {
        return _strikethroughThickness;
    }
    
    public void setStrikethroughThickness(float strikethroughThickness) {
        _strikethroughThickness = strikethroughThickness;
    }
    
    public float getUnderlineOffset() {
        return _underlineOffset;
    }
    
    public void setUnderlineOffset(float underlineOffset) {
        _underlineOffset = underlineOffset;
    }
    
    public float getUnderlineThickness() {
        return _underlineThickness;
    }
    
    public void setUnderlineThickness(float underlineThickness) {
        _underlineThickness = underlineThickness;
    }
    

}
