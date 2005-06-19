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

import org.xhtmlrenderer.css.StyleReference;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.swing.BasicPanel;

import java.awt.*;
import java.util.LinkedList;


/**
 * Repository for information about the current layout context
 *
 * @author Torbjörn Gannholm
 */
public interface Context {

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
     * Gets the canvas attribute of the Context object
     *
     * @return The canvas value
     */
    BasicPanel getCanvas();

    /**
     * Gets the ctx attribute of the Context object
     *
     * @return The ctx value
     */
    RenderingContext getCtx();

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
     * Gets the fixedRectangle attribute of the Context object
     *
     * @return The fixedRectangle value
     */
    Rectangle getFixedRectangle();

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

    boolean shrinkWrap();

    void setShrinkWrap();

    void unsetShrinkWrap();
	
	public void stopRendering();
	public boolean shouldStop();
}

