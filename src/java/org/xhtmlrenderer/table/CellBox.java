
/* 
 * {{{ header & license 
 * Copyright (c) 2004 Joshua Marinacci 
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

package org.xhtmlrenderer.table;
import org.xhtmlrenderer.render.*;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.*;
import org.xhtmlrenderer.util.u;

public class CellBox extends BlockBox {
    public Box sub_box;
    private boolean virtual = false;
    private CellBox real_box = null;
    public CellBox(int x, int y, int width, int height) {
        super(x,y,width,height);
    }
    
    public boolean isReal() {
        return !virtual;
    }
    public CellBox getReal() {
        return real_box;
    }
    
    public static CellBox createVirtual(CellBox real) {
        if(real == null) {
            u.p("WARNING: real is null!!!");
        }
        CellBox cb = new CellBox(0,0,0,0);
        cb.virtual = true;
        cb.real_box = real;
        return cb;
    }
    
    public RowBox rb;
        
}
