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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.FontResolver;
import org.xhtmlrenderer.css.StyleReference;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.swing.BasicPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    SharedContext.FormComponent addInputField(String name, Element element, JComponent comp);

    void setExtents(Rectangle rect);

    void setMaxWidth(int max_width);

    void setSelectionStart(Box box, int x);

    void setSelectionEnd(Box box, int x);

    void setListCounter(int counter);

    void setForm(String form_name, String action);

    void setSubBlock(boolean sub_block);

    Graphics2D getGraphics();

    Rectangle getExtents();

    int getMaxWidth();

    FontResolver getFontResolver();

    void flushFonts();

    Box getSelectionStart();

    Box getSelectionEnd();

    int getSelectionStartX();

    int getSelectionEndX();

    int getListCounter();

    String getForm();

    Iterator getInputFieldComponents(String form_name);

    List getInputFieldComponents(String form_name, String field_name);

    String getFormAction(String form_name);

    Map getForms();

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

    String getForm_name();

    void setForm_name(String form_name);

    void setForms(Map forms);

    Map getActions();

    void setActions(Map actions);

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

    Layout getLayout(Node node);

    org.xhtmlrenderer.render.Renderer getRenderer(Node node);
}
