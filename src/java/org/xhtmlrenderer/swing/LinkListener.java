package org.xhtmlrenderer.swing;

import org.w3c.dom.Element;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.Uu;

import javax.swing.event.MouseInputAdapter;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

public class LinkListener extends MouseInputAdapter {

    /**
     * Description of the Field
     */
    protected BasicPanel panel;

    /**
     * Constructor for the ClickMouseListener object
     *
     * @param panel PARAM
     */
    public LinkListener(BasicPanel panel) {
        this.panel = panel;
    }

    public void mouseEntered(MouseEvent evt) {
        Box box = panel.findBox(evt.getX(), evt.getY());
        setCursor(box);
    }

    public void mouseExited(MouseEvent evt) {
        Box box = panel.findBox(evt.getX(), evt.getY());
        setCursor(box);
    }

    public void mousePressed(MouseEvent evt) {
    }

    public void mouseReleased(MouseEvent evt) {
        Box box = panel.findBox(evt.getX(), evt.getY());
        if (box == null) return;

        Element elem = box.content.getElement();
        if (elem == null) return;

        if (panel.getContext().getRenderingContext().getLayoutFactory().isLink(elem)) {
            linkClicked(box, evt);
        }

    }

    public void mouseMoved(MouseEvent evt) {
        Box box = panel.findBox(evt.getX(), evt.getY());
        setCursor(box);
    }

    public void mouseDragged(MouseEvent evt) {
        Box box = panel.findBox(evt.getX(), evt.getY());
        setCursor(box);
    }

    public void linkClicked(Box box, MouseEvent evt) {
        box.clicked = true;
        panel.repaint();
        try {
            Element elem = box.content.getElement();
            if (elem.hasAttribute("href")) {
                panel.setDocumentRelative(elem.getAttribute("href"));
            }
        } catch (Exception ex) {
            Uu.p(ex);
        }
    }

    private Box prev;

    private void setCursor(Box box) {
        if (prev == box || box == null) {
            return;
        }

        if (box.content == null) {
            return;
        }

        if (box.content.getElement() == null) {
            return;
        }

        //TODO: this is namespace-specific. Do it via NamespaceHandler
        if (box.content.getElement().getNodeName().equals("a")) {
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
/*
    private Box findBox(MouseEvent evt) {
        Box box = panel.findBox(evt.getX(), evt.getY());
        if (box == null) return null;
        if (box instanceof LineBox) return null;
        return box;
    }
*/

}

