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

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.w3c.dom.Element;
import org.xhtmlrenderer.simple.extend.XhtmlForm;

class HiddenField extends InputField {
    public HiddenField(Element e, XhtmlForm form) {
        super(e, form);
    }

    public JComponent create() {
        JTextField textfield = new JTextField();

        // Just so we can see it
        textfield.setColumns(30);

        textfield.setEditable(false);
        textfield.setEnabled(false);

        return textfield;
    }
    
    public void applyOriginalState() {
        JTextField textfield = (JTextField) getComponent();
        
        textfield.setText(getOriginalState().getValue());
    }
    
    protected String[] getFieldValues() {
        JTextField textfield = (JTextField) getComponent();
        
        return new String[] {
                textfield.getText()
        };
    }
}
