package org.joshy.html.util;

import org.joshy.u;
import java.util.List;
import java.util.ArrayList;
import java.awt.Point;
import org.joshy.html.*;
import org.w3c.dom.*;
import org.joshy.html.box.*;
public class InlineUtil {

public static int doTextIndent(Context c, Element elem, int width, LineBox first_line) {
    if(c.css.hasProperty(elem,"text-indent")) {
        float indent = c.css.getFloatProperty(elem,"text-indent",width);
        width = width - (int)indent;
        first_line.x = first_line.x + (int)indent;
    }
    return width;
}

public static void handleFloated(Context c, InlineBox inline, LineBox line, 
    int full_width, Element enclosing_block) {

    if(inline.node == enclosing_block) {
        return;
    }
    // we must make sure not to grab the float from the containing
    // block incase it is floated.
    if(inline.node.getNodeType() == inline.node.TEXT_NODE) {
        if(inline.node.getParentNode() == enclosing_block) {
            return;
        }
    }

    String float_val = c.css.getStringProperty(inline.node,"float",false);

    if(float_val == null) {
        float_val = "none";
    }
    if(float_val.equals("none")) {
        return;
    }
    if(float_val.equals("left")) {
        // move the inline to the left
        inline.x = 0-inline.width;
        // adjust the left tab
        c.getLeftTab().x = inline.width;
        c.getLeftTab().y += inline.height;
    }
    if(float_val.equals("right")) {
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

public static List getInlineNodeList(Node node, Element elem, Context c) {
    return getInlineNodeList(node,elem,c,false);
}
public static List getInlineNodeList(Node node, Element elem, Context c, boolean stop_at_blocks) {
    List list = new ArrayList();
    if(node == null) { return list; }
    if(elem == null) { return list; }
    if(!elem.hasChildNodes()) {
        //u.p("it's empty");
        return list;
    }
    
    //u.p("starting at: " + node);
    Node curr = node;
    while(true) {
        //u.p("now list = " + list);

        // skip the first time through
        if(curr != node) {
            if(curr.getNodeType() == curr.TEXT_NODE) {
                //u.p("adding: " + curr);
                list.add(curr);
                node = curr;
                continue;
                //return curr;
            }
            if(InlineLayout.isReplaced(curr)) {
                //u.p("adding: " + curr);
                list.add(curr);
                node = curr;
                continue;
                //return curr;
            }
            if(InlineLayout.isFloatedBlock(curr,c)) {
                //u.p("adding: " + curr);
                list.add(curr);
                node = curr;
                continue;
                //return curr;
            }
            if(isBreak(curr)) {
                //u.p("adding: " + curr);
                list.add(curr);
                node = curr;
                continue;
                //return curr;
            }
            if(stop_at_blocks) {
                if(InlineLayout.isBlockNode(curr,c)) {
                    //u.p("at block boundary");
                    return list;
                }
            }
        }

        if(curr.hasChildNodes()) {
            //u.p("about to test: " + curr);
            // if it's a floating block we don't want to recurse
            if(!InlineLayout.isFloatedBlock(curr,c)) {
                curr = curr.getFirstChild();
                //u.p("going to first child " + curr);
                continue;
            }
            // it's okay to recurse if it's the root that's the float,
            // not the node being examined. this only matters when we
            // start the loop at the root of a floated block
            if(InlineLayout.isFloatedBlock(node,c)) {
                if(node == elem) {
                    curr = curr.getFirstChild();
                    continue;
                }
            }
        }

        if(curr.getNextSibling() != null) {
            curr = curr.getNextSibling();
            //u.p("going to next sibling: " + curr);
            continue;
        }

        // keep going up until we get another sibling
        // or we are at elem.
        while(true) {
            curr = curr.getParentNode();
            //u.p("going to parent: " + curr);
            // if we are at the top then return null
            if(curr == elem) {
                //u.p("at the top again. returning null");
                //u.p("returning the list");
                //u.p(list);
                return list;
                //return null;
            }
            if(curr.getNextSibling() != null) {
                curr = curr.getNextSibling();
                //u.p("going to next sibling: " + curr);
                break;
            }
        }
    }

}


public static Node nextTextNode(List node_list) {
    if(node_list.size() < 1) {
        return null;
    }
    Node nd = (Node)node_list.get(0);
    node_list.remove(nd);
    return nd;
}


public static boolean isBreak(Node node) {
    if(node instanceof Element) {
        if(((Element)node).getNodeName().equals("br")) {
            return true;
        }
    }
    return false;
}


}
