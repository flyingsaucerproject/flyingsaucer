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

/* should all of these properties be here? should some only be specified by the
conf/props files? */

public class RenderingContext {
    public RenderingContext() {
        setContext(new Context());
        getContext().css = new TBStyleReference(new NaiveUserAgent());
        XRLog.render( "Using CSS implementation from: " + getContext().css.getClass().getName() );
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
    /* utility methods */
    /* you will need to re-layout the document after you call these
     method. */
     /*
    public void setFontScale(float scale) {
    }
    public void incrementFontScale() {
    }
    public void decrementFontScale() {
    }
    public void setFontIncrementStepValue(float inc) {
    }
    */
    
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
    
    
    /* should this happen here or lower down? */
    protected AttributeResolver attr_res;
    public void setAttributeResolver(AttributeResolver attribute_resolver) {
        this.attr_res = attribute_resolver;
    }
    
    /* other features.
        set antialiasing
        set doctypes
        callbacks for validation
        callbacks for resource provider
    */
    
    /* add a new font mapping, or replace an existing one */
    public void setFontMapping(String name, Font font) {
        getContext().getFontResolver().setFontMapping(name, font);
    }
    
    /* should we reverse the order of these to match name=value ? */
    /*
    public void addLayoutByDisplayValue(Layout layout, String display) {
    }
    public void addLayoutByElementName(Layout layout, String element_name) {
    }
    public void addRendererByDisplayValue(Layout layout, String display) {
    }
    public void addRendererByElementName(Layout layout, String element_name) {
    }
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
    
    
    
    /* ??? do we need a new interface for font renderer */
    /* public void setFontRenderer(FontRenderer fr) { } */
    /* set to -1 for no antialiasing. set to 0 for all antialising.
    else, set to the threshold font size. does not take font scaling
    into account. */
    
    /*
    public void setAntiAliasedSizeThreshold(float fontsize) { }
    */
    
    
    /* used to adjust fonts, ems, points, into screen resolution */
    /* uses system default by default (can we get that?) */
    /* this should fix the reason firefox looks different */
    /*
    public void setDPI() { }
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
    
    
    
    /* is this really a property of the component that uses
    this rendering context ?? */
    protected boolean threaded_layout;
    public void setThreadedLayout(boolean threaded) { 
        threaded_layout = threaded;
    }

}
