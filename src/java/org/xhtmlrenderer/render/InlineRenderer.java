package org.xhtmlrenderer.render;

import org.xhtmlrenderer.layout.*;

public class InlineRenderer extends BoxRenderer {

    /**
    * Description of the Method
    *
    * @param c    PARAM
    * @param box  PARAM
    */
    public void paintComponent( Context c, Box box ) {
        if ( box.isAnonymous() ) {
            InlinePainter.paintInlineContext( c, box );
            return;
        }
        if ( BoxLayout.isBlockLayout( box.getElement(), c ) ) {
            super.paintComponent( c, box );
            return;
        }
        InlinePainter.paintInlineContext( c, box );
    }

    /**
    * Description of the Method
    *
    * @param c    PARAM
    * @param box  PARAM
    */
    public void paintChildren( Context c, Box box ) {
        if ( box.isAnonymous() ) {
            return;
        }
        if ( BoxLayout.isBlockLayout( box.getElement(), c ) ) {
            super.paintChildren( c, box );
        }
    }

}
