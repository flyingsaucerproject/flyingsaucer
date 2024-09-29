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
import org.xhtmlrenderer.extend.OutputDevice;
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

import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * The SharedContext is that which is kept between successive layout and render runs.
 *
 * @author empty
 */
public final class SharedContext {
    private static final Set<String> PAGED_MEDIA_TYPES =
            new HashSet<>(asList("print", "projection", "embossed", "handheld", "tv"));

    private TextRenderer textRenderer;
    private String media;
    private UserAgentCallback uac;
    private boolean interactive = true;
    private final Map<String, Box> idMap = new HashMap<>();

    /*
     * used to adjust fonts, ems, points, into screen resolution
     */
    private float dpi;
    private static final int MM__PER__CM = 10;
    private static final float CM__PER__IN = 2.54F;
    /**
     * dpi in a more usable way
     */
    private float mm_per_dot;

    private static final float DEFAULT_DPI = 72;
    private boolean print;

    private int dotsPerPixel = 1;

    private Map<Element, CalculatedStyle> styleMap;

    private ReplacedElementFactory replacedElementFactory;
    private Rectangle temporaryCanvas;

    private LineBreakingStrategy lineBreakingStrategy = new DefaultLineBreakingStrategy();

    public SharedContext() {
    }

    public SharedContext(UserAgentCallback uac) {
        fontResolver = new AWTFontResolver();
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


    public SharedContext(UserAgentCallback uac, FontResolver fr, ReplacedElementFactory ref, TextRenderer tr, float dpi) {
        fontResolver = fr;
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
        return new LayoutContext(this);
    }

    public RenderingContext newRenderingContextInstance(OutputDevice outputDevice, FontContext fontContext) {
        return newRenderingContextInstance(outputDevice, fontContext, 0);
    }

    public RenderingContext newRenderingContextInstance(OutputDevice outputDevice, FontContext fontContext, int initialPageNo) {
        return new RenderingContext(this, outputDevice, fontContext, initialPageNo);
    }

    /*
=========== Font stuff ============== */

    /**
     * Gets the fontResolver attribute of the Context object
     *
     * @return The fontResolver value
     */
    public FontResolver getFontResolver() {
        return fontResolver;
    }

    public void flushFonts() {
        fontResolver.flushCache();
    }

    private FontResolver fontResolver;

    /**
     * The media for this context
     */
    public String getMedia() {
        return media;
    }

    private StyleReference css;
    private boolean debug_draw_boxes;
    private boolean debug_draw_line_boxes;
    private boolean debug_draw_inline_boxes;
    private boolean debug_draw_font_metrics;

    private FSCanvas canvas;

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public boolean debugDrawBoxes() {
        return debug_draw_boxes;
    }

    public boolean debugDrawLineBoxes() {
        return debug_draw_line_boxes;
    }

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

    public void setTemporaryCanvas(Rectangle rect) {
        temporaryCanvas = rect;
    }


    public Rectangle getFixedRectangle() {
        if (getCanvas() == null) {
            return temporaryCanvas;
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
        idMap.put(id, box);
    }

    public Box getBoxById(String id) {
        return idMap.get(id);
    }

    public void removeBoxId(String id) {
        idMap.remove(id);
    }

    public Map<String, Box> getIdMap()
    {
        return idMap;
    }

    /**
     * Sets the textRenderer attribute of the RenderingContext object
     *
     * @param textRenderer The new textRenderer value
     */
    public void setTextRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    /**
     * <p>
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
        return dpi;
    }

    /**
     * Sets the effective DPI (Dots Per Inch) of the screen. You should normally
     * never need to override the dpi, as it is already set to the system
     * default by {@code Toolkit.getDefaultToolkit().getScreenResolution()}
     * . You can override the value if you want to scale the fonts for
     * accessibility or printing purposes. Currently, the DPI setting only
     * affects font sizing.
     *
     * @param dpi The new dPI value
     */
    public void setDPI(float dpi) {
        this.dpi = dpi;
        mm_per_dot = (CM__PER__IN * MM__PER__CM) / dpi;
    }

    /**
     * Gets the dPI attribute in a more useful form of the RenderingContext object
     *
     * @return The dPI value
     */
    public float getMmPerPx() {
        return mm_per_dot;
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
    public void setBaseURL(@Nullable String url) {
        uac.setBaseURL(url);
    }

    /**
     * Returns true if the currently set media type is paged. Currently, returns
     * true only for <i>print</i> , <i>projection</i> , and <i>embossed</i> ,
     * <i>handheld</i> , and <i>tv</i> . See the <a
     * href="http://www.w3.org/TR/CSS21/media.html">media section</a> of the CSS
     * 2.1 spec for more information on media types.
     *
     * @return The paged value
     */
    public boolean isPaged() {
        return PAGED_MEDIA_TYPES.contains(media);
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
     * <p>
     * Adds or overrides a font mapping, meaning you can associate a particular
     * font with a particular string. For example, the following would load a
     * font out of the cool.ttf file and associate it with the name <i>CoolFont
     * </i>:</p>
     *
     * <pre>
     *   Font font = Font.createFont(Font.TRUETYPE_FONT,
     *   new FileInputStream("cool.ttf");
     *   setFontMapping("CoolFont", font);
     * </pre>
     * <p>
     * You could then put the following css in your page </p> <pre>
     *   p { font-family: CoolFont Arial sans-serif; }
     * </pre>
     * <p>
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
        fontResolver = resolver;
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
        Map<Element, CalculatedStyle> localMap = styleMap;

        if (localMap == null) {
            localMap = new HashMap<>(1024, 0.75f);
        }

        CalculatedStyle result = null;
        if (! restyle) {
            result = localMap.get(e);
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

            localMap.put(e, result);
        }

        styleMap = localMap;

        return result;
    }

    public void reset() {
       styleMap = null;
       idMap.clear();
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
        if (id != null && !id.isEmpty()) {
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
