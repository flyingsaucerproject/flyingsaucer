package org.joshy.html.css;



import java.io.*;

import java.util.ArrayList;

import java.util.List;

import org.w3c.dom.*;

import org.w3c.dom.css.*;

import org.w3c.css.sac.*;

import com.steadystate.css.*;

import com.steadystate.css.parser.*;



/* ============ CSS searching and cascading code ================ */

public class RuleFinder implements RuleBank {

    List styles;
    
    List parsedElements;

    public RuleFinder() {

        styles = new ArrayList();

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
    

    public void addStyleRule(JStyle rule) {

        styles.add(rule);

    }

    

    

    

    

    

    public CSSStyleDeclaration findRule(Element elem, String property, boolean inherit) {

        // loop through the styles in reverse order

        for(int s=styles.size()-1; s>=0; s--) {

            JStyle style = (JStyle)styles.get(s);

            // loop through each selector for the style

            for(int i=0; i<style.selector_list.getLength(); i++) {

                Selector sel = style.selector_list.item(i);

                // if the selector matches

                if(matchSelector(sel,elem)) {

                    CSSStyleDeclaration dec = style.declaration;

                    // if the style has the property we want

                    if(dec != null && dec.getPropertyValue(property) != null &&

                            !dec.getPropertyValue(property).equals("")) {

                        return dec;

                    }

                }

            }

        }

        

        // since we didn't find anything, recurse up the chain

        if(inherit) {

            if(elem.getParentNode() != null) {

                Node parent = elem.getParentNode();

                //u.p("Parent node = " + parent.getNodeName());

                if(parent instanceof Element) {

                    return findRule((Element)elem.getParentNode(),property,inherit);

                }

            }

        }

        

        //u.p("no style found at all "  + elem.getNodeName() + " prop = " + property);

        return null;

    }

    

    

    

    private boolean match(String selector, Node node) {

        try {

            CSSOMParser parser = new CSSOMParser();

            SelectorList list = parser.parseSelectors(new InputSource(new StringReader(selector)));

            for(int i=0; i<list.getLength(); i++) {

                Selector sel = list.item(i);

                if(matchSelector(sel,node)) {

                    return true;

                }

            }

        } catch (Exception ex) {

            org.joshy.u.p(ex);

        }

        return false;

    }

    

    

    

    protected boolean matchSelector(Selector selector, Node node) {

        if(selector.getSelectorType() == selector.SAC_ELEMENT_NODE_SELECTOR) {

            return matchElement((ElementSelector)selector,node);

        }

        if(selector.getSelectorType() == selector.SAC_CONDITIONAL_SELECTOR) {

            ConditionalSelector cond_sel = (ConditionalSelector)selector;

            return matchConditional(cond_sel,node);

        }

        if(selector.getSelectorType() == selector.SAC_DESCENDANT_SELECTOR) {

            DescendantSelector desc_sel = (DescendantSelector)selector;

            return matchDescendant(desc_sel,node);

        }

        org.joshy.u.p("unrecognized selector type: " + selector + " node = " + node.getNodeName());

        return false;

    }

    

    

    

    private boolean matchElement(ElementSelector selector, Node node) {

        // null = any element (wildcard = *);

        if(selector.getLocalName() == null) {

            return true;

        }

        if(selector.getLocalName().equals(node.getNodeName())) {

            return true;

        }

        return false;

    }





    private boolean matchConditional(ConditionalSelector selector, Node node) {

        Condition cond = selector.getCondition();

        if(cond.getConditionType() == cond.SAC_CLASS_CONDITION) {

            return matchClassConditional((AttributeCondition)cond,selector.getSimpleSelector(),node);

        }

        if(cond.getConditionType() == cond.SAC_ID_CONDITION) {

            return matchIDConditional((AttributeCondition)cond,selector.getSimpleSelector(),node);

        }

        return false;

    }

    



    private boolean matchDescendant(DescendantSelector selector, Node node) {

        SimpleSelector child = selector.getSimpleSelector();

        Selector parent = selector.getAncestorSelector();

        if(matchSelector(child,node)) {

        //if(matchElement((ElementSelector)child, node)) {

            Node current_node = node;

            while(true) {

                Node parent_node = current_node.getParentNode();

                if(parent_node == null) {

                    return false;

                }

                if(matchSelector(parent,parent_node)) {

                    return true;

                }

                current_node = parent_node;

            }

        }

        return false;

    }

    

    

    private boolean matchClassConditional(AttributeCondition cond, SimpleSelector sel, Node node) {

        // if it's an element

        if(!(node.getNodeType() == node.ELEMENT_NODE)) {

            return false;

        }

        Element elem = (Element)node;

        // if it has a class attribute

        if(!elem.hasAttribute("class")) {

            return false;

        }

        // if the class attribute matches the condition's class

        if(!elem.getAttribute("class").equals(cond.getValue())) {

            return false;

        }

        

        // if local name = null then it's the 'any' selector (*)

        // so it matches

        ElementSelector es = (ElementSelector)sel;

        if(es.getLocalName() == null) {

            return true;

        }

        if(!((ElementSelector)sel).getLocalName().equals(node.getNodeName())) {

            return false;

        }

        return true;

    }



    private boolean matchIDConditional(AttributeCondition cond, SimpleSelector sel, Node node) {

        // if it's an element

        if(!(node.getNodeType() == node.ELEMENT_NODE)) { return false; }

        Element elem = (Element)node;

        // if it has a id attribute

        if(!elem.hasAttribute("id")) { return false; }

        // if the id attribute matches the condition's id

        if(!elem.getAttribute("id").equals(cond.getValue())) { return false; }

        

        // if the node names match up

        ElementSelector es = (ElementSelector)sel;

        // if local name = null then it's the 'any' selector (*)

        // so it matches

        if(es.getLocalName() == null) {

            return true;

        }

        if(!((ElementSelector)sel).getLocalName().equals(node.getNodeName())) { return false; }

        return true;

    }

}

