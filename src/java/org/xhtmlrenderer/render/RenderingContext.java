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

import org.xhtmlrenderer.context.FontResolver;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.swing.RootPanel;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Supplies information about the context in which rendering will take place
 *
 * @author jmarinacci
 *         November 16, 2004
 */
public class RenderingContext implements CssContext {

    /**
     * Description of the Field
     */
    protected SharedContext sharedContext;

    private Graphics2D graphics;

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

    /**
     * Sets the effective DPI (Dots Per Inch) of the screen. You should normally
     * never need to override the dpi, as it is already set to the system
     * default by <code>Toolkit.getDefaultToolkit().getScreenResolution()</code>
     * . You can override the value if you want to scale the fonts for
     * accessibility or printing purposes. Currently the DPI setting only
     * affects font sizing.
     *
     * @param dpi The new dPI value
     */
    public void setDPI(float dpi) {
        sharedContext.setDPI(dpi);
    }

    public UserAgentCallback getUac() {
        return sharedContext.getUac();
    }

    public SharedContext getContext() {
        return sharedContext;
    }

    public String getBaseURL() {
        return sharedContext.getBaseURL();
    }

    public float getDPI() {
        return sharedContext.getDPI();
    }

    public float getMmPerPx() {
        return sharedContext.getMmPerPx();
    }

    public float getFontSize2D(FontSpecification font) {
        return sharedContext.getFontSize2D(font);
    }

    public float getXHeight(FontSpecification parentFont) {
        return sharedContext.getXHeight(parentFont, graphics);
    }

    public float getFontSizeForXHeight(FontSpecification parent, FontSpecification desired, float xHeight) {
        return sharedContext.getFontSizeForXHeight(parent, desired, xHeight, graphics);
    }

    public TextRenderer getTextRenderer() {
        return sharedContext.getTextRenderer();
    }

    public String getMedia() {
        return sharedContext.getMedia();
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

    public Graphics2D getGraphics() {
        return graphics;
    }

    public void setGraphics(Graphics2D graphics) {
        this.graphics = graphics;
    }

    public FontResolver getFontResolver() {
        return sharedContext.getFontResolver();
    }

    public Font getFont(FontSpecification font) {
        return sharedContext.getFont(font);
    }

    public RootPanel getCanvas() {
        return sharedContext.getCanvas();
    }

    public Rectangle getFixedRectangle() {
        return sharedContext.getFixedRectangle();
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

    public boolean inSelection(Box box) {
        return sharedContext.inSelection(box);
    }

    public Box getSelectionStart() {
        return sharedContext.getSelectionStart();
    }

    public Box getSelectionEnd() {
        return sharedContext.getSelectionEnd();
    }

    public int getSelectionStartX() {
        return sharedContext.getSelectionStartX();
    }

    public int getSelectionEndX() {
        return sharedContext.getSelectionEndX();
    }

    public void updateSelection(Box box) {
        sharedContext.updateSelection(box);
    }

    public boolean isPrint() {
        return sharedContext.isPrint();
    }
}

