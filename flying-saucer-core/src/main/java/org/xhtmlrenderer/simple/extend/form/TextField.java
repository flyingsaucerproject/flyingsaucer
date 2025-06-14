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
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.XhtmlForm;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.plaf.basic.BasicTextUI;
import java.awt.*;

import static org.xhtmlrenderer.util.GeneralUtil.parseIntRelaxed;

class TextField extends InputField<JTextField> {
    TextField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    @Override
    public JTextField create() {
        TextFieldJTextField textField = new TextFieldJTextField();

        // Size of 0 doesn't make any sense, so use default value
        int size = parseIntRelaxed(getAttribute("size"), 15);
        textField.setColumns(size);

        if (hasAttribute("maxlength")) {
            textField.setDocument(
                    new SizeLimitedDocument(
                            parseIntRelaxed(getAttribute("maxlength"))));
        }

        if (getAttribute("readonly").equalsIgnoreCase("readonly")) {
            textField.setEditable(false);
        }

        applyComponentStyle(textField);

        return textField;
    }

    @Override
    protected void applyComponentStyle(JComponent component) {
        super.applyComponentStyle(component);

        JTextField field = component();
        CalculatedStyle style = getBox().getStyle();
        RectPropertySet padding = style.getCachedPadding();
        Insets margin = style.padding().withDefaults(new Insets(2, 3, 2, 3));

        //if a border is set or a background color is set, then use a special JButton with the BasicButtonUI.
        if (style.disableOSBorder()) {
            //when background color is set, need to use the BasicButtonUI, certainly when using XP l&f
            BasicTextUI ui = new BasicTextFieldUI();
            field.setUI(ui);
            Border fieldBorder = BorderFactory.createEmptyBorder(margin.top, margin.left, margin.bottom, margin.right);
            field.setBorder(fieldBorder);
        }
        else {
            field.setMargin(margin);
        }

        padding.reset();

        FSDerivedValue widthValue = style.valueByName(CSSName.WIDTH);
        if (widthValue instanceof LengthValue) {
            intrinsicWidth = getBox().getContentWidth() + margin.left + margin.right;
        }

        FSDerivedValue heightValue = style.valueByName(CSSName.HEIGHT);
        if (heightValue instanceof LengthValue) {
            intrinsicHeight = getBox().getHeight() + margin.top + margin.bottom;
        }
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
