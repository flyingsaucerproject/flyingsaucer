package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.render.Box;

public class Absolute {

    public static void preChildrenLayout(Context c, Box block) {
        if (isAbsolute(block)) {
            BlockFormattingContext bfc = new BlockFormattingContext(block);
            bfc.setWidth(block.width);
            c.pushBFC(bfc);
        }
    }

    public static void postChildrenLayout(Context c, Box block) {
        if (isAbsolute(block)) {
            c.getBlockFormattingContext().doFinalAdjustments();
            c.popBFC();
        }
    }

    private static boolean isAbsolute(Box box) {
        CascadedStyle style = box.content.getStyle();
        if(style == null) return false;
            if (!style.hasProperty(CSSName.POSITION)) return false;//default is inline
            String position = style.propertyByName(CSSName.POSITION).getValue().getCssText();
            if (position.equals("absolute")) return true;
            return false;
    }

    public static void setupAbsolute(Box box, Context c) {
        CalculatedStyle style = c.getCurrentStyle();
        String position = LayoutUtil.getPosition(style);
        if (position.equals("absolute")) {
            if (style.hasProperty("right")) {
                //Uu.p("prop = " + c.css.getProperty(box.getRealElement(),"right",false));
                if (style.isIdentifier("right")) {
                    if (style.getStringProperty("right").equals("auto")) {
                        box.right_set = false;
                        //Uu.p("right set to auto");
                    }
                } else {
                    box.right = (int) style.getFloatPropertyRelative("right", 0);
                    box.right_set = true;
                    //Uu.p("right set to : " + box.right);
                }
            }
            if (style.hasProperty("left")) {
                //Uu.p("prop = " + c.css.getProperty(box.getRealElement(),"left",false));
                if (style.isIdentifier("left")) {
                    if (style.getStringProperty("left").equals("auto")) {
                        box.left_set = false;
                        //Uu.p("left set to auto");
                    }
                } else {
                    box.left = (int) style.getFloatPropertyRelative("left", 0);
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
            
            if (style.hasProperty("bottom")) {
                box.top = (int) style.getFloatPropertyRelative("bottom", 0);
                box.bottom_set = true;
            }
            if (style.hasProperty("top")) {
                box.top = (int) style.getFloatPropertyRelative("top", 0);
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
                    - bfc.getMaster().totalRightPadding();
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

}
