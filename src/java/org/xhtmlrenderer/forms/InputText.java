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
package org.xhtmlrenderer.forms;

import org.w3c.dom.Element;
import org.xhtmlrenderer.layout.SharedContext;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


/**
 * Description of the Class
 *
 * @author empty
 */
public class InputText extends FormItemLayout {

    /**
     * Constructor for the InputText object
     */
    public InputText() {
    }

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param elem PARAM
     * @return Returns
     */
    public JComponent createComponent(SharedContext c, Element elem) {
        JTextField comp = _createComponent();
        commonPrep(comp, elem);
        if (elem.hasAttribute("value")) {
            comp.setText(elem.getAttribute("value"));
        }
        if (elem.hasAttribute("size")) {
            comp.setColumns(Integer.parseInt(elem.getAttribute("size")));
        } else {
            comp.setColumns(15);
        }
        if (elem.hasAttribute("maxlength")) {
            final int maxlength = Integer.parseInt(elem.getAttribute("maxlength"));
            comp.setDocument(new PlainDocument() {
                public void insertString(int offset, String str, AttributeSet attr)
                        throws BadLocationException {
                    if (str == null) {
                        return;
                    }
                    if ((getLength() + str.length()) <= maxlength) {
                        super.insertString(offset, str, attr);
                    }
                }
            });
        }
        if (elem.hasAttribute("readonly") &&
                elem.getAttribute("readonly").equals("readonly")) {
            comp.setEditable(false);
        }
        return comp;
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    protected JTextField _createComponent() {
        return new JTextField();
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2004/12/29 10:39:28  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.3  2004/10/23 13:40:29  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

