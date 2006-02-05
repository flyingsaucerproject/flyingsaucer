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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
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
import org.xhtmlrenderer.layout.InlinePaintable;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;

public class InlineBox extends Box implements InlinePaintable {
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
        
        result.setStyle(getStyle());
        result.setHeight(getHeight());
        
        result.pending = this.pending;
        
        result.setContainingLayer(getContainingLayer());
        
        return result;
    }
    
    public void calculateHeight(LayoutContext c) {
        BorderPropertySet border = getStyle().getCalculatedStyle().getBorder(c);
        RectPropertySet padding = getStyle().getPaddingWidth(c);
        
        FSFontMetrics metrics = getStyle().getFSFontMetrics(c);
        
        setHeight((int)Math.ceil(border.top() + padding.top() + metrics.getAscent() + 
                metrics.getDescent() + padding.bottom() + border.bottom()));
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
            b.initContainingLayer(c);
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
            InlineBox iB = (InlineBox)getParent();
            if (iB.isPending()) {
                iB.unmarkPending(c);
            }
        }
        
        setStartsHere(true);
        
        if (getStyle().requiresLayer()) {
            c.pushLayer(this);
            getLayer().setInline(true);
            connectChildrenToCurrentLayer(c);
        }
    }
    
    public void connectChildrenToCurrentLayer(LayoutContext c) {
        if (getInlineChildCount() > 0) {
            for (int i = 0; i < getInlineChildCount(); i++) {
                Object obj = getInlineChild(i);
                if (obj instanceof Box) {
                    Box box = (Box)obj;
                    box.setContainingLayer(c.getLayer());
                    box.connectChildrenToCurrentLayer(c);
                }
            }
        }
    }
    
    public void paintInline(RenderingContext c) {
        if (! getStyle().isVisible()) {
            return;
        }
        
        paintBackground(c);
        paintBorder(c);
        
        if (c.debugDrawInlineBoxes()) {
            paintDebugOutline(c);
        }
        
        if (textDecoration != null) {
            IdentValue val = 
                getStyle().getCalculatedStyle().getIdent(CSSName.TEXT_DECORATION);
            if (val == IdentValue.UNDERLINE || val == IdentValue.OVERLINE) {
                c.getOutputDevice().drawTextDecoration(c, this);
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
                c.getOutputDevice().drawTextDecoration(c, this);
            }
        }
    }
    
    public int getBorderSides() {
        int result = BorderPainter.TOP + BorderPainter.BOTTOM;
        
        if (startsHere) {
            result += BorderPainter.LEFT;
        }
        if (endsHere) {
            result += BorderPainter.RIGHT;
        }
        
        return result;
    }
    
    protected Rectangle getBorderEdge(int left, int top, CssContext cssCtx) {
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
                (int)(left + marginLeft), 
                (int)(top - border.top() - padding.top()), 
                (int)(getInlineWidth(cssCtx) - marginLeft - marginRight), 
                getHeight());
        return result;
    }
    
    public Rectangle getBounds(int left, int top, CssContext cssCtx, int tx, int ty) {
        Rectangle result = getBorderEdge(left, top, cssCtx);
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
        if (marginRight > 0) {
            result.width += marginRight;
        }
        if (marginLeft > 0) {
            result.x -= marginLeft;
            result.width += marginLeft;
        }
        result.translate(tx, ty);
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
            return getStyle().getMarginBorderPadding(cssCtx, CalculatedStyle.LEFT);
        } else {
            return 0;
        }
    }
    
    public int getRightMarginPaddingBorder(CssContext cssCtx) {
        if (endsHere) {
            return getStyle().getMarginBorderPadding(cssCtx, CalculatedStyle.RIGHT);
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
    
    private void addToContentList(List list) {
        list.add(this);
        
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = (Object)getInlineChild(i);
            if (child instanceof InlineBox) {
                ((InlineBox)child).addToContentList(list);
            } else if (child instanceof Box) {
                list.add(child);
            }
        }
    }
    
    public LineBox getLineBox() {
        Box b = getParent();
        while (! (b instanceof LineBox)) {
            b = b.getParent();
        }
        return (LineBox)b;
    }
    
    public List getElementWithContent() {
        // inefficient, but the lists in question shouldn't be very long
        
        List result = new ArrayList();
        
        BlockBox container = (BlockBox)getLineBox().getParent();
        while (true) {
            List elementBoxes = container.getElementBoxes(this.element);
            for (int i = 0; i < elementBoxes.size(); i++) {
                InlineBox iB = (InlineBox)elementBoxes.get(i);
                iB.addToContentList(result);
            }
            
            if ( ! (container instanceof AnonymousBlockBox) ||
                    containsEnd(result)) {
                break;
            }
            
            container = addFollowingBlockBoxes(container, result);
            
            if (container == null) {
                break;
            }
        }
        
        return result;
    }
    
    private AnonymousBlockBox addFollowingBlockBoxes(BlockBox container, List result) {
        Box parent = container.getParent();
        int current = 0;
        for (; current < parent.getChildCount(); current++) {
            if (parent.getChild(current) == container) {
                current++;
                break;
            }
        }
        
        for (; current < parent.getChildCount(); current++) {
            if (parent.getChild(current) instanceof AnonymousBlockBox) {
                break;
            } else {
                result.add(parent.getChild(current));
            }
        }
        
        return current == parent.getChildCount() ? null : 
            (AnonymousBlockBox)parent.getChild(current);
    }
    
    private boolean containsEnd(List result) {
        
        for (int i = 0; i < result.size(); i++) {
            Box b = (Box)result.get(i);
            if (b instanceof InlineBox) {
                InlineBox iB = (InlineBox)b;
                if (this.element == iB.element && iB.isEndsHere()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public List getElementBoxes(Element elem) {
        List result = new ArrayList();
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = getInlineChild(i);
            if (child instanceof Box) {
                Box b = (Box)child;
                if (b.element == elem) {
                    result.add(b);
                }
                result.addAll(b.getElementBoxes(elem));
            }
        }
        return result;
    }
    
    public Dimension positionRelative(CssContext cssCtx) {
        Dimension delta = super.positionRelative(cssCtx);
        
        this.x -= delta.width;
        this.y -= delta.height;
        
        List toTranslate = getElementWithContent();
        
        for (int i = 0; i < toTranslate.size(); i++) {
            Box b = (Box)toTranslate.get(i);
            b.x += delta.width;
            b.y += delta.height;
            
            b.calcCanvasLocation();
            b.calcChildLocations();
        }
        
        return delta;
    }
    
    public void addAllChildren(List list, Layer layer) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = getInlineChild(i);
            if (child instanceof Box) {
                if (((Box)child).getContainingLayer() == layer) {
                    list.add(child);
                    if (child instanceof InlineBox) {
                        ((InlineBox)child).addAllChildren(list, layer);
                    }
                }
            }
        }
    }
    
    public void paintDebugOutline(RenderingContext c) {
        c.getOutputDevice().drawDebugOutline(c, this, Color.BLUE);
    }
    
    protected void detachChildren() {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object object = getInlineChild(i);
            if (object instanceof Box) {
                ((Box)object).detach();
                i--;
            }
        }
    }
    
    public void removeChild(Box child) {
        if (inlineChildren != null) {
            inlineChildren.remove(child);
        }
    }
    
    public void removeChild(int i) {
        if (inlineChildren != null) {
            inlineChildren.remove(i);
        }
    }
    
    public void calcCanvasLocation() {
        LineBox lineBox = getLineBox();
        setAbsX(lineBox.getAbsX() + this.x);
        setAbsY(lineBox.getAbsY() + this.y);
    }
    
    public void calcChildLocations() {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object obj = getInlineChild(i);
            if (obj instanceof Box) {
                Box child = (Box)obj;
                child.calcCanvasLocation();
                child.calcChildLocations();
            }
        }
    }
    
    public void lookForPageCounters(RenderingContext c) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object obj = getInlineChild(i);
            if (obj instanceof InlineText) {
                InlineText iT = (InlineText)obj;
                if (iT.isPageCounter()) {
                    iT.setPageCounterValue(c);
                }
            } else if (obj instanceof InlineBox) {
                ((InlineBox)obj).lookForPageCounters(c);
            }
        } 
    }

    public InlineText findTrailingText() {
        if (getInlineChildCount() == 0) {
            return null;
        }
        
        InlineText result = null;
        
        for (int offset = getInlineChildCount() - 1; offset >= 0; offset--) {
            Object child = getInlineChild(offset);
            if (child instanceof InlineText) {
                result = (InlineText)child;
                if (result.isEmpty()) {
                    continue;
                }
                return result;
            } else if (child instanceof InlineBox) {
                result = ((InlineBox)child).findTrailingText();
                if (result != null && result.isEmpty()) {
                    continue;
                }
                return result;
            } else {
                return null;
            }
        }
        
        return result;
    }
}
