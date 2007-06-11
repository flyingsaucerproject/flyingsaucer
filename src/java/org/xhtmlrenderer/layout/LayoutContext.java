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
import java.util.LinkedList;

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
import org.xhtmlrenderer.swing.RootPanel;

/**
 * This class tracks state which changes over the course of a layout run.
 * Generally speaking, if possible, state information should be stored in the box
 * tree and not here.  It also provides pass-though calls to many methods in
 * {@link SharedContext}.
 */
public class LayoutContext implements CssContext {
    private SharedContext _sharedContext;
    
    private Layer _rootLayer;
    
    private StyleTracker _firstLines;
    private StyleTracker _firstLetters;
    private MarkerData _currentMarkerData;
    
    private LinkedList _bfcs;
    private LinkedList _layers;
    
    private FontContext _fontContext;
    
    private ContentFunctionFactory _contentFunctionFactory = new ContentFunctionFactory();
    
    private int _extraSpaceTop;
    private int _extraSpaceBottom;
    
    public TextRenderer getTextRenderer() {
        return _sharedContext.getTextRenderer();
    }

    public StyleReference getCss() {
        return _sharedContext.getCss();
    }

    public RootPanel getCanvas() {
        return _sharedContext.getCanvas();
    }

    public Rectangle getFixedRectangle() {
        return _sharedContext.getFixedRectangle();
    }

    public NamespaceHandler getNamespaceHandler() {
        return _sharedContext.getNamespaceHandler();
    }
    
    //the stuff that needs to have a separate instance for each run.
    LayoutContext(SharedContext sharedContext) {
        _sharedContext = sharedContext;
        _bfcs = new LinkedList();
        _layers = new LinkedList();
        
        _firstLines = new StyleTracker();
        _firstLetters = new StyleTracker();
    }
    
    public void reInit() {
        _firstLines = new StyleTracker();
        _firstLetters = new StyleTracker();
        _currentMarkerData = null;
        
        _bfcs = new LinkedList();
        
        _extraSpaceTop = 0;
        _extraSpaceBottom = 0;
    }
    
    public LayoutState captureLayoutState() {
        LayoutState result = new LayoutState();
        
        result.setFirstLines(_firstLines);
        result.setFirstLetters(_firstLetters);
        result.setCurrentMarkerData(_currentMarkerData);
        
        result.setBFCs(_bfcs);
        
        return result;
    }
    
    public void restoreLayoutState(LayoutState layoutState) {
        _firstLines = layoutState.getFirstLines();
        _firstLetters = layoutState.getFirstLetters();
        
        _currentMarkerData = layoutState.getCurrentMarkerData();
        
        _bfcs = layoutState.getBFCs();
    }
    
    public LayoutState copyStateForRelayout() {
        LayoutState result = new LayoutState();
        
        result.setFirstLetters(_firstLetters.copyOf());
        result.setFirstLines(_firstLines.copyOf());
        result.setCurrentMarkerData(_currentMarkerData);
        
        return result;
    }
    
    public void restoreStateForRelayout(LayoutState layoutState) {
        _firstLines = layoutState.getFirstLines();
        _firstLetters = layoutState.getFirstLetters();
        
        _currentMarkerData = layoutState.getCurrentMarkerData();
    }

    public BlockFormattingContext getBlockFormattingContext() {
        return (BlockFormattingContext)_bfcs.getLast();
    }

    public void pushBFC(BlockFormattingContext bfc) {
        _bfcs.add(bfc);
    }

    public void popBFC() {
        _bfcs.removeLast();
    }
    
    public void pushLayer(Box master) {
        Layer layer = null;
        
        if (_rootLayer == null) {
            layer = new Layer(master);
            _rootLayer = layer;
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
        _layers.add(layer);
    }
    
    public void popLayer() {
        Layer layer = getLayer();

        layer.finish(this);
        
        _layers.removeLast();
    }
    
    public Layer getLayer() {
        return (Layer)_layers.getLast();
    }
    
    public Layer getRootLayer() {
        return _rootLayer;
    }

    public void translate(int x, int y) {
        getBlockFormattingContext().translate(x, y);
    }

    /* code to keep track of all of the id'd boxes */
    public void addBoxId(String id, Box box) {
        _sharedContext.addBoxId(id, box);
    }
    
    public void removeBoxId(String id) {
        _sharedContext.removeBoxId(id);
    }
    
    public boolean isInteractive() {
        return _sharedContext.isInteractive();
    }

    public float getMmPerDot() {
        return _sharedContext.getMmPerPx();
    }
    
    public int getDotsPerPixel() {
        return _sharedContext.getDotsPerPixel();
    }

    public float getFontSize2D(FontSpecification font) {
        return _sharedContext.getFont(font).getSize2D();
    }

    public float getXHeight(FontSpecification parentFont) {
        return _sharedContext.getXHeight(getFontContext(), parentFont);
    }

    public FSFont getFont(FontSpecification font) {
        return _sharedContext.getFont(font);
    }

    public UserAgentCallback getUac() {
        return _sharedContext.getUac();
    }

    public boolean isPrint() {
        return _sharedContext.isPrint();
    }

    public StyleTracker getFirstLinesTracker() {
        return _firstLines;
    }
    
    public StyleTracker getFirstLettersTracker() {
        return _firstLetters;
    }

    public MarkerData getCurrentMarkerData() {
        return _currentMarkerData;
    }

    public void setCurrentMarkerData(MarkerData currentMarkerData) {
        _currentMarkerData = currentMarkerData;
    }

    public ReplacedElementFactory getReplacedElementFactory() {
        return _sharedContext.getReplacedElementFactory();
    }

    public FontContext getFontContext() {
        return _fontContext;
    }

    public void setFontContext(FontContext fontContext) {
        _fontContext = fontContext;
    }
    
    public ContentFunctionFactory getContentFunctionFactory() {
        return _contentFunctionFactory;
    }
    
    public SharedContext getSharedContext() {
        return _sharedContext;
    }

    public int getExtraSpaceBottom() {
        return _extraSpaceBottom;
    }

    public void setExtraSpaceBottom(int extraSpaceBottom) {
        _extraSpaceBottom = extraSpaceBottom;
    }

    public int getExtraSpaceTop() {
        return _extraSpaceTop;
    }

    public void setExtraSpaceTop(int extraSpaceTop) {
        _extraSpaceTop = extraSpaceTop;
    }
}
