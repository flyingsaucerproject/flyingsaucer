package org.xhtmlrenderer.extend;

import org.xhtmlrenderer.css.StyleReference;
import org.xhtmlrenderer.css.newmatch.AttributeResolver;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.Java2DTextRenderer;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.util.XRLog;

import java.awt.*;
import java.net.URL;

/**
 * Description of the Class
 *
 * @author jmarinacci
 * @created November 16, 2004
 */
public class RenderingContext {
    /**
     * Constructor for the RenderingContext object
     */
    public RenderingContext(UserAgentCallback uac) {
        setMedia("screen");
        setContext(new SharedContext());
        getContext().setCtx(this);
        this.uac = uac;
        getContext().setCss(new StyleReference(uac));
        XRLog.render("Using CSS implementation from: " + getContext().getCss().getClass().getName());
        setTextRenderer(new Java2DTextRenderer());
    }

    public RenderingContext() {
        this(new NaiveUserAgent());
    }

    protected UserAgentCallback uac;

    public UserAgentCallback getUac() {
        return uac;
    }

    /**
     * Description of the Field
     */
    protected SharedContext ctx;


    /**
     * Gets the context attribute of the RenderingContext object
     *
     * @return The context value
     */
    public SharedContext getContext() {
        return ctx;
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
     * Internal member variable for the css module
     */
    protected StyleReference css;


    /**
     * Sets the StyleReference implemenation. This is part of the CSS
     * module. Developers should normally never need to call this
     *
     * @return The styleReference value
     */
    public StyleReference getStyleReference() {
        return ctx.getCss();
    }


    /**
     * <b>need to make this protected</b>
     */
    protected Box root_box;


    /**
     * Gets the root of the fully rendered box tree
     *
     * @return The rootBox value
     */
    public Box getRootBox() {
        return root_box;
    }

    public void setRootBox(Box root_box) {
        this.root_box = root_box;
    }


    /**
     * Stores the attribute resolver. Only for use by subclasses. You should
     * normally call getAttributeResolver() and setAttributeResolver().
     */
    /*
     *  should this happen here or lower down?
     */
    protected AttributeResolver attr_res;


    /**
     * Sets the attributeResolver attribute of the RenderingContext object
     *
     * @param attribute_resolver The new attributeResolver value
     */
    public void setAttributeResolver(AttributeResolver attribute_resolver) {
        this.attr_res = attribute_resolver;
    }


    /**
     * <p/>
     * Adds or overrides a font mapping, meaning you can associate a particular
     * font with a particular string. For example, the following would load a
     * font out of the cool.ttf file and associate it with the name <i>CoolFont
     * </i>:</p>
     * <p/>
     * <pre>
     *   Font font = Font.createFont(Font.TRUETYPE_FONT,
     *   new FileInputStream("cool.ttf");
     *   setFontMapping("CoolFont", font);
     * </pre>
     * <p/>
     * <p>You could then put the following css in your page </p>
     * <pre>
     *   p { font-family: CoolFont Arial sans-serif; }
     * </pre>
     * <p/>
     * <p>You can also override existing font mappings, like replacing Arial
     * with Helvetica.</p>
     *
     * @param name The new font name
     * @param font The actual Font to map
     */
    /*
     *  add a new font mapping, or replace an existing one
     */
    public void setFontMapping(String name, Font font) {
        getContext().getFontResolver().setFontMapping(name, font);
    }


    /**
     * Description of the Field
     */
    protected URL base_url;


    /**
     * Gets the baseURL attribute of the RenderingContext object
     *
     * @return The baseURL value
     */
    public URL getBaseURL() {
        return base_url;
    }


    /**
     * Sets the baseURL attribute of the RenderingContext object
     *
     * @param url The new baseURL value
     */
    public void setBaseURL(URL url) {
        base_url = url;
    }


    /*
     *  used to adjust fonts, ems, points, into screen resolution
     */
    private float dpi = Toolkit.getDefaultToolkit().getScreenResolution();


    /**
     * Sets the effective DPI (Dots Per Inch) of the screen. You should normally
     * never need to override the dpi, as it is already
     * set to the system default by <code>Toolkit.getDefaultToolkit().getScreenResolution()</code>.
     * You can override the value if you want to scale the fonts for accessibility or printing
     * purposes. Currently the DPI setting only affects font sizing.
     *
     * @param dpi The new dPI value
     */
    public void setDPI(float dpi) {
        this.dpi = dpi;
    }


    /**
     * Gets the dPI attribute of the RenderingContext object
     *
     * @return The dPI value
     */
    public float getDPI() {
        return this.dpi;
    }


    /*
     *  is this really a property of the component that uses
     *  this rendering context ??
     */
    /**
     * Description of the Field
     */
    protected boolean threaded_layout;


    /**
     * Sets the threadedLayout attribute of the RenderingContext object
     *
     * @param threaded The new threadedLayout value
     */
    public void setThreadedLayout(boolean threaded) {
        threaded_layout = threaded;
    }


    /**
     * Description of the Field
     */
    protected TextRenderer text_renderer;


    /**
     * Gets the textRenderer attribute of the RenderingContext object
     *
     * @return The textRenderer value
     */
    public TextRenderer getTextRenderer() {
        return text_renderer;
    }


    /**
     * Sets the textRenderer attribute of the RenderingContext object
     *
     * @param text_renderer The new textRenderer value
     */
    public void setTextRenderer(TextRenderer text_renderer) {
        this.text_renderer = text_renderer;
    }


    /**
     * The member variable which holds the currently
     * media setting.
     */
    protected String media;// = "screen";

    /**
     * <p>Set the current media type. This is usually something
     * like <i>screen</i> or <i>print</i>. See the
     * <a href="http://www.w3.org/TR/CSS21/media.html">media section</a>
     * of the CSS 2.1 spec for more information on media types.</p>
     */
    public void setMedia(String media) {
        this.media = media;
    }

    public String getMedia() {
        return this.media;
    }

    /**
     * Returns true if the currently set media type is paged. Currently
     * returns true only for <i>print</i>, <i>projection</i>, and
     * <i>embossed</i>, <i>handheld</i>, and <i>tv</i>.
     * See the
     * <a href="http://www.w3.org/TR/CSS21/media.html">media section</a>
     * of the CSS 2.1 spec for more information on media types.
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
    
    /*
     *  utility methods
     */
    /*
     *  public void addUserCSS(File file) {
     *  }
     *  public void removeUserCSS(File file) {
     *  }
     *  public void removeAllUserCSS(File file) {
     *  }
     */
    /*
     *  *replaces* default.css
     */
    /*
     *  public void setUserAgentCSS(File file) {
     *  }
     */
    /*
     *  query and set whether the renderer should display or save
     */
    /*
     *  public boolean isTooltipsDisplayed() {
     *  }
     *  public void setTooltipsDisplayed(boolean tooltips) {
     *  }
     */
    /*
     *  other features.
     *  set antialiasing
     *  set doctypes
     *  callbacks for validation
     *  callbacks for resource provider
     */
    /*
     *  turn on validation
     */
    /*
     *  public void setValidating(boolean validate) {
     *  }
     *  public boolean isValidating() {
     *  return validate;
     *  }
     */
    /*
     *  override the default (xerces?)
     */
    /*
     *  public void setDomImplementation(String dom) {
     *  }
     */
    /*
     *  public void setLogging(boolean logging) { }
     */

    /*
    *  the default is browser, but you could change it to
    *  aural, paged, print, tv, slideshow, etc.
    */
     
    /*
     *  public void setProperties(Properties props) { }
     *  public void setProperty(String name, String value) { }
     *  public Properties getProperties() { }
     */
}

