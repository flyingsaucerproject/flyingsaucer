package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.render.Box;

public class LayoutUtil {

    public static IdentValue getDisplay(CascadedStyle style) {
        // Uu.p("checking: " + child);
        IdentValue display = style.getIdent(CSSName.DISPLAY);
        return ( isFloated(style) ? IdentValue.BLOCK : display );
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

    /*public static Border getBorder(CalculatedStyle style) {
        Border border = new Border(0, 0, 0, 0);
        border = style.getBorderWidth();
        String border_style = style.getStringProperty(CSSName.BORDER_STYLE_TOP);
        if (border_style.equals("none")) {
            border = new Border(0, 0, 0, 0);
        }
        return border;
    }*/

    /**
     * Gets the fixed attribute of the DefaultLayout object
     *
     * @param style
     * @return The fixed value
     */
    public static boolean isFixed(CascadedStyle style) {
        /*if (getPosition(style).equals("fixed")) {
            return true;
        }
        return false;*/
        IdentValue position = getPosition(style);
        return position != null && position == IdentValue.FIXED;
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
    public static IdentValue getPosition(CascadedStyle style) {
        if (style == null) return null;//TODO: this should not be necessary?
        IdentValue position = style.getIdent(CSSName.POSITION);
        return ( position == null ? IdentValue.STATIC : position );
    }


    /**
     * Gets the floated attribute of the DefaultLayout class
     *
     * @param style
     * @return The floated value
     */
    public static boolean isFloated(CascadedStyle style) {
        if (style == null) return false;//TODO: this should be unnecessary?
        IdentValue floatVal = style.getIdent(CSSName.FLOAT);
        return floatVal != null && ( floatVal == IdentValue.LEFT || floatVal == IdentValue.RIGHT );
    }
}
