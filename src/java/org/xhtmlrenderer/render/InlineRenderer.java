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
        //u.p("InlineLayout.paintComponent() " + box);
        //u.dump_stack();
        if ( box.isAnonymous() ) {
            //u.p("InlineLayout.paintComponent() : " + box);
            InlinePainter.paintInlineContext( c, box );
            return;
        }
        if ( BoxLayout.isBlockLayout( box.getElement(), c ) ) {
            //u.p("InlineLayout.paintComponent is block context: " + box);
            super.paintComponent( c, box );
            return;
        }
        //u.p("InlineLayout.paintComponent()" + box);
        InlinePainter.paintInlineContext( c, box );
    }
    /**
    * Description of the Method
    *
    * @param c    PARAM
    * @param box  PARAM
    */
    public void paintChildren( Context c, Box box ) {
        //u.p("InlineLayout.paintChildren() " + box);
        if ( box.isAnonymous() ) {
            //u.p("it's anonymous so no children");
            return;
        }
        if ( BoxLayout.isBlockLayout( box.getElement(), c ) ) {
            //u.p("is block. doing super");
            super.paintChildren( c, box );
        }
    }

}
