/*
 * {{{ header & license
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
 * }}}
 */
package org.xhtmlrenderer.layout.content;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Generates lists of Content
 */
public class ContentUtil {

    /**
     * Utility to pop the first Content from the list
     *
     * @param contentList the list to remove the first Content from
     * @return the first Content in the list
     */
    public static Content nextContent(List contentList) {
        if (contentList.size() < 1) {
            return null;
        }
        Content nd = (Content) contentList.get(0);
        contentList.remove(0);
        return nd;
    }

    /**
     * Gets the inline content of a sequence of nodes
     *
     * @param root           PARAM
     * @param elem           PARAM
     * @param c              PARAM
     * @param stop_at_blocks PARAM
     * @return The inlineNodeList value
     */
    public static List getInlineContentList(Node root, Element elem, Context c, boolean stop_at_blocks) {
        List contentList = new LinkedList();
        if (root == null) {
            throw new NullPointerException("Trying to get ContentList for null root node");
        }
        if (elem == null) {
            throw new NullPointerException("Trying to get ContentList for null element");
        }
        if (!elem.hasChildNodes()) {
            //u.p("it's empty");
            return contentList;
        }

        // u.p("starting at: " + root);
        Node curr = root;
        TextContent textContent = null;
        CalculatedStyle style = c.css.getStyle(elem);
        if (root == elem || root == elem.getFirstChild()) {
            if (mayHaveFirstLine(style)) {
                //put in a marker if there is first-line styling
                CascadedStyle firstLine = c.css.getPseudoElementStyle(curr, "first-line");
                if (firstLine != null) {
                    contentList.add(new FirstLineStyle(firstLine));
                }
            }
            if (mayHaveFirstLetter(style)) {
                //put in a marker if there is first-letter styling
                CascadedStyle firstLetter = c.css.getPseudoElementStyle(curr, "first-letter");
                if (firstLetter != null) {
                    contentList.add(new FirstLetterStyle(firstLetter));
                }
            }
        }
        while (true) {
            // u.p("now list = " + list);
            if (style == null) style = c.css.getStyle(curr);

            // handle the nodes
            // skip first time if root was the element
            handling: if (curr != elem) {
                if (curr.getNodeType() == Node.TEXT_NODE) {
                    // u.p("adding textContent: " + curr);
                    String text = curr.getNodeValue();
                    if (textContent == null) textContent = new TextContent((Element) curr.getParentNode(), style);
                    textContent.append(text);
                    break handling;
                }

                if (curr.getNodeType() != Node.ELEMENT_NODE) break handling;

                //Only elements here:
                CalculatedStyle elementStyle = c.css.getStyle(curr);

                if (LayoutUtil.isReplaced(c, curr)) {
                    // u.p("adding replaced: " + curr);
                    if (textContent != null) {
                        contentList.add(textContent);
                        textContent = null;
                    }
                    contentList.add(new ReplacedContent((Element) curr, elementStyle));
                    break handling;
                }

                //TODO: check CSS-spec 9.7 for computing values for floats
                if (LayoutUtil.isFloated(elementStyle)) {
                    // u.p("adding floated block: " + curr);
                    if (textContent != null) {
                        contentList.add(textContent);
                        textContent = null;
                    }
                    contentList.add(new FloatedBlockContent((Element) curr, elementStyle));
                    break handling;
                }

                if (LayoutUtil.isBlockNode(curr, c)) {
                    if (textContent != null) {
                        contentList.add(textContent);
                        textContent = null;
                    }
                    contentList.add(new BlockContent((Element) curr, elementStyle));
                }
                //TODO: handle run-in separately
                //TODO: how about Absolute and Fixed children?
            }

            if (curr.getNodeType() == Node.ELEMENT_NODE) {
                //TODO: before and after may be block!
                //<br/> handling should be done by :before content
                CascadedStyle before = c.css.getPseudoElementStyle(curr, "before");
                if (before != null) {
                    CalculatedStyle parentStyle = c.css.getStyle(curr);
                    CalculatedStyle derived = c.css.getDerivedStyle(parentStyle, before);
                    String content = derived.getStringProperty(CSSName.CONTENT);
                    if (!content.equals("")) {
                        if (textContent != null) {
                            contentList.add(textContent);
                            textContent = null;
                        }
                        textContent = new TextContent((Element) curr, derived);
                        textContent.append(content.replaceAll("\\\\A", "\n"));
                        contentList.add(textContent);
                        textContent = null;
                    }
                    //do not reset style here, because if this element is empty, we will not have changed context
                }
            }

            if (curr.hasChildNodes()) {
                // u.p("about to test: " + curr);
                // if it's a floating block we don't want to recurse
                if (!LayoutUtil.isFloatedBlock(curr, c) &&
                        !LayoutUtil.isReplaced(c, curr)) {
                    //new element, new style
                    if (textContent != null) {
                        contentList.add(textContent);
                        textContent = null;
                    }
                    style = null;

                    curr = curr.getFirstChild();
                    // u.p("going to first child " + curr);
                    continue;
                }

                // it's okay to recurse if it's the root that's the float,
                // not the node being examined. this only matters when we
                // start the loop at the root of a floated block

                if (LayoutUtil.isFloatedBlock(root, c)) {
                    if (root == elem) {
                        //ok, the style should already be set and textContent should be null
                        curr = curr.getFirstChild();
                        continue;
                    }
                }

            } else if (curr.getNodeType() == Node.ELEMENT_NODE) {
                //it might still have :after content
                CascadedStyle after = c.css.getPseudoElementStyle((Element) curr, "after");
                if (after != null) {
                    CalculatedStyle parentStyle = c.css.getStyle(curr);
                    CalculatedStyle derived = c.css.getDerivedStyle(parentStyle, after);
                    String content = derived.getStringProperty(CSSName.CONTENT);
                    if (!content.equals("")) {
                        if (textContent != null) {
                            contentList.add(textContent);
                            textContent = null;
                        }
                        textContent = new TextContent((Element) curr, derived);
                        textContent.append(content.replaceAll("\\\\A", "\n"));
                        contentList.add(textContent);
                        textContent = null;
                    }
                }
                if (curr == elem) {
                    //elem was empty, that can happen, right?
                    return contentList;
                }
            }

            if (curr.getNextSibling() != null) {
                //ok, the style should still apply
                curr = curr.getNextSibling();
                // u.p("going to next sibling: " + curr);
                continue;
            }

            // keep going up until we get another sibling
            // or we are at elem.
            while (true) {
                curr = curr.getParentNode();
                //new element, new style
                if (textContent != null) {
                    contentList.add(textContent);
                    textContent = null;
                }
                style = null;

                if (curr.getNodeType() == Node.ELEMENT_NODE) {
                    //check for :after content
                    //this has not been checked yet because this element had child nodes
                    CascadedStyle after = c.css.getPseudoElementStyle((Element) curr, "after");
                    if (after != null) {
                        CalculatedStyle parentStyle = c.css.getStyle(curr);
                        CalculatedStyle derived = c.css.getDerivedStyle(parentStyle, after);
                        String content = derived.getStringProperty(CSSName.CONTENT);
                        if (!content.equals("")) {
                            //textContent must be null here
                            textContent = new TextContent((Element) curr, derived);
                            textContent.append(content.replaceAll("\\\\A", "\n"));
                            contentList.add(textContent);
                            textContent = null;
                        }
                    }
                }

                // u.p("going to parent: " + curr);
                // if we are at the top then return null
                if (curr == elem) {
                    // u.p("at the top again. returning null");
                    // u.p("returning the list");
                    // u.p(list);
                    return contentList;
                    //return null;
                }

                if (curr.getNextSibling() != null) {
                    curr = curr.getNextSibling();
                    // u.p("going to next sibling: " + curr);
                    break;
                }

            }

        }

    }

    public static boolean mayHaveFirstLetter(CalculatedStyle style) {
        String display = style.getStringProperty(CSSName.DISPLAY);
        if (display.equals("block")) return true;
        if (display.equals("list-item")) return true;
        if (display.equals("table-cell")) return true;
        if (display.equals("table-caption")) return true;
        if (display.equals("inline-block")) return true;
        return false;
    }

    public static boolean mayHaveFirstLine(CalculatedStyle style) {
        String display = style.getStringProperty(CSSName.DISPLAY);
        if (display.equals("block")) return true;
        if (display.equals("list-item")) return true;
        if (display.equals("run-in")) return true;
        if (display.equals("table")) return true;
        if (display.equals("table-cell")) return true;
        if (display.equals("table-caption")) return true;
        if (display.equals("inline-block")) return true;
        return false;
    }

    public static boolean isBlockLevel(CalculatedStyle style) {
        String display = style.getStringProperty(CSSName.DISPLAY);
        if (display.equals("block")) return true;
        if (display.equals("list-item")) return true;
        if (display.equals("table")) return true;
        return false;
    }

    public static boolean isFloated(CalculatedStyle style) {
        String float_val = style.getStringProperty("float");
        if (float_val == null) {
            return false;
        }
        if (float_val.equals("left")) {
            return true;
        }
        if (float_val.equals("right")) {
            return true;
        }
        return false;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2004/12/08 00:42:30  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation. Also fixed 2 irritating bugs!
 *
 */

