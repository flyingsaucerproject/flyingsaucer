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
import org.xhtmlrenderer.layout.Context;

import javax.swing.*;


/**
 * Description of the Class
 *
 * @author empty
 */
public class InputButton extends FormItemLayout {

    /**
     * Constructor for the InputButton object
     */
    public InputButton() {
    }

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param elem PARAM
     * @return Returns
     */
    public JComponent createComponent(Context c, Element elem) {
        //U.p("created a button");
        JButton comp = new JButton();
        String type = elem.getAttribute("type");
        if (type == null || type.equals("")) {
            type = "button";
        }
        String label = elem.getAttribute("value");
        if (label == null || label.equals("")) {
            if (type.equals("reset")) {
                label = "Reset";
            }
            if (type.equals("submit")) {
                label = "Submit";
            }
        }
        comp.setText(label);
        commonPrep(comp, elem);
        return comp;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2004/12/12 02:58:33  tobega
 * Making progress
 *
 * Revision 1.3  2004/10/23 13:40:28  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

