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

import static org.xhtmlrenderer.util.GeneralUtil.parseIntRelaxed;

class PasswordField extends InputField<JPasswordField> {
    PasswordField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    @Override
    public JPasswordField create() {
        JPasswordField password = new JPasswordField();

        // Size of 0 doesn't make any sense, so use default value
        int size = parseIntRelaxed(getAttribute("size"), 15);
        password.setColumns(size);

        if (hasAttribute("maxlength")) {
            password.setDocument(
                    new SizeLimitedDocument(
                            parseIntRelaxed(getAttribute("maxlength"))));
        }

        if (getAttribute("readonly").equalsIgnoreCase("readonly")) {
            password.setEditable(false);
        }

        return password;
    }

    @Override
    protected void applyOriginalState() {
        JPasswordField password = component();
        password.setText(getOriginalState().getValue());
    }

    @Override
    protected String[] getFieldValues() {
        String password = new String(component().getPassword());
        return new String[]{password};
    }
}
