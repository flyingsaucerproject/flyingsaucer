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
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;

public class WhitespaceStripper {
    public static final String SPACE = " ";
    public static final String EOL = "\n";
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
            text = linefeed_space_collapse.matcher(text).replaceAll(SPACE);
        }
        //u.p("step 1 = \"" + text + "\"");
        
        // do step 2
        // pull out pre's for breaking
        // still not sure here
        
        // do step 3
        // convert line feeds to spaces
        if(whitespace.equals("normal") ||
           whitespace.equals("nowrap")) {
           text = linefeed_to_space.matcher(text).replaceAll(SPACE);
        }
        //u.p("step 3 = \"" + text +"\"");
        
        // do step 4
        if(whitespace.equals("normal") ||
           whitespace.equals("nowrap") ||
           whitespace.equals("pre-line") ) {
               
            text = tab_to_space.matcher(text).replaceAll(SPACE);
            //u.p("step 4.1 = \"" + text + "\"");

            text = space_collapse.matcher(text).replaceAll(SPACE);
            //u.p("step 4.2 = \"" + text + "\"");

            // collapse first space against prev inline
            if(text.startsWith(SPACE) &&
                           (prev != null) &&
                           (prev.whitespace.equals("normal") ||
                            prev.whitespace.equals("nowrap") ||
                            prev.whitespace.equals("pre-line")) &&
                           (prev.getSubstring().endsWith(SPACE))){
                text = text.substring(1,text.length());
            }
            //u.p("step 4.3 = \"" + text + "\"");

        }
        //u.p("final text = \"" + text + "\"");
        return text;
    }
    
    
    public InlineBox createInline(Context c, Node node, String text, InlineBox prev, InlineBox prev_align, int avail, int max, Font font) {
        // u.p("text = " + text);
        InlineBox inline = new InlineBox();
        inline.node = node;
        inline.whitespace =  getWhitespace(c,node);
        
        // prepare a new inline with a substring that goes
        // from the end of the previous (if applicable) to the
        // end of the master string
        if(prev == null  || prev.node != node) {
            // u.p("regen text for: " + text);
            text = stripWhitespace(c,node,prev,text);
            inline.setMasterText(text);
            inline.setSubstring(0,text.length());
        } else {
            //grab text from the previous inline
            // u.p("reusing text for: " + text);
            // u.p("prev = " + prev);
            
            text = prev.getMasterText();
            inline.setMasterText(text);
            inline.setSubstring(prev.end_index,text.length());
            // u.p("new = " + inline);
        }

        Breaker.breakText( c, inline, prev, prev_align, avail, max, font );
        prepBox( c, inline, prev_align, font );
        // u.p("final inline = " + inline);
        return inline;
    }
    

    
    public void prepBox(Context c, InlineBox box, InlineBox prev_align, Font font) {
        //u.p("box = " + box);
        //u.p("prev align = " + prev_align);


        // prepare the font, colors, border, etc
        box.setFont(font);
        BoxLayout.getBackgroundColor(c,box);
        BoxLayout.getBorder(c,box);
        BoxLayout.getMargin(c,box);
        BoxLayout.getPadding(c,box);


        // =========== setup the color
        if ( box.node.getNodeType() == box.node.TEXT_NODE ) {
            box.color = c.css.getColor( (Element)box.node.getParentNode(), true );
        } else {
            box.color = c.css.getColor( (Element)box.node, true );
        }





        // ============ set x ===========
        // shift left if starting a new line
        if(box.break_before) {
            box.x = 0;
        }

        // use the prev_align to calculate the x if not at start of
        // new line
        if ( prev_align != null && 
            !prev_align.break_after && 
            !box.break_before
            ) {
            //u.p("prev align = " + prev_align);
            //u.p("floated = " + LayoutUtil.isFloatedBlock( prev_align.node, c ) );
            box.x = prev_align.x + prev_align.width;
        } else {
            box.x = 0;

            // trim off leading space only if at start of new line
            if(box.getSubstring().startsWith(SPACE)) {
                box.setSubstring(box.start_index+1,box.end_index);
            }
        }
        
        
        
        // =========== set y ===========
        // y is  relative to the line, so it's always 0
        box.y = 0;
        
        
        
        
        // =========== set width ==========
        
        /*
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
        if(TextDecoration.isDecoratable(c,box.node)) {
            TextDecoration.setupTextDecoration( c, box.node, box );
        }
        
        
        // =========== setup vertical alignment
        VerticalAlign.setupVerticalAlign( c, box.node, box );
        
        // =========== setup relative
        //Relative.setupRelative( c, box );

        
        // ============= do special setup for first line
        
        // if first line then do extra setup        
        if(c.isFirstLine()) {
            // if there is a first line pseudo class
            CascadedStyle pseudo = c.css.getPseudoElementStyle(LineBreaker.getNearestBlockElement(box.node,c),"first-line");
            if(pseudo != null) {
                CalculatedStyle normal = c.css.getStyle(box.getRealElement());
                CalculatedStyle merged = new CalculatedStyle(normal,pseudo);
                LineBreaker.styleInlineBox(c,merged,box);
            }
        }
        
        
        
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

