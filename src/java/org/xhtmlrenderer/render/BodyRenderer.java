package org.xhtmlrenderer.render;

import java.awt.Image;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.css.*;

public class BodyRenderer extends BodyLayout {
    
    
    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */

    public void paintBackground( Context c, Box box ) {

        c.getGraphics().fillRect( 0, 0, c.canvas.getWidth(), c.canvas.getHeight() );

        super.paintBackground( c, box );

    }

}

