package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;

public class Absolute {
    
     public static void positionAbsoluteChild(Context c, Box child_box) {
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        // handle the left and right
        if(child_box.right_set) {
            child_box.x = -bfc.getX() + bfc.getWidth() - child_box.right - child_box.width
                          - bfc.getMaster().totalRightPadding();
        } else {
            child_box.x = bfc.getX() + child_box.left;
        }
        // handle the top
        child_box.y = bfc.getY() + child_box.top;
    }

    public static void setupAbsolute(Context c, Box box) {
        String position = LayoutUtil.getPosition(c, box);
        if (position.equals("absolute")) {
            if (c.css.hasProperty(box.node, "right", false)) {
                //u.p("prop = " + c.css.getProperty(box.getRealElement(),"right",false));
                if (LayoutUtil.hasIdent(c, box.getRealElement(), "right", false)) {
                    if (c.css.getStringProperty(box.node, "right", false).equals("auto")) {
                        box.right_set = false;
                        //u.p("right set to auto");
                    }
                } else {
                    box.right = (int) c.css.getFloatProperty(box.node, "right", 0, false);
                    box.right_set = true;
                    //u.p("right set to : " + box.right);
                }
            }
            if (c.css.hasProperty(box.node, "left", false)) {
                //u.p("prop = " + c.css.getProperty(box.getRealElement(),"left",false));
                if (LayoutUtil.hasIdent(c, box.getRealElement(), "left", false)) {
                    if (c.css.getStringProperty(box.node, "left", false).equals("auto")) {
                        box.left_set = false;
                        //u.p("left set to auto");
                    }
                } else {
                    box.left = (int) c.css.getFloatProperty(box.node, "left", 0, false);
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
            
            if (c.css.hasProperty(box.node, "bottom", false)) {
                box.top = -(int) c.css.getFloatProperty(box.node, "bottom", 0, false);
            }
            if (c.css.hasProperty(box.node, "top", false)) {
                box.top = (int) c.css.getFloatProperty(box.node, "top", 0, false);
            }
            box.setAbsolute(true);
            
            // if right and left are set calculate width
            if (box.right_set && box.left_set) {
                box.width = box.width - box.right - box.left;
            }
        }
    }
    
}
