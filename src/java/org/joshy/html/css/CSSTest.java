
/* 
 * {{{ header & license 
 * Copyright (c) 2004 Joshua Marinacci 
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

package org.joshy.html.css;

import org.joshy.*;
import java.io.*;

import com.steadystate.css.*;
import com.steadystate.css.parser.*;
import org.w3c.dom.*;
import org.w3c.dom.css.*;
import org.w3c.css.sac.*;

public class CSSTest {

    public static void main(String[] args) throws Exception {
        Reader r = new FileReader("test.css");
        //CSS2Parser parser = new CSS2Parser(r);
        CSSOMParser parser = new CSSOMParser();
        InputSource is = new InputSource(r);
        CSSStyleSheet stylesheet = parser.parseStyleSheet(is);
        //u.p("stylesheet = " + stylesheet);
        
        String css_text = "p { color: #f0f0f0; }";
        stylesheet.insertRule(css_text,stylesheet.getCssRules().getLength());
        stylesheet.insertRule("@import \"second.css\";",0);
        u.p("stylesheet = " + stylesheet);
        u.p("title = " + stylesheet.getTitle());
        u.p("type = " + stylesheet.getType());
        CSSRule rule = stylesheet.getCssRules().item(0);
        u.p("first rule = " + rule);
        CSSImportRule impru = (CSSImportRule)rule;
        u.p("sub sheet = " + impru.getStyleSheet());
        
        /*
        CSSRuleList list = stylesheet.getCssRules();
        for(int i=0; i<list.getLength(); i++) {
            CSSRule rule = list.item(i);
            u.p("rule = " + rule);
            if(rule.getType() == rule.STYLE_RULE) {
                CSSStyleRule style = (CSSStyleRule)rule;
                u.p("selector = " + style.getSelectorText());
                CSSStyleDeclaration decl = style.getStyle();
                u.p("decl = " + decl);
                for(int j=0; j<decl.getLength(); j++) {
                    u.p("item = " + decl.item(j));
                    u.p("value = " + decl.getPropertyCSSValue("color"));
                }
            }
        }
        */
    }

}
