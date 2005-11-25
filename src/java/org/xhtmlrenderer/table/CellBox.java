/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.table;

import java.util.Iterator;

import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;


/**
 * Description of the Class
 *
 * @author empty
 */
public class CellBox extends BlockBox {

    /**
     * Description of the Field
     */
    public Box sub_box;

    /**
     * Description of the Field
     */
    public RowBox rb;

    /**
     * Description of the Field
     */
    private boolean virtual = false;

    /**
     * Description of the Field
     */
    private CellBox real_box = null;
    public int colspan;
    public int rowspan;

    public CellBox() {
    }


    /**
     * Gets the real attribute of the CellBox object
     *
     * @return The real value
     */
    public boolean isReal() {

        return !virtual;
    }

    /**
     * Gets the real attribute of the CellBox object
     *
     * @return The real value
     */
    public CellBox getReal() {

        return real_box;
    }
    
    public void alignLines() {
    	// FIXME Should recurse into inline-block content, but don't
    	// bother for now.  shrinkWrap property on LayoutContext should
    	// be a stack (or implicitly treated that way)
    	
    	alignAllLinesHelper(this);
    }
    
    private void alignAllLinesHelper(Box b) {
    	for (Iterator i = b.getChildIterator(); i.hasNext(); ) {
    		Box box = (Box)i.next();
    		if (box instanceof LineBox) {
    			((LineBox)box).align();
    			((LineBox)box).setFloatDistances(null);
    		} else {
    			alignAllLinesHelper(box);
    		}
    	}
    }

}

/*
 * $Id$
 * $Log$
 * Revision 1.10  2005/11/25 22:42:07  peterbrant
 * Wait until table has completed layout before doing line alignment
 *
 * Revision 1.9  2005/10/06 03:20:24  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.8  2005/08/03 21:44:00  tobega
 * Now support rowspan
 *
 * Revision 1.7  2005/07/02 12:25:44  tobega
 * colspan is working!
 *
 * Revision 1.6  2005/06/05 01:02:35  tobega
 * Very simple and not completely functional table layout
 *
 * Revision 1.5  2005/01/29 20:22:26  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.4  2004/12/12 03:33:03  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 * Revision 1.3  2004/10/23 13:59:17  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 */

