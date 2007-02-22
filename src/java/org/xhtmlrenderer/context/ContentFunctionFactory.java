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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.extend.ContentFunction;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.RenderingContext;

public class ContentFunctionFactory {
    private List _functions = new ArrayList();
    
    {
        _functions.add(new PageCounterFunction());
        _functions.add(new PagesCounterFunction());
    }
    
    public ContentFunction lookupFunction(LayoutContext c, FSFunction function) {
        for (Iterator i = _functions.iterator(); i.hasNext(); ) {
            ContentFunction f = (ContentFunction)i.next();
            if (f.canHandle(c, function)) {
                return f;
            }
        }
        return null;
    }
    
    public void registerFunction(ContentFunction function) {
        _functions.add(function);
    }
    
    private static abstract class PageNumberFunction implements ContentFunction {
        public boolean isStatic() {
            return false;
        }
        
        public String calculate(LayoutContext c, FSFunction function) {
            return null;
        }
        
        public String getLayoutReplacementText() {
            return "999";
        }
        
        protected boolean isCounter(FSFunction function, String counterName) {
            if (function.getName().equals("counter")) {
                List parameters = function.getParameters();
                // XXX Not correct, since a counter may have a style parameter too
                // but we don't have generic counter support (yet)
                if (parameters.size() == 1) {
                    PropertyValue param = (PropertyValue)parameters.get(0);
                    if (param.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT &&
                            param.getStringValue().equals(counterName)) {
                        return true;
                    }
                }
            }
            
            return false;
        }
    }
    
    private static class PageCounterFunction extends PageNumberFunction implements ContentFunction {
        public String calculate(RenderingContext c, FSFunction function, InlineText text) {
            return Integer.toString(c.getPageNo() + 1);
        }
        
        public boolean canHandle(LayoutContext c, FSFunction function) {
            return c.isPrint() && isCounter(function, "page");
        }
    }
    
    private static class PagesCounterFunction extends PageNumberFunction implements ContentFunction {
        public String calculate(RenderingContext c, FSFunction function, InlineText text) {
            return Integer.toString(c.getPageCount());
        }

        public boolean canHandle(LayoutContext c, FSFunction function) {
            return c.isPrint() && isCounter(function, "pages");
        }
    }
}
