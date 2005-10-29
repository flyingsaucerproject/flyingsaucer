package org.xhtmlrenderer.layout;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.content.ContentUtil;
import org.xhtmlrenderer.layout.content.StylePush;
import org.xhtmlrenderer.render.*;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-okt-28
 * Time: 15:51:13
 * To change this template use File | Settings | File Templates.
 */
public class Restyling {
    public static void restyle(LayoutContext c, BlockBox box) {
        if (box.element != null) {
            CalculatedStyle parentStyle = box.getStyle().getCalculatedStyle().getParent();
            c.initializeStyles(parentStyle);
        }//else root box, already initialized
        restyleBox(c, box);
    }

    private static void restyleBox(LayoutContext c, Box box) {
        if (box instanceof AnonymousBlockBox) {
            //InlineRendering.paintInlineContext(c, block, restyle);
        } else {
            CascadedStyle style = c.getCss().getCascadedStyle(box.element, true);
            c.pushStyle(style);
            CalculatedStyle calculatedStyle = c.getCurrentStyle();
            box.getStyle().setCalculatedStyle(calculatedStyle);
            if (ContentUtil.mayHaveFirstLine(calculatedStyle)) {
                CascadedStyle firstLine = c.getCss().getPseudoElementStyle(box.element, "first-line");
                box.firstLineStyle = firstLine;
            }
            //TODO: first-letter
            //special style for first line?
            if (box.firstLineStyle != null) {
                c.addFirstLineStyle(box.firstLineStyle);
            }

            if (BoxRendering.isInlineLayedOut(box)) {
                restyleInlineContext(c, box);
            } else {
                restyleBlockContext(c, box);
            }
            //pop in case not used
            if (box.firstLineStyle != null) {
                c.popFirstLineStyle();
            }

        }
    }

    private static void restyleInlineContext(LayoutContext c, Box block) {
        for (int i = 0; i < block.getChildCount(); i++) {
            LineBox line = (LineBox) block.getChild(i);
            restyleLine(line, c);
        }
    }

    private static void restyleLine(LineBox line, LayoutContext c) {
        LinkedList pushedStyles = null;

        // for each inline box
        InlineBox lastInline = null;
        int padX = 0;
        for (int j = 0; j < line.getChildCount(); j++) {
            Box child = line.getChild(j);
            if (child.getStyle().isAbsolute()) {
                restyleBox(c, (BlockBox) child);
                continue;
            }

            InlineBox box = (InlineBox) child;
            lastInline = box;
            padX = 0;
            if (c.hasFirstLineStyles() && pushedStyles == null) {
                //Uu.p("doing first line styles");
                pushedStyles = new LinkedList();
                for (Iterator i = c.getFirstLineStyles().iterator(); i.hasNext();) {
                    CascadedStyle firstLineStyle = (CascadedStyle) i.next();
                    c.pushStyle(firstLineStyle);
                }
            }
            restyleInlineBox(c, box, pushedStyles);
        }

        if (c.hasFirstLineStyles() && pushedStyles != null) {
            for (int i = 0; i < pushedStyles.size(); i++) {
                c.popStyle();
            }
            for (Iterator i = pushedStyles.iterator(); i.hasNext();) {
                c.pushStyle((CascadedStyle) i.next());
            }
            c.clearFirstLineStyles();
        }
    }

    private static void restyleInlineBox(LayoutContext c, InlineBox ib, LinkedList pushedStyles) {
        if (ib.pushstyles != null) {
            for (Iterator i = ib.pushstyles.iterator(); i.hasNext();) {
                StylePush sp = (StylePush) i.next();
                Element e = sp.getElement();
                CascadedStyle cascaded;
                if (e == null) {//anonymous inline box
                    cascaded = CascadedStyle.emptyCascadedStyle;
                } else {
                    cascaded = c.getCss().getCascadedStyle(e, true);
                }
                if (pushedStyles != null) pushedStyles.addLast(cascaded);
                c.pushStyle(cascaded);
            }
        }

        if (ib.getStyle().isFloated()) {
            restyleBox(c, ib);
        } else if (ib instanceof InlineBlockBox) {
            c.pushStyle(c.getCss().getCascadedStyle(ib.element, true));
            restyleBox(c, ((InlineBlockBox) ib).sub_block);
        }

        ib.getStyle().setCalculatedStyle(c.getCurrentStyle());

        if (ib.popstyles != 0) {
            for (int i = 0; i < ib.popstyles; i++) {
                c.popStyle();
            }
        }
    }

    private static void restyleBlockContext(LayoutContext c, Box box) {
        if (box.component != null) {
            int start = 0;
            int end = box.getChildCount() - 1;
            for (int i = start; i <= end; i++) {
                Box child = (Box) box.getChild(i);
                restyleBox(c, (BlockBox) child);
            }
        }

    }
}
