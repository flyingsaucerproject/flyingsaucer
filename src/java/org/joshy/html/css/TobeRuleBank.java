/*
 * TobeRuleBank.java
 *
 * Created on den 30 juli 2004, 23:15
 */

package org.joshy.html.css;

import org.w3c.css.sac.*;
import net.homelinux.tobe.css.Ruleset;
import net.homelinux.tobe.css.AttributeResolver;
import net.homelinux.tobe.css.StyleMap;

/**
 * Tries to make sense of SAC, which is not easy. This might be very wrong because SAC totally SUCKS.
 *
 * @author  Torbjörn Gannholm
 */
public class TobeRuleBank implements RuleBank {
    
    java.util.List ruleList = new java.util.LinkedList();
    public StyleMap styleMap;//public for timing and testing
    
    /** Creates a new instance of TobeRuleBank */
    public TobeRuleBank() {
    }
    
    public void addRule(JStyle rule) {
        styleMap = null;//We have to remap
        Ruleset rs = new Ruleset();
        ruleList.add(rs);
        rs.setStyleDeclaration(rule);
        /*for(int i=0; i < rule.declaration.getLength(); i++) {
            String name = rule.declaration.item(i);
            Object value = rule.declaration.getPropertyCSSValue(name);
            //will this ever happen?
            if(value == null) System.err.println("Oops, we did not take care of a shorthand property properly");
            rs.addPropertyDeclaration(new PropertyDeclaration(name,value));
        }*/
        for(int i = 0; i < rule.selector_list.getLength(); i++) {
            Selector selector = rule.selector_list.item(i);
            Selector nextSelector = null;
            Condition cond = null;
            net.homelinux.tobe.css.Selector s = null;
            if(selector.getSelectorType() == Selector.SAC_DIRECT_ADJACENT_SELECTOR) {
                nextSelector = selector;
                selector = ((SiblingSelector) nextSelector).getSelector();
            }
            if(selector.getSelectorType() == Selector.SAC_CHILD_SELECTOR) {
                nextSelector = selector;
                selector = ((DescendantSelector) nextSelector).getAncestorSelector();
            }
            if(selector.getSelectorType() == Selector.SAC_DESCENDANT_SELECTOR) {
                nextSelector = selector;
                selector = ((DescendantSelector) nextSelector).getAncestorSelector();
            }
            if(selector.getSelectorType() == Selector.SAC_CONDITIONAL_SELECTOR) {
                cond = ((ConditionalSelector) selector).getCondition();
                selector = ((ConditionalSelector) selector).getSimpleSelector();
            }
            if(selector.getSelectorType() == Selector.SAC_ELEMENT_NODE_SELECTOR) {
                s = rs.createSelector(net.homelinux.tobe.css.Selector.DESCENDANT_AXIS,  ((ElementSelector) selector).getLocalName());
            }
            if(cond != null) addConditions(s, cond);
            if(nextSelector != null) addChainedSelector(s,nextSelector);
        }
    }
    
    public long time;
    public long map_time;
    
    /** note: not yet implemented evaluating cascade and specificity, we just find a value and that's it */
    public org.w3c.dom.css.CSSStyleDeclaration findRule(org.w3c.dom.Element elem, String property, boolean inherit) {
            long start_time = new java.util.Date().getTime();
        if(styleMap == null) styleMap = StyleMap.createMap(elem.getOwnerDocument(), ruleList, new StaticHtmlAttributeResolver());
            long end_time = new java.util.Date().getTime();
            map_time += end_time-start_time;
        java.util.List styleList = styleMap.getMappedProperties(elem);
        org.w3c.dom.css.CSSStyleDeclaration retval = null;
        for(java.util.Iterator i = styleList.iterator(); i.hasNext();) {
            JStyle style = (JStyle) i.next();
                    org.w3c.dom.css.CSSStyleDeclaration dec = style.declaration;
                    // if the style has the property we want
                    if(dec != null && dec.getPropertyValue(property) != null &&
                            !dec.getPropertyValue(property).equals("")) {
                        retval = dec;
                    }
        }
        //no bite, maybe look at parent?
        if(retval == null && inherit) {
            org.w3c.dom.Node parent = elem.getParentNode();
            if(parent != null && parent.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
                retval = findRule((org.w3c.dom.Element)parent, property, inherit);
        }
            end_time = new java.util.Date().getTime();
            time += end_time-start_time;
        return retval;
    }
    
    private void addConditions(net.homelinux.tobe.css.Selector s, Condition cond) {
        switch(cond.getConditionType()) {
            case Condition.SAC_AND_CONDITION:
                CombinatorCondition comb = (CombinatorCondition) cond;
                addConditions(s, comb.getFirstCondition());
                addConditions(s, comb.getSecondCondition());
            break;
            case Condition.SAC_ATTRIBUTE_CONDITION:
                AttributeCondition attr = (AttributeCondition) cond;
                if(attr.getSpecified()) {
                    s.addAttributeEqualsCondition(attr.getLocalName(), attr.getValue());
                } else {
                    s.addAttributeExistsCondition(attr.getLocalName());
                }
            break;
            case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
                attr = (AttributeCondition) cond;
                s.addAttributeMatchesFirstPartCondition(attr.getLocalName(), attr.getValue());
            break;
            case Condition.SAC_CLASS_CONDITION:
                attr = (AttributeCondition) cond;
                s.addClassCondition(attr.getValue());
            break;
            case Condition.SAC_ID_CONDITION:
                attr = (AttributeCondition) cond;
                s.addIDCondition(attr.getValue());
            break;
            case Condition.SAC_LANG_CONDITION:
                LangCondition lang = (LangCondition) cond;
                s.addLangCondition(lang.getLang());
            break;
            case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
                attr = (AttributeCondition) cond;
                s.addAttributeMatchesListCondition(attr.getLocalName(), attr.getValue());
            break;
            case Condition.SAC_POSITIONAL_CONDITION:
                PositionalCondition pos = (PositionalCondition) cond;
                s.addFirstChildCondition();
            break;
            case Condition.SAC_PSEUDO_CLASS_CONDITION:
                attr = (AttributeCondition) cond;
                if(attr.getValue().equals("link")) s.setPseudoClass(AttributeResolver.LINK_PSEUDOCLASS);
                if(attr.getValue().equals("visited")) s.setPseudoClass(AttributeResolver.VISITED_PSEUDOCLASS);
                if(attr.getValue().equals("hover")) s.setPseudoClass(AttributeResolver.HOVER_PSEUDOCLASS);
                if(attr.getValue().equals("active")) s.setPseudoClass(AttributeResolver.ACTIVE_PSEUDOCLASS);
                if(attr.getValue().equals("focus")) s.setPseudoClass(AttributeResolver.FOCUS_PSEUDOCLASS);
            break;
            default:
                System.err.println("Bad condition");
        }
    }
    
    private void addChainedSelector(net.homelinux.tobe.css.Selector s, Selector selector) {
            int axis = 0;
            switch(selector.getSelectorType()) {
                case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
                axis = net.homelinux.tobe.css.Selector.IMMEDIATE_SIBLING_AXIS;
                selector = ((SiblingSelector) selector).getSiblingSelector();
                break;
                case Selector.SAC_CHILD_SELECTOR:
                axis = net.homelinux.tobe.css.Selector.CHILD_AXIS;
                selector = ((DescendantSelector) selector).getSimpleSelector();
                break;
                case Selector.SAC_DESCENDANT_SELECTOR:
                axis = net.homelinux.tobe.css.Selector.DESCENDANT_AXIS;
                selector = ((DescendantSelector) selector).getAncestorSelector();
                break;
                default:
                    System.err.println("Bad selector");
            }
            
            Selector nextSelector = null;
            Condition cond = null;
            if(selector.getSelectorType() == selector.SAC_DIRECT_ADJACENT_SELECTOR) {
                nextSelector = selector;
                selector = ((SiblingSelector) nextSelector).getSelector();
            }
            if(selector.getSelectorType() == selector.SAC_CHILD_SELECTOR) {
                nextSelector = selector;
                selector = ((DescendantSelector) nextSelector).getAncestorSelector();
            }
            if(selector.getSelectorType() == selector.SAC_DESCENDANT_SELECTOR) {
                nextSelector = selector;
                selector = ((DescendantSelector) nextSelector).getAncestorSelector();
            }
            if(selector.getSelectorType() == selector.SAC_CONDITIONAL_SELECTOR) {
                cond = ((ConditionalSelector) selector).getCondition();
                selector = ((ConditionalSelector) selector).getSimpleSelector();
            }
            if(selector.getSelectorType() == selector.SAC_ELEMENT_NODE_SELECTOR) {
                s = s.appendChainedSelector(axis, ((ElementSelector) selector).getLocalName());
            }
            if(cond != null) addConditions(s, cond);
            if(nextSelector != null) addChainedSelector(s,nextSelector);
    }
    
}
