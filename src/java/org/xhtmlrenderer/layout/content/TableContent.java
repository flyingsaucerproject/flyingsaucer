/*
 * TableContent.java
 * Copyright (c) 2004, 2005 Torbj�rn Gannholm
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
package org.xhtmlrenderer.layout.content;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.LayoutContext;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Represents the content of a "block"-display element
 *
 * @author Torbj�rn Gannholm
 */
public class TableContent extends AbstractCollapsableContent implements CollapsableContent {
    /**
     * Description of the Field
     */
    final private Element _elem;
    /**
     * Description of the Field
     */
    final private CascadedStyle _style;
    /**
     * Description of the Field
     */
    final private LinkedList _children;

    private boolean _topMarginCollapsed;
    private boolean _bottomMarginCollapsed;

    /**
     * Constructor for the TableContent object
     *
     * @param e     PARAM
     * @param style PARAM
     */
    public TableContent(Element e, CascadedStyle style) {
        _elem = e;
        _style = style;
        _children = null;
    }

    /**
     * anonymous table
     */
    TableContent() {
        _elem = null;
        _style = null;
        _children = new LinkedList();
    }

    /**
     * for anonymous tables, child content is added as it is parsed by ContentUtil
     */
    void addChild(Element e) {
        _children.addLast(e);
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        return "Table: " + _elem.getNodeName();
    }

    /**
     * Gets the element attribute of the TableContent object
     *
     * @return The element value
     */
    public Element getElement() {
        return _elem;
    }

    /**
     * Gets the style attribute of the TableContent object
     *
     * @return The style value
     */
    public CascadedStyle getStyle() {
        return _style;
    }

    /**
     * Gets the childContent attribute of the TableContent object
     *
     * @param c PARAM
     * @return The childContent value
     */
    protected List makeChildContent(LayoutContext c) {
        LinkedList contentList = new LinkedList();
        FirstLineStyle firstLineStyle = null;
        FirstLetterStyle firstLetterStyle = null;

        if (_elem != null) {
            c.pushStyle(_style);
            if (ContentUtil.mayHaveFirstLine(c.getCurrentStyle())) {
                //put in a marker if there is first-line styling
                CascadedStyle firstLine = c.getCss().getPseudoElementStyle(_elem, "first-line");
                if (firstLine != null) {
                    firstLineStyle = new FirstLineStyle(firstLine);
                }
            }

            if (ContentUtil.mayHaveFirstLetter(c.getCurrentStyle())) {
                //put in a marker if there is first-letter styling
                CascadedStyle firstLetter = c.getCss().getPseudoElementStyle(_elem, "first-letter");
                if (firstLetter != null) {
                    firstLetterStyle = new FirstLetterStyle(firstLetter);
                }
            }
            c.popStyle();

            /* Ignore before content of tables for now
            //TODO: before and after may be block!
            //<br/> handling should be done by :before content
            CascadedStyle before = c.getCss().getPseudoElementStyle(parentElement, "before");
            if (before != null && before.hasProperty(CSSName.CONTENT)) {
                String content = ((CSSPrimitiveValue) before.propertyByName(CSSName.CONTENT).getValue()).getStringValue();
                if (!content.equals("")) {
                    contentList.add(new StylePush("before", parentElement));
                    c.pushStyle(before);
                    textContent = new StringBuffer();
                    textContent.append(content.replaceAll("\\\\A", "\n"));
                    contentList.add(new TextContent("before", parentElement, textContent.toString()));
                    textContent = null;
                    c.popStyle();
                    contentList.add(new StylePop("before", parentElement));
                }
                //do not reset style here, because if this element is empty, we will not have changed context
            }
            */
        }

        Iterator childIterator = getChildIterator();
        TableRowContent anonymousRow = null;
        //each child node can result in only one addition to content
        while (childIterator.hasNext()) {
            Node curr = (Node) childIterator.next();
            if (curr.getNodeType() != Node.ELEMENT_NODE && curr.getNodeType() != Node.TEXT_NODE) {
                continue;
            }//must be a comment or pi or something

            if (curr.getNodeType() == Node.TEXT_NODE) {
                String text = curr.getNodeValue();
                text = WhitespaceStripper.collapseWhitespace(IdentValue.NORMAL, text, true);
                if (!text.equals("")) {
                    if (anonymousRow == null) {
                        anonymousRow = new TableRowContent();
                        contentList.add(anonymousRow);
                    }
                    anonymousRow.addChild(curr);
                }
                continue;
            }

            Element elem = (Element) curr;
            CascadedStyle style = c.getCss().getCascadedStyle(elem, true);//this is the place where restyle is done for layout (boxing)
            c.pushStyle(style);//just remember to pop it before continue
            IdentValue display = c.getCurrentStyle().getIdent(CSSName.DISPLAY);

            //If this element can't be directly uder a table, stick it in an anonymous row
            if (display != IdentValue.TABLE_ROW
                    && display != IdentValue.TABLE_CAPTION
                    && display != IdentValue.TABLE_COLUMN
                    && display != IdentValue.TABLE_COLUMN_GROUP
                    && display != IdentValue.TABLE_FOOTER_GROUP
                    && display != IdentValue.TABLE_HEADER_GROUP
                    && display != IdentValue.TABLE_ROW_GROUP) {
                if (anonymousRow == null) {
                    anonymousRow = new TableRowContent();
                    contentList.add(anonymousRow);
                }
                anonymousRow.addChild(elem);
                c.popStyle();
                continue;
            }
            //if we reach here, our anonymous row is done
            anonymousRow = null;

            if (display == IdentValue.TABLE_ROW) {
                contentList.add(new TableRowContent(elem, style));
                c.popStyle();
                continue;
            }

            if (display == IdentValue.TABLE_CAPTION) {
                contentList.add(new TableCaptionContent(elem, style));
                c.popStyle();
                continue;
            }

            if (display == IdentValue.TABLE_COLUMN) {
                contentList.add(new TableColumnContent(elem, style));
                c.popStyle();
                continue;
            }

            if (display == IdentValue.TABLE_COLUMN_GROUP) {
                contentList.add(new TableColumnGroupContent(elem, style));
                c.popStyle();
                continue;
            }

            if (display == IdentValue.TABLE_FOOTER_GROUP) {
                contentList.add(new TableFooterGroupContent(elem, style));
                c.popStyle();
                continue;
            }

            if (display == IdentValue.TABLE_HEADER_GROUP) {
                contentList.add(new TableHeaderGroupContent(elem, style));
                c.popStyle();
                continue;
            }

            if (display == IdentValue.TABLE_ROW_GROUP) {
                contentList.add(new TableRowGroupContent(elem, style));
                c.popStyle();
                continue;
            }
        }

        /* ignore after content, at least for now
        if (parentElement != null) {
            //TODO: after may be block!
            CascadedStyle after = c.getCss().getPseudoElementStyle(parentElement, "after");
            if (after != null && after.hasProperty(CSSName.CONTENT)) {
                String content = ((CSSPrimitiveValue) after.propertyByName(CSSName.CONTENT).getValue()).getStringValue();
                if (!content.equals("")) {//a worthwhile reduncancy-check
                    textContent = saveTextContent(textContent, contentList, parentElement, parent);
                    contentList.add(new StylePush("after", parentElement));
                    textContent = new StringBuffer();
                    textContent.append(content.replaceAll("\\\\A", "\n"));
                    contentList.add(new TextContent(parentElement, textContent.toString()));
                    textContent = null;
                    contentList.add(new StylePop("after", parentElement));
                }
            }
        }
        */

        if (firstLetterStyle != null) {
            contentList.addFirst(firstLetterStyle);
        }
        if (firstLineStyle != null) {
            contentList.addFirst(firstLineStyle);
        }

        return contentList;

    }

    private Iterator getChildIterator() {
        if (_children != null) return _children.iterator();
        return new Iterator() {
            NodeList nl = _elem.getChildNodes();
            int i = 0;

            public boolean hasNext() {
                return i < nl.getLength();
            }

            public Object next() {
                if (hasNext()) {
                    return nl.item(i++);
                } else {
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean isTopMarginCollapsed() {
        return _topMarginCollapsed;
    }

    public void setTopMarginCollapsed(boolean topMarginCollapsed) {
        _topMarginCollapsed = topMarginCollapsed;
    }

    public boolean mayCollapseInto() {
        return false;
    }

    public boolean isBottomMarginCollapsed() {
        return _bottomMarginCollapsed;
    }

    public void setBottomMarginCollapsed(boolean bottomMarginCollapsed) {
        _bottomMarginCollapsed = bottomMarginCollapsed;
    }

}

