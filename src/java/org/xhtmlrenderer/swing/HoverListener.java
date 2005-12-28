package org.xhtmlrenderer.swing;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.layout.Restyling;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class HoverListener extends MouseInputAdapter {
    private BasicPanel panel;
    private Box prev;

    public HoverListener(BasicPanel panel) {
        this.panel = panel;
    }

    public void mouseMoved(MouseEvent evt) {
        Box ib = findBox(evt);
        restyle(ib);
    }

    public void mouseEntered(MouseEvent evt) {
        Box ib = findBox(evt);
        restyle(ib);
    }

    public void mouseExited(MouseEvent evt) {
        Box ib = findBox(evt);
        restyle(ib);
    }

    public Box findBox(MouseEvent evt) {
        //Box box = panel.findElementBox(evt.getX(), evt.getY());
        //Uu.p("-----");
        Box box = BoxFinder.findBox(panel.getRootLayer(), evt.getX(), evt.getY());
        //Uu.p("-----");
        if (box == null) return null;
        if (box instanceof LineBox) return null;
        return box;
    }


    private void restyle(Box ib) {
        //Uu.p("under cursor = " + ib);
        boolean needRepaint = false;
        // return this box or one if it's parents to find the deepest hovered element.
        // if none then just return null
        //ib = getDeepestHover(ib);
        //Uu.p("deepest hover = " + ib);
        Element hovered_element = getHoveredElement(ib);

        if (hovered_element == panel.hovered_element) {
            return;
        }
        Element previousHoveredElement = panel.hovered_element;
        panel.hovered_element = hovered_element;

        // if moved out of the old block then unstyle it
        if (prev != null) {
            //prev.hover = false;
            needRepaint = true;
            restyleElementChildBoxes(previousHoveredElement, prev);
            prev = null;
        }

        if (hovered_element != null) {
            needRepaint = true;
            restyleElementChildBoxes(hovered_element, ib);
            prev = ib;
        }
        if (needRepaint) panel.repaint();
    }

    private void restyleElementChildBoxes(Element e, Box ib) {
        //HACK:Find the parent block box whose element is self-or-parent of e
        Box p = ib;
        while (true) {
            while (p != null && (!(p instanceof BlockBox) || p.element == null)) {
                p = p.getParent();
            }
            if (p == null) {//root box was not a block box! impossible at time of coding
                Restyling.restyle(panel.getSharedContext().newLayoutContextInstance(panel.getBaseExtents()),
                        (BlockBox) panel.getRootBox());
                return;
            }
            Element pe = p.element;
            Element ie = e;
            while (ie != null && ie != pe) {
                Node n = ie.getParentNode();
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    ie = (Element) n;
                } else {
                    ie = null;
                }
            }
            if (ie == pe) {
                Restyling.restyle(panel.getSharedContext().newLayoutContextInstance(panel.getBaseExtents()),
                        (BlockBox) p);
                return;
            }
        }
    }

    private Element getHoveredElement(Box ib) {
        if (ib == null) return null;
        Element e = ib.element;
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

