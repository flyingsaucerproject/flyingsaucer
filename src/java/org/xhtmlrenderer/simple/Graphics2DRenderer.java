package org.xhtmlrenderer.simple;
import org.w3c.dom.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Dimension;
import java.io.*;
import java.net.*;
import org.xhtmlrenderer.extend.*;

/**

  Graphics2DRenderer supports headless rendering of XHTML documents.

*/
public class Graphics2DRenderer {
    protected XHTMLPanel panel;
    
    public Graphics2DRenderer() {
        panel = new XHTMLPanel();
        panel.setThreadedLayout(false);
    }
    
    
    /* set the document to be rendered. */
    public void setDocument(URL url) throws Exception {
        panel.setDocument(url);
    }
    public void setDocument(Document doc, URL base_url) {
        panel.setDocument(doc,base_url);
    }
    
    /* lay out the document with the desired size. don't paint though. */
    /* maybe we could change the graphics2d to be a font rendering context? */
    public void layout(Graphics2D g2, Dimension dim) {
        panel.setSize(dim);
    }
    /* return the size image needed to render the document without anything
    going off the side. *could be different than the dimensions passed into layout
    because of text that couldn't break or a table that's set to be too big */
    /*
    public Dimension getMinimumSize() {
        return null;
    }
    */
    
    /* actually draw the */
    public void render(Graphics2D g2) {
        panel.paintComponent(g2);
    }
    
    /* reset the internal variables so you can reuse this class */
    /*
    public void reset() {
    }
    */
    
    
    public RenderingContext getRenderingContext() {
        return panel.getRenderingContext();
    }
    public void setRenderingContext(RenderingContext ctx) {
        panel.setRenderingContext(ctx);
    }
    
    /* a static utility method to automatically create an image
    the right size, then layout and render the document */
    /*
    public static Image renderToImage(URL url) {
        return null;
    }
    */

}
