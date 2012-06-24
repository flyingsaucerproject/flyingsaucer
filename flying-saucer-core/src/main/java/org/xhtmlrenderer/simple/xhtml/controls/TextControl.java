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

import org.w3c.dom.Element;
import org.xhtmlrenderer.simple.xhtml.XhtmlForm;

public class TextControl extends AbstractControl {

    public static final int DEFAULT_SIZE = 20;
    public static final int DEFAULT_ROWS = 3;

    private boolean _password, _readonly, _multiline;
    private int _size, _rows, _maxlength;

    public TextControl(XhtmlForm form, Element e) {
        super(form, e);

        _readonly = (e.getAttribute("readonly").length() > 0);
        if (e.getNodeName().equalsIgnoreCase("textarea")) {
            _multiline = true;
            _password = false;
            _size = getIntAttribute(e, "cols", DEFAULT_SIZE);
            _rows = getIntAttribute(e, "rows", DEFAULT_ROWS);
            _maxlength = -1;
            setInitialValue(collectText(e));
        } else { // <input>
            _multiline = false;
            _password = e.getAttribute("type").equalsIgnoreCase("password");
            _size = getIntAttribute(e, "size", DEFAULT_SIZE);
            _rows = 1;
            _maxlength = getIntAttribute(e, "maxlength", -1);
        }
    }

    public boolean isMultiLine() {
        return _multiline;
    }

    public boolean isPassword() {
        return _password;
    }

    public boolean isReadOnly() {
        return _readonly;
    }

    public int getSize() {
        return _size;
    }

    public int getRows() {
        return _rows;
    }

    /**
     * @return the maximum length or <code>-1</code> if not defined
     */
    public int getMaxLength() {
        return _maxlength;
    }

}
