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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.extend.ContentFunction;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.render.AnonymousBlockBox;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FloatedBlockBox;
import org.xhtmlrenderer.render.InlineBox;

public class BoxBuilder {
    private static final Pattern CONTENT_NEWLINE = Pattern.compile("\\\\A");
    
    public static BlockBox createRootBox(LayoutContext c, Document document) {
        // XXX assume block box for now
        Element root = document.getDocumentElement();
        
        BlockBox result = new BlockBox();
        result.element = root;
        result.setStyle(c.getSharedContext().getStyle(root));
        
        return result;
    }
    
    public static void createChildren(LayoutContext c, BlockBox box) {
        List children = new ArrayList();
        
        createChildren(c, box.element, children);
        
        if (children.size() > 0) {
            if (containsBlockContent(children)) {
                insertAnonymousBlocks(c.getSharedContext(), box, children);
                box.setChildrenContentType(BlockBox.CONTENT_BLOCK);
            } else {
                WhitespaceStripper2.stripInlineContent(children);
                if (children.size() > 0) {
                    box.setInlineContent(children);
                    box.setChildrenContentType(BlockBox.CONTENT_INLINE);
                } else {
                    box.setChildrenContentType(BlockBox.CONTENT_EMPTY);
                }
            }
        } else {
            box.setChildrenContentType(BlockBox.CONTENT_EMPTY);
        }
    }
    
    private static boolean containsBlockContent(List children) {
        for (Iterator i = children.iterator(); i.hasNext(); ) {
            Styleable node = (Styleable)i.next();
            if (! node.getStyle().isLayedOutInInlineContext()) {
                return true;
            }
        }
        
        return false;
    }
    
    private static void insertGeneratedContent(
            LayoutContext c, Element element, CalculatedStyle parentStyle, String peName, List children) {
        CascadedStyle before = c.getCss().getPseudoElementStyle(element, peName);
        if (before != null && before.hasProperty(CSSName.CONTENT)) {
            String content = ((CSSPrimitiveValue) before.propertyByName(CSSName.CONTENT).getValue()).getStringValue();
            // FIXME Don't think this test is right. Even empty inline content
            // should force a line box to be created.  Leave for now though.
            // TODO: need to handle hex values in CSS--\2192 is the Unicode for an arrow (HTML &8594;), though this
            // is a general string problem, anywhere strings can appear in CSS, not just content
            if (! content.equals("")) {
                CalculatedStyle calculatedStyle = parentStyle.deriveStyle(before);
                children.add(createGeneratedContent(c, element, calculatedStyle, content));
            }
        }
    }
    
    private static Styleable createGeneratedContent(
            LayoutContext c, Element element, CalculatedStyle calculatedStyle, String raw) {
        ContentFunction contentFunction = null;
        String content = CONTENT_NEWLINE.matcher(raw).replaceAll("\n");
        if (Idents.looksLikeAFunction(raw)) {
            contentFunction = c.getContentFunctionFactory().lookupFunction(c, raw);
            if (contentFunction != null && contentFunction.isStatic()) {
                content = contentFunction.calculate(c, raw); 
                contentFunction = null;
            }
        } else if (Idents.looksLikeAQuote(raw)) {
            // TODO: if the content is one of the quote idents, then look up the value of the quotes property
            content = "\"";
        } else if (Idents.looksLikeASkipQuote(content)) {
            // TODO: no content, but increment nesting level for quotes
        }
        
        InlineBox iB = new InlineBox(content);
        iB.setContentFunction(contentFunction);
        iB.setElement(element);
        iB.setStartsHere(true);
        iB.setEndsHere(true);
        
        if (calculatedStyle.isInline()) {
            iB.setStyle(calculatedStyle);
            
            return iB;
        } else {
            iB.setStyle(calculatedStyle.createAnonymousStyle());
            
            BlockBox result = createBlockBox(calculatedStyle);
            result.setStyle(calculatedStyle);
            result.setInlineContent(Collections.singletonList(iB));
            result.setElement(element);
            result.setChildrenContentType(BlockBox.CONTENT_INLINE);
            
            return result;
        }
    }
    
    private static BlockBox createBlockBox(CalculatedStyle style) {
        if (style.isFloated() && ! (style.isAbsolute() || style.isFixed())) {
            return new FloatedBlockBox();
        } else {
            return new BlockBox();
        }
    }
    
    private static void createChildren(LayoutContext c, Element parent, List children) {
        SharedContext sharedContext = c.getSharedContext();
        
        CalculatedStyle parentStyle = sharedContext.getStyle(parent);
        insertGeneratedContent(c, parent, parentStyle, "before", children);
        
        /*
        if (parentStyle.mayHaveFirstLine()) {
            CascadedStyle firstLine = c.getCss().getPseudoElementStyle(parent, "first-line");
        }

        if (parentStyle.mayHaveFirstLetter()) {
            CascadedStyle firstLetter = c.getCss().getPseudoElementStyle(parent, "first-letter");
        }
        */
        
        Node working = parent.getFirstChild();
        if (working != null) {
            InlineBox previousIB = null;
            do {
                Styleable child = null;
                if (working.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element)working;
                    CalculatedStyle style = sharedContext.getStyle(element);
                    
                    if (style.isHidden()) {
                        continue;
                    }
                
                    if (style.isInline()) {
                        createChildren(c, element, children);
                    } else {
                        child = createBlockBox(style);
                    }
                    
                    if (child != null) {
                        child.setStyle(style);
                        child.setElement(element);
                    }
                }
                else if (working.getNodeType() == Node.TEXT_NODE) {
                    Text textNode = (Text)working;
                    StringBuffer text = new StringBuffer(textNode.getData());
                    
                    Node maybeText = textNode;
                    while (true) {
                        maybeText = textNode.getNextSibling();
                        if (maybeText != null && maybeText.getNodeType() == Node.TEXT_NODE) {
                            textNode = (Text)maybeText;
                            text.append(textNode.getData());
                        } else {
                            break;
                        }
                    }
                    
                    working = textNode;
                    
                    child = new InlineBox(text.toString());
                    
                    InlineBox iB = (InlineBox)child;
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
                        iB.setStyle(parentStyle.createAnonymousStyle());
                    }
                } 
                
                if (child != null) {
                    children.add(child);
                }
            } while ( (working = working.getNextSibling()) != null);
        }
        insertGeneratedContent(c, parent, parentStyle, "after", children);
    }
    
    private static void insertAnonymousBlocks(SharedContext c, Box parent, List children) {
        List inline = new ArrayList();
        
        LinkedList parents = new LinkedList();
        List savedParents = null;
        
        for (Iterator i = children.iterator(); i.hasNext();) {
            Styleable child = (Styleable)i.next();
            if (child.getStyle().isLayedOutInInlineContext()) {
                inline.add(child);
                
                if (child.getStyle().isInline()) {
                    InlineBox iB = (InlineBox)child;
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
                parent.addChild((Box)child);
            }
        }
        
        createAnonymousBlock(c, parent, inline, savedParents);
    }

    private static void createAnonymousBlock(
            SharedContext c, Box parent, List inline, List savedParents) {
        WhitespaceStripper2.stripInlineContent(inline);
        if (inline.size() > 0) {
            AnonymousBlockBox anon = new AnonymousBlockBox(parent.element);
            anon.setStyle(c.getStyle(parent.element).createAnonymousStyle());
            if (savedParents != null && savedParents.size() > 0) {
                anon.setOpenParents(savedParents);
            }
            parent.addChild(anon);
            anon.setChildrenContentType(BlockBox.CONTENT_INLINE);
            anon.setInlineContent(inline);
        }
    }
}
