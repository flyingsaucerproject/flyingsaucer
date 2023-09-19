/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
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
package org.xhtmlrenderer.context;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.extend.ContentFunction;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.layout.CounterFunction;
import org.xhtmlrenderer.layout.InlineBoxing;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.render.RenderingContext;

import java.util.ArrayList;
import java.util.List;

public class ContentFunctionFactory {
    private final List<ContentFunction> _functions = new ArrayList<>();

    {
        _functions.add(new PageCounterFunction());
        _functions.add(new PagesCounterFunction());
        _functions.add(new TargetCounterFunction());
        _functions.add(new LeaderFunction());
    }

    public ContentFunction lookupFunction(LayoutContext c, FSFunction function) {
        for (ContentFunction f : _functions) {
            if (f.canHandle(c, function)) {
                return f;
            }
        }
        return null;
    }

    public void registerFunction(ContentFunction function) {
        _functions.add(function);
    }

    private abstract static class PageNumberFunction implements ContentFunction {
        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public String calculate(LayoutContext c, FSFunction function) {
            return null;
        }

        @Override
        public String getLayoutReplacementText() {
            return "999";
        }

        protected IdentValue getListStyleType(FSFunction function) {
            IdentValue result = IdentValue.DECIMAL;

            List<PropertyValue> parameters = function.getParameters();
            if (parameters.size() == 2) {
                PropertyValue pValue = parameters.get(1);
                IdentValue iValue = IdentValue.valueOf(pValue.getStringValue());
                if (iValue != null) {
                    result = iValue;
                }
            }

            return result;
        }

        protected boolean isCounter(FSFunction function, String counterName) {
            if (function.getName().equals("counter")) {
                List<PropertyValue> parameters = function.getParameters();
                if (parameters.size() == 1 || parameters.size() == 2) {
                    PropertyValue param = parameters.get(0);
                    if (param.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT ||
                            ! param.getStringValue().equals(counterName)) {
                        return false;
                    }

                    if (parameters.size() == 2) {
                        param = parameters.get(1);
                        return param.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT;
                    }

                    return true;
                }
            }

            return false;
        }
    }

    private static class PageCounterFunction extends PageNumberFunction implements ContentFunction {
        @Override
        public String calculate(RenderingContext c, FSFunction function, InlineText text) {
            int value = c.getRootLayer().getRelativePageNo(c) + 1;
            return CounterFunction.createCounterText(getListStyleType(function), value);
        }

        @Override
        public boolean canHandle(LayoutContext c, FSFunction function) {
            return c.isPrint() && isCounter(function, "page");
        }
    }

    private static class PagesCounterFunction extends PageNumberFunction implements ContentFunction {
        @Override
        public String calculate(RenderingContext c, FSFunction function, InlineText text) {
            int value = c.getRootLayer().getRelativePageCount(c);
            return CounterFunction.createCounterText(getListStyleType(function), value);
        }

        @Override
        public boolean canHandle(LayoutContext c, FSFunction function) {
            return c.isPrint() && isCounter(function, "pages");
        }
    }

    /**
     * Partially implements target counter as specified here:
     * <a href="http://www.w3.org/TR/2007/WD-css3-gcpm-20070504/#cross-references">...</a>
     */
    private static class TargetCounterFunction implements ContentFunction {
        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public String calculate(RenderingContext c, FSFunction function, InlineText text) {
            String uri = text.getParent().getElement().getAttribute("href");
            if (uri.startsWith("#")) {
                String anchor = uri.substring(1);
                Box target = c.getBoxById(anchor);
                if (target != null) {
                    int pageNo = c.getRootLayer().getRelativePageNo(c, target.getAbsY());
                    return CounterFunction.createCounterText(IdentValue.DECIMAL, pageNo + 1);
                }
            }
            return "";
        }

        @Override
        public String calculate(LayoutContext c, FSFunction function) {
            return null;
        }

        @Override
        public String getLayoutReplacementText() {
            return "999";
        }

        @Override
        public boolean canHandle(LayoutContext c, FSFunction function) {
            if (c.isPrint() && function.getName().equals("target-counter")) {
                List<PropertyValue> parameters = function.getParameters();
                if (parameters.size() == 2 || parameters.size() == 3) {
                    FSFunction f = parameters.get(0).getFunction();
                    if (f == null ||
                            f.getParameters().size() != 1 ||
                            f.getParameters().get(0).getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT ||
                            ! f.getParameters().get(0).getStringValue().equals("href")) {
                        return false;
                    }

                    PropertyValue param = parameters.get(1);
                    return param.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT &&
                            param.getStringValue().equals("page");
                }
            }

            return false;
        }
    }

    /**
     * Partially implements leaders as specified here:
     * <a href="http://www.w3.org/TR/2007/WD-css3-gcpm-20070504/#leaders">...</a>
     */
    private static class LeaderFunction implements ContentFunction {
        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public String calculate(RenderingContext c, FSFunction function, InlineText text) {
            InlineLayoutBox iB = text.getParent();
            LineBox lineBox = iB.getLineBox();

            // There might be a target-counter function after this function.
            // Because the leader should fill up the line, we need the correct
            // width and must first compute the target-counter function.
            boolean dynamic = false;
            for (Box child : lineBox.getChildren()) {
                if (child == iB) {
                    dynamic = true;
                } else if (dynamic && child instanceof InlineLayoutBox) {
                    ((InlineLayoutBox) child).lookForDynamicFunctions(c);
                }
            }
            if (dynamic) {
                int totalLineWidth = InlineBoxing.positionHorizontally(c, lineBox, 0);
                lineBox.setContentWidth(totalLineWidth);
            }

            // Get leader value and value width
            PropertyValue param = function.getParameters().get(0);
            String value = param.getStringValue();
            if (param.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                if (value.equals("dotted")) {
                    value = ". ";
                } else if (value.equals("solid")) {
                    value = "_";
                } else if (value.equals("space")) {
                    value = " ";
                }
            }

            // Compute value width using 100x string to get more precise width.
            // Otherwise, there might be a small gap on the right side. This is
            // necessary because a TextRenderer usually use double/float for width.
            StringBuilder tmp = new StringBuilder(100 * value.length());
            for (int i = 0; i < 100; i++) {
                tmp.append(value);
            }
            float valueWidth = c.getTextRenderer().getWidth(c.getFontContext(),
                    iB.getStyle().getFSFont(c), tmp.toString()) / 100.0f;
            int spaceWidth = c.getTextRenderer().getWidth(c.getFontContext(),
                    iB.getStyle().getFSFont(c), " ");

            // compute leader width and necessary count of values
            int leaderWidth = iB.getContainingBlockWidth() - iB.getLineBox().getWidth() + text.getWidth();
            int count = (int) ((leaderWidth - (2 * spaceWidth)) / valueWidth);

            // build leader string
            StringBuilder buf = new StringBuilder(count * value.length() + 2);
            buf.append(' ');
            for (int i = 0; i < count; i++) {
                buf.append(value);
            }
            buf.append(' ');
            String leaderString = buf.toString();

            // set left margin to ensure that the leader is right aligned (for TOC)
            int leaderStringWidth = c.getTextRenderer().getWidth(c.getFontContext(),
                    iB.getStyle().getFSFont(c), leaderString);
            iB.setMarginLeft(c, leaderWidth - leaderStringWidth);

            return leaderString;
        }

        @Override
        public String calculate(LayoutContext c, FSFunction function) {
            return null;
        }

        @Override
        public String getLayoutReplacementText() {
            return " . ";
        }

        @Override
        public boolean canHandle(LayoutContext c, FSFunction function) {
            if (c.isPrint() && function.getName().equals("leader")) {
                List<PropertyValue> parameters = function.getParameters();
                if (parameters.size() == 1) {
                    PropertyValue param = parameters.get(0);
                    if (param.getPrimitiveType() != CSSPrimitiveValue.CSS_STRING &&
                            (param.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT ||
                                (!param.getStringValue().equals("dotted") &&
                                        !param.getStringValue().equals("solid") &&
                                        !param.getStringValue().equals("space")))) {
                        return false;
                    }

                    return true;
                }
            }

            return false;
        }
    }
}
