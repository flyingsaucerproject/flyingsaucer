/*
 * RenderingContext.java
 * Copyright (c) 2004, 2005 Josh Marinacci
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
 *
 */
package org.xhtmlrenderer.extend;

import org.xhtmlrenderer.context.FontResolver;
import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.Java2DTextRenderer;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.util.XRLog;

import java.awt.*;


/**
 * Supplies information about the context in which rendering will take place
 *
 * @author jmarinacci
 *         November 16, 2004
 */
public class RenderingContext implements CssContext {

    /**
     * Description of the Field
     */
    protected UserAgentCallback uac;

    /**
     * Description of the Field
     */
    protected SharedContext ctx;

    /**
     * <b>need to make this protected</b>
     */
    protected Box root_box;

    /**
     * Description of the Field
     */
    protected TextRenderer text_renderer;

    /**
     * The member variable which holds the currently media setting.
     */
    protected String media;

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
    private float mm_per_px;

    private final static float DEFAULT_DPI = 72;

    /**
     * Constructor for the RenderingContext object
     *
     * @param uac PARAM
     */
    public RenderingContext(UserAgentCallback uac) {
        setMedia("screen");
        setContext(new SharedContext());
        getContext().setCtx(this);
        this.uac = uac;
        getContext().setCss(new StyleReference(uac));
        XRLog.render("Using CSS implementation from: " + getContext().getCss().getClass().getName());
        setTextRenderer(new Java2DTextRenderer());
        try {
            setDPI(Toolkit.getDefaultToolkit().getScreenResolution());
        } catch (HeadlessException e) {
            setDPI(DEFAULT_DPI);
        }
    }

    /**
     * Constructor for the RenderingContext object
     */
    public RenderingContext() {
        this(new NaiveUserAgent());
    }


    /**
     * Sets the context attribute of the RenderingContext object
     *
     * @param ctx The new context value
     */
    public void setContext(SharedContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Sets the rootBox attribute of the RenderingContext object
     *
     * @param root_box The new rootBox value
     */
    public void setRootBox(Box root_box) {
        this.root_box = root_box;
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
        getContext().getFontResolver().setFontMapping(name, font);
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
        this.mm_per_px = (CM__PER__IN * MM__PER__CM) / dpi;
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


    /**
     * Gets the context attribute of the RenderingContext object
     *
     * @return The context value
     */
    public SharedContext getContext() {
        return ctx;
    }


    /**
     * Sets the StyleReference implemenation. This is part of the CSS module.
     * Developers should normally never need to call this
     *
     * @return The styleReference value
     */
    public StyleReference getStyleReference() {
        return ctx.getCss();
    }


    /**
     * Gets the root of the fully rendered box tree
     *
     * @return The rootBox value
     */
    public Box getRootBox() {
        return root_box;
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
     * Gets the dPI attribute of the RenderingContext object
     *
     * @return The dPI value
     */
    public float getDPI() {
        return this.dpi;
    }

    /**
     * Gets the dPI attribute in a more useful form of the RenderingContext object
     *
     * @return The dPI value
     */
    public float getMmPerPx() {
        return this.mm_per_px;
    }

    public float getFontSize2D(FontSpecification font) {
        return getFontResolver().resolveFont(this, font).getSize2D();
    }


    /**
     * Gets the textRenderer attribute of the RenderingContext object
     *
     * @return The textRenderer value
     */
    public TextRenderer getTextRenderer() {
        return text_renderer;
    }

    /**
     * Gets the media attribute of the RenderingContext object
     *
     * @return The media value
     */
    public String getMedia() {
        return this.media;
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

    public Graphics2D getGraphics() {
        return ctx.getGraphics();
    }

    public FontResolver getFontResolver() {
        return ctx.getFontResolver();
    }

    public float getFontSizeForXHeight(FontSpecification parent, FontSpecification desired, float xHeight) {
        float bestGuess = getFontSize2D(parent);
        float bestHeight = getXHeight(parent);
        float nextGuess = bestGuess * xHeight / bestHeight;
        while (true) {
            desired.size = nextGuess;
            float nextHeight = getXHeight(desired);
            //this check is needed in cases where the iteration can hop back and forth between two values
            if (Math.abs(nextHeight - xHeight) < Math.abs(bestHeight - xHeight)) {
                bestGuess = nextGuess;
                bestHeight = nextHeight;
                nextGuess = bestGuess * xHeight / nextHeight;
            } else
                break;
        }
        return bestGuess;
    }

    //strike-through offset should always be half of the height of lowercase x...
    //and it is defined even for fonts without 'x'!
    public float getXHeight(FontSpecification fs) {
        Font f = getFontResolver().resolveFont(this, fs.families, fs.size, fs.fontWeight, fs.fontStyle, fs.variant);
        float sto = getTextRenderer().getLineMetrics(getGraphics(), f, " ").getStrikethroughOffset();
        return 2 * Math.abs(sto);
    }

    /*
     * utility methods
     */
    /*
     * public void addUserCSS(File file) {
     * }
     * public void removeUserCSS(File file) {
     * }
     * public void removeAllUserCSS(File file) {
     * }
     */
    /*
     * *replaces* default.css
     */
    /*
     * public void setUserAgentCSS(File file) {
     * }
     */
    /*
     * query and set whether the renderer should display or save
     */
    /*
     * public boolean isTooltipsDisplayed() {
     * }
     * public void setTooltipsDisplayed(boolean tooltips) {
     * }
     */
    /*
     * other features.
     * set antialiasing
     * set doctypes
     * callbacks for validation
     * callbacks for resource provider
     */
    /*
     * turn on validation
     */
    /*
     * public void setValidating(boolean validate) {
     * }
     * public boolean isValidating() {
     * return validate;
     * }
     */
    /*
     * override the default (xerces?)
     */
    /*
     * public void setDomImplementation(String dom) {
     * }
     */
    /*
     * public void setLogging(boolean logging) { }
     */
    /*
     * the default is browser, but you could change it to
     * aural, paged, print, tv, slideshow, etc.
     */
    /*
     * public void setProperties(Properties props) { }
     * public void setProperty(String name, String value) { }
     * public Properties getProperties() { }
     */
    public Font getFont(FontSpecification font) {
        return getFontResolver().resolveFont(this, font);
    }
}

