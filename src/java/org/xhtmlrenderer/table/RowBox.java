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

import org.w3c.dom.Element;
import org.xhtmlrenderer.render.BlockBox;

import java.util.ArrayList;
import java.util.List;


/**
 * Description of the Class
 *
 * @author empty
 */
public class RowBox extends BlockBox {

    /**
     * Description of the Field
     */
    public List cells = new ArrayList();
    
    private int containingBlockWidth;

    /**
     * Description of the Field
     */
    public Element elem;
    
    public int getContainingBlockWidth() {
        return containingBlockWidth;
    }

    public void setContainingBlockWidth(int containingBlockWidth) {
        this.containingBlockWidth = containingBlockWidth;
    }    

    public RowBox() {
    }
}

/*
 * $Id$
 * $Log$
 * Revision 1.6  2006/09/01 23:49:37  peterbrant
 * Implement basic margin collapsing / Various refactorings in preparation for shrink-to-fit / Add hack to treat auto margins as zero
 *
 * Revision 1.5  2005/10/06 03:20:24  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.4  2005/06/05 01:02:35  tobega
 * Very simple and not completely functional table layout
 *
 * Revision 1.3  2005/01/29 20:19:24  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.2  2004/10/23 13:59:17  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 */

