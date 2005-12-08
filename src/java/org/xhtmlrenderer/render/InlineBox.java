/*
 * {{{ header & license
 * Copyright (c) 2005 Joshua Marinacci, Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.BoxCollector;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;

public class InlineBox extends Box {
    private int baseline;
    
    private boolean startsHere;
    private boolean endsHere;
    
    private List inlineChildren;
    
    private boolean pending;
    
    private int inlineWidth;
    
    private TextDecoration textDecoration;
    
    public InlineBox(Element elem, CalculatedStyle style, int cbWidth) {
        this();
        this.element = elem;
        setStyle(new Style(style, cbWidth));
        getStyle().setMarginTopOverride(0.0f);
        getStyle().setMarginBottomOverride(0.0f);
        markPending();
    }
    
    private InlineBox() {
        setState(Box.DONE);
    }
    
    public InlineBox copyOf() {
        InlineBox result = new InlineBox();
        result.element = this.element;
        
        // XXX not right when first-line transition is happening
        result.setStyle(getStyle());
        result.setHeight(getHeight());
        
        result.pending = this.pending;
        
        result.setContainingInlineLayer(getContainingInlineLayer());
        
        return result;
    }
    
    public void calculateHeight(LayoutContext c) {
        BorderPropertySet border = getStyle().getCalculatedStyle().getBorder(c);
        RectPropertySet padding = getStyle().getPaddingWidth(c);
        
        LineMetrics metrics = getStyle().getLineMetrics(c);
        
        setHeight((int)(border.top() + padding.top() + metrics.getAscent() + metrics.getDescent() +
                padding.bottom() + border.bottom()));
    }

    public int getBaseline() {
        return baseline;
    }

    public void setBaseline(int baseline) {
        this.baseline = baseline;
    }

    public int getInlineChildCount() {
        return inlineChildren == null ? 0 : inlineChildren.size();
    }
    
    public void addInlineChild(LayoutContext c, Object child) {
        addInlineChild(c, child, true);
    }
    
    public void addInlineChild(LayoutContext c, Object child, boolean callUnmarkPending) {
        if (inlineChildren == null) {
            inlineChildren = new ArrayList();
        }
        
        inlineChildren.add(child);
        
        if (callUnmarkPending && isPending()) {
            unmarkPending(c);
        }
        
        if (child instanceof Box) {
            Box b = (Box)child;
            b.setParent(this);
            if (c.getLayer().isInline()) {
                b.setContainingInlineLayer(c.getLayer());
            }
        } else if (child instanceof InlineText) {
            ((InlineText)child).setParent(this);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public List getInlineChildren() {
        return inlineChildren == null ? Collections.EMPTY_LIST : inlineChildren;
    }
    
    public Object getInlineChild(int i) {
        if (inlineChildren == null) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            return inlineChildren.get(i);
        }
    }
    
    public int getInlineWidth(CssContext cssCtx) {
        return inlineWidth;
    }
    
    public void prunePending() {
        if (getInlineChildCount() > 0) {
            for (int i = getInlineChildCount() - 1; i >= 0; i--) {
                Object child = (Object)getInlineChild(i);
                if (! (child instanceof InlineBox)) {
                    break;
                }
                
                InlineBox iB = (InlineBox)child;
                iB.prunePending();
                
                if (iB.isPending()) {
                    removeChild(i);
                } else {
                    break;
                }
            }
        }
    }

    public boolean isEndsHere() {
        return endsHere;
    }

    public void setEndsHere(boolean endsHere) {
        this.endsHere = endsHere;
    }

    public boolean isStartsHere() {
        return startsHere;
    }

    public void setStartsHere(boolean startsHere) {
        this.startsHere = startsHere;
    }

    public boolean isPending() {
        return pending;
    }
    
    public void markPending() {
        this.pending = true;
    }
    
    private void unmarkPending(LayoutContext c) {
        this.pending = false;
        
        if (getParent() instanceof InlineBox) {
            ((InlineBox)getParent()).unmarkPending(c);
        }
        
        setStartsHere(true);
        
        if (getStyle().requiresLayer()) {
            c.pushLayer(this);
            getLayer().setInline(true);
            setContainingInlineLayer(getLayer());
            connectChildrenToCurrentLayer(c);
        }
    }
    
    public void connectChildrenToCurrentLayer(LayoutContext c) {
        for (int i = 0; i < inlineChildren.size(); i++) {
            Object obj = inlineChildren.get(i);
            if (obj instanceof Box) {
                Box box = (Box)obj;
                box.setContainingInlineLayer(c.getLayer());
                box.connectChildrenToCurrentLayer(c);
            }
        }
    }

    private void paintTextDecoration(RenderingContext c) {
        Graphics graphics = c.getGraphics();
        
        Color oldColor = graphics.getColor();
        
        graphics.setColor(getStyle().getCalculatedStyle().getColor());
        Rectangle edge = getContentAreaEdge(getAbsX(), getAbsY(), c);
        c.getGraphics().fillRect(
                edge.x, getAbsY() + textDecoration.getOffset(),
                edge.width, textDecoration.getThickness());
        
        graphics.setColor(oldColor);
    }
    
    public void paint(RenderingContext c) {
        paintBackground(c);
        paintBorder(c);
        
        if (textDecoration != null) {
            IdentValue val = 
                getStyle().getCalculatedStyle().getIdent(CSSName.TEXT_DECORATION);
            if (val == IdentValue.UNDERLINE || val == IdentValue.OVERLINE) {
                paintTextDecoration(c);
            }
        }
        
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = getInlineChild(i);
            if (child instanceof InlineText) {
                ((InlineText)child).paint(c);
            }
        }
        
        if (textDecoration != null) {
            IdentValue val = 
                getStyle().getCalculatedStyle().getIdent(CSSName.TEXT_DECORATION);
            if (val == IdentValue.LINE_THROUGH) {
                paintTextDecoration(c);
            }
        }
        
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = getInlineChild(i);
            
            if (child instanceof InlineBox) {
                ((InlineBox)child).paint(c);
            } else if (child instanceof Box) {
                Box b = (Box)child;
                if (b.getLayer() == null) {
                    Layer.paintAsLayer(c, b);
                }
            }
        }
    }
    
    protected int getBorderSides() {
        int result = BorderPainter.TOP + BorderPainter.BOTTOM;
        
        if (startsHere) {
            result += BorderPainter.LEFT;
        }
        if (endsHere) {
            result += BorderPainter.RIGHT;
        }
        
        return result;
    }
    
    protected Rectangle getPaintingBorderEdge(CssContext cssCtx) {
        // x, y pins the content area of the box so subtract off top border and padding
        // too
        
        float marginLeft = 0;
        float marginRight = 0;
        if (startsHere || endsHere) {
            RectPropertySet margin = (RectPropertySet)getStyle().getMarginWidth(cssCtx);
            if (startsHere) {
                marginLeft = margin.left();
            } 
            if (endsHere) {
                marginRight = margin.right();
            }
        }
        BorderPropertySet border = getStyle().getCalculatedStyle().getBorder(cssCtx);
        RectPropertySet padding = getStyle().getPaddingWidth(cssCtx);
        
        Rectangle result = new Rectangle(
                (int)(getAbsX() + marginLeft), 
                (int)(getAbsY() - border.top() - padding.top()), 
                (int)(getInlineWidth(cssCtx) - marginLeft - marginRight), 
                getHeight());
        return result;
    }
    
    protected Rectangle getContentAreaEdge(int left, int top, CssContext cssCtx) {
        BorderPropertySet border = getStyle().getCalculatedStyle().getBorder(cssCtx);
        RectPropertySet padding = getStyle().getPaddingWidth(cssCtx);
        
        float marginLeft = 0;
        float marginRight = 0;
        
        float borderLeft = 0;
        float borderRight = 0;
        
        float paddingLeft = 0;
        float paddingRight = 0;
        
        if (startsHere || endsHere) {
            RectPropertySet margin = (RectPropertySet)getStyle().getMarginWidth(cssCtx);
            if (startsHere) {
                marginLeft = margin.left();
                borderLeft = border.left();
                paddingLeft = padding.left();
            } 
            if (endsHere) {
                marginRight = margin.right();
                borderRight = border.right();
                paddingRight = padding.right();
            }
        }
        
        Rectangle result = new Rectangle(
                (int)(left + marginLeft + borderLeft + paddingLeft), 
                (int)(top - border.top() - padding.top()), 
                (int)(getInlineWidth(cssCtx) - marginLeft - borderLeft - paddingLeft
                    - paddingRight - borderRight - marginRight),
                getHeight());
        return result;
    }
    
    public int getLeftMarginBorderPadding(CssContext cssCtx) {
        if (startsHere) {
            return getStyle().getLeftMarginBorderPadding(cssCtx);
        } else {
            return 0;
        }
    }
    
    public int getRightMarginPaddingBorder(CssContext cssCtx) {
        if (endsHere) {
            return getStyle().getRightMarginBorderPadding(cssCtx);
        } else {
            return 0;
        }
    }    
    
    public int getInlineWidth() {
        return inlineWidth;
    }

    public void setInlineWidth(int inlineWidth) {
        this.inlineWidth = inlineWidth;
    }
    
    // XXX Could we just not add them in the first place?
    public boolean containsContent() {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = (Object)getInlineChild(i);
            if (child instanceof InlineText) {
                InlineText iT = (InlineText)child;
                if (! iT.isEmpty()) {
                    return true;
                }
            } else if (child instanceof InlineBox) {
                InlineBox iB = (InlineBox)child;
                if (iB.containsContent()) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }
    
    public boolean intersectsInlineBlocks(CssContext cssCtx, Shape clip) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object obj = getInlineChild(i);
            
            if (obj instanceof InlineBox) {
                boolean possibleResult = 
                    ((InlineBox)obj).intersectsInlineBlocks(cssCtx, clip);
                if (possibleResult) {
                    return true;
                }
            } else if (obj instanceof Box) {
                BoxCollector collector = new BoxCollector();
                if (collector.intersectsAny(cssCtx, clip, (Box)obj)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public TextDecoration getTextDecoration() {
        return textDecoration;
    }

    public void setTextDecoration(TextDecoration textDecoration) {
        this.textDecoration = textDecoration;
    }
}
