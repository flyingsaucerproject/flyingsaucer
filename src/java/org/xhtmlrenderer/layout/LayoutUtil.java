package org.xhtmlrenderer.layout;

import org.w3c.dom.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.css.*;
import org.xhtmlrenderer.util.*;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class LayoutUtil {

    public static String getDisplay(Context c, Element e) {
        // u.p("checking: " + child);
        String display = c.css.getStringProperty( e, "display", false );
        // u.p("display = " + display);
        
        // override for floated
        if(isFloated(c,e)) {
            return "block";
        }
        
        return display;
    }
        
    
    public static boolean isOutsideNormalFlow(Box box) {
        if(box.fixed) {
            return true;
        }
        if(box.isAbsolute()) {
            //u.p("box is abs: " + box);
            return true;
        }
        if(box.floated) {
            return true;
        }
        return false;
    }

    public static Border getBorder( Context c, Box box ) {
        if(isBlockOrInlineElementBox(c,box)) {
            if ( box.border == null ) {
                box.border = c.css.getBorderWidth( box.getRealElement() );
            }
        }
        return box.border;
    }

    /**
     * Gets the fixed attribute of the DefaultLayout object
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     The fixed value
     */
    public static boolean isFixed( Context c, Box box ) {
        if ( getPosition( c, box ).equals( "fixed" ) ) {
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
     * @param elem  PARAM
     * @param c     PARAM
     * @return      The blockLayout value
     */
    public static boolean isBlockLayout( Element elem, Context c ) {
        NodeList children = elem.getChildNodes();
        for ( int i = 0; i < children.getLength(); i++ ) {
            Node child = children.item( i );
            if ( isBlockNode( child, c ) && !isFloated( c, child ) ) {
                //u.p("this layout is block");
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the position attribute of the DefaultLayout class
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     The position value
     */
    public static String getPosition( Context c, Box box ) {
        String position = c.css.getStringProperty( box.node, "position", false );
        if ( position == null ) {
            position = "static";
        }
        return position;
    }


    /**
     * Gets the floated attribute of the DefaultLayout class
     *
     * @param inline  PARAM
     * @param c       PARAM
     * @return        The floated value
     */
    public static boolean isFloated( Box inline, Context c ) {
        return isFloated( c, inline.node);
    }

    /**
     * Gets the floated attribute of the DefaultLayout class
     *
     * @param node  PARAM
     * @param c     PARAM
     * @return      The floated value
     */
    public static boolean isFloated( Context c, Node node ) {
        String float_val = c.css.getStringProperty( node, "float" );
        if ( float_val == null ) {
            return false;
        }
        if ( float_val.equals( "left" ) ) {
            return true;
        }
        if ( float_val.equals( "right" ) ) {
            return true;
        }
        return false;
    }

    /**
     * Gets the blockNode attribute of the DefaultLayout class
     *
     * @param child  PARAM
     * @param c      PARAM
     * @return       The blockNode value
     */
    public static boolean isBlockNode( Node child, Context c ) {
        if ( child instanceof Element ) {
            String display = getDisplay(c,(Element)child);
            if ( display != null && 
                (display.equals( "block" ) ||
                 display.equals( "table-cell" ))
               ) {
                if(isFloated(c,(Element)child)) {
                    return true;
                }
                if ( !isFloated( c,(Element)child ) ) {
                    //u.p(child.getNodeName() + " is a block");
                    return true;
                } else {
                    // u.p("isBlockNode() found a floated block");
                }
            }
        }
        return false;
    }

    /**
     * Gets the hiddenNode attribute of the DefaultLayout class
     *
     * @param child  PARAM
     * @param c      PARAM
     * @return       The hiddenNode value
     */
    public static boolean isHiddenNode( Node child, Context c ) {
        if ( child instanceof Element ) {
            Element el = (Element)child;
            String display = getDisplay(c,el);//c.css.getStringProperty( el, "display", false );
            if ( display != null && display.equals( "none" ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the replaced attribute of the DefaultLayout class
     *
     * @param node  PARAM
     * @return      The replaced value
     */
    public static boolean isReplaced(Context c, Node node ) {
        return c.getRenderingContext().getLayoutFactory().isReplaced(node);
    }

    /**
     * Gets the floatedBlock attribute of the DefaultLayout class
     *
     * @param node  PARAM
     * @param c     PARAM
     * @return      The floatedBlock value
     */
    public static boolean isFloatedBlock( Node node, Context c ) {
        if ( node.getNodeType() != node.ELEMENT_NODE ) {
            return false;
        }

        Element el = (Element)node;
        String display = getDisplay(c,el);
        if ( isFloated( c, node ) ) {
            return true;
        }
        return false;
    }


    
    public static boolean isBlockOrInlineElementBox(Context c, Box box) {
        if((box.node.getNodeType()==Node.TEXT_NODE && 
           !LayoutUtil.isBlockNode(box.getRealElement(),c)) ||
           box.isElement()) {
           // u.p("box = " + box);
           // u.p("node type = " + box.node.getNodeType());
           // u.p("text node == " + Node.TEXT_NODE);
           // u.p("real element = " + box.getRealElement());
           // u.p("is block node = " + DefaultLayout.isBlockNode(box.getRealElement(),c));
           // u.p("is element = " + box.isElement());
           return true;
        }
        return false;
    }
    
    
    public static boolean hasIdent(Context c, Element elem, String property, boolean inherit) {
        CSSValue prop = c.css.getProperty(elem, property, inherit);
        CSSPrimitiveValue pval = (CSSPrimitiveValue) prop;
        //u.p("prim type = " + pval.getPrimitiveType());
        if (pval.getPrimitiveType() == pval.CSS_IDENT) {
            return true;
        }
        return false;
    }
    
    
    public static boolean isListItem(Context c, Box box) {
        String display = getDisplay(c,(Element) box.node);
        if (display.equals("list-item")) {
            return true;
        }
        return false;
    }


}
