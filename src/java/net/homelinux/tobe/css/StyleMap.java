/*
 * ElementStyleMap.java
 *
 * Created on den 25 juli 2004, 11:13
 */

package src.java.net.homelinux.tobe.css;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class StyleMap {
    
/**
 * Mapper represents a local CSS for a Node that is used to match the Node's children.
 *
 * @author  Torbjörn Gannholm
 */
    private class Mapper {

        /** Creates a new instance of Mapper */
        public Mapper(java.util.List rulesets) {
            for(java.util.Iterator i = rulesets.iterator(); i.hasNext();) {
                Ruleset r = (Ruleset) i.next();
                //at this point all selectors in a ruleset must be on the descendant axis
                descendantAxis.addAll(r.getSelectors());
            }
        }

        private Mapper(Mapper parent) {
            descendantAxis.addAll(parent.descendantAxis);
        }

        /** Maps the children of the given node */
        public void mapChildren(org.w3c.dom.Node node) {
            org.w3c.dom.NodeList children = node.getChildNodes();
            for(int i=0; i<children.getLength(); i++) {
                org.w3c.dom.Node child = children.item(i);
                if(child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    mapElement((org.w3c.dom.Element)child);
                }
            }
        }
        
        public void mapElement(org.w3c.dom.Element e) {
            
        }


        java.util.List descendantAxis = new java.util.LinkedList();
        java.util.List childAxis = new java.util.LinkedList();
        java.util.List immediateSiblingAxis = new java.util.LinkedList();

        java.util.List propertyDeclarations = new java.util.LinkedList();
    }
    
    /** Creates a new instance of ElementStyleMap to map the given document. Should only be called by the Mapper. */
    private StyleMap() {
        
    }
    
    /** Gets the DOM document that is mapped */
    public org.w3c.dom.Document getDocument() {
        return _doc;
    }
    
    /** TODO: this should probably return a StyleSet */
    public java.util.List getMappedStyle(org.w3c.dom.Element e) {
        return null;
    }
    
    public static StyleMap createMap(org.w3c.dom.Document doc, java.util.List rulesets) {
        StyleMap map = new StyleMap();
        map.map(doc, rulesets);
        return map;
    }
    
    private void map(org.w3c.dom.Document doc, java.util.List rulesets) {
        _doc = doc;
        docMapper = new Mapper(rulesets);
        docMapper.mapChildren(_doc);
    }
    
    private org.w3c.dom.Document _doc;
    private Mapper docMapper;

}
