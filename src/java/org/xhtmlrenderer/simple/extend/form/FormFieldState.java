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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.simple.extend.XhtmlForm;

public class FormFieldState {
    private String _value;
    private boolean _checked;
    private List _selected;
    
    private FormFieldState() {
        _value = "";
        _checked = false;
        _selected = new ArrayList();
    }
    
    public String getValue() {
        return _value;
    }

    public boolean isChecked() {
        return _checked;
    }

    public int[] getSelectedIndices() {
        
        int[] indices = new int [_selected.size()];
        
        for (int i = 0; i < _selected.size(); i++) {
            indices[i] = ((Integer) _selected.get(i)).intValue();
        }
        
        return indices;
    }
    
    public static FormFieldState fromElement(Element e) {
        FormFieldState stateObject = new FormFieldState();

        if (e.getNodeName().equals("input")) {
            String type = e.getAttribute("type");

            if (type.trim().length() == 0) {
                type = "text";
            }

            if (type.equals("text") || type.equals("password") || type.equals("hidden")) {
                stateObject._value = e.getAttribute("value");
            } else if (type.equals("checkbox") || type.equals("radio")) {
                if (e.getAttribute("checked") != null && e.getAttribute("checked").equals("checked")) {
                    stateObject._checked = true;
                }
            }
        } else if (e.getNodeName().equals("textarea")) {
            stateObject._value = XhtmlForm.collectText(e);
        } else if (e.getNodeName().equals("select")) {
            NodeList options = e.getElementsByTagName("option");

            for (int i = 0; i < options.getLength(); i++) {
                Element option = (Element) options.item(i);

                if (option.hasAttribute("selected") && option.getAttribute("selected").equals("selected")) {
                    stateObject._selected.add(new Integer(i));
                }
            }
        }
        
        return stateObject;
    }
}
