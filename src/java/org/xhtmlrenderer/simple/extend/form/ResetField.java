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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.w3c.dom.Element;
import org.xhtmlrenderer.simple.extend.XhtmlForm;
import org.xhtmlrenderer.util.XRLog;

class ResetField extends InputField {
    public ResetField(Element e, XhtmlForm form) {
        super(e, form);
    }
    
    public JComponent create() {
        JButton button = new JButton();

        String label = getAttribute("value");

        if (label.trim().length() == 0) {
            button.setText("Reset");
        } else {
            button.setText(label);
        }
        
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                XRLog.layout("Reset pressed: Restore");
                
                getParentForm().reset();
            }
        });

        return button;
    }
    
    public boolean includeInSubmission(JComponent source) {
        return false;
    }

    protected String[] getFieldValues() {
        return new String[] {
                hasAttribute("value") ? getAttribute("value") : "Reset" // TODO: Don't hardcode 
        };
    }
}
