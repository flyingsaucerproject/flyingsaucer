package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineTextBox;
import org.xhtmlrenderer.render.LineBox;

import java.util.ArrayList;
import java.util.List;

public class TextAlignJustify {
    public static boolean isJustified(CalculatedStyle style) {
        /*String text_align = style.getStringProperty(CSSName.TEXT_ALIGN);
        if (text_align != null && text_align.equals("justify")) {*/
        if ( style.isIdent(CSSName.TEXT_ALIGN, IdentValue.JUSTIFY)) {
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

    private static void splitInlineBox(Context c, InlineBox ibox, List temp_list) {
        if (!(ibox instanceof InlineTextBox)) {
            temp_list.add(ibox.copy());
            return;
        }
        InlineTextBox box = (InlineTextBox) ibox;
        String[] words = words(box);
        // don't split if only one word
        if (words.length < 2) {
            temp_list.add(box);
            return;
        }
        int currentWordPosition = box.start_index + box.getSubstring().indexOf(words[0]);
        for (int i = 0; i < words.length; i++) {
            InlineTextBox copy = (InlineTextBox) box.copy();
            copy.setSubstring(currentWordPosition, currentWordPosition + words[i].length());//was: (words[i]);
            currentWordPosition = currentWordPosition + words[i].length() + 1;//skip the space too
            copy.width = FontUtil.len(c, copy);
            temp_list.add(copy);
            if (i == 0) {
                copy.pushstyles = box.pushstyles;
            } else if (i == words.length - 1) {
                copy.popstyles = box.popstyles;
            }
        }
    }

    private static String[] words(InlineTextBox box) {
        String text = box.getSubstring();
        String[] words = text.split("\\s");
        return words;
    }

// --Commented out by Inspection START (2005-01-05 01:05):
//    private static int wordCount(InlineBox box) {
//        String text = box.getSubstring();
//        String[] words = text.split("\\s");
//        return words.length;
//    }
// --Commented out by Inspection STOP (2005-01-05 01:05)
}
