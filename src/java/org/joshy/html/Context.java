
/* 
 * {{{ header & license 
 * Copyright (c) 2004 Joshua Marinacci 
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

package org.joshy.html;



import org.joshy.html.css.StyleReference; // CLN: (PWW 13/08/04) added

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

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import org.joshy.u;
import org.w3c.dom.Element;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;




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

    

    // CLN: (PWW 13/08/04) 
    // replace with StyleReference so we can swap in different interface implementations
    //public CSSBank css;
    public StyleReference css;

    

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

        if(box == selection_end && box == selection_start) {

            in_selection = false;

        }

    }

    public boolean inSelection(Box box) {

        if(box == selection_end ||

            box == selection_start) {

            return true;

        }

        return in_selection;

    }

    



    protected int list_counter;

    public void setListCounter(int counter) {

        this.list_counter = counter;

    }

    public int getListCounter() {

        return list_counter;

    }


    /* =========== form access code ============= */
    protected String form_name = null;
    protected Map forms = new HashMap();
    protected Map actions = new HashMap();
    
    public void setForm(String form_name, String action) {
        //u.p("set form to: " + form_name);
        this.form_name = form_name;
        if(form_name != null) {
            forms.put(form_name,new HashMap());
            actions.put(form_name,action);
        }
    }
    
    public String getForm() {
        return this.form_name;
    }
    public FormComponent addInputField(String name, Element element, JComponent comp) {
        if(getForm() == null) {
            u.p("warning! attempted to add input field: '" + name + "' to a form without a 'name' attribute");
            return null;
        }
        Map fields = (Map)forms.get(getForm());
        List field_list = new ArrayList();
        if(fields.containsKey(name)) {
            field_list = (List)fields.get(name);
        }
        FormComponent fc = new FormComponent();
        fc.name = element.getAttribute("name");
        fc.element = element;
        fc.component = comp;
        field_list.add(fc);
        fields.put(name,field_list);
        return fc;
    }
    
    public Iterator getInputFieldComponents(String form_name) {
        Map fields = (Map)forms.get(form_name);
        return fields.values().iterator();
    }

    public List getInputFieldComponents(String form_name, String field_name) {
        Map fields = (Map)forms.get(form_name);
        List field_list = (List)fields.get(field_name);
        if(field_list == null) {
            return new ArrayList();
        }
        return field_list;
    }
    
    public String getFormAction(String form_name) {
        return (String)actions.get(form_name);
    }
    
    public Map getForms() {
        return forms;
    }
    
    public class FormComponent {
        public String name;
        public JComponent component;
        public Element element;
        public ButtonGroup group;
        public void reset() {
            u.p("resetting");
            if(component instanceof JTextField) {
                u.p("it's a text field");
                if(element.hasAttribute("value")) {
                    u.p("setting to : " + element.getAttribute("value"));
                    ((JTextField)component).setText(element.getAttribute("value"));
                } else {
                    ((JTextField)component).setText("");
                }
            }
        }
    }
    
    
    /* notes to help manage inline sub blocks (like table cells) */
    public void setSubBlock(boolean sub_block) {
        this.sub_block = sub_block;
    }
    private boolean sub_block = false;
    public boolean isSubBlock() {
        return sub_block;
    }
}

