/*
 *
 * StylesheetFactory.java
 * Copyright (c) 2004 Torbjörn Gannholm
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
 *
 */

package net.homelinux.tobe.renderer.css;

import com.steadystate.css.parser.CSSOMParser;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSStyleRule;

import net.homelinux.tobe.xhtmlrenderer.bridge.Stylesheet;
import net.homelinux.tobe.xhtmlrenderer.bridge.Ruleset;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class StylesheetFactory implements net.homelinux.tobe.xhtmlrenderer.StylesheetFactory {
    
    private CSSOMParser parser = new CSSOMParser();
    
    private int _cacheCapacity = 16;
    //an LRU cache
    private java.util.LinkedHashMap _cache = new java.util.LinkedHashMap(_cacheCapacity, 0.75f, true) {
        protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
            return size() > _cacheCapacity;
        }
    };
    
    /** Creates a new instance of StylesheetFactory */
    public StylesheetFactory() {
    }
    
    public net.homelinux.tobe.xhtmlrenderer.Stylesheet getStylesheet(Object key) {
        return (net.homelinux.tobe.xhtmlrenderer.Stylesheet) _cache.get(key);
    }
    
    public net.homelinux.tobe.xhtmlrenderer.Stylesheet parse(int origin, java.io.Reader reader) {
            InputSource is = new InputSource( reader );
            CSSStyleSheet style = null;
            try {
                style = parser.parseStyleSheet( is );
            }
            catch(java.io.IOException e) {
                
            }

        return new Stylesheet(style, origin);
    }
    
    public net.homelinux.tobe.xhtmlrenderer.Ruleset parseStyleDeclaration(int origin, String styleDeclaration) {
            try {
                java.io.StringReader reader = new java.io.StringReader("* {"+styleDeclaration+"}");
                InputSource is = new InputSource( reader );
                CSSStyleSheet style = parser.parseStyleSheet( is );
                reader.close();
                return new Ruleset((CSSStyleRule) style.getCssRules().item(0), net.homelinux.tobe.xhtmlrenderer.Stylesheet.AUTHOR);
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
            return null;
    }
    
    public void putStylesheet(Object key, net.homelinux.tobe.xhtmlrenderer.Stylesheet sheet) {
        _cache.put(key, sheet);
    }
    
}
