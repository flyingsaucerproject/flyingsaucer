package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineTextBox;
import org.xhtmlrenderer.render.LineBox;

import java.awt.font.LineMetrics;

public class VerticalAlign {

    /**
     * returns how much the baseline should be raised (negative means lowered)
     */
    public static int getBaselineOffset(Context c, LineBox curr_line, InlineBox new_inline, int blockLineHeight, LineMetrics blockLineMetrics) {
        int lineHeight;
        int ascent;
        int descent;
        int baselineOffset;
        if (new_inline instanceof InlineTextBox) {
            // should be the metrics of the font, actually is the metrics of the text
            LineMetrics metrics = FontUtil.getLineMetrics(c, new_inline);
            lineHeight = FontUtil.lineHeight(c);//assume that current context is valid for new_inline
            ascent = (int) metrics.getAscent();
            descent = (int) metrics.getDescent();
        } else {
            lineHeight = new_inline.height;
            ascent = lineHeight;
            descent = 0;
        }

        String vertical_align = c.getCurrentStyle().getStringProperty(CSSName.VERTICAL_ALIGN);

        if (vertical_align == null) {
            vertical_align = "baseline";
        }

        if (vertical_align.equals("baseline")) {
            baselineOffset = 0;
        } else if (vertical_align.equals("super")) {
            // works okay i think
            baselineOffset = (int) (-blockLineMetrics.getStrikethroughOffset() * 2.0);//up is negative in Java!
            //XRLog.render("baseline offset for super "+baselineOffset);
        } else if (vertical_align.equals("sub")) {
            // works okay i think
            baselineOffset = (int) blockLineMetrics.getStrikethroughOffset();//up is negative in Java!
            //XRLog.render("baseline offset for sub "+baselineOffset);
        } else if (vertical_align.equals("text-top")) {
            // the top of this text is equal to the top of the parent's text
            // so we take the parent's height above the baseline and subtract our
            // height above the baseline
            baselineOffset = (int) (blockLineMetrics.getAscent() - ascent);
            //XRLog.render("baseline offset for text-top"+baselineOffset);
        } else if (vertical_align.equals("text-bottom")) {
            baselineOffset = -(int) (blockLineMetrics.getDescent() - descent);
            //XRLog.render("baseline offset for text-bottom"+baselineOffset);
        } else if (vertical_align.equals("top")) {
            //oops, this will be difficult because we need to keep track of the element sub-tree!
            //HACK: for now, just align the top of this box with the top of the line
            baselineOffset = curr_line.getBaseline() - ascent;
        } else if (vertical_align.equals("bottom")) {
            //oops, this will be difficult because we need to keep track of the element sub-tree!
            //HACK: for now, just align the top of this box with the top of the line
            baselineOffset = descent - (curr_line.height - curr_line.getBaseline());
        } else {
            baselineOffset = (int) c.getCurrentStyle().getFloatPropertyProportionalHeight(CSSName.VERTICAL_ALIGN, c.getBlockFormattingContext().getHeight());
        }
        return baselineOffset;
    }

    /* calculated elsewhere
    public static void setupVerticalAlign(Context c, CalculatedStyle style, InlineBox box) {
        // Uu.p("setup vert align: " + box);
        //Content content = box.content;

        LineMetrics parent_metrics = FontUtil.getLineMetrics(c, box);

        // the height of the font
        float parent_height = parent_metrics.getHeight();

        String vertical_align = style.getStringProperty("vertical-align");

        if (vertical_align == null) {
            vertical_align = "baseline";
        }

        box.baseline = 0;

        // box.y is relative to the parent's baseline
        box.y = 0;
        
        // do nothing for 'baseline'
        box.vset = true;

        if (vertical_align.equals("baseline")) {
            box.y += FontUtil.getLineMetrics(c, box).getDescent();
        }
        
        // works okay i think
        if (vertical_align.equals("super")) {
            box.y = box.y + (int) (parent_metrics.getStrikethroughOffset() * 2.0);
        }

        // works okay, i think
        if (vertical_align.equals("sub")) {
            box.y = box.y - (int) parent_metrics.getStrikethroughOffset();
        }

        // joshy: this is using the current baseline instead of the parent's baseline
        // must fix
        if (vertical_align.equals("text-top")) {
            // the top of this text is equal to the top of the parent's text
            // so we take the parent's height above the baseline and subtract our
            // height above the baseline
            box.y = -((int) parent_height - box.height);
        }

        // not implemented correctly yet
        if (vertical_align.equals("text-bottom")) {
            box.y = 0;
        }

        // not implemented correctly yet.
        if (vertical_align.equals("top")) {
            box.y = box.y - box.baseline;
            box.top_align = true;
            box.vset = false;
        }

        if (vertical_align.equals("bottom")) {
            box.y = box.y - box.baseline;
            box.bottom_align = true;
            box.vset = false;
        }

    }*/

    /**
     * Description of the Method
     *
     * @param box PARAM
     */
    /*calculated elsewhere
    public static void setupVerticalAlign(LineBox box) {
        // Uu.p("setting up vertical align for a line: " + box);

        // top and bottom are max dist from baseline
        int top = 0;
        int bot = 0;
        int height = 0;
        for (int i = 0; i < box.getChildCount(); i++) {
            Box child = box.getChild(i);
            // skip floated inlines. they don't affect height calculations
            if (child.floated) {
                continue;
            }
            if (child.absolute) {
                // Uu.p("inline = " + inline);
                continue;
            }
            InlineBox inline = (InlineBox) child;
            if (inline.vset) {
                // compare the top of the box
                if (inline.y - inline.height < top) {
                    top = inline.y - inline.height;
                }
                // compare the bottom of the box
                if (inline.y + 0 > bot) {
                    bot = inline.y + 0;
                }
            } else {
                // if it's not one of the baseline derived vertical aligns
                // then just compare the straight height of the inline
                if (inline.height > height) {
                    height = inline.height;
                }
            }
        }

        if (bot - top > height) {
            box.height = bot - top;
            box.baseline = box.height - bot;
        } else {
            box.height = height;
            box.baseline = box.height;
        }

        // loop through all inlines to set the last ones
        for (int i = 0; i < box.getChildCount(); i++) {
            Box child = box.getChild(i);
            if (child.floated ||
                    child.absolute) {
                // Uu.p("skipping: " + inline);
                // Uu.p("adjusting floated inline:");
                // Uu.p("inline = " + inline);
                //inline.y = inline.y;// - box.baseline + inline.height;
                // Uu.p("inline = " + inline);
            } else {
                InlineBox inline = (InlineBox) child;
                if (!inline.vset) {
                    inline.vset = true;
                    if (inline.top_align) {
                        inline.y = -box.baseline + inline.height;
                    }
                    if (inline.bottom_align) {
                        inline.y = 0;
                    }
                }
            }
        }

    }*/
}
