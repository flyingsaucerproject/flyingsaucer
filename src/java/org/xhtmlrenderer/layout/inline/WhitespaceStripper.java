package org.xhtmlrenderer.layout.inline;


import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.content.StylePop;
import org.xhtmlrenderer.layout.content.StylePush;
import org.xhtmlrenderer.layout.content.TextContent;
import org.xhtmlrenderer.render.InlineBox;

import java.awt.Font;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class WhitespaceStripper {
    public static final String SPACE = " ";
    public static final String EOL = "\n";
    // update this to work on linefeeds on multiple platforms;
    public static final Pattern linefeed_space_collapse = Pattern.compile("\\s+\\n\\s+");//Pattern is thread-safe
    public static final Pattern linefeed_to_space = Pattern.compile("\\n");
    public static final Pattern tab_to_space = Pattern.compile("\\t");
    public static final Pattern space_collapse = Pattern.compile("( )+");

    /**
     * @deprecated whitespace should be stripped as early as possible
     */
    public static InlineBox createInline(Context c, TextContent content, InlineBox prev, InlineBox prev_align, int avail, int max, Font font) {
        InlineBox inline = new InlineBox();
        inline.content = content;
        CalculatedStyle style = c.getCurrentStyle();
        inline.whitespace = getWhitespace(style);
        
        // prepare a new inline with a substring that goes
        // from the end of the previous (if applicable) to the
        // end of the master string
        if (prev == null || prev.content != content) {
            String text = stripWhitespace(style, prev, content.getText());
            inline.setMasterText(text);
            inline.setSubstring(0, text.length());
        } else {
            //grab text from the previous inline
            String text = prev.getMasterText();
            inline.setMasterText(text);
            inline.setSubstring(prev.end_index, text.length());
        }

        Breaker.breakText(c, inline, prev_align, avail, max, font);
        BoxBuilder.prepBox(c, inline, prev_align, font);
        return inline;
    }


    // this function strips all whitespace from the text according to the
    // CSS 2.1 spec on whitespace handling. It accounts for the different
    // whitespace settings like normal, nowrap, pre, etc
    /**
     * @deprecated whitespace should be stripped already in the content list
     */
    public static String stripWhitespace(CalculatedStyle style, InlineBox prev, String text) {

        String whitespace = getWhitespace(style);
        //Uu.p("stripWhitespace: text = -" + text + "-");
        //Uu.p("whitespace = " + whitespace);
        

        // do step 1
        if (whitespace.equals("normal") ||
                whitespace.equals("nowrap") ||
                whitespace.equals("pre-line")) {
            text = linefeed_space_collapse.matcher(text).replaceAll(SPACE);
        }
        //Uu.p("step 1 = \"" + text + "\"");
        

        // do step 2
        // pull out pre's for breaking
        // still not sure here
        

        // do step 3
        // convert line feeds to spaces
        if (whitespace.equals("normal") ||
                whitespace.equals("nowrap")) {
            text = linefeed_to_space.matcher(text).replaceAll(SPACE);
        }
        //Uu.p("step 3 = \"" + text +"\"");
        

        // do step 4
        if (whitespace.equals("normal") ||
                whitespace.equals("nowrap") ||
                whitespace.equals("pre-line")) {

            text = tab_to_space.matcher(text).replaceAll(SPACE);
            //Uu.p("step 4.1 = \"" + text + "\"");

            text = space_collapse.matcher(text).replaceAll(SPACE);
            //Uu.p("step 4.2 = \"" + text + "\"");

            // collapse first space against prev inline
            if (text.startsWith(SPACE) &&
                    (prev != null) &&
                    (prev.whitespace.equals("normal") ||
                    prev.whitespace.equals("nowrap") ||
                    prev.whitespace.equals("pre-line")) &&
                    (prev.getSubstring().endsWith(SPACE))) {
                text = text.substring(1, text.length());
            }
            //Uu.p("step 4.3 = \"" + text + "\"");

        }

        //Uu.p("final text = \"" + text + "\"");
        return text;
    }

    /**
     * Strips whitespace early in inline content generation.
     * This can be done because "whitespage" does not ally to :first-line and :first-letter.
     * For dynamic pseudo-classes we are allowed to choose which properties apply.
     *
     * @param c
     * @param inlineContent
     * @return a list cleaned of empty content and the thereby redundant style-changes
     */
    public static List stripInlineContent(Context c, List inlineContent) {
        List stripped = new LinkedList();
        List pendingStylePushes = new LinkedList();
        boolean collapse = false;

        for (Iterator i = inlineContent.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof StylePush) {
                pendingStylePushes.add(o);
                c.pushStyle(((StylePush) o).getStyle());
                continue;
            }
            if (o instanceof TextContent) {
                TextContent tc = (TextContent) o;
                boolean collapseNext = stripWhitespace(c.getCurrentStyle(), collapse, tc);
                if (tc.getText().equals("")) {
                    //ignore it, let the next collapse with the previous
                    continue;
                } else {
                    stripped.addAll(pendingStylePushes);
                    pendingStylePushes.clear();
                    stripped.add(tc);
                    collapse = collapseNext;
                    continue;
                }
            }
            if (o instanceof StylePop) {
                c.popStyle();
                if (pendingStylePushes.size() != 0) {
                    //redundant style-change
                    pendingStylePushes.remove(pendingStylePushes.size() - 1);
                } else {
                    stripped.add(o);
                }
                continue;
            }
            //Here we have some other object, just add it with preceding styles
            stripped.addAll(pendingStylePushes);
            pendingStylePushes.clear();
            stripped.add(o);
            collapse = false;//no collapsing of the next one
        }

        //there may be relevant StylePushes pending, e.g. if this is content of AnonymousBlock
        stripped.addAll(pendingStylePushes);
        return stripped;
    }

    /**
     * this function strips all whitespace from the text according to the
     * CSS 2.1 spec on whitespace handling. It accounts for the different
     * whitespace settings like normal, nowrap, pre, etc
     *
     * @param style
     * @param collapseLeading
     * @param tc              the TextContent to strip. The text in it is modified.
     * @return whether the next leading space should collapse or not.
     */
    static boolean stripWhitespace(CalculatedStyle style, boolean collapseLeading, TextContent tc) {

        String whitespace = style.getStringProperty(CSSName.WHITE_SPACE);
        String text = tc.getText();

        // do step 1
        if (whitespace.equals("normal") ||
                whitespace.equals("nowrap") ||
                whitespace.equals("pre-line")) {
            text = linefeed_space_collapse.matcher(text).replaceAll(EOL);
        }

        // do step 2
        // pull out pre's for breaking
        // OK: any spaces in a pre or pre-wrap are considered to be non-breaking
        // resolve that later, the space-sequence may only be broken at the end!


        // do step 3
        // convert line feeds to spaces
        if (whitespace.equals("normal") ||
                whitespace.equals("nowrap")) {
            text = linefeed_to_space.matcher(text).replaceAll(SPACE);
        }

        // do step 4
        if (whitespace.equals("normal") ||
                whitespace.equals("nowrap") ||
                whitespace.equals("pre-line")) {

            text = tab_to_space.matcher(text).replaceAll(SPACE);
            text = space_collapse.matcher(text).replaceAll(SPACE);

            // collapse first space against prev inline
            if (text.startsWith(SPACE) &&
                    collapseLeading) {
                text = text.substring(1, text.length());
            }
        }

        boolean collapseNext = (text.endsWith(SPACE) &&
                (whitespace.equals("normal") ||
                whitespace.equals("nowrap") ||
                whitespace.equals("pre-line")));

        tc.setText(text);
        return collapseNext;
    }


    public static void unbreakable(InlineBox box, int n) {
        if (box.start_index == -1) {
            box.start_index = 0;
        }
        box.setSubstring(box.start_index, box.start_index + n);
        return;
    }

    public static String getWhitespace(CalculatedStyle style) {
        String whitespace = style.getStringProperty("white-space");
        if (whitespace == null) {//should never happen
            whitespace = "normal";
        }
        return whitespace;
    }

    public static void df(Context c, String text, Font f) {
        /*
        Uu.p("-------------------------");
        ((Graphics2D)c.getGraphics()).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            
        ((Graphics2D)c.getGraphics()).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        FontMetrics fm = c.getGraphics().getFontMetrics();
        Uu.p("graphics = " + c.getGraphics());
        Uu.p("fm = " + fm);
        Uu.p("text = -" + text + "-");
        Uu.p("real len = " + fm.stringWidth(text));
        Uu.p("real height = " + fm.getHeight());
        Uu.p("-------------------------");
        */
    }

}

