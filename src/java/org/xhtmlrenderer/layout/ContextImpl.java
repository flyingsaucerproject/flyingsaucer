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

import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.util.XRLog;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.Stack;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;

public class ContextImpl implements Context {
    SharedContext sharedContext;
    private LinkedList decorations = new LinkedList();
    private LinkedList inlineBorders = new LinkedList();
    private LinkedList firstLineStyles = new LinkedList();
    private boolean shrinkWrap = false;

    public RenderingContext getRenderingContext() {
        return sharedContext.getRenderingContext();
    }

    public TextRenderer getTextRenderer() {
        return sharedContext.getTextRenderer();
    }

    public boolean debugDrawBoxes() {
        return sharedContext.debugDrawBoxes();
    }

    public boolean debugDrawLineBoxes() {
        return sharedContext.debugDrawLineBoxes();
    }

    public boolean debugDrawInlineBoxes() {
        return sharedContext.debugDrawInlineBoxes();
    }

    public boolean debugDrawFontMetrics() {
        return sharedContext.debugDrawFontMetrics();
    }

    public void addMaxWidth(int max_width) {
        sharedContext.addMaxWidth(max_width);
    }

    public void clearSelection() {
        sharedContext.clearSelection();
    }

    public void updateSelection(Box box) {
        sharedContext.updateSelection(box);
    }

    public boolean inSelection(Box box) {
        return sharedContext.inSelection(box);
    }

    public Graphics2D getGraphics() {
        return sharedContext.getGraphics();
    }

    public void flushFonts() {
        sharedContext.flushFonts();
    }

    public Box getSelectionStart() {
        return sharedContext.getSelectionStart();
    }

    public Box getSelectionEnd() {
        return sharedContext.getSelectionEnd();
    }

    public int getSelectionStartX() {
        return sharedContext.getSelectionStartX();
    }

    public int getSelectionEndX() {
        return sharedContext.getSelectionEndX();
    }

    public StyleReference getCss() {
        return sharedContext.getCss();
    }

    public BasicPanel getCanvas() {
        return sharedContext.getCanvas();
    }

    public RenderingContext getCtx() {
        return sharedContext.getCtx();
    }

    public Rectangle getFixedRectangle() {
        return sharedContext.getFixedRectangle();
    }

    public NamespaceHandler getNamespaceHandler() {
        return sharedContext.getNamespaceHandler();
    }

    public LinkedList getDecorations() {
        return decorations;
    }

    public LinkedList getInlineBorders() {
        return inlineBorders;
    }

    public void addFirstLineStyle(CascadedStyle firstLineStyle) {
        firstLineStyles.addLast(firstLineStyle);
    }

    public void popFirstLineStyle() {
        if (firstLineStyles.size() != 0) {//there was no formatted first line
            firstLineStyles.removeLast();
        }
    }

    public boolean hasFirstLineStyles() {
        return firstLineStyles.size() != 0;
    }

    /**
     * NB, clone list first if you want to keep the contents!
     */
    public void clearFirstLineStyles() {
        firstLineStyles.clear();
    }

    /**
     * NB, you are getting a reference! Call clearFirstLineStyles at own risk!
     */
    public LinkedList getFirstLineStyles() {
        return firstLineStyles;
    }

    public boolean shrinkWrap() {
        return shrinkWrap;
    }

    public void setShrinkWrap() {
        shrinkWrap = true;
    }

    public void unsetShrinkWrap() {
        shrinkWrap = false;
    }

    //the stuff that needs to have a separate instance for each run.
    ContextImpl(SharedContext sharedContext, Rectangle extents) {
        this.sharedContext = sharedContext;
        bfc_stack = new Stack();
        setExtents(extents);
    }

    //Style-handling stuff
    private Stack styleStack;

    public void initializeStyles(EmptyStyle c) {
        styleStack = new Stack();
        styleStack.push(c);
    }

    public void pushStyle(CascadedStyle s) {
        CalculatedStyle parent = (CalculatedStyle) styleStack.peek();
        CalculatedStyle derived = parent.deriveStyle(s);
        styleStack.push(derived);
    }

    public void popStyle() {
        if (isStylesAllPopped()) {
            XRLog.general(Level.SEVERE, "Trying to pop base empty style");
        } else
            styleStack.pop();
    }

    public CalculatedStyle getCurrentStyle() {
        return (CalculatedStyle) styleStack.peek();
    }

    public boolean isStylesAllPopped() {
        return styleStack.size() == 1;//Is primed with an EmptyStyle to start off with
    }

    /**
     * the current block formatting context
     */
    private BlockFormattingContext bfc;
    protected Stack bfc_stack;

    public BlockFormattingContext getBlockFormattingContext() {
        return bfc;
    }

    public void pushBFC(BlockFormattingContext bfc) {
        bfc_stack.push(this.bfc);
        this.bfc = bfc;
    }

    public void popBFC() {
        bfc = (BlockFormattingContext) bfc_stack.pop();
    }

    /**
     * Description of the Field
     */
    private Stack extents_stack = new Stack();

    /**
     * Description of the Field
     */
    private Rectangle extents;

    /**
     * Sets the extents attribute of the Context object
     *
     * @param rect The new extents value
     */
    public void setExtents(Rectangle rect) {
        this.extents = rect;
        if (extents.width < 1) {
            XRLog.exception("width < 1");
            extents.width = 1;
        }
    }

    /**
     * Gets the extents attribute of the Context object
     *
     * @return The extents value
     */
    public Rectangle getExtents() {
        return this.extents;
    }

    /**
     * Description of the Method
     *
     * @param dw amount to shrink width by
     * @param dh amount to shrink height by
     */
    public void shrinkExtents(int dw, int dh) {

        extents_stack.push(getExtents());

        Rectangle rect = new Rectangle(0, 0,
                getExtents().width - dw,
                getExtents().height - dh);

        setExtents(rect);
    }

    /**
     * Description of the Method
     */
    public void unshrinkExtents() {
        setExtents((Rectangle) extents_stack.pop());
    }

    /**
     * Description of the Field
     */
    private int xoff = 0;

    /**
     * Description of the Field
     */
    private int yoff = 0;

    /* =========== List stuff ============== */

    /**
     * Description of the Field
     */
    protected int list_counter;

    /**
     * Gets the listCounter attribute of the Context object
     *
     * @return The listCounter value
     */
    public int getListCounter() {
        return list_counter;
    }

    /**
     * Sets the listCounter attribute of the Context object
     *
     * @param counter The new listCounter value
     */
    public void setListCounter(int counter) {
        list_counter = counter;
    }

    /* ================== Extra Utility Funtions ============== */

    /*
     * notes to help manage inline sub blocks (like table cells)
     */
    /**
     * Sets the subBlock attribute of the Context object
     *
     * @param sub_block The new subBlock value
     */
    public void setSubBlock(boolean sub_block) {
        this.sub_block = sub_block;
    }

    /**
     * Description of the Field
     */
    protected boolean sub_block = false;

    /**
     * Gets the subBlock attribute of the Context object
     *
     * @return The subBlock value
     */
    public boolean isSubBlock() {
        return sub_block;
    }

    /**
     * Description of the Method
     *
     * @param x PARAM
     * @param y PARAM
     */
    public void translate(int x, int y) {
        //Uu.p("trans: " + x + "," + y);
        getGraphics().translate(x, y);//TODO: is this healthy and thread-safe enough?
        if (bfc != null) {
            bfc.translate(x, y);
        }
        xoff += x;
        yoff += y;
    }

    public Point getOriginOffset() {
        return new Point(xoff, yoff);
    }

    /**
     * Description of the Method
     *
     * @param box PARAM
     * @deprecated
     */
    //TODO: this is wrong! margins can collapse, for starters!
    public void translateInsets(Box box) {
        if (box == null) {
            XRLog.render(Level.WARNING, "null box");
            return;//TODO: why?
        }
        translate(box.tx,
                box.ty);
    }

    /**
     * Description of the Method
     *
     * @param box PARAM
     * @deprecated
     */
    //TODO: this is wrong! margins can collapse, for starters!
    public void untranslateInsets(Box box) {
        translate(-box.tx,
                -box.ty);
    }

    private boolean shouldStop = false;

    public boolean shouldStop() {
        return shouldStop;
    }

    public Font getCurrentFont() {
        return getCtx().getFont(getCurrentStyle().getFont(getCtx()));
    }

    public void stopRendering() {
        this.shouldStop = true;
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        return "Context: extents = " +
                "(" + extents.x + "," + extents.y + ") -> (" + extents.width + "x" + extents.height + ")"
                + " offset = " + xoff + "," + yoff
                ;
    }
	
	
	
	/* code to keep track of all of the id'd boxes */
	public void addIDBox(String id, Box box) {
		this.sharedContext.addIDBox(id,box);
	}

	public Box getIDBox(String id) {
		return this.sharedContext.getIDBox(id);
	}
	
}
