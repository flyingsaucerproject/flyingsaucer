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
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Generates lists of Content.
 * This is the only place where it should be determined about the type of content an element is.
 * The layout and rendering should only work with Boxes and Content
 */
public class ContentUtil {

    /**
     * Gets the inline content of a sequence of nodes
     *
     * @param c      PARAM
     * @param parent
     * @return A list of content.
     */
    static List getChildContentList(Context c, Content parent) {
        List inlineList = new LinkedList();
        List blockList = null;
        FirstLineStyle firstLineStyle = null;
        FirstLetterStyle firstLetterStyle = null;


        TextContent textContent = null;
        CascadedStyle parentStyle = parent.getStyle();
        Element parentElement = parent.getElement();
        if (mayHaveFirstLine(parentStyle)) {
            //put in a marker if there is first-line styling
            CascadedStyle firstLine = c.css.getPseudoElementStyle(parentElement, "first-line");
            if (firstLine != null) {
                firstLineStyle = new FirstLineStyle(firstLine);
            }
        }
        if (mayHaveFirstLetter(parentStyle)) {
            //put in a marker if there is first-letter styling
            CascadedStyle firstLetter = c.css.getPseudoElementStyle(parentElement, "first-letter");
            if (firstLetter != null) {
                firstLetterStyle = new FirstLetterStyle(firstLetter);
            }
        }
        if (parentElement != null) {
            //TODO: before and after may be block!
            //<br/> handling should be done by :before content
            CascadedStyle before = c.css.getPseudoElementStyle(parentElement, "before");
            if (before != null && before.hasProperty(CSSName.CONTENT)) {
                String content = before.propertyByName(CSSName.CONTENT).getValue().getCssText();
                if (!content.equals("")) {
                    if (textContent != null) {
                        inlineList.add(textContent);
                        textContent = null;
                    }
                    textContent = new TextContent((Element) parentElement);
                    textContent.append(content.replaceAll("\\\\A", "\n"));
                    inlineList.add(textContent);
                    textContent = null;
                }
                //do not reset style here, because if this element is empty, we will not have changed context
            }
        }

        Node node = parentElement;
        if (node == null) node = ((BodyContent) parent).getNode();
        NodeList children = node.getChildNodes();
        //each child node can result in only one addition to content
        for (int i = 0; i < children.getLength(); i++) {
            Node curr = children.item(i);

            if (curr.getNodeType() == Node.TEXT_NODE) {
                String text = curr.getNodeValue();
                if (textContent == null) textContent = new TextContent((Element) curr.getParentNode());
                textContent.append(text);
                continue;
            }

            if (curr.getNodeType() != Node.ELEMENT_NODE) break;//must be a comment or pi or something
            Element elem = (Element) curr;
            CascadedStyle style = c.css.getCascadedStyle(elem);

            if (isHidden(style)) {
                continue;//at least for now, don't generate hidden content
            }

            //TODO: this replaced thing is Namespace-dependent    
            if (LayoutUtil.isReplaced(c, curr)) {
                // u.p("adding replaced: " + curr);
                if (textContent != null) {
                    inlineList.add(textContent);
                    textContent = null;
                }
                inlineList.add(new ReplacedContent((Element) curr, style));
                continue;
            }

            //TODO: check CSS-spec 9.7 for computing values for floats
            if (isFloated(style)) {
                // u.p("adding floated block: " + curr);
                if (textContent != null) {
                    inlineList.add(textContent);
                    textContent = null;
                }
                inlineList.add(new FloatedBlockContent((Element) curr, style));
                continue;
            }

            if (isInlineBlock(style)) {
                //treat it like a replaced element
                if (textContent != null) {
                    inlineList.add(textContent);
                    textContent = null;
                }
                inlineList.add(new ReplacedContent(elem, style));
                continue;
            }

            if (isBlockLevel(style)) {
                if (textContent != null) {
                    inlineList.add(textContent);
                    textContent = null;
                }
                if (blockList == null) blockList = new LinkedList();
                //TODO: handle run-in here
                if (inlineList.size() != 0) {
                    blockList.add(new AnonymousBlockContent(parentElement, parentStyle, inlineList));
                    inlineList = new LinkedList();
                }
                blockList.add(new BlockContent((Element) curr, style));
                continue;
            }
            //TODO: handle run-in content separately
            //TODO: how about Absolute and Fixed children?
            //TODO:list-items, tables, etc.

            //if we get here, we have inline content, need to get into it.
            Content inline = new InlineContent(elem, style);
            List childList = inline.getChildContent(c);
            if (isBlockContent(childList)) {
                //need to put current inlineList in front, with a StylePush appended
                if (textContent != null) {
                    inlineList.add(textContent);
                    textContent = null;
                }
                inlineList.add(new StylePush(style));
                for (Iterator ci = childList.iterator(); ci.hasNext();) {
                    Object o = ci.next();
                    if (o instanceof BlockContent) break;
                    ci.remove();//have to take it out to avoid duplicates
                    if (o instanceof AnonymousBlockContent) {
                        inlineList.addAll(((AnonymousBlockContent) o).getChildContent(c));
                    } else {
                        inlineList.add(o);
                    }
                }
                if (blockList == null) blockList = new LinkedList();
                //TODO: handle run-in here
                if (inlineList.size() != 0) {
                    blockList.add(new AnonymousBlockContent(parentElement, parentStyle, inlineList));
                    inlineList = new LinkedList();
                }
                //extract any trailing AnonymousBlock and put it in the inlineList
                Object last = childList.get(childList.size() - 1);
                if (last instanceof AnonymousBlockContent) {
                    inlineList.addAll(((AnonymousBlockContent) last).getChildContent(c));
                    childList.remove(childList.size() - 1);
                }
                //add the rest of the children
                blockList.addAll(childList);
                //append StylePop
                inlineList.add(new StylePop());
            } else {
                inlineList.add(new StylePush(style));
                inlineList.addAll(childList);
                inlineList.add(new StylePop());
            }
        }

        if (textContent != null) {
            inlineList.add(textContent);
            textContent = null;
        }
        if (parentElement != null) {
            //TODO: after may be block!
            CascadedStyle after = c.css.getPseudoElementStyle(parentElement, "after");
            if (after != null && after.hasProperty(CSSName.CONTENT)) {
                String content = after.propertyByName(CSSName.CONTENT).getValue().getCssText();
                if (!content.equals("")) {
                    textContent = new TextContent((Element) parentElement);
                    textContent.append(content.replaceAll("\\\\A", "\n"));
                    inlineList.add(textContent);
                    textContent = null;
                }
            }
        }

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
                blockList.add(new AnonymousBlockContent(parentElement, parentStyle, inlineList));
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

    public static boolean mayHaveFirstLetter(CascadedStyle style) {
        if (!style.hasProperty(CSSName.DISPLAY)) return false;//default is inline
        String display = style.propertyByName(CSSName.DISPLAY).getValue().getCssText();
        if (display.equals("block")) return true;
        if (display.equals("list-item")) return true;
        if (display.equals("table-cell")) return true;
        if (display.equals("table-caption")) return true;
        if (display.equals("inline-block")) return true;
        return false;
    }

    public static boolean mayHaveFirstLine(CascadedStyle style) {
        if (!style.hasProperty(CSSName.DISPLAY)) return false;//default is inline
        String display = style.propertyByName(CSSName.DISPLAY).getValue().getCssText();
        if (display.equals("block")) return true;
        if (display.equals("list-item")) return true;
        if (display.equals("run-in")) return true;
        if (display.equals("table")) return true;
        if (display.equals("table-cell")) return true;
        if (display.equals("table-caption")) return true;
        if (display.equals("inline-block")) return true;
        return false;
    }

    public static boolean isBlockLevel(CascadedStyle style) {
        if (!style.hasProperty(CSSName.DISPLAY)) return false;//default is inline
        String display = style.propertyByName(CSSName.DISPLAY).getValue().getCssText();
        if (display.equals("block")) return true;
        if (display.equals("list-item")) return true;
        if (display.equals("table")) return true;
        return false;
    }

    public static boolean isHidden(CascadedStyle style) {
        if (!style.hasProperty(CSSName.DISPLAY)) return false;//default is inline
        String display = style.propertyByName(CSSName.DISPLAY).getValue().getCssText();
        if (display.equals("none")) return true;
        return false;
    }

    public static boolean isInlineBlock(CascadedStyle style) {
        if (!style.hasProperty(CSSName.DISPLAY)) return false;//default is inline
        String display = style.propertyByName(CSSName.DISPLAY).getValue().getCssText();
        if (display.equals("none")) return true;
        return false;
    }

    public static boolean isFloated(CascadedStyle style) {
        if (!style.hasProperty(CSSName.DISPLAY)) return false;//default is inline
        String float_val = style.propertyByName(CSSName.DISPLAY).getValue().getCssText();
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

    public static boolean isBlockContent(List childContent) {
        if (childContent.size() == 0) return false;
        Object o = childContent.get(childContent.size() - 1);
        if (o instanceof BlockContent) return true;
        if (o instanceof AnonymousBlockContent) return true;
        return false;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2004/12/11 18:18:09  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
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

