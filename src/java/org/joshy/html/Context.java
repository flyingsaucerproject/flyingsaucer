package org.joshy.html;

import org.joshy.html.css.FontResolver;
import org.joshy.html.css.FontResolverTest;
import java.util.Stack;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import org.joshy.html.box.*;
import java.net.URL;

import org.joshy.u;

public class Context { 
    public Graphics graphics;
    public Graphics getGraphics() {
        return graphics;
    }
    
    private Rectangle extents;
    public Rectangle getExtents() {
        return this.extents;
    }
    public void setExtents(Rectangle rect) {
        this.extents = rect;
    }
    
    /*
    public Point origin;
    public Point getOrigin() {
        return origin;
    }
    
    public Rectangle bounds;
    public Rectangle getBounds() {
        return bounds;
    }
    */
    
    public Point cursor; 
    public Point getCursor() {
        return cursor;
    }
    
    public Color color;
    public Color getColor() {
        return color;
    }
    
    public Color background_color;
    public Color getBackgroundColor() {
        return background_color;
    }
    
    public CSSBank css;
    
    public boolean debug_draw_boxes;
    public boolean debugDrawBoxes() {
        return debug_draw_boxes;
    }
    
    public boolean debug_draw_line_boxes;
    public boolean debugDrawLineBoxes() {
        return debug_draw_line_boxes;
    }
    
    public boolean debug_draw_inline_boxes;
    public boolean debugDrawInlineBoxes() {
        return debug_draw_inline_boxes;
    }
    
    public JComponent canvas;
    //public Graphics canvas_graphics;
    public JComponent viewport;
    public JComponent getViewport() {
        return this.viewport;
    }
    
    
    private int xoff = 0;
    private int yoff = 0;
    public void translate(int x, int y) {
        this.graphics.translate(x,y);
        xoff+=x;
        yoff+=y;
    }
    public int getXoff() {
        return this.xoff;
    }
    public int getYoff() {
        return this.yoff;
    }
    
    private Point left_tab = new Point(0,0);
    private Point right_tab = new Point(0,0);
    public void setLeftTab(Point pt) {
        this.left_tab = pt;
    }
    public void setRightTab(Point pt) {
        this.right_tab = pt;
    }
    public Point getLeftTab() {
        return this.left_tab;
    }
    public Point getRightTab() {
        return this.right_tab;
    }
    
    public Point placement_point;
    public Box parent_box;
    
    
    private URL base_url;
    public void setBaseURL(URL base_url) {
        this.base_url = base_url;
    }
    public URL getBaseURL() {
        return this.base_url;
    }
    
    private int max_width;
    public int getMaxWidth() {
        return this.max_width;
    }
    public void setMaxWidth(int max_width) {
        this.max_width = max_width;
    }
    public void addMaxWidth(int max_width) {
        if(max_width > this.max_width) {
            this.max_width = max_width;
        }
    }
    
    Stack extents_stack = new Stack();
    public void shrinkExtents(BlockBox block) {
        extents_stack.push(getExtents());
        Border border = block.border;
        Border padding = block.padding;
        Border margin = block.margin;
        
        Rectangle rect = new Rectangle(0,0,
                getExtents().width - (margin.left + border.left + padding.left)
                 - (margin.right + border.right + padding.right),
                getExtents().height - (margin.top + border.top + padding.top)
                 - (margin.bottom + border.bottom + padding.bottom));
        setExtents(rect);
    }
    
    public void unshrinkExtents(BlockBox block) {
        setExtents((Rectangle)extents_stack.pop());
    }
    
    public void translateInsets(Box box) {
        translate(box.margin.left + box.border.left + box.padding.left,
                  box.margin.top + box.border.top + box.padding.top);
    }
    public void untranslateInsets(Box box) {
        translate(-(box.margin.left + box.border.left + box.padding.left),
                  -(box.margin.top + box.border.top + box.padding.top));
    }
    
    FontResolver font_resolver;
    
    public FontResolver getFontResolver() {
        return font_resolver;
    }
    
    public Context() {
        font_resolver = new FontResolverTest();
    }
    
    public String toString() {
        return "Context: extents = " + extents  //" cursor = " + cursor + 
        //"\n color = " + color + " background color = " + background_color;
        + " offset = " + xoff + "," + yoff
        ;
    }
    
    
    
    /* selection management code */
    protected Box selection_start, selection_end;
    protected int selection_end_x, selection_start_x;
    
    public void setSelectionStart(Box box) {
        selection_start = box;
    }
    public void setSelectionEnd(Box box) {
        selection_end = box;
    }
    public void setSelectionStartX(int x) {
        selection_start_x = x;
    }
    public void setSelectionEndX(int x) {
        selection_end_x = x;
    }
    
    public Box getSelectionStart() {
        return selection_start;
    }
    public Box getSelectionEnd() {
        return selection_end;
    }
    public int getSelectionStartX() {
        return selection_start_x;
    }
    public int getSelectionEndX() {
        return selection_end_x;
    }
    
    public void clearSelection() {
        selection_end = null;
        selection_start = null;
        selection_start_x = -1;
        selection_end_x = -1;
    }
    
    protected boolean in_selection = false;
    public void updateSelection(Box box) {
        if(box == selection_end) {
            in_selection = false;
        }
        if(box == selection_start) {
            in_selection = true;
        }
    }
    public boolean inSelection(Box box) {
        if(box == selection_end ||
            box == selection_start) {
            return true;
        }
        return in_selection;
    }
    
    
}
