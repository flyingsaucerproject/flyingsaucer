package org.joshy.html.box;

import org.w3c.dom.*;
import org.joshy.html.*;
import java.util.ArrayList;
import java.util.List;
import org.joshy.u;
import org.joshy.html.util.InlineUtil;

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
