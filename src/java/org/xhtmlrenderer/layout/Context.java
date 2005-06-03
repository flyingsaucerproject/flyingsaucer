/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjšrn Gannholm
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

import org.xhtmlrenderer.css.FontResolver;
import org.xhtmlrenderer.css.StyleReference;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.swing.BasicPanel;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.LinkedList;


/**
 * Repository for information about the current layout context
 *
 * @author Torbjörn Gannholm
 */
public interface Context {
    /**
     * Gets the media attribute of the Context object
     *
     * @return The media value
     */
    String getMedia();

    /**
     * Sets the graphics attribute of the Context object
     *
     * @param graphics The new graphics value
     */
    void setGraphics(Graphics2D graphics);

    /**
     * Gets the renderingContext attribute of the Context object
     *
     * @return The renderingContext value
     */
    RenderingContext getRenderingContext();

    /**
     * Gets the textRenderer attribute of the Context object
     *
     * @return The textRenderer value
     */
    TextRenderer getTextRenderer();

    /**
     * Description of the Method
     *
     * @param c PARAM
     */
    void initializeStyles(EmptyStyle c);

    /**
     * Description of the Method
     *
     * @param s PARAM
     */
    void pushStyle(CascadedStyle s);

    /**
     * Description of the Method
     */
    void popStyle();

    /**
     * Gets the currentStyle attribute of the Context object
     *
     * @return The currentStyle value
     */
    CalculatedStyle getCurrentStyle();

    boolean isStylesAllPopped();

    /**
     * Description of the Method
     *
     * @return Returns
     */
    boolean debugDrawBoxes();

    /**
     * Description of the Method
     *
     * @return Returns
     */
    boolean debugDrawLineBoxes();

    /**
     * Description of the Method
     *
     * @return Returns
     */
    boolean debugDrawInlineBoxes();

    /**
     * Description of the Method
     *
     * @return Returns
     */
    boolean debugDrawFontMetrics();

    /**
     * Description of the Method
     *
     * @param x PARAM
     * @param y PARAM
     */
    void translate(int x, int y);

    /**
     * Gets the originOffset attribute of the Context object
     *
     * @return The originOffset value
     */
    Point getOriginOffset();

    /**
     * Adds a feature to the MaxWidth attribute of the Context object
     *
     * @param max_width The feature to be added to the MaxWidth attribute
     */
    void addMaxWidth(int max_width);

    /**
     * Description of the Method
     *
     * @param dw how much to shrink width
     * @param dh how much to shrink height
     */
    void shrinkExtents(int dw, int dh);

    /**
     * Description of the Method
     */
    void unshrinkExtents();

    /**
     * Description of the Method
     *
     * @param box PARAM
     */
    void translateInsets(Box box);

    /**
     * Description of the Method
     *
     * @param box PARAM
     */
    void untranslateInsets(Box box);

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    String toString();

    /**
     * Description of the Method
     */
    void clearSelection();

    /**
     * Description of the Method
     *
     * @param box PARAM
     */
    void updateSelection(Box box);

    /**
     * Description of the Method
     *
     * @param box PARAM
     * @return Returns
     */
    boolean inSelection(Box box);

    /**
     * Sets the extents attribute of the Context object
     *
     * @param rect The new extents value
     */
    void setExtents(Rectangle rect);

    /**
     * Sets the selectionStart attribute of the Context object
     *
     * @param box The new selectionStart value
     * @param x   The new selectionStart value
     */
    void setSelectionStart(Box box, int x);

    /**
     * Sets the selectionEnd attribute of the Context object
     *
     * @param box The new selectionEnd value
     * @param x   The new selectionEnd value
     */
    void setSelectionEnd(Box box, int x);

    /**
     * Sets the listCounter attribute of the Context object
     *
     * @param counter The new listCounter value
     */
    void setListCounter(int counter);

    /**
     * Sets the subBlock attribute of the Context object
     *
     * @param sub_block The new subBlock value
     */
    void setSubBlock(boolean sub_block);

    /**
     * Gets the graphics attribute of the Context object
     *
     * @return The graphics value
     */
    Graphics2D getGraphics();

    /**
     * Gets the extents attribute of the Context object
     *
     * @return The extents value
     */
    Rectangle getExtents();

    /**
     * Gets the fontResolver attribute of the Context object
     *
     * @return The fontResolver value
     */
    FontResolver getFontResolver();

    /**
     * Description of the Method
     */
    void flushFonts();

    /**
     * Gets the selectionStart attribute of the Context object
     *
     * @return The selectionStart value
     */
    Box getSelectionStart();

    /**
     * Gets the selectionEnd attribute of the Context object
     *
     * @return The selectionEnd value
     */
    Box getSelectionEnd();

    /**
     * Gets the selectionStartX attribute of the Context object
     *
     * @return The selectionStartX value
     */
    int getSelectionStartX();

    /**
     * Gets the selectionEndX attribute of the Context object
     *
     * @return The selectionEndX value
     */
    int getSelectionEndX();

    /**
     * Gets the listCounter attribute of the Context object
     *
     * @return The listCounter value
     */
    int getListCounter();

    /**
     * Gets the css attribute of the Context object
     *
     * @return The css value
     */
    StyleReference getCss();

    /**
     * Sets the css attribute of the Context object
     *
     * @param css The new css value
     */
    void setCss(StyleReference css);

    /**
     * Sets the debug_draw_boxes attribute of the Context object
     *
     * @param debug_draw_boxes The new debug_draw_boxes value
     */
    void setDebug_draw_boxes(boolean debug_draw_boxes);

    /**
     * Sets the debug_draw_line_boxes attribute of the Context object
     *
     * @param debug_draw_line_boxes The new debug_draw_line_boxes value
     */
    void setDebug_draw_line_boxes(boolean debug_draw_line_boxes);

    /**
     * Sets the debug_draw_inline_boxes attribute of the Context object
     *
     * @param debug_draw_inline_boxes The new debug_draw_inline_boxes value
     */
    void setDebug_draw_inline_boxes(boolean debug_draw_inline_boxes);

    /**
     * Sets the debug_draw_font_metrics attribute of the Context object
     *
     * @param debug_draw_font_metrics The new debug_draw_font_metrics value
     */
    void setDebug_draw_font_metrics(boolean debug_draw_font_metrics);

    /**
     * Gets the canvas attribute of the Context object
     *
     * @return The canvas value
     */
    BasicPanel getCanvas();

    /**
     * Sets the canvas attribute of the Context object
     *
     * @param canvas The new canvas value
     */
    void setCanvas(BasicPanel canvas);

    /**
     * Gets the list_counter attribute of the Context object
     *
     * @return The list_counter value
     */
    int getList_counter();

    /**
     * Sets the list_counter attribute of the Context object
     *
     * @param list_counter The new list_counter value
     */
    void setList_counter(int list_counter);

    /**
     * Gets the ctx attribute of the Context object
     *
     * @return The ctx value
     */
    RenderingContext getCtx();

    /**
     * Sets the ctx attribute of the Context object
     *
     * @param ctx The new ctx value
     */
    void setCtx(RenderingContext ctx);

    /**
     * Gets the blockFormattingContext attribute of the Context object
     *
     * @return The blockFormattingContext value
     */
    BlockFormattingContext getBlockFormattingContext();

    /**
     * Description of the Method
     *
     * @param bfc PARAM
     */
    void pushBFC(BlockFormattingContext bfc);

    /**
     * Description of the Method
     */
    void popBFC();

    /**
     * Gets the subBlock attribute of the Context object
     *
     * @return The subBlock value
     */
    boolean isSubBlock();

    /**
     * Gets the firstLine attribute of the Context object
     *
     * @return The firstLine value
     */
    boolean isFirstLine();

    /**
     * Sets the firstLine attribute of the Context object
     *
     * @param first_line The new firstLine value
     */
    void setFirstLine(boolean first_line);

    /**
     * Gets the fixedRectangle attribute of the Context object
     *
     * @return The fixedRectangle value
     */
    Rectangle getFixedRectangle();

    /**
     * Sets the namespaceHandler attribute of the Context object
     *
     * @param nh The new namespaceHandler value
     */
    public void setNamespaceHandler(NamespaceHandler nh);

    /**
     * Gets the namespaceHandler attribute of the Context object
     *
     * @return The namespaceHandler value
     */
    public NamespaceHandler getNamespaceHandler();

    LinkedList getDecorations();

    LinkedList getInlineBorders();

    void addFirstLineStyle(CascadedStyle firstLineStyle);

    void popFirstLineStyle();

    boolean hasFirstLineStyles();

    void clearFirstLineStyles();

    LinkedList getFirstLineStyles();
}

