package org.xhtmlrenderer.extend;

import org.w3c.dom.*;
import java.io.*;
import java.awt.*;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.css.*;
import org.xhtmlrenderer.css.bridge.TBStyleReference;
import org.xhtmlrenderer.swing.*;
import org.xhtmlrenderer.util.*;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.Java2DTextRenderer;
import org.xhtmlrenderer.layout.LayoutFactory;
import java.net.*;

public class RenderingContext {
    public RenderingContext() {
        setContext(new Context());
        getContext().ctx = this;
        getContext().css = new TBStyleReference(new NaiveUserAgent());
        XRLog.render( "Using CSS implementation from: " + getContext().css.getClass().getName() );
        layout_factory = new LayoutFactory();
        setTextRenderer(new Java2DTextRenderer());
    }
    protected Context ctx;
    public Context getContext() {
        return ctx;
    }
    public void setContext(Context ctx) {
        this.ctx = ctx;
    }
    protected StyleReference css;
    public StyleReference getStyleReference() {
        return ctx.css;
    }
    
    public Box root_box;
    public Box getRootBox() {
        return root_box;
    }


    /* should this happen here or lower down? */
    protected AttributeResolver attr_res;
    public void setAttributeResolver(AttributeResolver attribute_resolver) {
        this.attr_res = attribute_resolver;
    }


    /* add a new font mapping, or replace an existing one */
    public void setFontMapping(String name, Font font) {
        getContext().getFontResolver().setFontMapping(name, font);
    }
    

    protected URL base_url;
    public URL getBaseURL() {
        return base_url;
    }
    public void setBaseURL(URL url) {
        base_url = url;
    }

    protected LayoutFactory layout_factory;
    public LayoutFactory getLayoutFactory() {
        return layout_factory;
    }
    public void setLayoutFactory(LayoutFactory layout_factory) {
        this.layout_factory = layout_factory;
    }
    
    /* used to adjust fonts, ems, points, into screen resolution */
    /* uses system default by default (can we get that?) */
    /* this should fix the reason firefox looks different */
    
    private float dpi = Toolkit.getDefaultToolkit().getScreenResolution();
    
    public void setDPI(float dpi) {
        this.dpi = dpi;
    }
    
    public float getDPI() {
        return this.dpi;
    }

    /* is this really a property of the component that uses
    this rendering context ?? */
    protected boolean threaded_layout;
    public void setThreadedLayout(boolean threaded) { 
        threaded_layout = threaded;
    }

    protected TextRenderer text_renderer;
    public TextRenderer getTextRenderer() {
        return text_renderer;
    }
    public void setTextRenderer(TextRenderer text_renderer) {
        this.text_renderer = text_renderer;
    }
    
    /*
    public void incrementFontScale() {
    }
    public void decrementFontScale() {
    }
    public void setFontIncrementStepValue(float inc) {
    }
    */

    
    /* utility methods */
    /* you will need to re-layout the document after you call these
     method. */

     /* what type should this take?*/
    /*
    public void addUserCSS(File file) {
    }
    public void removeUserCSS(File file) {
    }
    public void removeAllUserCSS(File file) {
    }*/
    /* *replaces* default.css */
    /*
    public void setUserAgentCSS(File file) {
    }
    */
    
    
    /* query and set whether the renderer should display or save
    */
    /*
    public boolean isTooltipsDisplayed() {
    }
    public void setTooltipsDisplayed(boolean tooltips) {
    }
    */
    
    
    
    /* other features.
        set antialiasing
        set doctypes
        callbacks for validation
        callbacks for resource provider
    */
    
    
    /* turn on validation */
    /*
    public void setValidating(boolean validate) {
    }
    public boolean isValidating() {
        return validate;
    }
    */
    
    
    
    /* override the default (xerces?) */
    /*
    public void setDomImplementation(String dom) {
    }
    */
    
    
    
    /*
    public void setLogging(boolean logging) { }
    */
    
    
    /* the default is browser, but you could change it to
     aural, paged, print, tv, slideshow, etc. should this be
     in the rendering context instead? */
     /*
    public void setMediaType(String media) {
    }
    */
    /* are all of these variations overkill? */
    /*
    public void addMediaType(String type) {
    }
    public void addMediaTypes(String[] types) {
    }
    public String[] getMediaTypes() {
    }
    public void clearMediaTypes() {
    }
    */
    
    
    
    /*
    public void setProperties(Properties props) { }
    public void setProperty(String name, String value) { }
    public Properties getProperties() { }
    */
    
    
    
}
