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

import java.util.Iterator;

import org.w3c.dom.Element;
import org.xhtmlrenderer.simple.xhtml.FormControl;
import org.xhtmlrenderer.simple.xhtml.XhtmlForm;

public class CheckControl extends AbstractControl {

    private boolean _initialValue, _radio;

    public CheckControl(XhtmlForm form, Element e) {
        super(form, e);

        _initialValue = e.getAttribute("checked").length() != 0;
        setSuccessful(_initialValue);

        _radio = e.getAttribute("type").equals("radio");
    }

    public void setSuccessful(boolean successful) {
        super.setSuccessful(successful);
        if (_radio && successful) {
            // mark all other radio with the same name as unsucessful
            XhtmlForm form = getForm();
            if (form == null) {
                return;
            }
            for (Iterator iter = form.getAllControls(getName()).iterator(); iter
                .hasNext();) {
                FormControl control = (FormControl) iter.next();
                if (control instanceof CheckControl) {
                    CheckControl check = (CheckControl) control;
                    if (check.isRadio() && check != this) {
                        check.setSuccessful(false);
                    }
                }
            }
        }
    }

    public boolean isRadio() {
        return _radio;
    }

    public void reset() {
        setSuccessful(_initialValue);
    }

}
