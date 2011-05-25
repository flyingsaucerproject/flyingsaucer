/*
 * {{{ header & license
 * Copyright (c) 2007 Sean Bright
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
package org.xhtmlrenderer.simple.extend.form;

import org.xhtmlrenderer.util.ArrayUtil;

import java.util.List;

public class FormFieldState {
    private String _value;
    private boolean _checked;
    private int [] _selected;
    
    private FormFieldState() {
        _value = "";
        _checked = false;
        _selected = null;
    }
    
    public String getValue() {
        return _value;
    }

    public boolean isChecked() {
        return _checked;
    }

    public int[] getSelectedIndices() {
        return ArrayUtil.cloneOrEmpty(_selected);
    }
    
    public static FormFieldState fromString(String s) {
        FormFieldState stateObject = new FormFieldState();
        
        stateObject._value = s;

        return stateObject;
    }
    
    public static FormFieldState fromBoolean(boolean b) {
        FormFieldState stateObject = new FormFieldState();
        
        stateObject._checked = b;
        
        return stateObject;
    }
    
    public static FormFieldState fromList(List list) {
        FormFieldState stateObject = new FormFieldState();
        
        int [] indices = new int [list.size()];
        
        for (int i = 0; i < list.size(); i++) {
            indices[i] = ((Integer) list.get(i)).intValue();
        }
        
        stateObject._selected = indices;

        return stateObject;
    }
}
