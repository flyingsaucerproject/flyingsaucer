package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.layout.content.FloatedBlockContent;
import org.xhtmlrenderer.layout.content.AbsoluteBlockContent;
import org.xhtmlrenderer.layout.content.InlineBlockContent;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.Uu;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;

public class VerticalAlign {

    public static void setupVerticalAlign(Context c, CalculatedStyle style, InlineBox box) {
        // Uu.p("setup vert align: " + box);
        Content content = box.content;

        //not used: CalculatedStyle parent_style = c.css.getStyle(LineBreaker.getElement(parent));
        Font parent_font = FontUtil.getFont(c, style);
        LineMetrics parent_metrics = null;
        if (!(content instanceof InlineBlockContent)) {
            if (!(content instanceof FloatedBlockContent) &&
                !(content instanceof AbsoluteBlockContent)) {
                parent_metrics = parent_font.getLineMetrics(box.getSubstring(), ((Graphics2D) c.getGraphics()).getFontRenderContext());
            } else {
                parent_metrics = parent_font.getLineMetrics("Test", ((Graphics2D) c.getGraphics()).getFontRenderContext());
            }
        } else {
            parent_metrics = parent_font.getLineMetrics("Test", ((Graphics2D) c.getGraphics()).getFontRenderContext());
        }

        // the height of the font
        float parent_height = parent_metrics.getHeight();

        String vertical_align = style.propertyByName("vertical-align").computedValue().asString();

        // set the height of the box to the height of the font
        if (!(content instanceof InlineBlockContent) &&
            !(content instanceof AbsoluteBlockContent)) {
            box.height = FontUtil.lineHeight(c, box);
        }

        if (vertical_align == null) {
            vertical_align = "baseline";
        }

        box.baseline = 0;

        // box.y is relative to the parent's baseline
        box.y = 0;
        
        // do nothing for 'baseline'
        box.vset = true;

        if (vertical_align.equals("baseline")) {
            Font font = FontUtil.getFont(c, box);
            box.y += FontUtil.getDescent(c, box, font);
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

    }

    /**
     * Description of the Method
     *
     * @param box PARAM
     */
    public static void setupVerticalAlign(LineBox box) {
        // Uu.p("setting up vertical align for a line: " + box);

        // top and bottom are max dist from baseline
        int top = 0;
        int bot = 0;
        int height = 0;
        for (int i = 0; i < box.getChildCount(); i++) {
            InlineBox inline = (InlineBox) box.getChild(i);
            // skip floated inlines. they don't affect height calculations
            if (inline.floated) {
                continue;
            }
            if (inline.content instanceof AbsoluteBlockContent) {
                // Uu.p("inline = " + inline);
                continue;
            }
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
            InlineBox inline = (InlineBox) box.getChild(i);
            if (inline.floated ||
                inline.content instanceof AbsoluteBlockContent) {
                    // Uu.p("skipping: " + inline);
                // Uu.p("adjusting floated inline:");
                // Uu.p("inline = " + inline);
                //inline.y = inline.y;// - box.baseline + inline.height;
                // Uu.p("inline = " + inline);
            } else {
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

    }
}
