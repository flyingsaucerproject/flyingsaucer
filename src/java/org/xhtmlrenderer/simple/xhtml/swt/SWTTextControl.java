/*
 * {{{ header & license
 * Copyright (c) 2007 Vianney le Clément
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
package org.xhtmlrenderer.simple.xhtml.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.simple.xhtml.FormControl;
import org.xhtmlrenderer.simple.xhtml.FormControlAdapter;
import org.xhtmlrenderer.simple.xhtml.controls.TextControl;
import org.xhtmlrenderer.swt.BasicRenderer;

public class SWTTextControl extends SWTXhtmlControl {

    private String _sizeText;
    private boolean _noChangeText = false;

    public SWTTextControl(FormControl control, BasicRenderer parent,
            LayoutContext c, CalculatedStyle style, UserAgentCallback uac) {
        super(control, parent, c, style, uac);
    }

    protected Control createSWTControl(FormControl control,
            BasicRenderer parent, LayoutContext c, CalculatedStyle style,
            UserAgentCallback uac) {
        final TextControl tc = (TextControl) control;

        int sty = SWT.BORDER;
        if (tc.isMultiLine()) {
            sty |= SWT.MULTI;
        }
        if (tc.isReadOnly()) {
            sty |= SWT.READ_ONLY;
        }
        if (tc.isPassword()) {
            sty |= SWT.PASSWORD;
        }
        final Text text = new Text(parent, sty);
        text.setText(encodeDelimiter(control.getInitialValue()));

        StringBuffer str = new StringBuffer(tc.getSize());
        for (int i = 0; i < tc.getSize(); i++) {
            str.append('M');
        }
        if (tc.isMultiLine()) {
            for (int i = 1; i < tc.getRows(); i++) {
                str.append(Text.DELIMITER);
            }
        }
        _sizeText = str.toString();

        if (tc.getMaxLength() >= 0) {
            text.setTextLimit(tc.getMaxLength());
        }

        tc.addFormControlListener(new FormControlAdapter() {
            public void changed(FormControl control) {
                if (!_noChangeText) {
                    text.setText(encodeDelimiter(control.getValue()));
                }
                _noChangeText = false;
            }
        });

        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                _noChangeText = true;
                tc.setValue(decodeDelimiter(text.getText()));
            }
        });

        return text;
    }

    public int getIdealWidth() {
        Text text = (Text) getSWTControl();
        String old = text.getText();
        text.setText(_sizeText);
        text.pack();
        int width = text.getSize().x;
        text.setText(old);
        return width;
    }

    public int getIdealHeight() {
        Text text = (Text) getSWTControl();
        String old = text.getText();
        text.setText(_sizeText);
        text.pack();
        int height = text.getSize().y;
        text.setText(old);
        return height;
    }

    private static String encodeDelimiter(String text) {
        return text.replaceAll("\n", Text.DELIMITER);
    }

    private static String decodeDelimiter(String text) {
        return text.replaceAll(Text.DELIMITER, "\n");
    }

}
