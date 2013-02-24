/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package org.xhtmlrenderer.pdf;

import java.util.List;

import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

public class TextAreaFormField extends AbstractFormField {
    private static final String FIELD_TYPE = "TextArea";
    
    private static final int DEFAULT_ROWS = 7;
    private static final int DEFAULT_COLS = 25;
    
    private List _lines;
    
    public TextAreaFormField(LayoutContext c, BlockBox box, int cssWidth, int cssHeight) {
        
    }
    
    protected String getFieldType() {
        return FIELD_TYPE;
    }

    public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box) {
        // TODO Auto-generated method stub
        
    }

    public int getBaseline() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean hasBaseline() {
        // TODO Auto-generated method stub
        return false;
    }

}
