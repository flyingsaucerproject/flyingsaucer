/*
 * Matcher.java
 * Copyright (c) 2004, 2005 Torbj�rn Gannholm
 * Copyright (c) 2006 Wisconsin Court System
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
package org.xhtmlrenderer.css.newmatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.xhtmlrenderer.css.extend.AttributeResolver;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.extend.TreeResolver;
import org.xhtmlrenderer.css.sheet.MediaRule;
import org.xhtmlrenderer.css.sheet.PageRule;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.util.XRLog;


/**
 * @author Torbj�rn Gannholm
 */
public class Matcher {

    Mapper docMapper;
    private org.xhtmlrenderer.css.extend.AttributeResolver _attRes;
    private org.xhtmlrenderer.css.extend.TreeResolver _treeRes;
    private org.xhtmlrenderer.css.extend.StylesheetFactory _styleFactory;

    private java.util.Map _map;

    //handle dynamic
    private Set _hoverElements;
    private Set _activeElements;
    private Set _focusElements;
    private Set _visitElements;
    
    private List _pageRules;
    
    private Map _elementStyles;
    private Map _nonCSSStyles;

    public Matcher(TreeResolver tr, AttributeResolver ar, 
            StylesheetFactory factory, List stylesheetInfos, List stylesheets, String medium) {
        newMaps();
        _treeRes = tr;
        _attRes = ar;
        _styleFactory = factory;
        
        if (stylesheetInfos != null) {
            docMapper = legacyCreateDocumentMapper(stylesheetInfos.iterator(), medium);
            _pageRules = new ArrayList();
            collectPageRules(stylesheetInfos, _pageRules, medium);
        } else {
            _pageRules = new ArrayList();
            docMapper = createDocumentMapper(stylesheets, medium);
        }
    }

    public CascadedStyle getCascadedStyle(Object e, boolean restyle) {
        synchronized (e) {
            Mapper em;
            if (!restyle) {
                em = getMapper(e);
            } else {
                em = matchElement(e);
            }
            return em.getCascadedStyle(e);
        }
    }

    /**
     * May return null.
     * We assume that restyle has already been done by a getCascadedStyle if necessary.
     */
    public CascadedStyle getPECascadedStyle(Object e, String pseudoElement) {
        synchronized (e) {
            Mapper em = getMapper(e);
            return em.getPECascadedStyle(e, pseudoElement);
        }
    }
    
    public CascadedStyle getPageCascadedStyle() {
        return getPageCascadedStyle(null);
    }
    
    public CascadedStyle getPageCascadedStyle(String pseudoPage) {
        List props = new ArrayList();

        if (pseudoPage == null) {
            addPageRules(props, null);
        } else if (pseudoPage.equals("left") || pseudoPage.equals("right")) {
            addPageRules(props, null);
            addPageRules(props, pseudoPage);
        } else if (pseudoPage.equals("first")) {
            addPageRules(props, null);
            // assume first page is a left page
            addPageRules(props, "left");
            addPageRules(props, "first");
        }
        
        if (props.isEmpty()) {
            return CascadedStyle.emptyCascadedStyle;
        } else {
            return new CascadedStyle(props.iterator());
        }
    }
    
    private void addPageRules(List props, String pseudoPage) {
        for (Iterator i = _pageRules.iterator(); i.hasNext(); ) {
            Ruleset r = (Ruleset)i.next();
            boolean matches = 
                (pseudoPage == null && 
                    (r.getSelectorText() == null || r.getSelectorText().trim().equals("")) ) ||
                (r.getSelectorText() != null && r.getSelectorText().trim().equals(":" + pseudoPage));
            if (matches) {
                props.addAll(r.getPropertyDeclarations());
            }
        }
    }

    public boolean isVisitedStyled(Object e) {
        return _visitElements.contains(e);
    }

    public boolean isHoverStyled(Object e) {
        return _hoverElements.contains(e);
    }

    public boolean isActiveStyled(Object e) {
        return _activeElements.contains(e);
    }

    public boolean isFocusStyled(Object e) {
        return _focusElements.contains(e);
    }

    protected Mapper matchElement(Object e) {
        synchronized (e) {
            Object parent = _treeRes.getParentElement(e);
            Mapper child;
            if (parent != null) {
                Mapper m = getMapper(parent);
                child = m.mapChild(e);
            } else {//has to be document or fragment node
                child = docMapper.mapChild(e);
            }
            return child;
        }
    }

    Mapper legacyCreateDocumentMapper(java.util.Iterator stylesheets, String medium) {
        int count = 0;
        java.util.TreeMap sorter = new java.util.TreeMap();
        while (stylesheets.hasNext()) {
            count = legacyAppendStylesheet((StylesheetInfo) stylesheets.next(), count, sorter, medium);
        }
        XRLog.match("Matcher created with " + sorter.size() + " selectors");
        return new Mapper(sorter.values());
    }
    

    private int legacyAppendStylesheet(StylesheetInfo si, int count, java.util.TreeMap sorter, String medium) {
        if (!si.appliesToMedia(medium)) {
            return count;
        }//this is logical, and also how firefox does it
        Stylesheet ss = si.getStylesheet();
        if (ss == null) {
            ss = _styleFactory.getStylesheet(si);
            si.setStylesheet(ss);
        }
        if (ss == null) {
            return count;
        }//couldn't load it
        for (java.util.Iterator rulesets = ss.getRulesets(); rulesets.hasNext();) {
            Object obj = rulesets.next();
            if (obj instanceof StylesheetInfo) {
                count = legacyAppendStylesheet((StylesheetInfo) obj, count, sorter, medium);
            } else {
                org.xhtmlrenderer.css.sheet.Ruleset r = (org.xhtmlrenderer.css.sheet.Ruleset) obj;
                //at this point all selectors in a ruleset must be placed on the descendant axis
                org.w3c.css.sac.SelectorList selector_list = r.getSelectorList();
                for (int i = 0; i < selector_list.getLength(); i++) {
                    org.w3c.css.sac.Selector selector = selector_list.item(i);
                    Selector s = addSelector(count++, r, selector);
                    sorter.put(s.getOrder(), s);
                }
            }
        }
        return count;
    }
    
    Mapper createDocumentMapper(List stylesheets, String medium) {
        java.util.TreeMap sorter = new java.util.TreeMap();
        addAllStylesheets(stylesheets, sorter, medium);
        XRLog.match("Matcher created with " + sorter.size() + " selectors");
        return new Mapper(sorter.values());
    }
    
    private void addAllStylesheets(List stylesheets, TreeMap sorter, String medium) {
        int count = 0;
        for (Iterator i = stylesheets.iterator(); i.hasNext(); ) {
            Stylesheet stylesheet = (Stylesheet)i.next();
            for (Iterator j = stylesheet.getContents().iterator(); j.hasNext(); ) {
                Object obj = (Object)j.next();
                if (obj instanceof Ruleset) {
                    for (Iterator k = ((Ruleset)obj).getFSSelectors().iterator(); k.hasNext(); ) {
                        Selector selector = (Selector)k.next();
                        selector.setPos(++count);
                        sorter.put(selector.getOrder(), selector);
                    }
                } else if (obj instanceof PageRule) {
                    _pageRules.add(((PageRule)obj).getRuleset());
                } else if (obj instanceof MediaRule) {
                    MediaRule mediaRule = (MediaRule)obj;
                    if (mediaRule.matches(medium)) {
                        for (Iterator k = mediaRule.getContents().iterator(); k.hasNext(); ) {
                            Ruleset ruleset = (Ruleset)k.next();
                            for (Iterator l = ruleset.getFSSelectors().iterator(); l.hasNext(); ) {
                                Selector selector = (Selector)l.next();
                                selector.setPos(++count);
                                sorter.put(selector.getOrder(), selector);
                            }
                        }
                    }
                }
            }
        }
    }

    private void link(Object e, Mapper m) {
        _map.put(e, m);
    }

    private void newMaps() {
        _map = Collections.synchronizedMap(new java.util.HashMap());
        _hoverElements = Collections.synchronizedSet(new java.util.HashSet());
        _activeElements = Collections.synchronizedSet(new java.util.HashSet());
        _focusElements = Collections.synchronizedSet(new java.util.HashSet());
        _visitElements = Collections.synchronizedSet(new java.util.HashSet());
    }
    
    private void collectPageRules(List infos, List pageRulesets, String medium) {
        for (Iterator i = infos.iterator(); i.hasNext(); ) {
            StylesheetInfo si = (StylesheetInfo)i.next();
            collectPageRules(si, pageRulesets, medium);
        }
    }
    
    private void collectPageRules(StylesheetInfo si, List pageRulesets, String medium) {
        if (! si.appliesToMedia(medium)) {
            return;
        }
        Stylesheet ss = si.getStylesheet();
        if (ss == null) {
            ss = _styleFactory.getStylesheet(si);
            si.setStylesheet(ss);
        }
        if (ss == null) {
            return;
        }
        
        pageRulesets.addAll(ss.getPageRulesets());
        
        for (java.util.Iterator rulesets = ss.getRulesets(); rulesets.hasNext();) {
            Object obj = rulesets.next();
            if (obj instanceof StylesheetInfo) {
                collectPageRules((StylesheetInfo)obj, pageRulesets, medium);
            }
        }
    }

    /**
     * Turn the selection logic inside-out: we want to start as close to the
     * root element as possible, while sac starts at the final selected element
     */
    private Selector addSelector(int pos, org.xhtmlrenderer.css.sheet.Ruleset rs, org.w3c.css.sac.Selector selector) {
        Selector s = null;
        if (selector.getSelectorType() == org.w3c.css.sac.Selector.SAC_DIRECT_ADJACENT_SELECTOR) {
            s = addSiblingSelector(pos, rs, selector);
        } else if (selector.getSelectorType() == org.w3c.css.sac.Selector.SAC_CHILD_SELECTOR) {
            s = addSelector(pos, rs, ((org.w3c.css.sac.DescendantSelector) selector).getAncestorSelector());
            addChainedSelector(s, selector);
        } else if (selector.getSelectorType() == org.w3c.css.sac.Selector.SAC_DESCENDANT_SELECTOR) {
            s = addSelector(pos, rs, ((org.w3c.css.sac.DescendantSelector) selector).getAncestorSelector());
            addChainedSelector(s, selector);
        } else if (selector.getSelectorType() == org.w3c.css.sac.Selector.SAC_CONDITIONAL_SELECTOR) {
            org.w3c.css.sac.Condition cond = ((org.w3c.css.sac.ConditionalSelector) selector).getCondition();
            s = addSelector(pos, rs, ((org.w3c.css.sac.ConditionalSelector) selector).getSimpleSelector());
            addConditions(s, cond);
        } else if (selector.getSelectorType() == org.w3c.css.sac.Selector.SAC_ELEMENT_NODE_SELECTOR) {
            s = new Selector(pos, rs, Selector.DESCENDANT_AXIS, ((org.w3c.css.sac.ElementSelector) selector).getLocalName());
        } else {
            XRLog.exception("unsupported selector in addSelector: " + selector.getSelectorType());
        }

        return s;
    }

    private void addChainedSelector(Selector s, org.w3c.css.sac.Selector selector) {
        int axis = 0;
        org.w3c.css.sac.SimpleSelector simple = null;
        switch (selector.getSelectorType()) {
            case org.w3c.css.sac.Selector.SAC_CHILD_SELECTOR:
                axis = Selector.CHILD_AXIS;
                simple = ((org.w3c.css.sac.DescendantSelector) selector).getSimpleSelector();
                break;
            case org.w3c.css.sac.Selector.SAC_DESCENDANT_SELECTOR:
                axis = Selector.DESCENDANT_AXIS;
                simple = ((org.w3c.css.sac.DescendantSelector) selector).getSimpleSelector();
                break;
            default:
                System.err.println("Bad selector in addChainedSelector");
        }

        org.w3c.css.sac.Condition cond = null;
        if (simple.getSelectorType() == org.w3c.css.sac.Selector.SAC_CONDITIONAL_SELECTOR) {
            cond = ((org.w3c.css.sac.ConditionalSelector) simple).getCondition();
            //if ConditionalSelectors can be nested, we are in trouble here
            simple = ((org.w3c.css.sac.ConditionalSelector) simple).getSimpleSelector();
        }
        if (simple.getSelectorType() == org.w3c.css.sac.Selector.SAC_ELEMENT_NODE_SELECTOR) {
            s = s.appendChainedSelector(axis, ((org.w3c.css.sac.ElementSelector) simple).getLocalName());
        }
        if (cond != null) {
            addConditions(s, cond);
        }
    }

    private Selector addSiblingSelector(int pos, org.xhtmlrenderer.css.sheet.Ruleset rs, org.w3c.css.sac.Selector siblingSelector) {
        int axis = Selector.DESCENDANT_AXIS;
        org.w3c.css.sac.SimpleSelector simple = null;
        org.w3c.css.sac.Selector sib = null;
        switch (siblingSelector.getSelectorType()) {
            case org.w3c.css.sac.Selector.SAC_DIRECT_ADJACENT_SELECTOR:
                simple = ((org.w3c.css.sac.SiblingSelector) siblingSelector).getSiblingSelector();
                sib = ((org.w3c.css.sac.SiblingSelector) siblingSelector).getSelector();
                break;
            default:
                XRLog.exception("Bad selector in addSiblingSelector");
        }

        Selector ancestor = null;
        Selector current = null;
        org.w3c.css.sac.Selector pa = sib;
        while (pa.getSelectorType() == org.w3c.css.sac.Selector.SAC_DIRECT_ADJACENT_SELECTOR) {
            pa = ((org.w3c.css.sac.SiblingSelector) pa).getSelector();
        }
        if (pa.getSelectorType() == org.w3c.css.sac.Selector.SAC_CHILD_SELECTOR) {
            ancestor = addSelector(pos, rs, ((org.w3c.css.sac.DescendantSelector) pa).getAncestorSelector());
            axis = Selector.CHILD_AXIS;
        } else if (pa.getSelectorType() == org.w3c.css.sac.Selector.SAC_DESCENDANT_SELECTOR) {
            ancestor = addSelector(pos, rs, ((org.w3c.css.sac.DescendantSelector) pa).getAncestorSelector());
        } else if (pa.getSelectorType() == org.w3c.css.sac.Selector.SAC_CONDITIONAL_SELECTOR) {
            //do nothing, no ancestor selector exists
        } else if (pa.getSelectorType() == org.w3c.css.sac.Selector.SAC_ELEMENT_NODE_SELECTOR) {
            //do nothing, no ancestor selector exists
        } else {
            XRLog.exception("unsupported selector in addSiblingSelector: " + pa.getSelectorType());
        }

        org.w3c.css.sac.Condition cond = null;
        if (simple.getSelectorType() == org.w3c.css.sac.Selector.SAC_CONDITIONAL_SELECTOR) {
            cond = ((org.w3c.css.sac.ConditionalSelector) simple).getCondition();
            //if ConditionalSelectors can be nested, we are in trouble here
            simple = ((org.w3c.css.sac.ConditionalSelector) simple).getSimpleSelector();
        }
        if (simple.getSelectorType() == org.w3c.css.sac.Selector.SAC_ELEMENT_NODE_SELECTOR) {
            if (ancestor == null) {
                current = new Selector(pos, rs, Selector.DESCENDANT_AXIS, ((org.w3c.css.sac.ElementSelector) simple).getLocalName());
                ancestor = current;
            } else {
                current = ancestor.appendChainedSelector(axis, ((org.w3c.css.sac.ElementSelector) simple).getLocalName());
            }
        }
        if (cond != null) {
            addConditions(current, cond);
        }

        while (sib.getSelectorType() == org.w3c.css.sac.Selector.SAC_DIRECT_ADJACENT_SELECTOR) {
            simple = ((org.w3c.css.sac.SiblingSelector) sib).getSiblingSelector();
            cond = null;
            Selector s = null;
            if (simple.getSelectorType() == org.w3c.css.sac.Selector.SAC_CONDITIONAL_SELECTOR) {
                cond = ((org.w3c.css.sac.ConditionalSelector) simple).getCondition();
                //if ConditionalSelectors can be nested, we are in trouble here
                simple = ((org.w3c.css.sac.ConditionalSelector) simple).getSimpleSelector();
            }
            if (simple.getSelectorType() == org.w3c.css.sac.Selector.SAC_ELEMENT_NODE_SELECTOR) {
                s = current.appendSiblingSelector(Selector.IMMEDIATE_SIBLING_AXIS, ((org.w3c.css.sac.ElementSelector) simple).getLocalName());
            }
            if (cond != null) {
                addConditions(s, cond);
            }
            sib = ((org.w3c.css.sac.SiblingSelector) sib).getSelector();
        }
        simple = null;
        switch (sib.getSelectorType()) {
            case org.w3c.css.sac.Selector.SAC_CHILD_SELECTOR:
                simple = ((org.w3c.css.sac.DescendantSelector) sib).getSimpleSelector();
                break;
            case org.w3c.css.sac.Selector.SAC_DESCENDANT_SELECTOR:
                simple = ((org.w3c.css.sac.DescendantSelector) sib).getSimpleSelector();
                break;
            default:
                simple = (org.w3c.css.sac.SimpleSelector) sib;
        }

        cond = null;
        Selector s = null;
        if (simple.getSelectorType() == org.w3c.css.sac.Selector.SAC_CONDITIONAL_SELECTOR) {
            cond = ((org.w3c.css.sac.ConditionalSelector) simple).getCondition();
            //if ConditionalSelectors can be nested, we are in trouble here
            simple = ((org.w3c.css.sac.ConditionalSelector) simple).getSimpleSelector();
        }
        if (simple.getSelectorType() == org.w3c.css.sac.Selector.SAC_ELEMENT_NODE_SELECTOR) {
            s = current.appendSiblingSelector(Selector.IMMEDIATE_SIBLING_AXIS, ((org.w3c.css.sac.ElementSelector) simple).getLocalName());
        }
        if (cond != null) {
            addConditions(s, cond);
        }
        return ancestor;
    }

    private void addConditions(Selector s, org.w3c.css.sac.Condition cond) {
        switch (cond.getConditionType()) {
            case org.w3c.css.sac.Condition.SAC_AND_CONDITION:
                org.w3c.css.sac.CombinatorCondition comb = (org.w3c.css.sac.CombinatorCondition) cond;
                addConditions(s, comb.getFirstCondition());
                addConditions(s, comb.getSecondCondition());
                break;
            case org.w3c.css.sac.Condition.SAC_ATTRIBUTE_CONDITION:
                org.w3c.css.sac.AttributeCondition attr = (org.w3c.css.sac.AttributeCondition) cond;
                if (attr.getSpecified()) {
                    s.addAttributeEqualsCondition(attr.getLocalName(), attr.getValue());
                } else {
                    s.addAttributeExistsCondition(attr.getLocalName());
                }
                break;
            case org.w3c.css.sac.Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
                attr = (org.w3c.css.sac.AttributeCondition) cond;
                s.addAttributeMatchesFirstPartCondition(attr.getLocalName(), attr.getValue());
                break;
            case org.w3c.css.sac.Condition.SAC_CLASS_CONDITION:
                attr = (org.w3c.css.sac.AttributeCondition) cond;
                s.addClassCondition(attr.getValue());
                break;
            case org.w3c.css.sac.Condition.SAC_ID_CONDITION:
                attr = (org.w3c.css.sac.AttributeCondition) cond;
                s.addIDCondition(attr.getValue());
                break;
            case org.w3c.css.sac.Condition.SAC_LANG_CONDITION:
                org.w3c.css.sac.LangCondition lang = (org.w3c.css.sac.LangCondition) cond;
                s.addLangCondition(lang.getLang());
                break;
            case org.w3c.css.sac.Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
                attr = (org.w3c.css.sac.AttributeCondition) cond;
                s.addAttributeMatchesListCondition(attr.getLocalName(), attr.getValue());
                break;
            case org.w3c.css.sac.Condition.SAC_POSITIONAL_CONDITION:
                org.w3c.css.sac.PositionalCondition pos = (org.w3c.css.sac.PositionalCondition) cond;
                if (pos.getPosition() == 0) {
                    s.addFirstChildCondition();
                } else {
                    s.addUnsupportedCondition();
                }
                break;
            case org.w3c.css.sac.Condition.SAC_PSEUDO_CLASS_CONDITION:
                attr = (org.w3c.css.sac.AttributeCondition) cond;
                if (attr.getValue().equals("link")) {
                    s.addLinkCondition();
                } else if (attr.getValue().equals("visited")) {
                    s.setPseudoClass(Selector.VISITED_PSEUDOCLASS);
                } else if (attr.getValue().equals("hover")) {
                    s.setPseudoClass(Selector.HOVER_PSEUDOCLASS);
                } else if (attr.getValue().equals("active")) {
                    s.setPseudoClass(Selector.ACTIVE_PSEUDOCLASS);
                } else if (attr.getValue().equals("focus")) {
                    s.setPseudoClass(Selector.FOCUS_PSEUDOCLASS);
                } else if (attr.getValue().equals("first-child")) {
                    // With SS CSS parser, :first-child is reported here
                    // and not above.  Seems like a bug, but easy enough
                    // to work around here
                    s.addFirstChildCondition();
                } else {//it must be a pseudo-element
                    s.setPseudoElement(attr.getValue());
                }
                break;
            default:
                System.err.println("Bad condition");
        }
    }

    private Mapper getMapper(Object e) {
        Mapper m = (Mapper) _map.get(e);
        if (m != null) {
            return m;
        }
        m = matchElement(e);
        return m;
    }

    private static java.util.Iterator getMatchedRulesets(final List mappedSelectors) {
        return
                new java.util.Iterator() {
                    java.util.Iterator selectors = mappedSelectors.iterator();

                    public boolean hasNext() {
                        return selectors.hasNext();
                    }

                    public Object next() {
                        if (hasNext()) {
                            return ((Selector) selectors.next()).getRuleset();
                        } else {
                            throw new java.util.NoSuchElementException();
                        }
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
    }

    private static java.util.Iterator getSelectedRulesets(java.util.List selectorList) {
        final java.util.List sl = selectorList;
        return
                new java.util.Iterator() {
                    java.util.Iterator selectors = sl.iterator();

                    public boolean hasNext() {
                        return selectors.hasNext();
                    }

                    public Object next() {
                        if (hasNext()) {
                            return ((Selector) selectors.next()).getRuleset();
                        } else {
                            throw new java.util.NoSuchElementException();
                        }
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
    }

    private org.xhtmlrenderer.css.sheet.Ruleset getElementStyle(Object e) {
        synchronized (e) {
            if (_attRes == null || _styleFactory == null) {
                return null;
            }
            if (_elementStyles == null) {
                String style = _attRes.getElementStyling(e);
                if (style == null || style.equals("")) {
                    return null;
                }
                return _styleFactory.parseStyleDeclaration(org.xhtmlrenderer.css.sheet.StylesheetInfo.AUTHOR, style);
            } else {
                return (Ruleset)_elementStyles.get(e);
            }
        }
    }

    private org.xhtmlrenderer.css.sheet.Ruleset getNonCssStyle(Object e) {
        synchronized (e) {
            if (_attRes == null || _styleFactory == null) {
                return null;
            }
            if (_nonCSSStyles == null) {
                String style = _attRes.getNonCssStyling(e);
                if (style == null || style.equals("")) {
                    return null;
                }
                return _styleFactory.parseStyleDeclaration(org.xhtmlrenderer.css.sheet.StylesheetInfo.AUTHOR, style);
            } else {
                return (Ruleset)_nonCSSStyles.get(e);
            }
        }
    }

    /**
     * Mapper represents a local CSS for a Node that is used to match the Node's
     * children.
     *
     * @author Torbj�rn Gannholm
     */
    class Mapper {
        java.util.List axes;
        private HashMap pseudoSelectors;
        private List mappedSelectors;
        private HashMap children;

        Mapper(java.util.Collection selectors) {
            axes = new java.util.ArrayList();
            axes.addAll(selectors);
        }

        private Mapper() {
        }

        /**
         * Side effect: creates and stores a Mapper for the element
         *
         * @param e
         * @return The selectors that matched, sorted according to specificity
         *         (more correct: preserves the sort order from Matcher creation)
         */
        Mapper mapChild(Object e) {
            //Mapper childMapper = new Mapper();
            java.util.List childAxes = new ArrayList();
            java.util.HashMap pseudoSelectors = new java.util.HashMap();
            java.util.List mappedSelectors = new java.util.LinkedList();
            StringBuffer key = new StringBuffer();
            for (int i = 0; i < axes.size(); i++) {
                Selector sel = (Selector) axes.get(i);
                if (sel.getAxis() == Selector.DESCENDANT_AXIS) {
                    //carry it forward to other descendants
                    childAxes.add(sel);
                } else if (sel.getAxis() == Selector.IMMEDIATE_SIBLING_AXIS) {
                    throw new RuntimeException();
                }
                if (!sel.matches(e, _attRes, _treeRes)) {
                    continue;
                }
                //Assumption: if it is a pseudo-element, it does not also have dynamic pseudo-class
                String pseudoElement = sel.getPseudoElement();
                if (pseudoElement != null) {
                    java.util.List l = (java.util.List) pseudoSelectors.get(pseudoElement);
                    if (l == null) {
                        l = new java.util.LinkedList();
                        pseudoSelectors.put(pseudoElement, l);
                    }
                    l.add(sel);
                    key.append(sel.getSelectorID()).append(":");
                    continue;
                }
                if (sel.isPseudoClass(Selector.VISITED_PSEUDOCLASS)) {
                    _visitElements.add(e);
                }
                if (sel.isPseudoClass(Selector.ACTIVE_PSEUDOCLASS)) {
                    _activeElements.add(e);
                }
                if (sel.isPseudoClass(Selector.HOVER_PSEUDOCLASS)) {
                    _hoverElements.add(e);
                }
                if (sel.isPseudoClass(Selector.FOCUS_PSEUDOCLASS)) {
                    _focusElements.add(e);
                }
                if (!sel.matchesDynamic(e, _attRes, _treeRes)) {
                    continue;
                }
                key.append(sel.getSelectorID()).append(":");
                Selector chain = sel.getChainedSelector();
                if (chain == null) {
                    mappedSelectors.add(sel);
                } else if (chain.getAxis() == Selector.IMMEDIATE_SIBLING_AXIS) {
                    throw new RuntimeException();
                } else {
                    childAxes.add(chain);
                }
            }
            if (children == null) children = new HashMap();
            Mapper childMapper = (Mapper) children.get(key.toString());
            if (childMapper == null) {
                childMapper = new Mapper();
                childMapper.axes = childAxes;
                childMapper.pseudoSelectors = pseudoSelectors;
                childMapper.mappedSelectors = mappedSelectors;
                children.put(key.toString(), childMapper);
            }
            link(e, childMapper);
            return childMapper;
        }

        CascadedStyle getCascadedStyle(Object e) {
            CascadedStyle result;
            synchronized (e) {
                CascadedStyle cs = null;
                org.xhtmlrenderer.css.sheet.Ruleset elementStyling = getElementStyle(e);
                org.xhtmlrenderer.css.sheet.Ruleset nonCssStyling = getNonCssStyle(e);
                List propList = new LinkedList();
                //specificity 0,0,0,0
                if (nonCssStyling != null) {
                    propList.addAll(nonCssStyling.getPropertyDeclarations());
                }
                //these should have been returned in order of specificity
                for (Iterator i = getMatchedRulesets(mappedSelectors); i.hasNext();) {
                    org.xhtmlrenderer.css.sheet.Ruleset rs = (org.xhtmlrenderer.css.sheet.Ruleset) i.next();
                    propList.addAll(rs.getPropertyDeclarations());
                }
                //specificity 1,0,0,0
                if (elementStyling != null) {
                    propList.addAll(elementStyling.getPropertyDeclarations());
                }
                if (propList.size() == 0)
                    cs = CascadedStyle.emptyCascadedStyle;
                else {
                    cs = new CascadedStyle(propList.iterator());
                }

                result = cs;
            }
            return result;
        }

        /**
         * May return null.
         * We assume that restyle has already been done by a getCascadedStyle if necessary.
         */
        public CascadedStyle getPECascadedStyle(Object e, String pseudoElement) {
            java.util.Iterator si = pseudoSelectors.entrySet().iterator();
            if (!si.hasNext()) {
                return null;
            }
            CascadedStyle cs = null;
            java.util.List pe = (java.util.List) pseudoSelectors.get(pseudoElement);
            if (pe == null) return null;

            java.util.List propList = new java.util.LinkedList();
            for (java.util.Iterator i = getSelectedRulesets(pe); i.hasNext();) {
                org.xhtmlrenderer.css.sheet.Ruleset rs = (org.xhtmlrenderer.css.sheet.Ruleset) i.next();
                propList.addAll(rs.getPropertyDeclarations());
            }
            if (propList.size() == 0)
                cs = CascadedStyle.emptyCascadedStyle;//already internalized
            else {
                cs = new CascadedStyle(propList.iterator());
            }
            return cs;
        }
    }

    public Map getElementStyles() {
        return _elementStyles;
    }

    public void setElementStyles(Map elementStyles) {
        _elementStyles = elementStyles;
    }

    public Map getNonCSSStyles() {
        return _nonCSSStyles;
    }

    public void setNonCSSStyles(Map nonCSSStyles) {
        _nonCSSStyles = nonCSSStyles;
    }
}

