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

import org.w3c.dom.Element;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.XhtmlForm;

import javax.swing.*;

class CheckboxField extends InputField<JCheckBox> {
    CheckboxField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    @Override
    public JCheckBox create() {
        JCheckBox checkbox = new JCheckBox();

        checkbox.setText("");
        checkbox.setOpaque(false);

        return checkbox;
    }

    @Override
    protected FormFieldState loadOriginalState() {
        return FormFieldState.fromBoolean(
                getAttribute("checked").equalsIgnoreCase("checked"));
    }

    @Override
    protected void applyOriginalState() {
        JToggleButton button = component();
        button.setSelected(getOriginalState().isChecked());
    }

    @Override
    protected String[] getFieldValues() {
        JToggleButton button = component();

        if (button.isSelected()) {
            return new String [] {
                    hasAttribute("value") ? getAttribute("value") : "on"
            };
        } else {
            return new String[] {};
        }
    }
}
