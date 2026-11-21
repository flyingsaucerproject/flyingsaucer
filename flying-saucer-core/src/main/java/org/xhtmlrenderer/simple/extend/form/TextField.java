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
import java.awt.*;

class TextField extends AbstractTextField<JTextField> {
    TextField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    @Override
    public JTextField create() {
        TextFieldJTextField textField = new TextFieldJTextField();
        prepareTextField(textField);
        applyComponentStyle(textField);
        return textField;
    }

    @Override
    protected void applyOriginalState() {
        JTextField textField = component();

        textField.setText(getOriginalState().getValue());

        // Make sure we are showing the front of 'value' instead of the end.
        textField.setCaretPosition(0);
    }

    @Override
    protected String[] getFieldValues() {
        JTextField textField = component();

        return new String[] {
                textField.getText()
        };
    }

    private static class TextFieldJTextField extends JTextField {
        //override getColumnWidth to base on 'o' instead of 'm'.  more like other browsers
        private int columnWidth = 0;
        @Override
        protected int getColumnWidth() {
            if (columnWidth == 0) {
                FontMetrics metrics = getFontMetrics(getFont());
                columnWidth = metrics.charWidth('o');
            }
            return columnWidth;
        }
    }
}
