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

import org.xhtmlrenderer.css.extend.ContentFunction;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;

public class ContentFunctionFactory {
    private List _functions = new ArrayList();
    
    {
        _functions.add(new PageCounterFunction());
        _functions.add(new PagesCounterFunction());
        _functions.add(new FSCurrentPageFunction());
        _functions.add(new FSPageRangeFunction());
    }
    
    public ContentFunction lookupFunction(LayoutContext c, String declaration) {
        for (Iterator i = _functions.iterator(); i.hasNext(); ) {
            ContentFunction function = (ContentFunction)i.next();
            if (function.canHandle(c, declaration)) {
                return function;
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
        
        public String calculate(LayoutContext c, String declaration) {
            return null;
        }
        
        public String getLayoutReplacementText() {
            return "999";
        }
    }
    
    private static class PageCounterFunction extends PageNumberFunction implements ContentFunction {
        public String calculate(RenderingContext c, String declaration, InlineText text) {
            return Integer.toString(c.getPageNo() + 1);
        }
        
        public boolean canHandle(LayoutContext c, String declaration) {
            return c.isPrint() && declaration.equals("counter(page)");
        }
    }
    
    private static class PagesCounterFunction extends PageNumberFunction implements ContentFunction {
        public String calculate(RenderingContext c, String declaration, InlineText text) {
            return Integer.toString(c.getPageCount());
        }

        public boolean canHandle(LayoutContext c, String declaration) {
            return c.isPrint() && declaration.equals("counter(pages)");
        }
    }
    
    private static class FSCurrentPageFunction extends PageNumberFunction implements ContentFunction {
        public String calculate(RenderingContext c, String declaration, InlineText text) {
            String id = declaration.substring(
                    "-fs-current-page(".length(), declaration.length() - 1).trim();
            Box b = c.getIDBox(id);
            if (b == null) {
                return "";
            } else {
                Layer root = text.getParent().getContainingLayer().findRoot();
                PageBox pageBox = root.getFirstPage(c, b);
                return Integer.toString(c.getPageNo() - pageBox.getPageNo() + 1);
            }
        }
        
        public boolean canHandle(LayoutContext c, String declaration) {
            return c.isPrint() && declaration.startsWith("-fs-current-page(");
        }
    }
    
    private static class FSPageRangeFunction extends PageNumberFunction implements ContentFunction {
        public String calculate(RenderingContext c, String declaration, InlineText text) {
            String id = declaration.substring(
                    "-fs-page-range(".length(), declaration.length() - 1).trim();
            Box b = c.getIDBox(id);
            if (b == null) {
                return "";
            } else {
                Layer root = text.getParent().getContainingLayer().findRoot();
                PageBox first = root.getFirstPage(c, b);
                PageBox last = root.getLastPage(c, b);
                return Integer.toString(last.getPageNo() - first.getPageNo() + 1);
            }
        }
        
        public boolean canHandle(LayoutContext c, String declaration) {
            return c.isPrint() && declaration.startsWith("-fs-page-range(");
        }
    }
}
