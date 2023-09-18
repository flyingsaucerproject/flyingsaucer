/*
 * Matcher.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.constants.MarginBoxName;
import org.xhtmlrenderer.css.extend.AttributeResolver;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.extend.TreeResolver;
import org.xhtmlrenderer.css.sheet.FontFaceRule;
import org.xhtmlrenderer.css.sheet.MediaRule;
import org.xhtmlrenderer.css.sheet.PageRule;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.util.Util;
import org.xhtmlrenderer.util.XRLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import static java.util.Collections.synchronizedMap;
import static java.util.Collections.synchronizedSet;
import static java.util.Comparator.comparingLong;


/**
 * @author Torbjoern Gannholm
 */
public class Matcher {

    private final Mapper docMapper;
    private final AttributeResolver _attRes;
    private final TreeResolver _treeRes;
    private final StylesheetFactory _styleFactory;

    private final Map<Node, Mapper> _map = synchronizedMap(new HashMap<>());

    //handle dynamic
    private final Set<Node> _hoverElements = synchronizedSet(new HashSet<>());
    private final Set<Node> _activeElements = synchronizedSet(new HashSet<>());
    private final Set<Node> _focusElements = synchronizedSet(new HashSet<>());
    private final Set<Node> _visitElements = synchronizedSet(new HashSet<>());
    private final List<PageRule> _pageRules = new ArrayList<>();
    private final List<FontFaceRule> _fontFaceRules = new ArrayList<>();

    public Matcher(TreeResolver tr, AttributeResolver ar, 
                   StylesheetFactory factory, List<Stylesheet> stylesheets, String medium) {
        _treeRes = tr;
        _attRes = ar;
        _styleFactory = factory;
        docMapper = createDocumentMapper(stylesheets, medium);
    }

    public void removeStyle(Element e) {
        _map.remove(e);
    }

    public CascadedStyle getCascadedStyle(Element e, boolean restyle) {
        synchronized (e) {
            Mapper em = restyle ? matchElement(e) : getMapper(e);
            return em.getCascadedStyle(e);
        }
    }

    /**
     * May return null.
     * We assume that restyle has already been done by a getCascadedStyle if necessary.
     */
    public CascadedStyle getPECascadedStyle(Element e, String pseudoElement) {
        synchronized (e) {
            Mapper em = getMapper(e);
            return em.getPECascadedStyle(e, pseudoElement);
        }
    }

    public PageInfo getPageCascadedStyle(String pageName, String pseudoPage) {
        List<PropertyDeclaration> props = new ArrayList<>();
        Map<MarginBoxName, List<PropertyDeclaration>> marginBoxes = new HashMap<>();

        for (PageRule pageRule : _pageRules) {
            if (pageRule.applies(pageName, pseudoPage)) {
                props.addAll(pageRule.getRuleset().getPropertyDeclarations());
                marginBoxes.putAll(pageRule.getMarginBoxes());
            }
        }

        CascadedStyle style = props.isEmpty() ? CascadedStyle.emptyCascadedStyle : new CascadedStyle(props);
        return new PageInfo(props, style, marginBoxes);
    }

    public List<FontFaceRule> getFontFaceRules() {
        return _fontFaceRules;
    }

    public boolean isVisitedStyled(Node e) {
        return _visitElements.contains(e);
    }

    public boolean isHoverStyled(Node e) {
        return _hoverElements.contains(e);
    }

    public boolean isActiveStyled(Node e) {
        return _activeElements.contains(e);
    }

    public boolean isFocusStyled(Node e) {
        return _focusElements.contains(e);
    }

    protected Mapper matchElement(Node e) {
        synchronized (e) {
            Node parent = _treeRes.getParentElement(e);
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

    Mapper createDocumentMapper(List<Stylesheet> stylesheets, String medium) {
        Map<String, Selector> sorter = new TreeMap<>();
        addAllStylesheets(stylesheets, sorter, medium);
        XRLog.match("Matcher created with " + sorter.size() + " selectors");
        return new Mapper(sorter.values());
    }

    private void addAllStylesheets(List<Stylesheet> stylesheets, Map<String, Selector> sorter, String medium) {
        int count = 0;
        int pCount = 0;
        for (Stylesheet stylesheet : stylesheets) {
            for (Object obj : stylesheet.getContents()) {
                if (obj instanceof Ruleset) {
                    for (Selector selector : ((Ruleset) obj).getFSSelectors()) {
                        selector.setPos(++count);
                        sorter.put(selector.getOrder(), selector);
                    }
                } else if (obj instanceof PageRule) {
                    PageRule pageRule = (PageRule) obj;
                    pageRule.setPos(++pCount);
                    _pageRules.add(pageRule);
                } else if (obj instanceof MediaRule) {
                    MediaRule mediaRule = (MediaRule) obj;
                    if (mediaRule.matches(medium)) {
                        for (Ruleset ruleset : mediaRule.getContents()) {
                            for (Selector selector : ruleset.getFSSelectors()) {
                                selector.setPos(++count);
                                sorter.put(selector.getOrder(), selector);
                            }
                        }
                    }
                }
            }

            _fontFaceRules.addAll(stylesheet.getFontFaceRules());
        }

        _pageRules.sort(comparingLong(PageRule::getOrder));
    }

    private void link(Node e, Mapper m) {
        _map.put(e, m);
    }

    private Mapper getMapper(Node e) {
        Mapper m = _map.get(e);
        if (m != null) {
            return m;
        }
        m = matchElement(e);
        return m;
    }

    private static Iterator<Ruleset> getMatchedRulesets(final List<Selector> mappedSelectors) {
        return
                new Iterator<Ruleset>() {
                    final Iterator<Selector> selectors = mappedSelectors.iterator();

                    @Override
                    public boolean hasNext() {
                        return selectors.hasNext();
                    }

                    @Override
                    public Ruleset next() {
                        if (hasNext()) {
                            return selectors.next().getRuleset();
                        } else {
                            throw new NoSuchElementException();
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
    }

    private static Iterator<Ruleset> getSelectedRulesets(List<Selector> selectorList) {
        final List<Selector> sl = selectorList;
        return
                new Iterator<Ruleset>() {
                    final Iterator<Selector> selectors = sl.iterator();

                    @Override
                    public boolean hasNext() {
                        return selectors.hasNext();
                    }

                    @Override
                    public Ruleset next() {
                        if (hasNext()) {
                            return selectors.next().getRuleset();
                        } else {
                            throw new NoSuchElementException();
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
    }

    private Ruleset getElementStyle(Node e) {
        synchronized (e) {
            if (_attRes == null || _styleFactory == null) {
                return null;
            }

            String style = _attRes.getElementStyling(e);
            if (Util.isNullOrEmpty(style)) {
                return null;
            }

            return _styleFactory.parseStyleDeclaration(org.xhtmlrenderer.css.sheet.StylesheetInfo.AUTHOR, style);
        }
    }

    private Ruleset getNonCssStyle(Node e) {
        synchronized (e) {
            if (_attRes == null || _styleFactory == null) {
                return null;
            }
            String style = _attRes.getNonCssStyling(e);
            if (Util.isNullOrEmpty(style)) {
                return null;
            }
            return _styleFactory.parseStyleDeclaration(org.xhtmlrenderer.css.sheet.StylesheetInfo.AUTHOR, style);
        }
    }

    /**
     * Mapper represents a local CSS for a Node that is used to match the Node's
     * children.
     *
     * @author Torbjoern Gannholm
     */
    protected class Mapper {
        private List<Selector> axes;
        private Map<String, List<Selector>> pseudoSelectors;
        private List<Selector> mappedSelectors;
        private Map<String, Mapper> children;

        Mapper(Collection<Selector> selectors) {
            axes = new ArrayList<>(selectors.size());
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
        Mapper mapChild(Node e) {
            //Mapper childMapper = new Mapper();
            List<Selector> childAxes = new ArrayList<>(axes.size() + 10);
            Map<String, List<Selector>> pseudoSelectors = new HashMap<>();
            List<Selector> mappedSelectors = new LinkedList<>();
            StringBuilder key = new StringBuilder();
            for (Selector axe : axes) {
                if (axe.getAxis() == Selector.DESCENDANT_AXIS) {
                    //carry it forward to other descendants
                    childAxes.add(axe);
                } else if (axe.getAxis() == Selector.IMMEDIATE_SIBLING_AXIS) {
                    throw new RuntimeException();
                }
                if (!axe.matches(e, _attRes, _treeRes)) {
                    continue;
                }
                //Assumption: if it is a pseudo-element, it does not also have dynamic pseudo-class
                String pseudoElement = axe.getPseudoElement();
                if (pseudoElement != null) {
                    List<Selector> l = pseudoSelectors.computeIfAbsent(pseudoElement, k -> new LinkedList<>());
                    l.add(axe);
                    key.append(axe.getSelectorID()).append(":");
                    continue;
                }
                if (axe.isPseudoClass(Selector.VISITED_PSEUDOCLASS)) {
                    _visitElements.add(e);
                }
                if (axe.isPseudoClass(Selector.ACTIVE_PSEUDOCLASS)) {
                    _activeElements.add(e);
                }
                if (axe.isPseudoClass(Selector.HOVER_PSEUDOCLASS)) {
                    _hoverElements.add(e);
                }
                if (axe.isPseudoClass(Selector.FOCUS_PSEUDOCLASS)) {
                    _focusElements.add(e);
                }
                if (!axe.matchesDynamic(e, _attRes, _treeRes)) {
                    continue;
                }
                key.append(axe.getSelectorID()).append(":");
                Selector chain = axe.getChainedSelector();
                if (chain == null) {
                    mappedSelectors.add(axe);
                } else if (chain.getAxis() == Selector.IMMEDIATE_SIBLING_AXIS) {
                    throw new RuntimeException();
                } else {
                    childAxes.add(chain);
                }
            }
            if (children == null) children = new HashMap<>();
            Mapper childMapper = children.get(key.toString());
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

        CascadedStyle getCascadedStyle(Node e) {
            synchronized (e) {
                Ruleset elementStyling = getElementStyle(e);
                Ruleset nonCssStyling = getNonCssStyle(e);
                List<PropertyDeclaration> propList = new ArrayList<>();
                //specificity 0,0,0,0
                if (nonCssStyling != null) {
                    propList.addAll(nonCssStyling.getPropertyDeclarations());
                }
                //these should have been returned in order of specificity
                for (Iterator<Ruleset> i = getMatchedRulesets(mappedSelectors); i.hasNext();) {
                    Ruleset rs = i.next();
                    propList.addAll(rs.getPropertyDeclarations());
                }
                //specificity 1,0,0,0
                if (elementStyling != null) {
                    propList.addAll(elementStyling.getPropertyDeclarations());
                }
                return propList.isEmpty() ? CascadedStyle.emptyCascadedStyle : new CascadedStyle(propList);
            }
        }

        /**
         * May return null.
         * We assume that restyle has already been done by a getCascadedStyle if necessary.
         */
        public CascadedStyle getPECascadedStyle(Node e, String pseudoElement) {
            Iterator<Map.Entry<String, List<Selector>>> si = pseudoSelectors.entrySet().iterator();
            if (!si.hasNext()) {
                return null;
            }
            List<Selector> pe = pseudoSelectors.get(pseudoElement);
            if (pe == null) return null;

            List<PropertyDeclaration> propList = new ArrayList<>();
            for (Iterator<Ruleset> i = getSelectedRulesets(pe); i.hasNext();) {
                Ruleset rs = i.next();
                propList.addAll(rs.getPropertyDeclarations());
            }

            if (propList.size() == 0)
                return CascadedStyle.emptyCascadedStyle; // already internalized
            else {
                return new CascadedStyle(propList);
            }
        }
    }
}

