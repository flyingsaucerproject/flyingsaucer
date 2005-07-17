package org.xhtmlrenderer.layout;



import org.xhtmlrenderer.util.Uu;
import java.awt.Rectangle;
import java.awt.Point;
import java.util.List;
import org.xhtmlrenderer.render.Box;

public class FloatManager {
	
	/* The purpose of get float distance is to figure out how far to the left or
	right (the x offset) you will need to position the target box so that it
	will not overlap with any floats currently in the BFC. This is called from
	the BFC via the getleftdistance and getrightdistance methods.
	
		
		notes: first of all, are we only dealing with lines or other boxes.
		we are making the assumption that the box has valid dimensions. it looks like
		line boxes are being passed in when they don't have a height yet. the default height
		should be the line-height, i'm guessing, and then we can use that for the
		determination.
	*/

    public static int getFloatDistance(Box line, List floatsList, BlockFormattingContext bfc) {
		//Uu.p("---\n---\ngetting the float distance for line: " + line + " " + line.element);
		//Uu.p(floatsList);
		//Uu.dump_stack();

		// if
        if (floatsList.size() == 0) {
			//Uu.p("returning nada");
            return 0;
        }

        int xoff = 0;
		// create a rectangle for the line we are attempting to adjust
		Rectangle lr = new Rectangle(line.x,line.y,line.width,line.height);
		
		// this is a hack to deal with lines w/o width or height. is this valid?
		// possibly, since the line doesn't know how long it should be until it's already
		// done float adjustments
		if(line.width == 0) {
			lr.width = 10;
		}
		if(line.height == 0) {
			lr.height = 10;
		}
		//Uu.p("line rect = " + lr);
		Point lpt = new Point(bfc.x, bfc.y);
		//Uu.p("trans by: " + lpt);
		// convert to abs coords
		lr.translate(-lpt.x,-lpt.y);
		//Uu.p("line rect = " + lr);
		// josh: note. this code doesn't handle floats on the same line!
		// loop through all of the floats
        for (int i = 0; i < floatsList.size(); i++) {
			// get the current float
            Box floater = (Box) floatsList.get(i);
			//Uu.p("the floater = " + floater);
			// create a rect from the box
			Rectangle fr = new Rectangle(floater.x,floater.y,floater.width,floater.height);
			//Uu.p("float rect: " + fr);
			// get the point where the float was added
            Point fpt = bfc.getOffset(floater);
			// get the origin of this BFC
			//Uu.p("fpt = " + fpt + " lpt = " + lpt);
			// convert to abs coords
			fr.translate(-fpt.x,-fpt.y);
			//Uu.p("float rect: " + fr);
			// if the line is lower than bottom of the floater
			// josh: is this calc right? shouldn't floater.y be in there somewhere?
			if(lr.intersects(fr)) {
				//Uu.p("it intersects!");
				lr.translate(fr.width,0);
				xoff+=fr.width;
				//Uu.p("new lr = " + lr);
			}
			//Uu.p("xoff = " + xoff);
        }
		
		//Uu.p("final val = " + xoff);
        return xoff;
    }
	
	/* dummy code to copy from

        // count backwards through the floats
        int x = 0;
        for (int i = left_floats.size() - 1; i >= 0; i--) {
            Box floater = (Box) left_floats.get(i);
            // Uu.p("box = " + box);
            // Uu.p("testing against float = " + floater);
            x = floater.x + floater.width;
            if (floater.y + floater.height > box.y) {
                // Uu.p("float["+i+"] blocks the box vertically");
                return floater;
            } else {
                // Uu.p("float["+i+"] doesn't block. moving to next");
            }
        }
	*/

	
	
	/*
		this method is called (currently) by the float util to set up the
		float itself. it should return the right most box on the left
	*/
	public static Box newGetLeftFloatX(Box box, List left_floats, BlockFormattingContext bfc) {
		//Uu.p("in new bfc.getLeftFloatX( " + box + " ) ");
        // count backwards through the floats
        //int x = 0;
        for (int i = left_floats.size() - 1; i >= 0; i--) {
            Box floater = (Box) left_floats.get(i);
            //Uu.p("box = " + box);
			//Uu.p("content = " + box.element);
            //Uu.p("testing against float = " + floater + " " + floater.hashCode());
			//Uu.p("content = " + floater.element);

			Rectangle fr = new Rectangle(floater.x,floater.y,floater.width,floater.height);
			//Uu.p("float rect: " + fr);
			// get the point where the float was added
            Point fpt = bfc.getOffset(floater);
			// get the origin of this BFC
			//Uu.p("fpt = " + fpt);// + " lpt = " + lpt);
			// convert to abs coords
			fr.translate(fpt.x,fpt.y);
			//Uu.p("float rect: " + fr);


            //x = floater.x + floater.width;
			// skip if the box and the floater have the same element. this means they are really the same box
			// this is a hack to account for when the same float is run through twice. i don't know
			// why this is happening. hopefully we can fix it in the future.
			if(floater.element == box.element) {
				continue;
			}
            if (fr.y + fr.height > box.y) {
                //Uu.p("float["+i+"] blocks the box vertically");
                return floater;
            } else {
                //Uu.p("float["+i+"] doesn't block. moving to next");
            }
        }
		//Uu.p("returning null");
        return null;
    }

}
