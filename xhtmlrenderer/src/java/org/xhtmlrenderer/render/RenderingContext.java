/*
 * RenderingContext.java
 * Copyright (c) 2004, 2005 Josh Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.render;

import java.awt.Rectangle;

import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.*;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.swing.RootPanel;

/**
 * Supplies information about the context in which rendering will take place
 *
 * @author jmarinacci
 *         November 16, 2004
 */
public class RenderingContext implements CssContext {
    protected SharedContext sharedContext;
    private OutputDevice outputDevice;
    private FontContext fontContext;
    
    private int pageCount;
    
    private int pageNo;
    private PageBox page;
    
    private Layer rootLayer;
    
    private int initialPageNo;
    
    /**
     * <p/>
     * needs a new instance every run
     */
    public RenderingContext(SharedContext sharedContext) {
        this.sharedContext = sharedContext;
    }

    public void setContext(SharedContext sharedContext) {
        this.sharedContext = sharedContext;
    }

    public void setBaseURL(String url) {
        sharedContext.setBaseURL(url);
    }

    public UserAgentCallback getUac() {
        return sharedContext.getUac();
    }

    public String getBaseURL() {
        return sharedContext.getBaseURL();
    }

    public float getDPI() {
        return sharedContext.getDPI();
    }

    public float getMmPerDot() {
        return sharedContext.getMmPerPx();
    }
    
    public int getDotsPerPixel() {
        return sharedContext.getDotsPerPixel();
    }    
    
    public float getFontSize2D(FontSpecification font) {
        return sharedContext.getFont(font).getSize2D();
    }

    public float getXHeight(FontSpecification parentFont) {
        return sharedContext.getXHeight(getFontContext(), parentFont);
    }

    public TextRenderer getTextRenderer() {
        return sharedContext.getTextRenderer();
    }

    /**
     * Returns true if the currently set media type is paged. Currently returns
     * true only for <i>print</i> , <i>projection</i> , and <i>embossed</i> ,
     * <i>handheld</i> , and <i>tv</i> . See the <a
     * href="http://www.w3.org/TR/CSS21/media.html">media section</a> of the CSS
     * 2.1 spec for more information on media types.
     *
     * @return The paged value
     */
    public boolean isPaged() {
        return sharedContext.isPaged();
    }

    public FontResolver getFontResolver() {
        return sharedContext.getFontResolver();
    }
    
    public FSFont getFont(FontSpecification font) {
        return sharedContext.getFont(font);
    }

    public FSCanvas getCanvas() {
        return sharedContext.getCanvas();
    }

    public Rectangle getFixedRectangle() {
        Rectangle result;
        if (! isPrint()) {
            result = sharedContext.getFixedRectangle();
        } else {
            result = new Rectangle(0, -this.page.getTop(), 
                    this.page.getContentWidth(this),
                    this.page.getContentHeight(this)-1);
        }
        result.translate(-1, -1);
        return result;
    }
    
    public Rectangle getViewportRectangle() {
        Rectangle result = new Rectangle(getFixedRectangle());
        result.y *= -1;
        
        return result;
    }
    
    public boolean debugDrawBoxes() {
        return sharedContext.debugDrawBoxes();
    }

    public boolean debugDrawLineBoxes() {
        return sharedContext.debugDrawLineBoxes();
    }

    public boolean debugDrawInlineBoxes() {
        return sharedContext.debugDrawInlineBoxes();
    }

    public boolean debugDrawFontMetrics() {
        return sharedContext.debugDrawFontMetrics();
    }

    public boolean isInteractive() {
        return sharedContext.isInteractive();
    }

    public boolean isPrint() {
        return sharedContext.isPrint();
    }

    public OutputDevice getOutputDevice() {
        return outputDevice;
    }

    public void setOutputDevice(OutputDevice outputDevice) {
        this.outputDevice = outputDevice;
    }

    public FontContext getFontContext() {
        return fontContext;
    }

    public void setFontContext(FontContext fontContext) {
        this.fontContext = fontContext;
    }

    public void setPage(int pageNo, PageBox page) {
        this.pageNo = pageNo;
        this.page = page;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public PageBox getPage() {
        return page;
    }

    public int getPageNo() {
        return pageNo;
    }
    
    public StyleReference getCss() {
        return sharedContext.getCss();
    }
    
    public FSFontMetrics getFSFontMetrics(FSFont font) {
        return getTextRenderer().getFSFontMetrics(getFontContext(), font, "");
    }
    
    public Layer getRootLayer() {
        return rootLayer;
    }

    public void setRootLayer(Layer rootLayer) {
        this.rootLayer = rootLayer;
    }

    public int getInitialPageNo() {
        return initialPageNo;
    }

    public void setInitialPageNo(int initialPageNo) {
        this.initialPageNo = initialPageNo;
    }    

    public Box getBoxById(String id) {
        return sharedContext.getBoxById(id);
    }
}

