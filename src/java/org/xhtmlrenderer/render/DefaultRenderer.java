package org.xhtmlrenderer.render;

import java.awt.*;
import java.awt.geom.*;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.util.*;

public class DefaultRenderer implements Renderer {
//    public int contents_height;
/*
     * ========== painting code ==============
     */
    // the core function that implements the recursive layout/paint loop
    // perhaps we should call it something else?
    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paint( Context c, Box box ) {
        //u.p("Layout.paint() " + box);
        //Point old_cursor = new Point(c.getCursor());
        //Rectangle contents = layoutChildren(c,elem);
        //c.cursor = old_cursor;
        paintBackground( c, box );
        paintComponent( c, box );
        paintChildren( c, box );
        paintBorder( c, box );
        //this.contents_height = box.height;
    }

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paintBackground( Context c, Box box ) { }

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paintComponent( Context c, Box box ) { }

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paintBorder( Context c, Box box ) { }

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paintChildren( Context c, Box box ) {
        //u.p("Layout.paintChildren(): " + box);
        //u.p("child count = " + box.getChildCount());
        for ( int i = 0; i < box.getChildCount(); i++ ) {
            Box child = (Box)box.getChild( i );
            //u.p("child = " + child);
            Renderer renderer = null;
            if ( child.isAnonymous() ) {
                renderer = LayoutFactory.getAnonymousRenderer();
            } else {
                if(child.node == null) {
                    u.p("null node of child: " + child);
                }
                renderer = LayoutFactory.getRenderer( child.node );
            }
            paintChild( c, child, renderer );
        }
    }

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param box     PARAM
     * @param layout  PARAM
     */
    public void paintChild( Context c, Box box, Renderer layout ) {
        
        if(Configuration.isTrue("xr.renderer.viewport-repaint",false)) {
            if(c.getGraphics().getClip() != null) {
                Rectangle2D oldclip = (Rectangle2D)c.getGraphics().getClip();
                Rectangle2D box_rect = new Rectangle(box.x,box.y,box.width,box.height);
                if(oldclip.intersects(box_rect)) {
                    layout.paint( c, box );
                }
                return;
            }
        }
        
        layout.paint( c, box );
    }
}

