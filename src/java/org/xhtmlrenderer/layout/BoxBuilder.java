/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjoern Gannholm, Joshua Marinacci
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.extend.ContentFunction;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.newtable.TableBox;
import org.xhtmlrenderer.newtable.TableCellBox;
import org.xhtmlrenderer.newtable.TableColumn;
import org.xhtmlrenderer.newtable.TableRowBox;
import org.xhtmlrenderer.newtable.TableSectionBox;
import org.xhtmlrenderer.render.AnonymousBlockBox;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FloatedBoxData;
import org.xhtmlrenderer.render.InlineBox;

public class BoxBuilder {
    private static final Pattern CONTENT_NEWLINE = Pattern.compile("\\\\A");

    public static BlockBox createRootBox(LayoutContext c, Document document) {
        Element root = document.getDocumentElement();

        BlockBox result = new BlockBox();
        result.setElement(root);
        result.setStyle(c.getSharedContext().getStyle(root));

        return result;
    }

    public static void createChildren(LayoutContext c, BlockBox parent) {
        List children = new ArrayList();

        ChildBoxInfo info = new ChildBoxInfo();

        createChildren(c, parent, parent.getElement(), children, info);

        boolean parentIsNestingTableContent = isNestingTableContent(parent.getStyle().getIdent(
                CSSName.DISPLAY));
        if (!parentIsNestingTableContent && !info.isContainsTableContent()) {
            resolveChildren(c, parent, children, info);
        } else {
            stripAllWhitespace(children);
            if (parentIsNestingTableContent) {
                resolveTableContent(c, parent, children, info);
            } else {
                resolveChildTableContent(c, parent, children, info, IdentValue.TABLE_CELL);
            }
        }
    }

    private static void resolveChildren(
            LayoutContext c, BlockBox owner, List children, ChildBoxInfo info) {
        if (children.size() > 0) {
            if (info.isContainsBlockLevelContent()) {
                insertAnonymousBlocks(c.getSharedContext(), owner, children);
                owner.setChildrenContentType(BlockBox.CONTENT_BLOCK);
            } else {
                WhitespaceStripper.stripInlineContent(children);
                if (children.size() > 0) {
                    owner.setInlineContent(children);
                    owner.setChildrenContentType(BlockBox.CONTENT_INLINE);
                } else {
                    owner.setChildrenContentType(BlockBox.CONTENT_EMPTY);
                }
            }
        } else {
            owner.setChildrenContentType(BlockBox.CONTENT_EMPTY);
        }
    }

    private static boolean isAllProperTableNesting(IdentValue parentDisplay, List children) {
        for (Iterator i = children.iterator(); i.hasNext();) {
            Styleable child = (Styleable) i.next();
            if (!isProperTableNesting(parentDisplay, child.getStyle().getIdent(CSSName.DISPLAY))) {
                return false;
            }
        }

        return true;
    }

    private static void resolveChildTableContent(
            LayoutContext c, BlockBox parent, List children, ChildBoxInfo info, IdentValue target) {
        List childrenForAnonymous = new ArrayList();
        List childrenWithAnonymous = new ArrayList();

        IdentValue nextUp = getPreviousTableNestingLevel(target);
        for (Iterator i = children.iterator(); i.hasNext();) {
            Styleable styleable = (Styleable) i.next();

            if (matchesTableLevel(target, styleable.getStyle().getIdent(CSSName.DISPLAY))) {
                childrenForAnonymous.add(styleable);
            } else {
                if (childrenForAnonymous.size() > 0) {
                    createAnonymousTableContent(c, (BlockBox) childrenForAnonymous.get(0), nextUp,
                            childrenForAnonymous, childrenWithAnonymous);

                    childrenForAnonymous = new ArrayList();
                }
                childrenWithAnonymous.add(styleable);
            }
        }

        if (childrenForAnonymous.size() > 0) {
            createAnonymousTableContent(c, (BlockBox) childrenForAnonymous.get(0), nextUp,
                    childrenForAnonymous, childrenWithAnonymous);
        }

        if (nextUp == IdentValue.TABLE) {
            rebalanceInlineContent(childrenWithAnonymous);
            info.setContainsBlockLevelContent(true);
            resolveChildren(c, parent, childrenWithAnonymous, info);
        } else {
            resolveChildTableContent(c, parent, childrenWithAnonymous, info, nextUp);
        }
    }

    private static boolean matchesTableLevel(IdentValue target, IdentValue value) {
        if (target == IdentValue.TABLE_ROW_GROUP) {
            return value == IdentValue.TABLE_ROW_GROUP || value == IdentValue.TABLE_HEADER_GROUP
                    || value == IdentValue.TABLE_FOOTER_GROUP || value == IdentValue.TABLE_CAPTION;
        } else {
            return target == value;
        }
    }

    /**
     * Makes sure that any <code>InlineBox</code> in <code>content</code>
     * both starts and ends within <code>content</code>. Used to ensure that
     * it is always possible to construct anonymous blocks once an element's
     * children has been distributed among anonymous table objects.
     */
    private static void rebalanceInlineContent(List content) {
        Map boxesByElement = new HashMap();
        for (Iterator i = content.iterator(); i.hasNext();) {
            Styleable styleable = (Styleable) i.next();
            if (styleable instanceof InlineBox) {
                InlineBox iB = (InlineBox) styleable;
                Element elem = iB.getElement();

                if (!boxesByElement.containsKey(elem)) {
                    iB.setStartsHere(true);
                }

                boxesByElement.put(elem, iB);
            }
        }

        for (Iterator i = boxesByElement.values().iterator(); i.hasNext();) {
            InlineBox iB = (InlineBox) i.next();
            iB.setEndsHere(true);
        }
    }

    private static void stripAllWhitespace(List content) {
        int start = 0;
        int current = 0;
        boolean started = false;
        for (current = 0; current < content.size(); current++) {
            Styleable styleable = (Styleable) content.get(current);
            if (! styleable.getStyle().isLayedOutInInlineContext()) {
                if (started) {
                    int before = content.size();
                    WhitespaceStripper.stripInlineContent(content.subList(start, current));
                    int after = content.size();
                    current -= (before - after);
                }
                started = false;
            } else {
                if (! started) {
                    started = true;
                    start = current;
                }
            }
        }
        
        if (started) {
            WhitespaceStripper.stripInlineContent(content.subList(start, current));
        }
    }

    private static void resolveTableContent(
            LayoutContext c, BlockBox parent, List children, ChildBoxInfo info) {
        IdentValue parentDisplay = parent.getStyle().getIdent(CSSName.DISPLAY);
        IdentValue next = getNextTableNestingLevel(parentDisplay);
        if (next == null || isAllProperTableNesting(parentDisplay, children)) {
            if (parent.isAnonymous()) {
                rebalanceInlineContent(children);
            }
            resolveChildren(c, parent, children, info);
        } else {
            List childrenForAnonymous = new ArrayList();
            List childrenWithAnonymous = new ArrayList();
            for (Iterator i = children.iterator(); i.hasNext();) {
                Styleable child = (Styleable) i.next();
                IdentValue childDisplay = child.getStyle().getIdent(CSSName.DISPLAY);

                if (isProperTableNesting(parentDisplay, childDisplay)) {
                    if (childrenForAnonymous.size() > 0) {
                        createAnonymousTableContent(c, parent, next, childrenForAnonymous,
                                childrenWithAnonymous);

                        childrenForAnonymous = new ArrayList();
                    }
                    childrenWithAnonymous.add(child);
                } else {
                    childrenForAnonymous.add(child);
                }
            }

            if (childrenForAnonymous.size() > 0) {
                createAnonymousTableContent(c, parent, next, childrenForAnonymous,
                        childrenWithAnonymous);
            }

            info.setContainsBlockLevelContent(true);
            resolveChildren(c, parent, childrenWithAnonymous, info);
        }
    }

    private static boolean isParentInline(BlockBox box) {
        CalculatedStyle parentStyle = box.getStyle().getParent();
        return parentStyle != null && parentStyle.isInline();
    }

    private static void createAnonymousTableContent(LayoutContext c, BlockBox source,
            IdentValue next, List childrenForAnonymous, List childrenWithAnonymous) {
        ChildBoxInfo nested = lookForBlockContent(childrenForAnonymous);
        IdentValue anonDisplay;
        if (isParentInline(source) && next == IdentValue.TABLE) {
            anonDisplay = IdentValue.INLINE_TABLE;
        } else {
            anonDisplay = next;
        }
        CalculatedStyle anonStyle = source.getStyle().createAnonymousStyle(anonDisplay);
        BlockBox anonBox = createBlockBox(anonStyle, nested);
        anonBox.setStyle(anonStyle);
        anonBox.setAnonymous(true);
        // XXX Doesn't really make sense, but what to do?
        anonBox.setElement(source.getElement());
        resolveTableContent(c, anonBox, childrenForAnonymous, nested);

        if (next == IdentValue.TABLE) {
            childrenWithAnonymous.add(reorderTableContent((TableBox)anonBox));
        } else {
            childrenWithAnonymous.add(anonBox);
        }
    }
    
    private static BlockBox reorderTableContent(TableBox table) {
        List topCaptions = new LinkedList();
        Box header = null;
        List bodies = new LinkedList();
        Box footer = null;
        List bottomCaptions = new LinkedList();
        
        for (Iterator i = table.getChildIterator(); i.hasNext(); ) {
            Box b = (Box)i.next();
            IdentValue display = b.getStyle().getIdent(CSSName.DISPLAY);
            if (display == IdentValue.TABLE_CAPTION) {
                IdentValue side = b.getStyle().getIdent(CSSName.CAPTION_SIDE);
                if (side == IdentValue.BOTTOM) {
                    bottomCaptions.add(b);
                } else { /* side == IdentValue.TOP */
                    topCaptions.add(b);
                }
            } else if (display == IdentValue.TABLE_HEADER_GROUP && header == null) {
                header = b;
            } else if (display == IdentValue.TABLE_FOOTER_GROUP && footer == null) {
                footer = b;
            } else {
                bodies.add(b);
            }
        }
        
        table.removeAllChildren();
        if (header != null) {
            table.addChild(header);
        }
        table.addAllChildren(bodies);
        if (footer != null) {
            table.addChild(footer);
        }
        
        if (topCaptions.size() == 0 && bottomCaptions.size() == 0) {
            return table;
        } else {
            CalculatedStyle anonStyle = table.getStyle().createAnonymousStyle(IdentValue.BLOCK);
            BlockBox anonBox = new BlockBox();
            anonBox.setStyle(anonStyle);
            anonBox.setAnonymous(true);
            anonBox.setElement(table.getElement());
            
            anonBox.setChildrenContentType(BlockBox.CONTENT_BLOCK);
            anonBox.addAllChildren(topCaptions);
            anonBox.addChild(table);
            anonBox.addAllChildren(bottomCaptions);
            
            return anonBox;
        }
    }

    private static ChildBoxInfo lookForBlockContent(List styleables) {
        ChildBoxInfo result = new ChildBoxInfo();
        for (Iterator i = styleables.iterator(); i.hasNext();) {
            Styleable s = (Styleable) i.next();
            if (!s.getStyle().isLayedOutInInlineContext()) {
                result.setContainsBlockLevelContent(true);
                break;
            }
        }
        return result;
    }

    private static IdentValue getNextTableNestingLevel(IdentValue display) {
        if (display == IdentValue.TABLE || display == IdentValue.INLINE_TABLE) {
            return IdentValue.TABLE_ROW_GROUP;
        } else if (display == IdentValue.TABLE_HEADER_GROUP
                || display == IdentValue.TABLE_ROW_GROUP
                || display == IdentValue.TABLE_FOOTER_GROUP) {
            return IdentValue.TABLE_ROW;
        } else if (display == IdentValue.TABLE_ROW) {
            return IdentValue.TABLE_CELL;
        } else {
            return null;
        }
    }

    private static IdentValue getPreviousTableNestingLevel(IdentValue display) {
        if (display == IdentValue.TABLE_CELL) {
            return IdentValue.TABLE_ROW;
        } else if (display == IdentValue.TABLE_ROW) {
            return IdentValue.TABLE_ROW_GROUP;
        } else if (display == IdentValue.TABLE_HEADER_GROUP
                || display == IdentValue.TABLE_ROW_GROUP
                || display == IdentValue.TABLE_FOOTER_GROUP) {
            return IdentValue.TABLE;
        } else {
            return null;
        }
    }

    private static boolean isProperTableNesting(IdentValue parent, IdentValue child) {
        return (parent == IdentValue.TABLE && (child == IdentValue.TABLE_HEADER_GROUP ||
                                                child == IdentValue.TABLE_ROW_GROUP || 
                                                child == IdentValue.TABLE_FOOTER_GROUP ||
                                                child == IdentValue.TABLE_CAPTION))
                || ((parent == IdentValue.TABLE_HEADER_GROUP ||
                        parent == IdentValue.TABLE_ROW_GROUP || 
                        parent == IdentValue.TABLE_FOOTER_GROUP) && 
                    child == IdentValue.TABLE_ROW)
                || (parent == IdentValue.TABLE_ROW && child == IdentValue.TABLE_CELL)
                || (parent == IdentValue.INLINE_TABLE && (child == IdentValue.TABLE_HEADER_GROUP ||
                                                    child == IdentValue.TABLE_ROW_GROUP || 
                                                    child == IdentValue.TABLE_FOOTER_GROUP));

    }

    private static boolean isNestingTableContent(IdentValue display) {
        return display == IdentValue.TABLE || display == IdentValue.INLINE_TABLE ||
                display == IdentValue.TABLE_HEADER_GROUP || display == IdentValue.TABLE_ROW_GROUP || 
                display == IdentValue.TABLE_FOOTER_GROUP || display == IdentValue.TABLE_ROW;
    }

    // table -> section -> row -> cell

    private static void insertGeneratedContent(LayoutContext c, Element element,
            CalculatedStyle parentStyle, String peName, List children, ChildBoxInfo info) {
        CascadedStyle peStyle = c.getCss().getPseudoElementStyle(element, peName);
        if (peStyle != null && peStyle.hasProperty(CSSName.CONTENT)) {
            String content = ((CSSPrimitiveValue) peStyle.propertyByName(CSSName.CONTENT).getValue()).getStringValue();
            // FIXME Don't think this test is right. Even empty inline content
            // should force a line box to be created. Leave for now though.
            // TODO: need to handle hex values in CSS--\2192 is the Unicode for
            // an arrow (HTML &8594;), though this
            // is a general string problem, anywhere strings can appear in CSS,
            // not just content
            if (!content.equals("")) {
                CalculatedStyle calculatedStyle = parentStyle.deriveStyle(peStyle);
                children.add(createGeneratedContent(c, element, peName, calculatedStyle, content,
                        info));
            }
        }
    }

    private static Styleable createGeneratedContent(LayoutContext c, Element element,
            String peName, CalculatedStyle calculatedStyle, String raw, ChildBoxInfo info) {
        ContentFunction contentFunction = null;
        String content = CONTENT_NEWLINE.matcher(raw).replaceAll("\n");
        if (Idents.looksLikeAFunction(raw)) {
            contentFunction = c.getContentFunctionFactory().lookupFunction(c, raw);
            if (contentFunction != null && contentFunction.isStatic()) {
                content = contentFunction.calculate(c, raw);
                contentFunction = null;
            }
        } else if (Idents.looksLikeAQuote(raw)) {
            // TODO: if the content is one of the quote idents, then look up the
            // value of the quotes property
            content = "\"";
        } else if (Idents.looksLikeASkipQuote(content)) {
            // TODO: no content, but increment nesting level for quotes
        }

        InlineBox iB = new InlineBox(content);
        iB.setContentFunction(contentFunction);
        iB.setElement(element);
        iB.setPseudoElementOrClass(peName);
        iB.setStartsHere(true);
        iB.setEndsHere(true);

        if (calculatedStyle.isInline()) {
            iB.setStyle(calculatedStyle);
            iB.applyTextTransform();
            return iB;
        } else {
            iB.setStyle(calculatedStyle.createAnonymousStyle(IdentValue.INLINE));
            iB.setElement(null);

            // XXX A table / table content display value won't work here.
            // Should (somehow) reset to display: block
            BlockBox result = createBlockBox(calculatedStyle, info);
            result.setStyle(calculatedStyle);
            result.setInlineContent(Collections.singletonList(iB));
            result.setElement(element);
            result.setChildrenContentType(BlockBox.CONTENT_INLINE);
            result.setPseudoElementOrClass(peName);

            return result;
        }
    }

    private static BlockBox createBlockBox(CalculatedStyle style, ChildBoxInfo info) {
        if (style.isFloated() && !(style.isAbsolute() || style.isFixed())) {
            BlockBox result;
            if (style.isTable() || style.isInlineTable()) {
                result = new TableBox();
            } else {
                result = new BlockBox();
            }
            result.setFloatedBoxData(new FloatedBoxData());
            return result;
        } else if (style.isSpecifiedAsBlock()) {
            return new BlockBox();
        } else if (style.isTable() || style.isInlineTable()) {
            return new TableBox();
        } else if (style.isTableCell()) {
            info.setContainsTableContent(true);
            return new TableCellBox();
        } else if (style.isTableRow()) {
            info.setContainsTableContent(true);
            return new TableRowBox();
        } else if (style.isTableSection()) {
            info.setContainsTableContent(true);
            return new TableSectionBox();
        } else if (style.isTableCaption()) {
            info.setContainsTableContent(true);
            return new BlockBox();
        } else {
            return new BlockBox();
        }
    }
    
    private static void addColumns(LayoutContext c, TableBox table, TableColumn parent) {
        SharedContext sharedContext = c.getSharedContext();
        
        Node working = parent.getElement().getFirstChild();
        boolean found = false;
        while (working != null) {
            if (working.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) working;
                CalculatedStyle style = sharedContext.getStyle(element);
                
                if (style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)) {
                    found = true;
                    TableColumn col = new TableColumn(element, style);
                    col.setParent(parent);
                    table.addStyleColumn(col);
                }
            }
        }
        if (! found) {
            table.addStyleColumn(parent);
        }
    }
    
    private static void addColumnOrColumnGroup(
            LayoutContext c, TableBox table, Element e, CalculatedStyle style) {
        if (style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)) {
            table.addStyleColumn(new TableColumn(e, style));
        } else { /* style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN_GROUP) */
            addColumns(c, table, new TableColumn(e, style));
        }
    }

    private static void createChildren(
            LayoutContext c, BlockBox blockParent, Element parent, List children, ChildBoxInfo info) {
        SharedContext sharedContext = c.getSharedContext();

        CalculatedStyle parentStyle = sharedContext.getStyle(parent);
        insertGeneratedContent(c, parent, parentStyle, "before", children, info);

        Node working = parent.getFirstChild();
        if (working != null) {
            InlineBox previousIB = null;
            do {
                Styleable child = null;
                if (working.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) working;
                    CalculatedStyle style = sharedContext.getStyle(element);

                    if (style.isHidden()) {
                        continue;
                    }

                    if (style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)
                            || style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN_GROUP)) {
                        if (blockParent != null && 
                                blockParent.getStyle().isTable() || blockParent.getStyle().isInlineTable()) {
                            TableBox table = (TableBox)blockParent;
                            addColumnOrColumnGroup(c, table, element, style);
                        }
                        
                        continue;
                    }

                    if (style.isInline()) {
                        createChildren(c, null, element, children, info);
                    } else {
                        child = createBlockBox(style, info);
                        child.setStyle(style);
                        child.setElement(element);
                        
                        if (style.isTable() || style.isInlineTable()) {
                            TableBox table = (TableBox)child;
                            table.ensureChildren(c);
                            
                            child = reorderTableContent(table);
                        }

                        if (!info.isContainsBlockLevelContent()
                                && !style.isLayedOutInInlineContext()) {
                            info.setContainsBlockLevelContent(true);
                        }

                        BlockBox block = (BlockBox) child;
                        if (block.getStyle().mayHaveFirstLine()) {
                            block.setFirstLineStyle(c.getCss().getPseudoElementStyle(element,
                                    "first-line"));
                        }
                        if (block.getStyle().mayHaveFirstLetter()) {
                            block.setFirstLetterStyle(c.getCss().getPseudoElementStyle(element,
                                    "first-letter"));
                        }
                    }
                } else if (working.getNodeType() == Node.TEXT_NODE) {
                    Text textNode = (Text) working;
                    StringBuffer text = new StringBuffer(textNode.getData());

                    Node maybeText = textNode;
                    while (true) {
                        maybeText = textNode.getNextSibling();
                        if (maybeText != null && maybeText.getNodeType() == Node.TEXT_NODE) {
                            textNode = (Text) maybeText;
                            text.append(textNode.getData());
                        } else {
                            break;
                        }
                    }

                    working = textNode;

                    child = new InlineBox(text.toString());

                    InlineBox iB = (InlineBox) child;
                    iB.setEndsHere(true);
                    if (previousIB == null) {
                        iB.setStartsHere(true);
                    } else {
                        previousIB.setEndsHere(false);
                    }
                    previousIB = iB;

                    if (parentStyle.isInline()) {
                        iB.setStyle(parentStyle);
                        iB.setElement(parent);
                    } else {
                        iB.setStyle(parentStyle.createAnonymousStyle(IdentValue.INLINE));
                    }
                    
                    iB.applyTextTransform();
                }

                if (child != null) {
                    children.add(child);
                }
            } while ((working = working.getNextSibling()) != null);
        }
        insertGeneratedContent(c, parent, parentStyle, "after", children, info);
    }

    private static void insertAnonymousBlocks(SharedContext c, Box parent, List children) {
        List inline = new ArrayList();

        LinkedList parents = new LinkedList();
        List savedParents = null;

        for (Iterator i = children.iterator(); i.hasNext();) {
            Styleable child = (Styleable) i.next();
            if (child.getStyle().isLayedOutInInlineContext()) {
                inline.add(child);

                if (child.getStyle().isInline()) {
                    InlineBox iB = (InlineBox) child;
                    if (iB.isStartsHere()) {
                        parents.add(iB);
                    }
                    if (iB.isEndsHere()) {
                        parents.removeLast();
                    }
                }
            } else {
                if (inline.size() > 0) {
                    createAnonymousBlock(c, parent, inline, savedParents);
                    inline = new ArrayList();
                    savedParents = new ArrayList(parents);
                }
                parent.addChild((Box) child);
            }
        }

        createAnonymousBlock(c, parent, inline, savedParents);
    }

    private static void createAnonymousBlock(SharedContext c, Box parent, List inline,
            List savedParents) {
        WhitespaceStripper.stripInlineContent(inline);
        if (inline.size() > 0) {
            AnonymousBlockBox anon = new AnonymousBlockBox(parent.getElement());
            anon.setStyle(c.getStyle(parent.getElement()).createAnonymousStyle(IdentValue.BLOCK));
            anon.setAnonymous(true);
            if (savedParents != null && savedParents.size() > 0) {
                anon.setOpenInlineBoxes(savedParents);
            }
            parent.addChild(anon);
            anon.setChildrenContentType(BlockBox.CONTENT_INLINE);
            anon.setInlineContent(inline);
        }
    }

    private static class ChildBoxInfo {
        private boolean _containsBlockLevelContent;
        private boolean _containsTableContent;
        public ChildBoxInfo() {
        }

        public boolean isContainsBlockLevelContent() {
            return _containsBlockLevelContent;
        }

        public void setContainsBlockLevelContent(boolean containsBlockLevelContent) {
            _containsBlockLevelContent = containsBlockLevelContent;
        }

        public boolean isContainsTableContent() {
            return _containsTableContent;
        }

        public void setContainsTableContent(boolean containsTableContent) {
            _containsTableContent = containsTableContent;
        }
    }
}
