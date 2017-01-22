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

import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.context.AWTFontResolver;
import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.FSCanvas;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.breaker.DefaultLineBreakingStrategy;
import org.xhtmlrenderer.layout.breaker.LineBreakingStrategy;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.swing.Java2DTextRenderer;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;
import org.xhtmlrenderer.util.XRLog;

/**
 * The SharedContext is that which is kept between successive layout and render runs.
 *
 * @author empty
 */
public class SharedContext {
    private TextRenderer text_renderer;
    private String media;
    private UserAgentCallback uac;

    private boolean interactive = true;

    private Map idMap;

    /*
     * used to adjust fonts, ems, points, into screen resolution
     */
    /**
     * Description of the Field
     */
    private float dpi;
    /**
     * Description of the Field
     */
    private final static int MM__PER__CM = 10;
    /**
     * Description of the Field
     */
    private final static float CM__PER__IN = 2.54F;
    /**
     * dpi in a more usable way
     */
    private float mm_per_dot;

    private final static float DEFAULT_DPI = 72;
    private boolean print;

    private int dotsPerPixel = 1;

    private Map styleMap;

    private ReplacedElementFactory replacedElementFactory;
    private Rectangle temp_canvas;
    
    private LineBreakingStrategy lineBreakingStrategy = new DefaultLineBreakingStrategy();

    public SharedContext() {
    }

    /**
     * Constructor for the Context object
     */
    public SharedContext(UserAgentCallback uac) {
        font_resolver = new AWTFontResolver();
        replacedElementFactory = new SwingReplacedElementFactory();
        setMedia("screen");
        this.uac = uac;
        setCss(new StyleReference(uac));
        XRLog.render("Using CSS implementation from: " + getCss().getClass().getName());
        setTextRenderer(new Java2DTextRenderer());
        try {
            setDPI(Toolkit.getDefaultToolkit().getScreenResolution());
        } catch (HeadlessException e) {
            setDPI(DEFAULT_DPI);
        }
    }


    /**
     * Constructor for the Context object
     */
    public SharedContext(UserAgentCallback uac, FontResolver fr, ReplacedElementFactory ref, TextRenderer tr, float dpi) {
        font_resolver = fr;
        replacedElementFactory = ref;
        setMedia("screen");
        this.uac = uac;
        setCss(new StyleReference(uac));
        XRLog.render("Using CSS implementation from: " + getCss().getClass().getName());
        setTextRenderer(tr);
        setDPI(dpi);
    }

    public void setFormSubmissionListener(FormSubmissionListener fsl) {
        replacedElementFactory.setFormSubmissionListener(fsl);
    }

    public LayoutContext newLayoutContextInstance() {
        LayoutContext c = new LayoutContext(this);
        return c;
    }

    public RenderingContext newRenderingContextInstance() {
        RenderingContext c = new RenderingContext(this);
        return c;
    }

    /*
=========== Font stuff ============== */

    /**
     * Gets the fontResolver attribute of the Context object
     *
     * @return The fontResolver value
     */
    public FontResolver getFontResolver() {
        return font_resolver;
    }

    public void flushFonts() {
        font_resolver.flushCache();
    }

    /**
     * Description of the Field
     */
    protected FontResolver font_resolver;

    /**
     * The media for this context
     */
    public String getMedia() {
        return media;
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
    protected FSCanvas canvas;

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

    public TextRenderer getTextRenderer() {
        return text_renderer;
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


    /*
=========== Selection Management ============== */


    public StyleReference getCss() {
        return css;
    }

    public void setCss(StyleReference css) {
        this.css = css;
    }

    public FSCanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(FSCanvas canvas) {
        this.canvas = canvas;
    }

    public void set_TempCanvas(Rectangle rect) {
        this.temp_canvas = rect;
    }


    public Rectangle getFixedRectangle() {
        //Uu.p("this = " + canvas);
        if (getCanvas() == null) {
            return this.temp_canvas;
        } else {
            Rectangle rect = getCanvas().getFixedRectangle();
            rect.translate(getCanvas().getX(), getCanvas().getY());
            return rect;
        }
    }

    private NamespaceHandler namespaceHandler;

    public void setNamespaceHandler(NamespaceHandler nh) {
        namespaceHandler = nh;
    }

    public NamespaceHandler getNamespaceHandler() {
        return namespaceHandler;
    }

    public void addBoxId(String id, Box box) {
        if (idMap == null) {
            idMap = new HashMap();
        }
        idMap.put(id, box);
    }

    public Box getBoxById(String id) {
        if (idMap == null) {
            idMap = new HashMap();
        }
        return (Box) idMap.get(id);
    }

    public void removeBoxId(String id) {
        if (idMap != null) {
            idMap.remove(id);
        }
    }

    public Map getIdMap()
    {
        return idMap;
    }

    /**
     * Sets the textRenderer attribute of the RenderingContext object
     *
     * @param text_renderer The new textRenderer value
     */
    public void setTextRenderer(TextRenderer text_renderer) {
        this.text_renderer = text_renderer;
    }// = "screen";

    /**
     * <p/>
     * <p/>
     * Set the current media type. This is usually something like <i>screen</i>
     * or <i>print</i> . See the <a href="http://www.w3.org/TR/CSS21/media.html">
     * media section</a> of the CSS 2.1 spec for more information on media
     * types.</p>
     *
     * @param media The new media value
     */
    public void setMedia(String media) {
        this.media = media;
    }

    /**
     * Gets the uac attribute of the RenderingContext object
     *
     * @return The uac value
     */
    public UserAgentCallback getUac() {
        return uac;
    }

    public UserAgentCallback getUserAgentCallback() {
        return uac;
    }

    public void setUserAgentCallback(UserAgentCallback userAgentCallback) {
        StyleReference styleReference = getCss();
        if (styleReference != null) {
            styleReference.setUserAgentCallback(userAgentCallback);
        }
        uac = userAgentCallback;
    }

    /**
     * Gets the dPI attribute of the RenderingContext object
     *
     * @return The dPI value
     */
    public float getDPI() {
        return this.dpi;
    }

    /**
     * Sets the effective DPI (Dots Per Inch) of the screen. You should normally
     * never need to override the dpi, as it is already set to the system
     * default by <code>Toolkit.getDefaultToolkit().getScreenResolution()</code>
     * . You can override the value if you want to scale the fonts for
     * accessibility or printing purposes. Currently the DPI setting only
     * affects font sizing.
     *
     * @param dpi The new dPI value
     */
    public void setDPI(float dpi) {
        this.dpi = dpi;
        this.mm_per_dot = (CM__PER__IN * MM__PER__CM) / dpi;
    }

    /**
     * Gets the dPI attribute in a more useful form of the RenderingContext object
     *
     * @return The dPI value
     */
    public float getMmPerPx() {
        return this.mm_per_dot;
    }

    public FSFont getFont(FontSpecification spec) {
        return getFontResolver().resolveFont(this, spec);
    }

    //strike-through offset should always be half of the height of lowercase x...
    //and it is defined even for fonts without 'x'!
    public float getXHeight(FontContext fontContext, FontSpecification fs) {
        FSFont font = getFontResolver().resolveFont(this, fs);
        FSFontMetrics fm = getTextRenderer().getFSFontMetrics(fontContext, font, " ");
        float sto = fm.getStrikethroughOffset();
        return fm.getAscent() - 2 * Math.abs(sto) + fm.getStrikethroughThickness();
    }

    /**
     * Gets the baseURL attribute of the RenderingContext object
     *
     * @return The baseURL value
     */
    public String getBaseURL() {
        return uac.getBaseURL();
    }

    /**
     * Sets the baseURL attribute of the RenderingContext object
     *
     * @param url The new baseURL value
     */
    public void setBaseURL(String url) {
        uac.setBaseURL(url);
    }

    /**
     * Returns true if the currently set media type is paged. Currently returns
     * true only for <i>print</i> , <i>projection</i> , and <i>embossed</i> ,
     * <i>handheld</i> , and <i>tv</i> . See the <a
     * href="http://www.w3.org/TR/CSS21/media.html">media section</a> of the CSS
     * 2.1 spec for more information on media types.
     *
     * @return The paged value
     */
    public boolean isPaged() {
        if (media.equals("print")) {
            return true;
        }
        if (media.equals("projection")) {
            return true;
        }
        if (media.equals("embossed")) {
            return true;
        }
        if (media.equals("handheld")) {
            return true;
        }
        if (media.equals("tv")) {
            return true;
        }
        return false;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    public boolean isPrint() {
        return print;
    }

    public void setPrint(boolean print) {
        this.print = print;
        if (print) {
            setMedia("print");
        } else {
            setMedia("screen");
        }
    }

    /**
     * <p/>
     * <p/>
     * Adds or overrides a font mapping, meaning you can associate a particular
     * font with a particular string. For example, the following would load a
     * font out of the cool.ttf file and associate it with the name <i>CoolFont
     * </i>:</p> <p/>
     * <p/>
     * <pre>
     *   Font font = Font.createFont(Font.TRUETYPE_FONT,
     *   new FileInputStream("cool.ttf");
     *   setFontMapping("CoolFont", font);
     * </pre> <p/>
     * <p/>
     * <p/>
     * <p/>
     * You could then put the following css in your page </p> <pre>
     *   p { font-family: CoolFont Arial sans-serif; }
     * </pre> <p/>
     * <p/>
     * <p/>
     * <p/>
     * You can also override existing font mappings, like replacing Arial with
     * Helvetica.</p>
     *
     * @param name The new font name
     * @param font The actual Font to map
     */
    /*
     * add a new font mapping, or replace an existing one
     */
    public void setFontMapping(String name, Font font) {
        FontResolver resolver = getFontResolver();
        if (resolver instanceof AWTFontResolver) {
            ((AWTFontResolver)resolver).setFontMapping(name, font);
        }
    }

    public void setFontResolver(FontResolver resolver) {
        font_resolver = resolver;
    }

    public int getDotsPerPixel() {
        return dotsPerPixel;
    }

    public void setDotsPerPixel(int pixelsPerDot) {
        this.dotsPerPixel = pixelsPerDot;
    }

    public CalculatedStyle getStyle(Element e) {
        return getStyle(e, false);
    }

    public CalculatedStyle getStyle(Element e, boolean restyle) {
        if (styleMap == null) {
            styleMap = new HashMap(1024, 0.75f);
        }

        CalculatedStyle result = null;
        if (! restyle) {
            result = (CalculatedStyle)styleMap.get(e);
        }
        if (result == null) {
            Node parent = e.getParentNode();
            CalculatedStyle parentCalculatedStyle;
            if (parent instanceof Document) {
                parentCalculatedStyle = new EmptyStyle();
            } else {
                parentCalculatedStyle = getStyle((Element)parent, false);
            }

            result = parentCalculatedStyle.deriveStyle(getCss().getCascadedStyle(e, restyle));

            styleMap.put(e, result);
        }

        return result;
    }

    public void reset() {
       styleMap = null;
       idMap = null;
       replacedElementFactory.reset();
    }

    public ReplacedElementFactory getReplacedElementFactory() {
        return replacedElementFactory;
    }

    public void setReplacedElementFactory(ReplacedElementFactory ref) {
        if (ref == null) {
            throw new NullPointerException("replacedElementFactory may not be null");
        }

        if (this.replacedElementFactory != null) {
            this.replacedElementFactory.reset();
        }
        this.replacedElementFactory = ref;
    }

    public void removeElementReferences(Element e) {
        String id = namespaceHandler.getID(e);
        if (id != null && id.length() > 0) {
            removeBoxId(id);
        }

        if (styleMap != null) {
            styleMap.remove(e);
        }

        getCss().removeStyle(e);
        getReplacedElementFactory().remove(e);

        if (e.hasChildNodes()) {
            NodeList children = e.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    removeElementReferences((Element)child);
                }
            }
        }
    }

	public LineBreakingStrategy getLineBreakingStrategy() {
		return lineBreakingStrategy;
	}

	public void setLineBreakingStrategy(LineBreakingStrategy lineBreakingStrategy) {
		this.lineBreakingStrategy = lineBreakingStrategy;
	}
}

/*
 *
 * $Log$
 * Revision 1.46  2009/05/08 12:22:30  pdoubleya
 * Merge Vianney's SWT branch to trunk. Passes regress.verify and browser still works :).
 *
 * Revision 1.42  2008/01/22 00:29:24  peterbrant
 * Need to propagate changes to user agent in SharedContext to containing StyleReference
 *
 * Revision 1.41  2007/08/19 22:22:52  peterbrant
 * Merge R8pbrant changes to HEAD
 *
 * Revision 1.39.2.2  2007/08/07 17:06:30  peterbrant
 * Implement named pages / Implement page-break-before/after: left/right / Experiment with efficient selection
 *
 * Revision 1.39.2.1  2007/07/04 14:12:33  peterbrant
 * Permit a custom user agent with rendering to PDF
 *
 * Revision 1.39  2007/05/26 19:04:13  peterbrant
 * Implement support for removing all references to a particular Element (in order to support limited dynamic DOM changes)
 *
 * Revision 1.38  2007/04/16 20:56:49  pdoubleya
 * New image rendering based on PDF rendering approach. Hacked small change in SharedContext which is dependent on panel for sizing, currently.
 *
 * Revision 1.37  2007/04/02 17:08:36  peterbrant
 * Improve method name
 *
 * Revision 1.36  2007/04/02 16:56:20  peterbrant
 * Maintain ReplacedElementFactory across layout runs (to allow component caching to work) / Add reset() to ReplacedElementFactory to allow implementations to clean up after navigating away from a page
 *
 * Revision 1.35  2007/03/17 22:55:51  peterbrant
 * Remove distinction between box IDs and named anchors
 *
 * Revision 1.34  2007/02/22 15:30:43  peterbrant
 * Internal links should be able to target block boxes too (plus other minor cleanup)
 *
 * Revision 1.33  2007/02/20 17:07:13  peterbrant
 * Clean up ex calculation
 *
 * Revision 1.32  2007/02/07 16:33:35  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.31  2006/08/29 17:29:10  peterbrant
 * Make Style object a thing of the past
 *
 * Revision 1.30  2006/08/27 00:35:38  peterbrant
 * Initial commit of (initial) R7 work
 *
 * Revision 1.29  2006/03/01 00:42:52  peterbrant
 * Provide ability to remove named anchors
 *
 * Revision 1.28  2006/02/28 01:31:36  peterbrant
 * Add ability to define PDF bookmarks in header
 *
 * Revision 1.27  2006/02/02 19:25:20  peterbrant
 * Fix (silly) field name mistake
 *
 * Revision 1.26  2006/02/02 13:04:34  peterbrant
 * Make "dots" the fundamental unit of measure, pixels are now some number of dots
 *
 * Revision 1.25  2006/02/01 01:30:12  peterbrant
 * Initial commit of PDF work
 *
 * Revision 1.24  2006/01/27 01:15:30  peterbrant
 * Start on better support for different output devices
 *
 * Revision 1.23  2006/01/01 02:38:15  peterbrant
 * Merge more pagination work / Various minor cleanups
 *
 * Revision 1.22  2005/12/28 00:50:49  peterbrant
 * Continue ripping out first try at pagination / Minor method name refactoring
 *
 * Revision 1.21  2005/12/21 02:36:26  peterbrant
 * - Calculate absolute positions incrementally (prep work for pagination)
 * - Light cleanup
 * - Fix bug where floats nested in floats could cause the outer float to be positioned in the wrong place
 *
 * Revision 1.20  2005/12/07 20:34:46  peterbrant
 * Remove unused fields/methods from RenderingContext / Paint line content using absolute coords (preparation for relative inline layers)
 *
 * Revision 1.19  2005/11/08 01:53:49  tobega
 * Corrected x-height and line-through by taking StrikethroughThickness into account.
 *
 * Revision 1.18  2005/10/27 00:09:01  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.17  2005/09/29 21:34:03  joshy
 * minor updates to a lot of files. pulling in more incremental rendering code.
 * fixed another resize bug
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2005/09/27 23:48:39  joshy
 * first merge of basicpanel reworking and incremental layout. more to come.
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2005/07/18 17:53:32  joshy
 * fixed anchor jumping
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
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
 * Separated current state Context into LayoutContext and the rest into SharedContext.
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
