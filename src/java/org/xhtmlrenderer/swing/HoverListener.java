package org.xhtmlrenderer.swing;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.PaintingInfo;
import org.xhtmlrenderer.render.Box;

public class HoverListener extends MouseInputAdapter {
    private BasicPanel panel;
    private Box prev;

    public HoverListener(BasicPanel panel) {
        this.panel = panel;
    }
    
    private Box find(MouseEvent evt) {
        Box result = panel.find(evt);
        return result;
    }

    public void mouseMoved(MouseEvent evt) {
        Box ib = find(evt);
        restyle(ib);
    }

    public void mouseEntered(MouseEvent evt) {
        Box ib = find(evt);
        restyle(ib);
    }

    public void mouseExited(MouseEvent evt) {
        Box ib = find(evt);
        restyle(ib);
    }

    private void restyle(Box ib) {
        LayoutContext c = panel.getLayoutContext();
        if (c == null) {
            return;
        }
        
        boolean needRepaint = false;

        Element hovered_element = getHoveredElement(ib);

        if (hovered_element == panel.hovered_element) {
            return;
        }
        
        panel.hovered_element = hovered_element;
        
        boolean targetedRepaint = true;
        Rectangle repaintRegion = null;

        // if moved out of the old block then unstyle it
        if (prev != null) {
            needRepaint = true;
            prev.restyle(c);
            
            PaintingInfo pI = prev.getPaintingInfo();
            if (pI != null) {
                repaintRegion = new Rectangle(pI.getAggregateBounds());
            } else {
                targetedRepaint = false;
            }
            
            prev = null;
        }
        
        if (hovered_element != null) {
            needRepaint = true;
            Box target = ib.getRestyleTarget();
            target.restyle(c);
            
            if (targetedRepaint) {
                PaintingInfo pI = target.getPaintingInfo();
                if (pI != null) {
                    if (repaintRegion == null) {
                        repaintRegion = new Rectangle(pI.getAggregateBounds());
                    } else {
                        repaintRegion.add(pI.getAggregateBounds());
                    }
                } else {
                    targetedRepaint = false;
                }
            }
            
            prev = target;
        }
        
        if (needRepaint) {
            if(! targetedRepaint) {
                panel.repaint();
            } else {
                panel.repaint(repaintRegion);
            }
        }
    }
    
    private Element getHoveredElement(Box ib) {
        if (ib == null) return null;
        Element e = ib.getElement();
        while (e != null && !panel.getSharedContext().getCss().isHoverStyled(e)) {
            Node n = e.getParentNode();
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                e = (Element) n;
            } else {
                e = null;
            }
        }
        return e;
    }

    public void reset() {
        prev = null;
    }
}

