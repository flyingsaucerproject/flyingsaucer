/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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

import org.xhtmlrenderer.context.FontResolver;
import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.swing.BasicPanel;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Map;
import java.util.HashMap;

/**
 * Description of the Class
 *
 * @author empty
 */
//TODO: clarify this class, is it just a pile of different functionality at the moment?
public class SharedContext {

    /**
     * Constructor for the Context object
     */
    public SharedContext() {
        font_resolver = new FontResolver();
    }

    public Context newContextInstance(Rectangle extents) {
        Context c = new ContextImpl(this, extents);
        c.initializeStyles(new EmptyStyle());
        return c;
    }

    /* =========== Font stuff ============== */

    /**
     * Gets the fontResolver attribute of the Context object
     *
     * @return The fontResolver value
     */
    public FontResolver getFontResolver() {
        return font_resolver;
    }

    public void flushFonts() {
        font_resolver = new FontResolver();
    }

    /**
     * Description of the Field
     */
    protected FontResolver font_resolver;

    /**
     * Description of the Field
     */
    protected Graphics2D graphics;

    /**
     * Gets the graphics attribute of the Context object
     *
     * @return The graphics value
     */
    public Graphics2D getGraphics() {
        return graphics;
    }

    /**
     * Description of the Field
     */
    public void setGraphics(Graphics2D graphics) {
        this.graphics = graphics;
    }

    /**
     * The media for this context
     */
    public String getMedia() {
        return getCtx().getMedia();
    }

    /**
     * Description of the Field
     */
    protected StyleReference css;

    /**
     * Description of the Field
     */
    protected boolean debug_draw_boxes;

    /**
     * Description of the Field
     */
    protected boolean debug_draw_line_boxes;
    protected boolean debug_draw_inline_boxes;
    protected boolean debug_draw_font_metrics;

    /**
     * Description of the Field
     */
    protected BasicPanel canvas;

    /*
     * selection management code
     */
    /**
     * Description of the Field
     */
    protected Box selection_start, selection_end;

    /**
     * Description of the Field
     */
    protected int selection_end_x, selection_start_x;


    /**
     * Description of the Field
     */
    protected boolean in_selection = false;

    /**
     * Description of the Field
     */
    protected int max_width;

    protected RenderingContext ctx;

    public RenderingContext getRenderingContext() {
        return getCtx();
    }

    public TextRenderer getTextRenderer() {
        return getCtx().getTextRenderer();
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    public boolean debugDrawBoxes() {
        return debug_draw_boxes;
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    public boolean debugDrawLineBoxes() {
        return debug_draw_line_boxes;
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    public boolean debugDrawInlineBoxes() {
        return debug_draw_inline_boxes;
    }

    public boolean debugDrawFontMetrics() {
        return debug_draw_font_metrics;
    }

    public void setDebug_draw_boxes(boolean debug_draw_boxes) {
        this.debug_draw_boxes = debug_draw_boxes;
    }

    public void setDebug_draw_line_boxes(boolean debug_draw_line_boxes) {
        this.debug_draw_line_boxes = debug_draw_line_boxes;
    }

    public void setDebug_draw_inline_boxes(boolean debug_draw_inline_boxes) {
        this.debug_draw_inline_boxes = debug_draw_inline_boxes;
    }

    public void setDebug_draw_font_metrics(boolean debug_draw_font_metrics) {
        this.debug_draw_font_metrics = debug_draw_font_metrics;
    }

    /**
     * Adds a feature to the MaxWidth attribute of the Context object
     *
     * @param max_width The feature to be added to the MaxWidth attribute
     */
    public void addMaxWidth(int max_width) {
        if (max_width > this.max_width) {
            this.max_width = max_width;
        }
    }


    /**
     * Description of the Method
     */
    public void clearSelection() {
        selection_end = null;
        selection_start = null;
        int selection_start_x1 = -1;
        selection_start_x = selection_start_x1;
        int selection_end_x1 = -1;
        selection_end_x = selection_end_x1;
    }

    /**
     * Description of the Method
     *
     * @param box PARAM
     */
    public void updateSelection(Box box) {
        if (box == selection_end) {
            in_selection = false;
        }
        if (box == selection_start) {
            in_selection = true;
        }
        if (box == selection_end && box == selection_start) {
            in_selection = false;
        }
    }

    /**
     * Description of the Method
     *
     * @param box PARAM
     * @return Returns
     */
    public boolean inSelection(Box box) {
        if (box == selection_end ||
                box == selection_start) {
            return true;
        }
        return in_selection;
    }

    /**
     * Sets the maxWidth attribute of the Context object
     *
     * @param max_width The new maxWidth value
     */
    public void setMaxWidth(int max_width) {
        this.max_width = max_width;
    }


    /**
     * Gets the maxWidth attribute of the Context object
     *
     * @return The maxWidth value
     */
    public int getMaxWidth() {
        return this.max_width;
    }


    
    
    /* =========== Selection Management ============== */
    
    
    /**
     * Gets the selectionStart attribute of the Context object
     *
     * @return The selectionStart value
     */
    public Box getSelectionStart() {
        return selection_start;
    }

    /**
     * Gets the selectionEnd attribute of the Context object
     *
     * @return The selectionEnd value
     */
    public Box getSelectionEnd() {
        return selection_end;
    }

    /**
     * Gets the selectionStartX attribute of the Context object
     *
     * @return The selectionStartX value
     */
    public int getSelectionStartX() {
        return selection_start_x;
    }

    /**
     * Gets the selectionEndX attribute of the Context object
     *
     * @return The selectionEndX value
     */
    public int getSelectionEndX() {
        return selection_end_x;
    }

    /**
     * Sets the selectionStart attribute of the Context object
     *
     * @param box The new selectionStart value
     */
    //TODO: is this the place for selections? A separate kind of context for that kind of stuff might be better?
    public void setSelectionStart(Box box, int x) {
        selection_start = box;
        selection_start_x = x;
    }

    /**
     * Sets the selectionEnd attribute of the Context object
     *
     * @param box The new selectionEnd value
     */
    public void setSelectionEnd(Box box, int x) {
        selection_end = box;
        selection_end_x = x;
        //TODO: find a way to do this
        /*if (box instanceof InlineBox) {
            InlineBox ib = (InlineBox) box;
            int i = ib.getTextIndex(x, getGraphics());
            selection_end_x = ib.getAdvance(i, getGraphics());
        }*/
    }


    
    
    /* =========== Form Stuff ============== */


    public StyleReference getCss() {
        return css;
    }

    public void setCss(StyleReference css) {
        this.css = css;
    }

    public BasicPanel getCanvas() {
        return canvas;
    }

    public void setCanvas(BasicPanel canvas) {
        this.canvas = canvas;
    }


    public RenderingContext getCtx() {
        return ctx;
    }

    public void setCtx(RenderingContext ctx) {
        this.ctx = ctx;
    }


    public Rectangle getFixedRectangle() {
        //Uu.p("this = " + canvas);
        Rectangle rect = getCanvas().getFixedRectangle();
        rect.translate(getCanvas().getX(), getCanvas().getY());
        return rect;
    }

    private NamespaceHandler namespaceHandler;

    public void setNamespaceHandler(NamespaceHandler nh) {
        namespaceHandler = nh;
    }

    public NamespaceHandler getNamespaceHandler() {
        return namespaceHandler;
    }

	private Map id_map;
	public void addIDBox(String id, Box box) {
		if(id_map == null) {
			id_map = new HashMap();
		}
		id_map.put(id,box);
	}

	public Box getIDBox(String id) {
		return (Box)id_map.get(id);
	}
	
	
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.14  2005/07/02 07:26:59  joshy
 * better support for jumping to anchor tags
 * also some testing for the resize issue
 * need to investigate making the history remember document position.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2005/06/22 23:48:45  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.12  2005/06/16 07:24:51  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.11  2005/05/08 14:36:57  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.10  2005/03/24 23:12:56  pdoubleya
 * EmptyStyle now takes SC in constructor.
 *
 * Revision 1.9  2005/01/29 20:19:24  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.8  2005/01/13 00:48:46  tobega
 * Added preparation of values for a form submission
 *
 * Revision 1.7  2005/01/08 11:55:17  tobega
 * Started massaging the extension interfaces
 *
 * Revision 1.6  2005/01/05 17:56:35  tobega
 * Reduced memory more, especially by using WeakHashMap for caching Mappers. Look over other caching to use similar schemes (cache when memory available).
 *
 * Revision 1.5  2005/01/05 01:10:15  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.4  2005/01/02 12:22:19  tobega
 * Cleaned out old layout code
 *
 * Revision 1.3  2005/01/02 01:00:09  tobega
 * Started sketching in code for handling replaced elements in the NamespaceHandler
 *
 * Revision 1.2  2005/01/01 08:09:20  tobega
 * Now using entirely static methods for render. Need to implement table. Need to clean.
 *
 * Revision 1.1  2004/12/29 10:39:33  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.40  2004/12/29 07:35:38  tobega
 * Prepared for cloned Context instances by encapsulating fields
 *
 * Revision 1.39  2004/12/28 01:48:23  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
 * Revision 1.38  2004/12/27 09:40:47  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.37  2004/12/27 07:43:31  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.36  2004/12/16 17:22:25  joshy
 * minor code cleanup
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.35  2004/12/16 17:10:41  joshy
 * fixed box bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.34  2004/12/14 02:28:48  joshy
 * removed some comments
 * some bugs with the backgrounds still
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.33  2004/12/14 01:56:23  joshy
 * fixed layout width bugs
 * fixed extra border on document bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.32  2004/12/13 15:15:57  joshy
 * fixed bug where inlines would pick up parent styles when they aren't supposed to
 * fixed extra Xx's in printed text
 * added conf boolean to turn on box outlines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.31  2004/12/12 03:32:58  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.30  2004/12/11 23:36:48  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.29  2004/12/11 18:18:10  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.28  2004/12/10 06:51:02  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.27  2004/12/05 05:22:35  joshy
 * fixed NPEs in selection listener
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.26  2004/12/02 15:50:58  joshy
 * added debugging
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.25  2004/12/01 14:02:52  joshy
 * modified media to use the value from the rendering context
 * added the inline-block box
 * - j
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.24  2004/11/30 20:28:27  joshy
 * support for multiple floats on a single line.
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.23  2004/11/28 23:29:02  tobega
 * Now handles media on Stylesheets, still need to handle at-media-rules. The media-type should be set in Context.media (set by default to "screen") before calling setContext on StyleReference.
 *
 * Revision 1.22  2004/11/18 14:12:44  joshy
 * added whitespace test
 * cleaned up some code, spacing, and comments
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.21  2004/11/18 02:58:06  joshy
 * collapsed the font resolver and font resolver test into one class, and removed
 * the other
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.20  2004/11/17 14:58:18  joshy
 * added actions for font resizing
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.19  2004/11/16 07:25:12  tobega
 * Renamed HTMLPanel to BasicPanel
 *
 * Revision 1.18  2004/11/14 21:33:47  joshy
 * new font rendering interface support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
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

