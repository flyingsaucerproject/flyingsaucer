
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
import javax.swing.JTextField;
import javax.swing.JComponent;
import org.joshy.u;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.layout.*;
import org.w3c.dom.*;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;

public class InputText extends FormItemLayout {
    
    public InputText() {
    }
    
    protected JTextField _createComponent() {
        return new JTextField();
    }
    public JComponent createComponent(Context c, Element elem) {
        JTextField comp = _createComponent();
        commonPrep(comp,elem);
        if(elem.hasAttribute("value")) {
            comp.setText(elem.getAttribute("value"));
        }
        if(elem.hasAttribute("size")) {
            comp.setColumns(Integer.parseInt(elem.getAttribute("size")));
        } else {
            comp.setColumns(15);
        }
        if(elem.hasAttribute("maxlength")) {
            final int maxlength = Integer.parseInt(elem.getAttribute("maxlength"));
            comp.setDocument(new PlainDocument() {
                public void insertString(int offset, String str, AttributeSet attr)
                throws BadLocationException {
                    if(str == null) { return; }
                    if((getLength() + str.length()) <= maxlength) {
                        super.insertString(offset,str,attr);
                    }
                }
            });
        }
        if(elem.hasAttribute("readonly") && 
           elem.getAttribute("readonly").equals("readonly")) {
            comp.setEditable(false);
        }
        return comp;
    }
    
}
