
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

import org.w3c.dom.*;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.layout.*;
import java.util.ArrayList;
import java.util.List;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.layout.InlineUtil;

public class AnonymousBlockBox extends BlockBox {
    public List node_list;
    public Node last_node;

    public AnonymousBlockBox(Node startNode, Context c) {
        this.node = startNode.getParentNode();
        node_list = InlineUtil.getInlineNodeList(startNode,(Element)this.node,c,true);
        node_list.add(0,startNode);
        last_node = (Node)node_list.get(node_list.size()-1);
        /*
        while(true) {
            Node sibling = startNode.getNextSibling();
            if(sibling == null) { break; }
            if(Layout.isBlockNode(sibling,c)) { break; }
            u.p("adding sibiling: " + sibling);
            node_list.add(sibling);
            startNode = sibling;
        }
        */

    }

    public boolean isAnonymous() {
        return true;
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AnonymousBlockBox:");
        sb.append(super.toString());
        return sb.toString();
    }

}
