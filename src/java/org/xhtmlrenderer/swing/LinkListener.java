package org.xhtmlrenderer.swing;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.u;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LinkListener extends MouseAdapter {

    /**
     * Description of the Field
     */
    BasicPanel panel;

    /**
     * Constructor for the ClickMouseListener object
     *
     * @param panel PARAM
     */
    public LinkListener(BasicPanel panel) {
        this.panel = panel;
    }

    public void mousePressed(MouseEvent evt) {
        Box box = panel.findBox(evt.getX(), evt.getY());
        if (box == null) {
            return;
        }
        u.p("pressed " + box);

        if (box.node != null) {
            Node node = box.node;
            if (node.getNodeType() == node.TEXT_NODE) {
                node = node.getParentNode();
            }

            if (panel.getContext().getRenderingContext().getLayoutFactory().isLink(node)) {
                u.p("clicked on a link");
                box.clicked = true;
                box.color = new Color(255, 255, 0);
                panel.repaint();
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param evt PARAM
     */
    public void mouseReleased(MouseEvent evt) {

        Box box = panel.findBox(evt.getX(), evt.getY());
        if (box == null) {
            return;
        }
        u.p("pressed " + box);
        if (box.node != null) {
            Node node = box.node;
            if (node.getNodeType() == node.TEXT_NODE) {
                node = node.getParentNode();
            }

            if (panel.getContext().getRenderingContext().getLayoutFactory().isLink(node)) {
                u.p("clicked on a link");
                box.clicked = true;
                box.color = new Color(255, 0, 0);
                panel.repaint();
                followLink((Element) node);
            }

        }

    }

    /**
     * Description of the Method
     *
     * @param elem PARAM
     */
    private void followLink(final Element elem) {
        try {
            if (elem.hasAttribute("href")) {
                panel.setDocumentRelative(elem.getAttribute("href"));
            }
        } catch (Exception ex) {
            u.p(ex);
        }
    }

}

