package org.xhtmlrenderer.render;

//import java.awt.Image;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.css.*;
import java.awt.Color;
import org.xhtmlrenderer.util.*;

public class BodyRenderer extends BoxRenderer {
    
    private static final Color transparent = new Color(0,0,0,0);
    
    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */

    public void paintBackground( Context c, Box box ) {
        if(!Configuration.isTrue("xr.renderer.draw.backgrounds",true)) {
            return;
        }

        if(box.background_color != null) {
            if(!box.background_color.equals(transparent)) {
                c.getGraphics().setColor(box.background_color);
                c.getGraphics().fillRect( 0, 0, c.canvas.getWidth(), c.canvas.getHeight() );
            }
            super.paintBackground( c, box );
        }

    }

}

