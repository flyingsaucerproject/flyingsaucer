/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjï¿½rn Gannholm, Joshua Marinacci
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.extend.ContentFunction;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.LayoutContext;


/**
 * Generates lists of Content. This is the only place where it should be
 * determined about the type of content an element is. The layout and rendering
 * should only work with Boxes and Content
 *
 * @author Joshua Marinacci
 * @author Torbjörn Gannholm
 */
public class ContentUtil {

    //TODO: following methods should not need to be public
    /**
     * Description of the Method
     *
     * @param style PARAM
     * @return Returns
     */
    public static boolean mayHaveFirstLetter(CalculatedStyle style) {
        if (style == null) {
            return false;
        }//for DomToplevelNode
        IdentValue display = style.getIdent(CSSName.DISPLAY);
        return display != null &&
                (display == IdentValue.BLOCK ||
                display == IdentValue.LIST_ITEM ||
                display == IdentValue.TABLE_CELL ||
                display == IdentValue.TABLE_CAPTION ||
                display == IdentValue.INLINE_BLOCK);
    }

    /**
     * Description of the Method
     *
     * @param style PARAM
     * @return Returns
     */
    public static boolean mayHaveFirstLine(CalculatedStyle style) {
        if (style == null) {
            return false;
        }//for DomToplevelNode
        IdentValue display = style.getIdent(CSSName.DISPLAY);
        return display != null &&
                (display == IdentValue.BLOCK ||
                display == IdentValue.LIST_ITEM ||
                display == IdentValue.RUN_IN ||
                display == IdentValue.TABLE ||
                display == IdentValue.TABLE_CELL ||
                display == IdentValue.TABLE_CAPTION ||
                display == IdentValue.INLINE_BLOCK);
    }

    /**
     * Description of the Method
     *
     * @param childContent PARAM
     * @return Returns
     */
    public static boolean hasBlockContent(List childContent) {
        for (Iterator i = childContent.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof TableContent) {
                return true;
            }
            if (o instanceof BlockContent) {
                return true;
            }
            if (o instanceof AnonymousBlockContent) {
                return true;
            }
            if (o instanceof RunInContent) {
                return true;
            }//if it has run-ins, it will be block, one way or another
        }
        return false;
    }

    /**
     * Gets the blockLevel attribute of the ContentUtil class
     *
     * @param style PARAM
     * @return The blockLevel value
     */
    public static boolean isBlockLevel(CalculatedStyle style) {
        if (style == null) {
            return false;
        }
        IdentValue display = style.getIdent(CSSName.DISPLAY);
        return display != null && (display == IdentValue.BLOCK || display == IdentValue.LIST_ITEM || display == IdentValue.TABLE);
    }

    /**
     * Gets the hidden attribute of the ContentUtil class
     *
     * @param style PARAM
     * @return The hidden value
     */
    public static boolean isHidden(CalculatedStyle style) {
        IdentValue display = style.getIdent(CSSName.DISPLAY);
        return display != null && display == IdentValue.NONE;
    }

    /**
     * Gets the runIn attribute of the ContentUtil class
     *
     * @param style PARAM
     * @return The runIn value
     */
    public static boolean isRunIn(CalculatedStyle style) {
        IdentValue display = style.getIdent(CSSName.DISPLAY);
        return display != null && display == IdentValue.RUN_IN;
    }

    /**
     * Gets the table attribute of the ContentUtil class
     *
     * @param style PARAM
     * @return The table value
     */
    public static boolean isTable(CalculatedStyle style) {
        IdentValue display = style.getIdent(CSSName.DISPLAY);
        return display != null && display == IdentValue.TABLE;
    }

    /**
     * Gets the table attribute of the ContentUtil class
     *
     * @param style PARAM
     * @return The table value
     */
    public static boolean isTableDescendant(CalculatedStyle style) {
        IdentValue display = style.getIdent(CSSName.DISPLAY);
        if (display == null) return false;
        if (display == IdentValue.TABLE) return false;
        if (display.toString().startsWith("table")) return true;
        return false;
    }

    /**
     * Gets the absoluteOrFixed attribute of the ContentUtil class
     *
     * @param style PARAM
     * @return The absoluteOrFixed value
     */
    public static boolean isAbsoluteOrFixed(CalculatedStyle style) {
        IdentValue position = style.getIdent(CSSName.POSITION);
        return position != null && (position == IdentValue.ABSOLUTE || position == IdentValue.FIXED);
    }

    /**
     * Gets the inlineBlock attribute of the ContentUtil class
     *
     * @param style PARAM
     * @return The inlineBlock value
     */
    public static boolean isInlineBlock(CalculatedStyle style) {
        IdentValue display = style.getIdent(CSSName.DISPLAY);
        return display != null && display == IdentValue.INLINE_BLOCK;
    }

    /**
     * Gets the floated attribute of the ContentUtil class
     *
     * @param style PARAM
     * @return The floated value
     */
    public static boolean isFloated(CalculatedStyle style) {
        if (style == null) {
            return false;
        }
        IdentValue floatVal = style.getIdent(CSSName.FLOAT);
        return floatVal != null && (floatVal == IdentValue.LEFT || floatVal == IdentValue.RIGHT);
    }

    /**
     * Description of the Method
     *
     * @param pendingInlines PARAM
     * @param parentElement  PARAM
     * @param c              PARAM
     * @return Returns
     */
    static List resolveBlockContent(List pendingInlines, Element parentElement, LayoutContext c) {
        //return new LinkedList(pendingInlines);//pendingInlines.clone();

        List inline = new LinkedList();
        List block = new LinkedList();
        for (Iterator i = pendingInlines.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof BlockContent
                    || o instanceof RunInContent
                    || o instanceof TableContent
                    || o instanceof FirstLineStyle
                    || o instanceof FirstLetterStyle) {
                inline = WhitespaceStripper.stripInlineContent(c, inline);
                if (inline.size() != 0) {
                    //Uu.p("resove runin : new anony");
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
            //Uu.p("resove runin : new anony 2");
            //Uu.p("stripped list = " + inline);
            block.add(new AnonymousBlockContent(parentElement, inline));
        }
        return block;
    }

    /**
     * Gets the inline content of a sequence of nodes.
     * This routine will not work for restricted scenarios, like content of a table or table-row
     *
     * @param c      The current context. The current style in the context must
     *               correspond to that of the parent Content
     * @param parent The parent Content to get the child content for
     * @return A list of content.
     */
    static List getChildContentList(LayoutContext c, Content parent) {
        List inlineList = new ArrayList();
        FirstLineStyle firstLineStyle = null;
        FirstLetterStyle firstLetterStyle = null;
        StringBuffer textContent = null;
        CascadedStyle parentStyle = parent.getStyle();
        Element parentElement = parent.getElement();

        if (parentElement != null) {
            c.pushStyle(parentStyle);
            if (mayHaveFirstLine(c.getCurrentStyle())) {
                //put in a marker if there is first-line styling
                CascadedStyle firstLine = c.getCss().getPseudoElementStyle(parentElement, "first-line");
                if (firstLine != null) {
                    firstLineStyle = new FirstLineStyle(firstLine);
                }
            }

            if (mayHaveFirstLetter(c.getCurrentStyle())) {
                //put in a marker if there is first-letter styling
                CascadedStyle firstLetter = c.getCss().getPseudoElementStyle(parentElement, "first-letter");
                if (firstLetter != null) {
                    firstLetterStyle = new FirstLetterStyle(firstLetter);
                }
            }
            c.popStyle();

            //TODO: before and after may be block!
            //<br/> handling should be done by :before content
            CascadedStyle before = c.getCss().getPseudoElementStyle(parentElement, "before");
            if (before != null && before.hasProperty(CSSName.CONTENT)) {
                String content = ((CSSPrimitiveValue) before.propertyByName(CSSName.CONTENT).getValue()).getStringValue();
                // FIXME Don't think this test is right. Even empty inline content
                // should force a line box to be created.  Leave for now though.
                if (!content.equals("")) {
                    inlineList.add(new StylePush("before", parentElement));
                    c.pushStyle(before);
                    textContent = new StringBuffer();
                    textContent.append(content.replaceAll("\\\\A", "\n"));
                    ContentFunction contentFunction = null;
                    if (Idents.looksLikeAFunction(content)) {
                        contentFunction = c.getContentFunctionFactory().lookupFunction(
                                c, textContent.toString());
                        if (contentFunction !=  null && contentFunction.isStatic()) {
                            String value = contentFunction.calculate(c, textContent.toString());
                            textContent = new StringBuffer(value);
                            contentFunction = null;
                        }
                    }
                    inlineList.add(new TextContent("before", parentElement, 
                            textContent.toString(), contentFunction));
                    textContent = null;
                    c.popStyle();
                    inlineList.add(new StylePop("before", parentElement));
                }
                //do not reset style here, because if this element is empty, we will not have changed context
            }
        }

        Iterator i = getNodeIterator(parent);
        TableContent anonymousTable = null;
        //each child node can result in only one addition to content
        while (i.hasNext()) {
            Node curr = (Node) i.next();
            if (curr.getNodeType() != Node.ELEMENT_NODE && curr.getNodeType() != Node.TEXT_NODE) {
                continue;
            }//must be a comment or pi or something

            if (curr.getNodeType() == Node.TEXT_NODE) {
                String text = curr.getNodeValue();
                if (textContent == null) {
                    textContent = new StringBuffer();
                }
                textContent.append(text);
                //if we reach here, our anonymous table is done
                anonymousTable = null;
                continue;
            }

            Element elem = (Element) curr;
            CascadedStyle style = c.getCss().getCascadedStyle(elem, true);//this is the place where restyle is done for layout (boxing)
            c.pushStyle(style);//just remember to pop it before continue
            CalculatedStyle currentStyle = c.getCurrentStyle();

            if (isTableDescendant(currentStyle)) {
                if (anonymousTable == null) {
                    anonymousTable = new TableContent();
                    inlineList.add(anonymousTable);
                }
                anonymousTable.addChild(elem);
                c.popStyle();
                continue;
            }
            //if we reach here, our anonymous table is done
            anonymousTable = null;

            if (isHidden(currentStyle)) {
                c.popStyle();
                continue;//at least for now, don't generate hidden content
            }

            if (isAbsoluteOrFixed(currentStyle)) {
                // Uu.p("adding replaced: " + curr);
                textContent = saveTextContent(textContent, inlineList, parentElement, parent);
                inlineList.add(new AbsolutelyPositionedContent((Element) curr, style));
                c.popStyle();
                continue;
            }

            //have to check for float here already. The element may still be replaced, though
            if (isFloated(currentStyle)) {
                // Uu.p("adding floated block: " + curr);
                textContent = saveTextContent(textContent, inlineList, parentElement, parent);
                inlineList.add(new FloatedBlockContent((Element) curr, style));
                c.popStyle();
                continue;
            }

            if (isInlineBlock(currentStyle)) {
                //treat it like a replaced element
                textContent = saveTextContent(textContent, inlineList, parentElement, parent);
                inlineList.add(new InlineBlockContent(elem, style));
                c.popStyle();
                continue;
            }

            if (isRunIn(currentStyle)) {
                RunInContent runIn = new RunInContent(elem, style);
                textContent = saveTextContent(textContent, inlineList, parentElement, parent);
                inlineList.add(runIn);//resolve it when we can
                c.popStyle();
                continue;
            }

            if (isTable(currentStyle)) {
                textContent = saveTextContent(textContent, inlineList, parentElement, parent);
                TableContent table = new TableContent(elem, style);
                inlineList.add(table);
                c.popStyle();
                continue;
            }

            //TODO:list-items, anonymous tables, inline tables, etc.

            if (isBlockLevel(currentStyle)) {
                textContent = saveTextContent(textContent, inlineList, parentElement, parent);
                BlockContent block = new BlockContent(elem, style);
                inlineList.add(block);
                c.popStyle();
                continue;
            }

            //if we get here, we have inline element content, need to get into it.
            textContent = saveTextContent(textContent, inlineList, parentElement, parent);
            Content inline = new InlineContent(elem, style);
            List childList = inline.getChildContent(c);
            inlineList.add(new StylePush(null, elem));//this is already pushed to context
            //the child list represents the entire contents of an element,
            //therefore we need not concern ourselves with style-changes, as they will even out
            for (Iterator ci = childList.iterator(); ci.hasNext();) {
                Object o = ci.next();
                if (o instanceof AnonymousBlockContent) {
                    inlineList.addAll(((AnonymousBlockContent) o).getChildContent(c));
                } else {
                    inlineList.add(o);
                }
            }
            inlineList.add(new StylePop(null, elem));//pop from c below
            c.popStyle();
        }

        textContent = saveTextContent(textContent, inlineList, parentElement, parent);
        if (parentElement != null) {
            //TODO: after may be block!
            CascadedStyle after = c.getCss().getPseudoElementStyle(parentElement, "after");
            if (after != null && after.hasProperty(CSSName.CONTENT)) {
                String content = ((CSSPrimitiveValue) after.propertyByName(CSSName.CONTENT).getValue()).getStringValue();
                // FIXME Don't think this test is right. Even empty inline content
                // should force a line box to be created.  Leave for now though.
                if (!content.equals("")) {//a worthwhile reduncancy-check
                    textContent = saveTextContent(textContent, inlineList, parentElement, parent);
                    inlineList.add(new StylePush("after", parentElement));
                    textContent = new StringBuffer();
                    textContent.append(content.replaceAll("\\\\A", "\n"));
                    ContentFunction contentFunction = null;
                    if (Idents.looksLikeAFunction(content)) {
                        contentFunction = c.getContentFunctionFactory().lookupFunction(
                                c, textContent.toString());
                        if (contentFunction !=  null && contentFunction.isStatic()) {
                            String value = contentFunction.calculate(c, textContent.toString());
                            textContent = new StringBuffer(value);
                            contentFunction = null;
                        }
                    }
                    inlineList.add(new TextContent("after", parentElement, 
                            textContent.toString(), contentFunction));
                    textContent = null;
                    inlineList.add(new StylePop("after", parentElement));
                }
            }
        }

        List blockList = null;
        if (firstLetterStyle != null) {
            inlineList.add(0, firstLetterStyle);
        }
        if (firstLineStyle != null) {
            inlineList.add(0, firstLineStyle);
        }

        if (hasBlockContent(inlineList)) {
            blockList = new LinkedList();
            blockList.addAll(resolveBlockContent(inlineList, parentElement, c));
            return blockList;
        } else {
            inlineList = WhitespaceStripper.stripInlineContent(c, inlineList);
            return inlineList;
        }

    }

    private static Iterator getNodeIterator(Content parent) {
        final Node node;
        if (parent instanceof TableCellContent) {
            if (((TableCellContent) parent).getChildIterator() != null) return ((TableCellContent) parent).getChildIterator();
            node = parent.getElement();
        } else if (parent instanceof DomToplevelNode) {
            node = ((DomToplevelNode) parent).getNode();
        } else {
            node = parent.getElement();
        }
        return new Iterator() {
            NodeList nl = node.getChildNodes();
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

    private static StringBuffer saveTextContent(StringBuffer textContent, List inlineList, Element parentElement, Content parent) {
        if (textContent != null) {
            boolean isAnonymousInlineBox = !(parent instanceof InlineContent);
            if (isAnonymousInlineBox) inlineList.add(new StylePush(null, null));
            inlineList.add(new TextContent(parentElement, textContent.toString()));
            if (isAnonymousInlineBox) inlineList.add(new StylePop(null, null));
            textContent = null;
        }
        return textContent;
    }

    
    public static boolean isNotInFlow(Object current) {
        if (current instanceof AnonymousBlockContent) {
            AnonymousBlockContent anon = (AnonymousBlockContent)current;
            for (Iterator i = anon.getChildContent(null).iterator(); i.hasNext(); ) {
                Object content = (Object)i.next();
                if ( ! (content instanceof FloatedBlockContent || 
                            content instanceof AbsolutelyPositionedContent)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.48  2006/04/02 22:22:32  peterbrant
 * Add function interface for generated content / Implement page counters in terms of this, removing previous hack / Add custom page numbering functions
 *
 * Revision 1.47  2006/02/03 23:57:54  peterbrant
 * Implement counter(page) and counter(pages) / Bug fixes to alignment calculation
 *
 * Revision 1.46  2005/12/22 23:18:42  peterbrant
 * Add comment
 *
 * Revision 1.45  2005/12/05 00:07:54  peterbrant
 * Remove unused method
 *
 * Revision 1.44  2005/11/09 18:41:26  peterbrant
 * Fixes to vertical margin collapsing in the presence of floats / Paint floats as
 * layers
 *
 * Revision 1.43  2005/10/30 00:02:35  peterbrant
 * - Minor cleanup to get rid of unused CssContext in Style constructor
 * - Switch to ArrayList from LinkedList in a few places (saves several MBs of memory on Hamlet)
 * - Introduce ScaledLineMetrics to work around apparent Java bug
 *
 * Revision 1.42  2005/10/27 00:08:55  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.41  2005/10/15 23:39:16  tobega
 * patch from Peter Brant
 *
 * Revision 1.40  2005/09/26 22:40:18  tobega
 * Applied patch from Peter Brant concerning margin collapsing
 *
 * Revision 1.39  2005/06/16 07:24:49  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.38  2005/06/04 21:17:19  tobega
 * Created a content model for tables
 *
 * Revision 1.37  2005/06/04 16:55:43  tobega
 * Fixed bug concerning first-line style and Anonymous block boxes
 *
 * Revision 1.36  2005/05/17 06:56:23  tobega
 * Inline backgrounds now work correctly, as does mixing of inlines and blocks for style inheritance
 *
 * Revision 1.35  2005/05/16 13:48:59  tobega
 * Fixe inline border mismatch and started on styling problem in switching between blocks and inlines
 *
 * Revision 1.34  2005/05/08 13:02:38  tobega
 * Fixed a bug whereby styles could get lost for inline elements, notably if root element was inline. Did a few other things which probably has no importance at this moment, e.g. refactored out some unused stuff.
 *
 * Revision 1.33  2005/01/29 20:22:15  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.32  2005/01/25 14:45:55  pdoubleya
 * Added support for IdentValue mapping on property declarations. On both CascadedStyle and PropertyDeclaration you can now request the value as an IdentValue, for object-object comparisons. Updated 99% of references that used to get the string value of PD to return the IdentValue instead; remaining cases are for pseudo-elements where the PD content needs to be manipulated as a String.
 *
 * Revision 1.31  2005/01/25 12:38:11  pdoubleya
 * ASK comment.
 *
 * Revision 1.30  2005/01/16 18:50:04  tobega
 * Re-introduced caching of styles, which make hamlet and alice scroll nicely again. Background painting still slow though.
 *
 * Revision 1.29  2005/01/09 00:29:27  tobega
 * Removed XPath usages from core classes. Also happened to find and fix a layout-bug that I introduced a while ago.
 *
 * Revision 1.28  2005/01/07 00:29:27  tobega
 * Removed Content reference from Box (mainly to reduce memory footprint). In the process stumbled over and cleaned up some messy stuff.
 *
 * Revision 1.27  2005/01/02 12:22:15  tobega
 * Cleaned out old layout code
 *
 * Revision 1.26  2005/01/02 01:00:08  tobega
 * Started sketching in code for handling replaced elements in the NamespaceHandler
 *
 * Revision 1.25  2005/01/01 22:37:43  tobega
 * Started adding in the table support.
 *
 * Revision 1.24  2004/12/29 10:39:30  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.23  2004/12/29 07:35:37  tobega
 * Prepared for cloned Context instances by encapsulating fields
 *
 * Revision 1.22  2004/12/28 01:48:23  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
 * Revision 1.21  2004/12/20 23:25:30  tobega
 * Cleaned up handling of absolute boxes and went back to correct use of anonymous boxes in ContentUtil
 *
 * Revision 1.20  2004/12/16 17:41:46  joshy
 * fixed floats.  it was looking for the display property instead of the float
 * property
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.19  2004/12/16 17:33:15  joshy
 * moved back to abs pos content
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.18  2004/12/16 15:53:09  joshy
 * fixes for absolute layout
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
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

