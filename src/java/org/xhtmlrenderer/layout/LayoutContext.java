/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbj�rn Gannholm
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

import java.awt.Rectangle;
import java.util.Stack;

import org.xhtmlrenderer.context.ContentFunctionFactory;
import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.MarkerData;
import org.xhtmlrenderer.render.RenderQueue;
import org.xhtmlrenderer.swing.RootPanel;

/**
 * This class tracks state which changes over the course of a layout run.
 * Generally speaking, if possible, state information should be stored in the box
 * tree and not here.  It also provides pass-though calls to many methods in
 * {@link SharedContext}.
 */
public class LayoutContext implements CssContext {
    private SharedContext sharedContext;
    private RenderQueue renderQueue;
    
    private Layer rootLayer;
    
    private StyleTracker firstLines;
    private StyleTracker firstLetters;
    private MarkerData currentMarkerData;
    
    private Stack bfcs;
    private Stack layers;
    
    private boolean shouldStop = false;
    
    private ReplacedElementFactory replacedElementFactory;
    
    private FontContext fontContext;
    
    private ContentFunctionFactory contentFunctionFactory = new ContentFunctionFactory();

    public TextRenderer getTextRenderer() {
        return sharedContext.getTextRenderer();
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
    
    //the stuff that needs to have a separate instance for each run.
    LayoutContext(SharedContext sharedContext) {
        this.sharedContext = sharedContext;
        this.bfcs = new Stack();
        this.layers = new Stack();
        
        this.firstLines = new StyleTracker();
        this.firstLetters = new StyleTracker();
    }
    
    public void reInit() {
        this.firstLines = new StyleTracker();
        this.firstLetters = new StyleTracker();
        this.currentMarkerData = null;
        
        this.bfcs = new Stack();
    }
    
    public LayoutState captureLayoutState() {
        LayoutState result = new LayoutState();
        
        result.setFirstLines(this.firstLines);
        result.setFirstLetters(this.firstLetters);
        result.setCurrentMarkerData(this.currentMarkerData);
        
        result.setBFCs(this.bfcs);
        
        return result;
    }
    
    public void restoreLayoutState(LayoutState layoutState) {
        this.firstLines = layoutState.getFirstLines();
        this.firstLetters = layoutState.getFirstLetters();
        
        this.currentMarkerData = layoutState.getCurrentMarkerData();
        
        this.bfcs = layoutState.getBFCs();
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
            
            if (master.getStyle().isAlternateFlow()) {
                while (parent.getParent() != null) {
                    parent = parent.getParent();
                }
            }
            
            layer = new Layer(parent, master);

            parent.addChild(layer);
        }
    
        pushLayer(layer);
    }
    
    public void pushLayer(Layer layer) {
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

    public void translate(int x, int y) {
        getBlockFormattingContext().translate(x, y);
    }

    public boolean shouldStop() {
        return shouldStop;
    }

    public void stopRendering() {
        this.shouldStop = true;
    }

    /* code to keep track of all of the id'd boxes */
    public void addBoxId(String id, Box box) {
        this.sharedContext.addBoxId(id, box);
    }
    
    public void removeBoxId(String id) {
        this.sharedContext.removeBoxId(id);
    }
    
    public void addNamedAnchor(String name, Box box) {
        this.sharedContext.addNamedAnchor(name, box);
    }
    
    public void removeNamedAnchor(String name) {
        this.sharedContext.removeNamedAnchor(name);
    }

    public boolean isInteractive() {
        return sharedContext.isInteractive();
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

    public FSFont getFont(FontSpecification font) {
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

    public ReplacedElementFactory getReplacedElementFactory() {
        return replacedElementFactory;
    }

    public void setReplacedElementFactory(
            ReplacedElementFactory replacedElementFactory) {
        this.replacedElementFactory = replacedElementFactory;
    }

    public FontContext getFontContext() {
        return fontContext;
    }

    public void setFontContext(FontContext fontContext) {
        this.fontContext = fontContext;
    }
    
    public ContentFunctionFactory getContentFunctionFactory() {
        return contentFunctionFactory;
    }
    
    public SharedContext getSharedContext() {
        return sharedContext;
    }
}
