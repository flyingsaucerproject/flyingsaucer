package org.xhtmlrenderer.swing;

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

    private void restyle(Box ib) {
        // Uu.p("under cursor = " + ib);
        boolean needRepaint = false;
        if (prev == ib) {
            return;
        }

        if (ib == null)
            panel.hovered_element = null;
        else
            panel.hovered_element = ib.element;

        // if moved out of the old block then unstyle it
        if (prev != null) {
            boolean restyled = panel.getContext().getCss().isHoverStyled(prev.element);
            if (restyled) {
                //prev.restyle = true;//notify rendering to restyle the box
                //prev.hover = false;
                needRepaint = true;
            }
        }

        prev = ib;
        // return if no new hovered block;
        if (ib != null) {

            /*
                if the box is an inline box
                 and if it is a text only box, meaning it
                 does not have it's own element but is merely a child
                 of an enclosing box.
                 is inline element
            */
            // Uu.p("real element = " + ib.getRealElement());
            // skip it if it's just a text child of a block. we should
            // do the block instead
            //if (ib.isInlineElement() || !(ib instanceof InlineBox)) {
            boolean restyled = panel.getContext().getCss().isHoverStyled(ib.element);
            //Uu.p("was styled = " + ib);

            // if the block has a hover style then restyle it
            if (restyled) {
                //ib.restyle = true;
                //ib.hover = true;
                needRepaint = true;
            }
            //}
        }
        if (needRepaint) panel.repaint();
    }

    private Box findBox(MouseEvent evt) {
        Box box = panel.findElementBox(evt.getX(), evt.getY());
        if (box == null) return null;
        if (box instanceof LineBox) return null;
        return box;
    }
}

