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
package org.xhtmlrenderer.simple.xhtml.controls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.xhtmlrenderer.simple.xhtml.XhtmlForm;

public class ButtonControl extends AbstractControl {

    private String _type, _label;
    private boolean _extended;
    private List _listeners = new ArrayList();

    public ButtonControl(XhtmlForm form, Element e) {
        super(form, e);

        _extended = e.getNodeName().equalsIgnoreCase("button");
        if (_extended) {
            _label = collectText(e);
        } else {
            _label = getValue();
        }

        _type = e.getAttribute("type").toLowerCase();
        if (!_type.equals("reset") && !_type.equals("button")) {
            _type = "submit";
        }
    }

    public String getType() {
        return _type;
    }
    
    public String getLabel() {
        return _label;
    }

    /**
     * @return <code>true</code> if this button has been defined with
     *         <code>&lt;button&gt;</code>, <code>false</code> if this
     *         button has been defined with <code>&lt;input&gt;</code>
     */
    public boolean isExtended() {
        return _extended;
    }

    public void addButtonControlListener(ButtonControlListener listener) {
        _listeners.add(listener);
    }

    public void removeButtonControlListener(ButtonControlListener listener) {
        _listeners.remove(listener);
    }

    public boolean press() {
        for (Iterator iter = _listeners.iterator(); iter.hasNext();) {
            if(!((ButtonControlListener) iter.next()).pressed(this))
                return false;
        }
        return true;
    }
}
