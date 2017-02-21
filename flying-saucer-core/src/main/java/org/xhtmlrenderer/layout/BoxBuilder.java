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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.EntityReference;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.constants.MarginBoxName;
import org.xhtmlrenderer.css.constants.PageElementPosition;
import org.xhtmlrenderer.css.extend.ContentFunction;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.newmatch.PageInfo;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.css.style.FSDerivedValue;
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

/**
 * This class is responsible for creating the box tree from the DOM.  This is
 * mostly just a one-to-one translation from the <code>Element</code> to an
 * <code>InlineBox</code> or a <code>BlockBox</code> (or some subclass of
 * <code>BlockBox</code>), but the tree is reorganized according to the CSS rules.
 * This includes inserting anonymous block and inline boxes, anonymous table
 * content, and <code>:before</code> and <code>:after</code> content.  White
 * space is also normalized at this point.  Table columns and table column groups
 * are added to the table which owns them, but are not created as regular boxes.
 * Floated and absolutely positioned content is always treated as inline
 * content for purposes of inserting anonymous block boxes and calculating
 * the kind of content contained in a given block box.
 */
public class BoxBuilder {
    public static final int MARGIN_BOX_VERTICAL = 1;
    public static final int MARGIN_BOX_HORIZONTAL = 2;

    private static final int CONTENT_LIST_DOCUMENT = 1;
    private static final int CONTENT_LIST_MARGIN_BOX = 2;

    public static BlockBox createRootBox(LayoutContext c, Document document) {
        Element root = document.getDocumentElement();

        CalculatedStyle style = c.getSharedContext().getStyle(root);

        BlockBox result;
        if (style.isTable() || style.isInlineTable()) {
            result = new TableBox();
        } else {
            result = new BlockBox();
        }

        result.setStyle(style);
        result.setElement(root);

        c.resolveCounters(style);

        c.pushLayer(result);
        if (c.isPrint()) {
            if (! style.isIdent(CSSName.PAGE, IdentValue.AUTO)) {
                c.setPageName(style.getStringProperty(CSSName.PAGE));
            }
            c.getRootLayer().addPage(c);
        }

        return result;
    }

    public static void createChildren(LayoutContext c, BlockBox parent) {

		List children = new ArrayList();

        ChildBoxInfo info = new ChildBoxInfo();

        createChildren(c, parent, parent.getElement(), children, info, false);

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

    public static TableBox createMarginTable(
            LayoutContext c,
            PageInfo pageInfo,
            MarginBoxName[] names,
            int height,
            int direction)
    {
        if (! pageInfo.hasAny(names)) {
            return null;
        }

        Element source = c.getRootLayer().getMaster().getElement(); // HACK

        ChildBoxInfo info = new ChildBoxInfo();
        CalculatedStyle pageStyle = new EmptyStyle().deriveStyle(pageInfo.getPageStyle());

        CalculatedStyle tableStyle = pageStyle.deriveStyle(
                CascadedStyle.createLayoutStyle(new PropertyDeclaration[] {
                        new PropertyDeclaration(
                                CSSName.DISPLAY,
                                new PropertyValue(IdentValue.TABLE),
                                true,
                                StylesheetInfo.USER),
                        new PropertyDeclaration(
                                CSSName.WIDTH,
                                new PropertyValue(CSSPrimitiveValue.CSS_PERCENTAGE, 100.0f, "100%"),
                                true,
                                StylesheetInfo.USER),
                }));
        TableBox result = (TableBox)createBlockBox(tableStyle, info, false);
        result.setMarginAreaRoot(true);
        result.setStyle(tableStyle);
        result.setElement(source);
        result.setAnonymous(true);
        result.setChildrenContentType(BlockBox.CONTENT_BLOCK);

        CalculatedStyle tableSectionStyle = pageStyle.createAnonymousStyle(IdentValue.TABLE_ROW_GROUP);
        TableSectionBox section = (TableSectionBox)createBlockBox(tableSectionStyle, info, false);
        section.setStyle(tableSectionStyle);
        section.setElement(source);
        section.setAnonymous(true);
        section.setChildrenContentType(BlockBox.CONTENT_BLOCK);

        result.addChild(section);

        TableRowBox row = null;
        if (direction == MARGIN_BOX_HORIZONTAL) {
            CalculatedStyle tableRowStyle = pageStyle.createAnonymousStyle(IdentValue.TABLE_ROW);
            row = (TableRowBox)createBlockBox(tableRowStyle, info, false);
            row.setStyle(tableRowStyle);
            row.setElement(source);
            row.setAnonymous(true);
            row.setChildrenContentType(BlockBox.CONTENT_BLOCK);

            row.setHeightOverride(height);

            section.addChild(row);
        }

        int cellCount = 0;
        boolean alwaysCreate = names.length > 1 && direction == MARGIN_BOX_HORIZONTAL;

        for (int i = 0; i < names.length; i++) {
            CascadedStyle cellStyle = pageInfo.createMarginBoxStyle(names[i], alwaysCreate);
            if (cellStyle != null) {
                TableCellBox cell = createMarginBox(c, cellStyle, alwaysCreate);
                if (cell != null) {
                    if (direction == MARGIN_BOX_VERTICAL) {
                        CalculatedStyle tableRowStyle = pageStyle.createAnonymousStyle(IdentValue.TABLE_ROW);
                        row = (TableRowBox)createBlockBox(tableRowStyle, info, false);
                        row.setStyle(tableRowStyle);
                        row.setElement(source);
                        row.setAnonymous(true);
                        row.setChildrenContentType(BlockBox.CONTENT_BLOCK);

                        row.setHeightOverride(height);

                        section.addChild(row);
                    }
                    row.addChild(cell);
                    cellCount++;
                }
            }
        }

        if (direction == MARGIN_BOX_VERTICAL && cellCount > 0) {
            int rHeight = 0;
            for (Iterator i = section.getChildIterator(); i.hasNext(); ) {
                TableRowBox r = (TableRowBox)i.next();
                r.setHeightOverride(height / cellCount);
                rHeight += r.getHeightOverride();
            }

            for (Iterator i = section.getChildIterator(); i.hasNext() && rHeight < height; ) {
                TableRowBox r = (TableRowBox)i.next();
                r.setHeightOverride(r.getHeightOverride()+1);
                rHeight++;
            }
        }

        return cellCount > 0 ? result : null;
    }

    private static TableCellBox createMarginBox(
            LayoutContext c,
            CascadedStyle cascadedStyle,
            boolean alwaysCreate) {
        boolean hasContent = true;

        PropertyDeclaration contentDecl = cascadedStyle.propertyByName(CSSName.CONTENT);

        CalculatedStyle style = new EmptyStyle().deriveStyle(cascadedStyle);

        if (style.isDisplayNone() && ! alwaysCreate) {
            return null;
        }

        if (style.isIdent(CSSName.CONTENT, IdentValue.NONE) ||
                style.isIdent(CSSName.CONTENT, IdentValue.NORMAL)) {
            hasContent = false;
        }

        if (style.isAutoWidth() && ! alwaysCreate && ! hasContent) {
            return null;
        }

        List children = new ArrayList();

        ChildBoxInfo info = new ChildBoxInfo();
        info.setContainsTableContent(true);
        info.setLayoutRunningBlocks(true);

        TableCellBox result = new TableCellBox();
        result.setAnonymous(true);
        result.setStyle(style);
        result.setElement(c.getRootLayer().getMaster().getElement()); // XXX Doesn't make sense, but we need something here

        if (hasContent && ! style.isDisplayNone()) {
            children.addAll(createGeneratedMarginBoxContent(
                    c,
                    c.getRootLayer().getMaster().getElement(),
                    (PropertyValue)contentDecl.getValue(),
                    style,
                    info));

            stripAllWhitespace(children);
        }

        if (children.size() == 0 && style.isAutoWidth() && ! alwaysCreate) {
            return null;
        }

        resolveChildTableContent(c, result, children, info, IdentValue.TABLE_CELL);

        return result;
    }

    private static void resolveChildren(
            LayoutContext c, BlockBox owner, List children, ChildBoxInfo info) {
        if (children.size() > 0) {
            if (info.isContainsBlockLevelContent()) {
                insertAnonymousBlocks(
                        c.getSharedContext(), owner, children, info.isLayoutRunningBlocks());
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

    /**
     * Handles the situation when we find table content, but our parent is not
     * table related.  For example, <code>div</code> -> <code>td</td></code>.
     * Anonymous tables are then constructed by repeatedly pulling together
     * consecutive same-table-level siblings and wrapping them in the next
     * highest table level (e.g. consecutive <code>td</code> elements will
     * be wrapped in an anonymous <code>tr</code>, then a <code>tbody</code>, and
     * finally a <code>table</code>).
     */
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

    /**
     * Handles the situation when our current parent is table related.  If
     * everything is properly nested (e.g. a <code>tr</code> contains only
     * <code>td</code> elements), nothing is done.  Otherwise anonymous boxes
     * are inserted to ensure the integrity of the table model.
     */
    private static void resolveTableContent(
            LayoutContext c, BlockBox parent, List children, ChildBoxInfo info) {
        IdentValue parentDisplay = parent.getStyle().getIdent(CSSName.DISPLAY);
        IdentValue next = getNextTableNestingLevel(parentDisplay);
        if (next == null && parent.isAnonymous() && containsOrphanedTableContent(children)) {
            resolveChildTableContent(c, parent, children, info, IdentValue.TABLE_CELL);
        } else if (next == null || isAllProperTableNesting(parentDisplay, children)) {
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

    private static boolean containsOrphanedTableContent(List children) {
        for (Iterator i = children.iterator(); i.hasNext();) {
            Styleable child = (Styleable) i.next();
            IdentValue display = child.getStyle().getIdent(CSSName.DISPLAY);
            if (display == IdentValue.TABLE_HEADER_GROUP ||
                    display == IdentValue.TABLE_ROW_GROUP ||
                    display == IdentValue.TABLE_FOOTER_GROUP ||
                    display == IdentValue.TABLE_ROW) {
                return true;
            }
        }

        return false;
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
        BlockBox anonBox = createBlockBox(anonStyle, nested, false);
        anonBox.setStyle(anonStyle);
        anonBox.setAnonymous(true);
        // XXX Doesn't really make sense, but what to do?
        anonBox.setElement(source.getElement());
        resolveTableContent(c, anonBox, childrenForAnonymous, nested);

        if (next == IdentValue.TABLE) {
            childrenWithAnonymous.add(reorderTableContent(c, (TableBox) anonBox));
        } else {
            childrenWithAnonymous.add(anonBox);
        }
    }

    /**
     * Reorganizes a table so that the header is the first row group and the
     * footer the last.  If the table has caption boxes, they will be pulled
     * out and added to an anonymous block box along with the table itself.
     * If not, the table is returned.
     */
    private static BlockBox reorderTableContent(LayoutContext c, TableBox table) {
        List topCaptions = new LinkedList();
        Box header = null;
        List bodies = new LinkedList();
        Box footer = null;
        List bottomCaptions = new LinkedList();

        for (Iterator i = table.getChildIterator(); i.hasNext();) {
            Box b = (Box) i.next();
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
            ((TableSectionBox)header).setHeader(true);
            table.addChild(header);
        }
        table.addAllChildren(bodies);
        if (footer != null) {
            ((TableSectionBox)footer).setFooter(true);
            table.addChild(footer);
        }

        if (topCaptions.size() == 0 && bottomCaptions.size() == 0) {
            return table;
        } else {
            // If we have a floated table with a caption, we need to float the
            // outer anonymous box and not the table
            CalculatedStyle anonStyle;
            if (table.getStyle().isFloated()) {
                CascadedStyle cascadedStyle = CascadedStyle.createLayoutStyle(
                        new PropertyDeclaration[]{
                                CascadedStyle.createLayoutPropertyDeclaration(
                                        CSSName.DISPLAY, IdentValue.BLOCK),
                                CascadedStyle.createLayoutPropertyDeclaration(
                                        CSSName.FLOAT, table.getStyle().getIdent(CSSName.FLOAT))});

                anonStyle = table.getStyle().deriveStyle(cascadedStyle);
            } else {
                anonStyle = table.getStyle().createAnonymousStyle(IdentValue.BLOCK);
            }

            BlockBox anonBox = new BlockBox();
            anonBox.setStyle(anonStyle);
            anonBox.setAnonymous(true);
            anonBox.setFromCaptionedTable(true);
            anonBox.setElement(table.getElement());

            anonBox.setChildrenContentType(BlockBox.CONTENT_BLOCK);
            anonBox.addAllChildren(topCaptions);
            anonBox.addChild(table);
            anonBox.addAllChildren(bottomCaptions);

            if (table.getStyle().isFloated()) {
                anonBox.setFloatedBoxData(new FloatedBoxData());
                table.setFloatedBoxData(null);

                CascadedStyle original = c.getSharedContext().getCss().getCascadedStyle(
                        table.getElement(), false);
                CascadedStyle modified = CascadedStyle.createLayoutStyle(
                        original,
                        new PropertyDeclaration[]{
                                CascadedStyle.createLayoutPropertyDeclaration(
                                        CSSName.FLOAT, IdentValue.NONE)
                        });
                table.setStyle(table.getStyle().getParent().deriveStyle(modified));
            }

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

    private static boolean isAttrFunction(FSFunction function) {
        if (function.getName().equals("attr")) {
            List params = function.getParameters();
            if (params.size() == 1) {
                PropertyValue value = (PropertyValue) params.get(0);
                return value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT;
            }
        }

        return false;
    }

    public static boolean isElementFunction(FSFunction function) {
        if (function.getName().equals("element")) {
            List params = function.getParameters();
            if (params.size() < 1 || params.size() > 2) {
                return false;
            }
            boolean ok = true;
            PropertyValue value1 = (PropertyValue) params.get(0);
            ok = value1.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT;
            if (ok && params.size() == 2) {
                PropertyValue value2 = (PropertyValue) params.get(1);
                ok = value2.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT;
            }

            return ok;
        }

        return false;
    }

    private static CounterFunction makeCounterFunction(FSFunction function, LayoutContext c, CalculatedStyle style) {
        if (function.getName().equals("counter")) {
            List params = function.getParameters();
            if (params.size() < 1 || params.size() > 2) {
                return null;
            }

            PropertyValue value = (PropertyValue) params.get(0);
            if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
                return null;
            }

            String s = value.getStringValue();
            // counter(page) and counter(pages) are handled separately
            if (s.equals("page") || s.equals("pages")) {
                return null;
            }

            String counter = value.getStringValue();
            IdentValue listStyleType = IdentValue.DECIMAL;
            if (params.size() == 2) {
                value = (PropertyValue) params.get(1);
                if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
                    return null;
                }

                IdentValue identValue = IdentValue.valueOf(value.getStringValue());
                if (identValue != null) {
                    value.setIdentValue(identValue);
                    listStyleType = identValue;
                }
            }

            int counterValue = c.getCounterContext(style).getCurrentCounterValue(counter);

            return new CounterFunction(counterValue, listStyleType);
        } else if (function.getName().equals("counters")) {
            List params = function.getParameters();
            if (params.size() < 2 || params.size() > 3) {
                return null;
            }

            PropertyValue value = (PropertyValue) params.get(0);
            if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
                return null;
            }

            String counter = value.getStringValue();

            value = (PropertyValue) params.get(1);
            if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_STRING) {
                return null;
            }

            String separator = value.getStringValue();

            IdentValue listStyleType = IdentValue.DECIMAL;
            if (params.size() == 3) {
                value = (PropertyValue) params.get(2);
                if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
                    return null;
                }

                IdentValue identValue = IdentValue.valueOf(value.getStringValue());
                if (identValue != null) {
                    value.setIdentValue(identValue);
                    listStyleType = identValue;
                }
            }

            List counterValues = c.getCounterContext(style).getCurrentCounterValues(counter);

            return new CounterFunction(counterValues, separator, listStyleType);
        } else {
            return null;
        }
    }

    private static String getAttributeValue(FSFunction attrFunc, Element e) {
        PropertyValue value = (PropertyValue) attrFunc.getParameters().get(0);
        return e.getAttribute(value.getStringValue());
    }

    private static List createGeneratedContentList(
            LayoutContext c, Element element, PropertyValue propValue,
            String peName, CalculatedStyle style, int mode, ChildBoxInfo info) {
        List values = propValue.getValues();

        if (values == null) {
            // content: normal or content: none
            return Collections.EMPTY_LIST;
        }

        List result = new ArrayList(values.size());

        for (Iterator i = values.iterator(); i.hasNext();) {
            PropertyValue value = (PropertyValue) i.next();

            ContentFunction contentFunction = null;
            FSFunction function = null;

            String content = null;

            short type = value.getPrimitiveType();
            if (type == CSSPrimitiveValue.CSS_STRING) {
                content = value.getStringValue();
            } else if (value.getPropertyValueType() == PropertyValue.VALUE_TYPE_FUNCTION) {
                if (mode == CONTENT_LIST_DOCUMENT && isAttrFunction(value.getFunction())) {
                    content = getAttributeValue(value.getFunction(), element);
                } else {
                    CounterFunction cFunc = null;

                    if (mode == CONTENT_LIST_DOCUMENT) {
                        cFunc = makeCounterFunction(value.getFunction(), c, style);
                    }

                    if (cFunc != null) {
                        //TODO: counter functions may be called with non-ordered list-style-types, e.g. disc
                        content = cFunc.evaluate();
                        contentFunction = null;
                        function = null;
                    } else if (mode == CONTENT_LIST_MARGIN_BOX && isElementFunction(value.getFunction())) {
                        BlockBox target = getRunningBlock(c, value);
                        if (target != null) {
                            result.add(target.copyOf());
                            info.setContainsBlockLevelContent(true);
                        }
                    } else {
                        contentFunction =
                                c.getContentFunctionFactory().lookupFunction(c, value.getFunction());
                        if (contentFunction != null) {
                            function = value.getFunction();

                            if (contentFunction.isStatic()) {
                                content = contentFunction.calculate(c, function);
                                contentFunction = null;
                                function = null;
                            } else {
                                content = contentFunction.getLayoutReplacementText();
                            }
                        }
                    }
                }
            } else if (type == CSSPrimitiveValue.CSS_IDENT) {
                FSDerivedValue dv = style.valueByName(CSSName.QUOTES);

                if (dv != IdentValue.NONE) {
                    IdentValue ident = value.getIdentValue();

                    if (ident == IdentValue.OPEN_QUOTE) {
                        String[] quotes = style.asStringArray(CSSName.QUOTES);
                        content = quotes[0];
                    } else if (ident == IdentValue.CLOSE_QUOTE) {
                        String[] quotes = style.asStringArray(CSSName.QUOTES);
                        content = quotes[1];
                    }
                }
            }

            if (content != null) {
                InlineBox iB = new InlineBox(content, null);
                iB.setContentFunction(contentFunction);
                iB.setFunction(function);
                iB.setElement(element);
                iB.setPseudoElementOrClass(peName);
                iB.setStartsHere(true);
                iB.setEndsHere(true);

                result.add(iB);
            }
        }

        return result;
    }

    public static BlockBox getRunningBlock(LayoutContext c, PropertyValue value) {
        List params = value.getFunction().getParameters();
        String ident = ((PropertyValue)params.get(0)).getStringValue();
        PageElementPosition position = null;
        if (params.size() == 2) {
            position = PageElementPosition.valueOf(
                    ((PropertyValue)params.get(1)).getStringValue());
        }
        if (position == null) {
            position = PageElementPosition.FIRST;
        }
        BlockBox target = c.getRootDocumentLayer().getRunningBlock(ident, c.getPage(), position);
        return target;
    }

    private static void insertGeneratedContent(
            LayoutContext c, Element element, CalculatedStyle parentStyle,
            String peName, List children, ChildBoxInfo info) {
        CascadedStyle peStyle = c.getCss().getPseudoElementStyle(element, peName);
        if (peStyle != null) {
            PropertyDeclaration contentDecl = peStyle.propertyByName(CSSName.CONTENT);
            PropertyDeclaration counterResetDecl = peStyle.propertyByName(CSSName.COUNTER_RESET);
            PropertyDeclaration counterIncrDecl = peStyle.propertyByName(CSSName.COUNTER_INCREMENT);

            CalculatedStyle calculatedStyle = null;
            if (contentDecl != null || counterResetDecl != null || counterIncrDecl != null) {
                calculatedStyle = parentStyle.deriveStyle(peStyle);
                if (calculatedStyle.isDisplayNone()) return;
                if (calculatedStyle.isIdent(CSSName.CONTENT, IdentValue.NONE)) return;
                if (calculatedStyle.isIdent(CSSName.CONTENT, IdentValue.NORMAL) && (peName.equals("before") || peName.equals("after")))
                    return;

                if (calculatedStyle.isTable() || calculatedStyle.isTableRow() || calculatedStyle.isTableSection()) {
                    CascadedStyle newPeStyle =
                        CascadedStyle.createLayoutStyle(peStyle, new PropertyDeclaration[] {
                            CascadedStyle.createLayoutPropertyDeclaration(
                                CSSName.DISPLAY,
                                IdentValue.BLOCK),
                        });
                    calculatedStyle = parentStyle.deriveStyle(newPeStyle);
                }
                c.resolveCounters(calculatedStyle);
            }

            if (contentDecl != null) {
                CSSPrimitiveValue propValue = contentDecl.getValue();
                children.addAll(createGeneratedContent(c, element, peName, calculatedStyle,
                        (PropertyValue) propValue, info));
            }
        }
    }

    private static List createGeneratedContent(
            LayoutContext c, Element element, String peName,
            CalculatedStyle style, PropertyValue property, ChildBoxInfo info) {
        if (style.isDisplayNone() || style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)
                || style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN_GROUP)) {
            return Collections.EMPTY_LIST;
        }

        List inlineBoxes = createGeneratedContentList(
                c, element, property, peName, style, CONTENT_LIST_DOCUMENT, null);

        if (style.isInline()) {
            for (Iterator i = inlineBoxes.iterator(); i.hasNext();) {
                InlineBox iB = (InlineBox) i.next();
                iB.setStyle(style);
                iB.applyTextTransform();
            }
            return inlineBoxes;
        } else {
            CalculatedStyle anon = style.createAnonymousStyle(IdentValue.INLINE);
            for (Iterator i = inlineBoxes.iterator(); i.hasNext();) {
                InlineBox iB = (InlineBox) i.next();
                iB.setStyle(anon);
                iB.applyTextTransform();
                iB.setElement(null);
            }

            BlockBox result = createBlockBox(style, info, true);
            result.setStyle(style);
            result.setInlineContent(inlineBoxes);
            result.setElement(element);
            result.setChildrenContentType(BlockBox.CONTENT_INLINE);
            result.setPseudoElementOrClass(peName);

            if (! style.isLayedOutInInlineContext()) {
                info.setContainsBlockLevelContent(true);
            }

            return new ArrayList(Collections.singletonList(result));
        }
    }

    private static List createGeneratedMarginBoxContent(
            LayoutContext c, Element element, PropertyValue property,
            CalculatedStyle style, ChildBoxInfo info) {
        List result = createGeneratedContentList(
                c, element, property, null, style, CONTENT_LIST_MARGIN_BOX, info);

        CalculatedStyle anon = style.createAnonymousStyle(IdentValue.INLINE);
        for (Iterator i = result.iterator(); i.hasNext();) {
            Styleable s = (Styleable) i.next();
            if (s instanceof InlineBox) {
                InlineBox iB = (InlineBox)s;
                iB.setElement(null);
                iB.setStyle(anon);
                iB.applyTextTransform();
            }
        }

        return result;
    }

    private static BlockBox createBlockBox(
            CalculatedStyle style, ChildBoxInfo info, boolean generated) {
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
        } else if (! generated && (style.isTable() || style.isInlineTable())) {
            return new TableBox();
        } else if (style.isTableCell()) {
            info.setContainsTableContent(true);
            return new TableCellBox();
        } else if (! generated && style.isTableRow()) {
            info.setContainsTableContent(true);
            return new TableRowBox();
        } else if (! generated && style.isTableSection()) {
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
            working = working.getNextSibling();
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

    private static InlineBox createInlineBox(
            String text, Element parent, CalculatedStyle parentStyle, Text node) {
        InlineBox result = new InlineBox(text, node);

        if (parentStyle.isInline() && ! (parent.getParentNode() instanceof Document)) {
            result.setStyle(parentStyle);
            result.setElement(parent);
        } else {
            result.setStyle(parentStyle.createAnonymousStyle(IdentValue.INLINE));
        }

        result.applyTextTransform();

        return result;
    }

    private static void createChildren(
            LayoutContext c, BlockBox blockParent, Element parent,
            List children, ChildBoxInfo info, boolean inline) {
        SharedContext sharedContext = c.getSharedContext();

        CalculatedStyle parentStyle = sharedContext.getStyle(parent);

        insertGeneratedContent(c, parent, parentStyle, "before", children, info);

        Node working = parent.getFirstChild();
        boolean needStartText = inline;
        boolean needEndText = inline;
        if (working != null) {
            InlineBox previousIB = null;
            do {
                Styleable child = null;
                short nodeType = working.getNodeType();
                if (nodeType == Node.ELEMENT_NODE) {
                    Element element = (Element) working;
                    CalculatedStyle style = sharedContext.getStyle(element);

                    if (style.isDisplayNone()) {
                        continue;
                    }

                    Integer start = null;
					if ("ol".equals(working.getNodeName())) {
						Node startAttribute = working.getAttributes().getNamedItem("start");
						if (startAttribute != null) {
							try {
								start = new Integer(Integer.parseInt(startAttribute.getNodeValue()) - 1);
							} catch (NumberFormatException e) {
								// ignore
							}
						}
					} else if ("li".equals(working.getNodeName())) {
						Node valueAttribute = working.getAttributes().getNamedItem("value");
						if (valueAttribute != null) {
							try {
								start = new Integer(Integer.parseInt(valueAttribute.getNodeValue()) - 1);
							} catch (NumberFormatException e) {
								// ignore
							}
						}
					}

	                c.resolveCounters(style, start);

                    if (style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)
                            || style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN_GROUP)) {
                        if ((blockParent != null) &&
                                (blockParent.getStyle().isTable() || blockParent.getStyle().isInlineTable())) {
                            TableBox table = (TableBox) blockParent;
                            addColumnOrColumnGroup(c, table, element, style);
                        }

                        continue;
                    }

                    if (style.isInline()) {
                        if (needStartText) {
                            needStartText = false;
                            InlineBox iB = createInlineBox("", parent, parentStyle, null);
                            iB.setStartsHere(true);
                            iB.setEndsHere(false);
                            children.add(iB);
                            previousIB = iB;
                        }
                        createChildren(c, null, element, children, info, true);
                        if (inline) {
                            if (previousIB != null) {
                                previousIB.setEndsHere(false);
                            }
                            needEndText = true;
                        }
                    } else {
                        child = createBlockBox(style, info, false);
                        child.setStyle(style);
                        child.setElement(element);
                        if (style.isListItem()) {
                            BlockBox block = (BlockBox) child;
                            block.setListCounter(c.getCounterContext(style).getCurrentCounterValue("list-item"));
                        }

                        if (style.isTable() || style.isInlineTable()) {
                            TableBox table = (TableBox) child;
                            table.ensureChildren(c);

                            child = reorderTableContent(c, table);
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
                        //I think we need to do this to evaluate counters correctly
                        block.ensureChildren(c);
                    }
                } else if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
                    needStartText = false;
                    needEndText = false;

                    Text textNode = (Text)working;

                    /*
                    StringBuffer text = new StringBuffer(textNode.getData());

                    Node maybeText = textNode;
                    while (true) {
                        maybeText = textNode.getNextSibling();
                        if (maybeText != null) {
                            short maybeNodeType = maybeText.getNodeType();
                            if (maybeNodeType == Node.TEXT_NODE ||
                                    maybeNodeType == Node.CDATA_SECTION_NODE) {
                                textNode = (Text)maybeText;
                                text.append(textNode.getData());
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }

                    working = textNode;
                    child = createInlineBox(text.toString(), parent, parentStyle, textNode);
                    */

                    child = createInlineBox(textNode.getData(), parent, parentStyle, textNode);

                    InlineBox iB = (InlineBox) child;
                    iB.setEndsHere(true);
                    if (previousIB == null) {
                        iB.setStartsHere(true);
                    } else {
                        previousIB.setEndsHere(false);
                    }
                    previousIB = iB;
                } else if(nodeType == Node.ENTITY_REFERENCE_NODE) {
                    EntityReference entityReference = (EntityReference)working;
                    child = createInlineBox(entityReference.getTextContent(), parent, parentStyle, null);

                    InlineBox iB = (InlineBox) child;
                    iB.setEndsHere(true);
                    if (previousIB == null) {
                        iB.setStartsHere(true);
                    } else {
                        previousIB.setEndsHere(false);
                    }
                    previousIB = iB;
                }

                if (child != null) {
                    children.add(child);
                }
            } while ((working = working.getNextSibling()) != null);
        }
        if (needStartText || needEndText) {
            InlineBox iB = createInlineBox("", parent, parentStyle, null);
            iB.setStartsHere(needStartText);
            iB.setEndsHere(needEndText);
            children.add(iB);
        }
        insertGeneratedContent(c, parent, parentStyle, "after", children, info);
    }

    private static void insertAnonymousBlocks(
            SharedContext c, Box parent, List children, boolean layoutRunningBlocks) {
        List inline = new ArrayList();

        LinkedList parents = new LinkedList();
        List savedParents = null;

        for (Iterator i = children.iterator(); i.hasNext();) {
            Styleable child = (Styleable) i.next();
            if (child.getStyle().isLayedOutInInlineContext() &&
                    ! (layoutRunningBlocks && child.getStyle().isRunning())) {
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
            anon.setStyle(parent.getStyle().createAnonymousStyle(IdentValue.BLOCK));
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
        private boolean _layoutRunningBlocks;

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

        public boolean isLayoutRunningBlocks() {
            return _layoutRunningBlocks;
        }

        public void setLayoutRunningBlocks(boolean layoutRunningBlocks) {
            _layoutRunningBlocks = layoutRunningBlocks;
        }
    }
}
