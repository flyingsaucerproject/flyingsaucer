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

/**
 * Description of the Class
 *
 * @author   empty
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
     * @param c                PARAM
     * @param inline           PARAM
     * @param line             PARAM
     * @param full_width       PARAM
     * @param enclosing_block  PARAM
     */
    public static void handleFloated( Context c, InlineBox inline, LineBox line,
                                      int full_width, Element enclosing_block ) {

        if ( inline.node == enclosing_block ) {

            return;
        }

        // we must make sure not to grab the float from the containing

        // block incase it is floated.

        if ( inline.node.getNodeType() == inline.node.TEXT_NODE ) {

            if ( inline.node.getParentNode() == enclosing_block ) {

                return;
            }
        }

        String float_val = c.css.getStringProperty( inline.node, CSSName.FLOAT, false );

        if ( float_val == null ) {

            float_val = "none";

        }

        if ( float_val.equals( "none" ) ) {

            return;
        }

        if ( float_val.equals( "left" ) ) {

            // move the inline to the left

            inline.x = 0 - inline.width;

            // adjust the left tab

            c.getLeftTab().x = inline.width;

            c.getLeftTab().y += inline.height;

        }

        if ( float_val.equals( "right" ) ) {

            // move the inline to the right

            inline.x = full_width - inline.width;

            // adjust the right tab

            c.getRightTab().x = inline.width;

            c.getRightTab().y += inline.height;

        }

        // shrink the line width

        line.width = line.width - inline.width;

        // mark as floated

        inline.floated = true;

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

                if ( InlineLayout.isReplaced( curr ) ) {

                    //u.p("adding: " + curr);

                    list.add( curr );

                    node = curr;

                    continue;
                    //return curr;

                }

                if ( InlineLayout.isFloatedBlock( curr, c ) ) {

                    //u.p("adding: " + curr);

                    list.add( curr );

                    node = curr;

                    continue;
                    //return curr;

                }

                if ( isBreak( curr ) ) {

                    //u.p("adding: " + curr);

                    list.add( curr );

                    node = curr;

                    continue;
                    //return curr;

                }

                if ( stop_at_blocks ) {

                    if ( InlineLayout.isBlockNode( curr, c ) ) {

                        //u.p("at block boundary");

                        return list;
                    }
                }
            }

            if ( curr.hasChildNodes() ) {

                //u.p("about to test: " + curr);

                // if it's a floating block we don't want to recurse

                if ( !InlineLayout.isFloatedBlock( curr, c ) &&
                        !InlineLayout.isReplaced( curr ) ) {

                    curr = curr.getFirstChild();

                    //u.p("going to first child " + curr);

                    continue;
                }

                // it's okay to recurse if it's the root that's the float,

                // not the node being examined. this only matters when we

                // start the loop at the root of a floated block

                if ( InlineLayout.isFloatedBlock( node, c ) ) {

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


    /**
     * Gets the break attribute of the InlineUtil class
     *
     * @param node  PARAM
     * @return      The break value
     */
    public static boolean isBreak( Node node ) {

        if ( node instanceof Element ) {

            if ( ( (Element)node ).getNodeName().equals( "br" ) ) {

                return true;
            }
        }

        return false;
    }


}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

