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
    /*public java.util.List getPropertyDeclarations() {
        return declarations;
    }*/
    public Object getStyleDeclaration() {
        return styleDeclaration;
    }
    
    /** Leave parameter as Object, tests of logic rely on it. Refactor!? */
    public void setStyleDeclaration(Object declaration) {
        //declarations.add(declaration);
        styleDeclaration = declaration;
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
    private Object styleDeclaration;
    
}
