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
import java.awt.Rectangle;
import java.util.Stack;
import java.util.logging.Level;

import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.MarkerData;
import org.xhtmlrenderer.render.RenderQueue;
import org.xhtmlrenderer.swing.RootPanel;
import org.xhtmlrenderer.util.XRLog;

public class LayoutContext implements CssContext {
    private SharedContext sharedContext;
    private RenderQueue renderQueue;
    
    private boolean shrinkWrap = false;
    private Layer rootLayer;
    
    private Graphics2D graphics;

    private StyleTracker firstLines;
    private StyleTracker firstLetters;
    private MarkerData currentMarkerData;
    
    //Style-handling stuff
    private Stack styleStack;

    private Content parentContent;
    
    private Stack bfcs;
    private Stack layers;
    
    private Rectangle extents;   
    
    int renderIndex = 0;
    
    private boolean shouldStop = false;

    public TextRenderer getTextRenderer() {
        return sharedContext.getTextRenderer();
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
        this.bfcs = new Stack();
        this.layers = new Stack();
        this.styleStack = new Stack();
        this.styleStack.push(new EmptyStyle());
        setExtents(extents);
        
        this.firstLines = new StyleTracker();
        this.firstLetters = new StyleTracker();
    }
    
    public void reInit(CalculatedStyle currentStyle) {
        this.firstLines = new StyleTracker();
        this.firstLetters = new StyleTracker();
        this.currentMarkerData = null;
        
        this.styleStack = new Stack();
        this.styleStack.push(new EmptyStyle());
        this.styleStack.push(currentStyle);
        
        this.parentContent = null;
        this.bfcs = new Stack();
        
        this.extents = null;
    }
    
    public LayoutState captureLayoutState() {
        LayoutState result = new LayoutState();
        
        result.setFirstLines(this.firstLines);
        result.setFirstLetters(this.firstLetters);
        result.setCurrentMarkerData(this.currentMarkerData);
        
        result.setStyleStack(this.styleStack);
        result.setParentContent(this.parentContent);
        
        result.setBFCs(this.bfcs);
        
        result.setExtents(this.extents);
        
        return result;
    }
    
    public void restoreLayoutState(LayoutState layoutState) {
        this.firstLines = layoutState.getFirstLines();
        this.firstLetters = layoutState.getFirstLetters();
        
        this.currentMarkerData = layoutState.getCurrentMarkerData();
        
        this.styleStack = layoutState.getStyleStack();
        this.parentContent = layoutState.getParentContent();
        
        this.bfcs = layoutState.getBFCs();
        
        this.extents = layoutState.getExtents();
    }
    
    public LayoutState copyStateForRelayout() {
        LayoutState result = new LayoutState();
        
        result.setFirstLetters(this.firstLetters.copyOf());
        result.setFirstLines(this.firstLines.copyOf());
        result.setCurrentMarkerData(this.currentMarkerData);
        
        return result;
    }
    
    public void restoreStateForRelayout(LayoutState layoutState) {
        this.firstLines = layoutState.getFirstLines();
        this.firstLetters = layoutState.getFirstLetters();
        
        this.currentMarkerData = layoutState.getCurrentMarkerData();
    }

    public void pushStyle(CascadedStyle s) {
        CalculatedStyle parent = (CalculatedStyle) styleStack.peek();
        CalculatedStyle derived = parent.deriveStyle(s);
        styleStack.push(derived);
    }

    public void popStyle() {
        if (isStylesAllPopped()) {
            XRLog.general(Level.SEVERE, "Trying to pop base empty style");
        } else {
            styleStack.pop();
        }
    }

    public CalculatedStyle getCurrentStyle() {
        return (CalculatedStyle) styleStack.peek();
    }

    public boolean isStylesAllPopped() {
        return styleStack.size() == 1;//Is primed with an EmptyStyle to setStartStyle off with
    }   

    public BlockFormattingContext getBlockFormattingContext() {
        return (BlockFormattingContext)bfcs.peek();
    }

    public void pushBFC(BlockFormattingContext bfc) {
        bfcs.push(bfc);
    }

    public void popBFC() {
        bfcs.pop();
    }
    
    public void pushLayer(Box master) {
        Layer layer = null;
        
        if (rootLayer == null) {
            layer = new Layer(master);
            rootLayer = layer;
        } else {
            Layer parent = getLayer();
            
            if (master.getStyle().isFixed() || master.getStyle().isAlternateFlow()) {
                while (parent.getParent() != null) {
                    parent = parent.getParent();
                }
            }
            
            layer = new Layer(parent, master);

            parent.addChild(layer);
        }
        
        layers.push(layer);
    }
    
    public void popLayer() {
        Layer layer = getLayer();

        layer.finish(this);
        
        layers.pop();
    }
    
    public Layer getLayer() {
        return (Layer)layers.peek();
    }
    
    public Layer getRootLayer() {
        return rootLayer;
    }

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
     * @return the extents before shrinking
     */
    public Rectangle shrinkExtents(int dw, int dh) {
        Rectangle result = getExtents();

        Rectangle rect = new Rectangle(0, 0,
                getExtents().width - dw,
                getExtents().height - dh);

        setExtents(rect);
        
        return result;
    }

    public void translate(int x, int y) {
        getBlockFormattingContext().translate(x, y);
    }

    public boolean shouldStop() {
        return shouldStop;
    }

    public void stopRendering() {
        this.shouldStop = true;
    }

    public String toString() {
        return "Context: extents = " +
                "(" + extents.x + "," + extents.y + ") -> (" + extents.width + "x" + extents.height + ")"
                ;
    }

    /* code to keep track of all of the id'd boxes */
    public void addIDBox(String id, Box box) {
        this.sharedContext.addIDBox(id, box);
    }

    public boolean isInteractive() {
        return sharedContext.isInteractive();
    }

    public Content getParentContent() {
        return this.parentContent;
    }
    
    public void setParentContent(Content parent) {
        this.parentContent = parent;
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

    public int getNewRenderIndex() {
        return renderIndex++;
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

    public boolean isPrint() {
        return sharedContext.isPrint();
    }

    public StyleTracker getFirstLinesTracker() {
        return firstLines;
    }
    
    public StyleTracker getFirstLettersTracker() {
        return firstLetters;
    }

    public MarkerData getCurrentMarkerData() {
        return currentMarkerData;
    }

    public void setCurrentMarkerData(MarkerData currentMarkerData) {
        this.currentMarkerData = currentMarkerData;
    }
}
