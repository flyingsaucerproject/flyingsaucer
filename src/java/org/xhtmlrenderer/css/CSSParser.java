
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

package org.xhtmlrenderer.css;



import java.util.Map;

import java.util.HashMap;

import java.util.List;

import java.util.ArrayList;

import java.util.Iterator;

import java.io.*;

import java.awt.Color;

import org.xhtmlrenderer.layout.*;



import com.steadystate.css.*;

import com.steadystate.css.parser.*;

import org.w3c.dom.*;

import org.w3c.dom.css.*;

import org.w3c.css.sac.*;



public class CSSParser {

    // CLN: changed to use RuleBank interface instead (PWW 13/08/04)
    // CSSBank bank;
    RuleBank bank;

    RuleNormalizer normalizer;

    // CLN: change to RuleBank (PWW 13/08/04)
    public CSSParser(RuleBank bank) {

        this.bank = bank;

        this.normalizer = new RuleNormalizer();

    }

    

    

    /* parsing and sorting */

    public void parse(Reader reader) throws IOException {

        CSSOMParser parser = new CSSOMParser();

        InputSource is = new InputSource(reader);

        CSSStyleSheet style = parser.parseStyleSheet(is);

        //u.p("got style sheet: " + style);

        // CLN: PWW
        // bank.sheets.add(style);

        this.pullOutStyles(style);

    }

    public void parse(String reader) throws IOException {

        CSSOMParser parser = new CSSOMParser();

        InputSource is = new InputSource(reader);

        CSSStyleSheet style = parser.parseStyleSheet(is);

        //u.p("got style sheet: " + style);

        // CLN: PWW
        // bank.sheets.add(style);

        this.pullOutStyles(style);

    }

    

    

    public void parseInlineStyles(Element elem) throws IOException {

        // if this is a style node

        if(elem.getNodeName().equals("style")) {

            // check if we've already imported it

            // CLN: PWW
            // if(!bank.style_nodes.contains(elem)) {
            if(!bank.wasElementParsed(elem)) {

                // import the style

                CSSOMParser parser = new CSSOMParser();

                CSSStyleSheet style = parser.parseStyleSheet(new InputSource(new StringReader(org.xhtmlrenderer.util.x.text(elem))));

                // save the new style to the list

                // CLN: PWW
                // bank.sheets.add(style);

                //u.p("parsed: " + style);

                // add this node to the imported list

                //bank.style_nodes.add(elem);
                bank.elementWasParsed(elem);

                this.pullOutStyles(style);

            }

        }

        // do all of the children

        NodeList nl = elem.getChildNodes();

        for(int i=0; i<nl.getLength(); i++) {

            Node n = nl.item(i);

            if(n.getNodeType() == n.ELEMENT_NODE) {

                parseInlineStyles((Element)n);

            }

        }

    }

    

    /*

    

    */

    public void pullOutStyles(CSSStyleSheet sheet) throws IOException {

        //u.p("pull out styles");

        CSSRuleList rules = sheet.getCssRules();

        for(int i=0; i<rules.getLength(); i++) {

            CSSRule rule = rules.item(i);

            if(rule.getType() == rule.STYLE_RULE) {

                CSSStyleRule style_rule = (CSSStyleRule) rule;

                style_rule = normalize(style_rule);

                JStyle style_holder = new JStyle();

                style_holder.rule = style_rule;

                style_holder.sheet = sheet;

                String selector = style_rule.getSelectorText();

                CSSOMParser parser = new CSSOMParser();

                SelectorList list = parser.parseSelectors(new InputSource(new StringReader(selector)));

                style_holder.selector_list = list;

                style_holder.declaration = style_rule.getStyle();

                // CLN: PWW
                // bank.rule_bank.addRule(style_holder);
                bank.addStyleRule(style_holder);

            }

        }

    }



    public CSSStyleRule normalize(CSSStyleRule rule) {

        return normalizer.normalize(rule);

    }

    

    

    

    

}

