package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.InlineLayout;
import org.xhtmlrenderer.layout.Layout;
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
        if (prev == ib) {
            return;
        }

        if (ib == null)
            panel.hovered_element = null;
        else
            panel.hovered_element = ib.getRealElement();

        // if moved out of the old block then unstyle it
        if (prev != null) {
            boolean restyled = panel.getContext().css.wasHoverRestyled(prev.getRealElement());
            //u.p("previous was styled = " + restyled);
            //u.p("prev = " + prev);
            CalculatedStyle style = panel.getContext().css.getStyle(prev.getRealElement());
            //u.p("prev calc style = " + style);
            //u.p("prev color = " + style.getColor());
            if (restyled) {
                Layout lt = panel.getContext().getLayout(prev.getRealElement());
                if (lt instanceof InlineLayout) {
                    ((InlineLayout) lt).restyle(panel.getContext(), prev);
                    panel.repaint();
                }
            }
        }

        prev = ib;
        // return if no new hovered block;
        if (ib == null) {
            return;
        }

        boolean restyled = panel.getContext().css.wasHoverRestyled(ib.getRealElement());
        //u.p("was styled = " + restyled);
        
        CalculatedStyle style = panel.getContext().css.getStyle(ib.getRealElement());
        //u.p("color = " + style.getColor());

        // if the block has a hover style then restyle it
        if (restyled) {
            Layout lt = panel.getContext().getLayout(ib.getRealElement());
            if (lt instanceof InlineLayout) {
                ((InlineLayout) lt).restyle(panel.getContext(), ib);
                panel.repaint();
            }
        }
    }

    private Box findBox(MouseEvent evt) {
        Box box = panel.findBox(evt.getX(), evt.getY());
        if (box == null) return null;
        if (box instanceof LineBox) return null;
        return box;
    }
}

