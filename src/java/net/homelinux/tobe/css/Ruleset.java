/*
 * Ruleset.java
 *
 * Created on den 25 juli 2004, 11:26
 */

package src.java.net.homelinux.tobe.css;

/**
 * Rulesets should be created by the CSS parser. A list of Rulesets make up a CSS.
 *
 * @author  Torbjörn Gannholm
 */
public class Ruleset {

     /**
     * A Selector is really a chain of CSS selectors.
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
        public Selector(int axis, String elementName) {
            _axis = axis;
            _name = elementName;
        }
        
        public boolean matches(org.w3c.dom.Element e) {
            //TODO: resolve question of how CSS should handle namespaces. Unfortunately getLocalName is null if no namespace.
            if(_name == null || _name.equals(e.getLocalName()) || (e.getLocalName() == null && _name.equals(e.getNodeName()))) {
                //TODO: handle conditions
                return true;
            }
            return false;
        }

        public void appendSelector(Selector s) {
            if(chainedSelector == null) chainedSelector = s;
            else chainedSelector.appendSelector(s);
        }

        public Selector getChainedSelector() {
            return chainedSelector;
        }

        private Selector chainedSelector = null;

        private int _axis;
        private String _name;

    }
    
    /** Creates a new instance of Ruleset */
    public Ruleset() {
    }
    
    /** TODO: returns the list of property declarations of this ruleset
     *  This method's signature may change
     */
    public java.util.List getPropertyDeclarations() {
        return null;
    }
    
    public void addSelector(Selector s) {
        selectors.add(s);
    }
    
    public java.util.List getSelectors() {
        return selectors;
    }
    
    private java.util.List selectors = new java.util.ArrayList();
    
}
