package org.xhtmlrenderer.render;


import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.U;

public class CustomBlockRenderer extends BoxRenderer {
    /**
     * override this to paint your component
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintComponent(Context c, Box box) {
        U.p("Custom components must override paintComponent");
    }


}

