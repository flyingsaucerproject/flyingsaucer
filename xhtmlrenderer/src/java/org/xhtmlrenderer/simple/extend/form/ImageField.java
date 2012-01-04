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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.XhtmlForm;
import org.xhtmlrenderer.swing.AWTFSImage;
import org.xhtmlrenderer.util.XRLog;

class ImageField extends InputField {
    public ImageField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    public JComponent create() {
        JButton button;
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
            final ImageIcon imgIcon = new ImageIcon(image, getAttribute("alt"));
            final Image img = imgIcon.getImage();
            button = new JButton() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                }

                public Dimension getPreferredSize() {
                    return new Dimension(imgIcon.getIconWidth(), imgIcon.getIconHeight());
                }
            };
        }

        button.setUI(new BasicButtonUI());
        button.setContentAreaFilled(false);


        CalculatedStyle style = getStyle();

        FSDerivedValue widthValue = style.valueByName(CSSName.WIDTH);
        if (widthValue instanceof LengthValue) {
            intrinsicWidth = new Integer(getBox().getContentWidth());
        }

        FSDerivedValue heightValue = style.valueByName(CSSName.HEIGHT);
        if (heightValue instanceof LengthValue) {
            intrinsicHeight = new Integer(getBox().getHeight());
        }

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                XRLog.layout("Image pressed: Submit");

                getParentForm().submit(getComponent());
            }
        });

        return button;
    }
}
