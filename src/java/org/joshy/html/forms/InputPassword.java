
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

package org.joshy.html.forms;

import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.*;
import javax.swing.JComponent;
import org.joshy.u;
import org.joshy.html.box.*;
import org.joshy.html.*;
import org.w3c.dom.*;

public class InputPassword extends InputText {
    
    public InputPassword() {
    }
    
    protected JTextField _createComponent() {
        return new JPasswordField();
    }
/*    
    public JComponent createComponent(Context c, Element elem) {
        JPasswordField comp = new JPasswordField();
        commonPrep(comp,elem);
        if(elem.hasAttribute("value")) {
            comp.setText(elem.getAttribute("value"));
        }
        if(elem.hasAttribute("size")) {
            comp.setColumns(Integer.parseInt(elem.getAttribute("size")));
        } else {
            comp.setColumns(15);
        }
        if(elem.hasAttribute("readonly") && 
           elem.getAttribute("readonly").equals("readonly")) {
            comp.setEditable(false);
        }
        return comp;
    }
    */
    
}
