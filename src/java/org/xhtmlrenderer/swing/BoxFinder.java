package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.render.*;
import java.util.Iterator;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.util.Uu;
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
	
	public static Box findElementBox2(Box box, int x, int y, BlockFormattingContext bfc) {
		//Uu.p("find element box: " + x + " " + y + " " + box + " " + bfc);
		//if(box.element != null) { Uu.p("elem : " + box.element.getNodeName()); }
		
		// skip if no box to look at
		if(box == null) {
			return null;
		}
		
		

		int dx = 0;
		int dy = 0;
		// adjust for own absolute positioning
		if(bfc != null) {
			if(box.absolute) {
				dx += bfc.getX();
				dy += bfc.getY();
                int[] adj = adjustForAbsolute2(box, x, y, bfc);
				dx += adj[0];
				dy += adj[1];
				//Uu.p("abs: " + dx+","+dy);
			}
		}

		// skip null box
		// adjust for own x,y
		dx += box.x;
		dy += box.y;
		//Uu.p(dx+","+dy);
		
		// adjust for own insets (margin + border + padding)
		dx += box.tx;
		dy += box.ty;
		//Uu.p(dx+","+dy);
		
		int ttx = x - dx + box.tx;
		int tty = y - dy + box.ty;
		
		
		//Uu.p("bfc = " + bfc);
        // go down to the next bfc
        if (box.getBlockFormattingContext() != null) {
            bfc = box.getBlockFormattingContext();
			//Uu.p("new bfc = " + bfc);
        }
		

		// if no way it could be inside this box, then return null
		if(!box.contains(ttx,tty)) {
			if(!box.isChildrenExceedBounds()) {
				//System.out.println("it's outside: " + box + " " + x + " " + y);
				//Uu.p("outside so skipping");
				return null;
			} else {
				//Uu.p("outside but exceeding children");
			}
		}


		// next test the children
		Iterator it = box.getChildIterator();
        while (it.hasNext()) {
            Box bx = (Box) it.next();
			// if returned something, then return it
			// shift the bfc
			bfc.translate(dx,dy);//box.x,box.y);
			Box retbox = findElementBox2(bx,x-dx,y-dy,bfc);
			bfc.translate(-dx,-dy);//-box.x,-box.y);
			//Uu.p("returned: " + retbox);
			if(retbox != null) { return retbox; }
		}

		// if this box matches, then return it
		//Uu.p("here you go");
		//Uu.p(x+","+y);
		//Uu.p(dx+","+dy);
		//Uu.p(box.tx+","+box.ty);
		//Uu.p(ttx+","+tty);
		if(box.contains(ttx,tty)) {
		//if(box.contains(x-dx+box.tx,y-dy+box.ty)) {
			//Uu.p("matched: " + box);
			return box;
		}
		return null;
	}
	

    private static int[] adjustForAbsolute2(Box bx, int tx, int ty, BlockFormattingContext bfc) {
		int dx = 0;
		int dy = 0;
        if (bfc != null) {
            if (bx.left_set) {
				//Uu.p("bfc = " + bfc);// + " x,y = " + bfc.getX() + "," + bfc.getY());
				//Uu.p("bfc = " + bfc.hashCode());
				//Uu.p(" insets = " + bfc.getInsets());
				//Uu.p(" padding = " + bfc.getPadding());
				//Uu.p("tx = " + tx);
				//Uu.p("bx = " + bx);
				//tx -= (bfc.getInsets().left - bfc.getPadding().left);
                dx = bx.left;
				//Uu.p("bx left = " + bx.left);
            }
			
			if (bx.right_set) {
                int off = (bfc.getWidth() - bx.width - bx.right);
                dx = off;
			}
			
			dy = bx.top;
        }

        int[] adjs = new int[2];
        adjs[0] = dx;
        adjs[1] = dy;
        return adjs;
    }


    public static Box findElementBox(Box box, int x, int y,
                              BlockFormattingContext bfc) {//TODO: why is this used? 
								  //A better way? should be in a render util?

		//Uu.p("find element box: " + x + " " + y + " " + box + " " + bfc);

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
		
		if(box.element != null) {
			Uu.p(box.element.getNodeName());
		}

        // loop through the children first
        Iterator it = box.getChildIterator();
        while (it.hasNext()) {
            Box bx = (Box) it.next();
			Uu.p("box = " + bx);
            int tx = x;
            int ty = y;
			//Uu.p("tx = " + tx + "," + ty);
            tx -= bx.x;
			//Uu.p("tx = " + tx + "," + ty);
            tx -= bx.tx;
			//Uu.p("tx = " + tx + "," + ty);
            ty -= bx.y;
            ty -= bx.ty;

            if (bx.absolute) {
				Uu.p("abs");
                int[] adj = adjustForAbsolute(bx, tx, ty, bfc);
                tx = adj[0];
                ty = adj[1];
            }
			Uu.p("tx = " + tx + "," + ty);


            // test the contents
            Box retbox = null;
			bfc.translate(bx.tx,bx.ty);
            retbox = findElementBox(bx, tx, ty, bfc);
			bfc.translate(-bx.tx,-bx.ty);
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
            
			Uu.p("x = " + x + " y = " + y + " tty = " + tty + " bx.x = " + bx.x + " bx.y = " + bx.y);
			Uu.p("tx = " + tx + "," + ty);
			if(bfc != null) {
				tx = x - bfc.getX();
				ty = tty - bfc.getY();
			}
            Uu.p("tx = " + tx + "," + ty);
			//if (bx.contains(tx-bx.x,ty-bx.y)) {//x - bx.x, tty - bx.y)) {
			if(bx.contains(x-bx.x,tty-bx.y)) {
					//TODO: if this is InlineBox, we need to find the first previous sibling with a pushStyle
				Uu.p("found: " + bx);
                return bx;
            }
        }

        return null;
    }

    private static int[] adjustForAbsolute(Box bx, int tx, int ty, BlockFormattingContext bfc) {
		tx -= bfc.getX();
		ty -= bfc.getY();
        if (bfc != null) {
            if (bx.left_set) {
				Uu.p("bfc = " + bfc);// + " x,y = " + bfc.getX() + "," + bfc.getY());
				//Uu.p("bfc = " + bfc.hashCode());
				Uu.p(" insets = " + bfc.getInsets());
				//Uu.p(" padding = " + bfc.getPadding());
				Uu.p("tx = " + tx);
				//Uu.p("bx = " + bx);
				//tx -= (bfc.getInsets().left - bfc.getPadding().left);
		
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
