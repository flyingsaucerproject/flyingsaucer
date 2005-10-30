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
import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.PageInfo;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.swing.RootPanel;
import org.xhtmlrenderer.util.XRLog;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.Stack;
import java.util.logging.Level;


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
     * Constructor for the RenderingContext object
     * <p/>
     * needs a new instance every run
     */
    public RenderingContext(SharedContext sharedContext, Rectangle extents) {
        this.sharedContext = sharedContext;
        bfc_stack = new Stack();
        setExtents(extents);
    }

    /**
     * Sets the context attribute of the RenderingContext object
     *
     * @param sharedContext The new context value
     */
    public void setContext(SharedContext sharedContext) {
        this.sharedContext = sharedContext;
    }


    /**
     * Sets the baseURL attribute of the RenderingContext object
     *
     * @param url The new baseURL value
     */
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


    /**
     * <p/>
     * <p/>
     * Set the current media type. This is usually something like <i>screen</i>
     * or <i>print</i> . See the <a href="http://www.w3.org/TR/CSS21/media.html">
     * media section</a> of the CSS 2.1 spec for more information on media
     * types.</p>
     *
     * @param media The new media value
     */
    public void setMedia(String media) {
        sharedContext.setMedia(media);
    }

    /**
     * Gets the uac attribute of the RenderingContext object
     *
     * @return The uac value
     */
    public UserAgentCallback getUac() {
        return sharedContext.getUac();
    }


    /**
     * Gets the context attribute of the RenderingContext object
     *
     * @return The context value
     */
    public SharedContext getContext() {
        return sharedContext;
    }

    /**
     * Gets the baseURL attribute of the RenderingContext object
     *
     * @return The baseURL value
     */
    public String getBaseURL() {
        return sharedContext.getBaseURL();
    }

    /**
     * Gets the dPI attribute of the RenderingContext object
     *
     * @return The dPI value
     */
    public float getDPI() {
        return sharedContext.getDPI();
    }

    /**
     * Gets the dPI attribute in a more useful form of the RenderingContext object
     *
     * @return The dPI value
     */
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


    /**
     * Gets the textRenderer attribute of the RenderingContext object
     *
     * @return The textRenderer value
     */
    public TextRenderer getTextRenderer() {
        return sharedContext.getTextRenderer();
    }

    /**
     * Gets the media attribute of the RenderingContext object
     *
     * @return The media value
     */
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

    /**
     * Description of the Field
     */
    public void setGraphics(Graphics2D graphics) {
        this.graphics = graphics;
    }

    public FontResolver getFontResolver() {
        return sharedContext.getFontResolver();
    }

    private Rectangle extents;

    /**
     * Sets the extents attribute of the Context object
     *
     * @param rect The new extents value
     */
    public void setExtents(Rectangle rect) {
        this.extents = rect;
        if (extents.width < 1) {
            XRLog.exception("width < 1");
            extents.width = 1;
        }
    }

    /**
     * Gets the extents attribute of the Context object
     *
     * @return The extents value
     */
    public Rectangle getExtents() {
        return this.extents;
    }

    //Style-handling stuff
    private Stack styleStack;

    private Stack parentContentStack = new Stack();

    public void initializeStyles(EmptyStyle c) {
        styleStack = new Stack();
        styleStack.push(c);
    }

    public void pushStyle(CascadedStyle s) {
        CalculatedStyle parent = (CalculatedStyle) styleStack.peek();
        CalculatedStyle derived = parent.deriveStyle(s);
        styleStack.push(derived);
    }

    public void popStyle() {
        if (isStylesAllPopped()) {
            XRLog.general(Level.SEVERE, "Trying to pop base empty style");
        } else
            styleStack.pop();
    }

    public CalculatedStyle getCurrentStyle() {
        return (CalculatedStyle) styleStack.peek();
    }

    public boolean isStylesAllPopped() {
        return styleStack.size() == 1;//Is primed with an EmptyStyle to start off with
    }

    /**
     * the current block formatting context
     */
    private BlockFormattingContext bfc;
    protected Stack bfc_stack;

    public BlockFormattingContext getBlockFormattingContext() {
        return bfc;
    }

    public void pushBFC(BlockFormattingContext bfc) {
        bfc_stack.push(this.bfc);
        this.bfc = bfc;
    }

    public void popBFC() {
        bfc = (BlockFormattingContext) bfc_stack.pop();
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

    /**
     * Description of the Field
     */
    private int xoff = 0;

    /**
     * Description of the Field
     */
    private int yoff = 0;

    /**
     * Description of the Method
     *
     * @param x PARAM
     * @param y PARAM
     */
    public void translate(int x, int y) {
        //Uu.p("trans: " + x + "," + y);
        //getGraphics().translate(x, y);//not thread-safe
        if (bfc != null) { //is now thread-safe
            bfc.translate(x, y);
        }
        xoff += x;
        yoff += y;
    }

    private LinkedList decorations = new LinkedList();
    private LinkedList inlineBorders = new LinkedList();
    private LinkedList firstLineStyles = new LinkedList();

    public LinkedList getDecorations() {
        return decorations;
    }

    public LinkedList getInlineBorders() {
        return inlineBorders;
    }

    public void addFirstLineStyle(CascadedStyle firstLineStyle) {
        firstLineStyles.addLast(firstLineStyle);
    }

    public void popFirstLineStyle() {
        if (firstLineStyles.size() != 0) {//there was no formatted first line
            firstLineStyles.removeLast();
        }
    }

    public boolean hasFirstLineStyles() {
        return firstLineStyles.size() != 0;
    }

    /**
     * NB, clone list first if you want to keep the contents!
     */
    public void clearFirstLineStyles() {
        firstLineStyles.clear();
    }

    /**
     * NB, you are getting a reference! Call clearFirstLineStyles at own risk!
     */
    public LinkedList getFirstLineStyles() {
        return firstLineStyles;
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

    public void setInteractive(boolean interactive) {
        sharedContext.setInteractive(interactive);
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

    public Point getOriginOffset() {
        return new Point(xoff, yoff);
    }

    public PageInfo getPageInfo() {
        return sharedContext.getPageInfo();
    }

    public boolean isPrint() {
        return sharedContext.isPrint();
    }

    private int currentPage;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setPrint(boolean b) {
        sharedContext.setPrint(b);
    }
}

