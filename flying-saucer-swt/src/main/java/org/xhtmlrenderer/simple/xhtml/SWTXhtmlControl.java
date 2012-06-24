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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Control;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.simple.xhtml.FormControl;
import org.xhtmlrenderer.simple.xhtml.FormControlAdapter;
import org.xhtmlrenderer.swt.BasicRenderer;
import org.xhtmlrenderer.swt.SWTFSFont;

public abstract class SWTXhtmlControl implements SWTFormControl {

    private FormControl _control;
    protected Control _swtControl;

    private Color _foreground, _background;

    public SWTXhtmlControl(FormControl control, BasicRenderer parent,
            LayoutContext c, CalculatedStyle style, UserAgentCallback uac) {
        _control = control;
        _swtControl = createSWTControl(control, parent, c, style, uac);
        // apply CSS styles
        _swtControl.setFont(((SWTFSFont) style.getFSFont(c)).getSWTFont());
        if (style.getColor() != null) {
            _foreground = createFromAWT(parent.getDisplay(), style.getColor());
            _swtControl.setForeground(_foreground);
        }
        if (style.getBackgroundColor() != null) {
            _background = createFromAWT(parent.getDisplay(), style
                .getBackgroundColor());
            _swtControl.setBackground(_background);
        }
        // some HTML attributes
        if (!control.isEnabled()) {
            _swtControl.setEnabled(false);
        }
        String title = control.getElement().getAttribute("title");
        if (title.length() != 0) {
            _swtControl.setToolTipText(title);
        }
        // enable/disable handler
        control.addFormControlListener(new FormControlAdapter() {
            public void enabled(FormControl control) {
                _swtControl.setEnabled(control.isEnabled());
            }
        });
    }

    private static Color createFromAWT(Device device, FSColor fsColor) {
        if (fsColor instanceof FSRGBColor) {
            FSRGBColor fsrgbcolor = ((FSRGBColor) fsColor);

            return new Color(device, fsrgbcolor.getRed(), fsrgbcolor.getGreen(),
                    fsrgbcolor.getBlue());
        } else {
            throw new IllegalArgumentException("Don't currently support CMYK in SWT rendering");
        }
    }

    protected abstract Control createSWTControl(FormControl control,
            BasicRenderer parent, LayoutContext c, CalculatedStyle style,
            UserAgentCallback uac);

    public void dispose() {
        _swtControl.dispose();
        if (_foreground != null) {
            _foreground.dispose();
        }
        if (_background != null) {
            _background.dispose();
        }
    }

    public Control getSWTControl() {
        return _swtControl;
    }

    public FormControl getFormControl() {
        return _control;
    }

}
