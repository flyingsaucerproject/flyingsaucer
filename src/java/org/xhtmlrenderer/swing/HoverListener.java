package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.util.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.css.bridge.*;
import org.xhtmlrenderer.css.*;
import org.xhtmlrenderer.css.style.*;
import javax.swing.event.*;
import java.awt.event.*;

public class HoverListener extends MouseInputAdapter {
    private HTMLPanel panel;
    private InlineBox prev;
    public HoverListener(HTMLPanel panel) {
        this.panel = panel;
    }
    public void mouseMoved( MouseEvent evt ) {
        InlineBox ib = findInlineBox(evt);
        restyle(ib);
    }
    public void mouseEntered( MouseEvent evt ) {
        InlineBox ib = findInlineBox(evt);
        restyle(ib);
    }
    public void mouseExited( MouseEvent evt ) {
        InlineBox ib = findInlineBox(evt);
        restyle(ib);
    }
    private void restyle(InlineBox ib) {
        if(prev == ib) {
            return;
        }

        if(ib == null) panel.hovered_element = null;
        else panel.hovered_element = ib.getRealElement();

        // if moved out of the old block then unstyle it
        if(prev != null) {
            boolean restyled = panel.getContext().css.wasHoverRestyled(prev.getRealElement());
            //u.p("previous was styled = " + restyled);
            //u.p("prev = " + prev);
            CalculatedStyle style = panel.getContext().css.getStyle(prev.getRealElement());
            //u.p("prev calc style = " + style);
            //u.p("prev color = " + style.getColor());
            if(restyled) {
                Layout lt = LayoutFactory.getLayout(prev.getRealElement());
                if(lt instanceof InlineLayout) {
                    ((InlineLayout)lt).restyle(panel.getContext(), prev);
                    panel.repaint();
                }
            }
        }

        prev = ib;
        // return if no new hovered block;
        if(ib == null) { return; }
        
        boolean restyled = panel.getContext().css.wasHoverRestyled(ib.getRealElement());
        //u.p("was styled = " + restyled);
        
        CalculatedStyle style = panel.getContext().css.getStyle(ib.getRealElement());
        //u.p("color = " + style.getColor());

        // if the block has a hover style then restyle it
        if(restyled) {
            Layout lt = LayoutFactory.getLayout(ib.getRealElement());
            if(lt instanceof InlineLayout) {
                ((InlineLayout)lt).restyle(panel.getContext(), ib);
                panel.repaint();
            }
        }
    }
    
    private InlineBox findInlineBox(MouseEvent evt) {
        Box box = panel.findBox( evt.getX(), evt.getY() );
        if ( box == null ) return null;
        if(box instanceof InlineBox) {
            InlineBox ib = (InlineBox)box;
            return ib;
        }
        return null;
    }
}

