package org.xhtmlrenderer.render;


import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.util.u;

public class CustomBlockRenderer extends BoxRenderer {
    /**
     * override this to paint your component
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paintComponent( Context c, Box box ) {
        u.p( "Custom components must override paintComponent" );
    }
    
    
}

