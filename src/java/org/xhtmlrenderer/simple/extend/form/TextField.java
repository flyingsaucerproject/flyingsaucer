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

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.plaf.basic.BasicTextUI;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.XhtmlForm;
import org.xhtmlrenderer.util.GeneralUtil;

import java.awt.*;

class TextField extends InputField {
    public TextField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    public JComponent create() {
        TextFieldJTextField textfield = new TextFieldJTextField();

        if (hasAttribute("size")) {
            int size = GeneralUtil.parseIntRelaxed(getAttribute("size"));
            
            // Size of 0 doesn't make any sense, so use default value
            if (size == 0) {
                textfield.setColumns(15);
            } else {
                textfield.setColumns(size);
            }
        } else {
            textfield.setColumns(15);
        }

        if (hasAttribute("maxlength")) {
            textfield.setDocument(
                    new SizeLimitedDocument(
                            GeneralUtil.parseIntRelaxed(getAttribute("maxlength"))));
        }

        if (hasAttribute("readonly") &&
                getAttribute("readonly").equalsIgnoreCase("readonly")) {
            textfield.setEditable(false);
        }

        applyComponentStyle(textfield);
        
        return textfield;
    }

    protected void applyComponentStyle(JComponent component) {
        super.applyComponentStyle(component);

        TextFieldJTextField field = (TextFieldJTextField)component;

        CalculatedStyle style = getBox().getStyle();
        BorderPropertySet border = style.getBorder(null);
        boolean disableOSBorder = (border.leftStyle() != null && border.rightStyle() != null || border.topStyle() != null || border.bottomStyle() != null);

        RectPropertySet padding = style.getCachedPadding();

        Integer paddingTop = getLengthValue(style, CSSName.PADDING_TOP);
        Integer paddingLeft = getLengthValue(style, CSSName.PADDING_LEFT);
        Integer paddingBottom = getLengthValue(style, CSSName.PADDING_BOTTOM);
        Integer paddingRight = getLengthValue(style, CSSName.PADDING_RIGHT);


        int top = paddingTop == null ? 2 : Math.max(2, paddingTop.intValue());
        int left = paddingLeft == null ? 3 : Math.max(3, paddingLeft.intValue());
        int bottom = paddingBottom == null ? 2 : Math.max(2, paddingBottom.intValue());
        int right = paddingRight == null ? 3 : Math.max(3, paddingRight.intValue());

        //if a border is set or a background color is set, then use a special JButton with the BasicButtonUI.
        if (disableOSBorder) {
            //when background color is set, need to use the BasicButtonUI, certainly when using XP l&f
            BasicTextUI ui = new BasicTextFieldUI();
            field.setUI(ui);
            Border fieldBorder = BorderFactory.createEmptyBorder(top, left, bottom, right);
            field.setBorder(fieldBorder);
        }
        else {
            field.setMargin(new Insets(top, left, bottom, right));
        }

        padding.setRight(0);
        padding.setLeft(0);
        padding.setTop(0);
        padding.setBottom(0);

        FSDerivedValue widthValue = style.valueByName(CSSName.WIDTH);
        if (widthValue instanceof LengthValue) {
            intrinsicWidth = new Integer(getBox().getContentWidth() + left + right);
        }

        FSDerivedValue heightValue = style.valueByName(CSSName.HEIGHT);
        if (heightValue instanceof LengthValue) {
            intrinsicHeight = new Integer(getBox().getHeight() + top + bottom);
        }
    }


    
    protected void applyOriginalState() {
        JTextField textfield = (JTextField) getComponent();
        
        textfield.setText(getOriginalState().getValue());
        
        // Make sure we are showing the front of 'value' instead of the end.
        textfield.setCaretPosition(0);
    }
    
    protected String[] getFieldValues() {
        JTextField textfield = (JTextField) getComponent();
        
        return new String[] {
                textfield.getText()
        };
    }

    private static class TextFieldJTextField extends JTextField {
        //override getColumnWidth to base on 'o' instead of 'm'.  more like other browsers
        int columnWidth = 0;
        protected int getColumnWidth() {
            if (columnWidth == 0) {
                FontMetrics metrics = getFontMetrics(getFont());
                columnWidth = metrics.charWidth('o');
            }
            return columnWidth;
        }
    }

}
