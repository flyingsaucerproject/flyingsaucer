/*
 *
 * Stylesheet.java
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

package net.homelinux.tobe.xhtmlrenderer.bridge;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class Stylesheet implements net.homelinux.tobe.xhtmlrenderer.Stylesheet {
    
    final org.w3c.dom.css.CSSStyleSheet _sheet;
    int _origin;
    java.util.List _rulesets;
    
    /** Creates a new instance of Stylesheet */
    public Stylesheet(org.w3c.dom.css.CSSStyleSheet sheet, int origin) {
        _sheet = sheet;
        _origin = origin;
        _rulesets = new java.util.LinkedList();
        pullRulesets();
    }
    
    private void pullRulesets() {
         org.w3c.dom.css.CSSRuleList rl = _sheet.getCssRules();
         int nr = rl.getLength();
         for(int i = 0; i < nr; i++) {
             if(rl.item(i).getType() != org.w3c.dom.css.CSSRule.STYLE_RULE) continue;
             _rulesets.add(new Ruleset((org.w3c.dom.css.CSSStyleRule) rl.item(i), getOrigin()));
         }
        
    }
    
    public int getOrigin() {
                return _origin;
    }
    
    public java.util.Iterator getRulesets() {
        return _rulesets.iterator();
    }
    
}
