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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import java.awt.Point;

import org.xhtmlrenderer.layout.FloatManager;
import org.xhtmlrenderer.layout.Layer;

public class FloatedBlockBox extends BlockBox {
    private boolean pending;
    private Layer drawingLayer;
    
    private FloatManager manager;
    
    public FloatedBlockBox() {
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }
    
    public String toString() {
        return super.toString() + " (floated)";
    }

    public Layer getDrawingLayer() {
        return drawingLayer;
    }

    public void setDrawingLayer(Layer drawingLayer) {
        this.drawingLayer = drawingLayer;
    }
    
    public void detach() {
        super.detach();
        drawingLayer.removeFloat(this);
    }
    
    public void calcCanvasLocation() {
        Point offset = manager.getOffset(this);
        setAbsX(manager.getMaster().getAbsX() + this.x - offset.x);
        setAbsY(manager.getMaster().getAbsY() + this.y - offset.y);
        super.calcCanvasLocation();
    }

    public FloatManager getManager() {
        return manager;
    }

    public void setManager(FloatManager manager) {
        this.manager = manager;
    }
    
    public void setAbsY(int y) {
        super.setAbsY(y);
    }
}
