/*
 * ElementStyleMap.java
 *
 * Created on den 25 juli 2004, 11:13
 */

package net.homelinux.tobe.css;

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
    public class Mapper {

        /** Creates a new instance of Mapper */
        Mapper(java.util.List rulesets) {
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
        void mapChildren(org.w3c.dom.Node node) {
            org.w3c.dom.NodeList children = node.getChildNodes();
            for(int i=0; i<children.getLength(); i++) {
                org.w3c.dom.Node child = children.item(i);
                if(child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    mapElement((org.w3c.dom.Element)child);
                }
            }
        }
        
        private void mapElement(org.w3c.dom.Element e) {
            Mapper childMapper = new Mapper(this);
            if(immediateSiblingAxis != null) {
                java.util.List tmp = immediateSiblingAxis;
                immediateSiblingAxis = null;//clear immediately because it is only relevant for this element
                for(java.util.Iterator i = tmp.iterator(); i.hasNext();) {
                    match((Ruleset.Selector) i.next(), e, childMapper);
                }
            }
            for(java.util.Iterator i = childAxis.iterator(); i.hasNext();) {
                match((Ruleset.Selector) i.next(), e, childMapper);
            }
            for(java.util.Iterator i = descendantAxis.iterator(); i.hasNext();) {
                match((Ruleset.Selector) i.next(), e, childMapper);
            }
            link(e, childMapper);
            childMapper.mapChildren(e);
        }
        
        private void match(Ruleset.Selector sel, org.w3c.dom.Element e, Mapper child) {
            if(!sel.matches(e, _attRes)) return;
            Ruleset.Selector chain = sel.getChainedSelector();
            if(chain == null) {
                child.propertyDeclarations.addAll(sel.getRuleset().getPropertyDeclarations());
            } else if(chain.getAxis() == Ruleset.Selector.CHILD_AXIS) {
                child.childAxis.add(chain);
            } else if(chain.getAxis() == Ruleset.Selector.DESCENDANT_AXIS) {
                child.descendantAxis.add(chain);
            } else if(chain.getAxis() == Ruleset.Selector.IMMEDIATE_SIBLING_AXIS) {
                //add it to this mapper!
                if(immediateSiblingAxis == null) immediateSiblingAxis = new java.util.LinkedList();
                immediateSiblingAxis.add(chain);
            }
        }


        java.util.List descendantAxis = new java.util.LinkedList();
        java.util.List childAxis = new java.util.LinkedList();
        java.util.List immediateSiblingAxis;

        java.util.List propertyDeclarations = new java.util.LinkedList();
    }
    
    private StyleMap() {
        
    }
    
    /** Gets the DOM document that is mapped */
    public org.w3c.dom.Document getDocument() {
        return _doc;
    }
    
    /** TODO: this should probably return a StyleSet */
    public java.util.List getMappedProperties(org.w3c.dom.Element e) {
        Mapper m = (Mapper) map.get(e);
        return m.propertyDeclarations;
    }
    
    /** create a StyleMap from a DOM document. If ClassAndIDResolver is null, then identity conditions cannot match */
    public static StyleMap createMap(org.w3c.dom.Document doc, java.util.List rulesets, AttributeResolver attRes) {
        StyleMap map = new StyleMap();
        map.mapDocument(doc, rulesets, attRes);
        return map;
    }
    
    private void mapDocument(org.w3c.dom.Document doc, java.util.List rulesets, AttributeResolver attRes) {
        _doc = doc;
        _attRes = attRes;
        docMapper = new Mapper(rulesets);
        docMapper.mapChildren(_doc);
    }
    
    private void link(org.w3c.dom.Element e, Mapper m) {
        map.put(e, m);
    }
    
    private org.w3c.dom.Document _doc;
    private Mapper docMapper;
    private AttributeResolver _attRes;
    private java.util.HashMap map = new java.util.HashMap();

}
