/*
 * {{{ header & license
 * Copyright (c) 2004 Torbjörn Gannholm, Joshua Marinacci
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
     * @param elem PARAM
     * @param c    PARAM
     * @return A list of content.
     *         If it contains BlockContent, it will only contain AnonymousBlockContent and BlockContent objects
     */
    public static List getInlineContentList(Element elem, Context c) {
        List inlineList = new LinkedList();
        List blockList = null;
        FirstLineStyle firstLineStyle = null;
        FirstLetterStyle firstLetterStyle = null;
        if (elem == null) {
            throw new NullPointerException("Trying to get ContentList for null element");
        }
        if (!elem.hasChildNodes()) {
            //u.p("it's empty");
            return inlineList;
        }

        // u.p("starting at: " + root);
        Node curr = elem;
        TextContent textContent = null;
        CalculatedStyle style = c.css.getStyle(elem);
        if (mayHaveFirstLine(style)) {
            //put in a marker if there is first-line styling
            CascadedStyle firstLine = c.css.getPseudoElementStyle(curr, "first-line");
            if (firstLine != null) {
                firstLineStyle = new FirstLineStyle(firstLine);
            }
        }
        if (mayHaveFirstLetter(style)) {
            //put in a marker if there is first-letter styling
            CascadedStyle firstLetter = c.css.getPseudoElementStyle(curr, "first-letter");
            if (firstLetter != null) {
                firstLetterStyle = new FirstLetterStyle(firstLetter);
            }
        }
        while (true) {
            // u.p("now list = " + list);
            if (style == null) style = c.css.getStyle(curr);
            boolean descend = true;

            if (style.getStringProperty(CSSName.DISPLAY).equals("none")) descend = false;

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
                        inlineList.add(textContent);
                        textContent = null;
                    }
                    inlineList.add(new ReplacedContent((Element) curr, elementStyle));
                    descend = false;
                    break handling;
                }

                //TODO: check CSS-spec 9.7 for computing values for floats
                if (LayoutUtil.isFloated(elementStyle)) {
                    // u.p("adding floated block: " + curr);
                    if (textContent != null) {
                        inlineList.add(textContent);
                        textContent = null;
                    }
                    inlineList.add(new FloatedBlockContent((Element) curr, elementStyle));
                    descend = false;
                    break handling;
                }

                if (LayoutUtil.isBlockNode(curr, c)) {
                    if (textContent != null) {
                        inlineList.add(textContent);
                        textContent = null;
                    }
                    if (blockList == null) blockList = new LinkedList();
                    //TODO: handle run-in here
                    if (inlineList.size() != 0) {
                        blockList.add(new AnonymousBlockContent(elem, c.css.getStyle(elem), inlineList));
                        inlineList = new LinkedList();
                    }
                    blockList.add(new BlockContent((Element) curr, elementStyle));
                    descend = false;
                }
                //TODO: handle run-in content separately
                //TODO: how about Absolute and Fixed children?
            }

            if (curr.getNodeType() == Node.ELEMENT_NODE && descend) {
                //TODO: before and after may be block!
                //<br/> handling should be done by :before content
                CascadedStyle before = c.css.getPseudoElementStyle(curr, "before");
                if (before != null) {
                    CalculatedStyle parentStyle = c.css.getStyle(curr);
                    CalculatedStyle derived = c.css.getDerivedStyle(parentStyle, before);
                    String content = derived.getStringProperty(CSSName.CONTENT);
                    if (!content.equals("")) {
                        if (textContent != null) {
                            inlineList.add(textContent);
                            textContent = null;
                        }
                        textContent = new TextContent((Element) curr, derived);
                        textContent.append(content.replaceAll("\\\\A", "\n"));
                        inlineList.add(textContent);
                        textContent = null;
                    }
                    //do not reset style here, because if this element is empty, we will not have changed context
                }
            }

            if (curr.hasChildNodes() && descend) {
                //new element, new style
                if (textContent != null) {
                    inlineList.add(textContent);
                    textContent = null;
                }
                style = null;

                curr = curr.getFirstChild();
                // u.p("going to first child " + curr);
                continue;

            }

            // keep going up until we get another sibling
            // or we are at elem.
            while (true) {

                if (curr.getNodeType() == Node.ELEMENT_NODE && descend) {
                    //check for :after content
                    //this has not been checked yet because this element had child nodes
                    CascadedStyle after = c.css.getPseudoElementStyle((Element) curr, "after");
                    if (after != null) {
                        CalculatedStyle parentStyle = c.css.getStyle(curr);
                        CalculatedStyle derived = c.css.getDerivedStyle(parentStyle, after);
                        String content = derived.getStringProperty(CSSName.CONTENT);
                        if (!content.equals("")) {
                            if (textContent != null) {
                                inlineList.add(textContent);
                                textContent = null;
                            }
                            textContent = new TextContent((Element) curr, derived);
                            textContent.append(content.replaceAll("\\\\A", "\n"));
                            inlineList.add(textContent);
                            textContent = null;
                        }
                    }
                }

                // u.p("going to parent: " + curr);
                // if we are at the top then return null
                if (curr == elem) {
                    if (blockList == null) {//this was inline content
                        if (firstLetterStyle != null) {
                            inlineList.add(0, firstLetterStyle);
                        }
                        if (firstLineStyle != null) {
                            inlineList.add(0, firstLineStyle);
                        }
                        return inlineList;
                    } else {
                        if (inlineList.size() != 0) {
                            blockList.add(new AnonymousBlockContent(elem, c.css.getStyle(elem), inlineList));
                        }
                        if (firstLetterStyle != null) {
                            blockList.add(0, firstLetterStyle);
                        }
                        if (firstLineStyle != null) {
                            blockList.add(0, firstLineStyle);
                        }
                        return blockList;
                    }
                }

                if (curr.getNextSibling() != null) {
                    curr = curr.getNextSibling();
                    // u.p("going to next sibling: " + curr);
                    break;
                }

                curr = curr.getParentNode();
                //new element, new style
                if (textContent != null) {
                    inlineList.add(textContent);
                    textContent = null;
                }
                style = null;

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
 * Revision 1.3  2004/12/10 06:51:00  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.2  2004/12/09 00:11:50  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.1  2004/12/08 00:42:30  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation. Also fixed 2 irritating bugs!
 *
 */

