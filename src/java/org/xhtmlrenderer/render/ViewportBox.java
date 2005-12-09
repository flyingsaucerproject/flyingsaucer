package org.xhtmlrenderer.render;

import java.awt.Rectangle;

import org.xhtmlrenderer.css.style.CssContext;

/**
 * A dummy box representing the viewport
 */
public class ViewportBox extends BlockBox {
    private Rectangle viewport;
    
    public ViewportBox(Rectangle viewport) {
        this.viewport = viewport;
    }
    
    public int getWidth() {
        return viewport.width;
    }
    
    public int getHeight() {
        return viewport.height;
    }
    
    public int getContentWidth() {
        return viewport.height;
    }
    
    protected Rectangle getContentAreaEdge(int left, int top, CssContext cssCtx) {
        return new Rectangle(left, top, viewport.width, viewport.height);
    }
}
