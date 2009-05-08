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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.simple.xhtml.FormControl;
import org.xhtmlrenderer.simple.xhtml.controls.ButtonControl;
import org.xhtmlrenderer.swt.BasicRenderer;
import org.xhtmlrenderer.swt.SWTFSImage;

public class SWTButtonControl extends SWTXhtmlControl {

    private Image _image = null;

    public SWTButtonControl(FormControl control, BasicRenderer parent,
            LayoutContext c, CalculatedStyle style, UserAgentCallback uac) {
        super(control, parent, c, style, uac);
    }

    protected Control createSWTControl(FormControl control,
            BasicRenderer parent, LayoutContext c, CalculatedStyle style,
            UserAgentCallback uac) {

        final ButtonControl bc = (ButtonControl) control;
        final Button button = new Button(parent, SWT.PUSH);
        button.setText(bc.getLabel());
        if (bc.isExtended()) {
            // when defined with <button>, allow the first image to be used
            NodeList images = bc.getElement().getElementsByTagName("img");
            if (images.getLength() > 0) {
                Element img = (Element) images.item(0);
                String uri = c.getNamespaceHandler().getImageSourceURI(img);
                ImageResource res = uac.getImageResource(uri);
                SWTFSImage fsi = (SWTFSImage) res.getImage();
                // copy the image to prevent disposal, and apply a disabled
                // effect if needed
                _image = new Image(button.getDisplay(), fsi.getImage(), (bc
                    .isEnabled() ? SWT.IMAGE_COPY : SWT.IMAGE_DISABLE));
                button.setImage(_image);
            }
        }

        if (bc.getType().equals("submit")) {
            button.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (bc.press()) {
                        bc.getForm().submit();
                    }
                }
            });
            // TODO better per form handling?
            parent.getShell().setDefaultButton(button);
        } else if (bc.getType().equals("reset")) {
            button.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (bc.press()) {
                        bc.getForm().reset();
                    }
                }
            });
        } else {
            button.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    bc.press();
                }
            });
        }

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

    public void dispose() {
        super.dispose();
        if (_image != null) {
            _image.dispose();
        }
    }

}
