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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.simple.xhtml.FormControl;
import org.xhtmlrenderer.simple.xhtml.FormControlAdapter;
import org.xhtmlrenderer.simple.xhtml.controls.CheckControl;
import org.xhtmlrenderer.swt.BasicRenderer;

public class SWTCheckControl extends SWTXhtmlControl {

    public SWTCheckControl(FormControl control, BasicRenderer parent,
            LayoutContext c, CalculatedStyle style, UserAgentCallback uac) {
        super(control, parent, c, style, uac);
    }

    protected Control createSWTControl(FormControl control,
            BasicRenderer parent, LayoutContext c, CalculatedStyle style,
            UserAgentCallback uac) {
        final CheckControl cc = (CheckControl) control;

        final Button button = new Button(parent, (cc.isRadio() ? SWT.RADIO
                : SWT.CHECK));
        button.setText("");
        button.setSelection(cc.isSuccessful());

        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (!button.getSelection() && cc.isRadio()) {
                    button.setSelection(true);
                } else {
                    cc.setSuccessful(button.getSelection());
                }
            }
        });

        cc.addFormControlListener(new FormControlAdapter() {
            public void successful(FormControl control) {
                button.setSelection(control.isSuccessful());
            }
        });

        return button;
    }

    public int getIdealHeight() {
        getSWTControl().pack();
        return getSWTControl().getSize().y;
    }

    public int getIdealWidth() {
        getSWTControl().pack();
        return getSWTControl().getSize().x;
    }

}
