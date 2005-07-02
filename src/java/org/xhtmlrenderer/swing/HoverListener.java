package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.Uu;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;


import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.layout.*;
import java.util.Iterator;

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
		Box box = BoxFinder.findElementBox(panel.getRootBox(),evt.getX(),evt.getY(),null);
        if (box == null) return null;
        if (box instanceof LineBox) return null;
        return box;
    }
	

    private void restyle(Box ib) {
		//Uu.p("under cursor = " + ib);
        boolean needRepaint = false;
		// return this box or one if it's parents to find the deepest hovered element.
		// if none then just return null
		ib = getDeepestHover(ib);
        //Uu.p("deepest hover = " + ib);

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
                prev.restyle = true;//notify rendering to restyle the box
                //prev.hover = false;
                needRepaint = true;
            }
        }

        // return if no new hovered block;
        if (ib != null) {
			//System.out.println("Using: " + ib);

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
			
			// if the block isn't a hover then go up to the parent.

            // if the block has a hover style then restyle it
            if (restyled) {
                ib.restyle = true;
                //ib.hover = true;
                needRepaint = true;
            }
            //}
        }
        prev = ib;
        if (needRepaint) panel.repaint();
    }
	
	private Box getDeepestHover(Box box) {
		if(box == null) {
			return null;
		}
		
		//System.out.println("elem =       " + box.element);
		//System.out.println("parent elem = " + box.getParent().element);
		//System.out.println("parent elem = " + box.getParent().getParent().element);
		
		// joshy: this is a hack to determine if the child is really just a text node child of
		// a real element that's the parent. in that case we really want to check the hover of the
		// parent. we do getparent().getParent() to be sure we skip line boxes
		// text only child node
		if(box.getParent()!=null) {
			if(box.getParent().getParent()!=null) {
				if(box.element == box.getParent().getParent().element) {
					return getDeepestHover(box.getParent());
				}
			}
		}
		if(panel.getContext().getCss().isHoverStyled(box.element)) {
			return box;
		}
		//System.out.println("going to parent: " + box.getParent());
		return getDeepestHover(box.getParent());
	}

	
}

