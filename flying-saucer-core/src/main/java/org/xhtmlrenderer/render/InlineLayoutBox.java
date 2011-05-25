/*
 * {{{ header & license
 * Copyright (c) 2005 Joshua Marinacci
 * Copyright (c) 2005, 2007 Wisconsin Court System
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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.BoxCollector;
import org.xhtmlrenderer.layout.InlineBoxing;
import org.xhtmlrenderer.layout.InlinePaintable;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.PaintingInfo;

/**
 * A {@link Box} which contains the portion of an inline element layed out on a
 * single line.  It may contain content from several {@link InlineBox} objects
 * if the original inline element was interrupted by nested content.  
 * Unlike other boxes, its children may be either <code>Box</code> objects
 * (for example, a box with <code>display: inline-block</code>) or 
 * <code>InlineText</code> objects.  For this reason, it's children are not
 * stored in the <code>children</code> property, but instead stored in the 
 * <code>inlineChildren</code> property.  
 */
public class InlineLayoutBox extends Box implements InlinePaintable {
    private int _baseline;
    
    private boolean _startsHere;
    private boolean _endsHere;
    
    private List _inlineChildren;
    
    private boolean _pending;
    
    private int _inlineWidth;
    
    private List _textDecorations;
    
    private int _containingBlockWidth;
    
    public InlineLayoutBox(LayoutContext c, Element elem, CalculatedStyle style, int cbWidth) {
        this();
        setElement(elem);
        setStyle(style);
        setContainingBlockWidth(cbWidth);
        setMarginTop(c, 0);
        setMarginBottom(c, 0);
        setPending(true);
        calculateHeight(c);
    }
    
    private InlineLayoutBox() {
        setState(Box.DONE);
    }
    
    public InlineLayoutBox copyOf() {
        InlineLayoutBox result = new InlineLayoutBox();
        result.setElement(getElement());
        
        result.setStyle(getStyle());
        result.setHeight(getHeight());
        
        result._pending = _pending;
        
        result.setContainingLayer(getContainingLayer());
        
        return result;
    }
    
    public void calculateHeight(LayoutContext c) {
        BorderPropertySet border = getBorder(c);
        RectPropertySet padding = getPadding(c);
        
        FSFontMetrics metrics = getStyle().getFSFontMetrics(c);
        
        setHeight((int)Math.ceil(border.top() + padding.top() + metrics.getAscent() + 
                metrics.getDescent() + padding.bottom() + border.bottom()));
    }

    public int getBaseline() {
        return _baseline;
    }

    public void setBaseline(int baseline) {
        _baseline = baseline;
    }

    public int getInlineChildCount() {
        return _inlineChildren == null ? 0 : _inlineChildren.size();
    }
    
    public void addInlineChild(LayoutContext c, Object child) {
        addInlineChild(c, child, true);
    }
    
    public void addInlineChild(LayoutContext c, Object child, boolean callUnmarkPending) {
        if (_inlineChildren == null) {
            _inlineChildren = new ArrayList();
        }
        
        _inlineChildren.add(child);
        
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
        return _inlineChildren == null ? Collections.EMPTY_LIST : _inlineChildren;
    }
    
    public Object getInlineChild(int i) {
        if (_inlineChildren == null) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            return _inlineChildren.get(i);
        }
    }
    
    public int getInlineWidth(CssContext cssCtx) {
        return _inlineWidth;
    }
    
    public void prunePending() {
        if (getInlineChildCount() > 0) {
            for (int i = getInlineChildCount() - 1; i >= 0; i--) {
                Object child = (Object)getInlineChild(i);
                if (! (child instanceof InlineLayoutBox)) {
                    break;
                }
                
                InlineLayoutBox iB = (InlineLayoutBox)child;
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
        return _endsHere;
    }

    public void setEndsHere(boolean endsHere) {
        _endsHere = endsHere;
    }

    public boolean isStartsHere() {
        return _startsHere;
    }

    public void setStartsHere(boolean startsHere) {
        _startsHere = startsHere;
    }

    public boolean isPending() {
        return _pending;
    }
    
    public void setPending(boolean b) {
        _pending = b;
    }
    
    public void unmarkPending(LayoutContext c) {
        _pending = false;
        
        if (getParent() instanceof InlineLayoutBox) {
            InlineLayoutBox iB = (InlineLayoutBox)getParent();
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
    
    public void paintSelection(RenderingContext c) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = getInlineChild(i);
            if (child instanceof InlineText) {
                ((InlineText)child).paintSelection(c);
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
        
        List textDecorations = getTextDecorations();
        if (textDecorations != null) {
            for (Iterator i = textDecorations.iterator(); i.hasNext(); ) {
                TextDecoration tD = (TextDecoration)i.next();
                IdentValue ident = tD.getIdentValue();
                if (ident == IdentValue.UNDERLINE || ident == IdentValue.OVERLINE) {
                    c.getOutputDevice().drawTextDecoration(c, this, tD);    
                }
            }
        }
        
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = getInlineChild(i);
            if (child instanceof InlineText) {
                ((InlineText)child).paint(c);
            }
        }
        
        if (textDecorations != null) {
            for (Iterator i = textDecorations.iterator(); i.hasNext(); ) {
                TextDecoration tD = (TextDecoration)i.next();
                IdentValue ident = tD.getIdentValue();
                if (ident == IdentValue.LINE_THROUGH) {
                    c.getOutputDevice().drawTextDecoration(c, this, tD);    
                }
            }
        }
    }
    
    public int getBorderSides() {
        int result = BorderPainter.TOP + BorderPainter.BOTTOM;
        
        if (_startsHere) {
            result += BorderPainter.LEFT;
        }
        if (_endsHere) {
            result += BorderPainter.RIGHT;
        }
        
        return result;
    }
    
    public Rectangle getBorderEdge(int left, int top, CssContext cssCtx) {
        // x, y pins the content area of the box so subtract off top border and padding
        // too
        
        float marginLeft = 0;
        float marginRight = 0;
        if (_startsHere || _endsHere) {
            RectPropertySet margin = (RectPropertySet)getMargin(cssCtx);
            if (_startsHere) {
                marginLeft = margin.left();
            } 
            if (_endsHere) {
                marginRight = margin.right();
            }
        }
        BorderPropertySet border = getBorder(cssCtx);
        RectPropertySet padding = getPadding(cssCtx);
        
        Rectangle result = new Rectangle(
                (int)(left + marginLeft), 
                (int)(top - border.top() - padding.top()), 
                (int)(getInlineWidth(cssCtx) - marginLeft - marginRight), 
                getHeight());
        return result;
    }
    
    public Rectangle getMarginEdge(int left, int top, CssContext cssCtx, int tx, int ty) {
        Rectangle result = getBorderEdge(left, top, cssCtx);
        float marginLeft = 0;
        float marginRight = 0;
        if (_startsHere || _endsHere) {
            RectPropertySet margin = (RectPropertySet)getMargin(cssCtx);
            if (_startsHere) {
                marginLeft = margin.left();
            } 
            if (_endsHere) {
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
    
    public Rectangle getContentAreaEdge(int left, int top, CssContext cssCtx) {
        BorderPropertySet border = getBorder(cssCtx);
        RectPropertySet padding = getPadding(cssCtx);
        
        float marginLeft = 0;
        float marginRight = 0;
        
        float borderLeft = 0;
        float borderRight = 0;
        
        float paddingLeft = 0;
        float paddingRight = 0;
        
        if (_startsHere || _endsHere) {
            RectPropertySet margin = (RectPropertySet)getMargin(cssCtx);
            if (_startsHere) {
                marginLeft = margin.left();
                borderLeft = border.left();
                paddingLeft = padding.left();
            } 
            if (_endsHere) {
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
        if (_startsHere) {
            return getMarginBorderPadding(cssCtx, CalculatedStyle.LEFT);
        } else {
            return 0;
        }
    }
    
    public int getRightMarginPaddingBorder(CssContext cssCtx) {
        if (_endsHere) {
            return getMarginBorderPadding(cssCtx, CalculatedStyle.RIGHT);
        } else {
            return 0;
        }
    }    
    
    public int getInlineWidth() {
        return _inlineWidth;
    }

    public void setInlineWidth(int inlineWidth) {
        _inlineWidth = inlineWidth;
    }
    
    public boolean isContainsVisibleContent() {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = (Object)getInlineChild(i);
            if (child instanceof InlineText) {
                InlineText iT = (InlineText)child;
                if (! iT.isEmpty()) {
                    return true;
                }
            } else if (child instanceof InlineLayoutBox) {
                InlineLayoutBox iB = (InlineLayoutBox)child;
                if (iB.isContainsVisibleContent()) {
                    return true;
                }
            } else {
                Box b = (Box)child;
                if (b.getWidth() > 0 || b.getHeight() > 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean intersectsInlineBlocks(CssContext cssCtx, Shape clip) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object obj = getInlineChild(i);
            
            if (obj instanceof InlineLayoutBox) {
                boolean possibleResult = 
                    ((InlineLayoutBox)obj).intersectsInlineBlocks(cssCtx, clip);
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

    public List getTextDecorations() {
        return _textDecorations;
    }

    public void setTextDecorations(List textDecoration) {
        _textDecorations = textDecoration;
    }
    
    private void addToContentList(List list) {
        list.add(this);
        
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = (Object)getInlineChild(i);
            if (child instanceof InlineLayoutBox) {
                ((InlineLayoutBox)child).addToContentList(list);
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
            List elementBoxes = container.getElementBoxes(getElement());
            for (int i = 0; i < elementBoxes.size(); i++) {
                InlineLayoutBox iB = (InlineLayoutBox)elementBoxes.get(i);
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
            if (b instanceof InlineLayoutBox) {
                InlineLayoutBox iB = (InlineLayoutBox)b;
                if (getElement() == iB.getElement() && iB.isEndsHere()) {
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
                if (b.getElement() == elem) {
                    result.add(b);
                }
                result.addAll(b.getElementBoxes(elem));
            }
        }
        return result;
    }
    
    public Dimension positionRelative(CssContext cssCtx) {
        Dimension delta = super.positionRelative(cssCtx);
        
        setX(getX() - delta.width);
        setY(getY() - delta.height);
        
        List toTranslate = getElementWithContent();
        
        for (int i = 0; i < toTranslate.size(); i++) {
            Box b = (Box)toTranslate.get(i);
            b.setX(b.getX() + delta.width);
            b.setY(b.getY() + delta.height);
            
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
                    if (child instanceof InlineLayoutBox) {
                        ((InlineLayoutBox)child).addAllChildren(list, layer);
                    }
                }
            }
        }
    }
    
    public void paintDebugOutline(RenderingContext c) {
        c.getOutputDevice().drawDebugOutline(c, this, FSRGBColor.BLUE);
    }
    
    protected void resetChildren(LayoutContext c) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object object = getInlineChild(i);
            if (object instanceof Box) {
                ((Box)object).reset(c);
            }
        }
    }
    
    public void removeChild(Box child) {
        if (_inlineChildren != null) {
            _inlineChildren.remove(child);
        }
    }
    
    public void removeChild(int i) {
        if (_inlineChildren != null) {
            _inlineChildren.remove(i);
        }
    }
    
    protected Box getPrevious(Box child) {
        if (_inlineChildren == null) {
            return null;
        }
        
        for (int i = 0; i < _inlineChildren.size() - 1; i++) {
            Object obj = _inlineChildren.get(i);
            if (obj == child) {
                if (i == 0) {
                    return null;
                } else {
                    Object previous = _inlineChildren.get(i-1);
                    return previous instanceof Box ? (Box)previous : null;
                }
            }
        }
        
        return null;
    }
    
    protected Box getNext(Box child) {
        if (_inlineChildren == null) {
            return null;
        }
        
        for (int i = 0; i < _inlineChildren.size() - 1; i++) {
            Object obj = _inlineChildren.get(i);
            if (obj == child) {
                Object next = _inlineChildren.get(i+1);
                return next instanceof Box ? (Box)next : null;
            }
        }
        
        return null;
    }
    
    public void calcCanvasLocation() {
        LineBox lineBox = getLineBox();
        setAbsX(lineBox.getAbsX() + getX());
        setAbsY(lineBox.getAbsY() + getY());
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
    
    public void clearSelection(List modified) {
        boolean changed = false;
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object obj = getInlineChild(i);
            if (obj instanceof Box) {
                ((Box)obj).clearSelection(modified);
            } else {
                changed |= ((InlineText)obj).clearSelection();
            }
        }
        
        if (changed) {
            modified.add(this);
        }
    }
    
    public void selectAll() {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object obj = getInlineChild(i);
            if (obj instanceof Box) {
                ((Box)obj).selectAll();
            } else {
                ((InlineText)obj).selectAll();
            }
        }
    }
    
    protected void calcChildPaintingInfo(
            CssContext c, PaintingInfo result, boolean useCache) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object obj = getInlineChild(i);
            if (obj instanceof Box) {
                PaintingInfo info = ((Box)obj).calcPaintingInfo(c, useCache);
                moveIfGreater(result.getOuterMarginCorner(), info.getOuterMarginCorner());
                result.getAggregateBounds().add(info.getAggregateBounds());
            } 
        }
    }
    
    public void lookForDynamicFunctions(RenderingContext c) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object obj = getInlineChild(i);
            if (obj instanceof InlineText) {
                InlineText iT = (InlineText)obj;
                if (iT.isDynamicFunction()) {
                    iT.updateDynamicValue(c);
                }
            } else if (obj instanceof InlineLayoutBox) {
                ((InlineLayoutBox)obj).lookForDynamicFunctions(c);
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
            } else if (child instanceof InlineLayoutBox) {
                result = ((InlineLayoutBox)child).findTrailingText();
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
    
    public void calculateTextDecoration(LayoutContext c) {
        List decorations = 
            InlineBoxing.calculateTextDecorations(this, getBaseline(), 
                    getStyle().getFSFontMetrics(c));
        setTextDecorations(decorations);
    }
    
    public Box find(CssContext cssCtx, int absX, int absY, boolean findAnonymous) {
        PaintingInfo pI = getPaintingInfo();
        if (pI != null && ! pI.getAggregateBounds().contains(absX, absY)) {
            return null;
        }
        
        Box result = null;
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = getInlineChild(i);
            if (child instanceof Box) {
                    result = ((Box)child).find(cssCtx, absX, absY, findAnonymous);
                    if (result != null) {
                        return result;
                    }
            }
        }
        
        Rectangle edge = getContentAreaEdge(getAbsX(), getAbsY(), cssCtx);
        result = edge.contains(absX, absY) && getStyle().isVisible() ? this : null;
        
        if (! findAnonymous && result != null && getElement() == null) {
            return getParent().getParent();
        } else {
            return result;
        }
    }

    public int getContainingBlockWidth() {
        return _containingBlockWidth;
    }

    public void setContainingBlockWidth(int containingBlockWidth) {
        _containingBlockWidth = containingBlockWidth;
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("InlineLayoutBox: ");
        if (getElement() != null) {
            result.append("<");
            result.append(getElement().getNodeName());
            result.append("> ");
        } else {
            result.append("(anonymous) ");
        }       
        if (isStartsHere() || isEndsHere()) {
            result.append("(");
            if (isStartsHere()) {
                result.append("S");
            }
            if (isEndsHere()) {
                result.append("E");
            }
            result.append(") ");
        }
        result.append("(baseline=");
        result.append(_baseline);
        result.append(") ");
        result.append("(" + getAbsX() + "," + getAbsY() + ")->(" + getInlineWidth() + " x " + getHeight() + ")");
        return result.toString();
    } 
    
    public String dump(LayoutContext c, String indent, int which) {
        if (which != Box.DUMP_RENDER) {
            throw new IllegalArgumentException();
        }
        
        StringBuffer result = new StringBuffer(indent);
        result.append(this);
        result.append('\n');
        
        for (Iterator i = getInlineChildren().iterator(); i.hasNext(); ) {
            Object obj = i.next();
            if (obj instanceof Box) {
                Box b = (Box)obj;
                result.append(b.dump(c, indent + "  ", which));
                if (result.charAt(result.length()-1) == '\n') {
                    result.deleteCharAt(result.length()-1);
                }
            } else {
                result.append(indent + "  ");
                result.append(obj.toString());
            }
            if (i.hasNext()) {
                result.append('\n');
            }
        }
        
        return result.toString();
    }
    
    public void restyle(LayoutContext c) {
        super.restyle(c);
        calculateTextDecoration(c);
    }
    
    protected void restyleChildren(LayoutContext c) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object obj = getInlineChild(i);
            if (obj instanceof Box) {
                ((Box)obj).restyle(c);
            }
        }
    }
    
    public Box getRestyleTarget() {
        // Inline boxes may be broken across lines so back out
        // to the nearest block box
        Box result = getParent();
        while (result instanceof InlineLayoutBox) {
            result = result.getParent();
        }
        return result.getParent();
    }
    
    public void collectText(RenderingContext c, StringBuffer buffer) throws IOException {
        for (Iterator i = getInlineChildren().iterator(); i.hasNext(); ) {
            Object obj = (Object)i.next();
            if (obj instanceof InlineText) {
                buffer.append(((InlineText)obj).getTextExportText());
            } else {
                ((Box)obj).collectText(c, buffer);
            }
        }
    }
    
    public void countJustifiableChars(CharCounts counts) {
        boolean justifyThis = getStyle().isTextJustify();
        for (Iterator i = getInlineChildren().iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (o instanceof InlineLayoutBox) {
                ((InlineLayoutBox)o).countJustifiableChars(counts);
            } else if (o instanceof InlineText && justifyThis) {
                ((InlineText)o).countJustifiableChars(counts);
            }
        }
    }
    
    public float adjustHorizontalPosition(JustificationInfo info, float adjust) {
        float runningTotal = adjust;
        
        float result = 0.0f;
        
        for (Iterator i = getInlineChildren().iterator(); i.hasNext(); ) {
            Object o = i.next();
            
            if (o instanceof InlineText) {
                InlineText iT = (InlineText)o;
                
                iT.setX(iT.getX() + Math.round(result));
                
                float adj = iT.calcTotalAdjustment(info);
                result += adj;
                runningTotal += adj;
            } else {
                Box b = (Box)o;
                b.setX(b.getX() + Math.round(runningTotal));
                
                if (b instanceof InlineLayoutBox) {
                    float adj = ((InlineLayoutBox)b).adjustHorizontalPosition(info, runningTotal);
                    result += adj;
                    runningTotal += adj;
                }
            }
        }
        
        return result;
    }
    
    public int getEffectiveWidth() {
        return getInlineWidth();
    }
}
