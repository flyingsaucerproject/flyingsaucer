/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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
package org.xhtmlrenderer.layout;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.inline.content.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author joshy
 */
public class InlineUtil {

    /**
     * Description of the Method
     *
     * @param node_list PARAM
     * @return Returns
     */
    public static Node nextTextNode(List node_list) {
        if (node_list.size() < 1) {
            return null;
        }
        Node nd = (Node) node_list.get(0);
        node_list.remove(nd);
        return nd;
    }


    /**
     * Gets the inlineNodeList attribute of the InlineUtil class
     *
     * @param elem PARAM
     * @param c    PARAM
     * @return The inlineNodeList value
     */
    public static List getInlineNodeList(Element elem, Context c) {
        return getInlineNodeList(elem, elem, c, false);
    }

    /**
     * Gets the inlineNodeList attribute of the InlineUtil class
     *
     * @param root           PARAM
     * @param elem           PARAM
     * @param c              PARAM
     * @param stop_at_blocks PARAM
     * @return The inlineNodeList value
     */
    public static List getInlineNodeList(Node root, Element elem, Context c, boolean stop_at_blocks) {
        List list = new ArrayList();
        if (root == null) {
            return list;
        }
        if (elem == null) {
            return list;
        }
        if (!elem.hasChildNodes()) {
            //u.p("it's empty");
            return list;
        }

        // u.p("starting at: " + root);
        Node curr = root;
        while (true) {
            // u.p("now list = " + list);

            // handle the current node
            // skip if the node is the element
            handling: if (curr != elem) {
                if (curr.getNodeType() == curr.TEXT_NODE) {
                    // u.p("adding text: " + curr);
                    list.add(curr);
                    //root = curr;
                    break handling;
                }

                if (LayoutUtil.isReplaced(c, curr)) {
                    // u.p("adding replaced: " + curr);
                    list.add(curr);
                    //root = curr;
                    break handling;
                }

                if (LayoutUtil.isFloatedBlock(curr, c)) {
                    // u.p("adding floated block: " + curr);
                    list.add(curr);
                    //root = curr;
                    break handling;
                }

                if (c.getRenderingContext().getLayoutFactory().isBreak(curr)) {
                    // u.p("adding break: " + curr);
                    list.add(curr);
                    //root = curr;
                    break handling;
                }

                if (stop_at_blocks) {
                    if (LayoutUtil.isBlockNode(curr, c)) {
                        //u.p("at block boundary");
                        return list;
                    }
                }
            }

            //recursing
            if (curr.hasChildNodes()) {
                // u.p("about to test: " + curr);
                // if it's a floating block we don't want to recurse
                if (!LayoutUtil.isFloatedBlock(curr, c) &&
                        !LayoutUtil.isReplaced(c, curr)) {
                    curr = curr.getFirstChild();
                    // u.p("going to first child " + curr);
                    continue;
                }

                // it's okay to recurse if it's the root that's the float,
                // not the node being examined. this only matters when we
                // start the loop at the root of a floated block
                
                if (LayoutUtil.isFloatedBlock(curr, c)) {
                    if (curr == elem) {
                        curr = curr.getFirstChild();
                        continue;
                    }
                }

            }

            if (curr.getNextSibling() != null) {
                curr = curr.getNextSibling();
                // u.p("going to next sibling: " + curr);
                continue;
            }

            // keep going up until we get another sibling
            // or we are at elem.
            while (true) {
                curr = curr.getParentNode();
                // u.p("going to parent: " + curr);
                // if we are at the top then return null
                if (curr == elem) {
                    // u.p("at the top again. returning null");
                    // u.p("returning the list");
                    // u.p(list);
                    return list;
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
        CalculatedStyle style = null;
        while (true) {
            // u.p("now list = " + list);
            if (style == null) style = c.css.getStyle(curr);

            // handle the nodes
            // skip first time if root was the element
            handling: if (curr != elem) {
                if (curr.getNodeType() == curr.TEXT_NODE) {
                    // u.p("adding textContent: " + curr);
                    String text = curr.getNodeValue();
                    if (textContent == null) textContent = new TextContent((Element) curr.getParentNode(), style);
                    textContent.append(text);
                    break handling;
                }

                //TODO: what if a replaced element has :before and/or :after content?
                if (LayoutUtil.isReplaced(c, curr)) {
                    // u.p("adding replaced: " + curr);
                    if (textContent != null) {
                        contentList.add(textContent);
                        textContent = null;
                    }
                    CalculatedStyle replacedStyle = c.css.getStyle(curr);
                    contentList.add(new ReplacedContent((Element) curr, replacedStyle));
                    break handling;
                }

                //TODO: what if a floated block has :before and/or :after content?
                if (LayoutUtil.isFloatedBlock(curr, c)) {
                    // u.p("adding floated block: " + curr);
                    if (textContent != null) {
                        contentList.add(textContent);
                        textContent = null;
                    }
                    CalculatedStyle floatedStyle = c.css.getStyle(curr);
                    contentList.add(new FloatedBlockContent((Element) curr, floatedStyle));
                    break handling;
                }

                //TODO: check if this should maybe always be true
                if (stop_at_blocks) {
                    if (LayoutUtil.isBlockNode(curr, c)) {
                        //u.p("at block boundary");
                        return contentList;
                    }
                }
            }

            if (curr.getNodeType() == Node.ELEMENT_NODE) {
                //put in a marker if there is first-line styling
                CascadedStyle firstLine = c.css.getPseudoElementStyle(curr, "first-line");
                if (firstLine != null) {
                    if (textContent != null) {
                        contentList.add(textContent);
                        textContent = null;
                    }
                    contentList.add(new FirstLineStyle(firstLine));
                }
                //put in a marker if there is first-letter styling
                CascadedStyle firstLetter = c.css.getPseudoElementStyle(curr, "first-letter");
                if (firstLetter != null) {
                    if (textContent != null) {
                        contentList.add(textContent);
                        textContent = null;
                    }
                    contentList.add(new FirstLetterStyle(firstLetter));
                }
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

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.19  2004/12/06 02:55:43  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation.
 *
 * Revision 1.18  2004/12/06 00:19:15  tobega
 * Worked on handling :before and :after. Got sidetracked by BasicPanel causing layout to be done twice: solved. If solution causes problems, check BasicPanel.setSize
 *
 * Revision 1.17  2004/12/05 19:42:43  tobega
 * Made recursion in InlineUtil easier to understand
 *
 * Revision 1.16  2004/12/05 18:11:38  tobega
 * Now uses style cache for pseudo-element styles. Also started preparing to replace inline node handling with inline content handling.
 *
 * Revision 1.15  2004/11/27 15:46:38  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/18 23:29:38  joshy
 * fixed xml bug
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/18 16:45:11  joshy
 * improved the float code a bit.
 * now floats are automatically forced to be blocks
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/11/18 14:26:22  joshy
 * more code cleanup
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/18 02:37:26  joshy
 * moved most of default layout into layout util or box layout
 *
 * start spliting parts of box layout into the block subpackage
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/14 16:40:58  joshy
 * refactored layout factory
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/09 16:24:30  joshy
 * moved float code into separate class
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/08 20:50:59  joshy
 * improved float support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/11/06 01:50:41  joshy
 * support for line-height
 * cleaned up the alice demo
 * added unit tests for font family selection and line-height
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/04 15:35:45  joshy
 * initial float support
 * includes right and left float
 * cannot have more than one float per line per side
 * floats do not extend beyond enclosing block
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/10/28 13:46:32  joshy
 * removed dead code
 * moved code about specific elements to the layout factory (link and br)
 * fixed form rendering bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

