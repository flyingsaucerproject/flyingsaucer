/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.layout;


import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.render.AnonymousBlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.value.BorderColor;
import org.xhtmlrenderer.render.Renderer;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.util.*;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class DefaultLayout implements Layout {
    /** Description of the Field */
    public int contents_height;

    /*
     * ============= layout code ===================
     */
    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param elem  PARAM
     * @return      Returns
     */
    public Box layout( Context c, Element elem ) {
        //u.p("Layout.layout(): " + elem);
        Box box = createBox( c, elem );
        return layoutChildren( c, box );
    }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param node  PARAM
     * @return      Returns
     */
    public Box createBox( Context c, Node node ) {
        Box box = new Box();
        box.node = node;
        return box;
    }

    /*
     * // we need to pass the graphics incase we need to grab font info for sizing
     * public Rectangle layoutNode(Context c, Node node) {
     * return new Rectangle(0,0);
     * }
     */
    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     Returns
     */
    public Box layoutChildren( Context c, Box box ) {
        //u.p("Layout.layoutChildren: " + box);

        Element elem = (Element)box.node;
        // prepare for the list items
        int old_counter = c.getListCounter();
        c.setListCounter( 0 );

        // for each child
        NodeList nl = elem.getChildNodes();
        for ( int i = 0; i < nl.getLength(); i++ ) {
            Node child = nl.item( i );

            // get the layout for this child
            Layout layout = LayoutFactory.getLayout( child );
            if ( layout == null ) {
                continue;
            }
            if ( layout instanceof NullLayout ) {
                continue;
            }
            if ( LayoutFactory.isBreak(child)) {//.getNodeName().equals( "br" ) ) {
                continue;
            }
            if ( child.getNodeType() == child.COMMENT_NODE ) {
                continue;
            }

            Box child_box = null;
            if ( child.getNodeType() == child.ELEMENT_NODE ) {
                // update the counter for printing OL list items
                c.setListCounter( c.getListCounter() + 1 );

                Element child_elem = (Element)child;
                // execute the layout and get the return bounds
                //c.parent_box = box;
                c.placement_point = new Point( 0, box.height );
                c.getBlockFormattingContext().translate(0,box.height);
                //u.p("default doing layout on" + child_elem);
                //u.p("with layout: " + layout);
                //u.p("yoff = " + c.getBlockFormattingContext().getY());
                child_box = layout.layout( c, child_elem );
                c.getBlockFormattingContext().translate(0,-box.height);
                child_box.list_count = c.getListCounter();
            } else {
                // create anonymous block box
                // prepare the node list of the text children
                //child_box = new AnonymousBlockBox(child);
                // call layout
                child_box = ( (AnonymousBoxLayout)layout ).layout( c, elem, child );

                // skip text children if the prev_child == anonymous block box
                // because that means they were sucked into this block
                Node last_node = ( (AnonymousBlockBox)child_box ).last_node;
                // if anonymous box is only one node wide then skip this
                // junk
                if ( child != last_node ) {
                    while ( true ) {
                        i++;
                        Node ch = nl.item( i );
                        //u.p("trying to skip: " + ch);
                        if ( ch == last_node ) {
                            break;
                        }
                    }
                }

            }
            box.addChild( child_box );
            // set the child_box location
            child_box.x = 0;
            child_box.y = box.height;

            //joshy fix the 'fixed' stuff later
            // if fixed or abs then don't modify the final layout bounds
            // because fixed elements are removed from normal flow
            if ( child_box.fixed ) {
                // put fixed positioning in later
            }
            
            if ( child_box.absolute ) {
                positionAbsoluteChild(c,child_box);
            }
            
            // skip adjusting the parent box if the child
            // doesn't affect flow layout
            if (isOutsideNormalFlow(child_box)) {
                continue;
            }

            // increase the final layout width if the child was greater
            if ( child_box.width > box.width ) {
                //u.p("upping: " + box.width + " -> " + child_box.width);
                box.width = child_box.width;
            }

            // increase the final layout height by the height of the child
            box.height += child_box.height;
            //u.p("final extents = " + lt);
            //u.p("final child box was: " + child_box);
        }
        c.addMaxWidth( box.width );

        c.setListCounter( old_counter );

        return box;
    }


    
    
    public Renderer getRenderer() {
        return new DefaultRenderer();
    }

    
    public boolean isOutsideNormalFlow(Box box) {
        if(box.fixed) {
            return true;
        }
        if(box.absolute) {
            return true;
        }
        if(box.floated) {
            return true;
        }
        return false;
    }
    
    public void positionAbsoluteChild(Context c, Box child_box) {
        //u.p("modifying it");
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        // u.p("bfc = " + bfc);
        // u.p("child = " + child_box);
        // u.p("parent = " + child_box.getParent());
        // u.p("width = " + bfc.getWidth());
        // handle the left and right
        if(child_box.right_set) {
            // joshy: this doesn't seem right. shouldn't there be a call
            // to bfc.getX() when doing the right positioning?
            child_box.x = -bfc.getX() + bfc.getWidth() - child_box.right - child_box.width
             - bfc.getMaster().totalRightPadding();
            ;
        } else {
            child_box.x = bfc.getX() + child_box.left;
        }
        // handle the top
        child_box.y = bfc.getY() + child_box.top;
    }


    /*
     * =========== utility code =============
     */
    /**
     * Gets the layout attribute of the DefaultLayout object
     *
     * @param node  PARAM
     * @return      The layout value
     */
     /*
    public Layout getLayout( Node node ) {
        return LayoutFactory.getLayout( node );
    }
    */

    /**
     * Gets the fixed attribute of the DefaultLayout object
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     The fixed value
     */
    public boolean isFixed( Context c, Box box ) {
        if ( getPosition( c, box ).equals( "fixed" ) ) {
            return true;
        }
        return false;
    }

    /**
     * Gets the blockLayout attribute of the DefaultLayout object
     *
     * @param elem  PARAM
     * @param c     PARAM
     * @return      The blockLayout value
     */
    public static boolean isBlockLayout( Element elem, Context c ) {
        NodeList children = elem.getChildNodes();
        for ( int i = 0; i < children.getLength(); i++ ) {
            Node child = children.item( i );
            if ( isBlockNode( child, c ) ) {
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
        return isFloated( inline.node, c );
    }

    /**
     * Gets the floated attribute of the DefaultLayout class
     *
     * @param node  PARAM
     * @param c     PARAM
     * @return      The floated value
     */
    public static boolean isFloated( Node node, Context c ) {
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
            // u.p("checking: " + child);
            Element el = (Element)child;
            String display = c.css.getStringProperty( el, "display", false );
            // u.p("display = " + display);
            if ( display != null && 
                (display.equals( "block" ) ||
                 display.equals( "table-cell" ))
               ) {
                if(isFloated(el,c)) {
                    return true;
                }
                if ( !isFloated( el, c ) ) {
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
            String display = c.css.getStringProperty( el, "display", false );
            //u.p("display = " + display);
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
    public static boolean isReplaced( Node node ) {
        return LayoutFactory.isReplaced(node);
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
        String display = c.css.getStringProperty( el, "display", false );
        //u.p("display = " + display);
        if ( display != null && display.equals( "block" ) ) {
            if ( isFloated( node, c ) ) {
                //u.p("it's a floated block");
                return true;
            }
        }
        return false;
    }


    public static Border getBorder( Context c, Box box ) {
        //if text but parent is not block, then do border
        // if block then do border
        // if text but parent is block, then no border
        if(isBlockOrInlineElementBox(c,box)) {
           //u.p("doing border for: " + box);
           //u.p("hascode = " + box.hashCode());
            if ( box.border == null ) {
                //if(box instanceof BlockBox) {
                    box.border = c.css.getBorderWidth( box.getRealElement() );
                //}
            }
        }
        return box.border;
    }
    
    public static boolean isBlockOrInlineElementBox(Context c, Box box) {
        if((box.node.getNodeType()==Node.TEXT_NODE && 
           !DefaultLayout.isBlockNode(box.getRealElement(),c)) ||
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

    public void restyle(Context ctx, Box box) {
        box.color = ctx.css.getColor(box.getRealElement());
        box.setBorderColor(ctx.css.getBorderColor( box.getRealElement() ));
        box.border_style = ctx.css.getStringProperty( box.getRealElement(), "border-top-style" );
        box.background_color = ctx.css.getBackgroundColor( box.getRealElement() );
    }
    /*public void restyleHover(Context c, Box box) {
        box.color = Color.black;
        box.setBorderColor(new BorderColor(new Color(.7f,.7f,1f)));
        box.border_style = "solid";
        box.background_color = new Color(.9f,.9f,.9f);
    }*/


}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.17  2004/11/12 02:42:19  joshy
 * context cleanup
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/12 00:20:29  tobega
 * Set up the HoverListener to work properly. Colors are changing!
 *
 * Revision 1.15  2004/11/09 15:53:48  joshy
 * initial support for hover (currently disabled)
 * moved justification code into it's own class in a new subpackage for inline
 * layout (because it's so blooming complicated)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/08 20:50:58  joshy
 * improved float support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/07 13:39:17  joshy
 * fixed missing borders on the table
 * changed td and th to display:table-cell
 * updated isBlockLayout() code to fix double border problem with tables
 *
 * -j
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/11/05 18:45:14  joshy
 * support for floated blocks (not just inline blocks)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/05 16:39:34  joshy
 * more float support
 * added border bug test
 * -j
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/04 15:35:45  joshy
 * initial float support
 * includes right and left float
 * cannot have more than one float per line per side
 * floats do not extend beyond enclosing block
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/03 23:54:33  joshy
 * added hamlet and tables to the browser
 * more support for absolute layout
 * added absolute layout unit tests
 * removed more dead code and moved code into layout factory
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/03 15:17:04  joshy
 * added intial support for absolute positioning
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/11/02 20:44:55  joshy
 * put in some prep work for float support
 * removed some dead debugging code
 * moved isBlock code to LayoutFactory
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/10/28 02:13:41  joshy
 * finished moving the painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/10/28 01:34:23  joshy
 * moved more painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/27 14:03:37  joshy
 * added initial viewport repainting support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/27 13:17:00  joshy
 * beginning to split out rendering code
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.2  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

