/*
 *
 * Selector.java
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


package org.xhtmlrenderer.css.match;

 /**
 * A Selector is really a chain of CSS selectors that all need to be valid for the selector to match.
 *
 * @author  Torbjörn Gannholm
 */
public class Selector {

    final public static int DESCENDANT_AXIS = 0;
    final public static int CHILD_AXIS = 1;
    final public static int IMMEDIATE_SIBLING_AXIS = 2;

    /** Creates a new instance of Selector. Only called in the context of adding a Selector to a Ruleset
     * or adding a chained Selector to another Selector.
     * @param axis see values above.
     * @param elementName matches any element if null
     */
    Selector(Ruleset parent, int axis, String elementName) {
        _parent = parent;
        _axis = axis;
        _name = elementName;
        _specificityB = 0;
        _specificityC = 0;
        _specificityD = 0;
        if(_name != null) _specificityD++;
    }
    
    private Selector(int specificityB, int specificityC, int specificityD, Ruleset parent, int axis, String elementName) {
        this(parent,axis,elementName);
        _specificityB += specificityB;
        _specificityC += specificityC;
        _specificityD += specificityD;
    }

    /** Check if the given Element matches this selector.
     * Note: the parser should give all class
     */
    public boolean matches(org.w3c.dom.Element e, AttributeResolver attRes) {
        //TODO: resolve question of how CSS should handle namespaces. Unfortunately getLocalName is null if no namespace.
        if(_name == null || _name.equals(e.getLocalName()) || (e.getLocalName() == null && _name.equals(e.getNodeName()))) {
            if(conditions != null) {
                // all conditions need to be true
                for(java.util.Iterator i = conditions.iterator(); i.hasNext();) {
                    Condition c = (Condition) i.next();
                    if(!c.matches(e, attRes)) return false;
                }
            }
            return true;
        }
        return false;
    }

    /** Check if the given Element matches this selector's dynamic properties.
     * Note: the parser should give all class
     */
    public boolean matchesDynamic(org.w3c.dom.Element e, AttributeResolver attRes) {
            if(isPseudoClass(AttributeResolver.LINK_PSEUDOCLASS))
            	if( attRes == null || !attRes.isPseudoClass(e, AttributeResolver.LINK_PSEUDOCLASS)) return false;
            if(isPseudoClass(AttributeResolver.VISITED_PSEUDOCLASS))
            	if( attRes == null || !attRes.isPseudoClass(e, AttributeResolver.VISITED_PSEUDOCLASS)) return false;
            if(isPseudoClass(AttributeResolver.ACTIVE_PSEUDOCLASS))
            	if( attRes == null || !attRes.isPseudoClass(e, AttributeResolver.ACTIVE_PSEUDOCLASS)) return false;
            if(isPseudoClass(AttributeResolver.HOVER_PSEUDOCLASS))
            	if( attRes == null || !attRes.isPseudoClass(e, AttributeResolver.HOVER_PSEUDOCLASS)) return false;
            if(isPseudoClass(AttributeResolver.FOCUS_PSEUDOCLASS))
            	if( attRes == null || !attRes.isPseudoClass(e, AttributeResolver.FOCUS_PSEUDOCLASS)) return false;
            return true;
    }

    /** append a selector to this chain, specifying which axis it should be evaluated on */
    public Selector appendChainedSelector(int axis, String elementName) {
        if(chainedSelector == null) return (chainedSelector = new Selector(_specificityB, _specificityC, _specificityD, _parent, axis, elementName));
        else return chainedSelector.appendChainedSelector(axis, elementName);
    }

    /** the CSS condition that element has pseudo-class :first-child */
    public void addFirstChildCondition() {
        _specificityC++;
        addCondition(Condition.createFirstChildCondition());
    }

    /** the CSS condition :lang(x) */
    public void addLangCondition(String lang) {
        _specificityC++;
        addCondition(Condition.createLangCondition(lang));
    }

    /** the CSS condition #ID */
    public void addIDCondition(String id) {
        _specificityB++;
        addCondition(Condition.createIDCondition(id));
    }

    /** the CSS condition .class */
    public void addClassCondition(String className) {
        _specificityC++;
        addCondition(Condition.createClassCondition(className));
    }

    /** the CSS condition [attribute] */
    public void addAttributeExistsCondition(String name) {
        _specificityC++;
        addCondition(Condition.createAttributeExistsCondition(name));
    }

   /** the CSS condition [attribute=value] */
    public void addAttributeEqualsCondition(String name, String value) {
        _specificityC++;
        addCondition(Condition.createAttributeEqualsCondition(name, value));
    }
    
    /** the CSS condition [attribute~=value] */
    public void addAttributeMatchesListCondition(String name, String value) {
        _specificityC++;
        addCondition(Condition.createAttributeMatchesListCondition(name, value));
    }
    
    /** the CSS condition [attribute|=value] */
    public void addAttributeMatchesFirstPartCondition(String name, String value) {
        _specificityC++;
        addCondition(Condition.createAttributeMatchesFirstPartCondition(name, value));
    }
    
    /** set which pseudoclasses must apply for this selector
     *  @param pc the values from AttributeResolver should be used. Once set they cannot be unset.
     *  Note that the pseudo-classes should be set one at a time, otherwise specificity of declaration becomes wrong.
     */
    public void setPseudoClass(int pc) {
        if(!isPseudoClass(pc)) _specificityC++;
        _pc |= pc;
    }
    
     /** query if a pseudoclass must apply for this selector
      *  @param pc the values from AttributeResolver should be used.
     */
    public boolean isPseudoClass(int pc) {
        return ((_pc & pc) != 0);
    }
    
    /** check if selector queries for dynamic properties */
    public boolean isDynamic() {
        return (_pc != 0);
    }
    
    private void addCondition(Condition c) {
        if(conditions == null) conditions = new java.util.ArrayList();
        conditions.add(c);
    }

    /** get the next selector in the chain, for matching against elements along the appropriate axis */
    public Selector getChainedSelector() {
        return chainedSelector;
    }

    /** get the Ruleset that this Selector is part of */
    public Ruleset getRuleset() {
        return _parent;
    }

    /** get the axis that this selector should be evaluated on */
    public int getAxis() {
        return _axis;
    }

    private Ruleset _parent;
    private Selector chainedSelector = null;

    private int _axis;
    private String _name;
    private int _pc = 0;
    
    //specificity - correct values are gotten from the last Selector in the chain
    private int _specificityB;
    private int _specificityC;
    private int _specificityD;
    
    private java.util.List conditions;

}
