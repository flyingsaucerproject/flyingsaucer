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
import org.w3c.dom.css.*;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.style.*;
import org.xhtmlrenderer.render.BackgroundPainter;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.BorderPainter;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.Renderer;
import org.xhtmlrenderer.render.BoxRenderer;
import org.xhtmlrenderer.render.ListItemPainter;
import org.xhtmlrenderer.util.GraphicsUtil;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.util.x;
import org.xhtmlrenderer.css.constants.CSSName;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class BoxLayout extends DefaultLayout {

    /** Constructor for the BoxLayout object */
    public BoxLayout() { }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param node  PARAM
     * @return      Returns
     */
    public Box createBox( Context c, Node node ) {
        BlockBox block = new BlockBox();
        block.node = node;
        String attachment = c.css.getStringProperty( block.getRealElement(), "background-attachment", false );
        if(attachment != null && attachment.equals("fixed")) {
            block.setChildrenExceedBounds(true);
        }
        return block;
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     * @param c    PARAM
     */
    public void prepareBox( Box box, Context c ) {
        Border border = getBorder( c, box );
        Border padding = getPadding( c, box );
        Border margin = getMargin( c, box );
    }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param elem  PARAM
     * @return      Returns
     */
    public Box layout( Context c, Element elem ) {
        // install a block formatting context for the body,
        // ie. if it's null.
        
        // this is to keep track of when we are inside of a form
        if(LayoutFactory.isForm(elem)) {
            if ( elem.hasAttribute( "name" ) ) {
                String name = elem.getAttribute( "name" );
                String action = elem.getAttribute( "action" );
                c.setForm( name, action );
            }
        }

        BlockBox block = (BlockBox)createBox( c, elem );
        // set up the bfc
        BlockFormattingContext old_bfc = null;
        boolean set_bfc = false;
        if(c.getBlockFormattingContext() == null) {
            BlockFormattingContext bfc = new BlockFormattingContext(block);
            c.setBlockFormattingContext(bfc);
            set_bfc = true;
            old_bfc = null;
            //u.p("extents here = " + c.getExtents());
            bfc.setWidth((int)c.getExtents().getWidth());
        }
        Rectangle oe = c.getExtents();
        c.setExtents( new Rectangle( oe ) );
        adjustWidth( c, block );
        adjustHeight( c, block );
        block.x = c.getExtents().x;
        block.y = c.getExtents().y;

        prepareBox( block, c );
        Border border = getBorder( c, block );
        Border padding = getPadding( c, block );
        Border margin = getMargin( c, block );
        getBackgroundColor( c, block );


        // do children's layout
        boolean old_sub = c.isSubBlock();
        c.setSubBlock( false );
        int tx = block.totalLeftPadding();
        int ty = block.totalTopPadding();
        c.translate(tx,ty);
        layoutChildren( c, block );
        c.translate(-tx,-ty);
        c.setSubBlock( old_sub );

        // calculate the inner width
        
        block.width = margin.left + border.left + padding.left + block.width +
                padding.right + border.right + margin.right;
        block.height = margin.top + border.top + padding.top + block.height +
                padding.bottom + border.bottom + margin.bottom;
                

        // if this is a fixed height, then set it explicitly
        /*
         * if (!block.auto_height) {
         * contents.height = block.height;
         * }
         */
        //restore the extents
        c.setExtents( oe );

        // account for special positioning
        setupRelative( c, block );
        setupAbsolute( c, block );
        setupFixed( c, block );
        setupFloat( c, block );

        this.contents_height = block.height;
        if(LayoutFactory.isForm(elem)) {
            if ( elem.hasAttribute( "name" ) ) {
                c.setForm( null, null );
            }
        }
        if(set_bfc) {
            c.setBlockFormattingContext(old_bfc);
        }
        return block;
    }


    // calculate the width based on css and available space
    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param block  PARAM
     */
    private void adjustWidth( Context c, BlockBox block ) {
        if ( !block.isElement() ) {
            return;
        }
        // initalize the width to all the available space
        //block.width = c.getExtents().width;
        Element elem = block.getElement();
        if ( c.css.hasProperty( elem, "width", false ) ) {
            // if it is a sub block then don't mess with the width
            if ( c.isSubBlock() ) {
                if ( !elem.getNodeName().equals( "td" ) ) {
                    u.p( "ERRRRRRRRRRORRRR!!! in a sub block that's not a TD!!!!" );
                }
                return;
            }
            // u.p("block = " + block);
            // u.p("extents = " + c.getExtents());
            // CalculatedStyle style = c.css.getStyle(elem);
            // u.p("style = " + style);
            // u.p("comp value = " + style.propertyByName("width").computedValue());
            // u.p("width = " + style.propertyByName("width")
            //     .computedValue().getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE));
            // u.p("type = " + style.propertyByName("width")
            //     .computedValue().getPrimitiveType());
            
            //float new_width = relativeHack(c.css.getFloatProperty( elem, "width", c.getExtents().width, false ),
            //    c.getExtents().width);
            float new_width = c.css.getFloatProperty( elem, "width", c.getExtents().width, false );
            //u.p("new width = " + new_width);
            c.getExtents().width = (int)new_width;
            block.width = (int)new_width;
            block.auto_width = false;
            //u.p("adjusted width: = " + block);
        }
    }
    
    private float relativeHack(float width, float parent_width) {
        u.p("calc width = " + width);
        if(width > 0.0 && width <= 1.0) {
            return width * parent_width;
        }
        return width;
    }

    // calculate the height based on css and available space
    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param block  PARAM
     */
    private void adjustHeight( Context c, BlockBox block ) {
        if ( !block.isElement() ) {
            return;
        }
        Element elem = block.getElement();
        if ( c.css.hasProperty( elem, "height" ) ) {
            float new_height = c.css.getFloatProperty( elem, "height", c.getExtents().height );
            c.getExtents().height = (int)new_height;
            block.height = (int)new_height;
            block.auto_height = false;
        }
    }


    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     Returns
     */
    public Box layoutChildren( Context c, Box box ) {
        BlockBox block = (BlockBox)box;
        c.shrinkExtents( block );
        super.layoutChildren( c, block );
        c.unshrinkExtents( block );
        return block;
    }




    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    
    public void setupFloat( Context c, Box box ) {
        if( isFloated(box.node, c)) {
            String float_val = c.css.getStringProperty( box.node, CSSName.FLOAT, false );
            if ( float_val == null ) {
                float_val = "none";
            }
            if ( float_val.equals( "none" ) ) {
                return;
            }
            box.floated = true;
            if ( float_val.equals( "left" ) ) {
                c.getBlockFormattingContext().addLeftFloat(box);
            }
            if ( float_val.equals( "right" ) ) {
                c.getBlockFormattingContext().addRightFloat(box);
            }
        }
    }

    /**
     * Gets the listItem attribute of the BoxLayout object
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     The listItem value
     */
    public static boolean isListItem( Context c, Box box ) {
        String display = c.css.getStringProperty( (Element)box.node, "display", false );
        //u.p("display = " + display);
        if ( display.equals( "list-item" ) ) {
            return true;
        }
        return false;
    }

    // === caching accessors =========

    /**
     * Gets the border attribute of the BoxLayout object
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     The border value
     */


    /**
     * Gets the padding attribute of the BoxLayout object
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     The padding value
     */
    public static Border getPadding( Context c, Box box ) {
        if(isBlockOrInlineElementBox(c,box)) {
            if ( box.padding == null ) {
                box.padding = c.css.getPaddingWidth( box.getRealElement() );
            }
        }
        return box.padding;
    }


    /**
     * Gets the margin attribute of the BoxLayout object
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     The margin value
     */
    public static Border getMargin( Context c, Box box ) {
        if(isBlockOrInlineElementBox(c,box)) {
            if ( box.margin == null ) {
                box.margin = c.css.getMarginWidth( box.getRealElement() );
            }
        }
        return box.margin;
    }

    /**
     * Gets the backgroundColor attribute of the BoxLayout object
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     The backgroundColor value
     */
    public static Color getBackgroundColor( Context c, Box box ) {
        if(isBlockOrInlineElementBox(c,box)) {
            if ( box.background_color == null ) {
                Object obj = c.css.getProperty( box.getRealElement(), "background-color", false );
                //u.p("got : " + obj);
                if ( obj.toString().equals( "transparent" ) ) {
                    box.background_color = new Color( 0, 0, 0, 0 );
                    return box.background_color;
                }
                box.background_color = c.css.getBackgroundColor( box.getRealElement() );
            }
        }
        return box.background_color;
    }


    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public static void setupRelative( Context c, Box box ) {
        String position = getPosition( c, box );
        if ( position.equals( "relative" ) ) {
            if ( c.css.hasProperty( box.node, "right", false ) ) {
                box.left = -(int)c.css.getFloatProperty( box.node, "right", 0, false );
            }
            if ( c.css.hasProperty( box.node, "bottom", false ) ) {
                box.top = -(int)c.css.getFloatProperty( box.node, "bottom", 0, false );
            }
            if ( c.css.hasProperty( box.node, "top", false ) ) {
                box.top = (int)c.css.getFloatProperty( box.node, "top", 0, false );
            }
            if ( c.css.hasProperty( box.node, "left", false ) ) {
                box.left = (int)c.css.getFloatProperty( box.node, "left", 0, false );
            }
            box.relative = true;
        }
    }

    public static boolean hasIdent(Context c, Element elem, String property, boolean inherit) {
        CSSValue prop = c.css.getProperty(elem,property,inherit);
        CSSPrimitiveValue pval = (CSSPrimitiveValue)prop;
        //u.p("prim type = " + pval.getPrimitiveType());
        if ( pval.getPrimitiveType() == pval.CSS_IDENT) {
            return true;
        }
        return false;
    }
    
    public static void setupAbsolute( Context c, Box box ) {
        String position = getPosition( c, box );
        if ( position.equals( "absolute" ) ) {
            if ( c.css.hasProperty( box.node, "right", false ) ) {
                //u.p("prop = " + c.css.getProperty(box.getRealElement(),"right",false));
                if(hasIdent(c,box.getRealElement(),"right",false)) {
                    if(c.css.getStringProperty(box.node,"right",false).equals("auto")) {
                        box.right_set = false;
                        //u.p("right set to auto");
                    }
                } else {
                    box.right = (int)c.css.getFloatProperty( box.node, "right", 0, false );
                    box.right_set = true;
                    //u.p("right set to : " + box.right);
                }
            }
            if ( c.css.hasProperty( box.node, "left", false ) ) {
                //u.p("prop = " + c.css.getProperty(box.getRealElement(),"left",false));
                if(hasIdent(c,box.getRealElement(),"left",false)) {
                    if(c.css.getStringProperty(box.node,"left",false).equals("auto")) {
                        box.left_set = false;
                        //u.p("left set to auto");
                    }
                } else {
                    box.left = (int)c.css.getFloatProperty( box.node, "left", 0, false );
                    box.left_set = true;
                    //u.p("left set to : " + box.left);
                }
            }
            /*
            if ( c.css.hasProperty( box.node, "left", false ) ) {
                box.left = (int)c.css.getFloatProperty( box.node, "left", 0, false );
                box.left_set = true;
            }
            */
            
            if ( c.css.hasProperty( box.node, "bottom", false ) ) {
                box.top = -(int)c.css.getFloatProperty( box.node, "bottom", 0, false );
            }
            if ( c.css.hasProperty( box.node, "top", false ) ) {
                box.top = (int)c.css.getFloatProperty( box.node, "top", 0, false );
            }
            box.absolute = true;
            
            // if right and left are set calculate width
            if(box.right_set && box.left_set) {
                box.width = box.width - box.right - box.left;
            }
        }
    }

    public Renderer getRenderer() {
        return new BoxRenderer();
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.18  2004/11/12 18:51:00  joshy
 * fixed repainting issue for background-attachment: fixed
 * added static util methods and get minimum size to graphics 2d renderer
 * added test for graphics 2d renderer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.17  2004/11/12 17:05:24  joshy
 * support for fixed positioning
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/09 15:53:48  joshy
 * initial support for hover (currently disabled)
 * moved justification code into it's own class in a new subpackage for inline
 * layout (because it's so blooming complicated)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/11/08 20:50:58  joshy
 * improved float support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/08 15:10:10  joshy
 * added support for styling :first-letter inline boxes
 * updated the absolute positioning tests
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
 * Revision 1.12  2004/11/06 22:49:51  joshy
 * cleaned up alice
 * initial support for inline borders and backgrounds
 * moved all of inlinepainter back into inlinerenderer, where it belongs.
 *
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/05 18:45:14  joshy
 * support for floated blocks (not just inline blocks)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/04 15:35:44  joshy
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
 * Revision 1.7  2004/10/28 02:13:40  joshy
 * finished moving the painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/10/28 01:34:23  joshy
 * moved more painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/10/26 00:13:14  joshy
 * added threaded layout support to the HTMLPanel
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/23 13:46:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

