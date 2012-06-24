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
package org.xhtmlrenderer.swt;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.render.Box;

/**
 * Listener to handle links.
 * 
 * @author Vianney le Clément
 * 
 */
public class LinkListener implements MouseListener {
    private final BasicRenderer _parent;

    public LinkListener(BasicRenderer parent) {
        _parent = parent;
        parent.addMouseListener(this);
    }

    public void mouseDoubleClick(MouseEvent e) {
    }

    public void mouseDown(MouseEvent e) {
    }

    public void mouseUp(MouseEvent e) {
        String uri = findLink(e.x, e.y);
        if (uri != null) {
            linkClicked(uri);
        }
    }

    protected void linkClicked(String uri) {
        _parent.setDocumentRelative(uri);
    }

    protected String findLink(int x, int y) {
        Box box = _parent.find(x, y);
        if (box == null) {
            return null;
        }
        Element elem = box.getElement();
        if (elem == null) {
            return null;
        }

        return findLink(elem);
    }

    protected String findLink(Element e) {
        String uri = null;

        for (Node node = e; node.getNodeType() == Node.ELEMENT_NODE; node = node
            .getParentNode()) {
            uri = _parent.getSharedContext().getNamespaceHandler().getLinkUri(
                (Element) node);
            if (uri != null) {
                break;
            }
        }

        return uri;
    }

}
