/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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
package org.xhtmlrenderer.layout;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.u;

/**
 * 
 *
 * @author   joshy
 */
public class InlineUtil {

    /**
     * Description of the Method
     *
     * @param c           PARAM
     * @param elem        PARAM
     * @param width       PARAM
     * @param first_line  PARAM
     * @return            Returns
     */
    public static int doTextIndent( Context c, Element elem, int width, LineBox first_line ) {
        if ( c.css.hasProperty( elem, CSSName.TEXT_INDENT ) ) {
            float indent = c.css.getFloatProperty( elem, CSSName.TEXT_INDENT, width );
            width = width - (int)indent;
            first_line.x = first_line.x + (int)indent;
        }
        return width;
    }




    /**
     * Description of the Method
     *
     * @param node_list  PARAM
     * @return           Returns
     */
    public static Node nextTextNode( List node_list ) {
        if ( node_list.size() < 1 ) {
            return null;
        }
        Node nd = (Node)node_list.get( 0 );
        node_list.remove( nd );
        return nd;
    }


    /**
     * Gets the inlineNodeList attribute of the InlineUtil class
     *
     * @param node  PARAM
     * @param elem  PARAM
     * @param c     PARAM
     * @return      The inlineNodeList value
     */
    public static List getInlineNodeList( Node node, Element elem, Context c ) {
        return getInlineNodeList( node, elem, c, false );
    }

    /**
     * Gets the inlineNodeList attribute of the InlineUtil class
     *
     * @param node            PARAM
     * @param elem            PARAM
     * @param c               PARAM
     * @param stop_at_blocks  PARAM
     * @return                The inlineNodeList value
     */
    public static List getInlineNodeList( Node node, Element elem, Context c, boolean stop_at_blocks ) {
        List list = new ArrayList();
        if ( node == null ) {
            return list;
        }
        if ( elem == null ) {
            return list;
        }
        if ( !elem.hasChildNodes() ) {
            //u.p("it's empty");
            return list;
        }

        //u.p("starting at: " + node);
        Node curr = node;
        while ( true ) {
            //u.p("now list = " + list);
            // skip the first time through
            if ( curr != node ) {
                if ( curr.getNodeType() == curr.TEXT_NODE ) {
                    //u.p("adding: " + curr);
                    list.add( curr );
                    node = curr;
                    continue;
                    //return curr;
                }

                if ( LayoutUtil.isReplaced(c, curr ) ) {
                    //u.p("adding: " + curr);
                    list.add( curr );
                    node = curr;
                    continue;
                    //return curr;
                }

                if ( LayoutUtil.isFloatedBlock( curr, c ) ) {
                    //u.p("adding: " + curr);
                    list.add( curr );
                    node = curr;
                    continue;
                    //return curr;
                }

                if ( c.getRenderingContext().getLayoutFactory().isBreak( curr ) ) {
                    //u.p("adding: " + curr);
                    list.add( curr );
                    node = curr;
                    continue;
                    //return curr;
                }

                if ( stop_at_blocks ) {
                    if ( LayoutUtil.isBlockNode( curr, c ) ) {
                        //u.p("at block boundary");
                        return list;
                    }
                }
            }

            if ( curr.hasChildNodes() ) {
                //u.p("about to test: " + curr);
                // if it's a floating block we don't want to recurse
                if ( !LayoutUtil.isFloatedBlock( curr, c ) &&
                        !LayoutUtil.isReplaced(c, curr ) ) {
                    curr = curr.getFirstChild();
                    //u.p("going to first child " + curr);
                    continue;
                }

                // it's okay to recurse if it's the root that's the float,
                // not the node being examined. this only matters when we
                // start the loop at the root of a floated block
                if ( LayoutUtil.isFloatedBlock( node, c ) ) {
                    if ( node == elem ) {
                        curr = curr.getFirstChild();
                        continue;
                    }
                }
            }

            if ( curr.getNextSibling() != null ) {
                curr = curr.getNextSibling();
                //u.p("going to next sibling: " + curr);
                continue;
            }

            // keep going up until we get another sibling
            // or we are at elem.

            while ( true ) {
                curr = curr.getParentNode();
                //u.p("going to parent: " + curr);
                // if we are at the top then return null
                if ( curr == elem ) {
                    //u.p("at the top again. returning null");
                    //u.p("returning the list");
                    //u.p(list);
                    return list;
                    //return null;
                }

                if ( curr.getNextSibling() != null ) {
                    curr = curr.getNextSibling();
                    //u.p("going to next sibling: " + curr);
                    break;
                }

            }

        }

    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.12  2004/11/18 14:26:22  joshy
 * more code cleanup
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/18 02:37:26  joshy
 * moved most of default layout into layout util or box layout
 *
 * start spliting parts of box layout into the block subpackage
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/14 16:40:58  joshy
 * refactored layout factory
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/09 16:24:30  joshy
 * moved float code into separate class
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/08 20:50:59  joshy
 * improved float support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/11/06 01:50:41  joshy
 * support for line-height
 * cleaned up the alice demo
 * added unit tests for font family selection and line-height
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/04 15:35:45  joshy
 * initial float support
 * includes right and left float
 * cannot have more than one float per line per side
 * floats do not extend beyond enclosing block
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/10/28 13:46:32  joshy
 * removed dead code
 * moved code about specific elements to the layout factory (link and br)
 * fixed form rendering bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

