/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2006, 2007 Wisconsin Courts System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.layout.BlockBoxing;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.FloatManager;
import org.xhtmlrenderer.layout.InlineBoxing;
import org.xhtmlrenderer.layout.InlinePaintable;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.PaintingInfo;
import org.xhtmlrenderer.layout.PersistentBFC;
import org.xhtmlrenderer.layout.Styleable;
import org.xhtmlrenderer.newtable.TableRowBox;

public class BlockBox extends Box implements InlinePaintable {

    public static final int POSITION_VERTICALLY = 1;
    public static final int POSITION_HORIZONTALLY = 2;
    public static final int POSITION_BOTH = POSITION_VERTICALLY | POSITION_HORIZONTALLY;
    
    public static final int CONTENT_UNKNOWN = 0;
    public static final int CONTENT_INLINE = 1;
    public static final int CONTENT_BLOCK = 2;
    public static final int CONTENT_EMPTY = 4;
    
    protected static final int NO_BASELINE = Integer.MIN_VALUE;
    
    private MarkerData _markerData;
    
    private int _listCounter;
    
    private PersistentBFC _persistentBFC;
    
    private Box _staticEquivalent;
    
    private boolean _resetMargins;
    private boolean _needPageClear;
    
    private ReplacedElement _replacedElement;
    
    private int _childrenContentType;
    
    private List _inlineContent;
    
    private boolean _topMarginCalculated;
    private boolean _bottomMarginCalculated;
    private MarginCollapseResult _pendingCollapseCalculation;
    
    private int _minWidth;
    private int _maxWidth;
    private boolean _minMaxCalculated;
    
    private boolean _dimensionsCalculated;
    private boolean _needShrinkToFitCalculatation;
    
    private CascadedStyle _firstLineStyle;
    private CascadedStyle _firstLetterStyle;
    
    private FloatedBoxData _floatedBoxData;
    
    private int _childrenHeight;
    
    public BlockBox() {
        super();
    }
    
    public void expandToMaxChildWidth() {
        int maxChildWidth = 0;
        for (int i = 0; i < getChildCount(); i++) {
            Box child = (Box)getChild(i);
            if (child instanceof BlockBox) {
                ((BlockBox)child).expandToMaxChildWidth();
            }
            int childWidth = child.getWidth();
            if (childWidth > maxChildWidth) {
                maxChildWidth = childWidth;
            }
        }
        if (getStyle().isAutoWidth() && maxChildWidth > getContentWidth()) {
            setContentWidth(maxChildWidth);
        }
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        String className = getClass().getName();
        result.append(className.substring(className.lastIndexOf('.')+1));
        result.append(": ");
        if (getElement() != null && ! isAnonymous()) {
            result.append("<");
            result.append(getElement().getNodeName());
            result.append("> ");
        }
        if (isAnonymous()) {
            result.append("(anonymous) ");
        }
        if (getPseudoElementOrClass() != null) {
            result.append(':');
            result.append(getPseudoElementOrClass());
            result.append(' ');
        }
        result.append('(');
        result.append(getStyle().getIdent(CSSName.DISPLAY).toString());
        result.append(") ");
        
        result.append('(');
        switch (getChildrenContentType()) {
        case CONTENT_BLOCK:
            result.append('B');
            break;
        case CONTENT_INLINE:
            result.append('I');
            break;
        case CONTENT_EMPTY:
            result.append('E');
            break;
        }
        result.append(") ");
        
        appendPositioningInfo(result);
        result.append("(" + getAbsX() + "," + getAbsY() + ")->(" + getWidth() + " x " + getHeight() + ")");
        return result.toString();
    }
    
    protected void appendPositioningInfo(StringBuffer result) {
        if (getStyle().isRelative()) {
            result.append("(relative) ");
        }
        if (getStyle().isFixed()) {
            result.append("(fixed) ");
        }
        if (getStyle().isAbsolute()) {
            result.append("(absolute) ");
        }
        if (getStyle().isFloated()) {
            result.append("(floated) ");
        }
    }  
    
    public String dump(LayoutContext c, String indent, int which) {
        StringBuffer result = new StringBuffer(indent);
        
        ensureChildren(c);
        
        result.append(this);
        
        if (getChildrenContentType() != CONTENT_EMPTY) {
            result.append('\n');
        }
        
        switch (getChildrenContentType()) {
        case CONTENT_BLOCK:
            dumpBoxes(c, indent, getChildren(), which, result);
            break;
        case CONTENT_INLINE:
            if (which == Box.DUMP_RENDER) {
                dumpBoxes(c, indent, getChildren(), which, result);
            } else {
                for (Iterator i = getInlineContent().iterator(); i.hasNext(); ) {
                    Styleable styleable = (Styleable)i.next();
                    if (styleable instanceof BlockBox) {
                        BlockBox b = (BlockBox)styleable;
                        result.append(b.dump(c, indent + "  ", which));
                        if (result.charAt(result.length()-1) == '\n') {
                            result.deleteCharAt(result.length()-1);
                        }
                    } else {
                        result.append(indent + "  ");
                        result.append(styleable.toString());
                    }
                    if (i.hasNext()) {
                        result.append('\n');
                    }
                }
            }
            break;
        }
        
        return result.toString();
    }

    public double getAbsTop() {
        return getAbsY();
    }

    public double getAbsBottom() {
        return getAbsY() + getHeight();
    }

    public void paintListMarker(RenderingContext c) {
        if (! getStyle().isVisible()) {
            return;
        }
        
        if (getStyle().isListItem()) {
            ListItemPainter.paint(c, this);
        }
    }

    public Rectangle getPaintingClipEdge(CssContext cssCtx) {
        Rectangle result = super.getPaintingClipEdge(cssCtx);
        
        // HACK Don't know how wide the list marker is (or even where it is)
        // so extend the bounding box all the way over to the left edge of
        // the canvas
        if (getStyle().isListItem()) {
            int delta = result.x;
            result.x = 0;
            result.width += delta;
        }
        
        return result;
    }
    
    public void paintInline(RenderingContext c) {
        if (! getStyle().isVisible()) {
            return;
        }
        
        getContainingLayer().paintAsLayer(c, this);
    }
    
    public boolean isInline() {
        Box parent = getParent();
        return parent instanceof LineBox || parent instanceof InlineLayoutBox;
    }
    
    public LineBox getLineBox() {
        if (! isInline()) {
            return null;
        } else {
            Box b = getParent();
            while (! (b instanceof LineBox)) {
                b = b.getParent();
            }
            return (LineBox)b;
        }
    }
    
    public void paintDebugOutline(RenderingContext c) {
        c.getOutputDevice().drawDebugOutline(c, this, Color.RED);
    }

    public MarkerData getMarkerData() {
        return _markerData;
    }

    public void setMarkerData(MarkerData markerData) {
        _markerData = markerData;
    }
    
    public void createMarkerData(LayoutContext c, StrutMetrics strutMetrics) {
        boolean imageMarker = false;
        
        MarkerData result = new MarkerData();
        result.setStructMetrics(strutMetrics);
        
        CalculatedStyle style = getStyle();
        IdentValue listStyle = style.getIdent(CSSName.LIST_STYLE_TYPE);
        
        String image = style.getStringProperty(CSSName.LIST_STYLE_IMAGE);
        if (! image.equals("none")) {
            result.setImageMarker(makeImageMarker(c, strutMetrics, image));
            imageMarker = result.getImageMarker() != null;
        } 
        
        if (listStyle != IdentValue.NONE && ! imageMarker) {
            if (listStyle == IdentValue.CIRCLE || listStyle == IdentValue.SQUARE ||
                    listStyle == IdentValue.DISC) {
                result.setGlyphMarker(makeGlyphMarker(strutMetrics));
            } else {
                result.setTextMarker(makeTextMarker(c, listStyle));
            }
        }
        
        setMarkerData(result);
    }
    
    private MarkerData.GlyphMarker makeGlyphMarker(StrutMetrics strutMetrics) {
        int diameter = (int)((strutMetrics.getAscent() + strutMetrics.getDescent()) / 3);
        
        MarkerData.GlyphMarker result = new MarkerData.GlyphMarker();
        result.setDiameter(diameter);
        result.setLayoutWidth(diameter * 3);
       
       return result;
    }
    
    
    private MarkerData.ImageMarker makeImageMarker(
            LayoutContext c, StrutMetrics structMetrics, String image) {
        FSImage img = null;
        if (! image.equals("none")) {
            img = c.getUac().getImageResource(image).getImage();
            if (img != null) {
                StrutMetrics strutMetrics = structMetrics;
                if (img.getHeight() > strutMetrics.getAscent()) {
                    img.scale(-1, (int)strutMetrics.getAscent());
                }
                MarkerData.ImageMarker result = new MarkerData.ImageMarker();
                result.setImage(img);
                result.setLayoutWidth(img.getWidth() * 2);
                return result;
            }
        }
        return null;
    }
    
    private MarkerData.TextMarker makeTextMarker(LayoutContext c, IdentValue listStyle) {
        String text;

        if (listStyle == IdentValue.LOWER_LATIN || listStyle == IdentValue.LOWER_ALPHA) {
            text = toLatin(getListCounter()).toLowerCase();
        } else if (listStyle == IdentValue.UPPER_LATIN || listStyle == IdentValue.UPPER_ALPHA) {
            text = toLatin(getListCounter()).toUpperCase();
        } else if (listStyle == IdentValue.LOWER_ROMAN) {
            text = toRoman(getListCounter()).toLowerCase();
        } else if (listStyle == IdentValue.UPPER_ROMAN) {
            text = toRoman(getListCounter()).toUpperCase();
        } else if (listStyle == IdentValue.DECIMAL_LEADING_ZERO) {
            text = (getListCounter() >= 10 ? "" : "0") + getListCounter();
        } else /* if (listStyle == IdentValue.DECIMAL) */ {
            text = Integer.toString(getListCounter());
        }
        
        text += ".  ";

        int w = c.getTextRenderer().getWidth(
                c.getFontContext(),
                getStyle().getFSFont(c), 
                text);
        
        MarkerData.TextMarker result = new MarkerData.TextMarker();
        result.setText(text);
        result.setLayoutWidth(w);
        
        return result;
    }

    private static String toLatin(int val) {
        if (val > 26) {
            int val1 = val % 26;
            int val2 = val / 26;
            return toLatin(val2) + toLatin(val1);
        }
        return ((char) (val + 64)) + "";
    }

    private static String toRoman(int val) {
        int[] ints = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] nums = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ints.length; i++) {
            int count = (int) (val / ints[i]);
            for (int j = 0; j < count; j++) {
                sb.append(nums[i]);
            }
            val -= ints[i] * count;
        }
        return sb.toString();
    }

    public int getListCounter() {
        return _listCounter;
    }

    public void setListCounter(int listCounter) {
        _listCounter = listCounter;
    }

    public PersistentBFC getPersistentBFC() {
        return _persistentBFC;
    }

    public void setPersistentBFC(PersistentBFC persistentBFC) {
        _persistentBFC = persistentBFC;
    }
    
    public Box getStaticEquivalent() {
        return _staticEquivalent;
    }

    public void setStaticEquivalent(Box staticEquivalent) {
        _staticEquivalent = staticEquivalent;
    }

    public boolean isReplaced() {
        return _replacedElement != null;
    }
    
    public void calcCanvasLocation() {
        if (isFloated()) {
            FloatManager manager = _floatedBoxData.getManager();
            Point offset = manager.getOffset(this);
            setAbsX(manager.getMaster().getAbsX() + getX() - offset.x);
            setAbsY(manager.getMaster().getAbsY() + getY() - offset.y);
        }
        
        LineBox lineBox = getLineBox();
        if (lineBox == null) {
            Box parent = getParent();
            if (parent != null) {
                setAbsX(parent.getAbsX() + parent.getTx() + getX());
                setAbsY(parent.getAbsY() + parent.getTy() + getY());
            } else if (isStyled() && (getStyle().isAbsolute() || getStyle().isFixed())) {
                Box cb = getContainingBlock();
                if (cb != null) {
                    setAbsX(cb.getAbsX() + getX());
                    setAbsY(cb.getAbsY() + getY());
                }
            }
        } else {
            setAbsX(lineBox.getAbsX() + getX());
            setAbsY(lineBox.getAbsY() + getY());
        }
        
        if (isReplaced()) {
            Point location = getReplacedElement().getLocation();
            if (location.x != getAbsX() || location.y != getAbsY()) {
                getReplacedElement().setLocation(getAbsX(), getAbsY());    
            }
        }
    }
    
    public void calcInitialFloatedCanvasLocation(LayoutContext c) {
        Point offset = c.getBlockFormattingContext().getOffset();
        FloatManager manager = c.getBlockFormattingContext().getFloatManager();
        setAbsX(manager.getMaster().getAbsX() + getX() - offset.x);
        setAbsY(manager.getMaster().getAbsY() + getY() - offset.y);
    }    
    
    public void calcChildLocations() {
        super.calcChildLocations();
        
        if (_persistentBFC != null) {
            _persistentBFC.getFloatManager().calcFloatLocations();
        }
    }

    public boolean isResetMargins() {
        return _resetMargins;
    }

    public void setResetMargins(boolean resetMargins) {
        _resetMargins = resetMargins;
    }

    public boolean isNeedPageClear() {
        return _needPageClear;
    }

    public void setNeedPageClear(boolean needPageClear) {
        _needPageClear = needPageClear;
    }
    
    
    private void alignToStaticEquivalent() {
        if (_staticEquivalent.getAbsY() != getAbsY()) {
                setY(_staticEquivalent.getAbsY() - getAbsY());
                setAbsY(_staticEquivalent.getAbsY());
        }
    }

    public void positionAbsolute(CssContext cssCtx, int direction) {
        CalculatedStyle style = getStyle();

        Rectangle boundingBox = null;
        
        int cbContentHeight = getContainingBlock().getContentAreaEdge(0, 0, cssCtx).height;

        if (getContainingBlock() instanceof BlockBox) {
            boundingBox = getContainingBlock().getPaddingEdge(0, 0, cssCtx);
        } else {
            boundingBox = getContainingBlock().getContentAreaEdge(0, 0, cssCtx);
        }

        if ((direction & POSITION_HORIZONTALLY) != 0) {
            setX(0);
            if (!style.isIdent(CSSName.LEFT, IdentValue.AUTO)) {
                setX((int)style.getFloatPropertyProportionalWidth(CSSName.LEFT, getContainingBlock().getContentWidth(), cssCtx));
            } else if (!style.isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
                setX(boundingBox.width -
                        (int)style.getFloatPropertyProportionalWidth(CSSName.RIGHT, getContainingBlock().getContentWidth(), cssCtx) - getWidth());
            }
            setX(getX() + boundingBox.x);
        }
        
        if ((direction & POSITION_VERTICALLY) != 0) {
            setY(0);
            if (!style.isIdent(CSSName.TOP, IdentValue.AUTO)) {
                setY((int)style.getFloatPropertyProportionalHeight(CSSName.TOP, cbContentHeight, cssCtx));
            } else if (!style.isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
                setY(boundingBox.height -
                        (int)style.getFloatPropertyProportionalWidth(CSSName.BOTTOM, cbContentHeight, cssCtx) - getHeight());
            }

            // Can't do this before now because our containing block
            // must be completed layed out
            int pinnedHeight = calcPinnedHeight(cssCtx);
            if (pinnedHeight != -1 && getCSSHeight(cssCtx) == -1) {
                setHeight(pinnedHeight);
                applyCSSMinMaxHeight(cssCtx);
            }
            
            setY(getY() + boundingBox.y);
        }
        
        calcCanvasLocation();
        
        if ((direction & POSITION_VERTICALLY) != 0 &&
                getStyle().isTopAuto() && getStyle().isBottomAuto()) {
            alignToStaticEquivalent();
        }
        
        calcChildLocations();
    }
    
    public void positionAbsoluteOnPage(LayoutContext c) {
        if (c.isPrint() &&
                (getStyle().isForcePageBreakBefore() || isNeedPageClear())) {
            moveToNextPage(c);
            calcCanvasLocation();
            calcChildLocations();
        }
    }

    public ReplacedElement getReplacedElement() {
        return _replacedElement;
    }

    public void setReplacedElement(ReplacedElement replacedElement) {
        _replacedElement = replacedElement;
    }
    
    public boolean containsLineBoxes() {
        return getChildCount() > 0 && getChild(0) instanceof LineBox;
    }
    
    public void reset(LayoutContext c) {
        super.reset(c);
        setTopMarginCalculated(false);
        setBottomMarginCalculated(false);
        setDimensionsCalculated(false);
        setMinMaxCalculated(false);
        setChildrenHeight(0);
        if (isReplaced()) {
            getReplacedElement().detach(c);
            setReplacedElement(null);
        }
        if (getChildrenContentType() == BlockBox.CONTENT_INLINE) {
            removeAllChildren();
        }
        
        if (isFloated()) {
            _floatedBoxData.getManager().removeFloat(this);
            _floatedBoxData.getDrawingLayer().removeFloat(this);
        }
    }
    
    private int calcPinnedContentWidth(CssContext c) {
        if (! getStyle().isIdent(CSSName.LEFT, IdentValue.AUTO) &&
                ! getStyle().isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
            int left = (int)getStyle().getFloatPropertyProportionalTo(
                    CSSName.LEFT, getContainingBlockWidth(), c);
            int right = (int)getStyle().getFloatPropertyProportionalTo(
                    CSSName.RIGHT, getContainingBlockWidth(), c);
            
            int result = getContainingBlock().getPaddingWidth(c) - 
                left - right - getLeftMBP() - getRightMBP();
            return result < 0 ? 0 : result;
        } 
        
        return -1;
    }
    
    private int calcPinnedHeight(CssContext c) {
        if (! getStyle().isIdent(CSSName.TOP, IdentValue.AUTO) &&
                ! getStyle().isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
            int top = (int)getStyle().getFloatPropertyProportionalTo(
                    CSSName.TOP, getContainingBlockWidth(), c);
            int bottom = (int)getStyle().getFloatPropertyProportionalTo(
                    CSSName.BOTTOM, getContainingBlockWidth(), c);
            
            int result = getContainingBlock().getPaddingEdge(0, 0, c).height - top - bottom;
            return result < 0 ? 0 : result;
        } 
        
        return -1;
    }
    
    private void resolveAutoMargins(
            LayoutContext c, int cssWidth, 
            RectPropertySet padding, BorderPropertySet border) {
        int withoutMargins = 
            (int)border.left() + (int)padding.left() +
            cssWidth +
            (int)padding.right() + (int)border.right();
        if (withoutMargins < getContainingBlockWidth()) {
            int available = getContainingBlockWidth() - withoutMargins;
            
            boolean autoLeft = getStyle().isIdent(CSSName.MARGIN_LEFT, IdentValue.AUTO);
            boolean autoRight = getStyle().isIdent(CSSName.MARGIN_RIGHT, IdentValue.AUTO);
            
            if (autoLeft && autoRight) {
                setMarginLeft(c, available / 2);
                setMarginRight(c, available / 2);
            } else if (autoLeft) {
                setMarginLeft(c, available);
            } else if (autoRight) {
                setMarginRight(c, available);
            }
        }
    }
    
    public void calcDimensions(LayoutContext c) {
        calcDimensions(c, getCSSWidth(c));
    }
    
    protected void calcDimensions(LayoutContext c, int cssWidth) {
        if (! isDimensionsCalculated()) {
            CalculatedStyle style = getStyle();
            
            RectPropertySet padding = getPadding(c);
            BorderPropertySet border = getBorder(c);
            
            if (cssWidth != -1 && !isAnonymous() &&
                    (getStyle().isIdent(CSSName.MARGIN_LEFT, IdentValue.AUTO) ||
                            getStyle().isIdent(CSSName.MARGIN_RIGHT, IdentValue.AUTO)) &&
                    getStyle().isNeedAutoMarginResolution()) {
                resolveAutoMargins(c, cssWidth, padding, border);
            }
            
            RectPropertySet margin = getMargin(c);
            
            // CLEAN: cast to int
            setLeftMBP((int) margin.left() + (int) border.left() + (int) padding.left());
            setRightMBP((int) padding.right() + (int) border.right() + (int) margin.right());
            setContentWidth((int) (getContainingBlockWidth() - getLeftMBP() - getRightMBP()));
            setHeight(0);
            
            if (! isAnonymous()) {
                int pinnedContentWidth = -1;
                
                if (cssWidth != -1) {
                    setContentWidth(cssWidth);
                } else if (getStyle().isAbsolute()) {
                    pinnedContentWidth = calcPinnedContentWidth(c);
                    if (pinnedContentWidth != -1) {
                        setContentWidth(pinnedContentWidth);
                    }
                }
                
                int cssHeight = getCSSHeight(c);
                if (cssHeight != -1) {
                    setHeight(cssHeight);
                }
                
                //check if replaced
                ReplacedElement re = getReplacedElement();
                if (re == null) {
                    re = c.getReplacedElementFactory().createReplacedElement(
                        c, this, c.getUac(), cssWidth, cssHeight);
                }
                if (re != null) {
                    setContentWidth(re.getIntrinsicWidth());
                    setHeight(re.getIntrinsicHeight());
                    setReplacedElement(re);
                } else if (cssWidth == -1 && pinnedContentWidth == -1 &&
                        (style.isInlineBlock() || style.isFloated() || 
                                style.isAbsolute() || style.isFixed())) { 
                    setNeedShrinkToFitCalculatation(true);
                }
                
                if (! isReplaced()) {
                    applyCSSMinMaxWidth(c);
                }
            }
            
            setDimensionsCalculated(true);
        }
    }
    
    private void calcClearance(LayoutContext c) {
        if (getStyle().isCleared()) {
            c.translate(0, -getY());
            c.getBlockFormattingContext().clear(c, this);
            c.translate(0, getY());
            calcCanvasLocation();
        }
    }
    
    public void layout(LayoutContext c) {
        layoutBlock(c);
    }
    
    private void addBoxID(LayoutContext c) {
        if (! isAnonymous()) {
            String id = c.getNamespaceHandler().getID(getElement());
            if (id != null) {
                c.addBoxId(id, this);
            }
        }
    }
    
    private void layoutBlock(LayoutContext c) {
        CalculatedStyle style = getStyle();
        
        if (style.isFixedBackground()) {
            c.getRootLayer().setFixedBackground(true);
        }
        
        boolean pushedLayer = false;
        if (isRoot() || style.requiresLayer()) {
            pushedLayer = true;
            if (getLayer() == null) {
                c.pushLayer(this);
                if (c.isPrint() && isRoot()) {
                    c.getLayer().addPage(c);
                }
            } else {
                c.pushLayer(getLayer());
            }
        }

        if (isRoot() || getStyle().establishesBFC()) {
            BlockFormattingContext bfc = new BlockFormattingContext(this, c);
            c.pushBFC(bfc);
        }
        
        addBoxID(c);
        
        calcDimensions(c);
        collapseMargins(c);
        
        calcClearance(c);
        calcShrinkToFitWidthIfNeeded(c);
        
        BorderPropertySet border = getBorder(c);
        RectPropertySet margin = getMargin(c);
        RectPropertySet padding = getPadding(c);
        
        if (isResetMargins()) {
            resetCollapsedMargin(c);
            setResetMargins(false);
        }
        
        // save height incase fixed height
        int originalHeight = getHeight();

        if (! isReplaced()) {
            setHeight(0);
        }
        
        boolean didSetMarkerData = false;
        if (getStyle().isListItem()) {
            StrutMetrics strutMetrics = InlineBoxing.createDefaultStrutMetrics(c, this);
            createMarkerData(c, strutMetrics);
            c.setCurrentMarkerData(getMarkerData());
            didSetMarkerData = true;
        }        

        // do children's layout
        int tx = (int) margin.left() + (int) border.left() + (int) padding.left();
        int ty = (int) margin.top() + (int) border.top() + (int) padding.top();
        setTx(tx);
        setTy(ty);        
        c.translate(getTx(), getTy());
        if (! isReplaced())
            layoutChildren(c);
        else {
            setState(Box.DONE);
        }
        c.translate(-getTx(), -getTy());

        setChildrenHeight(getHeight());
        
        if (! isReplaced()) {
            if (! isAutoHeight()) {
                setHeight(originalHeight);
            }
            
            applyCSSMinMaxHeight(c);
        }

        if (isRoot() || getStyle().establishesBFC()) {
            if (getStyle().isAutoHeight()) {
                int delta = 
                    c.getBlockFormattingContext().getFloatManager().getClearDelta(
                            c, getTy() + getHeight());
                if (delta > 0) {
                    setHeight(getHeight() + delta);
                    setChildrenHeight(getChildrenHeight() + delta);
                }
            }
            c.popBFC();
        }
        
        if (didSetMarkerData) {
            c.setCurrentMarkerData(null);
        }

        calcLayoutHeight(c, border, margin, padding);
        
        if (pushedLayer) {
            c.popLayer();
        }
    }

    protected void calcLayoutHeight(
            LayoutContext c, BorderPropertySet border, 
            RectPropertySet margin, RectPropertySet padding) {
        setHeight(getHeight() + ((int) margin.top() + (int) border.top() + (int) padding.top() + 
            (int) padding.bottom() + (int) border.bottom() + (int) margin.bottom()));
        setChildrenHeight(getChildrenHeight() + ((int) margin.top() + (int) border.top() + (int) padding.top() + 
                (int) padding.bottom() + (int) border.bottom() + (int) margin.bottom()));        
    }
    

    private void calcShrinkToFitWidthIfNeeded(LayoutContext c) {
        if (isNeedShrinkToFitCalculatation()) {
            setContentWidth(calcShrinkToFitWidth(c) - getLeftMBP() - getRightMBP());
            applyCSSMinMaxWidth(c);
            setNeedShrinkToFitCalculatation(false);
        }
    }

    private void applyCSSMinMaxWidth(CssContext c) {
        if (! getStyle().isMaxWidthNone()) {
            int cssMaxWidth = getCSSMaxWidth(c);
            if (getContentWidth() > cssMaxWidth) {
                setContentWidth(cssMaxWidth);
            }
        }
        int cssMinWidth = getCSSMinWidth(c);
        if (cssMinWidth > 0 && getContentWidth() < cssMinWidth) {
            setContentWidth(cssMinWidth);
        }
    }
    
    private void applyCSSMinMaxHeight(CssContext c) {
        if (! getStyle().isMaxHeightNone()) {
            int cssMaxHeight = getCSSMaxHeight(c);
            if (getHeight() > cssMaxHeight) {
                setHeight(cssMaxHeight);
            }
        }
        int cssMinHeight = getCSSMinHeight(c);
        if (cssMinHeight > 0 && getHeight() < cssMinHeight) {
            setHeight(cssMinHeight);
        }
    }
    
    public void ensureChildren(LayoutContext c) {
        if (getChildrenContentType() == CONTENT_UNKNOWN) {
            BoxBuilder.createChildren(c, this);
        }
    }
    
    public void layoutChildren(LayoutContext c) {
        setState(Box.CHILDREN_FLUX);
        ensureChildren(c);
        
        if (getFirstLetterStyle() != null) {
            c.getFirstLettersTracker().addStyle(getFirstLetterStyle());
        }
        if (getFirstLineStyle() != null) {
            c.getFirstLinesTracker().addStyle(getFirstLineStyle());
        }
        
        switch (getChildrenContentType()) {
            case CONTENT_INLINE:
                InlineBoxing.layoutContent(c, this);
                break;
            case CONTENT_BLOCK:
                BlockBoxing.layoutContent(c, this);
                break;
        }
        
        if (getFirstLetterStyle() != null) {
            c.getFirstLettersTracker().removeLast();
        }
        if (getFirstLineStyle() != null) {
            c.getFirstLinesTracker().removeLast();
        }
        
        setState(Box.DONE);
    }

    public int getChildrenContentType() {
        return _childrenContentType;
    }

    public void setChildrenContentType(int contentType) {
        _childrenContentType = contentType;
    }

    public List getInlineContent() {
        return _inlineContent;
    }

    public void setInlineContent(List inlineContent) {
        _inlineContent = inlineContent;
        if (inlineContent != null) {
            for (Iterator i = inlineContent.iterator(); i.hasNext(); ) {
                Styleable child = (Styleable)i.next();
                if (child instanceof Box) {
                    ((Box)child).setContainingBlock(this);
                }
            }
        }
    }
    
    protected boolean isSkipWhenCollapsingMargins() {
        return false;
    }
    
    protected boolean isMayCollapseMarginsWithChildren() {
        return (! isRoot()) && getStyle().isMayCollapseMarginsWithChildren();
    }
    
    // This will require a rethink if we ever truly layout incrementally
    // Should only ever collapse top margin and pick up collapsable
    // bottom margins by looking back up the tree.
    private void collapseMargins(LayoutContext c) {
        if (! isTopMarginCalculated() || ! isBottomMarginCalculated()) {
            RectPropertySet margin = getMargin(c);
            
            if (! isTopMarginCalculated() && ! isBottomMarginCalculated() && isVerticalMarginsAdjoin(c)) {
                MarginCollapseResult collapsedMargin = 
                    _pendingCollapseCalculation != null ?
                            _pendingCollapseCalculation : new MarginCollapseResult();
                collapseEmptySubtreeMargins(c, collapsedMargin);
                setCollapsedBottomMargin(c, margin, collapsedMargin);
            } else {
                if (! isTopMarginCalculated()) {
                    MarginCollapseResult collapsedMargin = 
                        _pendingCollapseCalculation != null ?
                                _pendingCollapseCalculation : new MarginCollapseResult();
                    
                    collapseTopMargin(c, true, collapsedMargin);
                    if ((int)margin.top() != collapsedMargin.getMargin()) {
                        setMarginTop(c, collapsedMargin.getMargin());
                    }
                }
                
                if (! isBottomMarginCalculated()) {
                    MarginCollapseResult collapsedMargin = new MarginCollapseResult();
                    collapseBottomMargin(c, true, collapsedMargin);
                    
                    setCollapsedBottomMargin(c, margin, collapsedMargin);
                }
            }
        }
    }

    private void setCollapsedBottomMargin(LayoutContext c, RectPropertySet margin, MarginCollapseResult collapsedMargin) {
        BlockBox next = null;
        if (! isInline()) {
            next = getNextCollapsableSibling(collapsedMargin);
        }
        if (! (next == null || next instanceof AnonymousBlockBox) && 
                collapsedMargin.hasMargin()) {
            next._pendingCollapseCalculation = collapsedMargin;
            setMarginBottom(c, 0);
        } else if ((int)margin.bottom() != collapsedMargin.getMargin()) {
            setMarginBottom(c, collapsedMargin.getMargin());
        }
    }

    private BlockBox getNextCollapsableSibling(MarginCollapseResult collapsedMargin) {
        BlockBox next = (BlockBox)getNextSibling();
        while (next != null) {
            if (next instanceof AnonymousBlockBox) {
                ((AnonymousBlockBox)next).provideSiblingMarginToFloats(
                        collapsedMargin.getMargin());
            }
            if (! next.isSkipWhenCollapsingMargins()) {
                break;
            } else {
                next = (BlockBox)next.getNextSibling();
            }
        }
        return next;
    }
    
    private void collapseTopMargin(
            LayoutContext c, boolean calculationRoot, MarginCollapseResult result) {
        if (! isTopMarginCalculated()) {
            if (! isSkipWhenCollapsingMargins()) {
                calcDimensions(c);
                RectPropertySet margin = getMargin(c);
                result.update((int)margin.top());
                
                if (! calculationRoot && (int)margin.top() != 0) {
                    setMarginTop(c, 0);
                }
                
                if (isMayCollapseMarginsWithChildren() && isNoTopPaddingOrBorder(c)) {
                    ensureChildren(c);
                    if (getChildrenContentType() == CONTENT_BLOCK) {
                        for (Iterator i = getChildIterator(); i.hasNext(); ) {
                            BlockBox child = (BlockBox)i.next();
                            child.collapseTopMargin(c, false, result);
                            
                            if (child.isSkipWhenCollapsingMargins()) {
                                continue;
                            }
                            
                            break;
                        }
                    }
                }
            }
            
            setTopMarginCalculated(true);
        }
    }
    
    private void collapseBottomMargin(
            LayoutContext c, boolean calculationRoot, MarginCollapseResult result) {
        if (! isBottomMarginCalculated()) {
            if (! isSkipWhenCollapsingMargins()) {
                calcDimensions(c);
                RectPropertySet margin = getMargin(c);
                result.update((int)margin.bottom());
                
                if (! calculationRoot && (int)margin.bottom() != 0) {
                    setMarginBottom(c, 0);
                }
                
                if (isMayCollapseMarginsWithChildren() && 
                        ! getStyle().isTable() && isNoBottomPaddingOrBorder(c)) {
                    ensureChildren(c);
                    if (getChildrenContentType() == CONTENT_BLOCK) {
                        for (int i = getChildCount() - 1; i >= 0; i--) {
                            BlockBox child = (BlockBox)getChild(i);
                            
                            if (child.isSkipWhenCollapsingMargins()) {
                                continue;
                            }
                            
                            child.collapseBottomMargin(c, false, result);
                            
                            break;
                        }
                    }
                }                  
            }
            
            setBottomMarginCalculated(true);
        }
    }
    
    private boolean isNoTopPaddingOrBorder(LayoutContext c) {
        RectPropertySet padding = getPadding(c);
        BorderPropertySet border = getBorder(c);
        
        return (int)padding.top() == 0 && (int)border.top() == 0;
    }
    
    private boolean isNoBottomPaddingOrBorder(LayoutContext c) {
        RectPropertySet padding = getPadding(c);
        BorderPropertySet border = getBorder(c);
        
        return (int)padding.bottom() == 0 && (int)border.bottom() == 0;
    }
    
    private void collapseEmptySubtreeMargins(LayoutContext c, MarginCollapseResult result) {
        RectPropertySet margin = getMargin(c);
        result.update((int)margin.top());
        result.update((int)margin.bottom());
        
        setMarginTop(c, 0);
        setTopMarginCalculated(true);
        setMarginBottom(c, 0);
        setBottomMarginCalculated(true);
        
        ensureChildren(c);
        if (getChildrenContentType() == CONTENT_BLOCK) {
            for (Iterator i = getChildIterator(); i.hasNext(); ) {
                BlockBox child = (BlockBox)i.next();
                child.collapseEmptySubtreeMargins(c, result);
            }
        }
    }
    
    private boolean isVerticalMarginsAdjoin(LayoutContext c) {
        CalculatedStyle style = getStyle();
        
        BorderPropertySet borderWidth = style.getBorder(c);
        RectPropertySet padding = getPadding(c);
        
        boolean bordersOrPadding = 
            (int)borderWidth.top() != 0 || (int)borderWidth.bottom() != 0 ||
            (int)padding.top() != 0 || (int)padding.bottom() != 0;
        
        if (bordersOrPadding) {
            return false;
        }
        
        ensureChildren(c);
        if (getChildrenContentType() == CONTENT_INLINE) {
            return false;
        } else if (getChildrenContentType() == CONTENT_BLOCK) {
            for (Iterator i = getChildIterator(); i.hasNext(); ) {
                BlockBox child = (BlockBox)i.next();
                if (child.isSkipWhenCollapsingMargins() || ! child.isVerticalMarginsAdjoin(c)) {
                    return false;
                }
            }
        }
        
        return style.asFloat(CSSName.MIN_HEIGHT) == 0 && 
                (isAutoHeight() || style.asFloat(CSSName.HEIGHT) == 0);
    }
    
    public boolean isTopMarginCalculated() {
        return _topMarginCalculated;
    }

    public void setTopMarginCalculated(boolean topMarginCalculated) {
        _topMarginCalculated = topMarginCalculated;
    }

    public boolean isBottomMarginCalculated() {
        return _bottomMarginCalculated;
    }

    public void setBottomMarginCalculated(boolean bottomMarginCalculated) {
        _bottomMarginCalculated = bottomMarginCalculated;
    }  
    
    protected int getCSSWidth(CssContext c) {
        if (! isAnonymous()) {
            if (! getStyle().isAutoWidth()) {
                int result = (int) getStyle().getFloatPropertyProportionalWidth(
                        CSSName.WIDTH, getContainingBlock().getContentWidth(), c);
                return result >= 0 ? result : -1;
            }
        }
        
        return -1;
    }
    
    private int getCSSHeight(CssContext c) {
        if (! isAnonymous()) {
            if (! isAutoHeight()) {
                if (! getContainingBlock().getStyle().isAutoHeight()) {
                    return (int)getStyle().getFloatPropertyProportionalHeight(
                            CSSName.HEIGHT, 
                            getContainingBlock().getStyle().getFloatPropertyProportionalHeight(CSSName.HEIGHT, 0, c),
                            c);
                } else {
                    return (int)getStyle().getFloatPropertyProportionalHeight(CSSName.HEIGHT, 0, c);
                }
            }
        }
        
        return -1;
    }
    
    public boolean isAutoHeight() {
        if (getStyle().isAutoHeight()) {
            return true;
        } else if (getStyle().hasAbsoluteUnit(CSSName.HEIGHT)) {
            return false;
        } else {
            return ! (getContainingBlock().isStyled() && 
                    ! getContainingBlock().getStyle().isAutoHeight() &&
                    getContainingBlock().getStyle().hasAbsoluteUnit(CSSName.HEIGHT));
        }
    }
    
    private int getCSSMinWidth(CssContext c) {
        return getStyle().getMinWidth(c, getContainingBlockWidth());
    }
    
    private int getCSSMaxWidth(CssContext c) {
        return getStyle().getMaxWidth(c, getContainingBlockWidth());
    }
    
    private int getCSSMinHeight(CssContext c) {
        return getStyle().getMinHeight(c, getContainingBlockCSSHeight(c));
    }
    
    private int getCSSMaxHeight(CssContext c) {
        return getStyle().getMaxHeight(c, getContainingBlockCSSHeight(c));
    }
    
    // Use only when the height of the containing block is required for
    // resolving percentage values.  Does not represent the actual (resolved) height
    // of the containing block.
    private int getContainingBlockCSSHeight(CssContext c) {
        if (! getContainingBlock().isStyled() || 
                    getContainingBlock().getStyle().isAutoHeight()) {
            return 0;
        } else {
            if (getContainingBlock().getStyle().hasAbsoluteUnit(CSSName.HEIGHT)) {
                return (int)getContainingBlock().getStyle().getFloatPropertyProportionalTo(
                        CSSName.HEIGHT, 0, c);
            } else {
                return 0;
            }
        }
    }
    
    private int calcShrinkToFitWidth(LayoutContext c) {
        calcMinMaxWidth(c);
        
        return Math.min(Math.max(getMinWidth(), getAvailableWidth(c)), getMaxWidth());
    }
    
    protected int getAvailableWidth(LayoutContext c) {
        if (! getStyle().isAbsolute()) {
            return getContainingBlockWidth();
        } else {
            int left = 0;
            int right = 0;
            if (! getStyle().isIdent(CSSName.LEFT, IdentValue.AUTO)) {
                left =
                    (int)getStyle().getFloatPropertyProportionalTo(CSSName.LEFT, 
                            getContainingBlock().getContentWidth(), c);
            }
            
            if (! getStyle().isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
                right =
                    (int)getStyle().getFloatPropertyProportionalTo(CSSName.RIGHT, 
                            getContainingBlock().getContentWidth(), c);
            }
            
            return getContainingBlock().getPaddingWidth(c) - left - right;
        }
    }
    
    protected boolean isFixedWidthAdvisoryOnly() {
        return false;
    }
    
    public void calcMinMaxWidth(LayoutContext c) {
        if (! isMinMaxCalculated()) {
            RectPropertySet margin = getMargin(c);
            BorderPropertySet border = getBorder(c);
            RectPropertySet padding = getPadding(c);
            
            int width = getCSSWidth(c);
            
            if (width == -1) {
                if (isReplaced()) {
                    width = getReplacedElement().getIntrinsicWidth();
                } else {
                    int height = getCSSHeight(c);
                    ReplacedElement re = c.getReplacedElementFactory().createReplacedElement(
                            c, this, c.getUac(), width, height);
                    if (re != null) {
                        setReplacedElement(re);
                        width = getReplacedElement().getIntrinsicWidth();
                    }
                }
            }
            
            if (isReplaced() || (width != -1 && ! isFixedWidthAdvisoryOnly())) {
                _minWidth = _maxWidth =
                    (int)margin.left() + (int)border.left() + (int)padding.left() +
                    width +
                    (int)margin.right() + (int)border.right() + (int)padding.right();
            } else {
                _minWidth = _maxWidth =
                    (int)margin.left() + (int)border.left() + (int)padding.left() +
                    (int)margin.right() + (int)border.right() + (int)padding.right();
                
                int minimumMaxWidth = _maxWidth;
                if (width != -1) {
                    minimumMaxWidth += width;
                }
                
                ensureChildren(c);
                
                if (getChildrenContentType() == CONTENT_BLOCK || 
                        getChildrenContentType() == CONTENT_INLINE) {
                    switch (getChildrenContentType()) {
                        case CONTENT_BLOCK:
                            calcMinMaxWidthBlockChildren(c);
                            break;
                        case CONTENT_INLINE:
                            calcMinMaxWidthInlineChildren(c);
                            break;
                    }
                }
                
                if (minimumMaxWidth > _maxWidth) {
                    _maxWidth = minimumMaxWidth;
                }
            }
            
            if (! isReplaced()) {
                calcMinMaxCSSMinMaxWidth(c, margin, border, padding);
            }
            
            setMinMaxCalculated(true);
        }
    }

    private void calcMinMaxCSSMinMaxWidth(
            LayoutContext c, RectPropertySet margin, BorderPropertySet border, 
            RectPropertySet padding) {
        int cssMinWidth = getCSSMinWidth(c);
        if (cssMinWidth > 0) {
            cssMinWidth += 
                (int)margin.left() + (int)border.left() + (int)padding.left() +
                (int)margin.right() + (int)border.right() + (int)padding.right();
            if (_minWidth < cssMinWidth) {
                _minWidth = cssMinWidth;
            }
        }
        if (! getStyle().isMaxWidthNone()) {
            int cssMaxWidth = getCSSMaxWidth(c);
            cssMaxWidth +=
                (int)margin.left() + (int)border.left() + (int)padding.left() +
                (int)margin.right() + (int)border.right() + (int)padding.right();
            if (_maxWidth > cssMaxWidth) {
                if (cssMaxWidth > _minWidth) {
                    _maxWidth = cssMaxWidth;
                } else {
                    _maxWidth = _minWidth;
                }
            }
        }
    }
    
    private void calcMinMaxWidthBlockChildren(LayoutContext c) {
        int childMinWidth = 0;
        int childMaxWidth = 0;
        
        for (Iterator i = getChildIterator(); i.hasNext(); ) {
            BlockBox child = (BlockBox)i.next();            
            child.calcMinMaxWidth(c);
            if (child.getMinWidth() > childMinWidth) {
                childMinWidth = child.getMinWidth();
            }
            if (child.getMaxWidth() > childMaxWidth) {
                childMaxWidth = child.getMaxWidth();
            }
        }
        
        _minWidth += childMinWidth;
        _maxWidth += childMaxWidth;
    }
    
    private void calcMinMaxWidthInlineChildren(LayoutContext c) {
        int textIndent = (int)getStyle().getFloatPropertyProportionalWidth(
                CSSName.TEXT_INDENT, getContentWidth(), c);
                
        int childMinWidth = 0;
        int childMaxWidth = 0;
        int lineWidth = 0;
        
        InlineBox trimmableIB = null;
        
        for (Iterator i = _inlineContent.iterator(); i.hasNext(); ) {
            Styleable child = (Styleable)i.next();
            
            if (child.getStyle().isAbsolute() || child.getStyle().isFixed()) {
                continue;
            }
            
            if (child.getStyle().isFloated() || child.getStyle().isInlineBlock() ||
                    child.getStyle().isInlineTable()) {
                if (child.getStyle().isFloated() && child.getStyle().isCleared()) {
                    if (trimmableIB != null) {
                        lineWidth -= trimmableIB.getTrailingSpaceWidth(c);
                    }
                    if (lineWidth > childMaxWidth) {
                        childMaxWidth = lineWidth;
                    }
                    lineWidth = 0;
                }
                trimmableIB = null;
                BlockBox block = (BlockBox)child;
                block.calcMinMaxWidth(c);
                lineWidth += block.getMaxWidth();
                if (block.getMinWidth() > childMinWidth) {
                    childMinWidth = block.getMinWidth();
                }
            } else { /* child.getStyle().isInline() */
                InlineBox iB = (InlineBox)child;
                IdentValue whitespace = iB.getStyle().getWhitespace();
                
                iB.calcMinMaxWidth(c, getContentWidth(), lineWidth == 0);
                
                if (whitespace == IdentValue.NOWRAP) {
                    lineWidth += textIndent + iB.getMaxWidth();
                    if (iB.getMinWidth() > childMinWidth) {
                        childMinWidth = iB.getMinWidth();
                    }
                    trimmableIB = iB;
                } else if (whitespace == IdentValue.PRE) {
                    if (trimmableIB != null) {
                        lineWidth -= trimmableIB.getTrailingSpaceWidth(c);
                    }
                    trimmableIB = null;
                    if (lineWidth > childMaxWidth) {
                        childMaxWidth = lineWidth;
                    }
                    lineWidth = textIndent + iB.getFirstLineWidth();
                    if (lineWidth > childMinWidth) {
                        childMinWidth = lineWidth;
                    }
                    lineWidth = iB.getMaxWidth();
                    if (lineWidth > childMinWidth) {
                        childMinWidth = lineWidth;
                    }
                    if (childMinWidth > childMaxWidth) {
                        childMaxWidth = childMinWidth;
                    }
                    lineWidth = 0;
                } else if (whitespace == IdentValue.PRE_WRAP || whitespace == IdentValue.PRE_LINE) {
                    lineWidth += textIndent + iB.getFirstLineWidth();
                    if (trimmableIB != null) {
                        lineWidth -= trimmableIB.getTrailingSpaceWidth(c);
                    }
                    if (lineWidth > childMaxWidth) {
                        childMaxWidth = lineWidth;
                    }
                    
                    if (iB.getMaxWidth() > childMaxWidth) {
                        childMaxWidth = iB.getMaxWidth();
                    }
                    if (iB.getMinWidth() > childMinWidth) {
                        childMinWidth = iB.getMinWidth();
                    }
                    if (whitespace == IdentValue.PRE_LINE) {
                        trimmableIB = iB;
                    } else {
                        trimmableIB = null;
                    }
                    lineWidth = 0;
                } else /* if (whitespace == IdentValue.NORMAL) */ {
                    lineWidth += textIndent + iB.getMaxWidth();
                    if (iB.getMinWidth() > childMinWidth) {
                        childMinWidth = textIndent + iB.getMinWidth();
                    }
                    trimmableIB = iB;
                }
                
                if (textIndent > 0) {
                    textIndent = 0;
                }                
            }
        }
        
        if (trimmableIB != null) {
            lineWidth -= trimmableIB.getTrailingSpaceWidth(c);
        }
        if (lineWidth > childMaxWidth) {
            childMaxWidth = lineWidth;
        }
        
        _minWidth += childMinWidth;
        _maxWidth += childMaxWidth;
    }
    
    public int getMaxWidth() {
        return _maxWidth;
    }

    protected void setMaxWidth(int maxWidth) {
        _maxWidth = maxWidth;
    }

    public int getMinWidth() {
        return _minWidth;
    }

    protected void setMinWidth(int minWidth) {
        _minWidth = minWidth;
    }
    
    public void styleText(LayoutContext c) {
        styleText(c, getStyle());
    }
    
    // FIXME Should be expanded into generic restyle facility
    public void styleText(LayoutContext c, CalculatedStyle style) {
        if (getChildrenContentType() == CONTENT_INLINE) {
            LinkedList styles = new LinkedList();
            styles.add(style);
            for (Iterator i = _inlineContent.iterator(); i.hasNext(); ) {
                Styleable child = (Styleable)i.next();
                if (child instanceof InlineBox) {
                    InlineBox iB = (InlineBox)child;
                    
                    if (iB.isStartsHere()) {
                        CascadedStyle cs = null;
                        if (iB.getElement() != null) {
                            if (iB.getPseudoElementOrClass() == null) {
                                cs = c.getCss().getCascadedStyle(iB.getElement(), false);
                            } else {
                                cs = c.getCss().getPseudoElementStyle(
                                        iB.getElement(), iB.getPseudoElementOrClass());
                            }
                            styles.add(((CalculatedStyle)styles.getLast()).deriveStyle(cs));
                        } else {
                            styles.add(style.createAnonymousStyle(IdentValue.INLINE));
                        }
                    }
                    
                    iB.setStyle(((CalculatedStyle)styles.getLast()));
                    
                    if (iB.isEndsHere()) {
                        styles.removeLast();
                    }
                }
            }
        }
    }
    
    protected void calcChildPaintingInfo(
            final CssContext c, final PaintingInfo result, final boolean useCache) {
        if (getPersistentBFC() != null) {
            ((BlockBox)this).getPersistentBFC().getFloatManager().performFloatOperation(
                    new FloatManager.FloatOperation() {
                        public void operate(Box floater) {
                            PaintingInfo info = floater.calcPaintingInfo(c, useCache);
                            moveIfGreater(
                                    result.getOuterMarginCorner(), 
                                    info.getOuterMarginCorner());
                        }
                    });
        }
        super.calcChildPaintingInfo(c, result, useCache);
    }  
    
    public CascadedStyle getFirstLetterStyle() {
        return _firstLetterStyle;
    }

    public void setFirstLetterStyle(CascadedStyle firstLetterStyle) {
        _firstLetterStyle = firstLetterStyle;
    }

    public CascadedStyle getFirstLineStyle() {
        return _firstLineStyle;
    }

    public void setFirstLineStyle(CascadedStyle firstLineStyle) {
        _firstLineStyle = firstLineStyle;
    }    

    protected boolean isMinMaxCalculated() {
        return _minMaxCalculated;
    }

    protected void setMinMaxCalculated(boolean minMaxCalculated) {
        _minMaxCalculated = minMaxCalculated;
    }

    protected void setDimensionsCalculated(boolean dimensionsCalculated) {
        _dimensionsCalculated = dimensionsCalculated;
    }

    private boolean isDimensionsCalculated() {
        return _dimensionsCalculated;
    }

    protected void setNeedShrinkToFitCalculatation(boolean needShrinkToFitCalculatation) {
        _needShrinkToFitCalculatation = needShrinkToFitCalculatation;
    }

    private boolean isNeedShrinkToFitCalculatation() {
        return _needShrinkToFitCalculatation;
    }
    
    public void initStaticPos(LayoutContext c, BlockBox parent, int childOffset) {
        setX(0);
        setY(childOffset);
    }
    
    public int calcBaseline(LayoutContext c) {
        for (int i = 0; i < getChildCount(); i++) {
            Box b = (Box)getChild(i);
            if (b instanceof LineBox) {
                return b.getAbsY() + ((LineBox)b).getBaseline();
            } else {
                if (b instanceof TableRowBox) {
                    return b.getAbsY() + ((TableRowBox)b).getBaseline();
                } else {
                    int result = ((BlockBox)b).calcBaseline(c);
                    if (result != NO_BASELINE) {
                        return result;
                    }
                }
            }
        }
        
        return NO_BASELINE;
    }
    
    public boolean isFloated() {
        return _floatedBoxData != null;
    }

    public FloatedBoxData getFloatedBoxData() {
        return _floatedBoxData;
    }

    public void setFloatedBoxData(FloatedBoxData floatedBoxData) {
        _floatedBoxData = floatedBoxData;
    }
    
    private static class MarginCollapseResult {
        private int maxPositive;
        private int maxNegative;
        
        public void update(int value) {
            if (value < 0 && value < maxNegative) {
                maxNegative = value;
            }
            
            if (value > 0 && value > maxPositive) {
                maxPositive = value;
            }
        }
        
        public int getMargin() {
            return maxPositive + maxNegative;
        }
        
        public boolean hasMargin() {
            return maxPositive != 0 || maxNegative != 0;
        }
    }

    public int getChildrenHeight() {
        return _childrenHeight;
    }

    protected void setChildrenHeight(int childrenHeight) {
        _childrenHeight = childrenHeight;
    }    
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.69  2007/02/24 01:57:30  peterbrant
 * toString() changes
 *
 * Revision 1.68  2007/02/23 15:50:37  peterbrant
 * Fix incorrect absolute box positioning with print medium
 *
 * Revision 1.67  2007/02/22 18:21:19  peterbrant
 * Add support for overflow: visible/hidden
 *
 * Revision 1.66  2007/02/22 16:10:54  peterbrant
 * Remove unused API
 *
 * Revision 1.65  2007/02/22 15:30:42  peterbrant
 * Internal links should be able to target block boxes too (plus other minor cleanup)
 *
 * Revision 1.64  2007/02/21 23:49:41  peterbrant
 * Can't calculate clearance until margins have been collapsed / Clearance must be calculated relative to the box's border edge, not margin edge
 *
 * Revision 1.63  2007/02/21 21:47:26  peterbrant
 * Implement (limited) support for collapsing through blocks with adjoining top and bottom margins / <body> should collapse with children
 *
 * Revision 1.62  2007/02/21 17:17:04  peterbrant
 * Calculate position of next child and block height independently.  They may not
 * move in lockstep in the face of negative vertical margins.
 *
 * Revision 1.61  2007/02/20 23:46:06  peterbrant
 * Include pseudo element in toString()
 *
 * Revision 1.60  2007/02/11 23:10:59  peterbrant
 * Make sure bounds information is calculated for fixed layers
 *
 * Revision 1.59  2007/02/07 16:33:25  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.58  2006/10/04 23:52:57  peterbrant
 * Implement support for margin: auto (centering blocks in their containing block)
 *
 * Revision 1.57  2006/10/04 21:35:49  peterbrant
 * Allow dimensions of absolutely positioned content to be specified with all four corners, not just one of left/right, top/bottom
 *
 * Revision 1.56  2006/10/04 19:49:08  peterbrant
 * Improve calculation of available width when calculating shrink-to-fit width
 *
 * Revision 1.55  2006/09/08 15:41:58  peterbrant
 * Calculate containing block width accurately when collapsing margins / Provide collapsed bottom
 * margin to floats / Revive :first-line and :first-letter / Minor simplication in InlineBoxing
 * (get rid of now-superfluous InlineBoxInfo)
 *
 * Revision 1.54  2006/09/07 16:24:50  peterbrant
 * Need to calculate (preliminary) box dimensions when collapsing margins
 *
 * Revision 1.53  2006/09/06 22:42:30  peterbrant
 * Implement min/max-height (non-replaced content only)
 *
 * Revision 1.52  2006/09/06 22:21:43  peterbrant
 * Fixes to shrink-to-fit implementation / Implement min/max-width (non-replaced content) only
 *
 * Revision 1.51  2006/09/05 23:03:44  peterbrant
 * Initial draft of shrink-to-fit support
 *
 * Revision 1.50  2006/09/01 23:49:38  peterbrant
 * Implement basic margin collapsing / Various refactorings in preparation for shrink-to-fit / Add hack to treat auto margins as zero
 *
 * Revision 1.49  2006/08/30 18:25:41  peterbrant
 * Further refactoring / Bug fix for problem reported by Mike Curtis
 *
 * Revision 1.48  2006/08/29 17:29:12  peterbrant
 * Make Style object a thing of the past
 *
 * Revision 1.47  2006/08/27 01:16:20  peterbrant
 * decimal-leading-zero patch from Thomas Palmer
 *
 * Revision 1.46  2006/08/27 01:15:00  peterbrant
 * Revert makeTextMarker() change to commit with proper attribution
 *
 * Revision 1.45  2006/08/27 00:36:47  peterbrant
 * Initial commit of (initial) R7 work
 *
 * Revision 1.44  2006/03/01 00:45:03  peterbrant
 * Provide LayoutContext when calling detach() and friends
 *
 * Revision 1.43  2006/02/22 02:20:19  peterbrant
 * Links and hover work again
 *
 * Revision 1.42  2006/02/21 20:55:45  peterbrant
 * Handle image marker failover and list-style-type: none
 *
 * Revision 1.41  2006/02/21 20:41:15  peterbrant
 * Default to decimal for text list markers
 *
 * Revision 1.40  2006/02/09 19:12:25  peterbrant
 * Fix bad interaction between page-break-inside: avoid and top: auto/bottom: auto for absolute blocks
 *
 * Revision 1.39  2006/02/02 02:47:35  peterbrant
 * Support non-AWT images
 *
 * Revision 1.38  2006/02/01 01:30:13  peterbrant
 * Initial commit of PDF work
 *
 * Revision 1.37  2006/01/27 01:15:35  peterbrant
 * Start on better support for different output devices
 *
 * Revision 1.36  2006/01/10 19:56:01  peterbrant
 * Fix inappropriate box resizing when width: auto
 *
 * Revision 1.35  2006/01/03 23:52:40  peterbrant
 * Remove unhelpful comment
 *
 * Revision 1.34  2006/01/03 17:04:50  peterbrant
 * Many pagination bug fixes / Add ability to position absolute boxes in margin area
 *
 * Revision 1.33  2006/01/03 02:12:21  peterbrant
 * Various pagination fixes / Fix fixed positioning
 *
 * Revision 1.32  2006/01/01 02:38:18  peterbrant
 * Merge more pagination work / Various minor cleanups
 *
 * Revision 1.31  2005/12/30 01:32:39  peterbrant
 * First merge of parts of pagination work
 *
 * Revision 1.30  2005/12/21 02:36:29  peterbrant
 * - Calculate absolute positions incrementally (prep work for pagination)
 * - Light cleanup
 * - Fix bug where floats nested in floats could cause the outer float to be positioned in the wrong place
 *
 * Revision 1.29  2005/12/17 02:24:14  peterbrant
 * Remove last pieces of old (now non-working) clip region checking / Push down handful of fields from Box to BlockBox
 *
 * Revision 1.28  2005/12/15 20:04:48  peterbrant
 * Implement visibility: hidden
 *
 * Revision 1.27  2005/12/13 20:46:06  peterbrant
 * Improve list support (implement list-style-position: inside, marker "sticks" to first line box even if there are other block boxes in between, plus other minor fixes) / Experimental support for optionally extending text decorations to box edge vs line edge
 *
 * Revision 1.26  2005/12/13 02:41:33  peterbrant
 * Initial implementation of vertical-align: top/bottom (not done yet) / Minor cleanup and optimization
 *
 * Revision 1.25  2005/12/09 21:41:20  peterbrant
 * Finish support for relative inline layers
 *
 * Revision 1.24  2005/12/09 01:24:56  peterbrant
 * Initial commit of relative inline layers
 *
 * Revision 1.23  2005/12/05 00:13:53  peterbrant
 * Improve list-item support (marker positioning is now correct) / Start support for relative inline layers
 *
 * Revision 1.22  2005/11/25 22:38:39  peterbrant
 * Clean imports
 *
 * Revision 1.21  2005/11/25 16:57:19  peterbrant
 * Initial commit of inline content refactoring
 *
 * Revision 1.20  2005/11/12 21:55:27  tobega
 * Inline enhancements: block box text decorations, correct line-height when it is a number, better first-letter handling
 *
 * Revision 1.19  2005/11/08 22:53:46  tobega
 * added getLineHeight method to CalculatedStyle and hacked in some list-item support
 *
 * Revision 1.18  2005/11/08 20:03:57  peterbrant
 * Further progress on painting order / improved positioning implementation
 *
 * Revision 1.17  2005/11/05 18:45:06  peterbrant
 * General cleanup / Remove obsolete code
 *
 * Revision 1.16  2005/10/27 00:09:02  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.15  2005/10/21 13:17:15  pdoubleya
 * Rename some methods in RectPropertySet, cleanup.
 *
 * Revision 1.14  2005/10/21 12:01:20  pdoubleya
 * Added cachable rect property for margin, cleanup minor in styling.
 *
 * Revision 1.13  2005/10/21 05:52:10  tobega
 * A little more experimenting with flattened render tree
 *
 * Revision 1.12  2005/10/18 20:57:04  tobega
 * Patch from Peter Brant
 *
 * Revision 1.11  2005/10/16 23:57:16  tobega
 * Starting experiment with flat representation of render tree
 *
 * Revision 1.10  2005/10/06 03:20:22  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.9  2005/10/02 21:29:59  tobega
 * Fixed a lot of concurrency (and other) issues from incremental rendering. Also some house-cleaning.
 *
 * Revision 1.8  2005/09/26 22:40:20  tobega
 * Applied patch from Peter Brant concerning margin collapsing
 *
 * Revision 1.7  2005/01/29 20:24:23  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.6  2004/12/16 15:53:10  joshy
 * fixes for absolute layout
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/12/11 23:36:49  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.4  2004/12/01 01:57:02  joshy
 * more updates for float support.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/11/18 16:45:12  joshy
 * improved the float code a bit.
 * now floats are automatically forced to be blocks
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.2  2004/10/23 13:50:26  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

