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

import org.w3c.dom.Element;
import org.xhtmlrenderer.simple.extend.XhtmlForm;
import org.xhtmlrenderer.util.GeneralUtil;

class TextAreaField extends FormField {
    private JTextArea _textarea;

    public TextAreaField(Element e, XhtmlForm form) {
        super(e, form);
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

        _textarea = new JTextArea(rows, cols);

        JScrollPane scrollpane = new JScrollPane(_textarea);

        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        return scrollpane;
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
}
