/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci, Torbjšrn Gannholm
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

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2004-dec-29
 * Time: 08:25:31
 * To change this template use File | Settings | File Templates.
 */
public interface Context {
    String getMedia();

    void setGraphics(Graphics2D graphics);

    RenderingContext getRenderingContext();

    TextRenderer getTextRenderer();

    void initializeStyles(EmptyStyle c);

    void pushStyle(CascadedStyle s);

    void popStyle();

    CalculatedStyle getCurrentStyle();

    boolean debugDrawBoxes();

    boolean debugDrawLineBoxes();

    boolean debugDrawInlineBoxes();

    boolean debugDrawFontMetrics();

    void translate(int x, int y);

    Point getOriginOffset();

    void addMaxWidth(int max_width);

    void shrinkExtents(Box block);

    void unshrinkExtents(Box block);

    void translateInsets(Box box);

    void untranslateInsets(Box box);

    String toString();

    void clearSelection();

    void updateSelection(Box box);

    boolean inSelection(Box box);

    void setExtents(Rectangle rect);

    // --Commented out by Inspection (2005-01-05 00:55): void setMaxWidth(int max_width);

    void setSelectionStart(Box box, int x);

    void setSelectionEnd(Box box, int x);

    void setListCounter(int counter);

    void setSubBlock(boolean sub_block);

    Graphics2D getGraphics();

    Rectangle getExtents();

    // --Commented out by Inspection (2005-01-05 00:53): int getMaxWidth();

    FontResolver getFontResolver();

    void flushFonts();

    Box getSelectionStart();

    Box getSelectionEnd();

    int getSelectionStartX();

    int getSelectionEndX();

    int getListCounter();

    StyleReference getCss();

    void setCss(StyleReference css);

    void setDebug_draw_boxes(boolean debug_draw_boxes);

    void setDebug_draw_line_boxes(boolean debug_draw_line_boxes);

    void setDebug_draw_inline_boxes(boolean debug_draw_inline_boxes);

    void setDebug_draw_font_metrics(boolean debug_draw_font_metrics);

    BasicPanel getCanvas();

    void setCanvas(BasicPanel canvas);

    int getList_counter();

    void setList_counter(int list_counter);

    RenderingContext getCtx();

    void setCtx(RenderingContext ctx);

    BlockFormattingContext getBlockFormattingContext();

    void pushBFC(BlockFormattingContext bfc);

    void popBFC();

    //void setBlockFormattingContext(BlockFormattingContext bfc);

    boolean isSubBlock();

    boolean isFirstLine();

    void setFirstLine(boolean first_line);

    Rectangle getFixedRectangle();

    public void setNamespaceHandler(NamespaceHandler nh);

    public NamespaceHandler getNamespaceHandler();

}
