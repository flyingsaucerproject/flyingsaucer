/*
 * Selector.java
 *
 * Created on den 28 juli 2004, 14:04
 */

package net.homelinux.tobe.css;

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

    /** append a selector to this chain, specifying which axis it should be evaluated on */
    public void appendChainedSelector(int axis, String elementName) {
        if(chainedSelector == null) chainedSelector = new Selector(_parent, axis, elementName);
        else chainedSelector.appendChainedSelector(axis, elementName);
    }

    /** the CSS condition #ID */
    public void addIDCondition(String id) {
        addCondition(Condition.createIDCondition(id));
    }

    /** the CSS condition .class */
    public void addClassCondition(String className) {
        addCondition(Condition.createClassCondition(className));
    }

    /** the CSS condition [attribute] */
    public void addAttributeExistsCondition(String name) {
        addCondition(Condition.createAttributeExistsCondition(name));
    }

   /** the CSS condition [attribute=value] */
    public void addAttributeEqualsCondition(String name, String value) {
        addCondition(Condition.createAttributeEqualsCondition(name, value));
    }
    
    /** the CSS condition [attribute~=value] */
    public void addAttributeMatchesListCondition(String name, String value) {
        addCondition(Condition.createAttributeMatchesListCondition(name, value));
    }
    
    /** the CSS condition [attribute|=value] */
    public void addAttributeMatchesFirstPartCondition(String name, String value) {
        addCondition(Condition.createAttributeMatchesFirstPartCondition(name, value));
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
    
    private java.util.List conditions;

}
