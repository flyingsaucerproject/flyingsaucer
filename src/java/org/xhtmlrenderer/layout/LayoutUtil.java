package org.xhtmlrenderer.layout;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.content.TextContent;
import org.xhtmlrenderer.render.Box;

public class LayoutUtil {

    public static String getDisplay(CalculatedStyle style) {
        // u.p("checking: " + child);
        String display = style.getStringProperty("display");
        // u.p("display = " + display);
        
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
            //u.p("box is abs: " + box);
            return true;
        }
        if (box.floated) {
            return true;
        }
        return false;
    }

    public static Border getBorder(Context c, Box box) {
        if (isBlockOrInlineElementBox(c, box)) {
            // u.p("setting border for: " + box);
            if (box.border == null) {
                box.border = box.getContent().getStyle().getBorderWidth();
            }
        } else {
            // u.p("skipping border for: " + box);
        }
        return box.border;
    }

    /**
     * Gets the fixed attribute of the DefaultLayout object
     *
     * @param style
     * @return The fixed value
     */
    public static boolean isFixed(CalculatedStyle style) {
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
                //u.p("this layout is block");
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
    public static String getPosition(CalculatedStyle style) {
        String position = style.getStringProperty("position");
        if (position == null) {
            //TODO: check if we ever can get here. CSS-code should have taken care of this, surely?
            position = "static";
        }
        return position;
    }


    /**
     * Gets the floated attribute of the DefaultLayout class
     *
     * @param inline PARAM
     * @param c      PARAM
     * @return The floated value
     */
    public static boolean isFloated(Box inline, Context c) {
        CalculatedStyle style = inline.getContent().getStyle();
        return isFloated(style);
    }

    /**
     * Gets the floated attribute of the DefaultLayout class
     *
     * @param style
     * @return The floated value
     */
    public static boolean isFloated(CalculatedStyle style) {
        String float_val = style.getStringProperty("float");
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
     * Gets the blockNode attribute of the DefaultLayout class
     *
     * @param child PARAM
     * @param c     PARAM
     * @return The blockNode value
     */
    public static boolean isBlockNode(Node child, Context c) {
        //need this as a sensible default
        if (child == child.getOwnerDocument().getDocumentElement()) return true;

        if (child instanceof Element) {
            CalculatedStyle style = c.css.getStyle(child);
            String display = getDisplay(style);
            if (display != null &&
                    (display.equals("block") ||
                    display.equals("table") ||
                    //TODO:table cell should not be block according to spec. What did I miss? tobe
                    display.equals("table-cell") ||
                    display.equals("list-item"))
            ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the hiddenNode attribute of the DefaultLayout class
     *
     * @param child PARAM
     * @param c     PARAM
     * @return The hiddenNode value
     */
    /* not used anymore: tobe 2004-12-10 public static boolean isHiddenNode(Node child, Context c) {
        if (child instanceof Element) {
            CalculatedStyle style = c.css.getStyle(child);
            String display = getDisplay(style);//c.css.getStringProperty( el, "display", false );
            if (display != null && display.equals("none")) {
                return true;
            }
        }
        return false;
    } */

    /**
     * Gets the replaced attribute of the DefaultLayout class
     *
     * @param node PARAM
     * @return The replaced value
     */
    public static boolean isReplaced(Context c, Node node) {
        return c.getRenderingContext().getLayoutFactory().isReplaced(node);
    }

    /**
     * Gets the floatedBlock attribute of the DefaultLayout class
     *
     * @param node PARAM
     * @param c    PARAM
     * @return The floatedBlock value
     */
    public static boolean isFloatedBlock(Node node, Context c) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        }

        CalculatedStyle style = c.css.getStyle(node);
        //not used: String display = getDisplay(c, el);
        if (isFloated(style)) {
            return true;
        }
        return false;
    }


    public static boolean isBlockOrInlineElementBox(Context c, Box box) {
        return !(box.getContent() instanceof TextContent);//TODO: check. This does seem to match what was intended, but the name is confusing
    }


    /*not used now public static boolean hasIdent(Context c, Element elem, String property, boolean inherit) {
        return c.css.getStyle(elem).isIdentifier(property);
    } */


    public static boolean isListItem(Box box) {
        CalculatedStyle style = box.getContent().getStyle();
        String display = getDisplay(style);
        if (display.equals("list-item")) {
            return true;
        }
        return false;
    }


}
