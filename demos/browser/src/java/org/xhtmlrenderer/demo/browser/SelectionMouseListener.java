package org.xhtmlrenderer.demo.browser;

import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineTextBox;
import org.xhtmlrenderer.swing.BasicPanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Description of the Class
 *
 * @author empty
 */
public class SelectionMouseListener implements MouseListener, MouseMotionListener {

    /**
     * Description of the Field
     */
    protected BasicPanel panel = null;

    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void mousePressed(MouseEvent e) {
        if (e.getComponent() instanceof BasicPanel) {
            panel = (BasicPanel) e.getComponent();
            panel.getContext().clearSelection();
            Box box = panel.findBox(e.getX(), e.getY());
            if (box == null) {
                return;
            }
            // if box is text node then start selection
            if (box instanceof InlineBox) {
                int x = panel.findBoxX(e.getX(), e.getY());
                panel.getContext().setSelectionStart(box, x);
                panel.getContext().setSelectionEnd(box, x + 1);
                panel.repaint();
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void mouseReleased(MouseEvent e) {
        if (panel != null) {
            panel.repaint();
        }
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void mouseDragged(MouseEvent e) {
        if (e.getComponent() instanceof BasicPanel) {
            panel = (BasicPanel) e.getComponent();
            Box box = panel.findBox(e.getX(), e.getY());
            if (box == null) {
                return;
            }
            // if box is text node then start selection
            if ((box instanceof InlineTextBox)) {
                int x = panel.findBoxX(e.getX(), e.getY());
                panel.getContext().setSelectionEnd(box, x);
                panel.repaint();
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void mouseMoved(MouseEvent e) {
    }


}

