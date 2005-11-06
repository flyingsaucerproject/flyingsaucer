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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
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

import javax.swing.event.MouseInputAdapter;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

public class LinkListener extends MouseInputAdapter {

    protected BasicPanel panel;
    private Box prev;

    public LinkListener(BasicPanel panel) {
        this.panel = panel;
    }

    public void mouseEntered(MouseEvent evt) {
        Box box = BoxFinder.findBox(panel.getRootLayer(), evt.getX(), evt.getY());
        setCursor(box);
    }

    public void mouseExited(MouseEvent evt) {
        Box box = BoxFinder.findBox(panel.getRootLayer(), evt.getX(), evt.getY());
        setCursor(box);
    }

    public void mousePressed(MouseEvent evt) {
    }

    public void mouseReleased(MouseEvent evt) {
        //Box box = panel.findBox(evt.getX(), evt.getY());
        Box box = BoxFinder.findBox(panel.getRootLayer(), evt.getX(), evt.getY());
        //Uu.p("in link listener: box finder returned: " + box);
        if (box == null) {
            return;
        }

        Element elem = box.element;
        if (elem == null) {
            return;
        }

        String uri = findLink(elem);
        if (uri != null) {
            linkClicked(uri);
        }
    }

    private String findLink(Element elem) {
        String uri = null;
        for (Node n = elem; uri == null && n.getNodeType() == Node.ELEMENT_NODE; n = n.getParentNode()) {
            uri = panel.getSharedContext().getNamespaceHandler().getLinkUri((Element) n);
        }
        //Uu.p("found a link: " + uri);
        return uri;
    }

    public void mouseMoved(MouseEvent evt) {
        //Box box = panel.findBox(evt.getX(), evt.getY());
        Box box = BoxFinder.findBox(panel.getRootLayer(), evt.getX(), evt.getY());
        setCursor(box);
    }
    
    public void mouseDragged(MouseEvent evt) {
        Box box = BoxFinder.findBox(panel.getRootLayer(), evt.getX(), evt.getY());
        setCursor(box);
    }

    public void linkClicked(String uri) {
        //Uu.p("clicked on: " + uri);
        panel.setDocumentRelative(uri);
        panel.repaint();
    }

    private void setCursor(Box box) {
        if (prev == box || box == null || box.element == null) {
            return;
        }

        if (findLink(box.element) != null) {
            if (!panel.getCursor().equals(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))) {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        } else {
            if (!panel.getCursor().equals(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))) {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

        prev = box;
    }

    public void reset() {
        prev = null;
    }
}

