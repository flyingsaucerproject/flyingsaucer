
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

import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.*;
import javax.swing.JComponent;
import org.joshy.u;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.layout.*;
import org.w3c.dom.*;

public class InputSelect extends FormItemLayout {
    
    public InputSelect() {
    }
    
    public JComponent createComponent(Context c, Element elem) {
        JComboBox comp = new JComboBox();
        commonPrep(comp,elem);
        
        NodeList options = elem.getElementsByTagName("option");
        int selected = -1;
        for(int i=0; i<options.getLength(); i++) {
            Element value = (Element)options.item(i);
            String svalue = value.getFirstChild().getNodeValue();
            comp.addItem(svalue);
            if(value.hasAttribute("selected") && value.getAttribute("selected").equals("selected")) {
                selected = i;
            }
        }
        
        if(selected != -1) { 
            comp.setSelectedIndex(selected);
        }
        return comp;
    }
    
}
