package org.xhtmlrenderer.render;


import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.util.Uu;

public class CustomBlockRenderer extends BoxRenderer {
    /**
     * override this to paint your component
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintComponent(SharedContext c, Box box) {
        Uu.p("Custom components must override paintComponent");
    }


}

