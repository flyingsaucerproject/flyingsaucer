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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.inline.WhitespaceStripper;


/**
 * Generates lists of Content. This is the only place where it should be
 * determined about the type of content an element is. The layout and rendering
 * should only work with Boxes and Content
 *
 * @author   Joshua Marinacci
 * @author   Torbjörn Gannholm
 */
public class ContentUtil {

    //TODO: following methods should not need to be public
    /**
     * Description of the Method
     *
     * @param style  PARAM
     * @return       Returns
     */
    public static boolean mayHaveFirstLetter( CascadedStyle style ) {
        // ASK: why use CascadedStyle here, instead of Calculated? (PWW 25-01-05)
        if ( style == null ) {
            return false;
        }//for DomToplevelNode
        IdentValue display = style.getIdent( CSSName.DISPLAY );
        return display != null &&
                ( display == IdentValue.BLOCK ||
                display == IdentValue.LIST_ITEM ||
                display == IdentValue.TABLE_CELL ||
                display == IdentValue.TABLE_CAPTION ||
                display == IdentValue.INLINE_BLOCK );
    }

    /**
     * Description of the Method
     *
     * @param style  PARAM
     * @return       Returns
     */
    public static boolean mayHaveFirstLine( CascadedStyle style ) {
        // ASK: why use CascadedStyle here, instead of Calculated? (PWW 25-01-05)
        if ( style == null ) {
            return false;
        }//for DomToplevelNode
        IdentValue display = style.getIdent( CSSName.DISPLAY );
        return display != null &&
                ( display == IdentValue.BLOCK ||
                display == IdentValue.LIST_ITEM ||
                display == IdentValue.RUN_IN ||
                display == IdentValue.TABLE ||
                display == IdentValue.TABLE_CELL ||
                display == IdentValue.TABLE_CAPTION ||
                display == IdentValue.INLINE_BLOCK );
    }

    /**
     * Description of the Method
     *
     * @param childContent  PARAM
     * @return              Returns
     */
    public static boolean hasBlockContent( List childContent ) {
        for ( Iterator i = childContent.iterator(); i.hasNext();  ) {
            Object o = i.next();
            if ( o instanceof TableContent ) {
                return true;
            }
            if ( o instanceof BlockContent ) {
                return true;
            }
            if ( o instanceof AnonymousBlockContent ) {
                return true;
            }
            if ( o instanceof RunInContent ) {
                return true;
            }//if it has run-ins, it will be block, one way or another
        }
        return false;
    }

    /**
     * Gets the blockLevel attribute of the ContentUtil class
     *
     * @param style  PARAM
     * @return       The blockLevel value
     */
    public static boolean isBlockLevel( CascadedStyle style ) {
        if ( style == null ) {
            return false;
        }
        IdentValue display = style.getIdent( CSSName.DISPLAY );
        return display != null && ( display == IdentValue.BLOCK || display == IdentValue.LIST_ITEM || display == IdentValue.TABLE );
    }

    /**
     * Gets the hidden attribute of the ContentUtil class
     *
     * @param style  PARAM
     * @return       The hidden value
     */
    public static boolean isHidden( CascadedStyle style ) {
        IdentValue display = style.getIdent( CSSName.DISPLAY );
        return display != null && display == IdentValue.NONE;
    }

    /**
     * Gets the runIn attribute of the ContentUtil class
     *
     * @param style  PARAM
     * @return       The runIn value
     */
    public static boolean isRunIn( CascadedStyle style ) {
        IdentValue display = style.getIdent( CSSName.DISPLAY );
        return display != null && display == IdentValue.RUN_IN;
    }

    /**
     * Gets the table attribute of the ContentUtil class
     *
     * @param style  PARAM
     * @return       The table value
     */
    public static boolean isTable( CascadedStyle style ) {
        IdentValue display = style.getIdent( CSSName.DISPLAY );
        return display != null && display == IdentValue.TABLE;
    }

    /**
     * Gets the listItem attribute of the ContentUtil class
     *
     * @param style  PARAM
     * @return       The listItem value
     */
    public static boolean isListItem( CascadedStyle style ) {
        if ( style == null ) {
            return false;
        }
        IdentValue display = style.getIdent( CSSName.DISPLAY );
        return display != null && display == IdentValue.LIST_ITEM;
    }

    /**
     * Gets the absoluteOrFixed attribute of the ContentUtil class
     *
     * @param style  PARAM
     * @return       The absoluteOrFixed value
     */
    public static boolean isAbsoluteOrFixed( CascadedStyle style ) {
        IdentValue position = style.getIdent( CSSName.POSITION );
        return position != null && ( position == IdentValue.ABSOLUTE || position == IdentValue.FIXED );
    }

    /**
     * Gets the inlineBlock attribute of the ContentUtil class
     *
     * @param style  PARAM
     * @return       The inlineBlock value
     */
    public static boolean isInlineBlock( CascadedStyle style ) {
        IdentValue display = style.getIdent( CSSName.DISPLAY );
        return display != null && display == IdentValue.INLINE_BLOCK;
    }

    /**
     * Gets the floated attribute of the ContentUtil class
     *
     * @param style  PARAM
     * @return       The floated value
     */
    public static boolean isFloated( CascadedStyle style ) {
        if ( style == null ) {
            return false;
        }
        IdentValue floatVal = style.getIdent( CSSName.FLOAT );
        return floatVal != null && ( floatVal == IdentValue.LEFT || floatVal == IdentValue.RIGHT );
    }

    /**
     * Gets the blockContent attribute of the ContentUtil class
     *
     * @param childContent  PARAM
     * @return              The blockContent value
     */
    public static boolean isBlockContent( List childContent ) {
        // Uu.p("checking block content: " + childContent);
        if ( childContent.size() == 0 ) {
            return false;
        }
        Object o = childContent.get( childContent.size() - 1 );
        if ( o instanceof TableContent ) {
            return true;
        }
        if ( o instanceof BlockContent ) {
            return true;
        }
        if ( o instanceof AnonymousBlockContent ) {
            return true;
        }
        if ( o instanceof RunInContent ) {
            return true;
        }//if it has run-ins, it will be block, one way or another
        return false;
    }

    /**
     * Description of the Method
     *
     * @param pendingInlines  PARAM
     * @param parentElement   PARAM
     * @param c               PARAM
     * @return                Returns
     */
    static List resolveBlockContent( List pendingInlines, Element parentElement, Context c ) {
        //return new LinkedList(pendingInlines);//pendingInlines.clone();

        List inline = new LinkedList();
        List block = new LinkedList();
        for ( Iterator i = pendingInlines.iterator(); i.hasNext();  ) {
            Object o = i.next();
            if ( o instanceof BlockContent || o instanceof RunInContent || o instanceof TableContent ) {
                inline = WhitespaceStripper.stripInlineContent( c, inline );
                if ( inline.size() != 0 ) {
                    //Uu.p("resove runin : new anony");
                    block.add( new AnonymousBlockContent( parentElement, inline ) );
                    inline = new LinkedList();
                }
                block.add( o );
            } else {
                inline.add( o );
            }
        }
        inline = WhitespaceStripper.stripInlineContent( c, inline );
        if ( inline.size() != 0 ) {
            //Uu.p("resove runin : new anony 2");
            //Uu.p("stripped list = " + inline);
            block.add( new AnonymousBlockContent( parentElement, inline ) );
        }
        return block;
    }

    /**
     * Gets the inline content of a sequence of nodes
     *
     * @param c       The current context. The current style in the context must
     *      correspond to that of the parent Content
     * @param parent  The parent Content to get the child content for
     * @return        A list of content.
     */
    static List getChildContentList( Context c, Content parent ) {
        List inlineList = new LinkedList();
        FirstLineStyle firstLineStyle = null;
        FirstLetterStyle firstLetterStyle = null;
        StringBuffer textContent = null;
        CascadedStyle parentStyle = parent.getStyle();
        Element parentElement = parent.getElement();

        if ( parentElement != null ) {
            if ( mayHaveFirstLine( parentStyle ) ) {
                //put in a marker if there is first-line styling
                CascadedStyle firstLine = c.getCss().getPseudoElementStyle( parentElement, "first-line" );
                if ( firstLine != null ) {
                    firstLineStyle = new FirstLineStyle( firstLine );
                }
            }

            if ( mayHaveFirstLetter( parentStyle ) ) {
                //put in a marker if there is first-letter styling
                CascadedStyle firstLetter = c.getCss().getPseudoElementStyle( parentElement, "first-letter" );
                if ( firstLetter != null ) {
                    firstLetterStyle = new FirstLetterStyle( firstLetter );
                }
            }

            //TODO: before and after may be block!
            //<br/> handling should be done by :before content
            CascadedStyle before = c.getCss().getPseudoElementStyle( parentElement, "before" );
            if ( before != null && before.hasProperty( CSSName.CONTENT ) ) {
                String content = ( (CSSPrimitiveValue)before.propertyByName( CSSName.CONTENT ).getValue() ).getStringValue();
                if ( !content.equals( "" ) ) {
                    inlineList.add( new StylePush( "before", parentElement ) );
                    c.pushStyle( before );
                    textContent = new StringBuffer();
                    textContent.append( content.replaceAll( "\\\\A", "\n" ) );
                    inlineList.add( new TextContent( "before", parentElement, textContent.toString() ) );
                    textContent = null;
                    c.popStyle();
                    inlineList.add( new StylePop( "before", parentElement ) );
                }
                //do not reset style here, because if this element is empty, we will not have changed context
            }
        }

        Node node = parentElement;
        if ( node == null ) {
            node = ( (DomToplevelNode)parent ).getNode();
        }
        NodeList children = node.getChildNodes();
        //each child node can result in only one addition to content
        for ( int i = 0; i < children.getLength(); i++ ) {
            Node curr = children.item( i );
            if ( curr.getNodeType() != Node.ELEMENT_NODE && curr.getNodeType() != Node.TEXT_NODE ) {
                continue;
            }//must be a comment or pi or something

            if ( curr.getNodeType() == Node.TEXT_NODE ) {
                String text = curr.getNodeValue();
                if ( textContent == null ) {
                    textContent = new StringBuffer();
                }
                textContent.append( text );
                continue;
            }

            Element elem = (Element)curr;
            CascadedStyle style = c.getCss().getCascadedStyle( elem, true );//this is the place where restyle is done for layout (boxing)
            c.pushStyle( style );//just remember to pop it before continue

            if ( isHidden( style ) ) {
                c.popStyle();
                continue;//at least for now, don't generate hidden content
            }

            if ( isAbsoluteOrFixed( style ) ) {
                // Uu.p("adding replaced: " + curr);
                if ( textContent != null ) {
                    inlineList.add( new TextContent( parentElement, textContent.toString() ) );
                    textContent = null;
                }
                inlineList.add( new AbsolutelyPositionedContent( (Element)curr, style ) );
                c.popStyle();
                continue;
            }

            //have to check for float here already. The element may still be replaced, though
            if ( isFloated( style ) ) {
                // Uu.p("adding floated block: " + curr);
                if ( textContent != null ) {
                    inlineList.add( new TextContent( parentElement, textContent.toString() ) );
                    textContent = null;
                }
                inlineList.add( new FloatedBlockContent( (Element)curr, style ) );
                c.popStyle();
                continue;
            }

            if ( isInlineBlock( style ) ) {
                //treat it like a replaced element
                if ( textContent != null ) {
                    inlineList.add( new TextContent( parentElement, textContent.toString() ) );
                    textContent = null;
                }
                inlineList.add( new InlineBlockContent( elem, style ) );
                c.popStyle();
                continue;
            }

            if ( isRunIn( style ) ) {
                RunInContent runIn = new RunInContent( elem, style );
                if ( textContent != null ) {
                    inlineList.add( new TextContent( parentElement, textContent.toString() ) );
                    textContent = null;
                }
                inlineList.add( runIn );//resolve it when we can
                c.popStyle();
                continue;
            }

            if ( isTable( style ) ) {
                if ( textContent != null ) {
                    inlineList.add( new TextContent( parentElement, textContent.toString() ) );
                    textContent = null;
                }
                TableContent table = new TableContent( elem, style );
                inlineList.add( table );
                c.popStyle();
                continue;
            }

            //TODO:list-items, anonymous tables, inline tables, etc.

            if ( isBlockLevel( style ) ) {
                if ( textContent != null ) {
                    inlineList.add( new TextContent( parentElement, textContent.toString() ) );
                    textContent = null;
                }
                BlockContent block = new BlockContent( elem, style );
                inlineList.add( block );
                c.popStyle();
                continue;
            }

            //if we get here, we have inline content, need to get into it.
            if ( textContent != null ) {
                inlineList.add( new TextContent( parentElement, textContent.toString() ) );
                textContent = null;
            }
            Content inline = new InlineContent( elem, style );
            List childList = inline.getChildContent( c );
            inlineList.add( new StylePush( null, elem ) );//this is already pushed to context
            //the child list represents the entire contents of an element,
            //therefore we need not concern ourselves with style-changes, as they will even out
            for ( Iterator ci = childList.iterator(); ci.hasNext();  ) {
                Object o = ci.next();
                if ( o instanceof AnonymousBlockContent ) {
                    inlineList.addAll( ( (AnonymousBlockContent)o ).getChildContent( c ) );
                } else {
                    inlineList.add( o );
                }
            }
            inlineList.add( new StylePop( null, elem ) );//pop from c below
            c.popStyle();
        }

        if ( textContent != null ) {
            inlineList.add( new TextContent( parentElement, textContent.toString() ) );
            textContent = null;
        }
        if ( parentElement != null ) {
            //TODO: after may be block!
            CascadedStyle after = c.getCss().getPseudoElementStyle( parentElement, "after" );
            if ( after != null && after.hasProperty( CSSName.CONTENT ) ) {
                String content = ( (CSSPrimitiveValue)after.propertyByName( CSSName.CONTENT ).getValue() ).getStringValue();
                if ( !content.equals( "" ) ) {//a worthwhile reduncancy-check
                    if ( textContent != null ) {
                        inlineList.add( new TextContent( parentElement, textContent.toString() ) );
                        textContent = null;
                    }
                    inlineList.add( new StylePush( "after", parentElement ) );
                    textContent = new StringBuffer();
                    textContent.append( content.replaceAll( "\\\\A", "\n" ) );
                    inlineList.add( new TextContent( parentElement, textContent.toString() ) );
                    textContent = null;
                    inlineList.add( new StylePop( "after", parentElement ) );
                }
            }
        }

        List blockList = null;
        if ( firstLetterStyle != null ) {
            inlineList.add( 0, firstLetterStyle );
        }
        if ( firstLineStyle != null ) {
            inlineList.add( 0, firstLineStyle );
        }

        if ( hasBlockContent( inlineList ) ) {
            blockList = new LinkedList();
            blockList.addAll( resolveBlockContent( inlineList, parentElement, c ) );
            return blockList;
        } else {
            inlineList = WhitespaceStripper.stripInlineContent( c, inlineList );
            return inlineList;
        }

    }

}

/*
 * $Id$
 *
 * $Log$
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
 * Separated current state Context into ContextImpl and the rest into SharedContext.
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

