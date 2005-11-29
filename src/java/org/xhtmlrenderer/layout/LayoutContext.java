/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjšrn Gannholm
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
 * }}}
 */
package org.xhtmlrenderer.layout;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Stack;
import java.util.logging.Level;

import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.PageContext;
import org.xhtmlrenderer.render.RenderQueue;
import org.xhtmlrenderer.swing.RootPanel;
import org.xhtmlrenderer.util.XRLog;

public class LayoutContext implements CssContext, PageContext {
    private SharedContext sharedContext;
    private boolean shrinkWrap = false;
    private RenderQueue renderQueue;
    private boolean pendingPageBreak;
    private double floatingY;
    private Layer rootLayer;
    
    private Graphics2D graphics;

    private StyleTracker firstLines = new StyleTracker();
    private StyleTracker firstLetters = new StyleTracker();

    public boolean isFirstLine() {
        return firstLine;
    }

    public void setFirstLine(boolean firstLine) {
        this.firstLine = firstLine;
    }

    private boolean firstLine;

    public TextRenderer getTextRenderer() {
        return sharedContext.getTextRenderer();
    }

    public void addMaxWidth(int max_width) {
        sharedContext.addMaxWidth(max_width);
    }

    public int getMaxWidth() {
        return sharedContext.getMaxWidth();
    }

    public void addMaxHeight(int max_height) {
        sharedContext.addMaxHeight(max_height);
    }

    public int getMaxHeight() {
        return sharedContext.getMaxHeight();
    }

    public Graphics2D getGraphics() {
        return graphics;
    }

    public void setGraphics(Graphics2D graphics) {
        this.graphics = graphics;
    }

    public StyleReference getCss() {
        return sharedContext.getCss();
    }

    public RootPanel getCanvas() {
        return sharedContext.getCanvas();
    }

    public Rectangle getFixedRectangle() {
        return sharedContext.getFixedRectangle();
    }

    public NamespaceHandler getNamespaceHandler() {
        return sharedContext.getNamespaceHandler();
    }

    
    public boolean shrinkWrap() {
        return shrinkWrap;
    }

    public void setShrinkWrap() {
        shrinkWrap = true;
    }

    public void unsetShrinkWrap() {
        shrinkWrap = false;
    }

    //the stuff that needs to have a separate instance for each run.
    LayoutContext(SharedContext sharedContext, Rectangle extents) {
        this.sharedContext = sharedContext;
        bfc_stack = new Stack();
        layer_stack = new Stack();
        setExtents(extents);
    }

    //Style-handling stuff
    private Stack styleStack;

    private Stack parentContentStack = new Stack();

    public void initializeStyles(CalculatedStyle c) {
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
        return styleStack.size() == 1;//Is primed with an EmptyStyle to setStartStyle off with
    }

    /**
     * The current block formatting context
     */
    private BlockFormattingContext bfc;
    private Stack bfc_stack;
    
    private Layer layer;
    private Stack layer_stack;    

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
    
    public void pushLayer(Box master) {
        Layer layer = null;
        
        if (rootLayer == null) {
            layer = new Layer(master);
            rootLayer = layer;
        } else {
            Layer parent = this.layer;
            
            if (master.getStyle().isFixed()) {
                while (parent.getParent() != null) {
                    parent = parent.getParent();
                }
            }
            
            layer = new Layer(parent, master);

            parent.addChild(layer);
        }
        
        layer_stack.push(this.layer);
        this.layer = layer;
    }
    
    public void popLayer() {
        layer.positionChildren(this);
        layer = (Layer)layer_stack.pop();
    }
    
    public Layer getLayer() {
        return layer;
    }
    
    public Layer getRootLayer() {
        return rootLayer;
    }

    private Stack extents_stack = new Stack();
    private Rectangle extents;

    public void setExtents(Rectangle rect) {
        this.extents = rect;
        if (extents.width < 1) {
            XRLog.exception("width < 1");
            extents.width = 1;
        }
    }

    public Rectangle getExtents() {
        return this.extents;
    }

    /**
     * @param dw amount to shrink width by
     * @param dh amount to shrink height by
     */
    public void shrinkExtents(int dw, int dh) {

        extents_stack.push(getExtents());

        Rectangle rect = new Rectangle(0, 0,
                getExtents().width - dw,
                getExtents().height - dh);

        setExtents(rect);
    }

    public void unshrinkExtents() {
        setExtents((Rectangle) extents_stack.pop());
    }

    private int xoff = 0;
    private int yoff = 0;

    /* =========== List stuff ============== */

    protected int list_counter;

    public int getListCounter() {
        return list_counter;
    }

    public void setListCounter(int counter) {
        list_counter = counter;
    }

    /* ================== Extra Utility Funtions ============== */

    /*
     * notes to help manage inline sub blocks (like table cells)
     */
    public void setSubBlock(boolean sub_block) {
        this.sub_block = sub_block;
    }

    protected boolean sub_block = false;

    public boolean isSubBlock() {
        return sub_block;
    }

    public void translate(int x, int y) {
        bfc.translate(x, y);
        xoff += x;
        yoff += y;
    }

    public Point getOriginOffset() {
        return new Point(xoff, yoff);
    }

    private boolean shouldStop = false;

    public boolean shouldStop() {
        return shouldStop;
    }

    public void stopRendering() {
        this.shouldStop = true;
    }

    public String toString() {
        return "Context: extents = " +
                "(" + extents.x + "," + extents.y + ") -> (" + extents.width + "x" + extents.height + ")"
                + " offset = " + xoff + "," + yoff
                ;
    }


    /* code to keep track of all of the id'd boxes */
    public void addIDBox(String id, Box box) {
        this.sharedContext.addIDBox(id, box);
    }

    public boolean isInteractive() {
        return sharedContext.isInteractive();
    }

    public void setInteractive(boolean interactive) {
        sharedContext.setInteractive(interactive);
    }

    public Content getParentContent() {
        return (Content) (parentContentStack.size() == 0 ? null : parentContentStack.peek());
    }

    public void pushParentContent(Content content) {
        parentContentStack.push(content);
    }

    public void popParentContent() {
        parentContentStack.pop();
    }

    public RenderQueue getRenderQueue() {
        return renderQueue;
    }

    public void setRenderQueue(RenderQueue renderQueue) {
        this.renderQueue = renderQueue;
    }

    public boolean isRenderQueueAvailable() {
        return this.renderQueue != null;
    }

    public boolean isPendingPageBreak() {
        return pendingPageBreak;
    }

    int renderIndex = 0;

    public int getNewRenderIndex() {
        return renderIndex++;
    }

    public void setPendingPageBreak(boolean pendingPageBreak) {
        this.pendingPageBreak = pendingPageBreak;
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

    public Font getFont(FontSpecification font) {
        return sharedContext.getFont(font);
    }

    public UserAgentCallback getUac() {
        return sharedContext.getUac();
    }

    public PageInfo getPageInfo() {
        return sharedContext.getPageInfo();
    }

    public void setPrint(boolean b) {
        sharedContext.setPrint(b);
    }

    public boolean isPrint() {
        return sharedContext.isPrint();
    }

    /**
     * A holder to provide the y-coordinate relative to the containing block
     * for floats
     */
    public double getFloatingY() {
        return floatingY;
    }

    /**
     * @param floatingY
     * @see {@link #getFloatingY()}
     */
    public void setFloatingY(double floatingY) {
        this.floatingY = floatingY;
    }

    public StyleTracker getFirstLinesTracker() {
        return firstLines;
    }
    
    public StyleTracker getFirstLettersTracker() {
        return firstLetters;
    }
}
