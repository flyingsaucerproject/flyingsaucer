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
import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.layout.inline.WhitespaceStripper;

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
     * @param c      The current context. The current style in the context must correspond to that of the parent Content
     * @param parent The parent Content to get the child content for
     * @return A list of content.
     */
    static List getChildContentList(Context c, Content parent) {
        List inlineList = new LinkedList();
        List blockList = null;
        FirstLineStyle firstLineStyle = null;
        FirstLetterStyle firstLetterStyle = null;
        StringBuffer textContent = null;
        CascadedStyle parentStyle = parent.getStyle();
        Element parentElement = parent.getElement();

        if (parentElement != null) {
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

            //TODO: before and after may be block!
            //<br/> handling should be done by :before content
            CascadedStyle before = c.css.getPseudoElementStyle(parentElement, "before");
            if (before != null && before.hasProperty(CSSName.CONTENT)) {
                String content = ((CSSPrimitiveValue) before.propertyByName(CSSName.CONTENT).getValue()).getStringValue();
                if (!content.equals("")) {
                    inlineList.add(new StylePush(before, parentElement));
                    c.pushStyle(before);
                    textContent = new StringBuffer();
                    textContent.append(content.replaceAll("\\\\A", "\n"));
                    inlineList.add(new TextContent(parentElement, textContent.toString()));
                    textContent = null;
                    c.popStyle();
                    inlineList.add(new StylePop(parentElement));
                }
                //do not reset style here, because if this element is empty, we will not have changed context
            }
        }

        Node node = parentElement;
        if (node == null) node = ((DomToplevelNode) parent).getNode();
        NodeList children = node.getChildNodes();
        //each child node can result in only one addition to content
        for (int i = 0; i < children.getLength(); i++) {
            Node curr = children.item(i);
            if (curr.getNodeType() != Node.ELEMENT_NODE && curr.getNodeType() != Node.TEXT_NODE) continue;//must be a comment or pi or something

            if (curr.getNodeType() == Node.TEXT_NODE) {
                String text = curr.getNodeValue();
                if (textContent == null) textContent = new StringBuffer();
                textContent.append(text);
                continue;
            }

            Element elem = (Element) curr;
            CascadedStyle style = c.css.getCascadedStyle(elem);
            c.pushStyle(style);//just remember to pop it before continue

            if (isHidden(style)) {
                c.popStyle();
                continue;//at least for now, don't generate hidden content
            }

            if (isAbsoluteOrFixed(style)) {
                // Uu.p("adding replaced: " + curr);
                if (textContent != null) {
                    inlineList.add(new TextContent(parentElement, textContent.toString()));
                    textContent = null;
                }
                inlineList.add(new InlineBlockContent((Element) curr, style));
                c.popStyle();
                continue;
            }

            //have to check for float here already. The element may still be replaced, though
            if (isFloated(style)) {
                // Uu.p("adding floated block: " + curr);
                if (textContent != null) {
                    inlineList.add(new TextContent(parentElement, textContent.toString()));
                    textContent = null;
                }
                inlineList.add(new FloatedBlockContent((Element) curr, style));
                c.popStyle();
                continue;
            }

            if (isInlineBlock(style)) {
                //treat it like a replaced element
                if (textContent != null) {
                    inlineList.add(new TextContent(parentElement, textContent.toString()));
                    textContent = null;
                }
                inlineList.add(new InlineBlockContent(elem, style));
                c.popStyle();
                continue;
            }

            if (isRunIn(style)) {
                RunInContent runIn = new RunInContent(elem, style);
                List childContent = runIn.getChildContent(c);
                if (isBlockContent(childContent)) {
                    if (textContent != null) {
                        inlineList.add(new TextContent(parentElement, textContent.toString()));
                        textContent = null;
                    }
                    if (blockList == null) blockList = new LinkedList();
                    if (inlineList.size() != 0) {
                        blockList.addAll(resolveRunInContent(inlineList, parentElement, c));
                        inlineList = new LinkedList();
                    }
                    blockList.add(runIn);
                } else {
                    inlineList.add(runIn);
                    //resolve it when we can
                }
                c.popStyle();
                continue;
            }

            if (isBlockLevel(style)) {
                if (textContent != null) {
                    inlineList.add(new TextContent(parentElement, textContent.toString()));
                    textContent = null;
                }
                if (blockList == null) blockList = new LinkedList();
                if (inlineList.size() != 0) {
                    blockList.addAll(resolveRunInContent(inlineList, parentElement, c));
                    inlineList = new LinkedList();
                }
                BlockContent block = new BlockContent(elem, style);
                blockList.add(block);
                c.popStyle();
                continue;
            }

            //TODO:list-items, tables, etc.

            //TODO: this replaced thing is Namespace-dependent
            if (LayoutUtil.isReplaced(c, curr)) {
                // Uu.p("adding replaced: " + curr);
                if (textContent != null) {
                    inlineList.add(new TextContent(parentElement, textContent.toString()));
                    textContent = null;
                }
                inlineList.add(new InlineBlockContent((Element) curr, style));
                c.popStyle();
                continue;
            }

            //if we get here, we have inline content, need to get into it.
            if (textContent != null) {
                inlineList.add(new TextContent(parentElement, textContent.toString()));
                textContent = null;
            }
            Content inline = new InlineContent(elem, style);
            List childList = inline.getChildContent(c);
            if (isBlockContent(childList)) {
                //need to put current inlineList in front, with a StylePush appended
                inlineList.add(new StylePush(style, elem));//this is already pushed to context
                //the child list represents the entire contents of an element,
                //therefore we need not concern ourselves with style-changes, as they will even out
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
                if (inlineList.size() != 0) {
                    blockList.addAll(resolveRunInContent(inlineList, parentElement, c));
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
                inlineList.add(new StylePop(elem));//pop from c below
            } else {
                inlineList.add(new StylePush(style, elem));
                inlineList.addAll(childList);
                inlineList.add(new StylePop(elem));//pop from c below
            }
            c.popStyle();
        }

        if (textContent != null) {
            inlineList.add(new TextContent(parentElement, textContent.toString()));
            textContent = null;
        }
        if (parentElement != null) {
            //TODO: after may be block!
            CascadedStyle after = c.css.getPseudoElementStyle(parentElement, "after");
            if (after != null && after.hasProperty(CSSName.CONTENT)) {
                String content = ((CSSPrimitiveValue) after.propertyByName(CSSName.CONTENT).getValue()).getStringValue();
                if (!content.equals("")) {//a worthwhile reduncancy-check
                    if (textContent != null) {
                        inlineList.add(new TextContent(parentElement, textContent.toString()));
                        textContent = null;
                    }
                    inlineList.add(new StylePush(after, parentElement));
                    textContent = new StringBuffer();
                    textContent.append(content.replaceAll("\\\\A", "\n"));
                    inlineList.add(new TextContent(parentElement, textContent.toString()));
                    textContent = null;
                    inlineList.add(new StylePop(parentElement));
                }
            }
        }

        //have to check if there were run-ins pending
        if (isBlockContent(inlineList)) {
            if (blockList == null) blockList = new LinkedList();
            blockList.addAll(resolveRunInContent(inlineList, parentElement, c));
            inlineList = new LinkedList();
        }

        if (blockList == null) {
            inlineList = WhitespaceStripper.stripInlineContent(c, inlineList);
            if (inlineList.size() == 0) return inlineList;
            if (firstLetterStyle != null) {
                inlineList.add(0, firstLetterStyle);
            }
            if (firstLineStyle != null) {
                inlineList.add(0, firstLineStyle);
            }
            return inlineList;
        } else {
            inlineList = WhitespaceStripper.stripInlineContent(c, inlineList);
            if (inlineList.size() != 0) {
                blockList.add(new AnonymousBlockContent(parentElement, inlineList));
            }
            //HACK: there should instead be a way of propagating firstLineStyles down through box-hierarchy
            if (blockList.get(0) instanceof AnonymousBlockContent) {
                inlineList = ((AnonymousBlockContent) blockList.get(0)).getChildContent(c);
                blockList.remove(0);
                if (firstLetterStyle != null) {
                    inlineList.add(0, firstLetterStyle);
                    firstLetterStyle = null;
                }
                if (firstLineStyle != null) {
                    inlineList.add(0, firstLineStyle);
                    firstLineStyle = null;
                }
                blockList.add(0, new AnonymousBlockContent(parentElement, inlineList));
            }
            //END-HACK
            if (firstLetterStyle != null) {
                blockList.add(0, firstLetterStyle);
            }
            if (firstLineStyle != null) {
                blockList.add(0, firstLineStyle);
            }
            return blockList;
        }

    }

    static List resolveRunInContent(List pendingInlines, Element parentElement, Context c) {
        List inline = new LinkedList();
        List block = new LinkedList();
        for (Iterator i = pendingInlines.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof RunInContent) {
                inline = WhitespaceStripper.stripInlineContent(c, inline);
                if (inline.size() != 0) {
                    block.add(new AnonymousBlockContent(parentElement, inline));
                    inline = new LinkedList();
                }
                block.add(o);
            } else {
                inline.add(o);
            }
        }
        inline = WhitespaceStripper.stripInlineContent(c, inline);
        if (inline.size() != 0) {
            block.add(new AnonymousBlockContent(parentElement, inline));
        }
        return block;
    }

    //TODO: following methods should not need to be public
    public static boolean mayHaveFirstLetter(CascadedStyle style) {
        if (style == null) return false;//for DomToplevelNode
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
        //if(style == null) return false;//for DomToplevelNode
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
        if (style == null) return false;
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

    public static boolean isRunIn(CascadedStyle style) {
        if (!style.hasProperty(CSSName.DISPLAY)) return false;//default is inline
        String display = style.propertyByName(CSSName.DISPLAY).getValue().getCssText();
        if (display.equals("run-in")) return true;
        return false;
    }

    public static boolean isTable(CascadedStyle style) {
        if (!style.hasProperty(CSSName.DISPLAY)) return false;//default is inline
        String display = style.propertyByName(CSSName.DISPLAY).getValue().getCssText();
        if (display.equals("table")) return true;
        return false;
    }

    public static boolean isListItem(CascadedStyle style) {
        if (style == null) return false;
        if (!style.hasProperty(CSSName.DISPLAY)) return false;//default is inline
        String display = style.propertyByName(CSSName.DISPLAY).getValue().getCssText();
        if (display.equals("list-item")) return true;
        return false;
    }

    public static boolean isAbsoluteOrFixed(CascadedStyle style) {
        if (!style.hasProperty(CSSName.POSITION)) return false;//default is inline
        String position = style.propertyByName(CSSName.POSITION).getValue().getCssText();
        if (position.equals("absolute")) return true;
        if (position.equals("fixed")) return true;
        return false;
    }

    public static boolean isInlineBlock(CascadedStyle style) {
        if (!style.hasProperty(CSSName.DISPLAY)) return false;//default is inline
        String display = style.propertyByName(CSSName.DISPLAY).getValue().getCssText();
        if (display.equals("inline-block")) return true;
        return false;
    }

    public static boolean isFloated(CascadedStyle style) {
        if (style == null) return false;
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
        if (o instanceof RunInContent) return true;//if it has run-ins, it will be block, one way or another
        return false;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.17  2004/12/14 00:32:19  tobega
 * Cleaned and fixed line breaking. Renamed BodyContent to DomToplevelNode
 *
 * Revision 1.16  2004/12/13 01:29:39  tobega
 * Got the scrollbars back (by accident), and now we should be able to display DocumentFragments as well as Documents, if someone finds that useful.
 *
 * Revision 1.15  2004/12/13 00:04:55  tobega
 * Inserted a hack to make firstLine-styling of first anonymous block work. Should be replaced by better mechanism later.
 *
 * Revision 1.14  2004/12/12 23:19:25  tobega
 * Tried to get hover working. Something happens, but not all that's supposed to happen.
 *
 * Revision 1.13  2004/12/12 19:12:25  tobega
 * Stripping whitespace already in content-analysis. (Whitespace property does not apply to pseudos that are resolved later)
 *
 * Revision 1.12  2004/12/12 18:06:51  tobega
 * Made simple layout (inline and box) a bit easier to understand
 *
 * Revision 1.11  2004/12/12 16:11:03  tobega
 * Fixed bug concerning order of inline content. Added a demo for pseudo-elements.
 *
 * Revision 1.10  2004/12/12 06:05:29  tobega
 * Small improvement to value of :before and :after. Wonder why inline elements get floated out?
 *
 * Revision 1.9  2004/12/12 05:51:48  tobega
 * Now things run. But there is a lot to do before it looks as nice as it did. At least we now have :before and :after content and handling of breaks by css.
 *
 * Revision 1.8  2004/12/12 03:32:56  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.7  2004/12/12 03:05:12  tobega
 * Making progress
 *
 * Revision 1.6  2004/12/12 02:49:58  tobega
 * Making progress
 *
 * Revision 1.5  2004/12/11 21:14:46  tobega
 * Prepared for handling run-in content (OK, I know, a side-track). Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
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

