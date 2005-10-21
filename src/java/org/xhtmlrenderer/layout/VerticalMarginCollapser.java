/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
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

import org.w3c.dom.Node;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.css.value.Border;
import org.xhtmlrenderer.layout.content.*;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Exposes a utility method to collapse vertical margins (8.3.1)
 * <p/>
 * Top margins are collapsed by recursively looking at initial in-flow block level
 * children.  Successive eligible children are collected as long as no intervening
 * border or padding gets in the way.  After that, the collapsed top margin is
 * calculated per the spec.  This value is assigned to the initial content node (i.e.
 * where we started).  The top margin of the collapsed children is set to zero.
 * <p/>
 * Collapsing bottom margins works the same way.  The main difference is the collapsed
 * bottom margin is not assigned to the starting block if the next in-flow sibling is
 * also block level (i.e. may participate in another collapsing calculation).  In that
 * case, the bottom margin of the initial block is also set to zero and the in-progress
 * calculation is used as the starting point when the in-flow sibling collapses its
 * top margin.
 * <p/>
 * Some block level elements can be collapsed through (see the spec for details).  Such
 * elements are collapsed and the margin values contribute to the nearest collapsing
 * calculation.  If this cannot be done, the collapsed margin value is (arbitrarily)
 * assigned to the outermost bottom margin.  The top margin is set to zero as are
 * bottom and top margins for the other collapsed nodes nested within the outermost
 * node.
 */
public class VerticalMarginCollapser {

    private static class CollapsingMargin {
        CollapsableContent content;
        float marginWidth;
    }

    private static class CollapsingNode {
        CollapsableContent content;
        float marginTop;
        float marginBottom;
    }

    public static class CollapsedMarginPair {
        private float _maxPositive;
        private float _maxNegative;

        public CollapsedMarginPair() {
        }

        public CollapsedMarginPair(float negative, float positive) {
            super();
            _maxNegative = negative;
            _maxPositive = positive;
        }

        public float getMaxNegative() {
            return _maxNegative;
        }

        public void setMaxNegative(float maxNegative) {
            _maxNegative = maxNegative;
        }

        public float getMaxPositive() {
            return _maxPositive;
        }

        public void setMaxPositive(float maxPositive) {
            _maxPositive = maxPositive;
        }

        public float getValue() {
            return _maxPositive + _maxNegative;
        }
    }

    /**
     * Be sure to use it or call {@link Box#getMarginWidth(Context, float)} to pick
     * up modified margins.
     */
    public static void collapseVerticalMargins(Context c, Box block, Content content, float parentWidth) {
        if (content instanceof CollapsableContent) {
            CollapsableContent collapsableContent = (CollapsableContent) content;

            if (!collapsableContent.isCollapsed()) {
                List adjoining = areMarginsAdjoining(c, collapsableContent, parentWidth);
                if (adjoining != null) {
                    boolean returnImmediately = collapseInBetweenAdjoining(c, block, collapsableContent, adjoining);
                    if (returnImmediately) {
                        return;
                    }
                }
            }

            Style style = block.getStyle();

            if (collapsableContent.isTopMarginCollapsed()) {
                style.setMarginTopOverride(0);
            } else if (mayCollapseInto(c, collapsableContent)) {
                Float collapsedTopMargin = collapseTopMargin(c, collapsableContent, parentWidth);
                if (collapsedTopMargin != null) {
                    style.setMarginTopOverride(collapsedTopMargin.intValue());
                }
            } else {
                Float collapsedTopMargin = calculateCollapsedTop(c, content, parentWidth);
                style.setMarginTopOverride(collapsedTopMargin.intValue());
            }

            if (collapsableContent.isBottomMarginCollapsed()) {
                style.setMarginBottomOverride(0);
            } else if (mayCollapseInto(c, collapsableContent)) {
                Float collapsedBottomMargin = collapseBottomMargin(c, collapsableContent, parentWidth);
                if (collapsedBottomMargin != null) {
                    style.setMarginBottomOverride(collapsedBottomMargin.intValue());
                }
            } else {
                Float collapsedBottomMargin = calculateAdjustedMarginBottom(c, content, parentWidth);
                style.setMarginBottomOverride(collapsedBottomMargin.intValue());
            }
        }
    }

    private static boolean collapseInBetweenAdjoining(Context c, Box block, CollapsableContent content, List adjoining) {
        CollapsedMarginPair result = collapseAdjoining(content.getMarginToCollapse(), adjoining);
        CachingContent parent = (CachingContent) c.getParentContent();
        Content sibling = parent.getNextInFlowSibling(c, content);
        if (sibling != null && sibling instanceof CollapsableContent) {
            ((CollapsableContent) sibling).setMarginToCollapse(result);
            return false;
        } else {
            Style style = block.getStyle();
            style.setMarginTopOverride(0f);
            style.setMarginBottomOverride(result.getValue());
            return true;
        }
    }

    private static boolean hasTopBorderOrPadding(Context c, float parentWidth) {
        Border width = c.getCurrentStyle().getBorderWidth(c.getCtx());
        RectPropertySet padding = c.getCurrentStyle().getPaddingRect(parentWidth, parentWidth, c.getCtx());

        return width.top != 0 || (int)padding.getTopWidth() != 0;
    }

    private static boolean hasBottomBorderOrPadding(Context c, float parentWidth) {
        Border width = c.getCurrentStyle().getBorderWidth(c.getCtx());
        RectPropertySet padding = c.getCurrentStyle().getPaddingRect(parentWidth, parentWidth, c.getCtx());

        return width.bottom != 0 || (int)padding.getBottomWidth() != 0;
    }

    private static Float collapseTopMargin(Context c, CachingContent content, float parentWidth) {
        if (content instanceof BlockContent) {
            if (content.getElement().getParentNode().getNodeType() == Node.DOCUMENT_NODE ||
                    content.getElement().getParentNode().getParentNode().getNodeType() == Node.DOCUMENT_NODE) {
                // don't collapse on html or body
                return null;
            }

            CollapsedMarginPair saved = ((BlockContent) content).getMarginToCollapse();

            if (hasTopBorderOrPadding(c, parentWidth)) {
                return calculateCollapsedTop(c, content, parentWidth);
            }

            List collapsed = new ArrayList();
            collapseTopMarginHelper(c, content, collapsed, parentWidth);

            if (collapsed.size() == 0) {
                return calculateCollapsedTop(c, content, parentWidth);
            } else {
                for (Iterator i = collapsed.iterator(); i.hasNext();) {
                    CollapsingMargin margin = (CollapsingMargin) i.next();
                    margin.content.setTopMarginCollapsed(true);
                }

                CollapsingMargin top = new CollapsingMargin();
                top.content = (CollapsableContent) content;
                top.marginWidth = c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.MARGIN_TOP, parentWidth, c.getCtx());

                collapsed.add(top);

                return new Float(calculateCollapsedMargin(saved, collapsed).getValue());
            }
        } else {
            return null;
        }
    }

    private static void collapseTopMarginHelper(Context c, CachingContent content, List collapsed, float parentWidth) {
        List children = content.getChildContent(c);
        Object target = null;
        for (Iterator i = children.iterator(); i.hasNext();) {
            target = i.next();
            if (ContentUtil.isNotInFlow(target)) {
                continue;
            } else {
                break;
            }
        }

        if (target instanceof CollapsableContent) {
            CollapsableContent targetContent = (CollapsableContent) target;
            float nextParentWidth = adjustWidth(parentWidth, c);

            targetContent =
                    collapseAdjoiningIntervening(c, content, nextParentWidth,
                            targetContent, collapsed, DIR_DOWN);
            if (targetContent == null) {
                return;
            }

            c.pushStyle(targetContent.getStyle());

            CollapsingMargin margin = new CollapsingMargin();
            margin.content = targetContent;
            margin.marginWidth = c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.MARGIN_TOP, nextParentWidth, c.getCtx());

            collapsed.add(margin);

            if (!hasTopBorderOrPadding(c, nextParentWidth) && mayCollapseInto(c, targetContent)) {
                collapseTopMarginHelper(c, targetContent, collapsed, nextParentWidth);
            }

            c.popStyle();
        }
    }

    private static final int DIR_DOWN = 1;
    private static final int DIR_UP = 2;

    private static CollapsableContent collapseAdjoiningIntervening(Context c, CachingContent parent, float parentWidth,
                                                                   CollapsableContent target, List collapsed, int direction) {
        CollapsableContent currentTarget = target;

        while (true) {
            c.pushStyle(currentTarget.getStyle());
            try {
                List adjoining = areMarginsAdjoining(c, currentTarget, parentWidth);

                if (adjoining != null) {
                    CollapsedMarginPair pair = collapseAdjoining(null, adjoining);
                    
                    // bit of a hack, but it works
                    
                    CollapsingMargin top = new CollapsingMargin();
                    top.content = currentTarget;
                    top.marginWidth = pair.getMaxNegative();
                    collapsed.add(top);

                    CollapsingMargin bottom = new CollapsingMargin();
                    bottom.content = currentTarget;
                    bottom.marginWidth = pair.getMaxPositive();
                    collapsed.add(bottom);

                    Object potentialTarget = null;
                    if (direction == DIR_DOWN) {
                        potentialTarget = parent.getNextInFlowSibling(c, currentTarget);
                    } else if (direction == DIR_UP) {
                        potentialTarget = parent.getPreviousInFlowSibling(c, currentTarget);
                    } else {
                        throw new IllegalArgumentException();
                    }

                    if (potentialTarget instanceof CollapsableContent) {
                        currentTarget = (CollapsableContent) potentialTarget;
                    } else {
                        return null;
                    }
                } else {
                    return currentTarget;
                }
            } finally {
                c.popStyle();
            }
        }
    }

    private static Float collapseBottomMargin(Context c, CachingContent content, float parentWidth) {
        if (content instanceof BlockContent) {
            if (content.getElement().getParentNode().getNodeType() == Node.DOCUMENT_NODE ||
                    content.getElement().getParentNode().getParentNode().getNodeType() == Node.DOCUMENT_NODE) {
                // don't collapse on html or body
                return null;
            }

            if (hasBottomBorderOrPadding(c, parentWidth)) {
                return calculateAdjustedMarginBottom(c, content, parentWidth);
            }

            List collapsed = new ArrayList();
            collapseBottomMarginHelper(c, content, collapsed, parentWidth);

            if (collapsed.size() == 0) {
                return calculateAdjustedMarginBottom(c, content, parentWidth);
            } else {
                for (Iterator i = collapsed.iterator(); i.hasNext();) {
                    CollapsingMargin margin = (CollapsingMargin) i.next();
                    margin.content.setBottomMarginCollapsed(true);
                }

                CollapsingMargin top = new CollapsingMargin();
                top.content = (CollapsableContent) content;
                top.marginWidth = c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.MARGIN_BOTTOM, parentWidth, c.getCtx());

                collapsed.add(top);

                return calculateAdjustedMarginBottom(c, collapsed);
            }
        } else {
            return null;
        }
    }

    private static Float calculateAdjustedMarginBottom(Context c, Content topContent, float parentWidth) {
        CollapsingMargin top = new CollapsingMargin();
        top.content = (CollapsableContent) topContent;
        top.marginWidth = c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.MARGIN_BOTTOM, parentWidth, c.getCtx());
        return calculateAdjustedMarginBottom(c, Collections.singletonList(top));
    }

    private static Float calculateAdjustedMarginBottom(Context c, List collapsed) {
        CachingContent parent = (CachingContent) c.getParentContent();
        CollapsingMargin which = (CollapsingMargin) collapsed.get(collapsed.size() - 1);

        Content sibling = parent.getNextInFlowSibling(c, which.content);

        if (sibling instanceof CollapsableContent) {
            ((CollapsableContent) sibling).setMarginToCollapse(calculateCollapsedMargin(null, collapsed));
            ((CollapsableContent) which.content).setBottomMarginCollapsed(true);
            return new Float(0f);
        } else {
            return new Float(calculateCollapsedMargin(null, collapsed).getValue());
        }
    }

    private static void collapseBottomMarginHelper(Context c, CachingContent content, List collapsed, float parentWidth) {
        List children = content.getChildContent(c);
        Object target = null;

        for (int i = children.size() - 1; i >= 0; i--) {
            target = children.get(i);
            if (ContentUtil.isNotInFlow(target)) {
                continue;
            } else {
                break;
            }
        }

        if (target instanceof CollapsableContent) {
            CollapsableContent targetContent = (CollapsableContent) target;
            float nextParentWidth = adjustWidth(parentWidth, c);

            targetContent = collapseAdjoiningIntervening(c, content, parentWidth, targetContent, collapsed, DIR_UP);
            if (targetContent == null) {
                return;
            }

            c.pushStyle(targetContent.getStyle());

            CollapsingMargin margin = new CollapsingMargin();
            margin.content = targetContent;

            margin.marginWidth = c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.MARGIN_TOP, nextParentWidth, c.getCtx());

            collapsed.add(margin);

            if (!hasBottomBorderOrPadding(c, nextParentWidth)
                    && mayCollapseInto(c, targetContent)) {
                collapseBottomMarginHelper(c, targetContent, collapsed, nextParentWidth);
            }

            c.popStyle();
        }
    }

    public static boolean mayCollapseInto(Context c, CollapsableContent content) {
        return content.mayCollapseInto() && c.getCurrentStyle().isIdent(CSSName.OVERFLOW, IdentValue.VISIBLE);
    }

    private static Float calculateCollapsedTop(Context c, Content topContent, float parentWidth) {
        CollapsingMargin top = new CollapsingMargin();
        top.content = (CollapsableContent) topContent;
        top.marginWidth = c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.MARGIN_TOP, parentWidth, c.getCtx());

        return new Float(calculateCollapsedMargin(((CollapsableContent) topContent).getMarginToCollapse(),
                Collections.singletonList(top)).getValue());
    }

    private static CollapsedMarginPair calculateCollapsedMargin(CollapsedMarginPair pair, List collapsed) {
        float maxPositive = 0;
        float maxNegative = 0;

        if (pair != null) {
            maxPositive = pair.getMaxPositive();
            maxNegative = pair.getMaxNegative();
        }

        for (Iterator i = collapsed.iterator(); i.hasNext();) {
            CollapsingMargin margin = (CollapsingMargin) i.next();

            if (margin.marginWidth > 0 && margin.marginWidth > maxPositive) {
                maxPositive = margin.marginWidth;
            } else if (margin.marginWidth < 0 && margin.marginWidth < maxNegative) {
                maxNegative = margin.marginWidth;
            }
        }
        return new CollapsedMarginPair(maxNegative, maxPositive);
    }

    private static CollapsedMarginPair collapseAdjoining(CollapsedMarginPair pair, List nodes) {
        float maxPositive = 0;
        float maxNegative = 0;

        if (pair != null) {
            maxPositive = pair.getMaxPositive();
            maxNegative = pair.getMaxNegative();
        }

        for (Iterator i = nodes.iterator(); i.hasNext();) {
            CollapsingNode node = (CollapsingNode) i.next();

            node.content.setBottomMarginCollapsed(true);
            node.content.setTopMarginCollapsed(true);
            node.content.setCollapsed(true);

            if (node.marginTop > 0 && node.marginTop > maxPositive) {
                maxPositive = node.marginTop;
            } else if (node.marginTop < 0 && node.marginTop < maxNegative) {
                maxNegative = node.marginTop;
            }

            if (node.marginBottom > 0 && node.marginBottom > maxPositive) {
                maxPositive = node.marginBottom;
            } else if (node.marginBottom < 0 && node.marginBottom < maxNegative) {
                maxNegative = node.marginBottom;
            }
        }

        return new CollapsedMarginPair(maxNegative, maxPositive);
    }

    private static float adjustWidth(float parentWidth, Context c) {
        CalculatedStyle style = c.getCurrentStyle();
        boolean hasSpecifiedWidth = !style.isIdent(CSSName.WIDTH, IdentValue.AUTO);
        if (hasSpecifiedWidth) {
            return style.getFloatPropertyProportionalWidth(CSSName.WIDTH, parentWidth, c.getCtx());
        } else {
            RectPropertySet margin = style.getMarginRect(parentWidth, parentWidth, c.getCtx());
            RectPropertySet padding = style.getPaddingRect(parentWidth, parentWidth, c.getCtx());
            Border borderWidth = style.getBorderWidth(c.getCtx());
            return parentWidth -
                    margin.getLeftWidth() - borderWidth.left - (int)padding.getLeftWidth() -
                    (int)padding.getRightWidth() - borderWidth.right - margin.getRightWidth();
        }
    }

    private static List areMarginsAdjoining(Context c, CollapsableContent content, float parentWidth) {
        return areMarginsAdjoiningHelper(c, content, parentWidth, new ArrayList());
    }

    private static List areMarginsAdjoiningHelper(Context c, CollapsableContent content, float parentWidth, List adjoining) {

        if (content.isMarginsAdjoiningCalculated() && !content.isMarginsAdjoining()) {
            // Fast path for common case, otherwise just recalculate
            return null;
        }

        List children = content.getChildContent(c);
        boolean b = hasNoBordersPaddingOrHeight(c, parentWidth);

        CollapsingNode n = new CollapsingNode();
        n.content = content;
        n.marginTop = c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.MARGIN_TOP, parentWidth, c.getCtx());
        n.marginBottom = c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.MARGIN_BOTTOM, parentWidth, c.getCtx());
        adjoining.add(n);

        if (!b) {
            return returnWithSave(content, null);
        } else if (children.size() == 0) {
            return returnWithSave(content, adjoining);
        }

        for (Iterator i = children.iterator(); i.hasNext();) {
            Object obj = (Object) i.next();
            if (ContentUtil.isNotInFlow(obj)) {
                continue;
            }

            if (!(obj instanceof CollapsableContent)) {
                return returnWithSave(content, null);
            }

            CollapsableContent childContent = (CollapsableContent) obj;
            float width = adjustWidth(parentWidth, c);
            c.pushStyle(childContent.getStyle());
            try {
                adjoining = areMarginsAdjoiningHelper(c, childContent, width, adjoining);

                if (adjoining == null) {
                    return returnWithSave(content, null);
                }
            } finally {
                c.popStyle();
            }
        }

        return returnWithSave(content, adjoining);
    }

    private static List returnWithSave(CollapsableContent content, List result) {
        content.setMarginsAdjoiningCalculated(true);
        content.setMarginsAdjoining(result != null);
        return result;
    }

    private static boolean hasNoBordersPaddingOrHeight(Context c, float parentWidth) {
        CalculatedStyle style = c.getCurrentStyle();
        Border borderWidth = style.getBorderWidth(c.getCtx());
        RectPropertySet padding = style.getPaddingRect(parentWidth, parentWidth, c.getCtx());

        return borderWidth.top == 0 && borderWidth.bottom == 0 &&
                (int)padding.getTopWidth() == 0 && (int)padding.getBottomWidth() == 0 &&
                (style.isIdent(CSSName.HEIGHT, IdentValue.AUTO) ||
                style.asFloat(CSSName.HEIGHT) == 0);
    }
}
