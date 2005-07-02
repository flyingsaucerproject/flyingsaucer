package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.render.*;
import java.util.Iterator;
import org.xhtmlrenderer.layout.*;
import java.awt.event.*;
import org.w3c.dom.*;
import java.awt.Point;

public class BoxFinder {
	/*
	
	
		> *) given a element, find all of the boxes that it creates and it's  
		> children create. difficult since a span could create more than one  
		> box and they will be separated by line boxes possibly.
		>
		> *) given a box, find the element that created it. there are  
		> sometimes several possible answers for this.
		>
		> *) given a coordinate, find the box that matches it
		>
		> *) given a coordinate, find the element that matches it
		>
		> *) given a box, find the absolute coordinate on screen
		>
		> *) given an ID, find the element with that ID
		>
		> *) find all possible hovers

	
	
	
	*/
	
	public static Element findElementByBox(Box box) {
		return null;
	}
	public static Box findBoxByCoords(int x, int y) {
		return null;
	}
	public static Element findElementByCoords(int x, int y) {
		return null;
	}
	public static Box findBoxByID(String id, SharedContext ctx) {
		return ctx.getIDBox(id);
	}
	public static Element findElementByID(String id, SharedContext ctx) {
		Box bx = findBoxByID(id,ctx);
		return findElementByBox(bx);
	}
	
	public static Point findCoordsByBox(Box box) {
		if(box.getParent() != null) {
			Point pt = findCoordsByBox(box.getParent());
			pt.translate(box.x,box.y);
			return pt;
		}
		return new Point(0,0);
	}
	
    public static Box findElementBox(Box box, int x, int y,
                              BlockFormattingContext bfc) {//TODO: why is this used? 
								  //A better way? should be in a render util?
        
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

    private static int[] adjustForAbsolute(Box bx, int tx, int ty, BlockFormattingContext bfc) {
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
