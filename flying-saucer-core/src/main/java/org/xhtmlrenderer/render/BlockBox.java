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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.layout.BlockBoxing;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.BreakAtLineContext;
import org.xhtmlrenderer.layout.CounterFunction;
import org.xhtmlrenderer.layout.FloatManager;
import org.xhtmlrenderer.layout.InlineBoxing;
import org.xhtmlrenderer.layout.InlinePaintable;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.PaintingInfo;
import org.xhtmlrenderer.layout.PersistentBFC;
import org.xhtmlrenderer.layout.Styleable;
import org.xhtmlrenderer.newtable.TableRowBox;

/**
 * A block box as defined in the CSS spec.  It also provides a base class for
 * other kinds of block content (for example table rows or cells).
 */
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

    private boolean _fromCaptionedTable;

    public BlockBox() {
        super();
    }

    public BlockBox copyOf() {
        BlockBox result = new BlockBox();
        result.setStyle(getStyle());
        result.setElement(getElement());

        return result;
    }

    protected String getExtraBoxDescription() {
        return "";
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        String className = getClass().getName();
        result.append(className.substring(className.lastIndexOf('.') + 1));
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

        if (getStyle().isRunning()) {
            result.append("(running) ");
        }

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

        result.append(getExtraBoxDescription());

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

        RectPropertySet margin = getMargin(c);
        result.append(" effMargin=[" + margin.top() + ", " + margin.right() + ", " +
                margin.bottom() + ", " + margin.right() + "] ");
        RectPropertySet styleMargin = getStyleMargin(c);
        result.append(" styleMargin=[" + styleMargin.top() + ", " + styleMargin.right() + ", " +
                styleMargin.bottom() + ", " + styleMargin.right() + "] ");

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
                    for (Iterator i = getInlineContent().iterator(); i.hasNext();) {
                        Styleable styleable = (Styleable) i.next();
                        if (styleable instanceof BlockBox) {
                            BlockBox b = (BlockBox) styleable;
                            result.append(b.dump(c, indent + "  ", which));
                            if (result.charAt(result.length() - 1) == '\n') {
                                result.deleteCharAt(result.length() - 1);
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
            return (LineBox) b;
        }
    }

    public void paintDebugOutline(RenderingContext c) {
        c.getOutputDevice().drawDebugOutline(c, this, FSRGBColor.RED);
    }

    public MarkerData getMarkerData() {
        return _markerData;
    }

    public void setMarkerData(MarkerData markerData) {
        _markerData = markerData;
    }

    public void createMarkerData(LayoutContext c) {
        if (getMarkerData() != null)
        {
            return;
        }

        StrutMetrics strutMetrics = InlineBoxing.createDefaultStrutMetrics(c, this);

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
        int diameter = (int) ((strutMetrics.getAscent() + strutMetrics.getDescent()) / 3);

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
                    img.scale(-1, (int) strutMetrics.getAscent());
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

        int listCounter = getListCounter();
        text = CounterFunction.createCounterText(listStyle, listCounter);

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
            if (manager != null) {
                Point offset = manager.getOffset(this);
                setAbsX(manager.getMaster().getAbsX() + getX() - offset.x);
                setAbsY(manager.getMaster().getAbsY() + getY() - offset.y);
            }
        }

        LineBox lineBox = getLineBox();
        if (lineBox == null) {
            Box parent = getParent();
            if (parent != null) {
                setAbsX(parent.getAbsX() + parent.getTx() + getX());
                setAbsY(parent.getAbsY() + parent.getTy() + getY());
            } else if (isStyled() && getStyle().isAbsFixedOrInlineBlockEquiv()) {
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
                setX((int) style.getFloatPropertyProportionalWidth(CSSName.LEFT, getContainingBlock().getContentWidth(), cssCtx));
            } else if (!style.isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
                setX(boundingBox.width -
                        (int) style.getFloatPropertyProportionalWidth(CSSName.RIGHT, getContainingBlock().getContentWidth(), cssCtx) - getWidth());
            }
            setX(getX() + boundingBox.x);
        }

        if ((direction & POSITION_VERTICALLY) != 0) {
            setY(0);
            if (!style.isIdent(CSSName.TOP, IdentValue.AUTO)) {
                setY((int) style.getFloatPropertyProportionalHeight(CSSName.TOP, cbContentHeight, cssCtx));
            } else if (!style.isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
                setY(boundingBox.height -
                        (int) style.getFloatPropertyProportionalWidth(CSSName.BOTTOM, cbContentHeight, cssCtx) - getHeight());
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
            forcePageBreakBefore(c, getStyle().getIdent(CSSName.PAGE_BREAK_BEFORE), false);
            calcCanvasLocation();
            calcChildLocations();

            setNeedPageClear(false);
        }
    }

    public ReplacedElement getReplacedElement() {
        return _replacedElement;
    }

    public void setReplacedElement(ReplacedElement replacedElement) {
        _replacedElement = replacedElement;
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

        if (getStyle().isRunning()) {
            c.getRootLayer().removeRunningBlock(this);
        }
    }

    private int calcPinnedContentWidth(CssContext c) {
        if (! getStyle().isIdent(CSSName.LEFT, IdentValue.AUTO) &&
                ! getStyle().isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
            Rectangle paddingEdge = getContainingBlock().getPaddingEdge(0, 0, c);

            int left = (int) getStyle().getFloatPropertyProportionalTo(
                    CSSName.LEFT, paddingEdge.width, c);
            int right = (int) getStyle().getFloatPropertyProportionalTo(
                    CSSName.RIGHT, paddingEdge.width, c);

            int result = paddingEdge.width - left - right - getLeftMBP() - getRightMBP();
            return result < 0 ? 0 : result;
        }

        return -1;
    }

    private int calcPinnedHeight(CssContext c) {
        if (! getStyle().isIdent(CSSName.TOP, IdentValue.AUTO) &&
                ! getStyle().isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
            Rectangle paddingEdge = getContainingBlock().getPaddingEdge(0, 0, c);

            int top = (int) getStyle().getFloatPropertyProportionalTo(
                    CSSName.TOP, paddingEdge.height, c);
            int bottom = (int) getStyle().getFloatPropertyProportionalTo(
                    CSSName.BOTTOM, paddingEdge.height, c);


            int result = paddingEdge.height - top - bottom;
            return result < 0 ? 0 : result;
        }

        return -1;
    }

    protected void resolveAutoMargins(
            LayoutContext c, int cssWidth,
            RectPropertySet padding, BorderPropertySet border) {
        int withoutMargins =
                (int) border.left() + (int) padding.left() +
                        cssWidth +
                        (int) padding.right() + (int) border.right();
        if (withoutMargins < getContainingBlockWidth()) {
            int available = getContainingBlockWidth() - withoutMargins;

            boolean autoLeft = getStyle().isAutoLeftMargin();
            boolean autoRight = getStyle().isAutoRightMargin();

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

    private int calcEffPageRelativeWidth(LayoutContext c) {
        int totalLeftMBP = 0;
        int totalRightMBP = 0;

        boolean usePageRelativeWidth = true;

        Box current = this;
        while (true) {
            CalculatedStyle style = current.getStyle();
            if (style.isAutoWidth() && ! style.isCanBeShrunkToFit()) {
                totalLeftMBP += current.getLeftMBP();
                totalRightMBP += current.getRightMBP();
            } else {
                usePageRelativeWidth = false;
                break;
            }

            if (current.getContainingBlock().isInitialContainingBlock()) {
                break;
            } else {
                current = current.getContainingBlock();
            }
        }

        if (usePageRelativeWidth) {
            PageBox currentPage = c.getRootLayer().getFirstPage(c, this);
            return currentPage.getContentWidth(c) - totalLeftMBP - totalRightMBP;
        } else {
            return getContainingBlockWidth() - getLeftMBP() - getRightMBP();
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

            recalcMargin(c);
            RectPropertySet margin = getMargin(c);

            // CLEAN: cast to int
            setLeftMBP((int) margin.left() + (int) border.left() + (int) padding.left());
            setRightMBP((int) padding.right() + (int) border.right() + (int) margin.right());
            if (c.isPrint() && getStyle().isDynamicAutoWidth()) {
                setContentWidth(calcEffPageRelativeWidth(c));
            } else {
                setContentWidth((getContainingBlockWidth() - getLeftMBP() - getRightMBP()));
            }
            setHeight(0);

            if (! isAnonymous() || (isFromCaptionedTable() && isFloated())) {
                int pinnedContentWidth = -1;

                boolean borderBox = style.isBorderBox();

                if (cssWidth != -1) {
                    if (borderBox) {
                        setContentWidth(cssWidth - (int)border.width() - (int)padding.width());
                    } else {
                        setContentWidth(cssWidth);
                    }
                } else if (getStyle().isAbsolute() || getStyle().isFixed()) {
                    pinnedContentWidth = calcPinnedContentWidth(c);
                    if (pinnedContentWidth != -1) {
                        setContentWidth(pinnedContentWidth);
                    }
                }

                int cssHeight = getCSSHeight(c);
                if (cssHeight != -1) {
                    if (borderBox) {
                        setHeight(cssHeight - (int)padding.height() - (int)border.height());
                    } else {
                        setHeight(cssHeight);
                    }

                }

                //check if replaced
                ReplacedElement re = getReplacedElement();
                if (re == null) {
                    re = c.getReplacedElementFactory().createReplacedElement(
                            c, this, c.getUac(), cssWidth, cssHeight);
                    if (re != null){
                        re = fitReplacedElement(c, re);
                    }
                }
                if (re != null) {
                    setContentWidth(re.getIntrinsicWidth());
                    setHeight(re.getIntrinsicHeight());
                    setReplacedElement(re);
                } else if (cssWidth == -1 && pinnedContentWidth == -1 &&
                        style.isCanBeShrunkToFit()) {
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
        if (getStyle().isCleared() && ! getStyle().isFloated()) {
            c.translate(0, -getY());
            c.getBlockFormattingContext().clear(c, this);
            c.translate(0, getY());
            calcCanvasLocation();
        }
    }

    private void calcExtraPageClearance(LayoutContext c) {
        if (c.isPageBreaksAllowed() &&
                c.getExtraSpaceTop() > 0 && (getStyle().isSpecifiedAsBlock() || getStyle().isListItem())) {
            PageBox first = c.getRootLayer().getFirstPage(c, this);
            if (first != null && first.getTop() + c.getExtraSpaceTop() > getAbsY()) {
                int diff = first.getTop() + c.getExtraSpaceTop() - getAbsY();
                setY(getY() + diff);
                c.translate(0, diff);
                calcCanvasLocation();
            }
        }
    }

    private void addBoxID(LayoutContext c) {
        if (! isAnonymous()) {
            String name = c.getNamespaceHandler().getAnchorName(getElement());
            if (name != null) {
                c.addBoxId(name, this);
            }
            String id = c.getNamespaceHandler().getID(getElement());
            if (id != null) {
                c.addBoxId(id, this);
            }
        }
    }

    public void layout(LayoutContext c) {
        layout(c, 0);
    }

    public void layout(LayoutContext c, int contentStart) {
        CalculatedStyle style = getStyle();

        boolean pushedLayer = false;
        if (isRoot() || style.requiresLayer()) {
            pushedLayer = true;
            if (getLayer() == null) {
                c.pushLayer(this);
            } else {
                c.pushLayer(getLayer());
            }
        }

        if (style.isFixedBackground()) {
            c.getRootLayer().setFixedBackground(true);
        }

        calcClearance(c);

        if (isRoot() || getStyle().establishesBFC() || isMarginAreaRoot()) {
            BlockFormattingContext bfc = new BlockFormattingContext(this, c);
            c.pushBFC(bfc);
        }

        addBoxID(c);

        if (c.isPrint() && getStyle().isIdent(CSSName.FS_PAGE_SEQUENCE, IdentValue.START)) {
            c.getRootLayer().addPageSequence(this);
        }

        calcDimensions(c);
        calcShrinkToFitWidthIfNeeded(c);
        collapseMargins(c);

        calcExtraPageClearance(c);

        if (c.isPrint()) {
            PageBox firstPage = c.getRootLayer().getFirstPage(c, this);
            if (firstPage != null && firstPage.getTop() == getAbsY() - getPageClearance()) {
                resetTopMargin(c);
            }
        }

        BorderPropertySet border = getBorder(c);
        RectPropertySet margin = getMargin(c);
        RectPropertySet padding = getPadding(c);

        // save height in case fixed height
        int originalHeight = getHeight();

        if (! isReplaced()) {
            setHeight(0);
        }

        boolean didSetMarkerData = false;
        if (getStyle().isListItem()) {
            createMarkerData(c);
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
            layoutChildren(c, contentStart);
        else {
            setState(Box.DONE);
        }
        c.translate(-getTx(), -getTy());

        setChildrenHeight(getHeight());

        if (! isReplaced()) {
            if (! isAutoHeight()) {
                int delta = originalHeight - getHeight();
                if (delta > 0 || isAllowHeightToShrink()) {
                    setHeight(originalHeight);
                }
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
        }

        if (didSetMarkerData) {
            c.setCurrentMarkerData(null);
        }

        calcLayoutHeight(c, border, margin, padding);

        if (isRoot() || getStyle().establishesBFC()) {
            c.popBFC();
        }

        if (pushedLayer) {
            c.popLayer();
        }
    }

    protected boolean isAllowHeightToShrink() {
        return true;
    }

    protected int getPageClearance() {
        return 0;
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

    protected void layoutChildren(LayoutContext c, int contentStart) {
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
                layoutInlineChildren(c, contentStart, calcInitialBreakAtLine(c), true);
                break;
            case CONTENT_BLOCK:
                BlockBoxing.layoutContent(c, this, contentStart);
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

    protected void layoutInlineChildren(
            LayoutContext c, int contentStart, int breakAtLine, boolean tryAgain) {
        InlineBoxing.layoutContent(c, this, contentStart, breakAtLine);

        if (c.isPrint() && c.isPageBreaksAllowed() && getChildCount() > 1) {
            satisfyWidowsAndOrphans(c, contentStart, tryAgain);
        }

        if (tryAgain && getStyle().isTextJustify()) {
            justifyText();
        }
    }

    private void justifyText() {
        for (Iterator i = getChildIterator(); i.hasNext(); ) {
            LineBox line = (LineBox)i.next();
            line.justify();
        }
    }

    private void satisfyWidowsAndOrphans(LayoutContext c, int contentStart, boolean tryAgain) {
        LineBox firstLineBox = (LineBox)getChild(0);
        PageBox firstPage = c.getRootLayer().getFirstPage(c, firstLineBox);

        if (firstPage == null) {
            return;
        }

        int noContentLBs = 0;
        int i = 0;
        int cCount = getChildCount();
        while (i < cCount) {
            LineBox lB = (LineBox)getChild(i);
            if (lB.getAbsY() >= firstPage.getBottom()) {
                break;
            }
            if (! lB.isContainsContent()) {
                noContentLBs++;
            }
            i++;
        }

        if (i != cCount) {
            int orphans = (int)getStyle().asFloat(CSSName.ORPHANS);
            if (i - noContentLBs < orphans) {
                setNeedPageClear(true);
            } else {
                LineBox lastLineBox = (LineBox)getChild(cCount-1);
                List pages = c.getRootLayer().getPages();
                PageBox lastPage = (PageBox)pages.get(firstPage.getPageNo()+1);
                while (lastPage.getPageNo() != pages.size() - 1 &&
                        lastPage.getBottom() < lastLineBox.getAbsY()) {
                    lastPage = (PageBox)pages.get(lastPage.getPageNo()+1);
                }

                noContentLBs = 0;
                i = cCount-1;
                while (i >= 0 && ((LineBox)getChild(i)).getAbsY() >= lastPage.getTop()) {
                    LineBox lB = (LineBox)getChild(i);
                    if (lB.getAbsY() < lastPage.getTop()) {
                        break;
                    }
                    if (! lB.isContainsContent()) {
                        noContentLBs++;
                    }
                    i--;
                }

                int widows = (int)getStyle().asFloat(CSSName.WIDOWS);
                if (cCount - 1 - i - noContentLBs < widows) {
                    if (cCount - 1 - widows < orphans) {
                        setNeedPageClear(true);
                    } else if (tryAgain) {
                        int breakAtLine = cCount - 1 - widows;

                        resetChildren(c);
                        removeAllChildren();

                        layoutInlineChildren(c, contentStart, breakAtLine, false);
                    }
                }
            }
        }
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
            for (Iterator i = inlineContent.iterator(); i.hasNext();) {
                Styleable child = (Styleable) i.next();
                if (child instanceof Box) {
                    ((Box) child).setContainingBlock(this);
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
            recalcMargin(c);
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
                    if ((int) margin.top() != collapsedMargin.getMargin()) {
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
        } else if ((int) margin.bottom() != collapsedMargin.getMargin()) {
            setMarginBottom(c, collapsedMargin.getMargin());
        }
    }

    private BlockBox getNextCollapsableSibling(MarginCollapseResult collapsedMargin) {
        BlockBox next = (BlockBox) getNextSibling();
        while (next != null) {
            if (next instanceof AnonymousBlockBox) {
                ((AnonymousBlockBox) next).provideSiblingMarginToFloats(
                        collapsedMargin.getMargin());
            }
            if (! next.isSkipWhenCollapsingMargins()) {
                break;
            } else {
                next = (BlockBox) next.getNextSibling();
            }
        }
        return next;
    }

    private void collapseTopMargin(
            LayoutContext c, boolean calculationRoot, MarginCollapseResult result) {
        if (! isTopMarginCalculated()) {
            if (! isSkipWhenCollapsingMargins()) {
                calcDimensions(c);
                if (c.isPrint() && getStyle().isDynamicAutoWidthApplicable()) {
                    // Force recalculation once box is positioned
                    setDimensionsCalculated(false);
                }
                RectPropertySet margin = getMargin(c);
                result.update((int) margin.top());

                if (! calculationRoot && (int) margin.top() != 0) {
                    setMarginTop(c, 0);
                }

                if (isMayCollapseMarginsWithChildren() && isNoTopPaddingOrBorder(c)) {
                    ensureChildren(c);
                    if (getChildrenContentType() == CONTENT_BLOCK) {
                        for (Iterator i = getChildIterator(); i.hasNext();) {
                            BlockBox child = (BlockBox) i.next();
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
                if (c.isPrint() && getStyle().isDynamicAutoWidthApplicable()) {
                    // Force recalculation once box is positioned
                    setDimensionsCalculated(false);
                }
                RectPropertySet margin = getMargin(c);
                result.update((int) margin.bottom());

                if (! calculationRoot && (int) margin.bottom() != 0) {
                    setMarginBottom(c, 0);
                }

                if (isMayCollapseMarginsWithChildren() &&
                        ! getStyle().isTable() && isNoBottomPaddingOrBorder(c)) {
                    ensureChildren(c);
                    if (getChildrenContentType() == CONTENT_BLOCK) {
                        for (int i = getChildCount() - 1; i >= 0; i--) {
                            BlockBox child = (BlockBox) getChild(i);

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

        return (int) padding.top() == 0 && (int) border.top() == 0;
    }

    private boolean isNoBottomPaddingOrBorder(LayoutContext c) {
        RectPropertySet padding = getPadding(c);
        BorderPropertySet border = getBorder(c);

        return (int) padding.bottom() == 0 && (int) border.bottom() == 0;
    }

    private void collapseEmptySubtreeMargins(LayoutContext c, MarginCollapseResult result) {
        RectPropertySet margin = getMargin(c);
        result.update((int) margin.top());
        result.update((int) margin.bottom());

        setMarginTop(c, 0);
        setTopMarginCalculated(true);
        setMarginBottom(c, 0);
        setBottomMarginCalculated(true);

        ensureChildren(c);
        if (getChildrenContentType() == CONTENT_BLOCK) {
            for (Iterator i = getChildIterator(); i.hasNext();) {
                BlockBox child = (BlockBox) i.next();
                child.collapseEmptySubtreeMargins(c, result);
            }
        }
    }

    private boolean isVerticalMarginsAdjoin(LayoutContext c) {
        CalculatedStyle style = getStyle();

        BorderPropertySet borderWidth = style.getBorder(c);
        RectPropertySet padding = getPadding(c);

        boolean bordersOrPadding =
                (int) borderWidth.top() != 0 || (int) borderWidth.bottom() != 0 ||
                        (int) padding.top() != 0 || (int) padding.bottom() != 0;

        if (bordersOrPadding) {
            return false;
        }

        ensureChildren(c);
        if (getChildrenContentType() == CONTENT_INLINE) {
            return false;
        } else if (getChildrenContentType() == CONTENT_BLOCK) {
            for (Iterator i = getChildIterator(); i.hasNext();) {
                BlockBox child = (BlockBox) i.next();
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
        return getCSSWidth(c, false);
    }

    protected int getCSSWidth(CssContext c, boolean shrinkingToFit) {
        if (! isAnonymous()) {
            if (! getStyle().isAutoWidth()) {
                if (shrinkingToFit && ! getStyle().isAbsoluteWidth()) {
                    return -1;
                } else {
                    int result = (int) getStyle().getFloatPropertyProportionalWidth(
                            CSSName.WIDTH, getContainingBlock().getContentWidth(), c);
                    return result >= 0 ? result : -1;
                }
            }
        }

        return -1;
    }

    protected int getCSSFitToWidth(CssContext c) {
        if (! isAnonymous()) {
            if (! getStyle().isIdent(CSSName.FS_FIT_IMAGES_TO_WIDTH, IdentValue.AUTO))
            {
                int result = (int) getStyle().getFloatPropertyProportionalWidth(
                        CSSName.FS_FIT_IMAGES_TO_WIDTH, getContainingBlock().getContentWidth(), c);
                return result >= 0 ? result : -1;
            }
        }

        return -1;
    }

    protected int getCSSHeight(CssContext c) {
        if (! isAnonymous()) {
            if (! isAutoHeight()) {
                if (getStyle().hasAbsoluteUnit(CSSName.HEIGHT)) {
                    return (int)getStyle().getFloatPropertyProportionalHeight(CSSName.HEIGHT, 0, c);
                } else {
                    return (int)getStyle().getFloatPropertyProportionalHeight(
                            CSSName.HEIGHT,
                            ((BlockBox)getContainingBlock()).getCSSHeight(c),
                            c);
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
            // We have a percentage height, defer to our block parent (if applicable)
            Box cb = getContainingBlock();
            if (cb.isStyled() && (cb instanceof BlockBox)) {
                return ((BlockBox)cb).isAutoHeight();
            } else if (cb instanceof BlockBox && ((BlockBox)cb).isInitialContainingBlock()) {
                return false;
            } else {
                return true;
            }
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
                return (int) getContainingBlock().getStyle().getFloatPropertyProportionalTo(
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
                        (int) getStyle().getFloatPropertyProportionalTo(CSSName.LEFT,
                                getContainingBlock().getContentWidth(), c);
            }

            if (! getStyle().isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
                right =
                        (int) getStyle().getFloatPropertyProportionalTo(CSSName.RIGHT,
                                getContainingBlock().getContentWidth(), c);
            }

            return getContainingBlock().getPaddingWidth(c) - left - right;
        }
    }

    protected boolean isFixedWidthAdvisoryOnly() {
        return false;
    }


    private void recalcMargin(LayoutContext c) {
        if (isTopMarginCalculated() && isBottomMarginCalculated()) {
            return;
        }

        // Check if we're a potential candidate upfront to avoid expensive
        // getStyleMargin(c, false) call
        FSDerivedValue topMargin = getStyle().valueByName(CSSName.MARGIN_TOP);
        boolean resetTop = topMargin instanceof LengthValue && ! topMargin.hasAbsoluteUnit();

        FSDerivedValue bottomMargin = getStyle().valueByName(CSSName.MARGIN_BOTTOM);
        boolean resetBottom = bottomMargin instanceof LengthValue && ! bottomMargin.hasAbsoluteUnit();

        if (! resetTop && ! resetBottom) {
            return;
        }

        RectPropertySet styleMargin = getStyleMargin(c, false);
        RectPropertySet workingMargin = getMargin(c);

        // A shrink-to-fit calculation may have set incorrect values for
        // percentage margins (as the containing block width
        // hasn't been calculated yet).  Reset top and bottom margins
        // in this case.
        if (! isTopMarginCalculated() &&
                styleMargin.top() != workingMargin.top()) {
            setMarginTop(c, (int) styleMargin.top());
        }

        if (! isBottomMarginCalculated() &&
                styleMargin.bottom() != workingMargin.bottom()) {
            setMarginBottom(c, (int) styleMargin.bottom());
        }
    }

    public void calcMinMaxWidth(LayoutContext c) {
        if (! isMinMaxCalculated()) {
            RectPropertySet margin = getMargin(c);
            BorderPropertySet border = getBorder(c);
            RectPropertySet padding = getPadding(c);

            int width = getCSSWidth(c, true);

            if (width == -1) {
                if (isReplaced()) {
                    width = getReplacedElement().getIntrinsicWidth();
                } else {
                    int height = getCSSHeight(c);
                    ReplacedElement re = c.getReplacedElementFactory().createReplacedElement(
                            c, this, c.getUac(), width, height);
                    if (re != null) {
                        re = fitReplacedElement(c, re);
                        setReplacedElement(re);
                        width = getReplacedElement().getIntrinsicWidth();
                    }
                }
            }

            if (isReplaced() || (width != -1 && ! isFixedWidthAdvisoryOnly())) {
                _minWidth = _maxWidth =
                        (int) margin.left() + (int) border.left() + (int) padding.left() +
                                width +
                                (int) margin.right() + (int) border.right() + (int) padding.right();
            } else {
                int cw = -1;
                if (width != -1) {
                    // Set a provisional content width on table cells so
                    // percentage values resolve correctly (but save and reset
                    // the existing value)
                    cw = getContentWidth();
                    setContentWidth(width);
                }

                _minWidth = _maxWidth =
                        (int) margin.left() + (int) border.left() + (int) padding.left() +
                                (int) margin.right() + (int) border.right() + (int) padding.right();

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

                if (cw != -1) {
                    setContentWidth(cw);
                }
            }

            if (! isReplaced()) {
                calcMinMaxCSSMinMaxWidth(c, margin, border, padding);
            }

            setMinMaxCalculated(true);
        }
    }

    private ReplacedElement fitReplacedElement(LayoutContext c,
            ReplacedElement re)
    {
        int maxImageWidth = getCSSFitToWidth(c);
        if (maxImageWidth > -1 && re.getIntrinsicWidth() > maxImageWidth)
        {
            double oldWidth = (double)re.getIntrinsicWidth();
            double scale = ((double)maxImageWidth)/oldWidth;
            re = c.getReplacedElementFactory().createReplacedElement(
                    c, this, c.getUac(), maxImageWidth, (int)Math.rint(scale * (double)re.getIntrinsicHeight()));
        }
        return re;
    }

    private void calcMinMaxCSSMinMaxWidth(
            LayoutContext c, RectPropertySet margin, BorderPropertySet border,
            RectPropertySet padding) {
        int cssMinWidth = getCSSMinWidth(c);
        if (cssMinWidth > 0) {
            cssMinWidth +=
                    (int) margin.left() + (int) border.left() + (int) padding.left() +
                            (int) margin.right() + (int) border.right() + (int) padding.right();
            if (_minWidth < cssMinWidth) {
                _minWidth = cssMinWidth;
            }
        }
        if (! getStyle().isMaxWidthNone()) {
            int cssMaxWidth = getCSSMaxWidth(c);
            cssMaxWidth +=
                    (int) margin.left() + (int) border.left() + (int) padding.left() +
                            (int) margin.right() + (int) border.right() + (int) padding.right();
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

        for (Iterator i = getChildIterator(); i.hasNext();) {
            BlockBox child = (BlockBox) i.next();
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
        int textIndent = (int) getStyle().getFloatPropertyProportionalWidth(
                CSSName.TEXT_INDENT, getContentWidth(), c);

        if (getStyle().isListItem() && getStyle().isListMarkerInside()) {
            createMarkerData(c);
            textIndent += getMarkerData().getLayoutWidth();
        }

        int childMinWidth = 0;
        int childMaxWidth = 0;
        int lineWidth = 0;

        InlineBox trimmableIB = null;

        for (Iterator i = _inlineContent.iterator(); i.hasNext();) {
            Styleable child = (Styleable) i.next();

            if (child.getStyle().isAbsolute() || child.getStyle().isFixed() || child.getStyle().isRunning()) {
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
                BlockBox block = (BlockBox) child;
                block.calcMinMaxWidth(c);
                lineWidth += block.getMaxWidth();
                if (block.getMinWidth() > childMinWidth) {
                    childMinWidth = block.getMinWidth();
                }
            } else { /* child.getStyle().isInline() */
                InlineBox iB = (InlineBox) child;
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
            for (Iterator i = _inlineContent.iterator(); i.hasNext();) {
                Styleable child = (Styleable) i.next();
                if (child instanceof InlineBox) {
                    InlineBox iB = (InlineBox) child;

                    if (iB.isStartsHere()) {
                        CascadedStyle cs = null;
                        if (iB.getElement() != null) {
                            if (iB.getPseudoElementOrClass() == null) {
                                cs = c.getCss().getCascadedStyle(iB.getElement(), false);
                            } else {
                                cs = c.getCss().getPseudoElementStyle(
                                        iB.getElement(), iB.getPseudoElementOrClass());
                            }
                            styles.add(((CalculatedStyle) styles.getLast()).deriveStyle(cs));
                        } else {
                            styles.add(style.createAnonymousStyle(IdentValue.INLINE));
                        }
                    }

                    iB.setStyle(((CalculatedStyle) styles.getLast()));
                    iB.applyTextTransform();

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
            (this).getPersistentBFC().getFloatManager().performFloatOperation(
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
            Box b = getChild(i);
            if (b instanceof LineBox) {
                return b.getAbsY() + ((LineBox) b).getBaseline();
            } else {
                if (b instanceof TableRowBox) {
                    return b.getAbsY() + ((TableRowBox) b).getBaseline();
                } else {
                    int result = ((BlockBox) b).calcBaseline(c);
                    if (result != NO_BASELINE) {
                        return result;
                    }
                }
            }
        }

        return NO_BASELINE;
    }

    protected int calcInitialBreakAtLine(LayoutContext c) {
        BreakAtLineContext bContext = c.getBreakAtLineContext();
        if (bContext != null && bContext.getBlock() == this) {
            return bContext.getLine();
        }
        return 0;
    }

    public boolean isCurrentBreakAtLineContext(LayoutContext c) {
        BreakAtLineContext bContext = c.getBreakAtLineContext();
        return bContext != null && bContext.getBlock() == this;
    }

    public BreakAtLineContext calcBreakAtLineContext(LayoutContext c) {
        if (! c.isPrint() || ! getStyle().isKeepWithInline()) {
            return null;
        }

        LineBox breakLine = findLastNthLineBox((int)getStyle().asFloat(CSSName.WIDOWS));
        if (breakLine != null) {
            PageBox linePage = c.getRootLayer().getLastPage(c, breakLine);
            PageBox ourPage = c.getRootLayer().getLastPage(c, this);
            if (linePage != null && ourPage != null && linePage.getPageNo() + 1 == ourPage.getPageNo()) {
                BlockBox breakBox = (BlockBox)breakLine.getParent();
                return new BreakAtLineContext(breakBox, breakBox.findOffset(breakLine));
            }
        }

        return null;
    }

    public int calcInlineBaseline(CssContext c) {
        if (isReplaced() && getReplacedElement().hasBaseline()) {
            Rectangle bounds = getContentAreaEdge(getAbsX(), getAbsY(), c);
            return bounds.y + getReplacedElement().getBaseline() - getAbsY();
        } else {
            LineBox lastLine = findLastLineBox();
            if (lastLine == null) {
                return getHeight();
            } else {
                return lastLine.getAbsY() + lastLine.getBaseline() - getAbsY();
            }
        }
    }

    public int findOffset(Box box) {
        int ccount = getChildCount();
        for (int i = 0; i < ccount; i++) {
            if (getChild(i) == box) {
                return i;
            }
        }
        return -1;
    }

    public LineBox findLastNthLineBox(int count) {
        LastLineBoxContext context = new LastLineBoxContext(count);
        findLastLineBox(context);
        return context.line;
    }

    private static class LastLineBoxContext {
        public int current;
        public LineBox line;

        public LastLineBoxContext(int i) {
            this.current = i;
        }
    }

    private void findLastLineBox(LastLineBoxContext context) {
        int type = getChildrenContentType();
        int ccount = getChildCount();
        if (ccount > 0) {
            if (type == CONTENT_INLINE) {
                for (int i = ccount - 1; i >= 0; i--) {
                    LineBox child = (LineBox) getChild(i);
                    if (child.getHeight() > 0) {
                        context.line = child;
                        if (--context.current == 0) {
                            return;
                        }
                    }
                }
            } else if (type == CONTENT_BLOCK) {
                for (int i = ccount - 1; i >= 0; i--) {
                    ((BlockBox) getChild(i)).findLastLineBox(context);
                    if (context.current == 0) {
                        break;
                    }
                }
            }
        }
    }

    private LineBox findLastLineBox() {
        int type = getChildrenContentType();
        int ccount = getChildCount();
        if (ccount > 0) {
            if (type == CONTENT_INLINE) {
                for (int i = ccount - 1; i >= 0; i--) {
                    LineBox result = (LineBox) getChild(i);
                    if (result.getHeight() > 0) {
                        return result;
                    }
                }
            } else if (type == CONTENT_BLOCK) {
                for (int i = ccount - 1; i >= 0; i--) {
                    LineBox result = ((BlockBox) getChild(i)).findLastLineBox();
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
    }

    private LineBox findFirstLineBox() {
        int type = getChildrenContentType();
        int ccount = getChildCount();
        if (ccount > 0) {
            if (type == CONTENT_INLINE) {
                for (int i = 0; i < ccount; i++) {
                    LineBox result = (LineBox) getChild(i);
                    if (result.getHeight() > 0) {
                        return result;
                    }
                }
            } else if (type == CONTENT_BLOCK) {
                for (int i = 0; i < ccount; i++) {
                    LineBox result = ((BlockBox) getChild(i)).findFirstLineBox();
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
    }

    public boolean isNeedsKeepWithInline(LayoutContext c) {
        if (c.isPrint() && getStyle().isKeepWithInline()) {
            LineBox line = findFirstLineBox();
            if (line != null) {
                PageBox linePage = c.getRootLayer().getFirstPage(c, line);
                PageBox ourPage = c.getRootLayer().getFirstPage(c, this);
                return linePage != null && ourPage != null && linePage.getPageNo() == ourPage.getPageNo()+1;
            }
        }

        return false;
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

    public int getChildrenHeight() {
        return _childrenHeight;
    }

    protected void setChildrenHeight(int childrenHeight) {
        _childrenHeight = childrenHeight;
    }

    public boolean isFromCaptionedTable() {
        return _fromCaptionedTable;
    }

    public void setFromCaptionedTable(boolean fromTable) {
        _fromCaptionedTable = fromTable;
    }

    protected boolean isInlineBlock() {
        return isInline();
    }

    public boolean isInMainFlow() {
        Box flowRoot = this;
        while (flowRoot.getParent() != null) {
            flowRoot = flowRoot.getParent();
        }

        return flowRoot.isRoot();
    }

    public Box getDocumentParent() {
        Box staticEquivalent = getStaticEquivalent();
        if (staticEquivalent != null) {
            return staticEquivalent;
        } else {
            return getParent();
        }
    }

    public boolean isContainsInlineContent(LayoutContext c) {
        ensureChildren(c);
        switch (getChildrenContentType()) {
            case CONTENT_INLINE:
                return true;
            case CONTENT_EMPTY:
                return false;
            case CONTENT_BLOCK:
                for (Iterator i = getChildIterator(); i.hasNext(); ) {
                    BlockBox box = (BlockBox)i.next();
                    if (box.isContainsInlineContent(c)) {
                        return true;
                    }
                }
                return false;
        }

        throw new RuntimeException("internal error: no children");
    }

    public boolean checkPageContext(LayoutContext c) {
        if (! getStyle().isIdent(CSSName.PAGE, IdentValue.AUTO)) {
            String pageName = getStyle().getStringProperty(CSSName.PAGE);
            if ( (! pageName.equals(c.getPageName())) && isInDocumentFlow() &&
                    isContainsInlineContent(c)) {
                c.setPendingPageName(pageName);
                return true;
            }
        } else if (c.getPageName() != null && isInDocumentFlow()) {
            c.setPendingPageName(null);
            return true;
        }

        return false;
    }

    public boolean isNeedsClipOnPaint(RenderingContext c) {
        return ! isReplaced() &&
            getStyle().isIdent(CSSName.OVERFLOW, IdentValue.HIDDEN) &&
            getStyle().isOverflowApplies();
    }


    protected void propagateExtraSpace(
            LayoutContext c,
            ContentLimitContainer parentContainer, ContentLimitContainer currentContainer,
            int extraTop, int extraBottom) {
        int start = currentContainer.getInitialPageNo();
        int end = currentContainer.getLastPageNo();
        int current = start;

        while (current <= end) {
            ContentLimit contentLimit =
                currentContainer.getContentLimit(current);

            if (current != start) {
                int top = contentLimit.getTop();
                if (top != ContentLimit.UNDEFINED) {
                    parentContainer.updateTop(c, top - extraTop);
                }
            }

            if (current != end) {
                int bottom = contentLimit.getBottom();
                if (bottom != ContentLimit.UNDEFINED) {
                    parentContainer.updateBottom(c, bottom + extraBottom);
                }
            }

            current++;
        }
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
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.102  2010/01/13 00:42:11  peterbrant
 * Calculate clearance before possibly establishing new BFC.  It needs to be calculated relative to the existing BFC and not the new one.
 *
 * Revision 1.101  2010/01/12 14:33:27  peterbrant
 * Ignore auto margins when calculating table min/max width.  Also, when deciding whether or not to proceed with the auto margin calculation for a table,  make sure we compare consistently with how the table min width is actually set.
 *
 * Revision 1.100  2009/11/08 23:52:48  peterbrant
 * Treat percentage widths as auto when calculating min/max widths
 *
 * Revision 1.99  2008/12/14 19:20:05  peterbrant
 * Skip running blocks when calculating min/max widths
 *
 * Revision 1.98  2008/12/14 13:53:31  peterbrant
 * Implement -fs-keep-with-inline: keep property that instructs FS to try to avoid breaking a box so that only borders and padding appear on a page
 *
 * Revision 1.97  2008/09/06 18:21:50  peterbrant
 * Need to account for list-marker-position: inside when calculating inline min/max widths
 *
 * Revision 1.96  2008/07/27 00:21:47  peterbrant
 * Implement CMYK color support for PDF output, starting with patch from Mykola Gurov / Banish java.awt.Color from FS core layout classes
 *
 * Revision 1.95  2008/07/14 11:12:35  peterbrant
 * Fix two bugs when -fs-table-paginate is paginate.  Block boxes in cells in a <thead> that were also early on the page could be positioned incorrectly.  Line boxes contained within inline-block or inline-table content in a paginated table were generally placed incorrectly.
 *
 * Revision 1.94  2008/06/18 17:44:48  peterbrant
 * Fix a pair of NPEs when absolutely positioned content is positioned off the page
 *
 * Revision 1.93  2008/05/24 16:36:22  peterbrant
 * If our minimum width is greater than the calculated CSS width, don't try to allocate any margin space to auto margins.
 *
 * Revision 1.92  2007/10/15 22:33:44  peterbrant
 * Only try to satisfy widows and orphans if page breaking is allowed
 *
 * Revision 1.91  2007/09/19 22:50:43  peterbrant
 * Fix nested percentage height calculations (per report and test case from Simon Buettner)
 *
 * Revision 1.90  2007/08/29 22:18:17  peterbrant
 * Experiment with text justification
 *
 * Revision 1.89  2007/08/28 22:31:26  peterbrant
 * Implement widows and orphans properties
 *
 * Revision 1.88  2007/08/27 19:28:50  peterbrant
 * Enable extra page clearance calculation
 *
 * Revision 1.87  2007/08/24 18:36:08  peterbrant
 * Further progress on AcroForm support
 *
 * Revision 1.86  2007/08/23 20:52:31  peterbrant
 * Begin work on AcroForm support
 *
 * Revision 1.85  2007/08/19 22:22:49  peterbrant
 * Merge R8pbrant changes to HEAD
 *
 * Revision 1.84.2.13  2007/08/19 21:55:20  peterbrant
 * Try to keep block boxes out of no man's land when paginating a table (commented out)
 *
 * Revision 1.84.2.12  2007/08/19 20:14:16  peterbrant
 * Support nested paginated tables
 *
 * Revision 1.84.2.11  2007/08/17 23:53:32  peterbrant
 * Get rid of layer hack for overflow: hidden
 *
 * Revision 1.84.2.10  2007/08/17 19:11:30  peterbrant
 * When paginating a table, move table past page break if header would overlap the page break
 *
 * Revision 1.84.2.9  2007/08/17 17:11:00  peterbrant
 * Try to avoid awkward row splits when paginating a table by making sure there is at least one line box on the same page as the start of the row
 *
 * Revision 1.84.2.8  2007/08/13 22:41:13  peterbrant
 * First pass at exporting the render tree as text
 *
 * Revision 1.84.2.7  2007/08/09 20:18:15  peterbrant
 * Bug fixes to improved pagination support
 *
 * Revision 1.84.2.6  2007/08/09 17:03:15  peterbrant
 * Finish running block implementation
 *
 * Revision 1.84.2.5  2007/08/08 21:44:09  peterbrant
 * Implement more flexible page numbering
 *
 * Revision 1.84.2.4  2007/08/08 18:28:25  peterbrant
 * Further progress on CSS3 paged media
 *
 * Revision 1.84.2.3  2007/08/07 17:06:32  peterbrant
 * Implement named pages / Implement page-break-before/after: left/right / Experiment with efficient selection
 *
 * Revision 1.84.2.2  2007/07/11 22:48:30  peterbrant
 * Further progress on running headers and footers
 *
 * Revision 1.84.2.1  2007/07/09 22:18:03  peterbrant
 * Begin work on running headers and footers and named pages
 *
 * Revision 1.84  2007/06/16 22:51:24  tobega
 * We now support counters!
 *
 * Revision 1.83  2007/06/13 15:04:55  peterbrant
 * Remove obsolete counter related code
 *
 * Revision 1.82  2007/06/07 16:56:29  peterbrant
 * When vertically aligning table cell content, call layout again on cells as necessary to make sure pagination properties are respected at the cell's final position (and to make sure line boxes can't straddle page breaks).
 *
 * Revision 1.81  2007/06/05 19:29:53  peterbrant
 * More progress on counter support
 *
 * Revision 1.80  2007/06/02 06:56:44  peterbrant
 * Table page clearance should be taken into account when checking whether or not the top margin should be reset when a box is moved to a new page
 *
 * Revision 1.79  2007/04/25 18:09:41  peterbrant
 * Always reset block box margin if it is the first thing on a page
 *
 * Revision 1.78  2007/04/23 21:13:16  peterbrant
 * Calculate table cell height as if it included borders and padding (matches FF and Opera behavior)
 *
 * Revision 1.77  2007/04/16 01:10:05  peterbrant
 * Vertical margin and padding with percentage values may be incorrect if box participated in a shrink-to-fit calculation.  Fix margin calculation.
 *
 * Revision 1.76  2007/04/15 00:34:40  peterbrant
 * Allow inline-block / inline-table content to be relatively positioned
 *
 * Revision 1.75  2007/04/12 12:29:10  peterbrant
 * Properly handle floated tables with captions
 *
 * Revision 1.74  2007/03/17 22:55:51  peterbrant
 * Remove distinction between box IDs and named anchors
 *
 * Revision 1.73  2007/03/12 21:11:20  peterbrant
 * Documentation update
 *
 * Revision 1.72  2007/03/08 01:41:50  peterbrant
 * Don't calculate clearance for floated boxes (clear on floated boxes is handled directly by FloatManager) / Make sure we have a root layer before checking whether we have a fixed background or not
 *
 * Revision 1.71  2007/03/07 20:34:51  peterbrant
 * Set a provisional content width on table cells when calulating min/max width to make sure percentage values in children resolve to something other than zero / Make sure style changes correctly account for text-transform
 *
 * Revision 1.70  2007/03/02 00:45:15  peterbrant
 * Calculate baseline correctly for inline-block and inline-table elements
 *
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

