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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicTextAreaUI;
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

class TextAreaField extends FormField {
    private TextAreaFieldJTextArea _textarea;

    public TextAreaField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    public JComponent create() {
        int rows = 4;
        int cols = 10;

        if (hasAttribute("rows")) {
            int parsedRows = GeneralUtil.parseIntRelaxed(getAttribute("rows"));

            if (parsedRows > 0) {
                rows = parsedRows;
            }
        }

        if (hasAttribute("cols")) {
            int parsedCols = GeneralUtil.parseIntRelaxed(getAttribute("cols"));

            if (parsedCols > 0) {
                cols = parsedCols;
            }
        }

        _textarea = new TextAreaFieldJTextArea(rows, cols);

        _textarea.setWrapStyleWord(true);
        _textarea.setLineWrap(true);

        JScrollPane scrollpane = new JScrollPane(_textarea);
        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        applyComponentStyle(_textarea, scrollpane);

        return scrollpane;
    }

    protected void applyComponentStyle(TextAreaFieldJTextArea textArea, JScrollPane scrollpane) {
        super.applyComponentStyle(textArea);

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
            BasicTextUI ui = new BasicTextAreaUI();
            textArea.setUI(ui);
            scrollpane.setBorder(null);
        }

        textArea.setMargin(new Insets(top, left, bottom, right));

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

    
    protected FormFieldState loadOriginalState() {
        return FormFieldState.fromString(
                XhtmlForm.collectText(getElement()));
    }

    protected void applyOriginalState() {
        _textarea.setText(getOriginalState().getValue());
    }
    
    protected String[] getFieldValues() {
        JTextArea textarea = (JTextArea) ((JScrollPane) getComponent()).getViewport().getView();
        
        return new String[] {
                textarea.getText()
        };
    }


    private class TextAreaFieldJTextArea extends JTextArea {
        int columnWidth = 0;

        public TextAreaFieldJTextArea(int rows, int columns) {
            super(rows, columns);
        }
        //override getColumnWidth to base on 'o' instead of 'm'.  more like other browsers
        protected int getColumnWidth() {
            if (columnWidth == 0) {
                FontMetrics metrics = getFontMetrics(getFont());
                columnWidth = metrics.charWidth('o');
            }
            return columnWidth;
        }

        //Avoid Swing bug #5042886.   This bug was fixed in java6
        public Dimension getPreferredScrollableViewportSize() {
            Dimension size = super.getPreferredScrollableViewportSize();
            size = (size == null) ? new Dimension(400,400) : size;
            Insets insets = getInsets();

            size.width = (getColumns() == 0) ? size.width : getColumns() * getColumnWidth() + insets.left + insets.right;
            size.height = (getRows() == 0) ? size.height : getRows() * getRowHeight() + insets.top + insets.bottom;
            return size;
        }
    }

}
