package org.xhtmlrenderer.layout.block;

import java.awt.*;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;

public class Fixed {
    public static void positionFixedChild(Context c, Box box) {
        if ( LayoutUtil.isFixed( c, box ) ) {
            Point origin = c.getOriginOffset();
            box.x = 0;
            box.y = 0;
            box.x -= origin.x;
            box.y -= origin.y;
        }
    }

    public static void setupFixed( Context c, Box box ) {
        if ( LayoutUtil.isFixed( c, box ) ) {
            box.fixed = true;
            box.setChildrenExceedBounds(true);
            
            if ( c.css.hasProperty( box.node, "top", false ) ) {
                box.top = (int)c.css.getFloatProperty( box.node, "top", 0, false );
                box.top_set = true;
            }
            if ( c.css.hasProperty( box.node, "right", false ) ) {
                box.right = (int)c.css.getFloatProperty( box.node, "right", 0, false );
                box.right_set = true;
            }
            if ( c.css.hasProperty( box.node, "bottom", false ) ) {
                box.bottom = (int)c.css.getFloatProperty( box.node, "bottom", 0, false );
                box.bottom_set = true;
            }
            if ( c.css.hasProperty( box.node, "left", false ) ) {
                box.left = (int)c.css.getFloatProperty( box.node, "left", 0, false );
                box.left_set = true;
            }
            
        }
    }
}
