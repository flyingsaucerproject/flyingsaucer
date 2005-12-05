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
package org.xhtmlrenderer.render;

public class StrutMetrics {
    private int baseline;
    private float ascent;
    private float descent;
    
    public StrutMetrics(float ascent, int baseline, float descent) {
        this.ascent = ascent;
        this.baseline = baseline;
        this.descent = descent;
    }

    public StrutMetrics() {
    }
    
    public float getAscent() {
        return ascent;
    }
    
    public void setAscent(float ascent) {
        this.ascent = ascent;
    }
    
    public int getBaseline() {
        return baseline;
    }
    
    public void setBaseline(int baseline) {
        this.baseline = baseline;
    }
    
    public float getDescent() {
        return descent;
    }
    
    public void setDescent(float descent) {
        this.descent = descent;
    }
}
