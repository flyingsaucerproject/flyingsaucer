/*
 * {{{ header & license
 * StyleMap.java
 * Copyright (c) 2004 Torbjörn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
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
    private class Mapper {

        /** Creates a new instance of Mapper */
        Mapper(java.util.List rulesets) {
            for(java.util.Iterator i = rulesets.iterator(); i.hasNext();) {
                Ruleset r = (Ruleset) i.next();
                //at this point all selectors in a ruleset must be on the descendant axis
                axes.addAll(r.getSelectors());
            }
        }

        private Mapper(Mapper parent) {
            /* do nothing! Add descendant selectors when iterating!*/
        }

        /** Maps the children of the given document */
        void mapChildren(org.w3c.dom.Document doc) {
            org.w3c.dom.NodeList children = doc.getChildNodes();
            mapElements(children);
        }

        /** Maps the children of the given element */
        //NB handling of immediate sibling requires child elements to be traversed in order
        private void mapChildren(org.w3c.dom.Element e) {
            org.w3c.dom.NodeList children = e.getChildNodes();
            mapElements(children);
        }
        
        private void mapElements(org.w3c.dom.NodeList children) {
            for(int i=0; i<children.getLength(); i++) {
                org.w3c.dom.Node child = children.item(i);
                if(child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    mapElement((org.w3c.dom.Element)child);
                }
            }
        }
        
        //should now preserve sort order of rules
        private void mapElement(org.w3c.dom.Element e) {
            Mapper childMapper = new Mapper(this);
            for(int i = 0; i < axes.size(); i++) {
                Selector sel = (Selector) axes.get(i);
                if(sel.getAxis() == Selector.DESCENDANT_AXIS) {
                    childMapper.axes.add(sel);
                } else if(sel.getAxis() == Selector.IMMEDIATE_SIBLING_AXIS) {
                    //remove it from this mapper immediately
                    //NB handling of immediate sibling requires child elements to be traversed in order
                    axes.remove(i);
                    i--;
                }
                if(!sel.matches(e, _attRes)) continue;
                //TODO: if this selector has dynamic properties, we could note it in the child mapper, for easier handling
                if(!sel.matchesDynamic(e, _attRes)) continue;
                Selector chain = sel.getChainedSelector();
                if(chain == null) {
                    childMapper.mappedSelectors.add(sel);
                } else if(chain.getAxis() == Selector.IMMEDIATE_SIBLING_AXIS) {
                    //add it to this mapper!
                    axes.add(i, chain);
                    i++;
                } else {
                    childMapper.axes.add(chain);
                }
            }
            link(e, childMapper);
            childMapper.mapChildren(e);
        }
        
        java.util.List axes = new java.util.ArrayList();
        
        java.util.List mappedSelectors = new java.util.LinkedList();
    }
    
    private StyleMap() {
        
    }
    
    /** Gets the DOM document that is mapped */
    public org.w3c.dom.Document getDocument() {
        return _doc;
    }
    
    /** TODO: this should probably return a StyleSet or PropertyDeclarations with Cascade info*/
    public java.util.List getMappedProperties(org.w3c.dom.Element e) {
        Mapper m = (Mapper) map.get(e);
        java.util.List mappedProperties = new java.util.LinkedList();
        for(java.util.Iterator i = m.mappedSelectors.iterator(); i.hasNext();) {
            Selector sel = (Selector) i.next();
            mappedProperties.add(sel.getRuleset().getStyleDeclaration());
        }
        return mappedProperties;
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
