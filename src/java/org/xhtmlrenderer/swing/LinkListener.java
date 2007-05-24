/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 xhtmlrenderer.dev.java.net
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
package org.xhtmlrenderer.swing;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.render.Box;

public class LinkListener implements FSMouseListener {
    public LinkListener() {
    }

    private void checkForLink(BasicPanel panel, Box box) {
        if (box == null || box.getElement() == null) {
            return;
        }

        String uri = findLink(panel, box.getElement());

        if (uri != null) {
            linkClicked(panel, uri);
        }
    }

    private String findLink(BasicPanel panel, Element e) {
        String uri = null;

        for (Node node = e; node.getNodeType() == Node.ELEMENT_NODE; node = node.getParentNode()) {
            uri = panel.getSharedContext().getNamespaceHandler().getLinkUri((Element) node);
            
            if (uri != null) {
                break;
            }
        }

        return uri;
    }

    public void linkClicked(BasicPanel panel, String uri) {
        panel.setDocumentRelative(uri);
        panel.repaint();
    }

    public void onMouseOut(BasicPanel panel, Box box) {
    }

    public void onMouseOver(BasicPanel panel, Box box) {
    }

    public void onMouseUp(BasicPanel panel, Box box) {
        checkForLink(panel, box);
    }

    public void reset() {
    }
}

