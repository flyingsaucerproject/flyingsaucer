/*
 * Ruleset.java
 *
 * Created on den 25 juli 2004, 11:26
 */

package net.homelinux.tobe.css;

/**
 * Rulesets should be created by the CSS parser. A list of Rulesets make up a CSS.
 *
 * A ruleset contains a list of selectors and a list of property declarations.
 *
 * @author  Torbjörn Gannholm
 */
public class Ruleset {

     /**
     * A Selector is really a chain of CSS selectors that all need to be valid for the selector to match.
     *
     * @author  Torbjörn Gannholm
     */
    public class Selector {

        final public static int DESCENDANT_AXIS = 0;
        final public static int CHILD_AXIS = 1;
        final public static int IMMEDIATE_SIBLING_AXIS = 2;

        /** Creates a new instance of Selector
         * @param axis see values above.
         * @param elementName matches any element if null
         */
        private Selector(int axis, String elementName) {
            _axis = axis;
            _name = elementName;
        }
        
        /** Check if the given Element matches this selector.
         * Note: the parser should give all class
         */
        public boolean matches(org.w3c.dom.Element e, AttributeResolver attRes) {
            //TODO: resolve question of how CSS should handle namespaces. Unfortunately getLocalName is null if no namespace.
            if(_name == null || _name.equals(e.getLocalName()) || (e.getLocalName() == null && _name.equals(e.getNodeName()))) {
                //TODO: handle conditions
                if(_id != null) {
                    if(attRes == null) return false;
                    if(!_id.equals(attRes.getID(e))) return false;
                }
                if(_classes != null) {
                    if(attRes == null) return false;
                    String c = attRes.getClass(e);
                    if(c == null) return false;
                    String[] ca = c.split(" ");
                    boolean matched = true;
                    for(java.util.Iterator i = _classes.iterator(); matched && i.hasNext();) {
                        String cc = (String) i.next();
                        matched=false;
                        for(int j=0; j < ca.length; j++) {
                            if(cc.equals(ca[j])) matched=true;
                        }
                    }
                    return matched;
                }
                return true;
            }
            return false;
        }

        /** append a selector to this chain, specifying which axis it should be evaluated on */
        public void appendChainedSelector(int axis, String elementName) {
            if(chainedSelector == null) chainedSelector = new Selector(axis, elementName);
            else chainedSelector.appendChainedSelector(axis, elementName);
        }
        
        /** only one ID-condition possible per element */
        public void setIDCondition(String id) {
            _id = id;
        }
        
        /** can be many class conditions */
        public void addClassCondition(String className) {
            if(_classes == null) _classes = new java.util.ArrayList();
            _classes.add(className);
        }

        /** get the next selector in the chain, for matching against elements along the appropriate axis */
        public Selector getChainedSelector() {
            return chainedSelector;
        }
        
        /** get the Ruleset that this Selector is part of */
        public Ruleset getRuleset() {
            return Ruleset.this;
        }
        
        /** get the axis that this selector should be evaluated on */
        public int getAxis() {
            return _axis;
        }

        private Selector chainedSelector = null;

        private int _axis;
        private String _name;
        private String _id;
        private java.util.List _classes;

    }
    
    /** Creates a new instance of Ruleset */
    public Ruleset() {
    }
    
    /** TODO: returns the list of property declarations of this ruleset
     *  This method's signature may change
     */
    public java.util.List getPropertyDeclarations() {
        return declarations;
    }
    
    /** TODO: add property declarations to this ruleset
     *  This method's signature may change
     *  Perhaps it might as well be Object, the mapping algorithm does not care
     */
    public void addPropertyDeclaration(Object declaration) {
        declarations.add(declaration);
    }
    
    public Selector createSelector(int axis, String elementName) {
        Selector s = new Selector(axis, elementName);
        selectors.add(s);
        return s;
    }
    
    public java.util.List getSelectors() {
        return selectors;
    }
    
    private java.util.List selectors = new java.util.ArrayList();
    private java.util.List declarations = new java.util.ArrayList();
    
}
