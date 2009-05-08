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
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.PaintingInfo;
import org.xhtmlrenderer.render.Box;

/**
 * Listener implementing css :hover class.
 * 
 * @author Vianney le Clément
 * 
 */
public class HoverListener implements MouseMoveListener {

    private final BasicRenderer _parent;
    private Box _previousBox = null;

    /**
     * Construct a HoverListener and add it to the parent.
     * 
     * @param parent
     */
    public HoverListener(BasicRenderer parent) {
        _parent = parent;
        parent.addMouseMoveListener(this);
    }

    public void mouseMove(MouseEvent e) {
        LayoutContext c = _parent.getLayoutContext();
        if (c == null) {
            return;
        }

        Box box = _parent.find(e.x, e.y);

        Element previous = _parent.getHovered_element();
        Element current = getHoveredElement(c.getCss(), box);
        if (previous == current) {
            return;
        }
        _parent.setHovered_element(current);

        boolean needRepaint = false;
        boolean targetedRepaint = true;
        Rectangle repaintTarget = null;

        if (previous != null) {
            needRepaint = true;
            _previousBox.restyle(c);

            PaintingInfo paintInfo = _previousBox.getPaintingInfo();
            if (paintInfo == null) {
                targetedRepaint = false;
            } else {
                java.awt.Rectangle rect = paintInfo.getAggregateBounds();
                repaintTarget = new Rectangle(rect.x, rect.y, rect.width,
                    rect.height);
            }

            _previousBox = null;
        }

        if (current != null) {
            needRepaint = true;
            Box target = box.getRestyleTarget();
            target.restyle(c);

            if (targetedRepaint) {
                PaintingInfo paintInfo = target.getPaintingInfo();

                if (paintInfo == null) {
                    targetedRepaint = false;
                } else {
                    if (repaintTarget == null) {
                        java.awt.Rectangle rect = paintInfo
                            .getAggregateBounds();
                        repaintTarget = new Rectangle(rect.x, rect.y,
                            rect.width, rect.height);
                    } else {
                        java.awt.Rectangle rect = paintInfo
                            .getAggregateBounds();
                        repaintTarget.add(new Rectangle(rect.x, rect.y,
                            rect.width, rect.height));
                    }
                }
            }

            _previousBox = box;
        }

        if (needRepaint) {
            if (targetedRepaint) {
                Point origin = _parent.getOrigin();
                repaintTarget.x -= origin.x;
                repaintTarget.y -= origin.y;
                _parent.invalidate(repaintTarget);
            } else {
                _parent.invalidate();
            }
        }
    }

    private Element getHoveredElement(StyleReference style, Box ib) {
        if (ib == null) {
            return null;
        }

        Element element = ib.getElement();

        while (element != null && !style.isHoverStyled(element)) {
            Node node = element.getParentNode();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                element = (Element) node;
            } else {
                element = null;
            }
        }

        return element;
    }

}
