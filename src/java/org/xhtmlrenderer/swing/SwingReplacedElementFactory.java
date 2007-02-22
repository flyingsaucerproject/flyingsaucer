/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.swing.*;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.XhtmlForm;

public class SwingReplacedElementFactory implements ReplacedElementFactory {
    protected HashMap imageComponents;
    protected LinkedHashMap forms;

    public ReplacedElement createReplacedElement(
            LayoutContext c, BlockBox box,
            UserAgentCallback uac, int cssWidth, int cssHeight) {
        Element e = box.getElement();
        JComponent cc = null;
        if (e == null) {
            return null;
        }
        if (e.getNodeName().equals("img")) {
            cc = getImageComponent(e);
            if (cc == null) {
                Image im = null;
                FSImage fsImage = uac.getImageResource(e.getAttribute("src")).getImage();
                if (fsImage != null) {
                    im = ((AWTFSImage) fsImage).getImage();
                }
                
                if (im != null) {
                    if (cssWidth != -1 || cssHeight != -1) {
                        im = im.getScaledInstance(cssWidth, cssHeight, Image.SCALE_FAST);
                    }
                    return new ImageReplacedElement(im);
                } else {
                    // XXX Should return "broken" image icon
                    return new EmptyReplacedElement(
                            cssWidth < 0 ? 0 : cssWidth,
                            cssHeight < 0 ? 0 : cssHeight);
                }
                
                /*
                JLabel lbl = null;
                if (im == null) {
                    lbl = new JLabel("Image unreachable. " + e.getAttribute("alt"));
                } else {
                    Image i2 = im.getScaledInstance(cssWidth, cssHeight, Image.SCALE_FAST);
                    ImageIcon ii = new ImageIcon(i2, e.getAttribute("alt"));
                    lbl = new JLabel(ii);
                }
                lbl.setBorder(BorderFactory.createEmptyBorder());
                lbl.setSize(lbl.getPreferredSize());
                lbl.setOpaque(false);
                lbl.setFocusable(false);

                addImageComponent(e, lbl);
                cc = lbl;
                */
            }
        } else {
            //form components
            Element parentForm = getParentForm(e);
            //parentForm may be null! No problem! Assume action is this document and method is get.
            XhtmlForm form = getForm(parentForm);
            if (form == null) {
                form = new XhtmlForm(uac, parentForm);
                addForm(parentForm, form);
            }
            cc = form.addComponent(e);
        }
        if (cc == null) {
            return null;
        } else {
            SwingReplacedElement result = new SwingReplacedElement(cc);
            if (c.isInteractive()) {
                c.getCanvas().add(cc);
            }
            return result;
        }
    }

    protected void addImageComponent(Element e, JComponent cc) {
        if (imageComponents == null) {
            imageComponents = new HashMap();
        }
        imageComponents.put(e, cc);
    }

    protected void addForm(Element e, XhtmlForm f) {
        if (forms == null) {
            forms = new LinkedHashMap();
        }
        forms.put(e, f);
    }

    protected JComponent getImageComponent(Element e) {
        if (imageComponents == null) {
            return null;
        }
        return (JComponent) imageComponents.get(e);
    }

    protected XhtmlForm getForm(Element e) {
        if (forms == null) {
            return null;
        }
        return (XhtmlForm) forms.get(e);
    }

    protected Element getParentForm(Element e) {
        Node n = e;
        do {
            n = n.getParentNode();
        } while (n.getNodeType() == Node.ELEMENT_NODE && !n.getNodeName().equals("form"));
        if (n.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        return (Element) n;
    }
}
