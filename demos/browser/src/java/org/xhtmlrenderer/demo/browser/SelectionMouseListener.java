package org.xhtmlrenderer.demo.browser;

import java.awt.*;
import java.awt.event.*;
import org.xhtmlrenderer.swing.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.util.*;
/**
 * Description of the Class
 *
 * @author   empty
 */
public class SelectionMouseListener implements MouseListener, MouseMotionListener {

    /** Description of the Field */
    protected HTMLPanel panel = null;

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void mouseClicked( MouseEvent e ) { }

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void mouseEntered( MouseEvent e ) { }

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void mouseExited( MouseEvent e ) { }

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void mousePressed( MouseEvent e ) {
        if ( e.getComponent() instanceof HTMLPanel ) {
            panel = (HTMLPanel)e.getComponent();
            panel.getContext().clearSelection();
            Box box = panel.findBox( e.getX(), e.getY() );
            if ( box == null ) {
                return;
            }
            // if box is text node then start selection
            if ( box instanceof InlineBox ) {
                int x = panel.findBoxX( e.getX(), e.getY() );
                panel.getContext().setSelectionStart( box , x);
                panel.getContext().setSelectionEnd( box , x + 1);
                panel.repaint();
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void mouseReleased( MouseEvent e ) {
        if ( panel != null ) {
            Box start = panel.getContext().getSelectionStart();
            Box end = panel.getContext().getSelectionEnd();
            StringBuffer sb = new StringBuffer();
            collectSelection(panel.getContext(), panel.getRootBox(), start, end, sb, false);
            u.p("selection = " + sb);
            panel.repaint();
        }
    }

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void mouseDragged( MouseEvent e ) {
        if ( e.getComponent() instanceof HTMLPanel ) {
            panel = (HTMLPanel)e.getComponent();
            Box box = panel.findBox( e.getX(), e.getY() );
            if ( box == null ) {
                return;
            }
            // if box is text node then start selection
            if ( ( box.node != null &&
                    box.node.getNodeName() != "body" ) &&
                    !( box instanceof BlockBox ) ) {
                int x = panel.findBoxX( e.getX(), e.getY() );
                panel.getContext().setSelectionEnd( box , x );
                panel.repaint();
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void mouseMoved( MouseEvent e ) { }
    
    
    public boolean collectSelection(Context ctx, Box root, Box current, Box last, StringBuffer sb, boolean in_selection) {
        
        if(root == current) {
            in_selection = true;
        }
        if(in_selection) {
            if(root instanceof InlineBox) {
                InlineBox ib = (InlineBox)root;
                int start = 0;
                int end = ib.getSubstring().length();
                if(ib == current) {
                    start = ib.getTextIndex(ctx,ctx.getSelectionStartX());
                }
                if(ib == last) {
                    end = ib.getTextIndex(ctx,ctx.getSelectionEndX());
                }
                String st = ib.getSubstring().substring(Math.max(0,start-1),end);
                sb.append(st);
            }
        }
        if(root == last) {
            in_selection = false;
        }
        for(int i=0; i<root.getChildCount(); i++) {
            Box child = root.getChild(i);
            in_selection = collectSelection(ctx, child,current,last,sb,in_selection);
        }
        
        return in_selection;
    }
    
    
}

