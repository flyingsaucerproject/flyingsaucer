
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

import org.w3c.dom.Element;
import com.steadystate.css.*;
import com.steadystate.css.parser.*;
import org.w3c.dom.css.*;
import org.w3c.css.sac.*;
import java.util.*;
import java.util.logging.*;

/**

This is a rule bank implementation that reuses some of the ideas from
Mozilla's CSS implementation, or at least as of the type of the Next Gen
Layout renderer.  <a href="http://mozilla.org/newlayout/doc/style-system.html">see here</a>.

Basically it presorts things into a set of hashtables depending on whether or
not the rule has ids, classes, or just elements in it. Currently this is just
a stub that does nothing.

*/

public class MozRuleBank implements RuleBank {
    public static Logger logger = Logger.getLogger("css");

    private Map id_map, class_map, element_map, all_map;
    private RuleFinder rb = new RuleFinder();
    
    private List parsedElements;
    
    public MozRuleBank() {
        id_map = new HashMap();
        class_map = new HashMap();
        element_map = new HashMap();
        all_map = new HashMap();
        parsedElements = new ArrayList();
    }
    
    /** CLN: added (PWW 13/08/04)
     * replaces style_nodes.contains(elem);
    */
    public boolean wasElementParsed(Element elem) {
      return this.parsedElements.contains(elem);
    } 

    /** CLN: added (PWW 13/08/04) 
     * replaces style_nodes.add(elem);
    */
    public void elementWasParsed(Element elem) {
      this.parsedElements.add(elem);
    }     

    /* =========================== add style rule implementation =========================== */
    public void addStyleRule(JStyle rule) {
        String key = null;
        for(int i=0; i<rule.selector_list.getLength(); i++) {
            key = null;
            Selector sel = rule.selector_list.item(i);
            //if rule is id rule
            if(isIdSelector(sel)) {
                //then safe add to map
                key = getIdName(sel);
                //u.p("added id rule: " + key);
                addToMap(id_map,rule,key);
                continue;
            }
            //if rule is class rule
            if(isClassSelector(sel)) {
                //then safe add to map
                key = getClassName(sel);
                //u.p("added class rule: " + key);
                addToMap(class_map,rule,key);
                continue;
            }
            //if rule is element rule
            if(isElementSelector(sel)) {
                key = getElementName(sel);
                //then safe add to map
                //u.p("added element rule: " + key);
                if(key == null) {
                    addToMap(all_map,rule,"*");
                } else if(key.equals("*")) {
                    addToMap(all_map,rule,key);
                } else {
                    addToMap(element_map,rule,key);
                }
                continue;
            }
            
            logger.severe("the rule: " + sel + " is an unknown type");
        }
    }
    
    private boolean isIdSelector(Selector sel) {
        Selector child = getChildSelector(sel);
        if(child.getSelectorType() == Selector.SAC_CONDITIONAL_SELECTOR) {
            ConditionalSelector cond_sel = (ConditionalSelector)child;
            Condition cond = cond_sel.getCondition();
            if(cond.getConditionType() == cond.SAC_ID_CONDITION) {
                return true;
            }
        }
        return false;
    }
    
    private String getIdName(Selector sel) {
        Selector child = getChildSelector(sel);
        ConditionalSelector cond_sel = (ConditionalSelector)child;
        AttributeCondition cond = (AttributeCondition)cond_sel.getCondition();
        return cond.getValue();
    }
    
    private boolean isClassSelector(Selector sel) {
        Selector child = getChildSelector(sel);
        if(child.getSelectorType() == sel.SAC_CONDITIONAL_SELECTOR) {
            ConditionalSelector cond_sel = (ConditionalSelector)child;
            Condition cond = cond_sel.getCondition();
            if(cond.getConditionType() == cond.SAC_CLASS_CONDITION) {
                return true;
            }
        }
        return false;
    }
    
    private String getClassName(Selector sel) {
        Selector child = getChildSelector(sel);
        ConditionalSelector cond_sel = (ConditionalSelector)child;
        AttributeCondition cond = (AttributeCondition)cond_sel.getCondition();
        return cond.getValue();
    }
    
    private boolean isElementSelector(Selector sel) {
        Selector child = getChildSelector(sel);
        if(child.getSelectorType() == sel.SAC_ELEMENT_NODE_SELECTOR) {
            return true;
        }
        return false;
    }
    
    /* Returns the deepest child, ie, the farthest left part of the
        selector.
     */
    private Selector getChildSelector(Selector sel) {
        if(sel.getSelectorType() == sel.SAC_DESCENDANT_SELECTOR) {
            DescendantSelector desc_sel = (DescendantSelector)sel;
            SimpleSelector child = desc_sel.getSimpleSelector();
            return getChildSelector(child);
        }
        return sel;
    }
     
    private String getElementName(Selector sel) {
        if(sel.getSelectorType() == sel.SAC_DESCENDANT_SELECTOR) {
            DescendantSelector desc_sel = (DescendantSelector)sel;
            SimpleSelector child = desc_sel.getSimpleSelector();
            return ((ElementSelector)child).getLocalName();
        }
        return ((ElementSelector)sel).getLocalName();
    }
    
    
    private void addToMap(Map map, JStyle rule, String key) {
        if(!map.containsKey(key)) {
            map.put(key,new ArrayList());
        }
        List list = (List)map.get(key);
        list.add(rule);
    }
    
    
    
    /* ===============================================================================
       =========================== find rule implementation ===========================
       =============================================================================== */
    public CSSStyleDeclaration findRule(Element elem, String property, boolean inherit) {
        if(elem.getNodeName().equals("a") && property.equals("color")) {
            //u.p("in here");
        }
        String key = null;
        
        //if element has an id
        if(elem.hasAttribute("id")) {
            // search through id rules
            //u.p("trying to match id on elem " + elem.getNodeName());
            key = elem.getAttribute("id");
            CSSStyleDeclaration dec = findRuleInMap(elem,property,inherit,id_map,key);
            if(dec != null) { 
                //u.p("returned an id dec");
                return dec; 
            }
        }
        //if element has a class
        if(elem.hasAttribute("class")) {
            key = elem.getAttribute("class");
            // search through class rules
            CSSStyleDeclaration dec = findRuleInMap(elem,property,inherit,class_map,key);
            if(dec != null) { return dec; }
        }
        
        
        
        //search through element rules
        
        CSSStyleDeclaration dec = findRuleInMap(elem,property,inherit,element_map,elem.getNodeName());
        if(elem.getNodeName().equals("a") && property.equals("color")) {
            //u.p("dec: = " + dec);
        }
        if(dec != null) { return dec; }
        dec = findRuleInMap(elem,property,inherit,all_map,"*");
        if(elem.getNodeName().equals("a") && property.equals("color")) {
            //u.p("went back to * for: " + property);
            //u.p("returning dec: " + dec);
        }
        return dec;
        
        
        //if still nothing found
        // then search through default rules
                // ??? how to do this?
        //return null;
    }
    
    private CSSStyleDeclaration findRuleInMap(Element elem, String property, boolean inherit, Map map, String key) {
        if(map == id_map) {
            //u.p("searching on key: " + key);
        }
        if(!map.containsKey(key)) {
            return null;
        }
        List list = (List)map.get(key);
        return findRuleInList(elem,property,inherit,list);
    }
    private static void p(String str, String property) {
        if(property.equals("color")) {
            System.out.println(str);
        }
    }
    
    private CSSStyleDeclaration findRuleInList(Element elem, String property, boolean inherit, List list) {

        for(int i=list.size()-1; i>=0; i--) {
            JStyle style = (JStyle)list.get(i);
            //p("style: " + style,property);
            for(int j=0; j<style.selector_list.getLength(); j++) {
                Selector sel = style.selector_list.item(j);
                //p("looking at selector: " + sel,property);
                if(rb.matchSelector(sel, elem)) {
                    CSSStyleDeclaration dec = style.declaration;
                    if(dec != null && dec.getPropertyValue(property) != null &&
                    !dec.getPropertyValue(property).equals("")) {
                        //p("found style for property: " + property + " in selector: " + sel,property);
                        //u.p("dec = " + dec);
                        return dec;
                    }
                }
            }
        }
        //p("======== ------ didn't find any rule to match : " + elem.getNodeName() + " " + property,property);
        return null;
        
    }
    
}
