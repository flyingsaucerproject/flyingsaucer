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
        Selector s = new Selector(this, axis, elementName);
        selectors.add(s);
        return s;
    }
    
    public java.util.List getSelectors() {
        return selectors;
    }
    
    private java.util.List selectors = new java.util.ArrayList();
    private java.util.List declarations = new java.util.ArrayList();
    
}
