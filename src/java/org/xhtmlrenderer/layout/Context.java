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
package org.xhtmlrenderer.layout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.swing.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.FontResolver;
import org.xhtmlrenderer.css.FontResolverTest;
import org.xhtmlrenderer.css.StyleReference;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.Renderer;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.extend.*;
import org.sektor37.minium.*;

/**
 * Description of the Class
 *
 * @author   empty
 */
public class Context {

    /** Description of the Field */
    public Graphics graphics;

    /** Description of the Field */
    public StyleReference css;

    /** Description of the Field */
    public boolean debug_draw_boxes;

    /** Description of the Field */
    public boolean debug_draw_line_boxes;
    public boolean debug_draw_inline_boxes;
    public boolean debug_draw_font_metrics;

    /** Description of the Field */
    public HTMLPanel canvas;

    //public Graphics canvas_graphics;

    /** Description of the Field */
    public JComponent viewport;

    /** Description of the Field */
    public Point placement_point;

    /*
     * selection management code
     */
    /** Description of the Field */
    protected Box selection_start, selection_end;

    /** Description of the Field */
    protected int selection_end_x, selection_start_x;


    /** Description of the Field */
    protected boolean in_selection = false;



    /** Description of the Field */
    protected int list_counter;

    /*
     * =========== form access code =============
     */
    /** Description of the Field */
    protected String form_name = null;
    /** Description of the Field */
    protected Map forms = new HashMap();
    /** Description of the Field */
    protected Map actions = new HashMap();
    /** the current block formatting context */
    protected BlockFormattingContext bfc;

    /** Description of the Field */
    Stack extents_stack = new Stack();

    /** Description of the Field */
    private FontResolver font_resolver;

    /** Description of the Field */
    private Rectangle extents;

    /** Description of the Field */
    private int xoff = 0;

    /** Description of the Field */
    private int yoff = 0;

    /** Description of the Field */
    private int max_width;
    /** Description of the Field */
    private boolean sub_block = false;
    
    public RenderingContext ctx;
    public RenderingContext getRenderingContext() {
        return ctx;
    }
    
    private TextRenderer text_renderer;
    public TextRenderer getTextRenderer() {
        return text_renderer;
    }

    /** Constructor for the Context object */
    public Context() {
        font_resolver = new FontResolverTest();
        
        // set up text rendering code
        TextRendererFactory text_renderer_factory = TextRendererFactory.newOversamplingInstance();
        text_renderer = text_renderer_factory.newTextRenderer();

        String text_renderer_quality = System.getProperty("org.xhtmlrenderer.minium.quality");
        if (null == text_renderer_quality) {
            text_renderer_quality = "lowest";
        }

        Map defaultHints;
        if ("low".equals(text_renderer_quality)) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_LOW;
        } else if ("medium".equals(text_renderer_quality)) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_MEDIUM;
        } else if ("high".equals(text_renderer_quality)) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_HIGH;
        } else if ("highest".equals(text_renderer_quality)) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_HIGHEST;
        } else {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_FASTEST;
        }
        text_renderer.setTextRenderingHints(defaultHints);
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public boolean debugDrawBoxes() {
        return debug_draw_boxes;
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public boolean debugDrawLineBoxes() {
        return debug_draw_line_boxes;
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public boolean debugDrawInlineBoxes() {
        return debug_draw_inline_boxes;
    }
    
    public boolean debugDrawFontMetrics() {
        return debug_draw_font_metrics;
    }

    /**
     * Description of the Method
     *
     * @param x  PARAM
     * @param y  PARAM
     */
    public void translate( int x, int y ) {
        this.graphics.translate( x, y );
        if(bfc != null) {
            bfc.translate(x,y);
        }
        xoff += x;
        yoff += y;
    }
    
    public Point getOriginOffset() {
        return new Point(xoff,yoff);
    }

    /**
     * Adds a feature to the MaxWidth attribute of the Context object
     *
     * @param max_width  The feature to be added to the MaxWidth attribute
     */
    public void addMaxWidth( int max_width ) {
        if ( max_width > this.max_width ) {
            this.max_width = max_width;
        }
    }

    /**
     * Description of the Method
     *
     * @param block  PARAM
     */
    public void shrinkExtents( Box block ) {

        extents_stack.push( getExtents() );

        Border border = block.border;
        Border padding = block.padding;
        Border margin = block.margin;

        Rectangle rect = new Rectangle( 0, 0,
                getExtents().width - ( margin.left + border.left + padding.left )
                - ( margin.right + border.right + padding.right ),
                getExtents().height - ( margin.top + border.top + padding.top )
                - ( margin.bottom + border.bottom + padding.bottom ) );

        setExtents( rect );

    }


    /**
     * Description of the Method
     *
     * @param block  PARAM
     */
    public void unshrinkExtents( Box block ) {
        setExtents( (Rectangle)extents_stack.pop() );
    }


    /**
     * Description of the Method
     *
     * @param box  PARAM
     */
    public void translateInsets( Box box ) {
        translate( box.margin.left + box.border.left + box.padding.left,
                box.margin.top + box.border.top + box.padding.top );
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     */
    public void untranslateInsets( Box box ) {
        translate( -( box.margin.left + box.border.left + box.padding.left ),
                -( box.margin.top + box.border.top + box.padding.top ) );
    }


    /**
     * Converts to a String representation of the object.
     *
     * @return   A string representation of the object.
     */
    public String toString() {
        return "Context: extents = " + extents //" cursor = " + cursor +
        //"\n color = " + color + " background color = " + background_color;
                + " offset = " + xoff + "," + yoff
                ;
    }


    /** Description of the Method */
    public void clearSelection() {
        selection_end = null;
        selection_start = null;
        selection_start_x = -1;
        selection_end_x = -1;
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     */
    public void updateSelection( Box box ) {
        if ( box == selection_end ) {
            in_selection = false;
        }
        if ( box == selection_start ) {
            in_selection = true;
        }
        if ( box == selection_end && box == selection_start ) {
            in_selection = false;
        }
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     * @return     Returns
     */
    public boolean inSelection( Box box ) {
        if ( box == selection_end ||
                box == selection_start ) {
            return true;
        }
        return in_selection;
    }

    /**
     * Adds a feature to the InputField attribute of the Context object
     *
     * @param name     The feature to be added to the InputField attribute
     * @param element  The feature to be added to the InputField attribute
     * @param comp     The feature to be added to the InputField attribute
     * @return         Returns
     */
    public FormComponent addInputField( String name, Element element, JComponent comp ) {
        if ( getForm() == null ) {
            u.p( "warning! attempted to add input field: '" + name + "' to a form without a 'name' attribute" );
            return null;
        }
        Map fields = (Map)forms.get( getForm() );
        List field_list = new ArrayList();
        if ( fields.containsKey( name ) ) {
            field_list = (List)fields.get( name );
        }
        FormComponent fc = new FormComponent();
        fc.name = element.getAttribute( "name" );
        fc.element = element;
        fc.component = comp;
        field_list.add( fc );
        fields.put( name, field_list );
        return fc;
    }

    /**
     * Sets the extents attribute of the Context object
     *
     * @param rect  The new extents value
     */
    public void setExtents( Rectangle rect ) {
        this.extents = rect;
        if(extents.width < 1) {
            XRLog.exception("width < 1");
            extents.width = 1;
        }
    }

    /**
     * Sets the maxWidth attribute of the Context object
     *
     * @param max_width  The new maxWidth value
     */
    public void setMaxWidth( int max_width ) {
        this.max_width = max_width;
    }


    /**
     * Sets the selectionStart attribute of the Context object
     *
     * @param box  The new selectionStart value
     */
    public void setSelectionStart( Box box, int x ) {
        selection_start = box;
        selection_start_x = x;
        if(box instanceof InlineBox) {
            InlineBox ib = (InlineBox)box;
            int i = ib.getTextIndex(this,x);
        }
    }

    /**
     * Sets the selectionEnd attribute of the Context object
     *
     * @param box  The new selectionEnd value
     */
    public void setSelectionEnd( Box box, int x ) {
        selection_end = box;
        selection_end_x = x;
        if(box instanceof InlineBox) {
            InlineBox ib = (InlineBox)box;
            int i = ib.getTextIndex(this,x);
            selection_end_x = ib.getAdvance(this,i);
        }
    }


    /**
     * Sets the listCounter attribute of the Context object
     *
     * @param counter  The new listCounter value
     */
    public void setListCounter( int counter ) {
        this.list_counter = counter;
    }

    /**
     * Sets the form attribute of the Context object
     *
     * @param form_name  The new form value
     * @param action     The new form value
     */
    public void setForm( String form_name, String action ) {
        this.form_name = form_name;
        if ( form_name != null ) {
            forms.put( form_name, new HashMap() );
            actions.put( form_name, action );
        }
    }


    /*
     * notes to help manage inline sub blocks (like table cells)
     */
    /**
     * Sets the subBlock attribute of the Context object
     *
     * @param sub_block  The new subBlock value
     */
    public void setSubBlock( boolean sub_block ) {
        this.sub_block = sub_block;
    }

    /**
     * Gets the graphics attribute of the Context object
     *
     * @return   The graphics value
     */
    public Graphics getGraphics() {
        return graphics;
    }

    /**
     * Gets the extents attribute of the Context object
     *
     * @return   The extents value
     */
    public Rectangle getExtents() {
        return this.extents;
    }

    /**
     * Gets the viewport attribute of the Context object
     *
     * @return   The viewport value
     */
    public JComponent getViewport() {
        return this.viewport;
    }

    /**
     * Gets the xoff attribute of the Context object
     *
     * @return   The xoff value
     */
    public int getXoff() {
        return this.xoff;
    }

    /**
     * Gets the yoff attribute of the Context object
     *
     * @return   The yoff value
     */
    public int getYoff() {
        return this.yoff;
    }

    /**
     * Gets the maxWidth attribute of the Context object
     *
     * @return   The maxWidth value
     */
    public int getMaxWidth() {
        return this.max_width;
    }


    /**
     * Gets the fontResolver attribute of the Context object
     *
     * @return   The fontResolver value
     */
    public FontResolver getFontResolver() {
        return font_resolver;
    }


    /**
     * Gets the selectionStart attribute of the Context object
     *
     * @return   The selectionStart value
     */
    public Box getSelectionStart() {
        return selection_start;
    }

    /**
     * Gets the selectionEnd attribute of the Context object
     *
     * @return   The selectionEnd value
     */
    public Box getSelectionEnd() {
        return selection_end;
    }

    /**
     * Gets the selectionStartX attribute of the Context object
     *
     * @return   The selectionStartX value
     */
    public int getSelectionStartX() {
        return selection_start_x;
    }

    /**
     * Gets the selectionEndX attribute of the Context object
     *
     * @return   The selectionEndX value
     */
    public int getSelectionEndX() {
        return selection_end_x;
    }

    /**
     * Gets the listCounter attribute of the Context object
     *
     * @return   The listCounter value
     */
    public int getListCounter() {
        return list_counter;
    }

    /**
     * Gets the form attribute of the Context object
     *
     * @return   The form value
     */
    public String getForm() {
        return this.form_name;
    }

    /**
     * Gets the inputFieldComponents attribute of the Context object
     *
     * @param form_name  PARAM
     * @return           The inputFieldComponents value
     */
    public Iterator getInputFieldComponents( String form_name ) {
        Map fields = (Map)forms.get( form_name );
        return fields.values().iterator();
    }

    /**
     * Gets the inputFieldComponents attribute of the Context object
     *
     * @param form_name   PARAM
     * @param field_name  PARAM
     * @return            The inputFieldComponents value
     */
    public List getInputFieldComponents( String form_name, String field_name ) {
        Map fields = (Map)forms.get( form_name );
        List field_list = (List)fields.get( field_name );
        if ( field_list == null ) {
            return new ArrayList();
        }
        return field_list;
    }

    /**
     * Gets the formAction attribute of the Context object
     *
     * @param form_name  PARAM
     * @return           The formAction value
     */
    public String getFormAction( String form_name ) {
        return (String)actions.get( form_name );
    }

    /**
     * Gets the forms attribute of the Context object
     *
     * @return   The forms value
     */
    public Map getForms() {
        return forms;
    }

    /**
     * Gets the subBlock attribute of the Context object
     *
     * @return   The subBlock value
     */
    public boolean isSubBlock() {
        return sub_block;
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    public class FormComponent {
        /** Description of the Field */
        public String name;
        /** Description of the Field */
        public JComponent component;
        /** Description of the Field */
        public Element element;
        /** Description of the Field */
        public ButtonGroup group;

        /** Description of the Method */
        public void reset() {
            u.p( "resetting" );
            if ( component instanceof JTextField ) {
                u.p( "it's a text field" );
                if ( element.hasAttribute( "value" ) ) {
                    u.p( "setting to : " + element.getAttribute( "value" ) );
                    ( (JTextField)component ).setText( element.getAttribute( "value" ) );
                } else {
                    ( (JTextField)component ).setText( "" );
                }
            }
        }
    }
    
    public BlockFormattingContext getBlockFormattingContext() {
        return bfc;
    }
    public void setBlockFormattingContext(BlockFormattingContext bfc) {
        this.bfc = bfc;
    }
    
    private boolean first_line = false;
    public boolean isFirstLine() {
        return first_line;
    }
    public void setFirstLine(boolean first_line) {
        this.first_line = first_line;
    }
    
    
    public Rectangle getFixedRectangle() {
        //u.p("this = " + canvas);
        Rectangle rect = canvas.getFixedRectangle();
        rect.translate(canvas.getX(),canvas.getY());
        return rect;
    }
    
    public Layout getLayout(Node node) {
        return ctx.getLayoutFactory().getLayout(this, node);
    }
    public Renderer getRenderer(Node node) {
        return ctx.getLayoutFactory().getRenderer(this, node);
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.17  2004/11/14 16:40:58  joshy
 * refactored layout factory
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/14 06:26:39  joshy
 * added better detection for width problems. should avoid most
 * crashes
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/11/12 22:02:00  joshy
 * initial support for mouse copy selection
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/12 17:05:24  joshy
 * support for fixed positioning
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/12 02:54:38  joshy
 * removed more dead code
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/12 02:47:33  joshy
 * moved baseurl to rendering context
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/10 17:28:54  joshy
 * initial support for anti-aliased text w/ minium
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/09 00:36:08  joshy
 * fixed more text alignment
 * added menu item to show font metrics
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/11/08 16:56:51  joshy
 * added first-line pseudo-class support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/03 23:54:33  joshy
 * added hamlet and tables to the browser
 * more support for absolute layout
 * added absolute layout unit tests
 * removed more dead code and moved code into layout factory
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/03 15:17:04  joshy
 * added intial support for absolute positioning
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/11/02 20:44:55  joshy
 * put in some prep work for float support
 * removed some dead debugging code
 * moved isBlock code to LayoutFactory
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 13:46:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

