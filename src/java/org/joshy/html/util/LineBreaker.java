package org.joshy.html.util;

import org.w3c.dom.*;
import org.joshy.html.box.*;
import org.joshy.u;
import org.joshy.html.*;
import java.awt.Rectangle;



public class LineBreaker {
public static InlineBox generateMultilineBreak(Context c, Node node, int start, String text,
    InlineBox prev, InlineBox prev_align, int avail) {
    //u.p("normal breaking");
    // calc end index to most words that will fit
    int end = start;
    int dbcount = 0;
    while(true) {
        dbcount++;
        //u.off();
        u.on();
        if(dbcount > 50) {
            u.on();
        }
        if(dbcount>100) {
            u.on();
            u.p("db 2 hit");
            u.p("text = " + text);
            u.p("end = " + end);
            System.exit(-1);
        }

        //u.p("end = " + end);
        int next_space = text.indexOf(" ",end);
        if(next_space == -1) { next_space = text.length(); }
        //u.p("next space = " + next_space);
        try {
            //u.p("end = " + end + " next space = " + next_space + " text = " + text.substring(end,next_space));
        } catch (Exception ex) {
            u.p(ex);
            System.exit(-1);
        }
        int len2 = FontUtil.len(c,node,text.substring(start,next_space));
        //u.p("len2 = " + len2 + " avail = " + avail);
        // if this won't fit, then break and use the previous span
        if(len2 > avail) {
            InlineBox box = newBox(c, node, start, end, prev, text, prev_align);
            //u.p("normal break returning span: " + box);
            return box;
        }
        // if this will fit but we are at the end then break and use current span
        if(next_space == text.length()) {
            InlineBox box = newBox(c, node, start, next_space, prev, text, prev_align);
            //u.p("normal break returning curr span: " + box);
            return box;
        }
        // skip over the space
        end = next_space + 1;
    }
}

public static boolean canFitOnLine(Context c, Node node, int start, String text, int avail) {
    // if the rest of text can fit on the current line
    // if length of remaining text < available width
    //u.p("avail = " + avail + " len = " + FontUtil.len(c,node,text.substring(start)));
    if(FontUtil.len(c,node,text.substring(start)) < avail) {
        return true;
    } else {
        return false;
    }
}
public static InlineBox generateRestOfTextNodeInlineBox(Context c, Node node, int start, String text,
    InlineBox prev, InlineBox prev_align) {
        InlineBox box = newBox(c,node,start,text.length(),prev,text, prev_align);
        // turn off breaking since more might fit on this line
        box.break_after = false;
        //u.p("fits on line returning : " + box);
        return box;
}

public static boolean isUnbreakableLine(Context c, Node node, int start, String text, int avail) {
    int first_word_index = text.indexOf(" ",start);
    if(first_word_index == -1) {
        first_word_index = text.length();
    }
    String first_word = text.substring(start,first_word_index);
    first_word = first_word.trim();
    if(avail < FontUtil.len(c, node, first_word)) {
        return true;
    } else {
        return false;
    }
}


public static InlineBox generateUnbreakableInlineBox(Context c, Node node, int start, String text,  InlineBox prev, InlineBox prev_align) {
    int first_word_index = text.indexOf(" ",start);
    if(first_word_index == -1) {
        first_word_index = text.length();
    }
    String first_word = text.substring(start,first_word_index);
    first_word = first_word.trim();
    InlineBox box = newBox(c, node, start, first_word_index, prev, text, prev_align);
    // move back to the left margin since this is on it's own line
    box.x = 0;
    box.break_before = true;
    //u.p("unbreakable long word returning: " + box);
    box.break_after = true;
    return box;
}

public static boolean isWhitespace(Context c, Element containing_block) {
    String white_space = c.css.getStringProperty(containing_block,"white-space");
    // if doing preformatted whitespace
    if(white_space!=null && white_space.equals("pre")) {
        return true;
    } else {
        return false;
    }
}

public static InlineBox generateWhitespaceInlineBox(Context c, Node node, int start,
    InlineBox prev, String text, InlineBox prev_align) {
        //u.p("preformatted text");
        int cr_index = text.indexOf("\n",start+1);
        //u.p("cr_index = " + cr_index);
        if(cr_index == -1) {
            cr_index = text.length();
        }
        InlineBox box = newBox(c,node,start,cr_index,prev,text, prev_align);
        return box;
}

public static InlineBox generateBreakInlineBox(Node node) {
    InlineBox box = new InlineBox();
    box.node = node;
    box.width = 0;
    box.height = 0;
    box.break_after = true;
    box.x = 0;
    box.y = 0;
    box.is_break = true;
    return box;
}

// change this to use the existing block instead of a new one
public static InlineBox generateFloatedBlockInlineBox(Context c, Node node, int avail, InlineBox prev, String text, InlineBox prev_align) {
    Layout layout = LayoutFactory.getLayout(node);
    Rectangle oe = c.getExtents();
    c.setExtents(new Rectangle(oe));
    BlockBox block = (BlockBox) layout.layout(c,(Element)node);
    Rectangle bounds = new Rectangle(block.x,block.y,block.width,block.height);
    c.setExtents(oe);
    InlineBox box = newBox(c,node,0,0,prev,text,bounds,prev_align);
    box.sub_block = block;
    box.width = bounds.width;
    box.height = bounds.height;
    box.break_after = false;
    if(box.width > avail) {
        box.break_before = true;
        box.x = 0;
    }
    return box;
}

    // change this to use the existing block instead of a new one
public static InlineBox generateReplacedInlineBox(Context c, Node node, int avail, InlineBox prev, String text, InlineBox prev_align) {
    //u.p("replaced element");
    // get the layout for the replaced element
    Layout layout = LayoutFactory.getLayout(node);
    BlockBox block = (BlockBox)layout.layout(c,(Element)node);
    Rectangle bounds = new Rectangle(block.x,block.y,block.width,block.height);
    /* joshy: change this to just modify the existing block instead of creating
    a  new one*/
    // create new inline
    InlineBox box = newBox(c,node,0,0,prev,text, bounds, prev_align);
    //joshy: activate this: box.block = block

    // set up the extents
    box.width = bounds.width;
    box.height = bounds.height;
    box.break_after = false;
    // if it won't fit on this line, then put it on the next one
    if(box.width > avail) {
        box.break_before = true;
        box.x = 0;
    }
    // return
    //u.p("last replaced = " + box);
    return box;
}

private static InlineBox newBox(Context c, Node node,int start, int end, InlineBox prev, String text, InlineBox prev_align) {
    return newBox(c,node,start,end,prev,text,null, prev_align);
}

private static InlineBox newBox(Context c, Node node,int start, int end, InlineBox prev, String text, Rectangle bounds, InlineBox prev_align) {
    //u.p("newBox node = " + node.getNodeName() + " start = " + start + " end = " + end +
    //" prev = " + prev + " text = " + text + " bounds = " + bounds + " prev_align = " + prev_align);
    //u.p("Making box for: "  + node);
    //u.p("prev = " + prev);
     if(prev_align != prev) {
         //u.p("prev = " + prev);
         //u.p("prev align inline = " + prev_align);
     }
    InlineBox box = new InlineBox();
    box.node = node;
    box.start_index = start;
    box.end_index = end;
    /*
    if(prev!= null && !prev.break_after) {
        box.x = prev.x + prev.width;
    } else {
        box.x = 0;
    }
    */

    // use the prev_align to calculate the x
    if(prev_align!= null && !prev_align.break_after) {
        //u.p("moving over w/ prev = " + prev);
        //u.p("moving over w/ prev align = " + prev_align);
        box.x = prev_align.x + prev_align.width;
    } else {
        //u.p("setting x to 0");
        box.x = 0;
    }

    box.y = 0; // it's relative to the line
    try {
        if(!InlineLayout.isReplaced(node)) {
            if(!InlineLayout.isFloatedBlock(node,c)) {
                box.width = FontUtil.len(c,node,text.substring(start,end));
            } else {
                box.width = bounds.width;
            }
        } else {
            box.width = bounds.width;
        }
    } catch (StringIndexOutOfBoundsException ex) {
        u.p("ex");
        u.p("start = " + start);
        u.p("end = " + end);
        u.p("text = " + node.getNodeValue());
        throw ex;
    }
    //u.p("box.x = " + box.x);
    if(InlineLayout.isReplaced(node)) {
        box.height = bounds.height;
    } else if(InlineLayout.isFloatedBlock(node,c)) {
        box.height = bounds.height;
    } else {
        box.height = FontUtil.lineHeight(c,node);
    }
    //u.p("box.x = " + box.x);
    //box.baseline = box.height;
    box.break_after = true;

    box.text = text;
    if(!InlineLayout.isReplaced(node)) {
        if(!InlineLayout.isFloatedBlock(node,c)) {
            FontUtil.setupTextDecoration(c,node,box);
            if(box.text == null) {
                return box;
            }
        }
    }
    //u.p("box.x = " + box.x);

    // do vertical alignment
    //u.p("setting up vertical align on: " + node);
    FontUtil.setupVerticalAlign(c,node,box);
    box.setFont(FontUtil.getFont(c,node));
    if(node.getNodeType()== node.TEXT_NODE) {
        box.color = c.css.getColor((Element)node.getParentNode(),true);
    } else {
        box.color = c.css.getColor((Element)node,true);
    }
    InlineLayout.setupRelative(c,box);

    //u.p("box.x = " + box.x);
    //u.p("returning box: " + box);
    //u.p("colo r= " + box.color);
    return box;
}

}
