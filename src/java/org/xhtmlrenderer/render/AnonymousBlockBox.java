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
package org.xhtmlrenderer.render;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.InlineUtil;

import java.util.List;

/**
 * Description of the Class
 *
 * @author empty
 */
public class AnonymousBlockBox extends BlockBox {

    /**
     * Description of the Field
     */
    public List node_list;

    /**
     * Description of the Field
     */
    public Node last_node;


    /**
     * Constructor for the AnonymousBlockBox object
     *
     * @param startNode PARAM
     * @param c         PARAM
     */
    public AnonymousBlockBox(Node startNode, Context c) {

        this.setNode(startNode.getParentNode());

        node_list = InlineUtil.getInlineNodeList(startNode, (Element) this.getNode(), c, true);

        node_list.add(0, startNode);

        last_node = (Node) node_list.get(node_list.size() - 1);

        /*
         * while(true) {
         * Node sibling = startNode.getNextSibling();
         * if(sibling == null) { break; }
         * if(Layout.isBlockNode(sibling,c)) { break; }
         * u.p("adding sibiling: " + sibling);
         * node_list.add(sibling);
         * startNode = sibling;
         * }
         */

    }


    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append("AnonymousBlockBox:");

        sb.append(super.toString());

        return sb.toString();
    }


    /**
     * Gets the anonymous attribute of the AnonymousBlockBox object
     *
     * @return The anonymous value
     */
    public boolean isAnonymous() {

        return true;
    }


}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2004/12/05 18:11:38  tobega
 * Now uses style cache for pseudo-element styles. Also started preparing to replace inline node handling with inline content handling.
 *
 * Revision 1.4  2004/12/05 00:48:58  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.3  2004/10/23 13:50:26  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

