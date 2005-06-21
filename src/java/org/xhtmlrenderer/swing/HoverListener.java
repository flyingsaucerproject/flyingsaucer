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

    private Box findBox(MouseEvent evt) {
        //Box box = panel.findElementBox(evt.getX(), evt.getY());
		Box box = findElementBox(panel.getRootBox(),evt.getX(),evt.getY(),null);
        if (box == null) return null;
        if (box instanceof LineBox) return null;
        return box;
    }
	
	
    public Box findElementBox(Box box, int x, int y,
                              BlockFormattingContext bfc) {//TODO: why is this used? A better way? should be in a render util?
        
        if (box == null) {
            return null;
        }

        // go down to the next bfc
        if (box.getBlockFormattingContext() != null) {
            bfc = box.getBlockFormattingContext();
        }
		
		// test if the point is within the box at all
		if(!box.contains(x,y) && !box.isChildrenExceedBounds()) {
			//System.out.println("it's outside: " + box + " " + x + " " + y);
			if(box.element != null) {
				//System.out.println(box.element.getNodeName());
			}
			return null;
		}
		
		//System.out.println("made it to: " + box);
		if(box.element != null) {
			//System.out.println(box.element.getNodeName());
		}

        // loop through the children first
        Iterator it = box.getChildIterator();
        while (it.hasNext()) {
            Box bx = (Box) it.next();
            int tx = x;
            int ty = y;
            tx -= bx.x;
            tx -= bx.tx;
            ty -= bx.y;
            ty -= bx.ty;

            if (bx.absolute) {
                int[] adj = adjustForAbsolute(bx, tx, ty, bfc);
                tx = adj[0];
                ty = adj[1];
            }


            // test the contents
            Box retbox = null;
            retbox = findElementBox(bx, tx, ty, bfc);
            if (retbox != null) {
                return retbox;
            }
            
            // test the box itself
            
            // skip if it's text only so that we can
            // hit the parent instead
			
            // skip line boxes
            if (bx instanceof LineBox) {
                continue;
            }

            int tty = y;
            if (bx instanceof InlineBox) {
                InlineBox ibx = (InlineBox) bx;
                LineBox lbx = (LineBox) box;
                int off = lbx.getBaseline() + ibx.y - ibx.height;//not really correct
                tty -= off;
            }
            
            if (bx.contains(x - bx.x, tty - bx.y)) {
                //TODO: if this is InlineBox, we need to find the first previous sibling with a pushStyle
                return bx;
            }
        }

        return null;
    }

    private int[] adjustForAbsolute(Box bx, int tx, int ty, BlockFormattingContext bfc) {
        if (bfc != null) {
            if (bx.left_set) {
                tx -= bx.left;
            }
            if (bx.right_set) {
                int off = (bfc.getWidth() - bx.width - bx.right);
                tx -= off;
            }
            if (bx.top_set) {
                ty -= bx.top;
            }
            if (bx.bottom_set) {
                int off = (bfc.getHeight() - bx.height - bx.bottom);
                ty -= off;
            }
        }

        int[] adjs = new int[2];
        adjs[0] = tx;
        adjs[1] = ty;
        return adjs;
    }
	
}

