package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.Boxing;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.Box;

import java.awt.*;

public class Absolute {

    public static void preChildrenLayout(Context c, Box block) {
        //if (isAbsolute(content.getStyle())) {
        BlockFormattingContext bfc = new BlockFormattingContext(block);
        bfc.setWidth(block.width);
        c.pushBFC(bfc);
        //}
    }

    public static void postChildrenLayout(Context c, Box block) {
        //if (isAbsolute(content.getStyle())) {
        c.getBlockFormattingContext().doFinalAdjustments();
        c.popBFC();
        //}
    }

    public static boolean isAbsolute(CascadedStyle style) {
        if (style == null) return false;
        if (!style.hasProperty(CSSName.POSITION)) return false;//default is inline
        String position = style.propertyByName(CSSName.POSITION).getValue().getCssText();
        // Uu.p("pos = " + position);
        if (position.equals("absolute")) return true;
        return false;
    }

    public static void setupAbsolute(Box box, Context c) {
        CalculatedStyle style = c.getCurrentStyle();
        if ( style.isIdent(CSSName.POSITION, IdentValue.ABSOLUTE)) {
            if (style.hasProperty(CSSName.RIGHT)) {
                //Uu.p("prop = " + c.css.getProperty(box.getRealElement(),"right",false));
                if (style.isIdentifier(CSSName.RIGHT)) {
                    if (style.isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
                        box.right_set = false;
                        //Uu.p("right set to auto");
                    }
                } else {
                    box.right = (int) style.getFloatPropertyProportionalWidth(CSSName.RIGHT, c.getBlockFormattingContext().getWidth());
                    box.right_set = true;
                    //Uu.p("right set to : " + box.right);
                }
            }
            if (style.hasProperty(CSSName.LEFT)) {
                // c.css.getProperty(box.getRealElement(),"left",false));
                if (style.isIdentifier(CSSName.LEFT)) {
                    if (style.isIdent(CSSName.LEFT, IdentValue.AUTO)) {
                        box.left_set = false;
                        //Uu.p("left set to auto");
                    }
                } else {
                    box.left = (int) style.getFloatPropertyProportionalWidth(CSSName.LEFT, c.getBlockFormattingContext().getWidth());
                    box.left_set = true;
                    //Uu.p("left set to : " + box.left);
                }
            }
            /*
            if ( c.css.hasProperty( box.node, "left", false ) ) {
                box.left = (int)c.css.getFloatProperty( box.node, "left", 0, false );
                box.left_set = true;
            }
            */
            
            if (style.hasProperty(CSSName.BOTTOM)) {
                box.top = (int) style.getFloatPropertyProportionalHeight(CSSName.BOTTOM, c.getBlockFormattingContext().getHeight());
                box.bottom_set = true;
            }
            if (style.hasProperty(CSSName.TOP)) {
                box.top = (int) style.getFloatPropertyProportionalHeight(CSSName.TOP, c.getBlockFormattingContext().getHeight());
                box.top_set = true;
            }
            box.absolute = true;
            
            // if right and left are set calculate width
            if (box.right_set && box.left_set) {
                box.width = box.width - box.right - box.left;
            }
        }
    }

    public static void positionAbsoluteChild(Context c, Box child_box) {
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        // handle the left and right
        if (child_box.right_set) {
            child_box.x = -bfc.getX() + bfc.getWidth() - child_box.right - child_box.width
                    - bfc.getMaster().totalRightPadding(c.getCurrentStyle());
        } else {
            child_box.x = bfc.getX() + child_box.left;
        }

        // handle the top and bottom
        if (child_box.bottom_set) {
            // can't actually do this part yet, so save for later
            bfc.addAbsoluteBottomBox(child_box);
            /*
            // bottom positioning
            child_box.y = bfc.getY() + bfc.getHeight() - child_box.height - child_box.top + 50;
            Uu.p("bfc = " + bfc);
            Uu.p("bfc.height = " + bfc.getHeight());
            Uu.p("final child box = " + child_box);
            */
        } else {
            // top positioning
            child_box.y = bfc.getY() + child_box.top;
        }
    }


    public static Box generateAbsoluteBox(Context c, Content content) {
        // Uu.p("generate absolute block inline box: avail = " + avail);
        //BoxLayout layout = (BoxLayout) c.getLayout(content.getElement()); //
        //BoxLayout layout = new BoxLayout();
        Rectangle oe = c.getExtents(); // copy the extents for safety
        c.setExtents(new Rectangle(oe));
        

        //InlineBlockBox inline_block = new InlineBlockBox();
        //inline_block.content = content;
        Box box = Boxing.layout(c, content);

        
        //Uu.p("got a block box from the sub layout: " + block);
        // Rectangle bounds = new Rectangle(inline_block.x, inline_block.y,
        // inline_block.width, inline_block.height);
        c.setExtents(oe);

        box.absolute = true;

        return box;
    }


}
