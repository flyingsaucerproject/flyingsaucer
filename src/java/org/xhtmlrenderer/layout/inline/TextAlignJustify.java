package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;

import java.util.ArrayList;
import java.util.List;

public class TextAlignJustify {
    public static boolean isJustified(CalculatedStyle style) {
        String text_align = style.getStringProperty(CSSName.TEXT_ALIGN);
        if (text_align != null && text_align.equals("justify")) {
            return true;
        }
        return false;
    }

    public static void justifyLine(Context c, LineBox line, int width) {
        if (line.width > width) {
            return;
        }

        // split each inline box into words
        List temp_list = new ArrayList();
        for (int i = 0; i < line.getChildCount(); i++) {
            splitInlineBox(c, (InlineBox) line.getChild(i), temp_list);
        }
        // clear out all inlines
        line.removeAllChildren();
        // add in all of the new inlines
        line.width = 0;
        for (int i = 0; i < temp_list.size(); i++) {
            InlineBox box = (InlineBox) temp_list.get(i);
            line.width += box.width;
            line.addChild(box);
        }

        int extra = width - line.width;
        int spaces = (line.getChildCount() - 1);
        float spacer = extra / (float) spaces;
        // now realign them
        int total_lengths = 0;
        for (int i = 0; i < line.getChildCount(); i++) {
            InlineBox box = (InlineBox) line.getChild(i);
            box.x = total_lengths + (int) (spacer * (float) i);
            total_lengths += box.width;
        }
    }

    private static void splitInlineBox(Context c, InlineBox box, List temp_list) {
        String[] words = words(box);
        // don't split if only one word
        if (words.length < 2) {
            temp_list.add(box);
            return;
        }
        for (int i = 0; i < words.length; i++) {
            InlineBox copy = new InlineBox(box);
            copy.setSubstring(words[i]);
            copy.width = FontUtil.len(c, copy);
            temp_list.add(copy);
        }
    }

    private static String[] words(InlineBox box) {
        String text = box.getSubstring();
        String[] words = text.split("\\s");
        return words;
    }

    private static int wordCount(InlineBox box) {
        String text = box.getSubstring();
        String[] words = text.split("\\s");
        return words.length;
    }
}
