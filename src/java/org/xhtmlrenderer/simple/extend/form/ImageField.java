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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.simple.extend.XhtmlForm;
import org.xhtmlrenderer.swing.AWTFSImage;
import org.xhtmlrenderer.util.XRLog;

class ImageField extends InputField {
    public ImageField(Element e, XhtmlForm form) {
        super(e, form);
    }

    public JComponent create() {
        JButton button = new JButton();
        Image image = null;

        if (hasAttribute("src")) {
            FSImage fsImage = getUserAgentCallback().getImageResource(getAttribute("src")).getImage();

            if (fsImage != null) {
                image = ((AWTFSImage) fsImage).getImage();
            }
        }

        if (image == null) {
            button = new JButton("Image unreachable. " + getAttribute("alt"));
        } else {
            button = new JButton(new ImageIcon(image, getAttribute("alt")));
        }

        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                XRLog.layout("Image pressed: Submit");
                
                getParentForm().submit(getComponent());
            }
        });

        return button;
    }
}
