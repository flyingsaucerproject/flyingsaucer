package org.xhtmlrenderer.layout;

import org.w3c.dom.Node;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.content.*;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.Uu;

public class LayoutUtil {

    public static String getDisplay(CascadedStyle style) {
        // Uu.p("checking: " + child);
        String display = style.propertyByName("display").getValue().getCssText();
        // Uu.p("display = " + display);
        
        // override for floated
        if (isFloated(style)) {
            return "block";
        }

        return display;
    }


    public static boolean isOutsideNormalFlow(Box box) {
        if (box.fixed) {
            return true;
        }
        if (box.absolute) {
            //Uu.p("box is abs: " + box);
            return true;
        }
        if (box.floated) {
            return true;
        }
        return false;
    }

    public static boolean shouldDrawBackground(Box box) {
        if (!isBlockOrInlineElementBox(box)) {
            return false;
        }
        if (box.content instanceof AnonymousBlockContent) {
            return false;
        }
        return true;
    }

    //TODO: move this to Box
    public static boolean isBlockOrInlineElementBox(Box box) {
        //Uu.p("box = " + box);
        if (box.content instanceof BlockContent) {
            //Uu.p("box is a block or element");
            return true;
        }
        if (box.content instanceof DomToplevelNode) {
            //Uu.p("box is a block or element");
            return true;
        }
        if (box.content instanceof AnonymousBlockContent) {
            //Uu.p("box is a block or element");
            return true;
        }

        if (box.content instanceof TextContent) {
            // Uu.p("box is not a block or inline element");
            return false;
        }

        if (box.content instanceof InlineBlockContent) {
            return true;
        }

        if (box.content instanceof AbsolutelyPositionedContent) {
            return true;
        }

        if (box.content instanceof FloatedBlockContent) {
            return true;
        }
        
        // if (box.content instanceof BlockContent ||
        //     (box.isInlineElement() && !(box.content instanceof TextContent))) {
        //     Uu.p("box is a block or element");
        //     return true;
        // }
        
        Uu.p("fall through!" + box);
        return true;
    }

    //TODO: move this to Box
    public static Border getBorder(Box box, CalculatedStyle style) {
        Border border = null;
        if (isBlockOrInlineElementBox(box)) {
            if (border == null) {
                border = style.getBorderWidth();
                String border_style = style.getStringProperty(CSSName.BORDER_STYLE_TOP);
                if (border_style.equals("none")) {
                    border = new Border(0, 0, 0, 0);
                }
            }
        }
        return border;
    }

    /**
     * Gets the fixed attribute of the DefaultLayout object
     *
     * @param style
     * @return The fixed value
     */
    public static boolean isFixed(CascadedStyle style) {
        if (getPosition(style).equals("fixed")) {
            return true;
        }
        return false;
    }

    /**
     * Checks that all direct children of this element will be laid
     * out using inline. If at least one is block the the whole thing
     * is. It skips floats, absolutes, and fixed because they don't
     * force the box into block layout.
     *
     * @param elem PARAM
     * @param c    PARAM
     * @return The blockLayout value
     */
    /* not used public static boolean isBlockLayout(Element elem, Context c) {
        NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            CalculatedStyle style = c.css.getStyle(child);
            if (isBlockNode(child, c) && !isFloated(style)) {
                //Uu.p("this layout is block");
                return true;
            }
            //grandchildren could be block! Tobe 2004-12-06
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (isBlockLayout((Element) child, c)) {
                    return true;
                }
            }
        }
        return false;
    } */

    /**
     * Gets the position attribute of the DefaultLayout class
     *
     * @param style
     * @return The position value
     */
    public static String getPosition(CascadedStyle style) {
        if (style == null) return "";//TODO: this should not be necessary?
        if (!style.hasProperty("position")) return "";
        String position = style.propertyByName("position").getValue().getCssText();
        if (position == null) {
            //TODO: check if we ever can get here. CSS-code should have taken care of this, surely?
            position = "static";
        }
        return position;
    }


    /**
     * Gets the floated attribute of the DefaultLayout class
     *
     * @param style
     * @return The floated value
     */
    public static boolean isFloated(CascadedStyle style) {
        if (style == null) return false;//TODO: this should be unnecessary?
        if (!style.hasProperty("float")) return false;
        String float_val = style.propertyByName("float").getValue().getCssText();
        if (float_val == null) {
            return false;
        }
        if (float_val.equals("left")) {
            return true;
        }
        if (float_val.equals("right")) {
            return true;
        }
        return false;
    }

    /**
     * Gets the replaced attribute of the DefaultLayout class
     *
     * @param node PARAM
     * @return The replaced value
     */
    public static boolean isReplaced(Context c, Node node) {
        return c.getRenderingContext().getLayoutFactory().isReplaced(node);
    }


}
