package org.xhtmlrenderer.layout.inline;


import org.w3c.dom.*;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.layout.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Color;
import java.awt.*;
import java.util.regex.*;
import org.xhtmlrenderer.util.u;

public class WhitespaceStripper {
    final String space = " ";
    // update this to work on linefeeds on multiple platforms;
    final Pattern linefeed_space_collapse = Pattern.compile("\\s+\\n\\s+");
    final Pattern linefeed_to_space = Pattern.compile("\\n");
    final Pattern tab_to_space = Pattern.compile("\\t");
    final Pattern space_collapse = Pattern.compile("( )+");
    
    // this function strips all whitespace from the text according to the
    // CSS 2.1 spec on whitespace handling. It accounts for the different
    // whitespace settings like normal, nowrap, pre, etc
    public String stripWhitespace(Context c, Node node, InlineBox prev, String text) {
        
        String whitespace = getWhitespace(c,node);
        //u.p("stripWhitespace: text = -" + text + "-");
        //u.p("whitespace = " + whitespace);
        
        // do step 1
        if(whitespace.equals("normal") ||
           whitespace.equals("nowrap") ||
           whitespace.equals("pre-line")) {
            text = linefeed_space_collapse.matcher(text).replaceAll(space);
        }
        //u.p("step 1 = \"" + text + "\"");
        
        // do step 2
        // pull out pre's for breaking
        // still not sure here
        
        // do step 3
        // convert line feeds to spaces
        if(whitespace.equals("normal") ||
           whitespace.equals("nowrap")) {
           text = linefeed_to_space.matcher(text).replaceAll(space);
        }
        //u.p("step 3 = \"" + text +"\"");
        
        // do step 4
        if(whitespace.equals("normal") ||
           whitespace.equals("nowrap") ||
           whitespace.equals("pre-line") ) {
               
            text = tab_to_space.matcher(text).replaceAll(space);
            //u.p("step 4.1 = \"" + text + "\"");

            text = space_collapse.matcher(text).replaceAll(space);
            //u.p("step 4.2 = \"" + text + "\"");

            // collapse first space against prev inline
            if(text.startsWith(space) &&
                           (prev != null) &&
                           (prev.whitespace.equals("normal") ||
                            prev.whitespace.equals("nowrap") ||
                            prev.whitespace.equals("pre-line")) &&
                           (prev.getSubstring().endsWith(space))){
                text = text.substring(1,text.length());
            }
            //u.p("step 4.3 = \"" + text + "\"");

        }
        //u.p("final text = \"" + text + "\"");
        return text;
    }
    
    
    public InlineBox createInline(Context c, Node node, String text, InlineBox prev, int avail, int max, Font font) {
        
        InlineBox inline = new InlineBox();
        inline.node = node;
        inline.whitespace =  getWhitespace(c,node);
        
        // prepare a new inline with a substring that goes
        // from the end of the previous (if applicable) to the
        // end of the master string
        if(prev == null  || prev.node != node) {
            text = stripWhitespace(c,node,prev,text);
            inline.setMasterText(text);
            inline.setSubstring(0,text.length());
        } else {
            //grab text from the previous inline
            text = prev.getMasterText();
            inline.setMasterText(text);
            inline.setSubstring(prev.end_index+1,text.length());
        }
        
        inline.x = max - avail;
        breakText( c, inline, prev, avail, max, font );
        prepBox( c, inline, prev, font );
        // u.p("final inline = " + inline);
        return inline;
    }
    
    public boolean breakText(Context c, InlineBox inline, InlineBox prev, int avail, int max, Font font) {
        // u.p("=========================");
        // u.p("breaking: " + inline);
        // u.p("breaking : '" + inline.getSubstring() + "'");
        // u.p("avail = " + avail);
        // u.p("max = " + max);
        boolean db = false;
        
        //text pre
        // if(text pre) {
        //     inline.text = break on lf
        //     update start/end
        // }
        
        


        
        // all of the text fits on the current line so just return it
        if(FontUtil.len(c,inline.getSubstring(),font) < avail) {
            String txt = inline.getSubstring();
            inline.break_after = false;
            if(db) { u.p("text fits on current line"); }
            return true;
        }



        
        //text too long and pre
        // if too long for line && pre {
        //     inline.text = break at lf
        //     return;
        // }



        
        /*
          there are lots of excess substrings generated in here!!!
          this should all be done with indexes
        */
        //text too long to fit on this line
        int n = 0;
        int pn = 0;
        while(true) {
            n = inline.getSubstring().indexOf(space,n+1);
            
            
            // a single unbreakable string that can't fit on the line
            // handle a single unbreakable string
            if(n == -1 && pn == 0) {
                if(inline.x != 0) {
                    inline.break_before = true;
                    inline.x = 0;
                }
                
                
                // make unbreakable to end of this inline
                //unbreakable(inline, inline.getSubstring().length());
                //if(inline.start_index == -1) {
                //    inline.start_index = 0;
                //}
                inline.setSubstring( inline.start_index, inline.getSubstring().length());
                if(db) { u.p("unbreakable string can't fit on line"); }
                return true;
            }


            
            // make a tenative breaking string
            String tenative = null;
            if(n == -1) {
                tenative = inline.getSubstring();
            } else {
                tenative = inline.getSubstring().substring(0,n);
            }
            // u.p("tenative = " + tenative);
            
            
            
            // if the string is too long to fit in the available space
            int len = FontUtil.len(c,tenative,font);
            // u.p("len = " + len + " avail = " + avail);
            if(len >= avail) {
                
                // if the previous tenative string would work, then add
                // put the box on this line and break after
                // if prev was okay, then go with that
                if(pn > 0) {
                    // if(inline.start_index == -1) {
                    //     inline.start_index = 0;
                    // }
                    
                    if(db) { u.p("normal break on current line. break after"); }
                    inline.setSubstring( inline.start_index, inline.start_index + pn);
                    inline.break_after = true;
                    return true;
                }
                
                
                // if this is an unbreakable word so put it on the next line                
                // HACK. not sure the better way to do this
                // if there is another space after this, then tack it on
                if(inline.getSubstring().length() > n &&
                                inline.getSubstring().charAt(n) == ' ') {
                    // u.p("ending space!!");
                    inline.setSubstring(inline.start_index, inline.start_index + n+1);
                } else {
                    inline.setSubstring(inline.start_index, inline.start_index + n);
                }
                if(db) { u.p("unbreakable word in inline. break before"); }
                inline.break_before = true;
                return true;


            }
            
            // loop
            pn = n;
            if(db) { u.p("loop " + n + " '" + tenative + "' avail = " + avail + " len = " + len); }
        }
        
    }

    
    public void prepBox(Context c, InlineBox box, InlineBox prev_align, Font font) {
        // prepare the font, colors, border, etc
        box.setFont(font);
        BoxLayout.getBackgroundColor(c,box);
        BoxLayout.getBorder(c,box);
        BoxLayout.getMargin(c,box);
        BoxLayout.getPadding(c,box);
        // NOTE: color should be set properly
        box.color = java.awt.Color.black;

        // shift left if starting a new line
        if(box.break_before) {
            box.x = 0;
        }
        
        // trim off leading space if at start of new line
        if(box.x == 0 && box.getSubstring().startsWith(space)) {
            box.setSubstring(box.start_index+1,box.end_index);
        }


        // ============ set x
        /*
        // use the prev_align to calculate the x
        if ( prev_align != null && !prev_align.break_after ) {
            box.x = prev_align.x + prev_align.width;
        } else {
            box.x = 0;
        }
        */
        
        
        // =========== set y
        // y is  relative to the line, so it's always 0
        box.y = 0;
        
        
        
        
        // =========== set width
        
        /*
        try {
            if ( !LayoutUtil.isReplaced(c, node ) ) {
                if ( !LayoutUtil.isFloatedBlock( node, c ) ) {
                    box.width = FontUtil.len( c, node, text.substring( start, end ), font );
                } else {
                    box.width = bounds.width;
                }
            } else {
                */
        box.width = FontUtil.len(c , box.node, box.getSubstring(), font);
        // u.p("width = " + box.width + " from '"+box.getSubstring() +"'");
        /*
                box.width = bounds.width;
            }
        } catch ( StringIndexOutOfBoundsException ex ) {
            u.p( "ex" );
            u.p( "start = " + start );
            u.p( "end = " + end );
            u.p( "text = " + node.getNodeValue() );
            throw ex;
        }
        //u.p("box.x = " + box.x);
        */
        
        
        
        
        // ============= set height
        
        /*
        if ( LayoutUtil.isReplaced(c, node ) ) {
            box.height = bounds.height;
        } else if ( LayoutUtil.isFloatedBlock( node, c ) ) {
            box.height = bounds.height;
        } else {
            */
        box.height = FontUtil.lineHeight( c, box.node );
            /*
        }
        */

        
        
        //box.break_after = true;
        // =========== setup text decorations
        /*
        if ( !LayoutUtil.isReplaced(c, node ) ) {
            if ( !LayoutUtil.isFloatedBlock( node, c ) ) {
                FontUtil.setupTextDecoration( c, node, box );
                if ( box.getText() == null ) {
                    return box;
                }
            }
        }
        */
        
        
        // =========== setup vertical alignment
        /*
        // do vertical alignment
        VerticalAlign.setupVerticalAlign( c, node, box );
        box.setFont( font );//FontUtil.getFont(c,node));
        if ( node.getNodeType() == node.TEXT_NODE ) {
            box.color = c.css.getColor( (Element)node.getParentNode(), true );
        } else {
            box.color = c.css.getColor( (Element)node, true );
        }
        Relative.setupRelative( c, box );
        */

        
        // ============= do special setup for first line
        /*
        // if first line then do extra setup        
        if(c.isFirstLine()) {
            // if there is a first line pseudo class
            CascadedStyle pseudo = c.css.getPseudoElementStyle(getNearestBlockElement(node,c),"first-line");
            if(pseudo != null) {
                CalculatedStyle normal = c.css.getStyle(box.getRealElement());
                CalculatedStyle merged = new CalculatedStyle(normal,pseudo);
                styleInlineBox(c,merged,box);
            }
        }
        */
        
        
        // adjust width based on borders and padding
        box.width += box.totalHorizontalPadding();
    }

    public void unbreakable(InlineBox box, int n) {
        if(box.start_index == -1) {
            box.start_index = 0;
        }
        box.setSubstring( box.start_index, box.start_index + n);
        return;
    }

    public String getWhitespace(Context c, Node node) {
        Element e = null;
        if(node instanceof Element) {
            e = (Element)node;
        } else {
            e = (Element)node.getParentNode();
        }
        String whitespace = c.css.getStringProperty(e, "white-space" );
        if(whitespace == null) {
            whitespace = "normal";
        }
        return whitespace;
    }
    
    public static void df(Context c, String text, Font f) {
        /*
        u.p("-------------------------");
        ((Graphics2D)c.getGraphics()).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            
        ((Graphics2D)c.getGraphics()).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        FontMetrics fm = c.getGraphics().getFontMetrics();
        u.p("graphics = " + c.getGraphics());
        u.p("fm = " + fm);
        u.p("text = -" + text + "-");
        u.p("real len = " + fm.stringWidth(text));
        u.p("real height = " + fm.getHeight());
        u.p("-------------------------");
        */
    }
    
}

