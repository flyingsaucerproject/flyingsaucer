package org.xhtmlrenderer.layout.inline;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.DerivedProperty;
import org.xhtmlrenderer.css.style.DerivedValue;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.render.InlineBox;

public class TextDecoration {

    public static void setupTextDecoration(Context c, Node node, InlineBox box) {
        Element el = null;
        if (node instanceof Element) {
            el = (Element) node;
        } else {
            el = (Element) node.getParentNode();
        }
        
        // set to defaults
        box.underline = false;
        box.strikethrough = false;
        box.overline = false;
        
        // override based on settings
        String text_decoration = c.css.getStyle(el).getStringProperty(CSSName.TEXT_DECORATION);
        if (text_decoration != null && text_decoration.equals("underline")) {
            box.underline = true;
        }
        if (text_decoration != null && text_decoration.equals("line-through")) {
            box.strikethrough = true;
        }
        if (text_decoration != null && text_decoration.equals("overline")) {
            box.overline = true;
        }
    }

    public static void setupTextDecoration(CalculatedStyle style, Node node, InlineBox box) {
        Element el = null;
        if (node instanceof Element) {
            el = (Element) node;
        } else {
            el = (Element) node.getParentNode();
        }
        if (style.hasProperty("text-decoration")) {
            DerivedProperty text_decoration = style.propertyByName("text-decoration");
            DerivedValue dv = text_decoration.computedValue();
            String td = dv.asString();
            if (td != null && td.equals("underline")) {
                box.underline = true;
            }
            if (td != null && td.equals("line-through")) {
                box.strikethrough = true;
            }
            if (td != null && td.equals("overline")) {
                box.overline = true;
            }
        }
    }


    public static boolean isDecoratable(Context c, Node node) {
        if (!LayoutUtil.isReplaced(c, node)) {
            if (!LayoutUtil.isFloatedBlock(node, c)) {
                return true;
            }
        }
        return false;
    }

}
