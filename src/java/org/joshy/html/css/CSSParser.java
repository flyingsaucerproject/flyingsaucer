package org.joshy.html.css;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;
import java.awt.Color;
import org.joshy.u;
import org.joshy.x;
import org.joshy.html.*;

import com.steadystate.css.*;
import com.steadystate.css.parser.*;
import org.w3c.dom.*;
import org.w3c.dom.css.*;
import org.w3c.css.sac.*;

public class CSSParser {
    CSSBank bank;
    public CSSParser(CSSBank bank) {
        this.bank = bank;
        init_color_map();
    }
    
    
    /* parsing and sorting */
    public void parse(Reader reader) throws IOException {
        CSSOMParser parser = new CSSOMParser();
        InputSource is = new InputSource(reader);
        CSSStyleSheet style = parser.parseStyleSheet(is);
        //u.p("got style sheet: " + style);
        bank.sheets.add(style);
        this.pullOutStyles(style);
    }
    
    
    public void parseInlineStyles(Element elem) throws IOException {
        // if this is a style node
        if(elem.getNodeName().equals("style")) {
            // check if we've already imported it
            if(!bank.style_nodes.contains(elem)) {
                // import the style
                CSSOMParser parser = new CSSOMParser();
                CSSStyleSheet style = parser.parseStyleSheet(new InputSource(new StringReader(x.text(elem))));
                // save the new style to the list
                bank.sheets.add(style);
                //u.p("parsed: " + style);
                // add this node to the imported list
                bank.style_nodes.add(elem);
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
                bank.styles.add(style_holder);
            }
        }
    }

    public CSSStyleRule normalize(CSSStyleRule rule) {
        for(int i=0; i<rule.getStyle().getLength(); i++) {
            String prop = rule.getStyle().item(i);
            if(prop.equals("color") ||
                prop.equals("background-color") ||
                prop.equals("border-color")) {
                    String value =  rule.getStyle().getPropertyValue(prop);
                    rule.getStyle().setProperty(prop,getColorHex(value),null);
            }
            if(prop.equals("padding")) {
                expand(prop,rule);
            }
            if(prop.equals("margin")) {
                expand(prop,rule);
            }
            if(prop.equals("border-width")) {
                expand("border-width",rule,"border","-width");
            }
            if(prop.equals("background-position")) {
                expandBackgroundPosition(rule);
            }
        }
        return rule;
    }
    
    public void expand(String prop, CSSStyleRule rule) {
        expand(prop,rule,prop,"");
    }
    
    public void expandBackgroundPosition(CSSStyleRule rule) {
        CSSStyleDeclaration dec = rule.getStyle();
        CSSValue val = dec.getPropertyCSSValue("background-position");
        if(val.getCssValueType() == val.CSS_VALUE_LIST) {
            //u.p("val = " + val);
            return;
        }
        if(val.getCssValueType() == val.CSS_PRIMITIVE_VALUE) {
            //u.p("val = " + val);
            String str = val.getCssText();
            if(str.startsWith("top")) { dec.setProperty("background-position","50% 0%",null); }
            if(str.startsWith("bottom")) { dec.setProperty("background-position","50% 100%",null); }
            if(str.startsWith("left")) { dec.setProperty("background-position","0% 50%",null); }
            if(str.startsWith("right")) { dec.setProperty("background-position","100% 50%",null); }
            return;
        }

    }
    
    public void expand(String prop, CSSStyleRule rule, String before, String after) {
        //u.p("rule = " + rule);
        //u.p("prop = " + prop);
        CSSStyleDeclaration dec = rule.getStyle();
        CSSValue val = dec.getPropertyCSSValue(prop);
        //u.p("value = " + val);
        CSSValueList list = (CSSValueList)val;
        CSSValue top, bottom, left, right;
        top = bottom = left = right = null;

        if(val.getCssValueType() == val.CSS_VALUE_LIST) {
            if(list.getLength() == 2) {
                //u.p("turning two into four");
                top = list.item(0);
                bottom = list.item(0);
                left = list.item(1);
                right = list.item(1);
            }
            if(list.getLength() == 3) {
                //u.p("turning three into four");
                top = list.item(0);
                left = list.item(1);
                right = list.item(1);
                bottom = list.item(2);
            }
            if(list.getLength() == 4) {
                //u.p("turning three into four");
                top = list.item(0);
                right = list.item(1);
                bottom = list.item(2);
                left = list.item(3);
            }
        } else {
            //u.p("only one to transform");
            top = list;
            bottom = list;//.item(0);
            left = list;//.item(0);
            right = list;//.item(0);
        }
        dec.setProperty(before+"-top"+after,top.getCssText(),null);
        dec.setProperty(before+"-bottom"+after,bottom.getCssText(),null);
        dec.setProperty(before+"-left"+after,left.getCssText(),null);
        dec.setProperty(before+"-right"+after,right.getCssText(),null);
    }
    
    public String getColorHex(String value) {
        if(value.indexOf("rgb")>=0) {
            return value;
        }
        String retval = (String)color_map.get(value.toLowerCase());
        return retval;
    }
    
        
    Map color_map;
    private void init_color_map() {
        color_map = new HashMap();
        color_map.put("black","#000000");
        color_map.put("white","#FFFFFF");
        color_map.put("red","#FF0000");
        color_map.put("yellow","#FFFF00");
        color_map.put("lime","#00ff00");
        color_map.put("aqua","#00ffff");
        color_map.put("blue","#0000ff");
        color_map.put("fuchsia","#ff00ff");
        color_map.put("gray","#808080");
        color_map.put("silver","#c0c0c0");
        color_map.put("maroon","#800000");
        color_map.put("olive","#808000");
        color_map.put("green","#008000");
        color_map.put("teal","#008080");
        color_map.put("navy","#000080");
        color_map.put("purple","#800080");
    }
    
    
    
}
