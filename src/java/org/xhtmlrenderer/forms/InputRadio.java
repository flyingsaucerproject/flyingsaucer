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
import java.util.List;


/**
 * Description of the Class
 *
 * @author empty
 */
public class InputRadio extends FormItemLayout {

    /**
     * Constructor for the InputRadio object
     */
    public InputRadio() {
    }

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param elem PARAM
     * @return Returns
     */
    public JComponent createComponent(SharedContext c, Element elem) {
        JRadioButton comp = new JRadioButton();
        comp.setText("");
        comp.setOpaque(false);
        if (elem.hasAttribute("checked") &&
                elem.getAttribute("checked").equals("checked")) {
            comp.setSelected(true);
        }
        commonPrep(comp, elem);

        if (elem.hasAttribute("name")) {
            String name = elem.getAttribute("name");
            List other_comps = c.getInputFieldComponents(c.getForm(), name);
            if (other_comps.size() > 0) {
                for (int i = 0; i < other_comps.size(); i++) {
                    SharedContext.FormComponent other_comp = (SharedContext.FormComponent) other_comps.get(i);
                    if (other_comp.component instanceof JRadioButton) {
                        JRadioButton other_radio = (JRadioButton) other_comp.component;
                        //Uu.p("found a matching component: " + other_radio);
                    }
                }
            }
        }
        return comp;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.6  2004/12/29 10:39:28  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.5  2004/12/12 03:32:55  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.4  2004/12/12 02:58:51  tobega
 * Making progress
 *
 * Revision 1.3  2004/10/23 13:40:28  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

