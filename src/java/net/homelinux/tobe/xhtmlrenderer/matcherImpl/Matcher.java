/*
 *
 * Matcher.java
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

package net.homelinux.tobe.xhtmlrenderer.matcherImpl;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class Matcher implements net.homelinux.tobe.xhtmlrenderer.Matcher {
    
/**
 * Mapper represents a local CSS for a Node that is used to match the Node's children.
 *
 * @author  Torbjörn Gannholm
 */
    class Mapper {

        /** Creates a new instance of Mapper */
        Mapper(java.util.Collection selectors) {
            axes.addAll(selectors);
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
    
    Matcher() {
        
    }
    
    public Matcher(org.w3c.dom.Document doc, net.homelinux.tobe.xhtmlrenderer.AttributeResolver ar, java.util.Iterator stylesheets) {
        setDocument(doc);
        setAttributeResolver(ar);
        setStylesheets(stylesheets);
    }
    
    private void link(org.w3c.dom.Element e, Mapper m) {
        _map.put(e, m);
    }
    
    private void clearMaps() {
        _map = null;
        _csCache = null;
        _csMap = null;
    }
    
    public void setAttributeResolver(net.homelinux.tobe.xhtmlrenderer.AttributeResolver ar) {
        _attRes = ar;
        clearMaps();
        if(_elStyle != null) _elStyle = new java.util.HashMap();
    }
    
    public void setDocument(org.w3c.dom.Document doc) {
        _doc = doc;
        clearMaps();
        if(_elStyle != null) _elStyle = new java.util.HashMap();
    }
    
    public void setStylesheets(java.util.Iterator stylesheets) {
            int count = 0;
            java.util.TreeMap sorter = new java.util.TreeMap();
            while(stylesheets.hasNext()) {
                net.homelinux.tobe.xhtmlrenderer.Stylesheet ss = (net.homelinux.tobe.xhtmlrenderer.Stylesheet) stylesheets.next();
                for(java.util.Iterator rulesets = ss.getRulesets(); rulesets.hasNext();) {
                    net.homelinux.tobe.xhtmlrenderer.Ruleset r = (net.homelinux.tobe.xhtmlrenderer.Ruleset) rulesets.next();
                    //at this point all selectors in a ruleset must be placed on the descendant axis
                    org.w3c.css.sac.SelectorList selector_list = r.getSelectorList();
                    for ( int i = 0; i < selector_list.getLength(); i++ ) {
                        org.w3c.css.sac.Selector selector = selector_list.item( i );
                        Selector s = addSelector(count++, r, selector);
                        sorter.put(s.getOrder(), s);
                    }
                }
            }
        docMapper = new Mapper(sorter.values());
        clearMaps();
    }
        
    /**
     * Adds the top-level or leftmost selector of the XRStyleReference
     * object.
     *
     * @param rs         The feature to be added to the ChainedSelector attribute
     * @param selector  The feature to be added to the ChainedSelector attribute
     */
    private Selector addSelector(int pos, net.homelinux.tobe.xhtmlrenderer.Ruleset rs, org.w3c.css.sac.Selector selector) {
            Selector s = null;
            if ( selector.getSelectorType() == org.w3c.css.sac.Selector.SAC_DIRECT_ADJACENT_SELECTOR ) {
                s = addSelector(pos, rs, ( (org.w3c.css.sac.SiblingSelector)selector ).getSelector());
                addChainedSelector(s, selector);
            } else if ( selector.getSelectorType() == org.w3c.css.sac.Selector.SAC_CHILD_SELECTOR ) {
                s = addSelector(pos, rs, ( (org.w3c.css.sac.DescendantSelector)selector ).getAncestorSelector());
                addChainedSelector(s, selector);
            } else if ( selector.getSelectorType() == org.w3c.css.sac.Selector.SAC_DESCENDANT_SELECTOR ) {
                s = addSelector(pos, rs, ( (org.w3c.css.sac.DescendantSelector)selector ).getAncestorSelector());
                addChainedSelector(s, selector);
            } else if ( selector.getSelectorType() == org.w3c.css.sac.Selector.SAC_CONDITIONAL_SELECTOR ) {
                org.w3c.css.sac.Condition cond = ( (org.w3c.css.sac.ConditionalSelector)selector ).getCondition();
                s = addSelector(pos, rs, ( (org.w3c.css.sac.ConditionalSelector)selector ).getSimpleSelector());
                addConditions( s, cond );
            } else if ( selector.getSelectorType() == org.w3c.css.sac.Selector.SAC_ELEMENT_NODE_SELECTOR ) {
                s = new Selector(pos, rs, net.homelinux.tobe.css.Selector.DESCENDANT_AXIS, ( (org.w3c.css.sac.ElementSelector)selector ).getLocalName() );
            } else System.err.println("bad selector in addSelector");

            return s;
    }

    /**
     * Adds a feature to the ChainedSelector attribute of the XRStyleReference
     * object.
     *
     * @param s         The feature to be added to the ChainedSelector attribute
     * @param selector  The feature to be added to the ChainedSelector attribute
     */
    private void addChainedSelector( Selector s, org.w3c.css.sac.Selector selector ) {
        int axis = 0;
        org.w3c.css.sac.SimpleSelector simple = null;
        switch ( selector.getSelectorType() ) {
            case org.w3c.css.sac.Selector.SAC_DIRECT_ADJACENT_SELECTOR:
                axis = net.homelinux.tobe.css.Selector.IMMEDIATE_SIBLING_AXIS;
                simple = ( (org.w3c.css.sac.SiblingSelector)selector ).getSiblingSelector();
                break;
            case org.w3c.css.sac.Selector.SAC_CHILD_SELECTOR:
                axis = net.homelinux.tobe.css.Selector.CHILD_AXIS;
                simple = ( (org.w3c.css.sac.DescendantSelector)selector ).getSimpleSelector();
                break;
            case org.w3c.css.sac.Selector.SAC_DESCENDANT_SELECTOR:
                axis = net.homelinux.tobe.css.Selector.DESCENDANT_AXIS;
                simple = ( (org.w3c.css.sac.DescendantSelector)selector ).getSimpleSelector();
                break;
            default:
                System.err.println( "Bad selector" );
        }

        org.w3c.css.sac.Condition cond = null;
        if ( simple.getSelectorType() == org.w3c.css.sac.Selector.SAC_CONDITIONAL_SELECTOR ) {
            cond = ( (org.w3c.css.sac.ConditionalSelector)simple ).getCondition();
            //if ConditionalSelectors can be nested, we are in trouble here
            simple = ( (org.w3c.css.sac.ConditionalSelector)simple ).getSimpleSelector();
        }
        if ( simple.getSelectorType() == org.w3c.css.sac.Selector.SAC_ELEMENT_NODE_SELECTOR ) {
            s = s.appendChainedSelector( axis, ( (org.w3c.css.sac.ElementSelector)simple ).getLocalName() );
        }
        if ( cond != null ) {
            addConditions( s, cond );
        }
    }

    /**
     * @param s     The feature to be added to the Conditions attribute
     * @param cond  The feature to be added to the Conditions attribute
     */
    private void addConditions( Selector s, org.w3c.css.sac.Condition cond ) {
        switch ( cond.getConditionType() ) {
            case org.w3c.css.sac.Condition.SAC_AND_CONDITION:
                org.w3c.css.sac.CombinatorCondition comb = (org.w3c.css.sac.CombinatorCondition)cond;
                addConditions( s, comb.getFirstCondition() );
                addConditions( s, comb.getSecondCondition() );
                break;
            case org.w3c.css.sac.Condition.SAC_ATTRIBUTE_CONDITION:
                org.w3c.css.sac.AttributeCondition attr = (org.w3c.css.sac.AttributeCondition)cond;
                if ( attr.getSpecified() ) {
                    s.addAttributeEqualsCondition( attr.getLocalName(), attr.getValue() );
                } else {
                    s.addAttributeExistsCondition( attr.getLocalName() );
                }
                break;
            case org.w3c.css.sac.Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
                attr = (org.w3c.css.sac.AttributeCondition)cond;
                s.addAttributeMatchesFirstPartCondition( attr.getLocalName(), attr.getValue() );
                break;
            case org.w3c.css.sac.Condition.SAC_CLASS_CONDITION:
                attr = (org.w3c.css.sac.AttributeCondition)cond;
                s.addClassCondition( attr.getValue() );
                break;
            case org.w3c.css.sac.Condition.SAC_ID_CONDITION:
                attr = (org.w3c.css.sac.AttributeCondition)cond;
                s.addIDCondition( attr.getValue() );
                break;
            case org.w3c.css.sac.Condition.SAC_LANG_CONDITION:
                org.w3c.css.sac.LangCondition lang = (org.w3c.css.sac.LangCondition)cond;
                s.addLangCondition( lang.getLang() );
                break;
            case org.w3c.css.sac.Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
                attr = (org.w3c.css.sac.AttributeCondition)cond;
                s.addAttributeMatchesListCondition( attr.getLocalName(), attr.getValue() );
                break;
            case org.w3c.css.sac.Condition.SAC_POSITIONAL_CONDITION:
                org.w3c.css.sac.PositionalCondition pos = (org.w3c.css.sac.PositionalCondition)cond;
                s.addFirstChildCondition();
                break;
            case org.w3c.css.sac.Condition.SAC_PSEUDO_CLASS_CONDITION:
                attr = (org.w3c.css.sac.AttributeCondition)cond;
                if ( attr.getValue().equals( "link" ) ) {
                    s.setPseudoClass( net.homelinux.tobe.xhtmlrenderer.AttributeResolver.LINK_PSEUDOCLASS );
                }
                if ( attr.getValue().equals( "visited" ) ) {
                    s.setPseudoClass( net.homelinux.tobe.xhtmlrenderer.AttributeResolver.VISITED_PSEUDOCLASS );
                }
                if ( attr.getValue().equals( "hover" ) ) {
                    s.setPseudoClass( net.homelinux.tobe.xhtmlrenderer.AttributeResolver.HOVER_PSEUDOCLASS );
                }
                if ( attr.getValue().equals( "active" ) ) {
                    s.setPseudoClass( net.homelinux.tobe.xhtmlrenderer.AttributeResolver.ACTIVE_PSEUDOCLASS );
                }
                if ( attr.getValue().equals( "focus" ) ) {
                    s.setPseudoClass( net.homelinux.tobe.xhtmlrenderer.AttributeResolver.FOCUS_PSEUDOCLASS );
                }
                break;
            default:
                System.err.println( "Bad condition" );
        }
    }

    public net.homelinux.tobe.xhtmlrenderer.CascadedStyle getCascadedStyle(org.w3c.dom.Element e) {
        if(_csMap == null) _csMap = new java.util.HashMap();
        net.homelinux.tobe.xhtmlrenderer.CascadedStyle cs = (net.homelinux.tobe.xhtmlrenderer.CascadedStyle)_csMap.get(e);
        if(cs != null) return cs;
        
        net.homelinux.tobe.xhtmlrenderer.Ruleset elementStyling = getElementStyle(e);
        String fingerprint = null;
        if(elementStyling == null) {//try to re-use a CascadedStyle
            StringBuffer sb = new StringBuffer();
            for(java.util.Iterator i = getMatchedRulesets(e); i.hasNext();) {
                sb.append(i.next());
                sb.append(":");
            }
            fingerprint = sb.toString();
            if(_csCache == null) _csCache = new java.util.HashMap();
            cs = (net.homelinux.tobe.xhtmlrenderer.CascadedStyle) _csCache.get(fingerprint);
        }
        
        if(cs == null) {
            java.util.List propList = new java.util.LinkedList();
            for(java.util.Iterator i = getMatchedRulesets(e); i.hasNext();) {
                net.homelinux.tobe.xhtmlrenderer.Ruleset rs = (net.homelinux.tobe.xhtmlrenderer.Ruleset) i.next();
                for(java.util.Iterator j = rs.getPropertyDeclarations(); j.hasNext();) {
                    propList.add((net.homelinux.tobe.xhtmlrenderer.PropertyDeclaration) j.next());
                }
            }
            if(elementStyling != null) {
                for(java.util.Iterator j = elementStyling.getPropertyDeclarations(); j.hasNext();) {
                    propList.add((net.homelinux.tobe.xhtmlrenderer.PropertyDeclaration) j.next());
                }
            }
            cs = new CascadedStyle(propList.iterator() );
        
            if(elementStyling == null) {
                _csCache.put(fingerprint, cs);
            }
        }
        _csMap.put(e, cs);
        
        return cs;
}
    
    private Mapper getMapper(org.w3c.dom.Element e) {
        if(_map == null) {
            _map = new java.util.HashMap();
            docMapper.mapChildren(_doc);
        }
        return (Mapper) _map.get(e);
    }
    
        
    private java.util.Iterator getMatchedRulesets(org.w3c.dom.Element e) {
        final Mapper m = getMapper(e);
        return new java.util.Iterator() {
            java.util.Iterator selectors = m.mappedSelectors.iterator();
            public boolean hasNext() {
                return selectors.hasNext();
            }
            public Object next() {
                if(hasNext()) return ((Selector)selectors.next()).getRuleset();
                else throw new java.util.NoSuchElementException();
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
   }
    
    private net.homelinux.tobe.xhtmlrenderer.Ruleset getElementStyle(org.w3c.dom.Element e) {
        if(_elStyle == null) return null;
        net.homelinux.tobe.xhtmlrenderer.Ruleset rs = (net.homelinux.tobe.xhtmlrenderer.Ruleset) _elStyle.get(e);
        if(rs == null) {
            rs = _styleFactory.parseStyleDeclaration(net.homelinux.tobe.xhtmlrenderer.Stylesheet.AUTHOR, _attRes.getElementStyling(e));
            _elStyle.put(e, rs);
        }
        return rs;
    }

    public void setStylesheetFactory(net.homelinux.tobe.xhtmlrenderer.StylesheetFactory styleFactory) {
        _styleFactory = styleFactory;
        //do not have to clear all maps here
        _elStyle = null;
        if(_styleFactory != null) _elStyle = new java.util.HashMap();
        _csMap = null;
    }
    
    private org.w3c.dom.Document _doc;
    Mapper docMapper;
    private net.homelinux.tobe.xhtmlrenderer.AttributeResolver _attRes;
    private java.util.HashMap _map;
    private java.util.HashMap _csMap;
    private java.util.HashMap _csCache;
    private java.util.HashMap _elStyle;
    private net.homelinux.tobe.xhtmlrenderer.StylesheetFactory _styleFactory;

}
