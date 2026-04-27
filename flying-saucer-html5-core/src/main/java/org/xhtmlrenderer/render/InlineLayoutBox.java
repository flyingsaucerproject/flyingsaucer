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

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle.Edge;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.BoxCollector;
import org.xhtmlrenderer.layout.InlineBoxing;
import org.xhtmlrenderer.layout.InlinePaintable;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.PaintingInfo;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * A {@link Box} which contains the portion of an inline element laid out on a
 * single line.  It may contain content from several {@link InlineBox} objects
 * if the original inline element was interrupted by nested content.
 * Unlike other boxes, its children may be either {@code Box} objects
 * (for example, a box with {@code display: inline-block}) or
 * {@code InlineText} objects.  For this reason, it's children are not
 * stored in the {@code children} property, but instead stored in the
 * {@code inlineChildren} property.
 */
public final class InlineLayoutBox extends Box implements InlinePaintable, InlineChild {
    private int _baseline;

    private boolean _startsHere;
    private boolean _endsHere;

    private final List<InlineChild> _inlineChildren = new ArrayList<>(1);

    private boolean _pending;

    private int _inlineWidth;

    @Nullable
    private List<TextDecoration> _textDecorations;

    private final int _containingBlockWidth;

    private InlineLayoutBox(@Nullable Element elem, @Nullable CalculatedStyle style) {
        super(elem, style, false);
        setState(State.DONE);
        _containingBlockWidth = 0;
    }

    public InlineLayoutBox(LayoutContext c, @Nullable Element elem, @Nullable CalculatedStyle style, int cbWidth) {
        super(elem, style, false);
        _containingBlockWidth = cbWidth;
        setMarginTop(c, 0);
        setMarginBottom(c, 0);
        setPending(true);
        calculateHeight(c);
    }

    public InlineLayoutBox copyOf() {
        InlineLayoutBox result = new InlineLayoutBox(getElement(), getStyle());
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
        return _inlineChildren.size();
    }

    public void addInlineChild(LayoutContext c, InlineChild child) {
        addInlineChild(c, child, true);
    }

    public void addInlineChild(LayoutContext c, InlineChild child, boolean callUnmarkPending) {
        _inlineChildren.add(child);

        if (callUnmarkPending && isPending()) {
            unmarkPending(c);
        }

        if (child instanceof Box b) {
            b.setParent(this);
            b.initContainingLayer(c);
        } else if (child instanceof InlineText inlineText) {
            inlineText.setParent(this);
        } else {
            throw new IllegalArgumentException("Inline child of type " + child.getClass().getName() + " not supported");
        }
    }

    public List<InlineChild> getInlineChildren() {
        return _inlineChildren;
    }

    public InlineChild getInlineChild(int i) {
        return _inlineChildren.get(i);
    }

    public int getInlineWidth(CssContext cssContext) {
        return _inlineWidth;
    }

    public void prunePending() {
        if (getInlineChildCount() > 0) {
            for (int i = getInlineChildCount() - 1; i >= 0; i--) {
                InlineChild child = getInlineChild(i);
                if (! (child instanceof InlineLayoutBox iB)) {
                    break;
                }

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

        if (getParent() instanceof InlineLayoutBox iB) {
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

    @Override
    public void connectChildrenToCurrentLayer(LayoutContext c) {
        if (getInlineChildCount() > 0) {
            for (int i = 0; i < getInlineChildCount(); i++) {
                InlineChild obj = getInlineChild(i);
                if (obj instanceof Box box) {
                    box.setContainingLayer(c.getLayer());
                    box.connectChildrenToCurrentLayer(c);
                }
            }
        }
    }

    public void paintSelection(RenderingContext c) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild child = getInlineChild(i);
            if (child instanceof InlineText inlineText) {
                inlineText.paintSelection(c);
            }
        }
    }

    @Override
    public void paintInline(RenderingContext c) {
        if (! getStyle().isVisible()) {
            return;
        }

        paintBackground(c);
        paintBorder(c);

        if (c.debugDrawInlineBoxes()) {
            paintDebugOutline(c);
        }

        List<TextDecoration> textDecorations = getTextDecorations();
        if (textDecorations != null) {
            for (TextDecoration tD : textDecorations) {
                IdentValue ident = tD.getIdentValue();
                if (ident == IdentValue.UNDERLINE || ident == IdentValue.OVERLINE) {
                    c.getOutputDevice().drawTextDecoration(c, this, tD);
                }
            }
        }

        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild child = getInlineChild(i);
            if (child instanceof InlineText inlineText) {
                inlineText.paint(c);
            }
        }

        if (textDecorations != null) {
            for (TextDecoration tD : textDecorations) {
                IdentValue ident = tD.getIdentValue();
                if (ident == IdentValue.LINE_THROUGH) {
                    c.getOutputDevice().drawTextDecoration(c, this, tD);
                }
            }
        }
    }

    @Override
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

    @Override
    public Rectangle getBorderEdge(int left, int top, CssContext cssCtx) {
        // x, y pins the content area of the box so subtract off top border and padding
        // too

        MarginLeftRight margin = getMarginLeftRight(cssCtx);

        BorderPropertySet border = getBorder(cssCtx);
        RectPropertySet padding = getPadding(cssCtx);

        return new Rectangle(
                (int)(left + margin.left),
                (int)(top - border.top() - padding.top()),
                (int)(getInlineWidth(cssCtx) - margin.left - margin.right),
                getHeight());
    }

    private record MarginLeftRight(float left, float right) {}

    private MarginLeftRight getMarginLeftRight(CssContext cssCtx) {
        float marginLeft = 0;
        float marginRight = 0;
        if (_startsHere || _endsHere) {
            RectPropertySet margin = getMargin(cssCtx);
            if (_startsHere) {
                marginLeft = margin.left();
            }
            if (_endsHere) {
                marginRight = margin.right();
            }
        }
        return new MarginLeftRight(marginLeft, marginRight);
    }

    @Override
    public Rectangle getMarginEdge(int left, int top, CssContext cssCtx, int tx, int ty) {
        Rectangle result = getBorderEdge(left, top, cssCtx);
        MarginLeftRight margin = getMarginLeftRight(cssCtx);
        if (margin.right > 0) {
            result.width += (int) margin.right;
        }
        if (margin.left > 0) {
            result.x -= (int) margin.left;
            result.width += (int) margin.left;
        }
        result.translate(tx, ty);
        return result;
    }

    @Override
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
            RectPropertySet margin = getMargin(cssCtx);
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

        return new Rectangle(
                (int)(left + marginLeft + borderLeft + paddingLeft),
                (int)(top - border.top() - padding.top()),
                (int)(getInlineWidth(cssCtx) - marginLeft - borderLeft - paddingLeft
                    - paddingRight - borderRight - marginRight),
                getHeight());
    }

    public int getLeftMarginBorderPadding(CssContext cssCtx) {
        if (_startsHere) {
            return getMarginBorderPadding(cssCtx, Edge.LEFT);
        } else {
            return 0;
        }
    }

    public int getRightMarginPaddingBorder(CssContext cssCtx) {
        if (_endsHere) {
            return getMarginBorderPadding(cssCtx, Edge.RIGHT);
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
            InlineChild child = getInlineChild(i);
            switch (child) {
                case InlineText iT -> {
                    if (!iT.isEmpty()) {
                        return true;
                    }
                }
                case InlineLayoutBox iB -> {
                    if (iB.isContainsVisibleContent()) {
                        return true;
                    }
                }
                case Box b -> {
                    if (b.getWidth() > 0 || b.getHeight() > 0) {
                        return true;
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + child);
            }
        }
        return false;
    }

    public boolean intersectsInlineBlocks(CssContext cssCtx, Shape clip) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild obj = getInlineChild(i);

            switch (obj) {
                case InlineLayoutBox inlineLayoutBox -> {
                    boolean possibleResult = inlineLayoutBox.intersectsInlineBlocks(cssCtx, clip);
                    if (possibleResult) {
                        return true;
                    }
                }
                case Box box -> {
                    BoxCollector collector = new BoxCollector();
                    if (collector.intersectsAny(cssCtx, clip, box)) {
                        return true;
                    }
                }
                default -> {
                }
            }
        }

        return false;
    }

    @Nullable
    @CheckReturnValue
    public List<TextDecoration> getTextDecorations() {
        return _textDecorations;
    }

    public void setTextDecorations(List<TextDecoration> textDecoration) {
        _textDecorations = unmodifiableList(requireNonNull(textDecoration));
    }

    private void addToContentList(List<Box> list) {
        list.add(this);

        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild child = getInlineChild(i);
            switch (child) {
                case InlineLayoutBox inlineLayoutBox -> inlineLayoutBox.addToContentList(list);
                case Box box -> list.add(box);
                default -> {
                }
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

    public List<Box> getElementWithContent() {
        // inefficient, but the lists in question shouldn't be very long

        List<Box> result = new ArrayList<>();

        BlockBox container = (BlockBox)getLineBox().getParent();
        while (true) {
            List<Box> elementBoxes = container.getElementBoxes(getElement());
            for (Box elementBox : elementBoxes) {
                ((InlineLayoutBox) elementBox).addToContentList(result);
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

    private AnonymousBlockBox addFollowingBlockBoxes(BlockBox container, List<Box> result) {
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

    private boolean containsEnd(List<Box> result) {
        for (Box box : result) {
            if (box instanceof InlineLayoutBox iB) {
                if (getElement() == iB.getElement() && iB.isEndsHere()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<Box> getElementBoxes(Element elem) {
        List<Box> result = new ArrayList<>();
        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild child = getInlineChild(i);
            if (child instanceof Box b) {
                if (b.getElement() == elem) {
                    result.add(b);
                }
                result.addAll(b.getElementBoxes(elem));
            }
        }
        return result;
    }

    @Override
    public Dimension positionRelative(CssContext cssCtx) {
        Dimension delta = super.positionRelative(cssCtx);

        setX(getX() - delta.width);
        setY(getY() - delta.height);

        List<Box> toTranslate = getElementWithContent();

        for (Box box : toTranslate) {
            box.setX(box.getX() + delta.width);
            box.setY(box.getY() + delta.height);

            box.calcCanvasLocation();
            box.calcChildLocations();
        }

        return delta;
    }

    public void addAllChildren(List<Box> list, Layer layer) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild child = getInlineChild(i);
            if (child instanceof Box box) {
                if (box.getContainingLayer() == layer) {
                    list.add(box);
                    if (child instanceof InlineLayoutBox inlineLayoutBox) {
                        inlineLayoutBox.addAllChildren(list, layer);
                    }
                }
            }
        }
    }

    public void paintDebugOutline(RenderingContext c) {
        c.getOutputDevice().drawDebugOutline(c, this, FSRGBColor.BLUE);
    }

    @Override
    protected void resetChildren(LayoutContext c) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild object = getInlineChild(i);
            if (object instanceof Box box) {
                box.reset(c);
            }
        }
    }

    @Override
    public void removeChild(Box child) {
        _inlineChildren.remove(child);
    }

    @Override
    public void removeChild(int i) {
        _inlineChildren.remove(i);
    }

    @Nullable
    @CheckReturnValue
    @Override
    protected Box getPrevious(Box child) {
        for (int i = 0; i < _inlineChildren.size() - 1; i++) {
            Object obj = _inlineChildren.get(i);
            if (obj == child) {
                if (i == 0) {
                    return null;
                } else {
                    Object previous = _inlineChildren.get(i-1);
                    return previous instanceof Box box ? box : null;
                }
            }
        }

        return null;
    }

    @Nullable
    @CheckReturnValue
    @Override
    protected Box getNext(Box child) {
        for (int i = 0; i < _inlineChildren.size() - 1; i++) {
            Object obj = _inlineChildren.get(i);
            if (obj == child) {
                Object next = _inlineChildren.get(i+1);
                return next instanceof Box box ? box : null;
            }
        }

        return null;
    }

    @Override
    public void calcCanvasLocation() {
        LineBox lineBox = getLineBox();
        setAbsX(lineBox.getAbsX() + getX());
        setAbsY(lineBox.getAbsY() + getY());
    }

    @Override
    public void calcChildLocations() {
        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild obj = getInlineChild(i);
            if (obj instanceof Box child) {
                child.calcCanvasLocation();
                child.calcChildLocations();
            }
        }
    }

    @Override
    public void clearSelection(List<Box> modified) {
        boolean changed = false;
        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild obj = getInlineChild(i);
            switch (obj) {
                case Box box -> box.clearSelection(modified);
                default -> changed = changed || ((InlineText) obj).clearSelection();
            }
        }

        if (changed) {
            modified.add(this);
        }
    }

    @Override
    public void selectAll() {
        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild obj = getInlineChild(i);
            switch (obj) {
                case Box box -> box.selectAll();
                case InlineText inlineText -> inlineText.selectAll();
                default -> throw new IllegalStateException("Unexpected inline child type: " + obj.getClass().getName());
            }
        }
    }

    @Override
    protected void calcChildPaintingInfo(
            CssContext c, PaintingInfo result, boolean useCache) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild obj = getInlineChild(i);
            if (obj instanceof Box box) {
                PaintingInfo info = box.calcPaintingInfo(c, useCache);
                moveIfGreater(result.getOuterMarginCorner(), info.getOuterMarginCorner());
                result.getAggregateBounds().add(info.getAggregateBounds());
            }
        }
    }

    public void lookForDynamicFunctions(RenderingContext c) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild obj = getInlineChild(i);
            switch (obj) {
                case InlineText iT -> {
                    if (iT.isDynamicFunction()) {
                        iT.updateDynamicValue(c);
                    }
                }
                case InlineLayoutBox inlineLayoutBox -> inlineLayoutBox.lookForDynamicFunctions(c);
                default -> {
                }
            }
        }
    }

    @Nullable
    @CheckReturnValue
    public InlineText findTrailingText() {
        if (getInlineChildCount() == 0) {
            return null;
        }

        InlineText result = null;

        for (int offset = getInlineChildCount() - 1; offset >= 0; offset--) {
            InlineChild child = getInlineChild(offset);
            switch (child) {
                case InlineText inlineText -> {
                    result = inlineText;
                    if (result.isEmpty()) {
                        continue;
                    }
                    return result;
                }
                case InlineLayoutBox inlineLayoutBox -> {
                    result = inlineLayoutBox.findTrailingText();
                    if (result != null && result.isEmpty()) {
                        continue;
                    }
                    return result;
                }
                default -> {
                    return null;
                }
            }
        }

        return result;
    }

    public void calculateTextDecoration(LayoutContext c) {
        List<TextDecoration> decorations =
            InlineBoxing.calculateTextDecorations(c, this, getBaseline(), getStyle().getFSFontMetrics(c));
        setTextDecorations(decorations);
    }

    @Nullable
    @CheckReturnValue
    @Override
    public Box find(CssContext cssCtx, int absX, int absY, boolean findAnonymous) {
        PaintingInfo pI = getPaintingInfo();
        if (pI != null && ! pI.getAggregateBounds().contains(absX, absY)) {
            return null;
        }

        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild child = getInlineChild(i);
            if (child instanceof Box childBox) {
                    Box result = childBox.find(cssCtx, absX, absY, findAnonymous);
                    if (result != null) {
                        return result;
                    }
            }
        }

        Rectangle edge = getContentAreaEdge(getAbsX(), getAbsY(), cssCtx);
        Box result = edge.contains(absX, absY) && getStyle().isVisible() ? this : null;

        if (! findAnonymous && result != null && getElement() == null) {
            return getParent().getParent();
        } else {
            return result;
        }
    }

    @Override
    public int getContainingBlockWidth() {
        return _containingBlockWidth;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getClass().getSimpleName());
        result.append(": ");
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
        appendPosition(result);
        appendSize(result);
        return result.toString();
    }

    @Override
    public String dump(LayoutContext c, String indent, Dump which) {
        if (which != Dump.RENDER) {
            throw new IllegalArgumentException("Which " + which + " not supported");
        }

        StringBuilder result = new StringBuilder(indent);
        result.append(this);

        for (InlineChild obj : getInlineChildren()) {
            result.append('\n');
            if (obj instanceof Box b) {
                result.append(b.dump(c, indent + "  ", which));
                if (result.charAt(result.length()-1) == '\n') {
                    result.deleteCharAt(result.length()-1);
                }
            } else {
                result.append(indent).append("  ").append(obj);
            }
        }

        return result.toString();
    }

    @Override
    public void restyle(LayoutContext c) {
        super.restyle(c);
        calculateTextDecoration(c);
    }

    @Override
    protected void restyleChildren(LayoutContext c) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            InlineChild obj = getInlineChild(i);
            if (obj instanceof Box box) {
                box.restyle(c);
            }
        }
    }

    @Override
    public Box getRestyleTarget() {
        // Inline boxes may be broken across lines so back out
        // to the nearest block box
        Box result = getParent();
        while (result instanceof InlineLayoutBox) {
            result = result.getParent();
        }
        return result.getParent();
    }

    @Override
    public void collectText(RenderingContext c, StringBuilder buffer) throws IOException {
        for (InlineChild obj : getInlineChildren()) {
            if (obj instanceof InlineText inlineText) {
                buffer.append(inlineText.getTextExportText());
            } else if (obj instanceof Box box) {
                box.collectText(c, buffer);
            } else {
                throw new IllegalStateException("Unexpected inline child type: " + obj.getClass().getName());
            }
        }
    }

    public void countJustifiableChars(CharCounts counts) {
        boolean justifyThis = getStyle().isTextJustify();
        for (InlineChild o : getInlineChildren()) {
            if (o instanceof InlineLayoutBox inlineLayoutBox) {
                inlineLayoutBox.countJustifiableChars(counts);
            } else if (o instanceof InlineText inlineText && justifyThis) {
                inlineText.countJustifiableChars(counts);
            }
        }
    }

    public float adjustHorizontalPosition(JustificationInfo info, float adjust) {
        float runningTotal = adjust;

        float result = 0.0f;

        for (InlineChild o : getInlineChildren()) {
            switch (o) {
                case InlineText iT -> {

                    iT.setX(iT.getX() + Math.round(result));

                    float adj = iT.calcTotalAdjustment(info);
                    result += adj;
                    runningTotal += adj;
                }
                case Box b -> {
                    b.setX(b.getX() + Math.round(runningTotal));

                    if (b instanceof InlineLayoutBox) {
                        float adj = ((InlineLayoutBox) b).adjustHorizontalPosition(info, runningTotal);
                        result += adj;
                        runningTotal += adj;
                    }
                }
                default -> {
                }
            }
        }

        return result;
    }

    @Override
    public int getEffectiveWidth() {
        return getInlineWidth();
    }
}
