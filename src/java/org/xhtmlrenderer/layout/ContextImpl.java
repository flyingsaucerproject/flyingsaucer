package org.xhtmlrenderer.layout;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.FontResolver;
import org.xhtmlrenderer.css.StyleReference;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.Renderer;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

public class ContextImpl implements Context {
    SharedContext sharedContext;

    //delegated methods
    public String getMedia() {
        return sharedContext.getMedia();
    }

    public void setGraphics(Graphics2D graphics) {
        sharedContext.setGraphics(graphics);
    }

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

    public SharedContext.FormComponent addInputField(String name, Element element, JComponent comp) {
        return sharedContext.addInputField(name, element, comp);
    }

    public void setMaxWidth(int max_width) {
        sharedContext.setMaxWidth(max_width);
    }

    public void setSelectionStart(Box box, int x) {
        sharedContext.setSelectionStart(box, x);
    }

    public void setSelectionEnd(Box box, int x) {
        sharedContext.setSelectionEnd(box, x);
    }

    public void setForm(String form_name, String action) {
        sharedContext.setForm(form_name, action);
    }

    public Graphics2D getGraphics() {
        return sharedContext.getGraphics();
    }

    public int getMaxWidth() {
        return sharedContext.getMaxWidth();
    }

    public FontResolver getFontResolver() {
        return sharedContext.getFontResolver();
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

    public String getForm() {
        return sharedContext.getForm();
    }

    public Iterator getInputFieldComponents(String form_name) {
        return sharedContext.getInputFieldComponents(form_name);
    }

    public List getInputFieldComponents(String form_name, String field_name) {
        return sharedContext.getInputFieldComponents(form_name, field_name);
    }

    public String getFormAction(String form_name) {
        return sharedContext.getFormAction(form_name);
    }

    public Map getForms() {
        return sharedContext.getForms();
    }

    public StyleReference getCss() {
        return sharedContext.getCss();
    }

    public void setCss(StyleReference css) {
        sharedContext.setCss(css);
    }

    public void setDebug_draw_boxes(boolean debug_draw_boxes) {
        sharedContext.setDebug_draw_boxes(debug_draw_boxes);
    }

    public void setDebug_draw_line_boxes(boolean debug_draw_line_boxes) {
        sharedContext.setDebug_draw_line_boxes(debug_draw_line_boxes);
    }

    public void setDebug_draw_inline_boxes(boolean debug_draw_inline_boxes) {
        sharedContext.setDebug_draw_inline_boxes(debug_draw_inline_boxes);
    }

    public void setDebug_draw_font_metrics(boolean debug_draw_font_metrics) {
        sharedContext.setDebug_draw_font_metrics(debug_draw_font_metrics);
    }

    public BasicPanel getCanvas() {
        return sharedContext.getCanvas();
    }

    public void setCanvas(BasicPanel canvas) {
        sharedContext.setCanvas(canvas);
    }

    public String getForm_name() {
        return sharedContext.getForm_name();
    }

    public void setForm_name(String form_name) {
        sharedContext.setForm_name(form_name);
    }

    public void setForms(Map forms) {
        sharedContext.setForms(forms);
    }

    public Map getActions() {
        return sharedContext.getActions();
    }

    public void setActions(Map actions) {
        sharedContext.setActions(actions);
    }

    public RenderingContext getCtx() {
        return sharedContext.getCtx();
    }

    public void setCtx(RenderingContext ctx) {
        sharedContext.setCtx(ctx);
    }

    public Rectangle getFixedRectangle() {
        return sharedContext.getFixedRectangle();
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
        CalculatedStyle derived = getCss().getDerivedStyle(parent, s);
        styleStack.push(derived);
    }

    public void popStyle() {
        styleStack.pop();
    }

    public CalculatedStyle getCurrentStyle() {
        return (CalculatedStyle) styleStack.peek();
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

    /*public void setBlockFormattingContext(BlockFormattingContext bfc) {
        this.bfc = bfc;
    }*/

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
     * @param block PARAM
     */
    public void shrinkExtents(Box block) {

        extents_stack.push(getExtents());


        //Border border = block.border;
        //Border padding = block.padding;
        //Border margin = block.margin;

        Rectangle rect = new Rectangle(0, 0,
                getExtents().width - block.totalHorizontalPadding(getCurrentStyle()),
                getExtents().height - block.totalVerticalPadding(getCurrentStyle()));

        setExtents(rect);
    }

    /**
     * Description of the Method
     *
     * @param block PARAM
     */
    public void unshrinkExtents(Box block) {
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
        return getList_counter();
    }

    /**
     * Sets the listCounter attribute of the Context object
     *
     * @param counter The new listCounter value
     */
    public void setListCounter(int counter) {
        this.setList_counter(counter);
    }

    public int getList_counter() {
        return list_counter;
    }

    public void setList_counter(int list_counter) {
        this.list_counter = list_counter;
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

    private boolean first_line = false;

    public boolean isFirstLine() {
        return first_line;
    }

    public void setFirstLine(boolean first_line) {
        this.first_line = first_line;
    }

    public Layout getLayout(Node node) {
        return getCtx().getLayoutFactory().getLayout(this, node);
    }

    public Renderer getRenderer(Node node) {
        return getCtx().getLayoutFactory().getRenderer(this, node);
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
     */
    public void translateInsets(Box box) {
        Border border = LayoutUtil.getBorder(box, getCurrentStyle());
        Border margin = getCurrentStyle().getMarginWidth();
        Border padding = getCurrentStyle().getPaddingWidth();
        if (box == null) {
            XRLog.render(Level.WARNING, "null box");
            return;//TODO: why?
        }
        if (margin == null) {
            XRLog.render(Level.WARNING, "translate insets: null margin on box of type " + box.getClass().getName() +
                    " content " + (box.content == null ? "null" : box.content.getClass().getName()));
            return;
        }
        if (border == null) {
            XRLog.render(Level.WARNING, "translate insets: null border on box of type " + box.getClass().getName() +
                    " content " + (box.content == null ? "null" : box.content.getClass().getName()));
            return;
        }
        if (padding == null) {
            XRLog.render(Level.WARNING, "translate insets: null padding on box of type " + box.getClass().getName() +
                    " content " + (box.content == null ? "null" : box.content.getClass().getName()));
            return;
        }
        translate(margin.left + border.left + padding.left,
                margin.top + border.top + padding.top);
    }

    /**
     * Description of the Method
     *
     * @param box PARAM
     */
    public void untranslateInsets(Box box) {
        Border border = LayoutUtil.getBorder(box, getCurrentStyle());
        Border margin = getCurrentStyle().getMarginWidth();
        Border padding = getCurrentStyle().getPaddingWidth();
        if (margin == null) {
            XRLog.render(Level.WARNING, "translate insets: null margin on box of type " + box.getClass().getName() +
                    " content " + (box.content == null ? "null" : box.content.getClass().getName()));
            return;
        }
        if (border == null) {
            XRLog.render(Level.WARNING, "translate insets: null border on box of type " + box.getClass().getName() +
                    " content " + (box.content == null ? "null" : box.content.getClass().getName()));
            return;
        }
        if (padding == null) {
            XRLog.render(Level.WARNING, "translate insets: null padding on box of type " + box.getClass().getName() +
                    " content " + (box.content == null ? "null" : box.content.getClass().getName()));
            return;
        }
        translate(-(margin.left + border.left + padding.left),
                -(margin.top + border.top + padding.top));
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        return "Context: extents = " +
                "(" + extents.x + "," + extents.y + ") -> (" + extents.width + "x" + extents.height + ")"
                //" cursor = " + cursor +
                //"\n color = " + color + " background color = " + background_color;
                + " offset = " + xoff + "," + yoff
                ;
    }

}
