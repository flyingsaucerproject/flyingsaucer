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
        if ( ib == null ) return;
        restyle(ib);
    }
    public void mouseEntered( MouseEvent evt ) {
        InlineBox ib = findInlineBox(evt);
        if ( ib == null ) return;
        panel.hovered_element = ib.getRealElement();
        XRLog.general("Element "+panel.hovered_element+" entered");
        //restyle(ib);
    }
    public void mouseExited( MouseEvent evt ) {
        InlineBox ib = findInlineBox(evt);
        //restyle(ib);
    }
    private void restyle(InlineBox ib) {
        // if moved out of the old block then unstyle it
        /* 
         if(prev != null && prev != ib) {
            Layout lt = LayoutFactory.getLayout(prev.getRealElement());
            if(lt instanceof InlineLayout) {
                ((InlineLayout)lt).restyleNormal(panel.getContext(), prev);
                panel.repaint();
            }
        }*/
        
        if(prev == ib) {
            return;
        }
        /*
        if(prev != null) {
            boolean b = panel.getContext().css.wasHoverRestyled(prev.getRealElement());
            u.p("previous was styled = " + b);
        }
        */
        prev = ib;
        // return if no new hovered block;
        if(ib == null) { return; }
        
        CalculatedStyle style = panel.getContext().css.getStyle(ib.getRealElement());
        u.p("color = " + style.getColor());
        boolean b = panel.getContext().css.wasHoverRestyled(ib.getRealElement());
        u.p("was styled = " + b);
        style = panel.getContext().css.getStyle(ib.getRealElement());
        u.p("color = " + style.getColor());
        
        // if the block has a hover style then restyle it
        if(b) {
            Layout lt = LayoutFactory.getLayout(ib.getRealElement());
            if(lt instanceof InlineLayout) {
                ((InlineLayout)lt).restyleHover(panel.getContext(), ib);
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

