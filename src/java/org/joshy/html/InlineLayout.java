package org.joshy.html;

import java.util.List;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.LineMetrics;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.joshy.u;

import org.joshy.html.box.InlineBox;
import org.joshy.html.box.LineBox;
import org.joshy.html.box.*;
import org.joshy.html.Context;
import org.joshy.html.util.GraphicsUtil;
import org.joshy.html.util.TextUtil;
import org.joshy.html.util.FontUtil;
import org.joshy.html.util.InfiniteLoopError;
import org.joshy.html.util.InlineUtil;
import org.joshy.html.util.LineBreaker;
import org.joshy.html.painter.*;

public class InlineLayout extends BoxLayout {


public Box layoutChildren(Context c, Box box) {
    if(isHiddenNode(box.getElement(),c)) return box;
    //u.p("InlineLayout.layoutChildren(: " + box);
    //u.dump_stack();
    //u.p("parent box = " + c.parent_box);
    //u.p("placement point = " + c.placement_point);
    if(!box.isAnonymous()) {
        if(isBlockLayout(box.getElement(),c)) {
            return super.layoutChildren(c,box);
        }
    }

    int debug_counter = 0;
    int childcount = 0;
    BlockBox block = (BlockBox)box;

    // calculate the initial position and dimensions
    Rectangle bounds = new Rectangle();
    bounds.width = c.getExtents().width;
    //u.p("initial bounds width = " + bounds.width);
    bounds.width -= box.margin.left + box.border.left + box.padding.left +
        box.padding.right + box.border.right + box.margin.right;
    //u.p("initial bounds width = " + bounds.width);
    bounds.x = 0;
    bounds.y = 0;
    bounds.height = 0;
    int remaining_width = bounds.width;

    // account for text-indent
    LineBox curr_line = new LineBox();
    curr_line.x = bounds.x;
    //curr_line.width = remaining_width;
    curr_line.width = 0;
    Element elem = block.getElement();
    remaining_width = InlineUtil.doTextIndent(c,elem,remaining_width,curr_line);
    LineBox prev_line = new LineBox();
    prev_line.y = bounds.y;
    prev_line.height = 0;
    InlineBox prev_inline = null;
    
    //boolean adjusted_left_tab = false;
    if(c.getLeftTab().y > 0) {
        c.getLeftTab().y -= c.placement_point.y;
    }
    if(c.getRightTab().y > 0) {
        c.getRightTab().y -= c.placement_point.y;
    }
    if(c.getRightTab().y < 0) {
        c.getRightTab().y = 0;
    }
    if(c.getLeftTab().y < 0) {
        c.getLeftTab().y = 0;
    }

    InlineBox prev_align_inline = null;
    List inline_node_list = null;
    if(box.isAnonymous()) {
        inline_node_list = ((AnonymousBlockBox)box).node_list;
    } else {
        inline_node_list = InlineUtil.getInlineNodeList(elem,elem,c);
    }

    // loop until no more nodes
    Node current_node = InlineUtil.nextTextNode(inline_node_list);
    TextUtil.stripWhitespace(c,current_node,elem);

    // ajdust the first line for tabs
    remaining_width = adjustForTab(c, prev_line, remaining_width);

    while(current_node != null) {
        // loop until no more text in this node
        while(true) {
            //u.p("loop start bounds width = " + bounds.width);
            //u.p("prev inline = " + prev_inline);
            //u.p("current node = " + current_node);
            if(bounds.width < 0) {
                u.p("bounds width = " + bounds.width);
                System.exit(-1);
            }
            // test if there is no more text in the current text node
            // if there is a prev, and if the prev was part of this current node
            if(prev_inline != null && prev_inline.node == current_node) {
                //u.p("prev inline = " + prev_inline);
                //u.p("current text len = " + current_node.getNodeValue().length());
                // replaced elements aren't split, so done with this one
                if(isReplaced(current_node)) {
                    //u.p("it's replaced. breaking");
                    break;
                }
                if(isFloatedBlock(current_node,c)) {
                    break;
                }
                if(InlineUtil.isBreak(current_node)) {
                    break;
                }
                // if no more unused text in this node
                if(prev_inline.end_index >= current_node.getNodeValue().length()) {
                    // then break
                    //u.p("used up all of the text. breaking");
                    break;
                }
            }
            if(bounds.width < 10) {
                u.p("warning. width < 10 " + bounds.width);
            }
            debug_counter++;
            final int limit = 140;
            if(debug_counter > limit && bounds.width < 10) {
                u.on();
                u.p("previous inline = " + prev_inline);
                u.p("current line = " + curr_line);
                u.p("lines = ");
                //u.p(block.boxes);
                u.p("current node = " + current_node + " text= " + current_node.getNodeValue());
                u.p("rem width = " + remaining_width + " width " + bounds.width);
            }
            if(debug_counter > limit+3 && bounds.width < 10) {
                u.p("element = " + elem);
                org.joshy.x.p(elem);
                u.p("previous inline = " + prev_inline);
                u.p("current inline = " + curr_line);
                u.p("lines = ");
                //u.p(block.boxes);
                u.p("db 1 hit");
                System.exit(-1);
                throw new InfiniteLoopError("Infinite loop detected in InlineLayout");
            }
            // look at current inline
            // break off the longest section that will fit
            //u.p("looking for another inline from the text: " + current_node.getNodeValue());
            //u.p("remaining width = " + remaining_width + " bounds.width = " + bounds.width);
            InlineBox new_inline = this.calculateInline(c,current_node,remaining_width,bounds.width,
                curr_line, prev_inline, elem, prev_align_inline);
            //u.p("new inline box: " + new_inline);

            // if this inline needs to be on a new line
            if(new_inline.break_before && !new_inline.floated) {
                // finish up the current line
                //u.p("is break before and not floated");
                remaining_width = bounds.width;
                saveLine(curr_line, prev_line, elem, bounds.width, bounds.x, c, block);
                bounds.height += curr_line.height;
                prev_line = curr_line;
                curr_line = new LineBox();
                curr_line.x = bounds.x;
                // adjust remaining width for floats
                //u.p("left tab = " + left_tab + " right tab = " + c.getRightTab());
                //if(c.getLeftTab().y > 0) {
                remaining_width = adjustForTab(c, prev_line, remaining_width);
                //curr_line.width = remaining_width;
                curr_line.width = 0;
            }

            // save the new inline to the list
            curr_line.addChild(new_inline);
            //u.p("added new_inline: " + new_inline);

            // calc new height of the line
            // don't count the inline towards the line height and
            //line baseline if it's a floating inline.
            if(!isFloated(new_inline,c)) {
                if(!this.isFloatedBlock(new_inline.node,c)) {
                    //u.p("calcing new height of line");
                    if(new_inline.height + new_inline.y > curr_line.height) {
                        curr_line.height = new_inline.height + new_inline.y;
                    }
                    if(new_inline.baseline > curr_line.baseline) {
                        curr_line.baseline = new_inline.baseline;
                    }
                }
            }

            InlineUtil.handleFloated(c, new_inline, curr_line, bounds.width, elem);

            // calc new width of the line
            curr_line.width += new_inline.width;
            // reduce the available width
            remaining_width = remaining_width - new_inline.width;
            // if the last inline was at the end of a line, then go to next line
            if(new_inline.break_after) {
                //u.p("is break after. doing a new line");
                // then remaining_width = max_width
                //u.p("remaining width = " + remaining_width);
                remaining_width = bounds.width;
                //u.p("remaining width = " + remaining_width);
                //u.p("width = " + width);
                // save the line
                //u.p("curr line = " + curr_line);
                saveLine(curr_line, prev_line,elem,bounds.width,bounds.x,c,block);
                // increase bounds height to account for the new line
                bounds.height += curr_line.height;
                //u.p("saved line: " + curr_line);
                prev_line = curr_line;
                curr_line = new LineBox();
                curr_line.x = bounds.x;
                // adjust remaining width for floats
                //u.p("rem width = " + remaining_width);
                //if(c.getLeftTab().y > 0) {
                remaining_width = adjustForTab(c, prev_line, remaining_width);
                //u.p("remaining width = " + remaining_width);
                //curr_line.width = remaining_width;
                curr_line.width = 0;
                //u.p("now rem width = " + remaining_width);
            }

            // set the inline to use for left alignment
            if(!isFloated(new_inline,c)) {
                prev_align_inline = new_inline;
            } else {
                prev_align_inline = prev_inline;
            }
            prev_inline = new_inline;
        }
        current_node = InlineUtil.nextTextNode(inline_node_list);
        TextUtil.stripWhitespace(c,current_node,elem);
    }

    saveLine(curr_line,prev_line,elem,bounds.width,bounds.x,c,block);
    //u.p("saving line 2: " + curr_line);
    bounds.height += curr_line.height;

    block.width = bounds.width;
    block.height = bounds.height;
    block.x = 0;
    block.y = 0;
    //u.p("returning final block of: " + box);
    //u.p("last tab = " + c.getLeftTab());
    //if(adjusted_left_tab) {
    c.getLeftTab().y += c.placement_point.y;
    c.getRightTab().y += c.placement_point.y;
    //}
    //c.setLeftTab(old_left_tab);
    //u.p("final tabl = " + c.getLeftTab());
    //u.p("debug counter = " + debug_counter);
    return block;
}


private int adjustForTab(Context c, LineBox prev_line, int remaining_width) {
    if(prev_line.y < c.getLeftTab().y) {
        remaining_width -= c.getLeftTab().x;
        //u.p("substracting off : " + c.getLeftTab().x);
    }
    if(prev_line.y + prev_line.height < c.getRightTab().y) {
        remaining_width -= c.getRightTab().x;
    //u.p("rem width = " + remaining_width);
    }
    return remaining_width;
}



// get longest inline possible
private InlineBox calculateInline(Context c, Node node, int avail, int max_width,
     LineBox line, InlineBox prev, Element containing_block, InlineBox prev_align) {
     //u.p("line calc with avail = " + avail);
    // calculate the starting index
    int start = 0;
    if(prev != null && prev.node == node) {
        start = prev.end_index;
    }

    // get the text of the node
    String text = node.getNodeValue();
    //u.p("calc inline for node: " + node);
    //u.p("text = " + text);

    // transform the text if required (like converting to caps)
    // this must be done before any measuring since it might change the
    // size of the text
    text = TextUtil.transformText(c,node,text);
    //u.p("text = " + text);

    Font font = FontUtil.getFont(c,node);
    if(isReplaced(node)) {
        //u.p("is replaced");
        return LineBreaker.generateReplacedInlineBox(c,node,avail,prev, text,prev_align,font);
    }
    if(isFloatedBlock(node,c)) {
        //u.p("is floated");
        return LineBreaker.generateFloatedBlockInlineBox(c,node,avail,prev, text,prev_align,font);
    }
    if(InlineUtil.isBreak(node)) {
        //u.p("is break");
        return LineBreaker.generateBreakInlineBox(node);
    }
    if(LineBreaker.isWhitespace(c,containing_block)) {
        //u.p("is whitespace");
        return LineBreaker.generateWhitespaceInlineBox(c,node,start,prev,text,prev_align,font);
    }
    // ==== unbreakable long word =====
    if(LineBreaker.isUnbreakableLine(c,node,start,text,avail,font)) {
        //u.p("is unbreakable");
        //u.p("node = " + node);
        //u.p("start = " + start);
        //u.p("text = " + text);
        //u.p("avail = " + avail);
        //u.p("font = " + font);
        return LineBreaker.generateUnbreakableInlineBox(c,node,start,text,prev,prev_align,font);
    }
    // rest of this string can fit on the line
    if(LineBreaker.canFitOnLine(c,node,start,text,avail,font)) {
        //u.p("can fit on line");
        return LineBreaker.generateRestOfTextNodeInlineBox(c,node,start,text,prev,prev_align,font);
    }
    // normal multiline break
    //u.p("is normal multiline break");
    return LineBreaker.generateMultilineBreak(c,node,start,text,prev,prev_align,avail);
}




private void saveLine(LineBox line_to_save, LineBox prev_line, Element containing_block, int width, int x,
    Context c,  BlockBox block) {


    //line_to_save.x = x;

    // account for text-align
    String text_align = c.css.getStringProperty(containing_block,"text-align",true);
    //u.p("text-align = " + text_align);
    if(text_align != null) {
        if(text_align.equals("right")) {
            //u.p("initial line: " + line_to_save);
            //u.p("x = " + x + " width = " + width);
            line_to_save.x = x + width - line_to_save.width;
            //line_to_save.x = x + line_to_save.width - width;
            //u.p("saved line: " + line_to_save);
        }
        if(text_align.equals("center")) {
            line_to_save.x = x + (width - line_to_save.width)/2;
        }
    }

    // set the y
    line_to_save.y = prev_line.y + prev_line.height;

    //if(c.getLeftTab().y >0) {
    //u.p("line to save y = " + line_to_save.y);
    //u.p("left tab y = " + c.getLeftTab().y);
    if(line_to_save.y < c.getLeftTab().y) {
        line_to_save.x+= c.getLeftTab().x;
    //}
    //if(c.getLeftTab().y > 0) {
        //c.getLeftTab().y -= line_to_save.height;
    }
    //if(c.getLeftTab().y <= 0) {
        //c.getLeftTab().x = 0;
    //}

    if(c.getRightTab().y >0) {
        //line_to_save.x+= c.getRightTab().x;
    //}
    //if(c.getRightTab().y > 0) {
        //c.getRightTab().y -= line_to_save.height;
    }
    //if(c.getRightTab().y <= 0) {
        //c.getRightTab().x = 0;
    //}

    FontUtil.setupVerticalAlign(c,containing_block,line_to_save);
    block.addChild(line_to_save);
    //u.p("final saved line = " + line_to_save);
    //u.p("block = " + block.hashCode());
}


public void paintComponent(Context c, Box box) {
    //u.p("InlineLayout.paintComponent() " + box);
    //u.dump_stack();
    if(box.isAnonymous()) {
        //u.p("InlineLayout.paintComponent() : " + box);
        InlinePainter.paintInlineContext(c,box);
        return;
    }
    if(this.isBlockLayout(box.getElement(),c)) {
        //u.p("InlineLayout.paintComponent is block context: " + box);
        super.paintComponent(c,box);
        return;
    }
    //u.p("InlineLayout.paintComponent()" + box);
    InlinePainter.paintInlineContext(c,box);
}


public void paintChildren(Context c, Box box) {
    //u.p("InlineLayout.paintChildren() " + box);
    if(box.isAnonymous()) {
        //u.p("it's anonymous so no children");
        return;
    }
    if(this.isBlockLayout(box.getElement(),c)) {
        //u.p("is block. doing super");
        super.paintChildren(c,box);
    }
}

}

